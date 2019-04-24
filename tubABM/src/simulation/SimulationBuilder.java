/**
 * 
 */
package simulation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.Citizen;
import model.DiseaseStage;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

/**
 * @author david
 *
 */
public class SimulationBuilder implements ContextBuilder<Object> {

	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("tubABM");

		// Create continuous space projection
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context,
				new SimpleCartesianAdder<Object>(), new repast.simphony.space.continuous.WrapAroundBorders(), 500, 500);

		// Create grid projection
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(
				new WrapAroundBorders(), new SimpleGridAdder<Object>(), true, 500, 500));

		// Get simulation parameters
		Parameters params = RunEnvironment.getInstance().getParameters();

		// Add susceptible citizens
		int susceptibleCount = params.getInteger("SusceptibleCount");
		for (int i = 0; i < susceptibleCount; i++) {
			context.add(new Citizen(space, grid, DiseaseStage.SUSCEPTIBLE));
		}

		// Add infected citizens
		int infectedCount = params.getInteger("InfectedCount");
		for (int i = 0; i < infectedCount; i++) {
			context.add(new Citizen(space, grid, DiseaseStage.INFECTED));
		}

		// Add infected exposed
		int exposedCount = params.getInteger("ExposedCount");
		for (int i = 0; i < exposedCount; i++) {
			context.add(new Citizen(space, grid, DiseaseStage.EXPOSED));
		}

		// Read citizen locations
		List<List<String>> records = new ArrayList<List<String>>();
		try (BufferedReader br = new BufferedReader(new FileReader("./data/citizen_locations.csv"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",", 0);
				records.add(Arrays.asList(values));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Set citizens to locations and move them in the projections
		int i = 1;
		for (Object obj : context) {
			Citizen citizen = (Citizen) obj;

			// Get location
			List<String> location = records.get(i);
			int houseHoldX = Integer.parseInt(location.get(1));
			int houseHoldY = Integer.parseInt(location.get(2));
			int workplaceX = Integer.parseInt(location.get(4));
			int workplaceY = Integer.parseInt(location.get(5));

			// Set citizen locations
			citizen.setHomeplaceLocation(houseHoldX, houseHoldY);
			citizen.setWorkplaceLocation(workplaceX, workplaceY);

			// Move citizen in the projections
			grid.moveTo(citizen, houseHoldX, houseHoldY);
			space.moveTo(citizen, houseHoldX, houseHoldY);

			i++;
		}
		
		// Set termination tick
		RunEnvironment.getInstance().endAt(87600);
		
		return context;
	}

}
