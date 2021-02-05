package output;

public class OutputManager {

	/**
	 * New cases (unit: people)
	 */
	private int newCases;

	/**
	 * Handle the 'onNewCase' event
	 */
	public void onNewCase() {
		this.newCases++;
	}

	/**
	 * Reset outputs
	 */
	public void resetOutputs() {
		this.newCases = 0;
	}

	/**
	 * Get new cases
	 */
	public int getNewCases() {
		return this.newCases;
	}

}