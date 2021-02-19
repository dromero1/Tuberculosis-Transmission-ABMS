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
	 * Create a new Q-learning-based tuning agent
	 */
	public QLearningTuningAgent() {
		this.qValues = new HashMap<>();
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
		}
	}

	/**
	 * Select action (epsilon-greedy)
	 */
	public Map<String, Double> selectAction() {
		Map<String, Double> parametersSetup = new HashMap<>();
		for (Entry<String, List<Pair<Double, Double>>> parameter : this.qValues
				.entrySet()) {
			List<Pair<Double, Double>> parameterSpace = parameter.getValue();
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
				index = RandomHelper.nextIntFromTo(0,
						parameterSpace.size() - 1);
				selectedPoint = parameterSpace.get(index);
			}
			String parameterId = parameter.getKey();
			double parameterValue = selectedPoint.getFirst();
			parametersSetup.put(parameterId, parameterValue);
		}
		return parametersSetup;
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
		double reward = computeReward(calibrationError, 0.01, 0.05);
		// Update rule
		for (Entry<String, List<Pair<Double, Double>>> parameter : this.qValues
				.entrySet()) {
			String parameterId = parameter.getKey();
			double lastValue = tunableParameters.get(parameterId);
			// Retrieve last point
			List<Pair<Double, Double>> parameterSpace = parameter.getValue();
			int indexLastAction = 0;
			for (int i = 0; i < parameterSpace.size(); i++) {
				Pair<Double, Double> point = parameterSpace.get(i);
				double value = point.getFirst();
				if (Math.abs(value - lastValue) < 1e-10) {
					indexLastAction = i;
					break;
				}
			}
			Pair<Double, Double> lastPoint = parameterSpace
					.get(indexLastAction);
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
			this.qValues.put(parameterId, parameterSpace);
		}
	}

	/**
	 * Fix learning parameters
	 */
	private void fixParameters() {
		// FIX AS SOON AS POSSIBLE
		this.epsilon = 0.2;
		this.learningRate = 0.3;
		this.discountFactor = 0.9;
	}

	/**
	 * Compute reward
	 * 
	 * @param calibrationError Calibration error
	 * @param lowThreshold     Low threshold
	 * @param highThreshold    High threshold
	 */
	private double computeReward(double calibrationError, double lowThreshold,
			double highThreshold) {
		double reward = 0;
		if (calibrationError < lowThreshold) {
			reward = 10 / (calibrationError + 0.01);
		} else if (calibrationError > highThreshold) {
			reward = -10 * (calibrationError - highThreshold);
		}
		return reward;
	}

}