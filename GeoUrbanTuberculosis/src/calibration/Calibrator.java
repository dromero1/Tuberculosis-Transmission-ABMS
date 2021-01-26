package calibration;

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
		//RunEnvironment.getInstance().endAt(SimulationBuilder.TICKS_PER_RUN);
	}

}