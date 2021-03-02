package calibration;

import repast.simphony.util.collections.Pair;

public class ParetoOptimalSetup {

	/**
	 * Similarity threshold
	 */
	public static final double SIMILARITY_THRESHOLD = 0.10;

	/**
	 * Parameter id
	 */
	private String parameterId;

	/**
	 * Parameter value
	 */
	private double parameterValue;

	/**
	 * Incidence rate error
	 */
	private double incidenceRateError;

	/**
	 * Exposure rate error
	 */
	private double exposureRateError;

	/**
	 * Create a new Pareto optimal setup
	 * 
	 * @param parameterId       Parameter id
	 * @param parameterValue    Parameter value
	 * @param calibrationErrors Calibration errors
	 */
	public ParetoOptimalSetup(String parameterId, double parameterValue,
			Pair<Double, Double> calibrationErrors) {
		this.parameterId = parameterId;
		this.parameterValue = parameterValue;
		this.incidenceRateError = calibrationErrors.getFirst();
		this.exposureRateError = calibrationErrors.getSecond();
	}

	/**
	 * Is almost the same as the reference setup?
	 * 
	 * @param setup Setup
	 */
	public boolean isAlmostTheSameAs(ParetoOptimalSetup setup) {
		if (this.parameterId.equals(setup.parameterId)
				&& this.parameterValue == setup.parameterValue) {
			return (Math
					.abs(this.incidenceRateError
							- setup.incidenceRateError) < SIMILARITY_THRESHOLD
					&& Math.abs(this.exposureRateError
							- setup.exposureRateError) < SIMILARITY_THRESHOLD);
		} else {
			return false;
		}
	}

	/**
	 * Get parameter id
	 */
	public String getParameterId() {
		return this.parameterId;
	}

	/**
	 * Get parameter value
	 */
	public double getParameterValue() {
		return this.parameterValue;
	}

	/**
	 * Get incidence rate error
	 */
	public double getIncidenceRateError() {
		return this.incidenceRateError;
	}

	/**
	 * Get exposure rate error
	 */
	public double getExposureRateError() {
		return this.exposureRateError;
	}

}