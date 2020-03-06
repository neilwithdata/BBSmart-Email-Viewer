package com.bbsmart.pda.blackberry.smartview.io;

import net.rim.device.api.util.Persistable;

public class EmailColorPreference implements Persistable {
	public int color;
	public String emailAddress;
	public String emailAccountName;

	public EmailColorPreference(String emailAddress, String emailAccountName,
			int color) {
		this.emailAccountName = emailAccountName;
		this.emailAddress = emailAddress;
		this.color = color;
	}

	public String toString() {
		return emailAddress;
	}
}