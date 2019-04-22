/**
 * 
 */
package model;

import java.util.List;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

/**
 * @author david
 *
 */
public class Citizen {

	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;
	private NdPoint homeplaceLocation;
	private NdPoint workplaceLocation;
	private int wakeUpTime;
	private int workTime;
	private int diseaseStage;
	private double expositionTick;
	private boolean isInmunodepressed;
	private boolean smokes;
	private boolean drinksAlcohol;
	private ISchedulableAction infectAction;
	private ISchedulableAction evaluateInfectionAction;

	public Citizen(ContinuousSpace<Object> space, Grid<Object> grid, int diseaseStage) {
		this.space = space;
		this.grid = grid;
		this.diseaseStage = diseaseStage;
		this.wakeUpTime = RandomHelper.nextIntFromTo(ModelParameters.INITIAL_WAKEUP_TIME,
				ModelParameters.FINAL_WAKEUP_TIME);
		this.workTime = RandomHelper.nextIntFromTo(ModelParameters.MIN_WORKTIME, ModelParameters.MAX_WORKTIME);
		scheduleRepeatingEvents();
	}

	public void scheduleRepeatingEvents() {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters params;

		// Schedule wake up event
		params = ScheduleParameters.createRepeating(wakeUpTime, ModelParameters.DAY_IN_HOURS);
		schedule.schedule(params, this, "wakeUp");

		// Schedule return home event
		params = ScheduleParameters.createRepeating(wakeUpTime + workTime, ModelParameters.DAY_IN_HOURS);
		schedule.schedule(params, this, "returnHome");

		if (diseaseStage == DiseaseStage.INFECTED) {
			setInfected();
		} else if (diseaseStage == DiseaseStage.EXPOSED) {
			setExposed();
		}
	}

	public void wakeUp() {
		goTo(workplaceLocation);
	}

	public void returnHome() {
		goTo(homeplaceLocation);
	}

	public void infect() {
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<Citizen> nghCreator = new GridCellNgh<Citizen>(grid, pt, Citizen.class, 0, 0);
		List<GridCell<Citizen>> gridCells = nghCreator.getNeighborhood(true);

		for (GridCell<Citizen> cell : gridCells) {
			for (Citizen citizen : cell.items()) {
				if (citizen.diseaseStage == DiseaseStage.SUSCEPTIBLE && isCitizenGettingExposed(1))
					citizen.setExposed();
			}
		}
	}

	public void setExposed() {
		diseaseStage = DiseaseStage.EXPOSED;
		expositionTick = Math.max(RepastEssentials.GetTickCount(), 0);
		scheduleInfectionEvaluationEvents();
	}

	public void setInfected() {
		diseaseStage = DiseaseStage.INFECTED;
		scheduleInfectionEvents();
		unscheduleEvents(evaluateInfectionAction);
		if (isCitizenGettingRecovered()) {
			scheduleRecoverEvent();
		}
	}

	public void setRecovered() {
		diseaseStage = DiseaseStage.RECOVERED;
		unscheduleEvents(infectAction);
	}

	public void evaluateInfection() {
		if (isCitizenGettingInfected()) {
			setInfected();
		}
	}

	private void goTo(NdPoint location) {
		space.moveTo(this, location.getX(), location.getY());
		grid.moveTo(this, (int) location.getX(), (int) location.getY());
	}

	private boolean isCitizenGettingExposed(int infectedCount) {
		double p = ModelParameters.AVG_PULMONARY_VENTILATION_RATE;
		double phi = ModelParameters.AVG_PATIENT_QUANTA_PRODUCTION;

		// Calculate probability of getting exposed
		double probability = InfectionProbabilityCalculator.calculateProbability(infectedCount, p, 3.0, phi, 75.0);

		double random = RandomHelper.nextDoubleFromTo(0, 1);

		return random <= probability;
	}

	private boolean isCitizenGettingInfected() {
		// Calculate exposed time
		double t = Math.max(RepastEssentials.GetTickCount() - expositionTick, 0);

		// Calculate probability of getting infected (Using interpolation function)
		double yearlyProbability = Math.max(0.1 + (-2.6595e-11) * Math.pow(t, 2), 0);

		// Weekly probability yP = 1-(1-wP)^52
		double weeklyProbability = 1 - Math.pow(1 - yearlyProbability, 1.0 / ModelParameters.WEEKS_IN_YEAR);

		// Adjust probability to risk factors
		if (isInmunodepressed)
			weeklyProbability *= ModelParameters.IMMUNODEFICIENCY_FOLD;
		if (smokes)
			weeklyProbability *= ModelParameters.RISK_FACTOR_ADJUSTMENT;
		if (drinksAlcohol)
			weeklyProbability *= ModelParameters.RISK_FACTOR_ADJUSTMENT;

		double random = RandomHelper.nextDoubleFromTo(0, 1);

		return random <= weeklyProbability;
	}

	private boolean isCitizenGettingRecovered() {
		// TODO Calculate the probability of getting recovered
		return false;
	}

	private void scheduleInfectionEvaluationEvents() {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double currentTick = Math.max(RepastEssentials.GetTickCount(), 0);
		int hoursToInitialEvaluation = ModelParameters.WEEK_IN_HOURS;
		double startTime = currentTick + hoursToInitialEvaluation;
		ScheduleParameters params = ScheduleParameters.createRepeating(startTime, ModelParameters.WEEK_IN_HOURS);
		evaluateInfectionAction = schedule.schedule(params, this, "evaluateInfection");
	}

	private void scheduleInfectionEvents() {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double currentTick = Math.max(RepastEssentials.GetTickCount(), 0);
		ScheduleParameters params = ScheduleParameters.createRepeating(currentTick, 1);
		infectAction = schedule.schedule(params, this, "infect");
	}

	private void scheduleRecoverEvent() {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double currentTick = Math.max(RepastEssentials.GetTickCount(), 0);
		// TODO Calculate the hours to recover
		int hoursToRecover = 8030;
		double startTime = currentTick + hoursToRecover;
		ScheduleParameters params = ScheduleParameters.createOneTime(startTime);
		schedule.schedule(params, this, "setRecovered");
	}

	private void unscheduleEvents(ISchedulableAction action) {
		if (action != null) {
			ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
			schedule.removeAction(action);
		}
	}

	public int getDiseaseStage() {
		return diseaseStage;
	}

	public NdPoint getWorkplaceLocation() {
		return workplaceLocation;
	}

	public void setWorkplaceLocation(double x, double y) {
		NdPoint workplaceLocation = new NdPoint(x, y);
		this.workplaceLocation = workplaceLocation;
	}

	public NdPoint getHomeplaceLocation() {
		return homeplaceLocation;
	}

	public void setHomeplaceLocation(double x, double y) {
		NdPoint homeplaceLocation = new NdPoint(x, y);
		this.homeplaceLocation = homeplaceLocation;
	}

	public int isSusceptible() {
		return (diseaseStage == DiseaseStage.SUSCEPTIBLE) ? 1 : 0;
	}

	public int isExposed() {
		return (diseaseStage == DiseaseStage.EXPOSED) ? 1 : 0;
	}

	public int isInfected() {
		return (diseaseStage == DiseaseStage.INFECTED) ? 1 : 0;
	}

	public int isRecovered() {
		return (diseaseStage == DiseaseStage.RECOVERED) ? 1 : 0;
	}

}
