/**
 * 
 */
package model;

public class InfectionProbabilityCalculator {

	public static double calculateProbability(int infectedPeople, double pulmonaryVentilationRate,
			double roomVentilationRate, double averageQuantaProductionPerPerson, double roomSpace) {

		double phi = infectedPeople * averageQuantaProductionPerPerson;
		return (pulmonaryVentilationRate * phi) / (roomSpace * roomVentilationRate);
	}

}
