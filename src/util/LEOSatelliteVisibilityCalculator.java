package util;

import core.Coord;
import core.SimScenario;

/**
 * A helper class which allows determining the visibility of simulated LEO
 * satellites for their ground stations.
 */
public final class LEOSatelliteVisibilityCalculator {

	/**
	 * Returns a value indicating whether the given satellite is within
	 * radio range (has at least the minimum elevation over the horizon)
	 * of the given ground station.
	 */
	public static boolean isWithinRange(Coord sat_coords, double sat_altitude,
										Coord gs_cooords, double min_elevation_degrees) {
		return LEOSatelliteVisibilityCalculator.elevationOverHorizon(
			sat_coords,
			sat_altitude,
			gs_cooords
		) >= (min_elevation_degrees * Math.PI / 180.0);
	}

	/**
	 * Gets the elevation angle of the given satellite over the horizon of
	 * the ground station (in map coordinates).
	 */
	public static double elevationOverHorizon(Coord sat_coords,
											  double sat_altitude,
											  Coord gs_cooords) {
		return LEOSatelliteVisibilityCalculator.elevationOverHorizon(
			LEOSatelliteVisibilityCalculator.getECEFCoordinates(
				sat_coords,
				sat_altitude
			),
			LEOSatelliteVisibilityCalculator.getECEFCoordinates(gs_cooords, 0)
		);
	}

	/**
	 * Returns a value indicating whether the given satellite is within
	 * radio range for an inter-satellite link with the given other satellite.
	 */
	public static boolean isWithinISLRange(Coord sat1_coords, double sat1_altitude,
										   double sat1_isl_range,
										   Coord sat2_coords, double sat2_altitude,
										   double sat2_isl_range) {
		Vector3D sat1_pos = LEOSatelliteVisibilityCalculator.getECEFCoordinates(
				sat1_coords, sat1_altitude);
		Vector3D sat2_pos = LEOSatelliteVisibilityCalculator.getECEFCoordinates(
				sat2_coords, sat2_altitude);
		double xdiff = sat1_pos.x - sat2_pos.x;
		double ydiff = sat1_pos.y - sat2_pos.y;
		double zdiff = sat1_pos.z - sat2_pos.z;
		return (Math.sqrt(xdiff * xdiff + ydiff * ydiff + zdiff * zdiff) <=
				Math.min(sat1_isl_range, sat2_isl_range));
	}

	/**
	 * Gets the elevation angle of the given satellite over the horizon of
	 * the ground station (in ECEF 3D coordinates)
	 *
	 * Taken from uPCN / RRND (BSD-3-licensed)
	 *
	 * Calculates e := elevation of sat. above horizon of gs
	 *
	 * s := distance between center of earth and sat
	 * g := distance between center of earth and gs
	 * d := distance between sat and gs
	 * alpha := angle between horizon and sat-gs-line (= altitude)
	 *
	 * sin(alpha) = e / d
	 * s^2 = d^2 + g^2 - 2dg*cos(90+alpha)
	 *     = d^2 + g^2 + 2dg*sin(alpha)
	 *     = d^2 + g^2 + 2ge
	 * e = (s^2 - d^2 - g^2) / (2g)
	 */
	public static double elevationOverHorizon(Vector3D satpos,
											  Vector3D gspos) {
		double s = satpos.getNorm();
		double g = gspos.getNorm();
		double d = satpos.getDifference(gspos).getNorm();

		return Math.asin((s * s - d * d - g * g) / (2 * g) / d);
	}

	/**
	 * Gets the Earth-centered-Earth-fixed (ECEF) coords for the given
	 * 2D map coordinates. The transformation is done via LLH coordinates,
	 * using the WGS84 ellipsoid.
	 *
	 * Taken from MIT-licensed library:
	 * https://github.com/chrisveness/geodesy/blob/master/latlon-ellipsoidal.js
	 */
	public static Vector3D getECEFCoordinates(Coord map_coords,
											  double height_over_ellipsoid) {
		// WGS 84 ellipsoid constants
		final double a = 6378137.0;
		final double b = 6356752.314245;
		final double f = 1 / 298.257223563;

		// Calculate latitude and longitude of node from map position
		SimScenario scenario = SimScenario.getInstance();
		double mapSizeX = scenario.getWorldSizeX();
		double mapSizeY = scenario.getWorldSizeY();
		double lat = Math.PI / 2 - (map_coords.getY() / mapSizeY) * Math.PI;
		double lon = (map_coords.getX() / mapSizeX) * 2 * Math.PI - Math.PI;

		// Transform latitude and longitude to ECEF (cartesian) coordinates
		double sin_lat = Math.sin(lat), cos_lat = Math.cos(lat);
		double sin_lon = Math.sin(lon), cos_lon = Math.cos(lon);

		// 1st eccentricity squared = (a^2-b^2)/a^2
		double eSq = 2 * f - f * f;
		// radius of curvature in prime vertical
		double ny = a / Math.sqrt(1 - eSq * sin_lat * sin_lat);

		double x = (ny + height_over_ellipsoid) * cos_lat * cos_lon;
		double y = (ny + height_over_ellipsoid) * cos_lat * sin_lon;
		double z = (ny * (1 - eSq) + height_over_ellipsoid) * sin_lat;

		return new Vector3D(x, y, z);
	}

	// Static class.
	private LEOSatelliteVisibilityCalculator() {}

}
