package com.bbsmart.pda.blackberry.smartview.util;

import net.rim.blackberry.api.mail.Message;

public class HTMLRetriever {
	private Message message;

	public HTMLRetriever(Message message) {
		this.message = message;
	}

	public static boolean isHTMLEmail() {
		return true;
	}
}
