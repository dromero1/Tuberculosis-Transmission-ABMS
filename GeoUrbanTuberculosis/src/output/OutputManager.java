package output;

public class OutputManager {

	/**
	 * Infections
	 */
	private int infections;

	/**
	 * Number of exposures
	 */
	private int exposures;

	/**
	 * Handle the 'onNewInfection' event
	 */
	public void onNewInfection() {
		this.infections++;
	}

	/**
	 * Handle the 'onNewExposure' event
	 */
	public void onNewExposure() {
		this.exposures++;
	}

	/**
	 * Get infections
	 */
	public int getInfections() {
		return this.infections;
	}

	/**
	 * Get exposures
	 */
	public int getExposures() {
		return this.exposures;
	}

	/**
	 * Reset outputs
	 */
	public void resetOutputs() {
		this.infections = 0;
		this.exposures = 0;
	}

}