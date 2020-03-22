/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;

/**
 * Reports relaying of messages
 * report:
 *  message_id creation_time deliver_time (duplicate)
 */
public class MessageRelayReport extends Report implements MessageListener {

	public MessageRelayReport() {
		init();
	}

	public void newMessage(Message m) {}

	public void messageTransferred(Message m, DTNHost from, DTNHost to,
			boolean firstDelivery) {
		if (firstDelivery) {
			write(m.getId() + " " + from + "->" + to
					+ " " + format(getSimTime()) + " delivered");
		} else {
			if (to.getAddress() == m.getTo().getAddress()) {
				write(m.getId() + " " + from + "->" + to
						+ " " + format(getSimTime()) + " duplicate");
			} else {
				write(m.getId() + " " + from + "->" + to
						+ " " + format(getSimTime()));
			}
		}
	}

	// nothing to implement for the rest
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {}
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}
}
