package com.bbsmart.pda.blackberry.smartview.io;

import java.util.Enumeration;
import java.util.Vector;

import net.rim.blackberry.api.mail.Message;
import net.rim.device.api.util.MultiMap;
import net.rim.device.api.util.Persistable;

public final class SmartNoteManager implements Persistable {
	// Maintains the collection of notes (key is message ID)
	private MultiMap notes;
	private MultiMap priorities;
	private int nextSmartNoteID;

	public SmartNoteManager() {
		notes = new MultiMap();
		priorities = new MultiMap();
		nextSmartNoteID = 0;
	}

	public Vector getSmartNotes(Message m) {
		Vector v = new Vector();
		Enumeration e = notes.elements(new Integer(m.getMessageId()));
		while (e.hasMoreElements()) {
			v.addElement(e.nextElement());
		}

		return v;
	}

	// Deletes all smartnotes associated with a message
	public void deleteSmartNotes(Message m) {
		Integer key = new Integer(m.getMessageId());

		notes.removeKey(key);
		priorities.removeKey(key);
	}

	public void addSmartNote(Message m, SmartNote sn) {
		Integer key = new Integer(m.getMessageId());

		if (!notes.containsKey(key)) {
			// Save the priority of the message for restoration later
			priorities.add(key, new Byte(m.getPriority()));
		}

		sn.id = nextSmartNoteID++;
		notes.add(key, sn);

		m.setPriority(Message.Priority.HIGH);
	}

	public void deleteSmartNote(Message m, SmartNote sn) {
		Integer key = new Integer(m.getMessageId());

		if (notes.containsKey(key)) {
			notes.removeValue(key, sn);
		}

		// If there are no more smartnotes now for this message
		// then set the priority back to what it used to be
		if (!notes.containsKey(key)) {
			Enumeration e = priorities.elements(key);
			byte oldPriority = ((Byte) e.nextElement()).byteValue();

			m.setPriority(oldPriority);

			// Now delete this message from the priorities list
			// No more smartnotes so no need to save it
			priorities.removeKey(key);
		}
	}
}