package movement;

import core.Coord;
import core.Settings;
import core.SettingsError;
import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.MapRoute;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duschi on 05.10.2016.
 */
public class SatelliteMovement extends MapRouteMovement implements SwitchableMovement {

	/** Per node group setting used for selecting a route file ({@value}) */
	private static final String ROUTE_FILE_S = "routeFile";
	/**
	 * Per node group setting used for selecting a route's type ({@value}).
	 * Integer value from {@link MapRoute} class.
	 */
	private static final String ROUTE_TYPE_S = "routeType";

	/**
	 * Per node group setting for selecting which stop (counting from 0 from
	 * the start of the route) should be the first one. By default, or if a
	 * negative value is given, a random stop is selected.
	 */
	private static final String ROUTE_FIRST_STOP_S = "routeFirstStop";

	private static final String ROUTE_TIME_PER_STEP = "timePerStep";

	/** the Dijkstra shortest path finder */
	private DijkstraPathFinder pathFinder;

	/** Prototype's reference to all routes read for the group */
	private List<MapRoute> allRoutes = null;
	/** next route's index to give by prototype */
	private Integer nextRouteIndex = null;
	/** Index of the first stop for a group of nodes (or -1 for random) */
	private int firstStopIndex = -1;

	private int stopDifference;

	private String mRouteFileName;

	/** Route of the movement model's instance */
	private MapRoute route;

	/**
	 * Creates a new movement model based on a Settings object's settings.
	 * @param settings The Settings object where the settings are read from
	 */
	public SatelliteMovement(Settings settings) {
		super(settings);
		mRouteFileName = settings.getSetting(ROUTE_FILE_S);
		int type = settings.getInt(ROUTE_TYPE_S);
		allRoutes = MapRoute.readRoutes(mRouteFileName, type, getMap());
		nextRouteIndex = 1;
		stopDifference = 30;
		if (settings.contains(ROUTE_TIME_PER_STEP)) {
			stopDifference = settings.getInt(ROUTE_TIME_PER_STEP);
		}
		pathFinder = new DijkstraPathFinder(getOkMapNodeTypes());
		this.route = this.allRoutes.get(this.nextRouteIndex).replicate();
		if (this.nextRouteIndex >= this.allRoutes.size()) {
			this.nextRouteIndex = 1;
		}

		if (settings.contains(ROUTE_FIRST_STOP_S)) {
			this.firstStopIndex = settings.getInt(ROUTE_FIRST_STOP_S);
			if (this.firstStopIndex >= this.route.getNrofStops()) {
				throw new SettingsError("Too high first stop's index (" +
						this.firstStopIndex + ") for route with only " +
						this.route.getNrofStops() + " stops");
			}
		}
	}

	/**
	 * Copyconstructor. Gives a route to the new movement model from the
	 * list of routes and randomizes the starting position.
	 * @param proto The MapRouteMovement prototype
	 */
	protected SatelliteMovement(SatelliteMovement proto) {
		super(proto);
		this.route = proto.allRoutes.get(proto.nextRouteIndex).replicate();
		this.firstStopIndex = proto.firstStopIndex;
		this.pathFinder = proto.pathFinder;
		this.stopDifference = proto.stopDifference;
		this.nextRouteIndex = proto.nextRouteIndex;
		this.allRoutes = new ArrayList<>(proto.allRoutes);
		mRouteFileName	  = proto.mRouteFileName;
	}

	/**
	 * Path is always from the start to the end
	 * @return
	 */
	@Override
	public Path getPath() {
		Path p = new Path(1.0);
		// don't take the first route, it's just there for connection purposes
		if (nextRouteIndex >= allRoutes.size())
			nextRouteIndex = 1;
		this.route = this.allRoutes.get(this.nextRouteIndex).replicate();

		// first and last nodes will always be the same
		lastMapNode = route.getStops().get(1);
		MapNode to = route.getStops().get(route.getNrofStops()-1);
		List<MapNode> nodePath = pathFinder.getShortestPath(lastMapNode, to);

		// this assertion should never fire if the map is checked in read phase
		assert nodePath.size() > 0 : "No path from " + lastMapNode + " to " +
				to + ". The simulation map isn't fully connected";

		MapNode last = null;
		for (MapNode n : nodePath) {
			if (last == null)
				p.addWaypoint(n.getLocation()); // add the first waypoint w/o speed (defined by constructor)
			else
				p.addWaypoint(n.getLocation(), this.getSpeedFor(last.getLocation(), n.getLocation()));
			last = n;
		}

		lastMapNode = to;
		this.nextRouteIndex++;
		return p;
	}

	/**
	 * Returns the first stop on the route
	 */
	@Override
	public Coord getInitialLocation() {
		return getInitialPosition().clone();
	}

	@Override
	public Coord getLastLocation() {
		if (lastMapNode != null) {
			return lastMapNode.getLocation().clone();
		} else {
			return null;
		}
	}


	@Override
	public SatelliteMovement replicate() {
		return new SatelliteMovement(this);
	}

	/**
	 * Returns the list of stops on the route
	 * @return The list of stops
	 */
	public List<MapNode> getStops() {
		return route.getStops();
	}

	/**
	 * get the initial position of the route
	 * @return initial position of the route
	 */
	public Coord getInitialPosition() {
		// skip the first position which was just added for connectivity
		return route.getStops().get(1).getLocation();
	}

	/**
	 * if router uses contact graph routing we only consider the max speed
	 * Generates and returns a speed value between min and max of the
	 * {@link #WAIT_TIME} setting.
	 * @return A new speed between min and max values or the max speed
	 */
	@Override
	protected double generateSpeed() {
		if (rng == null) {
			return 1;
		}
		return (maxSpeed - minSpeed) * rng.nextDouble() + minSpeed;
	}

	/**
	 * get speed in meter per second
	 * @param location
	 * @param destination
	 * @return
	 */
	protected double getSpeedFor(Coord location, Coord destination) {
		// returned distance is in m
		double distance = location.distance(destination);
		// stopDifference -> time in s needed for this distance
		return distance/(double)stopDifference;
	}

	/**
	 * Generates and returns a suitable waiting time at the end of a path.
	 * in this case one coordinate is missing, so we must wait one hop after the destination was reached
	 * @return The time as a double
	 */
	@Override
	protected double generateWaitTime() {
		return stopDifference;
	}
}
