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
	 * Simulation runs
	 */
	private int simulationRuns;

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
		int calibrationSteps = 5;
		double endTime = calibrationSteps
				* SimulationBuilder.SIMULATIONS_PER_CALIBRATION_STEP
				* (SimulationBuilder.TICKS_PER_RUN
						+ SimulationBuilder.TICKS_BETWEEN_RUNS);
		RunEnvironment.getInstance().endAt(endTime);
	}

	/**
	 * Handle the 'onNewSimulationRun' event
	 */
	@ScheduledMethod(start = 0, interval = SimulationBuilder.TICKS_PER_RUN
			+ SimulationBuilder.TICKS_BETWEEN_RUNS, priority = 2)
	public void onNewSimulationRun() {
		RandomHelper.init();
		if (this.simulationRuns >= SimulationBuilder.SIMULATIONS_PER_CALIBRATION_STEP) {
			updateParameters();
			this.simulationRuns = 0;
		}
		this.simulationRuns++;
	}

	/**
	 * Update parameters
	 */
	public void updateParameters() {
		ParametersAdapter parametersAdapter = this.simulationBuilder.parametersAdapter;
		double aVr = parametersAdapter.getAverageRoomVentilationRate();
		aVr = aVr * 0.1;
		parametersAdapter.setAverageRoomVentilationRate(aVr);
	}

}