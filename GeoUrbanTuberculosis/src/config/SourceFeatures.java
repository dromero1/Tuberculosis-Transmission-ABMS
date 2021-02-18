package config;

public final class SourceFeatures {

	/**
	 * Citizens' locations database - id column
	 */
	public static final int CITIZEN_LOCATIONS_ID_COLUMN = 0;

	/**
	 * Citizens' locations database - household's X coordinate column
	 */
	public static final int CITIZENS_LOCATIONS_HOUSEHOLD_X_COLUMN = 1;

	/**
	 * Citizens' locations database - household's Y coordinate column
	 */
	public static final int CITIZENS_LOCATIONS_HOUSEHOLD_Y_COLUMN = 2;

	/**
	 * Citizens' locations database - household's type column
	 */
	public static final int CITIZENS_LOCATIONS_HOUSEHOLD_TYPE_COLUMN = 3;

	/**
	 * Citizens' locations database - workplace's X coordinate column
	 */
	public static final int CITIZENS_LOCATIONS_WORKPLACE_X_COLUMN = 4;

	/**
	 * Citizens' locations database - workplace's Y coordinate column
	 */
	public static final int CITIZENS_LOCATIONS_WORKPLACE_Y_COLUMN = 5;

	/**
	 * Citizens' locations database - workplace's type column
	 */
	public static final int CITIZENS_LOCATIONS_WORKPLACE_TYPE_COLUMN = 6;

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