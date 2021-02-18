package simulation;

import java.util.ArrayList;
import java.util.List;
import calibration.CalibrationParameter;
import calibration.Calibrator;
import config.SourcePaths;
import datasource.Reader;
import model.Citizen;
import output.OutputManager;
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
	 * Citizens' locations
	 */
	public List<Pair<NdPoint, NdPoint>> locations;

	/**
	 * Calibration parameters
	 */
	public List<CalibrationParameter> calibrationSetup;

	/**
	 * Reference to parameters adapter
	 */
	public ParametersAdapter parametersAdapter;

	/**
	 * Output manager
	 */
	public OutputManager outputManager;

	/**
	 * Reference to calibrator
	 */
	private Calibrator calibrator;

	/**
	 * Build simulation
	 * 
	 * @param context Simulation context
	 */
	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("GeoUrbanTuberculosis");
		// Create continuous space projection
		this.space = createContinuousSpaceProjection(context);
		// Create grid projection
		this.grid = createGridProjection(context);
		// Read citizens' locations
		this.locations = Reader
				.readCitizensLocations(SourcePaths.CITIZENS_LOCATIONS_DATABASE);
		// Read calibration parameters
		this.calibrationSetup = Reader
				.readCalibrationSetup(SourcePaths.CALIBRATION_SETUP_DATABASE);
		// Initialize parameters' adapter
		this.parametersAdapter = new ParametersAdapter();
		context.add(this.parametersAdapter);
		// Initialize citizens
		List<Citizen> citizens = createCitizens();
		for (Citizen citizen : citizens) {
			context.add(citizen);
		}
		// Initialize calibrator
		this.calibrator = new Calibrator(this);
		context.add(this.calibrator);
		// Initialize output manager
		this.outputManager = new OutputManager();
		context.add(this.outputManager);
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
		int susceptibleCount = this.parametersAdapter.getSusceptibleCount();
		int exposedCount = this.parametersAdapter.getExposedCount();
		List<Citizen> citizens = new ArrayList<>();
		for (int i = 0; i < exposedCount; i++) {
			Citizen citizen = new Citizen(this, true);
			citizens.add(citizen);
		}
		for (int i = 0; i < susceptibleCount; i++) {
			Citizen citizen = new Citizen(this, false);
			citizens.add(citizen);
		}
		return citizens;
	}

}