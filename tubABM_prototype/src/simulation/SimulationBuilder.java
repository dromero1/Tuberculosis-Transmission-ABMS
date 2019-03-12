/**
 * 
 */
package simulation;

import agents.Citizen;
import agents.DiseaseStage;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
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
		context.setId("tuberculosisABM");

		// Create continuous space projection
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context,
				new RandomCartesianAdder<Object>(), 
				new repast.simphony.space.continuous.WrapAroundBorders(), 50, 50);

		// Create grid projection
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(
				new WrapAroundBorders(), 
				new SimpleGridAdder<Object>(), true, 50, 50));

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

		// Move citizens to the grid locations
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}

		return context;
	}
	
}
