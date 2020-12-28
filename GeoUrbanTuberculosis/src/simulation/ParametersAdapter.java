package simulation;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

public final class ParametersAdapter {

	/**
	 * Exposed count parameter id
	 */
	private static final String EXPOSED_COUNT_PARAM_ID = "exposedCount";

	/**
	 * Susceptible count parameter id
	 */
	private static final String SUSCEPTIBLE_COUNT_PARAM_ID = "susceptibleCount";

	/**
	 * Alcohol drinkers share parameter id
	 */
	private static final String ALCOHOL_DRINKERS_SHARE_PARAM_ID = "alcoholDrinkersShare";

	/**
	 * Immunodepression share parameter id
	 */
	private static final String IMMUNODEPRESSION_SHARE_PARAM_ID = "immunodepressionShare";

	/**
	 * Smokers share parameter id
	 */
	private static final String SMOKERS_SHARE_PARAM_ID = "smokersShare";

	/**
	 * Average room volume parameter id
	 */
	private static final String AVERAGE_ROOM_VOLUME_PARAM_ID = "averageRoomVolume";

	/**
	 * Average room ventilation rate parameter id
	 */
	private static final String AVERAGE_ROOM_VENTILATION_RATE_PARAM_ID = "averageRoomVentilationRate";

	/**
	 * Mean diagnosis delay parameter id
	 */
	private static final String MEAN_DIAGNOSIS_DELAY_PARAM_ID = "meanDiagnosisDelay";

	/**
	 * Treatment dropout rate parameter id
	 */
	private static final String TREATMENT_DROPOUT_RATE_PARAM_ID = "treatmentDropoutRate";

	/**
	 * Exposure probability parameter id
	 */
	private static final String EXPOSURE_PROBABILITY_PARAM_ID = "exposureProbability";

	/**
	 * Private constructor
	 */
	private ParametersAdapter() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Get exposed count
	 */
	public static int getExposedCount() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getInteger(EXPOSED_COUNT_PARAM_ID);
	}

	/**
	 * Get susceptible count
	 */
	public static int getSusceptibleCount() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getInteger(SUSCEPTIBLE_COUNT_PARAM_ID);
	}

	/**
	 * Get immunodepression share
	 */
	public static double getImmunodepressionShare() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(IMMUNODEPRESSION_SHARE_PARAM_ID);
	}

	/**
	 * Get alcohol drinkers share
	 */
	public static double getAlcoholDrinkersShare() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(ALCOHOL_DRINKERS_SHARE_PARAM_ID);
	}

	/**
	 * Get smokers share
	 */
	public static double getSmokersShare() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(SMOKERS_SHARE_PARAM_ID);
	}

	/**
	 * Get average room volume
	 */
	public static double getAverageRoomVolume() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(AVERAGE_ROOM_VOLUME_PARAM_ID);
	}

	/**
	 * Get average room ventilation rate
	 */
	public static double getAverageRoomVentilationRate() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(AVERAGE_ROOM_VENTILATION_RATE_PARAM_ID);
	}

	/**
	 * Get mean diagnosis delay
	 */
	public static double getMeanDiagnosisDelay() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(MEAN_DIAGNOSIS_DELAY_PARAM_ID);
	}

	/**
	 * Get treatment dropout rate
	 */
	public static double getTreatmentDropoutRate() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(TREATMENT_DROPOUT_RATE_PARAM_ID);
	}

	/**
	 * Get exposure probability
	 */
	public static double getExposureProbability() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(EXPOSURE_PROBABILITY_PARAM_ID);
	}

}