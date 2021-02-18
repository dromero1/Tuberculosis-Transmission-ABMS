package calibration;

public class CalibrationParameter {

	/**
	 * Parameter id
	 */
	private String parameterId;

	/**
	 * Tolerance
	 */
	private double tolerance;

	/**
	 * Lower bound
	 */
	private double lowerBound;

	/**
	 * Upper bound
	 */
	private double upperBound;

	/**
	 * Create a new calibration parameter
	 * 
	 * @param parameterId Parameter id
	 * @param tolerance   Tolerance
	 * @param lowerBound  Lower bound
	 * @param upperBound  Upper bound
	 */
	public CalibrationParameter(String parameterId, double tolerance,
			double lowerBound, double upperBound) {
		this.parameterId = parameterId;
		this.tolerance = tolerance;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	/**
	 * Get parameter id
	 */
	public String getParameterId() {
		return this.parameterId;
	}

	/**
	 * Get tolerance
	 */
	public double getTolerance() {
		return this.tolerance;
	}

	/**
	 * Get lower bound
	 */
	public double getLowerBound() {
		return this.lowerBound;
	}

	/**
	 * Get upper bound
	 */
	public double getUpperBound() {
		return this.upperBound;
	}

}