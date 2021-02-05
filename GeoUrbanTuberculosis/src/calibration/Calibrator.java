package calibration;

import java.util.ArrayList;
import java.util.List;
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
	public static final int SIMULATIONS_PER_CALIBRATION_STEP = 10;

	/**
	 * Reference to simulation builder
	 */
	private SimulationBuilder simulationBuilder;

	/**
	 * Current simulation run
	 */
	private int simulationRun;

	/**
	 * Incidence rates
	 */
	private List<Double> incidenceRates;

	/**
	 * Create a new calibrator
	 *
	 * @param simulationBuilder Simulation builder
	 */
	public Calibrator(SimulationBuilder simulationBuilder) {
		this.simulationBuilder = simulationBuilder;
		this.incidenceRates = new ArrayList<>();
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
		if (this.simulationRun > 0) {
			measureIncidenceRate();
			RandomHelper.init();
			this.simulationBuilder.outputManager.resetOutputs();
		}
		if (this.simulationRun > SIMULATIONS_PER_CALIBRATION_STEP) {
			System.out.println(this.incidenceRates);
			updateParameters();
			resetMetrics();
			this.simulationRun = 0;
		}
		this.simulationRun++;
	}

	/**
	 * Measure incidence rate
	 */
	public void measureIncidenceRate() {
		int newCases = this.simulationBuilder.outputManager.getNewCases();
		int initialPopulation = this.simulationBuilder.parametersAdapter
				.getSusceptibleCount();
		double incidenceRate = (newCases * 1.0) / initialPopulation;
		this.incidenceRates.add(incidenceRate);
	}

	/**
	 * Reset metrics
	 */
	public void resetMetrics() {
		this.incidenceRates = new ArrayList<>();
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