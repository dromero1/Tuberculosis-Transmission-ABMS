package simulation;

import java.util.ArrayList;
import java.util.List;
import calibration.Calibrator;
import config.SourcePaths;
import datasource.Reader;
import model.Citizen;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.collections.Pair;

public class SimulationBuilder implements ContextBuilder<Object> {

	/**
	 * Ticks per simulation run (unit: hours)
	 */
	public static final double TICKS_PER_RUN = 8760;

	/**
	 * Ticks between simulation runs (unit: hours)
	 */
	public static final double TICKS_BETWEEN_RUNS = 100;
	
	/**
	 * Maximum number of runs (unit: runs)
	 */
	public static final double MAX_RUNS = 100;

	/**
	 * City's length
	 */
	public static final int CITY_LENGTH = 500;

	/**
	 * City's width
	 */
	public static final int CITY_WIDTH = 500;

	/**
	 * Space projection id
	 */
	public static final String SPACE_PROJECTION_ID = "space";

	/**
	 * Grid projection id
	 */
	public static final String GRID_PROJECTION_ID = "grid";

	/**
	 * Reference to space projection
	 */
	public ContinuousSpace<Object> space;

	/**
	 * Reference to grid projection
	 */
	public Grid<Object> grid;

	/**
	 * Reference to context
	 */
	public Context<Object> context;

	/**
	 * Reference to calibrator
	 */
	public Calibrator calibrator;

	/**
	 * Citizens
	 */
	public List<Citizen> citizens;

	/**
	 * Citizens' locations
	 */
	public List<Pair<NdPoint, NdPoint>> locations;

	/**
	 * Build simulation
	 * 
	 * @param context Simulation context
	 */
	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("GeoUrbanTuberculosis");
		// Save context
		this.context = context;
		// Create continuous space projection
		this.space = createContinuousSpaceProjection(context);
		// Create grid projection
		this.grid = createGridProjection(context);
		// Read citizens' locations
		this.locations = Reader
				.readCitizensLocations(SourcePaths.CITIZENS_LOCATIONS_DATABASE);
		// Add citizens to the simulation
		this.citizens = createCitizens();
		for (Citizen citizen : this.citizens) {
			this.context.add(citizen);
		}
		// Add calibrator to the simulation
		this.calibrator = new Calibrator();
		this.context.add(this.calibrator);
		return context;
	}

	/**
	 * Create continuous space projection
	 * 
	 * @param context Simulation context
	 */
	private ContinuousSpace<Object> createContinuousSpaceProjection(
			Context<Object> context) {
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		return spaceFactory.createContinuousSpace(SPACE_PROJECTION_ID, context,
				new SimpleCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(),
				CITY_LENGTH, CITY_WIDTH);
	}

	/**
	 * Create grid projection
	 * 
	 * @param context Simulation context
	 */
	private Grid<Object> createGridProjection(Context<Object> context) {
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		return gridFactory.createGrid(GRID_PROJECTION_ID, context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<>(), true, CITY_LENGTH,
						CITY_WIDTH));
	}

	/**
	 * Create citizens
	 */
	private List<Citizen> createCitizens() {
		int susceptibleCount = ParametersAdapter.getSusceptibleCount();
		int exposedCount = ParametersAdapter.getExposedCount();
		List<Citizen> citizensList = new ArrayList<>();
		for (int i = 0; i < exposedCount; i++) {
			Citizen citizen = new Citizen(this, true);
			citizensList.add(citizen);
		}
		for (int i = 0; i < susceptibleCount; i++) {
			Citizen citizen = new Citizen(this, false);
			citizensList.add(citizen);
		}
		return citizensList;
	}

}