package calibration;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import simulation.ParametersAdapter;
import simulation.SimulationBuilder;

public class Calibrator {

	/**
	 * Ticks per simulation run (unit: hours)
	 */
	public static final double TICKS_PER_RUN = 8760;

	/**
	 * Ticks between simulation runs (unit: hours)
	 */
	public static final double TICKS_BETWEEN_RUNS = 100;

	/**
	 * Simulations per calibration step
	 */
	public static final double SIMULATIONS_PER_CALIBRATION_STEP = 10;

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
		double endTime = calibrationSteps * SIMULATIONS_PER_CALIBRATION_STEP
				* (TICKS_PER_RUN + TICKS_BETWEEN_RUNS);
		RunEnvironment.getInstance().endAt(endTime);
	}

	/**
	 * Handle the 'onNewSimulationRun' event
	 */
	@ScheduledMethod(start = 0, interval = TICKS_PER_RUN
			+ TICKS_BETWEEN_RUNS, priority = 2)
	public void onNewSimulationRun() {
		RandomHelper.init();
		if (this.simulationRuns >= SIMULATIONS_PER_CALIBRATION_STEP) {
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