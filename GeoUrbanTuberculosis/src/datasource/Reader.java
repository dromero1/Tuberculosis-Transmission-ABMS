package datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import calibration.CalibrationParameter;
import config.SourceFeatures;
import java.io.File;
import java.io.FileNotFoundException;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.collections.Pair;

public final class Reader {

	/**
	 * CSV source split regular expression
	 */
	private static final String SOURCE_SPLIT_REGEX = ",";

	/**
	 * Private constructor
	 */
	private Reader() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Read citizens' locations
	 * 
	 * @param filename File name
	 */
	public static List<Pair<NdPoint, NdPoint>> readCitizensLocations(
			String filename) {
		List<Pair<NdPoint, NdPoint>> locations = new ArrayList<>();
		File file = new File(filename);
		try (Scanner scanner = new Scanner(file)) {
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
				} else {
					String[] elements = data.split(SOURCE_SPLIT_REGEX);
					int householdX = 0;
					int householdY = 0;
					int workplaceX = 0;
					int workplaceY = 0;
					for (int i = 0; i < elements.length; i++) {
						switch (i) {
						case SourceFeatures.CITIZENS_LOCATIONS_HOUSEHOLD_X_COLUMN:
							householdX = Integer.parseInt(elements[i]);
							break;
						case SourceFeatures.CITIZENS_LOCATIONS_HOUSEHOLD_Y_COLUMN:
							householdY = Integer.parseInt(elements[i]);
							break;
						case SourceFeatures.CITIZENS_LOCATIONS_WORKPLACE_X_COLUMN:
							workplaceX = Integer.parseInt(elements[i]);
							break;
						case SourceFeatures.CITIZENS_LOCATIONS_WORKPLACE_Y_COLUMN:
							workplaceY = Integer.parseInt(elements[i]);
							break;
						default:
							break;
						}
					}
					NdPoint household = new NdPoint(householdX, householdY);
					NdPoint workplace = new NdPoint(workplaceX, workplaceY);
					Pair<NdPoint, NdPoint> reference = new Pair<>(household,
							workplace);
					locations.add(reference);
				}
			}
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		return locations;
	}

	/**
	 * Read calibration setup
	 * 
	 * @param filename File name
	 */
	public static Map<String, CalibrationParameter> readCalibrationSetup(
			String filename) {
		Map<String, CalibrationParameter> setup = new HashMap<>();
		File file = new File(filename);
		try (Scanner scanner = new Scanner(file)) {
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
				} else {
					String[] elements = data.split(SOURCE_SPLIT_REGEX);
					String parameterId = "";
					double tolerance = 0.0;
					double lowerBound = 0.0;
					double upperBound = 0.0;
					for (int i = 0; i < elements.length; i++) {
						switch (i) {
						case SourceFeatures.CALIBRATION_SETUP_PARAMETER_ID_COLUMN:
							parameterId = elements[i];
							break;
						case SourceFeatures.CALIBRATION_SETUP_TOLERANCE_COLUMN:
							tolerance = Double.parseDouble(elements[i]);
							break;
						case SourceFeatures.CALIBRATION_LOWER_BOUND_COLUMN:
							lowerBound = Double.parseDouble(elements[i]);
							break;
						case SourceFeatures.CALIBRATION_UPPER_BOUND_COLUMN:
							upperBound = Double.parseDouble(elements[i]);
							break;
						default:
							break;
						}
					}
					CalibrationParameter parameter = new CalibrationParameter(
							parameterId, tolerance, lowerBound, upperBound);
					setup.put(parameterId, parameter);
				}
			}
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		return setup;
	}

}