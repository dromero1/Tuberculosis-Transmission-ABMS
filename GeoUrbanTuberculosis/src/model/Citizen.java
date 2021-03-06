package model;

import java.util.ArrayList;
import java.util.List;
import calibration.Calibrator;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.collections.Pair;
import simulation.EventScheduler;
import simulation.SimulationBuilder;
import util.TickConverter;

public class Citizen {

	/**
	 * Particle expelling interval (unit: hours)
	 */
	public static final int PARTICLE_EXPELLING_INTERVAL = 1;

	/**
	 * Ticks between steps (unit: hours)
	 */
	public static final int TICKS_BETWEEN_STEPS = 1;

	/**
	 * Displacement per step (unit: grid step)
	 */
	public static final double DISPLACEMENT_PER_STEP = 1;

	/**
	 * Household
	 */
	private NdPoint household;

	/**
	 * Workplace
	 */
	private NdPoint workplace;

	/**
	 * Wake up time
	 */
	private double wakeUpTime;

	/**
	 * Returning home time
	 */
	private double returningHomeTime;

	/**
	 * Compartment
	 */
	private Compartment compartment;

	/**
	 * Was the citizen initially exposed to the disease?
	 */
	private boolean wasInitiallyExposed;

	/**
	 * Is the citizen immunosuppressed?
	 */
	private boolean isImmunosuppressed;

	/**
	 * Does the citizen smoke?
	 */
	private boolean smokes;

	/**
	 * Does the citizen drink alcohol?
	 */
	private boolean drinksAlcohol;

	/**
	 * Has the citizen notified the infection?
	 */
	private boolean hasNotifiedInfection;

	/**
	 * Has the citizen notified the exposure?
	 */
	private boolean hasNotifiedExposure;

	/**
	 * Reference to simulation builder
	 */
	private SimulationBuilder simulationBuilder;

	/**
	 * Scheduled particle expelling action
	 */
	private ISchedulableAction expelAction;

	/**
	 * Scheduled actions
	 */
	private List<ISchedulableAction> scheduledActions;

	/**
	 * Create a new citizen agent
	 * 
	 * @param simulationBuilder   Simulation builder
	 * @param wasInitiallyExposed Was the citizen initially exposed to the
	 *                            disease?
	 */
	public Citizen(SimulationBuilder simulationBuilder,
			boolean wasInitiallyExposed) {
		this.simulationBuilder = simulationBuilder;
		this.wasInitiallyExposed = wasInitiallyExposed;
		this.scheduledActions = new ArrayList<>();
		this.compartment = Compartment.SUSCEPTIBLE;
	}

	/**
	 * Initialize
	 */
	@ScheduledMethod(start = 0, interval = Calibrator.TICKS_PER_RUN, priority = 1)
	public void init() {
		this.wakeUpTime = Randomizer.getRandomWakeUpTime();
		this.returningHomeTime = Randomizer.getRandomReturningHomeTime();
		this.isImmunosuppressed = Randomizer.getRandomImmunodeficiency(
				this.simulationBuilder.parametersAdapter);
		this.smokes = Randomizer
				.getRandomSmoker(this.simulationBuilder.parametersAdapter);
		this.drinksAlcohol = Randomizer.getRandomAlcoholDrinker(
				this.simulationBuilder.parametersAdapter);
		this.hasNotifiedExposure = false;
		this.hasNotifiedInfection = false;
		unscheduleProgrammedEvents();
	}

	/**
	 * Start
	 */
	@ScheduledMethod(start = 1, interval = Calibrator.TICKS_PER_RUN
			+ Calibrator.TICKS_BETWEEN_RUNS)
	public void start() {
		initDisease();
		assignReferenceLocations();
		scheduleRecurringEvents();
		goHome();
	}

	/**
	 * Step
	 */
	public void step() {
		randomWalk();
	}

	/**
	 * Expel particles
	 */
	public void expelParticles() {
		infect();
	}

	/**
	 * Move to workplace
	 */
	public void goWork() {
		goTo(this.workplace);
	}

	/**
	 * Move to household
	 */
	public void goHome() {
		goTo(this.household);
	}

	/**
	 * Transition to the susceptible compartment
	 */
	public void transitionToSusceptible() {
		this.compartment = Compartment.SUSCEPTIBLE;
	}

	/**
	 * Transition to the exposed compartment
	 * 
	 * @param isInitialSetup Is initial setup?
	 */
	public void transitionToExposed(boolean isInitialSetup) {
		this.compartment = Compartment.EXPOSED;
		// Determine disease course
		if (Randomizer.isGettingInfected(this,
				this.simulationBuilder.parametersAdapter) || isInitialSetup) {
			double incubationPeriod = Randomizer.getRandomIncubationPeriod();
			double ticks = TickConverter.daysToTicks(incubationPeriod);
			EventScheduler eventScheduler = EventScheduler.getInstance();
			ISchedulableAction action = eventScheduler
					.scheduleOneTimeEvent(ticks, this, "transitionToInfected");
			this.scheduledActions.add(action);
		} else {
			transitionToSusceptible();
		}
		// Notify exposure
		if (!this.hasNotifiedExposure) {
			this.simulationBuilder.outputManager.onNewExposure();
			this.hasNotifiedExposure = true;
		}
	}

	/**
	 * Transition to the infected compartment
	 */
	public void transitionToInfected() {
		this.compartment = Compartment.INFECTED;
		// Schedule particle expelling
		EventScheduler eventScheduler = EventScheduler.getInstance();
		this.expelAction = eventScheduler.scheduleRecurringEvent(1, this,
				PARTICLE_EXPELLING_INTERVAL, "expelParticles");
		this.scheduledActions.add(this.expelAction);
		// Schedule diagnosis
		double daysToDiagnosis = Randomizer.getRandomDaysToDiagnosis(
				this.simulationBuilder.parametersAdapter);
		double ticks = TickConverter.daysToTicks(daysToDiagnosis);
		ISchedulableAction action = eventScheduler.scheduleOneTimeEvent(ticks,
				this, "transitionToOnTreament");
		this.scheduledActions.add(action);
		// Notify infection
		if (!this.hasNotifiedInfection) {
			this.simulationBuilder.outputManager.onNewInfection();
			this.hasNotifiedInfection = true;
		}
	}

	/**
	 * Transition to the on treatment compartment
	 */
	public void transitionToOnTreament() {
		this.compartment = Compartment.ON_TREATMENT;
		// Unschedule particle expelling
		unscheduleAction(this.expelAction);
		this.scheduledActions.remove(this.expelAction);
		// Schedule treatment dropout or recovery
		if (Randomizer.isDroppingOutTreatment(
				this.simulationBuilder.parametersAdapter)) {
			transitionToInfected();
		} else {
			double treatmentDuration = Randomizer.getRandomTreatmentDuration();
			double ticks = TickConverter.daysToTicks(treatmentDuration);
			EventScheduler eventScheduler = EventScheduler.getInstance();
			ISchedulableAction action = eventScheduler
					.scheduleOneTimeEvent(ticks, this, "transitionToImmune");
			this.scheduledActions.add(action);
		}
	}

	/**
	 * Transition to the immune compartment
	 */
	public void transitionToImmune() {
		this.compartment = Compartment.IMMUNE;
		// Schedule full recovery
		double daysToFullRecovery = Randomizer.getRandomDaysToFullRecovery();
		double ticks = TickConverter.daysToTicks(daysToFullRecovery);
		EventScheduler eventScheduler = EventScheduler.getInstance();
		ISchedulableAction action = eventScheduler.scheduleOneTimeEvent(ticks,
				this, "transitionToSusceptible");
		this.scheduledActions.add(action);
	}

	/**
	 * Get compartment
	 */
	public Compartment getCompartment() {
		return this.compartment;
	}

	/**
	 * Smokes?
	 */
	public boolean smokes() {
		return this.smokes;
	}

	/**
	 * Drinks alcohol?
	 */
	public boolean drinksAlcohol() {
		return this.drinksAlcohol;
	}

	/**
	 * Is immunodepressed?
	 */
	public boolean isImmunodepressed() {
		return this.isImmunosuppressed;
	}

	/**
	 * Is susceptible?
	 */
	public int isSusceptible() {
		return (this.compartment == Compartment.SUSCEPTIBLE) ? 1 : 0;
	}

	/**
	 * Is exposed?
	 */
	public int isExposed() {
		return (this.compartment == Compartment.EXPOSED) ? 1 : 0;
	}

	/**
	 * Is infected?
	 */
	public int isInfected() {
		return (this.compartment == Compartment.INFECTED) ? 1 : 0;
	}

	/**
	 * Is immune?
	 */
	public int isImmune() {
		return (this.compartment == Compartment.IMMUNE) ? 1 : 0;
	}

	/**
	 * Is on treatment?
	 */
	public int isOnTreatment() {
		return (this.compartment == Compartment.ON_TREATMENT) ? 1 : 0;
	}

	/**
	 * Walk randomly
	 */
	private void randomWalk() {
		double x = RandomHelper.nextDoubleFromTo(-DISPLACEMENT_PER_STEP,
				DISPLACEMENT_PER_STEP);
		double y = RandomHelper.nextDoubleFromTo(-DISPLACEMENT_PER_STEP,
				DISPLACEMENT_PER_STEP);
		NdPoint nextLocation = this.simulationBuilder.space
				.moveByDisplacement(this, x, y);
		goTo(nextLocation);
	}

	/**
	 * Infect nearby susceptible individuals
	 */
	private void infect() {
		GridPoint pt = this.simulationBuilder.grid.getLocation(this);
		GridCellNgh<Citizen> nghCreator = new GridCellNgh<>(
				this.simulationBuilder.grid, pt, Citizen.class, 0, 0);
		List<GridCell<Citizen>> gridCells = nghCreator.getNeighborhood(true);
		for (GridCell<Citizen> cell : gridCells) {
			int infectedCount = countInfectedPeople(cell.items());
			for (Citizen citizen : cell.items()) {
				if (citizen.compartment == Compartment.SUSCEPTIBLE
						&& Randomizer.isGettingExposed(infectedCount,
								this.simulationBuilder.parametersAdapter)) {
					citizen.transitionToExposed(false);
				}
			}
		}
	}

	/**
	 * Unschedule programmed events
	 */
	private void unscheduleProgrammedEvents() {
		for (int i = 0; i < this.scheduledActions.size(); i++) {
			ISchedulableAction action = this.scheduledActions.get(i);
			unscheduleAction(action);
		}
		this.scheduledActions = new ArrayList<>();
	}

	/**
	 * Initialize disease
	 */
	private void initDisease() {
		if (this.wasInitiallyExposed) {
			transitionToExposed(true);
		} else {
			transitionToSusceptible();
		}
	}

	/**
	 * Assign reference locations
	 */
	private void assignReferenceLocations() {
		Pair<NdPoint, NdPoint> location = Heuristics.getReferenceSpots();
		this.household = location.getFirst();
		this.workplace = location.getSecond();
	}

	/**
	 * Schedule recurring events
	 */
	private void scheduleRecurringEvents() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		ISchedulableAction wakeUpAction = eventScheduler.scheduleRecurringEvent(
				this.wakeUpTime, this, TickConverter.TICKS_PER_DAY, "goWork");
		ISchedulableAction returnHomeAction = eventScheduler
				.scheduleRecurringEvent(this.returningHomeTime, this,
						TickConverter.TICKS_PER_DAY, "goHome");
		ISchedulableAction stepAction = eventScheduler.scheduleRecurringEvent(
				this.wakeUpTime, this, TICKS_BETWEEN_STEPS, "step");
		this.scheduledActions.add(wakeUpAction);
		this.scheduledActions.add(returnHomeAction);
		this.scheduledActions.add(stepAction);
	}

	/**
	 * Count infected people
	 * 
	 * @param citizens Citizens
	 */
	private int countInfectedPeople(Iterable<Citizen> citizens) {
		int count = 0;
		for (Citizen citizen : citizens) {
			if (citizen.compartment == Compartment.INFECTED) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Go to location
	 * 
	 * @param location Location
	 */
	private void goTo(NdPoint location) {
		double x = location.getX();
		double y = location.getY();
		this.simulationBuilder.space.moveTo(this, x, y);
		this.simulationBuilder.grid.moveTo(this, (int) x, (int) y);
	}

	/**
	 * Unschedule action
	 * 
	 * @param action Action to unschedule
	 */
	private void unscheduleAction(ISchedulableAction action) {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		schedule.removeAction(action);
	}

}