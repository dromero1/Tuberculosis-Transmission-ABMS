/**
 * 
 */
package model;

public class InfectionProbabilityCalculator {

	public static double calculateProbability(int infectedPeople, double pulmonaryVentilationRate,
			double roomVentilationRate, double averageQuantaProductionPerPerson, double roomVolume) {

		double phi = infectedPeople * averageQuantaProductionPerPerson;
		return (pulmonaryVentilationRate * phi) / (roomVolume * roomVentilationRate);
	}

}
