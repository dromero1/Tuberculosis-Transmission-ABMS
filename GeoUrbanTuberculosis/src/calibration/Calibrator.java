package calibration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.Pair;
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
	 * Maximum calibration steps
	 */
	public static final double MAX_CALIBRATION_STEPS = 1000;

	/**
	 * Simulations per calibration step
	 */
	public static final int SIMULATIONS_PER_CALIBRATION_STEP = 20;

	/**
	 * Debug flag
	 */
	public static final boolean DEBUG = false;

	/**
	 * Current simulation run
	 */
	private int simulationRun;

	/**
	 * Incidence rates
	 */
	private List<Double> incidenceRates;

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
		this.tuningAgent = new QLearningTuningAgent();
		this.incidenceRates = new ArrayList<>();
		this.simulationRun = 1;
	}

	/**
	 * Initialize
	 */
	@ScheduledMethod(start = 0, priority = 3)
	public void init() {
		// Initialize tuning agent
		Map<String, CalibrationParameter> setup = this.simulationBuilder.calibrationSetup;
		Map<String, Double> tunableParameters = this.simulationBuilder.parametersAdapter
				.getTunableParameters();
		this.tuningAgent.init(tunableParameters, setup);
		// Schedule maximum end time
		double maxEndTime = MAX_CALIBRATION_STEPS
				* SIMULATIONS_PER_CALIBRATION_STEP
				* (TICKS_PER_RUN + TICKS_BETWEEN_RUNS);
		RunEnvironment.getInstance().endAt(maxEndTime);
	}

	/**
	 * Handle the 'onNewSimulationRun' event
	 */
	@ScheduledMethod(start = TICKS_PER_RUN
			+ TICKS_BETWEEN_RUNS, interval = TICKS_PER_RUN
					+ TICKS_BETWEEN_RUNS, priority = 2)
	public void onNewSimulationRun() {
		measureIncidenceRate();
		if (this.simulationRun >= SIMULATIONS_PER_CALIBRATION_STEP) {
			double calibrationError = calculateCalibrationError();
			updateParameters(calibrationError);
			resetMetrics();
			this.simulationRun = 0;
		}
		RandomHelper.init();
		this.simulationBuilder.outputManager.resetOutputs();
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
	 * Calculate calibration error
	 */
	public double calculateCalibrationError() {
		double reference = this.simulationBuilder.parametersAdapter
				.getMeanIncidenceRateGoal();
		DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
		for (double incidenceRate : this.incidenceRates) {
			descriptiveStatistics.addValue(Math.abs(incidenceRate - reference));
		}
		if (DEBUG) {
			System.out.printf("> Num. incidence rates = %d%n",
					this.incidenceRates.size());
		}
		return descriptiveStatistics.getMean();
	}

	/**
	 * Update parameters
	 * 
	 * @param calibrationError Calibration error
	 */
	public void updateParameters(double calibrationError) {
		// Update learning device
		Map<String, Double> tunableParameters = this.simulationBuilder.parametersAdapter
				.getTunableParameters();
		this.tuningAgent.updateLearning(calibrationError, tunableParameters);
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
	public void resetMetrics() {
		this.incidenceRates = new ArrayList<>();
	}

}