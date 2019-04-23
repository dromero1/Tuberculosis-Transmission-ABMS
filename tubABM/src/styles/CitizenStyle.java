/**
 * 
 */
package styles;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import java.awt.Color;

import model.Citizen;
import model.DiseaseStage;

/**
 * @author david
 *
 */
public class CitizenStyle extends DefaultStyleOGL2D {

	@Override
	public Color getColor(Object object) {
		Citizen citizen = (Citizen) object;
		int diseaseStage = citizen.getDiseaseStage();
		if (diseaseStage == DiseaseStage.EXPOSED) {
			return Color.BLACK;
		} else if (diseaseStage == DiseaseStage.INFECTED) {
			return Color.RED;
		} else if (diseaseStage == DiseaseStage.RECOVERED) {
			return Color.GREEN;
		} else if (diseaseStage == DiseaseStage.ON_TREATMENT) {
			return Color.ORANGE;
		}
		return Color.BLUE;
	}
	
	@Override
	public float getScale(Object object) {
		Citizen citizen = (Citizen) object;
		int diseaseStage = citizen.getDiseaseStage();
		if (diseaseStage == DiseaseStage.EXPOSED ||
			diseaseStage == DiseaseStage.RECOVERED ||
			diseaseStage == DiseaseStage.ON_TREATMENT) {
			return 3.0f;
		} else if (diseaseStage == DiseaseStage.INFECTED) {
			return 4.0f;
		}
		return 2.0f;
	}

}
