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
	protected Map<String, List<Pair<Double, Double>>> qValues;

	/**
	 * Epsilon parameter for epsilon-greedy action selection
	 */
	protected double epsilon;

	/**
	 * Learning rate for update rule
	 */
	protected double learningRate;

	/**
	 * Discount factor for update rule
	 */
	protected double discountFactor;

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
	 */
	public void init(Map<String, Double> tunableParameters) {
		for (Entry<String, Double> parameter : tunableParameters.entrySet()) {
			// FIX AS SOON AS POSSIBLE
			double step = parameter.getValue() * 0.10;
			List<Pair<Double, Double>> actions = new ArrayList<>();
			for (int i = 0; i <= 20; i++) {
				double action = i * step;
				double q0 = 0;
				actions.add(new Pair<>(action, q0));
			}
			this.qValues.put(parameter.getKey(), actions);
		}
	}

	/**
	 * Fix learning parameters
	 */
	public void fixParameters() {
		// FIX AS SOON AS POSSIBLE
		this.epsilon = 0.2;
		this.learningRate = 0.3;
		this.discountFactor = 0.9;
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
	 * @param reward Reward
	 */
	public void updateLearning(double reward) {
	}

}