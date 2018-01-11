package interfaces;

import core.NetworkInterface;
import core.Settings;

/**
 * A simple Network Interface that provides a constant connection to all others of its kind.
 */
public class InternetInterface extends SimpleBroadcastInterface {

	/**
	 * Reads the interface settings from the Settings file
	 */
	public InternetInterface(Settings s)	{
		super(s);
	}

	/**
	 * Copy constructor
	 * @param ni the copied network interface object
	 */
	public InternetInterface(InternetInterface ni) {
		super(ni);
	}

	@Override
	protected boolean isWithinRange(NetworkInterface anotherInterface) {
		return true;
	}

	@Override
	public void connect(NetworkInterface anotherInterface) {
		if (anotherInterface instanceof InternetInterface)
			super.connect(anotherInterface);
	}

	@Override
	public NetworkInterface replicate() {
		return new InternetInterface(this);
	}

	@Override
	protected double getDefaultTransmitRange() {
		return 1.0;
	}
}
