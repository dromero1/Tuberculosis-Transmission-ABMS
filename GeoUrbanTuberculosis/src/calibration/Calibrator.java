package calibration;

import repast.simphony.engine.environment.RunEnvironment;
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
		RunEnvironment.getInstance().endAt(10 * SimulationBuilder.TICKS_PER_RUN
				+ 9 * SimulationBuilder.TICKS_BETWEEN_RUNS);
	}

}