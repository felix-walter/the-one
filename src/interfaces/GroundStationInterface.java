package interfaces;

import core.NetworkInterface;
import core.Settings;
import util.LEOSatelliteVisibilityCalculator;

/**
 * A simple Network Interface that only connects with SatelliteInterface
 * if the simulated LEO satellite becomes visible for it.
 * Visibility is determined via the satellite altitude and a configured
 * minimum elevation.
 */
public class GroundStationInterface extends SimpleBroadcastInterface {

	private static final String GS_MIN_ELEV_S = "minElevation";

	private double min_elevation_degrees;

	public GroundStationInterface(Settings s) {
		super(s);
		min_elevation_degrees = s.getDouble(GS_MIN_ELEV_S, 0);
	}

	public GroundStationInterface(GroundStationInterface ni) {
		super(ni);
		min_elevation_degrees = ni.min_elevation_degrees;
	}

	@Override
	protected boolean isWithinRange(NetworkInterface anotherInterface) {
		if (anotherInterface instanceof SatelliteInterface) {
			return LEOSatelliteVisibilityCalculator.isWithinRange(
					anotherInterface.getHost().getLocation(),
					((SatelliteInterface) anotherInterface).getAltitude(),
					this.host.getLocation(),
					this.min_elevation_degrees);
		}
		return false;
	}

	@Override
	public void connect(NetworkInterface anotherInterface) {
		if (anotherInterface instanceof SatelliteInterface)
			super.connect(anotherInterface);
	}

	@Override
	public NetworkInterface replicate() {
		return new GroundStationInterface(this);
	}

	public double getMinElevation() {
		return min_elevation_degrees;
	}
}
