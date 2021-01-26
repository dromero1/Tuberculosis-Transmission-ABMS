package calibration;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import simulation.SimulationBuilder;

public class Calibrator {

	/**
	 * Reference to simulation builder
	 */
	private SimulationBuilder simulationBuilder;
	
	/**
	 * Create a new calibrator
	 * 
	 * @param simulationBuilder Simulation builder
	 */
	public Calibrator(SimulationBuilder simulationBuilder) {
		this.simulationBuilder = simulationBuilder;
		init();
	}

	/**
	 * Initialize
	 */
	@ScheduledMethod(start = 0)
	public void init() {
		RunEnvironment.getInstance().endAt(SimulationBuilder.TICKS_PER_RUN);
	}

}