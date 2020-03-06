package com.bbsmart.pda.blackberry.smartview.io;

import net.rim.device.api.util.Persistable;

/**
 * Represents a single SmartNote
 * 
 * @author Neil
 */
public final class SmartNote implements Persistable {
	public static final String LOW_P = "Low";
	public static final String NORM_P = "Normal";
	public static final String HIGH_P = "High";

	public int id;
	public String text;
	public String priority;

	public boolean equals(Object obj) {
		if (obj instanceof SmartNote) {
			SmartNote sn = (SmartNote) obj;
			return sn.id == this.id;
		}

		return false;
	}
}