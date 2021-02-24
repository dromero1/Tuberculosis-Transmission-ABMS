package calibration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.collections.Pair;
import simulation.ParametersAdapter;
import simulation.SimulationBuilder;

public class Calibrator {

	/**
	 * Debug flag
	 */
	public static final boolean DEBUG = true;

	/**
	 * Ticks per simulation run (unit: hours)
	 */
	public static final double TICKS_PER_RUN = 8760;

	/**
	 * Ticks between simulation runs (unit: hours)
	 */
	public static final double TICKS_BETWEEN_RUNS = 100;

	/**
	 * Maximum calibration steps
	 */
	public static final double MAX_CALIBRATION_STEPS = 500;

	/**
	 * Simulations per calibration step
	 */
	public static final int SIMULATIONS_PER_CALIBRATION_STEP = 30;

	/**
	 * Calibrations runs before parameter swap
	 */
	public static final int CALIBRATIONS_BEFORE_PARAMETER_SWAP = 20;

	/**
	 * Current simulation run
	 */
	private int simulationRun;

	/**
	 * Incidence rates
	 */
	private List<Double> incidenceRates;

	/**
	 * Exposure rates
	 */
	private List<Double> exposureRates;

	/**
	 * Q-learning-based tuning agent
	 */
	private QLearningTuningAgent tuningAgent;

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
		this.incidenceRates = new ArrayList<>();
		this.exposureRates = new ArrayList<>();
	}

	/**
	 * Initialize
	 */
	@ScheduledMethod(start = 0, priority = 3)
	public void init() {
		// Initialize simulation run
		this.simulationRun = 1;
		// Initialize tuning agent
		initTuningAgent();
		// Schedule end time
		double endTime = MAX_CALIBRATION_STEPS
				* SIMULATIONS_PER_CALIBRATION_STEP
				* (TICKS_PER_RUN + TICKS_BETWEEN_RUNS);
		RunEnvironment.getInstance().endAt(endTime);
	}

	/**
	 * Handle the 'onNewSimulationRun' event
	 */
	@ScheduledMethod(start = TICKS_PER_RUN
			+ TICKS_BETWEEN_RUNS, interval = TICKS_PER_RUN
					+ TICKS_BETWEEN_RUNS, priority = 2)
	public void onNewSimulationRun() {
		measureOutputs();
		if (this.simulationRun >= SIMULATIONS_PER_CALIBRATION_STEP) {
			Pair<Double, Double> calibrationErrors = calculateCalibrationErrors();
			updateParameters(calibrationErrors);
			resetMetrics();
			this.simulationRun = 0;
		}
		this.simulationBuilder.resetSimulation();
		this.simulationRun++;
	}

	/**
	 * Initialize tuning agent
	 */
	private void initTuningAgent() {
		// Instantiate tuning agent
		ParametersAdapter parametersAdapter = this.simulationBuilder.parametersAdapter;
		double epsilon = parametersAdapter.getEpsilon();
		double learningRate = parametersAdapter.getLearningRate();
		double discountFactor = parametersAdapter.getDiscountFactor();
		this.tuningAgent = new QLearningTuningAgent(epsilon, learningRate,
				discountFactor);
		// Activate tuning agent
		Map<String, CalibrationParameter> setup = this.simulationBuilder.calibrationSetup;
		Map<String, Double> tunableParameters = this.simulationBuilder.parametersAdapter
				.getTunableParameters();
		this.tuningAgent.activate(tunableParameters, setup);
	}

	/**
	 * Measure outputs
	 */
	private void measureOutputs() {
		// Measure incidence rate
		int infections = this.simulationBuilder.outputManager.getInfections();
		int initialSusceptibleCount = this.simulationBuilder.parametersAdapter
				.getSusceptibleCount();
		int initialExposedCount = this.simulationBuilder.parametersAdapter
				.getExposedCount();
		int population = initialSusceptibleCount + initialExposedCount;
		double incidenceRate = (infections * 1.0) / population;
		// Measure mean infections per citizen
		int exposures = this.simulationBuilder.outputManager.getExposures();
		double exposureRate = (exposures * 1.0) / population;
		// Save results
		this.incidenceRates.add(incidenceRate);
		this.exposureRates.add(exposureRate);
		// Debugging only
		if (DEBUG) {
			System.out.printf("> Incidence rate = %.4f, Exposure rate = %.4f%n",
					incidenceRate, exposureRate);
		}
	}

	/**
	 * Calculate calibration error
	 */
	private Pair<Double, Double> calculateCalibrationErrors() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		// Calculate incidence rates' MAD
		double incidenceGoal = this.simulationBuilder.parametersAdapter
				.getIncidenceRateGoal();
		for (double incidenceRate : this.incidenceRates) {
			stats.addValue(Math.abs(incidenceRate - incidenceGoal));
		}
		double incidenceMAD = stats.getPercentile(50);
		// Calculate exposure rates' MAD
		double exposureGoal = this.simulationBuilder.parametersAdapter
				.getExposureRateGoal();
		stats.clear();
		for (double exposureRate : this.exposureRates) {
			stats.addValue(Math.abs(exposureRate - exposureGoal));
		}
		double exposureMAD = stats.getPercentile(50);
		// Debugging only
		if (DEBUG) {
			System.out.printf(
					"> Num. incidence rates = %d, MADi = %.4f, MADe = %.4f%n",
					this.incidenceRates.size(), incidenceMAD, exposureMAD);
		}
		return new Pair<>(incidenceMAD, exposureMAD);
	}

	/**
	 * Update parameters
	 * 
	 * @param calibrationErrors Calibration errors
	 */
	private void updateParameters(Pair<Double, Double> calibrationErrors) {
		// Update learning device
		Map<String, Double> tunableParameters = this.simulationBuilder.parametersAdapter
				.getTunableParameters();
		this.tuningAgent.updateLearning(calibrationErrors, tunableParameters);
		// Select action
		Pair<String, Double> parameterSelection = this.tuningAgent
				.selectAction();
		// Update simulation parameters
		ParametersAdapter parametersAdapter = this.simulationBuilder.parametersAdapter;
		String parameterId = parameterSelection.getFirst();
		double value = parameterSelection.getSecond();
		parametersAdapter.setParameterValue(parameterId, value);
	}

	/**
	 * Reset metrics
	 */
	private void resetMetrics() {
		this.incidenceRates = new ArrayList<>();
	}

}