package calibration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.Pair;

public class QLearningTuningAgent {

	/**
	 * Debug flag
	 */
	public static final boolean DEBUG = true;

	/**
	 * Multi-objective flag
	 */
	public static final boolean MULTIOBJECTIVE_FLAG = false;

	/**
	 * Similarity threshold
	 */
	public static final double SIMILARITY_THRESHOLD = 1e-10;

	/**
	 * Initial Q-value estimate
	 */
	public static final int INITIAL_Q_VALUE_ESTIMATE = 0;

	/**
	 * Q-values for state-action pairs
	 */
	private Map<String, List<Pair<Double, Double>>> qValues;

	/**
	 * Pareto optimal setups
	 */
	private List<ParetoOptimalSetup> paretoOptimalSetups;

	/**
	 * Epsilon parameter for epsilon-greedy action selection
	 */
	private double epsilon;

	/**
	 * Learning rate for update rule
	 */
	private double learningRate;

	/**
	 * Discount factor for update rule
	 */
	private double discountFactor;

	/**
	 * Last best calibration error
	 */
	private double lastBestCalibrationError;

	/**
	 * Parameters' tags
	 */
	private List<String> parametersTags;

	/**
	 * Current parameter being tuned
	 */
	private String currentParameter = "";

	/**
	 * Update counter
	 */
	private int updateCounter;

	/**
	 * Create a new Q-learning-based tuning agent
	 * 
	 * @param epsilon        Epsilon
	 * @param learningRate   Learning rate
	 * @param discountFactor Discount factor
	 */
	public QLearningTuningAgent(double epsilon, double learningRate,
			double discountFactor) {
		this.epsilon = epsilon;
		this.learningRate = learningRate;
		this.discountFactor = discountFactor;
		this.qValues = new HashMap<>();
		this.paretoOptimalSetups = new ArrayList<>();
		this.lastBestCalibrationError = Double.POSITIVE_INFINITY;
		this.parametersTags = new ArrayList<>();
	}

	/**
	 * Activate
	 * 
	 * @param tunableParameters Tunable parameters
	 * @param calibrationSetup  Calibration setup
	 */
	public void activate(Map<String, Double> tunableParameters,
			Map<String, CalibrationParameter> calibrationSetup) {
		for (Entry<String, Double> parameter : tunableParameters.entrySet()) {
			String parameterId = parameter.getKey();
			CalibrationParameter calibrationParameter = calibrationSetup
					.get(parameterId);
			double tolerance = calibrationParameter.getTolerance();
			double lowerBound = calibrationParameter.getLowerBound();
			double upperBound = calibrationParameter.getUpperBound();
			double step = 2 * tolerance * (upperBound - lowerBound);
			double numStates = Math.round((upperBound - lowerBound) / step);
			List<Pair<Double, Double>> actions = new ArrayList<>();
			for (int i = 0; i <= numStates; i++) {
				double action = lowerBound + i * step;
				double q0 = INITIAL_Q_VALUE_ESTIMATE;
				actions.add(new Pair<>(action, q0));
			}
			this.qValues.put(parameterId, actions);
			this.parametersTags.add(parameterId);
		}
		resetCurrentParameter();
	}

	/**
	 * Select action (epsilon-greedy)
	 */
	public Pair<String, Double> selectAction() {
		List<Pair<Double, Double>> parameterSpace = this.qValues
				.get(this.currentParameter);
		Pair<Double, Double> selectedPoint = null;
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		int index = -1;
		if (r < 1 - this.epsilon) {
			double topValue = Double.NEGATIVE_INFINITY;
			List<Pair<Double, Double>> ties = new ArrayList<>();
			for (Pair<Double, Double> point : parameterSpace) {
				double qValue = point.getSecond();
				if (qValue > topValue) {
					topValue = qValue;
					ties.clear();
					ties.add(point);
				} else if (qValue == topValue) {
					ties.add(point);
				}
			}
			index = RandomHelper.nextIntFromTo(0, ties.size() - 1);
			selectedPoint = ties.get(index);
		} else {
			index = RandomHelper.nextIntFromTo(0, parameterSpace.size() - 1);
			selectedPoint = parameterSpace.get(index);
		}
		double parameterValue = selectedPoint.getFirst();
		return new Pair<>(this.currentParameter, parameterValue);
	}

	/**
	 * Update learning
	 * 
	 * @param calibrationErrors Calibration errors
	 * @param tunableParameters Tunable parameters
	 */
	public void updateLearning(Pair<Double, Double> calibrationErrors,
			Map<String, Double> tunableParameters) {
		// Get parameter space
		List<Pair<Double, Double>> parameterSpace = this.qValues
				.get(this.currentParameter);
		// Retrieve last point
		double lastValue = tunableParameters.get(this.currentParameter);
		int indexLastAction = -1;
		for (int i = 0; i < parameterSpace.size(); i++) {
			Pair<Double, Double> point = parameterSpace.get(i);
			double value = point.getFirst();
			if (Math.abs(value - lastValue) < SIMILARITY_THRESHOLD) {
				indexLastAction = i;
				break;
			}
		}
		Pair<Double, Double> lastPoint = parameterSpace.get(indexLastAction);
		// Compute reward
		double reward = computeReward(calibrationErrors, lastValue);
		// Obtain old Q-value
		double oldQ = lastPoint.getSecond();
		// Estimate optimal future value
		double maxQ = Double.NEGATIVE_INFINITY;
		for (Pair<Double, Double> point : parameterSpace) {
			double q = point.getSecond();
			if (q >= maxQ) {
				maxQ = q;
			}
		}
		// Compute new Q-value
		double qValue = oldQ + this.learningRate
				* (reward + this.discountFactor * maxQ - oldQ);
		// Update Q-value
		lastPoint.setSecond(qValue);
		parameterSpace.set(indexLastAction, lastPoint);
		this.qValues.put(this.currentParameter, parameterSpace);
		// Update counter
		this.updateCounter++;
		// Update epsilon
		updateEpsilon();
		// Debugging only
		if (DEBUG) {
			double incidenceRateError = calibrationErrors.getFirst();
			double exposureRateError = calibrationErrors.getSecond();
			System.out.printf(
					"> %d - Error IrE = %.4f | Error ErE = %.4f | Reward = %.4f, ",
					this.updateCounter, incidenceRateError, exposureRateError,
					reward);
			System.out.printf("Param = %s | Value = %.4f | Q-value = %.4f%n",
					this.currentParameter, lastValue, qValue);
		}
		// Update last best calibration error
		if (!MULTIOBJECTIVE_FLAG) {
			double calibrationError = calibrationErrors.getFirst();
			if (calibrationError < this.lastBestCalibrationError) {
				this.lastBestCalibrationError = calibrationError;
			}
		}
		// Check parameter change
		if (this.updateCounter >= Calibrator.CALIBRATIONS_BEFORE_PARAMETER_SWAP) {
			resetCurrentParameter();
		}
	}

	/**
	 * Compute reward
	 * 
	 * @param calibrationErrors Calibration errors
	 * @param parameterValue    Parameter value
	 */
	private double computeReward(Pair<Double, Double> calibrationErrors,
			double parameterValue) {
		if (MULTIOBJECTIVE_FLAG) {
			ParetoOptimalSetup currentSetup = new ParetoOptimalSetup(
					this.currentParameter, parameterValue, calibrationErrors);
			if (this.paretoOptimalSetups.isEmpty()) {
				this.paretoOptimalSetups.add(currentSetup);
				return 0.0;
			} else {
				List<ParetoOptimalSetup> dominatedSolutions = new ArrayList<>();
				for (int i = 0; i < this.paretoOptimalSetups.size(); i++) {
					ParetoOptimalSetup paretoSetup = this.paretoOptimalSetups
							.get(i);
					if (dominates(currentSetup, paretoSetup)) {
						dominatedSolutions.add(paretoSetup);
					} else if (dominates(paretoSetup, currentSetup)) {
						if (currentSetup.isAlmostTheSameAs(paretoSetup)) {
							return 0.02;
						} else {
							return -1.0;
						}
					}
				}
				for (ParetoOptimalSetup dominatedSolution : dominatedSolutions) {
					this.paretoOptimalSetups.remove(dominatedSolution);
				}
				if (!this.paretoOptimalSetups.contains(currentSetup)) {
					this.paretoOptimalSetups.add(currentSetup);
				}
				return 1.0;
			}
		} else {
			double calibrationError = calibrationErrors.getFirst();
			if (this.lastBestCalibrationError == Double.POSITIVE_INFINITY
					|| Math.abs(calibrationError
							- this.lastBestCalibrationError) < ParetoOptimalSetup.SIMILARITY_THRESHOLD) {
				return 0;
			} else {
				return (this.lastBestCalibrationError - calibrationError);
			}
		}
	}

	/**
	 * Update epsilon
	 */
	private void updateEpsilon() {
		this.epsilon = this.epsilon
				- this.epsilon / Calibrator.MAX_CALIBRATION_STEPS;
	}

	/**
	 * Reset current parameter
	 */
	private void resetCurrentParameter() {
		String nextParameter = "";
		do {
			int index = RandomHelper.nextIntFromTo(0,
					this.parametersTags.size() - 1);
			nextParameter = this.parametersTags.get(index);
		} while (this.currentParameter.equals(nextParameter));
		this.currentParameter = nextParameter;
		this.updateCounter = 0;
	}

	/**
	 * Setup A dominates Setup B?
	 * 
	 * @param setupA Setup A
	 * @param setupB Setup B
	 */
	private boolean dominates(ParetoOptimalSetup setupA,
			ParetoOptimalSetup setupB) {
		double aIrE = setupA.getIncidenceRateError();
		double aErE = setupA.getExposureRateError();
		double bIrE = setupB.getIncidenceRateError();
		double bErE = setupB.getExposureRateError();
		return (aIrE <= bIrE && aErE <= bErE) && (aIrE < bIrE || aErE < bErE);
	}

}