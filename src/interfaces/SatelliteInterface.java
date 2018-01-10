package interfaces;

import core.NetworkInterface;
import core.Settings;
import util.LEOSatelliteVisibilityCalculator;

/**
 * A simple Network Interface that only connects with GroundStationInterface
 * if the simulated LEO satellite becomes visible for it.
 * Visibility is determined via the satellite altitude and a configured
 * minimum elevation.
 */
public class SatelliteInterface extends SimpleBroadcastInterface {

	private static final String SATELLITE_ALTITUDE_S  = "altitude";

	protected double altitude;

	public SatelliteInterface(Settings s) {
		super(s);
		altitude = s.getDouble(SATELLITE_ALTITUDE_S);
	}

	public SatelliteInterface(SatelliteInterface ni) {
		super(ni);
		altitude = ni.altitude;
	}

	@Override
	protected boolean isWithinRange(NetworkInterface anotherInterface) {
		return anotherInterface instanceof GroundStationInterface &&
				LEOSatelliteVisibilityCalculator.isWithinRange(
					this.host.getLocation(), this.altitude,
					anotherInterface.getHost().getLocation(),
					((GroundStationInterface) anotherInterface).getMinElevation());
	}

	@Override
	public NetworkInterface replicate() {
		return new SatelliteInterface(this);
	}

	public double getAltitude() {
		return altitude;
	}
}
