package calibration;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import simulation.ParametersAdapter;
import simulation.SimulationBuilder;

public class Calibrator {

	/**
	 * Reference to simulation builder
	 */
	private SimulationBuilder simulationBuilder;

	/**
	 * Run
	 */
	private int run;

	/**
	 * Create a new calibrator
	 *
	 * @param simulationBuilder Simulation builder
	 */
	public Calibrator(SimulationBuilder simulationBuilder) {
		this.simulationBuilder = simulationBuilder;
	}

	/**
	 * Initialize
	 */
	@ScheduledMethod(start = 0, priority = 3)
	public void init() {
		int runs = 10;
		double endTime = runs * (SimulationBuilder.TICKS_PER_RUN
				+ SimulationBuilder.TICKS_BETWEEN_RUNS);
		RunEnvironment.getInstance().endAt(endTime);
	}

	/**
	 * Update parameters
	 */
	@ScheduledMethod(start = 0, interval = SimulationBuilder.TICKS_PER_RUN
			+ SimulationBuilder.TICKS_BETWEEN_RUNS, priority = 2)
	public void updateParameters() {
		if (this.run > 0) {
			ParametersAdapter parametersAdapter = this.simulationBuilder.parametersAdapter;
			double aVr = parametersAdapter.getAverageRoomVentilationRate();
			aVr = aVr * 0.1;
			parametersAdapter.setAverageRoomVentilationRate(aVr);
			RandomHelper.init();
		}
		this.run++;
	}

}