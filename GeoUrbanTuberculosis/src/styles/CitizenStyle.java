package styles;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import java.awt.Color;
import model.Citizen;
import model.Compartment;

public class CitizenStyle extends DefaultStyleOGL2D {

	/**
	 * Get color
	 * 
	 * @param object Object
	 */
	@Override
	public Color getColor(Object object) {
		Citizen citizen = (Citizen) object;
		Compartment compartment = citizen.getCompartment();
		switch (compartment) {
		case EXPOSED:
			return Color.ORANGE;
		case IMMUNE:
			return Color.GREEN;
		case INFECTED:
			return Color.RED;
		case ON_TREATMENT:
			return Color.CYAN;
		default:
			return Color.BLUE;
		}
	}

	/**
	 * Get scale
	 * 
	 * @param object Object
	 */
	@Override
	public float getScale(Object object) {
		Citizen citizen = (Citizen) object;
		Compartment compartment = citizen.getCompartment();
		switch (compartment) {
		case EXPOSED:
		case IMMUNE:
		case ON_TREATMENT:
			return 3.0f;
		case INFECTED:
			return 4.0f;
		default:
			return 2.0f;
		}
	}

}