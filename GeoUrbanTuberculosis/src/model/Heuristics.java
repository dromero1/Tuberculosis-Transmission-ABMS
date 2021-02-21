package model;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.collections.Pair;
import simulation.SimulationBuilder;

public final class Heuristics {

	/**
	 * Private constructor
	 */
	private Heuristics() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Get reference spots
	 */
	public static Pair<NdPoint, NdPoint> getReferenceSpots() {
		double householdX = RandomHelper.nextDoubleFromTo(0,
				SimulationBuilder.CITY_WIDTH);
		double householdY = RandomHelper.nextDoubleFromTo(0,
				SimulationBuilder.CITY_LENGTH);
		NdPoint household = new NdPoint(householdX,householdY);
		double workplaceX = RandomHelper.nextDoubleFromTo(0,
				SimulationBuilder.CITY_WIDTH);
		double workplaceY = RandomHelper.nextDoubleFromTo(0,
				SimulationBuilder.CITY_LENGTH);
		NdPoint workplace = new NdPoint(workplaceX,workplaceY);
		return new Pair<>(household, workplace);
	}

}