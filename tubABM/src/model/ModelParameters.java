/**
 * 
 */
package model;

public class ModelParameters {

	// Intuition
	public static final int INITIAL_WAKEUP_TIME = 5; // hour
	public static final int FINAL_WAKEUP_TIME = 8; // hour
	public static final int MIN_WORKTIME = 9; // hours per day
	public static final int MAX_WORKTIME = 11; // hours per day

	// Taken from ...
	public static final double IMMUNODEFICIENCY_FOLD = 10;
	public static final double RISK_FACTOR_ADJUSTMENT = 1.5;

	// Taken from Beggs et. al. (2003)
	public static final double AVG_PATIENT_QUANTA_PRODUCTION = 1.25; // droplets per hour

	// Taken from Noakes et al. (2006)
	public static final double AVG_PULMONARY_VENTILATION_RATE = 0.48; // m3 per hour

	// Simple conversions
	public static final int DAY_IN_HOURS = 24;
	public static final int WEEK_IN_HOURS = 168;
	public static final int YEAR_IN_HOURS = 8760;
	public static final int WEEKS_IN_YEAR = 52;

}
