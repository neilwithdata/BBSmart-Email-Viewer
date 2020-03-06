package com.bbsmart.pda.blackberry.smartview.util;

public class EmailTemplate {
	public String title;
	public String text;

	public EmailTemplate(String title, String text) {
		this.title = title;
		this.text = text;
	}
	
	public String toString() {
		return title;
	}
}
