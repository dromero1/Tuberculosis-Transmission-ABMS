package config;

public final class SourceFeatures {

	/**
	 * Calibration setup database - parameter id column
	 */
	public static final int CALIBRATION_SETUP_PARAMETER_ID_COLUMN = 0;

	/**
	 * Calibration setup database - tolerance column
	 */
	public static final int CALIBRATION_SETUP_TOLERANCE_COLUMN = 1;

	/**
	 * Calibration setup database - lower bound column
	 */
	public static final int CALIBRATION_LOWER_BOUND_COLUMN = 2;

	/**
	 * Calibration setup database - upper bound column
	 */
	public static final int CALIBRATION_UPPER_BOUND_COLUMN = 3;

	/**
	 * Private constructor
	 */
	private SourceFeatures() {
		throw new UnsupportedOperationException("Utility class");
	}

}