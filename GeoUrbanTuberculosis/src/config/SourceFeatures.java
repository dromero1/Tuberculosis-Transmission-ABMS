package config;

public final class SourceFeatures {

	/**
	 * Citizen's locations database - id column
	 */
	public static final int CITIZEN_LOCATIONS_ID_COLUMN = 0;

	/**
	 * Citizen's locations database - household's X coordinate column
	 */
	public static final int CITIZEN_LOCATIONS_HOUSEHOLD_X_COLUMN = 1;

	/**
	 * Citizen's locations database - household's Y coordinate column
	 */
	public static final int CITIZEN_LOCATIONS_HOUSEHOLD_Y_COLUMN = 2;

	/**
	 * Citizen's locations database - household's type column
	 */
	public static final int CITIZEN_LOCATIONS_HOUSEHOLD_TYPE_COLUMN = 3;

	/**
	 * Citizen's locations database - workplace's X coordinate column
	 */
	public static final int CITIZEN_LOCATIONS_WORKPLACE_X_COLUMN = 4;

	/**
	 * Citizen's locations database - workplace's Y coordinate column
	 */
	public static final int CITIZEN_LOCATIONS_WORKPLACE_Y_COLUMN = 5;

	/**
	 * Citizen's locations database - workplace's type column
	 */
	public static final int CITIZEN_LOCATIONS_WORKPLACE_TYPE_COLUMN = 6;

	/**
	 * Private constructor
	 */
	private SourceFeatures() {
		throw new UnsupportedOperationException("Utility class");
	}

}