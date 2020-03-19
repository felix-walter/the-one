package interfaces;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.NetworkInterface;
import core.Settings;
import core.SimClock;
import core.SimError;

import util.Tuple;

/**
 * A Network Interface allowing to provide contacts as a CSV file.
 */
public class ForcedConnectionInterface extends SimpleBroadcastInterface {

	private static final String CONTACTS_CSV_S  = "contactsCsv";

	protected Map<String, Map<String, List<Tuple<Double, Double>>>> contactMap;

	public ForcedConnectionInterface(Settings s) {
		super(s);

		this.contactMap = new HashMap<String, Map<String, List<Tuple<Double, Double>>>>();

		BufferedReader br = null;
		String line = "";

		try {
			br = new BufferedReader(new FileReader(s.getSetting(CONTACTS_CSV_S)));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] contactInfo = line.split(",");

				String txNode = contactInfo[0];
				String rxNode = contactInfo[1];
				Double start = Double.valueOf(contactInfo[2]);
				Double end = Double.valueOf(contactInfo[3]);

				if (!this.contactMap.containsKey(txNode))
					this.contactMap.put(txNode, new HashMap<String, List<Tuple<Double, Double>>>());
				if (!this.contactMap.get(txNode).containsKey(rxNode))
					this.contactMap.get(txNode).put(rxNode, new ArrayList<Tuple<Double, Double>>());

				Tuple<Double, Double> contactTuple = new Tuple<Double, Double>(start, end);
				this.contactMap.get(txNode).get(rxNode).add(contactTuple);
			}
		} catch (FileNotFoundException e) {
			throw new SimError("Could not find contacts CSV: " + e);
		} catch (IOException e) {
			throw new SimError("Could not read contacts CSV: " + e);
		}
	}

	public ForcedConnectionInterface(ForcedConnectionInterface ni) {
		super(ni);
		this.contactMap = ni.contactMap;
	}

	@Override
	protected boolean isWithinRange(NetworkInterface anotherInterface) {
		if (!(anotherInterface instanceof ForcedConnectionInterface))
			return false;

		String txNode = getHost().toString();
		String rxNode = anotherInterface.getHost().toString();

		if (!this.contactMap.containsKey(txNode))
			return false;
		if (!this.contactMap.get(txNode).containsKey(rxNode))
			return false;

		Double curTime = SimClock.getTime();

		for (Tuple<Double, Double> ci : this.contactMap.get(txNode).get(rxNode)) {
			if (ci.getKey() <= curTime && ci.getValue() >= curTime)
				return true;
		}

		return false;
	}

	@Override
	public void connect(NetworkInterface anotherInterface) {
		if (anotherInterface instanceof ForcedConnectionInterface)
			super.connect(anotherInterface);
	}

	@Override
	public NetworkInterface replicate() {
		return new ForcedConnectionInterface(this);
	}

	@Override
	protected double getDefaultTransmitRange() {
		return 1.0;
	}
}
