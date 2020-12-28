package model;

import cern.jet.random.Exponential;
import repast.simphony.random.RandomHelper;
import simulation.ParametersAdapter;

public final class Randomizer {

	/**
	 * Minimum wake up time (unit: hours)
	 */
	public static final double MIN_WAKE_UP_TIME = 6;

	/**
	 * Maximum wake up time (unit: hours)
	 */
	public static final double MAX_WAKE_UP_TIME = 9;

	/**
	 * Minimum returning home time (unit: hours)
	 */
	public static final double MIN_RETURN_HOME_TIME = 17;

	/**
	 * Maximum returning home time (unit: hours)
	 */
	public static final double MAX_RETURN_HOME_TIME = 20;

	/**
	 * Minimum incubation period (unit: days)
	 */
	public static final double MIN_INCUBATION_PERIOD = 15;

	/**
	 * Maximum incubation period (unit: days)
	 */
	public static final double MAX_INCUBATION_PERIOD = 84;

	/**
	 * Mean pulmonary ventilation rate (unit: cubic meters per hour)
	 */
	public static final double MEAN_PULMONARY_VENTILATION_RATE = 0.48;

	/**
	 * Minimum treatment duration (unit: days)
	 */
	public static final double MIN_TREATMENT_DURATION = 180;

	/**
	 * Maximum treatment duration (unit: days)
	 */
	public static final double MAX_TREATMENT_DURATION = 244;

	/**
	 * Minimum days to full recovery (unit: days)
	 */
	public static final double MIN_DAYS_TO_FULL_RECOVERY = 608;

	/**
	 * Maximum days to full recovery (unit: days)
	 */
	public static final double MAX_DAYS_TO_FULL_RECOVERY = 852;

	/**
	 * Minimum treatment duration (unit: days)
	 */
	public static final double TREATMENT_DURATION = 180;

	/**
	 * Mean quanta production (unit: droplets per hour)
	 */
	public static final double MEAN_QUANTA_PRODUCTION = 1.25;

	/**
	 * Immunodeficiency fold
	 */
	public static final double IMMUNODEFICIENCY_FOLD = 10;

	/**
	 * Risk factor fold
	 */
	public static final double RISK_FACTOR_FOLD = 1.5;

	/**
	 * Private constructor
	 */
	private Randomizer() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Get random wake up time (unit: hours)
	 */
	public static double getRandomWakeUpTime() {
		return RandomHelper.nextDoubleFromTo(MIN_WAKE_UP_TIME,
				MAX_WAKE_UP_TIME);
	}

	/**
	 * Get random returning home time (unit: hours)
	 */
	public static double getRandomReturningHomeTime() {
		return RandomHelper.nextDoubleFromTo(MIN_RETURN_HOME_TIME,
				MAX_RETURN_HOME_TIME);
	}

	/**
	 * Get random incubation period (unit: days)
	 */
	public static double getRandomIncubationPeriod() {
		return RandomHelper.nextDoubleFromTo(MIN_INCUBATION_PERIOD,
				MAX_INCUBATION_PERIOD);
	}

	/**
	 * Get random days to diagnosis (unit: days)
	 */
	public static double getRandomDaysToDiagnosis() {
		double meanDiagnosisDelay = ParametersAdapter.getMeanDiagnosisDelay();
		double lambda = 1 / meanDiagnosisDelay;
		Exponential exp = RandomHelper.createExponential(lambda);
		return exp.nextDouble();
	}

	/**
	 * Get random treatment duration (unit: days)
	 */
	public static double getRandomTreatmentDuration() {
		return RandomHelper.nextDoubleFromTo(MIN_TREATMENT_DURATION,
				MAX_TREATMENT_DURATION);
	}

	/**
	 * Get random days to full recovery (unit: days)
	 */
	public static double getRandomDaysToFullRecovery() {
		return RandomHelper.nextDoubleFromTo(MIN_DAYS_TO_FULL_RECOVERY,
				MAX_DAYS_TO_FULL_RECOVERY);
	}

	/**
	 * Get random immunodeficiency
	 */
	public static boolean getRandomImmunodeficiency() {
		double p = ParametersAdapter.getImmunodepressionShare();
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		return r <= p;
	}

	/**
	 * Get random smoker
	 */
	public static boolean getRandomSmoker() {
		double p = ParametersAdapter.getSmokersShare();
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		return r <= p;
	}

	/**
	 * Get random alcohol drinker
	 */
	public static boolean getRandomAlcoholDrinker() {
		double p = ParametersAdapter.getAlcoholDrinkersShare();
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		return r <= p;
	}

	/**
	 * Is the citizen getting exposed?
	 * 
	 * @param infectedPeople Infected people around
	 */
	public static boolean isGettingExposed(int infectedPeople) {
		double aVr = ParametersAdapter.getAverageRoomVentilationRate();
		double aRv = ParametersAdapter.getAverageRoomVolume();
		double phi = infectedPeople * MEAN_QUANTA_PRODUCTION;
		double p = (MEAN_PULMONARY_VENTILATION_RATE * phi) / (aRv * aVr);
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		return r <= p;
	}

	/**
	 * Is the citizen dropping treatment?
	 */
	public static boolean isDroppingOutTreatment() {
		double p = ParametersAdapter.getTreatmentDropoutRate();
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		return r <= p;
	}

	/**
	 * Is the citizen getting infected?
	 * 
	 * @param citizen Citizen
	 */
	public static boolean isGettingInfected(Citizen citizen) {
		double p = ParametersAdapter.getInfectionProbability();
		if (citizen.isImmunodepressed()) {
			p *= IMMUNODEFICIENCY_FOLD;
		}
		if (citizen.smokes()) {
			p *= RISK_FACTOR_FOLD;
		}
		if (citizen.drinksAlcohol()) {
			p *= RISK_FACTOR_FOLD;
		}
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		return r <= p;
	}

}