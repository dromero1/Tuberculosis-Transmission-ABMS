/**
 * 
 */
package model;

import java.util.List;
import cern.jet.random.Exponential;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.parameter.Parameters;
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
	private boolean isInmunodepressed;
	private boolean smokes;
	private boolean drinksAlcohol;
	private ISchedulableAction infectAction;
	private ISchedulableAction evaluateInfectionAction;

	public Citizen(ContinuousSpace<Object> space, Grid<Object> grid, int diseaseStage) {
		this.space = space;
		this.grid = grid;
		this.diseaseStage = diseaseStage;

		// Get random wake up and work time
		this.wakeUpTime = RandomHelper.nextIntFromTo(ModelParameters.INITIAL_WAKEUP_TIME,
				ModelParameters.FINAL_WAKEUP_TIME);
		this.workTime = RandomHelper.nextIntFromTo(ModelParameters.MIN_WORK_TIME, ModelParameters.MAX_WORK_TIME);

		// Schedule daily routine
		scheduleRecurringEvent(wakeUpTime, ModelParameters.HOURS_IN_DAY, "wakeUp");
		scheduleRecurringEvent(wakeUpTime + workTime, ModelParameters.HOURS_IN_DAY, "returnHome");

		// Initialize disease related events
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
			int infectedCount = countInfectedPeople(cell.items());
			for (Citizen citizen : cell.items()) {
				if (citizen.diseaseStage == DiseaseStage.SUSCEPTIBLE && isCitizenGettingExposed(infectedCount))
					citizen.setExposed();
			}
		}
	}

	public void setSusceptible() {
		diseaseStage = DiseaseStage.SUSCEPTIBLE;
	}

	public void setExposed() {
		diseaseStage = DiseaseStage.EXPOSED;
		evaluateInfectionAction = scheduleRecurringEvent(1, 1, "evaluateInfection");
	}

	public void setInfected() {
		diseaseStage = DiseaseStage.INFECTED;
		infectAction = scheduleRecurringEvent(1, 1, "infect");
		unscheduleEvents(evaluateInfectionAction);
		double hoursToDiagnosis = calculateHoursToDiagnosis();
		scheduleOneTimeEvent(hoursToDiagnosis, "diagnosed");
	}

	public void diagnosed() {
		diseaseStage = DiseaseStage.ON_TREATMENT;
		unscheduleEvents(infectAction);

		Parameters params = RunEnvironment.getInstance().getParameters();
		double treatmentDropoutRate = params.getDouble("TreatmentDropoutRate");
		double random = RandomHelper.nextDoubleFromTo(0, 1);

		// Is the patient dropping the treatment
		if (random <= treatmentDropoutRate) {
			setInfected();
		} else {
			scheduleOneTimeEvent(ModelParameters.TREATMENT_DURATION, "setRecovered");
		}
	}

	public void setRecovered() {
		diseaseStage = DiseaseStage.RECOVERED;
		scheduleOneTimeEvent(ModelParameters.TIME_TO_FULL_RECOVERY, "setSusceptible");
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

		// Get necessary simulation parameters
		Parameters params = RunEnvironment.getInstance().getParameters();
		double aVr = params.getDouble("AverageRoomVentilationRate");
		double aRv = params.getDouble("AverageRoomVolume");

		// Calculate probability of getting exposed
		double probability = InfectionProbabilityCalculator.calculateProbability(infectedCount, p, aVr, phi, aRv);

		double random = RandomHelper.nextDoubleFromTo(0, 1);

		return random <= probability;
	}

	private boolean isCitizenGettingInfected() {
		double probability = ModelParameters.INFECTION_PROGRESSION_RATE;

		// Adjust probability to risk factors
		if (isInmunodepressed)
			probability *= ModelParameters.IMMUNODEFICIENCY_FOLD;
		if (smokes)
			probability *= ModelParameters.RISK_FACTOR_ADJUSTMENT;
		if (drinksAlcohol)
			probability *= ModelParameters.RISK_FACTOR_ADJUSTMENT;

		double random = RandomHelper.nextDoubleFromTo(0, 1);

		return random <= probability;
	}

	private double calculateHoursToDiagnosis() {
		// Get mean diagnosis delay parameter
		Parameters params = RunEnvironment.getInstance().getParameters();
		double mDd = params.getDouble("MeanDiagnosisDelay") * ModelParameters.HOURS_IN_DAY;

		// Create exponential function
		double lambda = 1 / mDd;
		Exponential exp = RandomHelper.createExponential(lambda);

		// Get random hours to diagnosis
		return exp.nextDouble();
	}

	private ISchedulableAction scheduleRecurringEvent(double ticksToEvent, double tickInterval, String methodName,
			Object... methodParams) {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double currentTick = Math.max(RepastEssentials.GetTickCount(), 0);
		double startTime = currentTick + ticksToEvent;
		ScheduleParameters params = ScheduleParameters.createRepeating(startTime, tickInterval);
		return schedule.schedule(params, this, methodName, methodParams);
	}

	private void scheduleOneTimeEvent(double ticksToEvent, String methodName, Object... methodParams) {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double currentTick = Math.max(RepastEssentials.GetTickCount(), 0);
		double startTime = currentTick + ticksToEvent;
		ScheduleParameters params = ScheduleParameters.createOneTime(startTime);
		schedule.schedule(params, this, methodName, methodParams);
	}

	private void unscheduleEvents(ISchedulableAction action) {
		if (action != null) {
			ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
			schedule.removeAction(action);
		}
	}

	private int countInfectedPeople(Iterable<Citizen> citizens) {
		int count = 0;
		for (Citizen citizen : citizens) {
			if (citizen.diseaseStage == DiseaseStage.INFECTED) {
				count++;
			}
		}
		return count;
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

	public int isOnTreatment() {
		return (diseaseStage == DiseaseStage.ON_TREATMENT) ? 1 : 0;
	}

	public boolean isInmunodepressed() {
		return isInmunodepressed;
	}

	public void setInmunodepressed() {
		this.isInmunodepressed = true;
	}

	public boolean smokes() {
		return smokes;
	}

	public void setSmoker() {
		this.smokes = true;
	}

	public boolean drinksAlcohol() {
		return drinksAlcohol;
	}

	public void setAlcoholDrinker() {
		this.drinksAlcohol = true;
	}

}
