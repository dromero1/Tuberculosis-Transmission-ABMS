package model;

import java.util.List;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.collections.Pair;

public final class Heuristics {

	/**
	 * Private constructor
	 */
	private Heuristics() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Get reference spots
	 * 
	 * @param locations Locations
	 */
	public static Pair<NdPoint, NdPoint> getReferenceSpots(
			List<Pair<NdPoint, NdPoint>> locations) {
		int index = RandomHelper.nextIntFromTo(0, locations.size() - 1);
		return locations.get(index);
	}

}