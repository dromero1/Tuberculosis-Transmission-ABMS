package datasource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import calibration.CalibrationParameter;
import config.SourceFeatures;

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