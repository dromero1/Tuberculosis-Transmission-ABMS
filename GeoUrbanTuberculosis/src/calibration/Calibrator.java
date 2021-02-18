package calibration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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
		// FIX AS SOON AS POSSIBLE
		int calibrationSteps = 100;
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
		if (this.simulationRun >= SIMULATIONS_PER_CALIBRATION_STEP) {
			double calibrationError = calculateCalibrationError();
			System.out.println(calibrationError);
			updateParameters(calibrationError);
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
	 * Calculate calibration error
	 */
	public double calculateCalibrationError() {
		double goal = this.simulationBuilder.parametersAdapter
				.getMeanIncidenceRateGoal();
		DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
		for (double incidenceRate : this.incidenceRates) {
			descriptiveStatistics.addValue(Math.abs(incidenceRate - goal));
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
		this.tuningAgent.updateLearning(calibrationError);
		// Procure new parameter setup
		Map<String, Double> parameterSetup = this.tuningAgent.selectAction();
		// Update simulation parameters
		ParametersAdapter parametersAdapter = this.simulationBuilder.parametersAdapter;
		for (Entry<String, Double> parameter : parameterSetup.entrySet()) {
			String key = parameter.getKey();
			double value = parameter.getValue();
			parametersAdapter.setParameterValue(key, value);
		}
	}

	/**
	 * Reset metrics
	 */
	public void resetMetrics() {
		this.incidenceRates = new ArrayList<>();
	}

}