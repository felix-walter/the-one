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
	public static final String ISL_ACTIVE_S = "islActive";
	public static final String ISL_RANGE_S = "islRange";

	protected double altitude;
	protected boolean islActive;
	protected double islRange;

	public SatelliteInterface(Settings s) {
		super(s);
		this.altitude = s.getDouble(SATELLITE_ALTITUDE_S);
		this.islActive = s.getBoolean(ISL_ACTIVE_S, false);
		this.islRange = s.getDouble(ISL_RANGE_S, 0.0);
	}

	public SatelliteInterface(SatelliteInterface ni) {
		super(ni);
		this.altitude = ni.altitude;
		this.islActive = ni.islActive;
		this.islRange = ni.islRange;
	}

	@Override
	protected boolean isWithinRange(NetworkInterface anotherInterface) {
		if (anotherInterface instanceof GroundStationInterface)
			return LEOSatelliteVisibilityCalculator.isWithinRange(
				this.host.getLocation(),
				this.altitude,
				anotherInterface.getHost().getLocation(),
				((GroundStationInterface)anotherInterface).getMinElevation()
			);
		else if (anotherInterface instanceof SatelliteInterface && this.islActive &&
				 ((SatelliteInterface)anotherInterface).isISLActive())
			return LEOSatelliteVisibilityCalculator.isWithinISLRange(
					this.host.getLocation(),
					this.altitude,
					this.islRange,
					anotherInterface.getHost().getLocation(),
					((SatelliteInterface)anotherInterface).getAltitude(),
					((SatelliteInterface)anotherInterface).getISLRange()
			);
		else
			return false;
	}

	@Override
	public void connect(NetworkInterface anotherInterface) {
		if (anotherInterface instanceof GroundStationInterface ||
			(anotherInterface instanceof SatelliteInterface && this.islActive &&
			 ((SatelliteInterface)anotherInterface).isISLActive())) {
			super.connect(anotherInterface);
		}
	}

	@Override
	public NetworkInterface replicate() {
		return new SatelliteInterface(this);
	}

	public double getAltitude() {
		return altitude;
	}

	public boolean isISLActive() {
		return this.islActive;
	}

	public double getISLRange() {
		return this.islRange;
	}
}
