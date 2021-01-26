package calibration;

import repast.simphony.engine.environment.RunEnvironment;
import simulation.SimulationBuilder;

public class Calibrator {

	/**
	 * Create a new calibrator
	 */
	public Calibrator() {
		int runs = 20;
		double endTime = runs * (SimulationBuilder.TICKS_PER_RUN
				+ SimulationBuilder.TICKS_BETWEEN_RUNS);
		RunEnvironment.getInstance().endAt(endTime);
	}

}