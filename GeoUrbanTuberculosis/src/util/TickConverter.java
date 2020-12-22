package util;

import repast.simphony.util.collections.Pair;

public class TickConverter {

	/**
	 * Ticks per minute (unit: ticks)
	 */
	public static final double TICKS_PER_MINUTE = 1.0 / 60;

	/**
	 * Ticks per week (unit: ticks)
	 */
	public static final int TICKS_PER_WEEK = 168;

	/**
	 * Ticks per day (unit: ticks)
	 */
	public static final int TICKS_PER_DAY = 24;

	/**
	 * Ticks per year (unit: ticks)
	 */
	public static final int TICKS_PER_YEAR = 8760;

	/**
	 * Days per week (unit: days)
	 */
	public static final int DAYS_PER_WEEK = 7;

	/**
	 * Private constructor
	 */
	private TickConverter() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Day and time to ticks
	 * 
	 * @param day  Day
	 * @param time Time
	 */
	public static double dayTimeToTicks(int day, double time) {
		return (day - 1) * TICKS_PER_DAY + time;
	}

	/**
	 * Ticks to day and time
	 * 
	 * @param ticks Ticks
	 */
	public static Pair<Integer, Double> ticksToDayTime(double ticks) {
		int day = (int) Math.floor(((ticks / TICKS_PER_DAY) % 7) + 1);
		double time = (((ticks / TICKS_PER_DAY) % 7) + 1 - day) * TICKS_PER_DAY;
		return new Pair<>(day, time);
	}

	/**
	 * Minutes to ticks
	 * 
	 * @param minutes Minutes
	 */
	public static double minutesToTicks(double minutes) {
		return minutes * TICKS_PER_MINUTE;
	}

	/**
	 * Days to ticks
	 * 
	 * @param days Days
	 */
	public static double daysToTicks(double days) {
		return days * TICKS_PER_DAY;
	}

	/**
	 * Ticks to days
	 * 
	 * @param ticks Ticks
	 */
	public static double ticksToDays(double ticks) {
		return ticks / TICKS_PER_DAY;
	}

	/**
	 * Ticks to day of the week
	 * 
	 * @param ticks Ticks
	 */
	public static int ticksToWeekday(double ticks) {
		return (int) ticksToDays(ticks) % DAYS_PER_WEEK;
	}

}