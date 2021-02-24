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
	 * Similarity threshold
	 */
	public static final double SIMILARITY_THRESHOLD = 1e-10;

	/**
	 * Initial Q-value estimate
	 */
	public static final int INITIAL_Q_VALUE_ESTIMATE = 0;

	/**
	 * Just noticeable error difference / meaningful error difference threshold
	 */
	public static final double JUST_NOTICEABLE_ERROR_DIFFERENCE = 0.01;

	/**
	 * Q-values for state-action pairs
	 */
	private Map<String, List<Pair<Double, Double>>> qValues;

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
	 * Parameters' tags
	 */
	private List<String> parametersTags;

	/**
	 * Current parameter being tuned
	 */
	private String currentParameter = "";

	/**
	 * Last best calibration error
	 */
	private double lastBestCalibrationError = Double.POSITIVE_INFINITY;

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
	 * @param calibrationError  Calibration error
	 * @param tunableParameters Tunable parameters
	 */
	public void updateLearning(double calibrationError,
			Map<String, Double> tunableParameters) {
		// Compute reward
		double reward = computeReward(calibrationError);
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
		// Display
		if (DEBUG) {
			System.out.printf(
					"> %d - Error = %.4f, Reward = %.4f, Param. %s, Value = %.4f, Q-value = %.4f%n",
					this.updateCounter, calibrationError, reward,
					this.currentParameter, lastValue, qValue);
		}
		// Update last best calibration error
		if (calibrationError < this.lastBestCalibrationError) {
			this.lastBestCalibrationError = calibrationError;
		}
		// Update counter
		this.updateCounter++;
		// Update epsilon
		updateEpsilon();
		// Check parameter change
		if (this.updateCounter >= Calibrator.CALIBRATIONS_BEFORE_PARAMETER_SWAP) {
			resetCurrentParameter();
		}
	}

	/**
	 * Compute reward
	 * 
	 * @param calibrationError Calibration error
	 */
	private double computeReward(double calibrationError) {
		if (this.lastBestCalibrationError == Double.POSITIVE_INFINITY
				|| Math.abs(calibrationError
						- this.lastBestCalibrationError) < JUST_NOTICEABLE_ERROR_DIFFERENCE) {
			return 0;
		} else {
			return (this.lastBestCalibrationError - calibrationError);
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

}