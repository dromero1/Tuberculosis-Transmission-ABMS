/**
 * 
 */
package agents;

import java.util.List;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
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
	private ISchedulableAction infectAction;

	public Citizen(ContinuousSpace<Object> space, Grid<Object> grid, int diseaseStage) {
		this.space = space;
		this.grid = grid;
		this.diseaseStage = diseaseStage;
		this.wakeUpTime = RandomHelper.nextIntFromTo(5, 8);
		this.workTime = RandomHelper.nextIntFromTo(9, 11);
		init();
	}

	public void init() {
		selectWorkplaceLocation();
		scheduleRepeatingEvents();
	}

	public void scheduleRepeatingEvents() {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters params;

		// Schedule wake up event
		params = ScheduleParameters.createRepeating(wakeUpTime, 24);
		schedule.schedule(params, this, "wakeUp");

		// Schedule return home event
		params = ScheduleParameters.createRepeating(wakeUpTime + workTime, 24);
		schedule.schedule(params, this, "returnHome");

		if (diseaseStage == DiseaseStage.INFECTED) {
			setInfected();
		} else if (diseaseStage == DiseaseStage.EXPOSED) {
			setExposed();
		}
	}

	public void wakeUp() {
		if (homeplaceLocation == null)
			homeplaceLocation = space.getLocation(this);
		goTo(workplaceLocation);
	}

	public void returnHome() {
		goTo(homeplaceLocation);
	}

	public void infect() {
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<Citizen> nghCreator = new GridCellNgh<Citizen>(grid, pt, Citizen.class, 1, 1);
		List<GridCell<Citizen>> gridCells = nghCreator.getNeighborhood(true);

		for (GridCell<Citizen> cell : gridCells) {
			for (Citizen citizen : cell.items()) {
				if (citizen.diseaseStage == DiseaseStage.SUSCEPTIBLE 
						&& isCitizenGettingExposed())
					citizen.setExposed();
			}
		}
	}

	public void setExposed() {
		diseaseStage = DiseaseStage.EXPOSED;
		if (isCitizenGettingInfected())
			scheduleGetInfectedEvent();
	}

	public void setInfected() {
		diseaseStage = DiseaseStage.INFECTED;
		scheduleInfectionEvents();
		if (isCitizenGettingRecovered())
			scheduleRecoverEvent();
	}

	public void setRecovered() {
		diseaseStage = DiseaseStage.RECOVERED;
		removeInfectionEvents();
	}

	public int getDiseaseStage() {
		return diseaseStage;
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

	private void selectWorkplaceLocation() {
		Dimensions dimensions = this.space.getDimensions();
		double x = RandomHelper.nextDoubleFromTo(0, dimensions.getWidth());
		double y = RandomHelper.nextDoubleFromTo(0, dimensions.getHeight());
		this.workplaceLocation = new NdPoint(x, y);
	}

	private void goTo(NdPoint location) {
		space.moveTo(this, location.getX(), location.getY());
		grid.moveTo(this, (int) location.getX(), (int) location.getY());
	}

	private boolean isCitizenGettingExposed() {
		return RandomHelper.nextIntFromTo(1, 10) == 1;
	}

	private boolean isCitizenGettingInfected() {
		return RandomHelper.nextIntFromTo(1, 10) == 1;
	}

	private boolean isCitizenGettingRecovered() {
		return RandomHelper.nextIntFromTo(1, 10) == 1;
	}

	private void scheduleGetInfectedEvent() {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		int hoursToBecomeInfected = RandomHelper.nextIntFromTo(336, 8760);
		double currentTick = RepastEssentials.GetTickCount();
		double startTime = currentTick + hoursToBecomeInfected;
		ScheduleParameters params = ScheduleParameters.createOneTime(startTime);
		schedule.schedule(params, this, "setInfected");
	}

	private void scheduleInfectionEvents() {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double currentTick = RepastEssentials.GetTickCount();
		ScheduleParameters params = ScheduleParameters.createRepeating(currentTick, 1);
		infectAction = schedule.schedule(params, this, "infect");
	}

	private void scheduleRecoverEvent() {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		double currentTick = RepastEssentials.GetTickCount();
		int hoursToBecomeSusceptible = RandomHelper.nextIntFromTo(4380, 8760);
		double startTime = currentTick + hoursToBecomeSusceptible;
		ScheduleParameters params = ScheduleParameters.createOneTime(startTime);
		schedule.schedule(params, this, "setRecovered");
	}

	private void removeInfectionEvents() {
		if (infectAction != null) {
			ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
			schedule.removeAction(infectAction);
		}
	}

}
