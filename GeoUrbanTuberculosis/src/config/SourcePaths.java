package config;

public final class SourcePaths {

	/**
	 * Citizens' locations database
	 */
	public static final String CITIZENS_LOCATIONS_DATABASE = "./data/citizens-locations.csv";

	/**
	 * Calibration setup database
	 */
	public static final String CALIBRATION_SETUP_DATABASE = "./data/calibration-setup.csv";

	/**
	 * Private constructor
	 */
	private SourcePaths() {
		throw new UnsupportedOperationException("Utility class");
	}

}