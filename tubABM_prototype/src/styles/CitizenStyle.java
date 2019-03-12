/**
 * 
 */
package styles;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import java.awt.Color;
import agents.Citizen;
import agents.DiseaseStage;

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
        }
		return Color.BLUE;
    }
	
}
