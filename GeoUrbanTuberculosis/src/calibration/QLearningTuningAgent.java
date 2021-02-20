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
	private String currentParameter;

	/**
	 * Last calibration error
	 */
	private double lastCalibrationError;

	/**
	 * Update counter
	 */
	private double updateCounter;

	/**
	 * Create a new Q-learning-based tuning agent
	 */
	public QLearningTuningAgent() {
		this.qValues = new HashMap<>();
		this.parametersTags = new ArrayList<>();
		this.lastCalibrationError = Double.POSITIVE_INFINITY;
		fixParameters();
	}

	/**
	 * Initialize learning
	 * 
	 * @param tunableParameters Tunable parameters
	 * @param calibrationSetup  Calibration setup
	 */
	public void init(Map<String, Double> tunableParameters,
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
				double q0 = 0;
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
		double reward = computeReward(calibrationError,
				this.lastCalibrationError);
		// Get parameter space
		List<Pair<Double, Double>> parameterSpace = this.qValues
				.get(this.currentParameter);
		// Retrieve last point
		double lastValue = tunableParameters.get(this.currentParameter);
		int indexLastAction = -1;
		for (int i = 0; i < parameterSpace.size(); i++) {
			Pair<Double, Double> point = parameterSpace.get(i);
			double value = point.getFirst();
			if (Math.abs(value - lastValue) < 1e-10) {
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
		if (true) {
			System.out.printf(
					"(Error = %.4f, Reward = %.4f, Param. %s, Value = %.4f, Q-value = %.4f)%n",
					calibrationError, reward, this.currentParameter, lastValue,
					qValue);
		}
		// Check parameter change
		if (this.updateCounter >= 5) {
			resetCurrentParameter();
		}
		// Update last calibration error
		this.lastCalibrationError = calibrationError;
		// Update counter
		this.updateCounter++;
	}

	/**
	 * Fix learning parameters
	 */
	private void fixParameters() {
		// FIX AS SOON AS POSSIBLE
		this.epsilon = 0.1;
		this.learningRate = 0.1;
		this.discountFactor = 0.8;
	}

	/**
	 * Compute reward
	 * 
	 * @param calibrationError     Calibration error
	 * @param lastCalibrationError Last calibration error
	 */
	private double computeReward(double calibrationError,
			double lastCalibrationError) {
		double reward = 0;
		if (calibrationError < lastCalibrationError) {
			reward = 1;
		} else if (calibrationError > lastCalibrationError) {
			reward = -1;
		}
		return reward;
	}

	/**
	 * Reset current parameter
	 */
	private void resetCurrentParameter() {
		int index = RandomHelper.nextIntFromTo(0,
				this.parametersTags.size() - 1);
		this.currentParameter = this.parametersTags.get(index);
		this.updateCounter = 0;
	}

}