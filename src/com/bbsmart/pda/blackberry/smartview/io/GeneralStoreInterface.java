package com.bbsmart.pda.blackberry.smartview.io;

import java.util.Vector;

import com.bbsmart.pda.blackberry.smartview.util.EmailTemplate;

public class GeneralStoreInterface {
	public static String getStoreVersion() {
		return (String) getProperty("storeVersion");
	}

	public static String getHeaderFontSize() {
		return (String) getProperty("headerFontSize");
	}

	public static String getBISConnUID() {
		return (String) getProperty("bisConnUID");
	}

	public static void setBISConnUID(String bisConnUID) {
		setProperty("bisConnUID", bisConnUID);
	}

	public static void setHeaderFontSize(String value) {
		setProperty("headerFontSize", value);
	}

	public static boolean isHeaderBoldFont() {
		return ((Boolean) getProperty("headerBoldFont")).booleanValue();
	}

	public static void setHeaderBoldFont(boolean value) {
		setProperty("headerBoldFont", new Boolean(value));
	}

	public static EmailTemplate[] getEmailTemplates() {
		Vector titles = (Vector) getProperty("templateTitles");
		Vector texts = (Vector) getProperty("templateValues");

		EmailTemplate[] templates = new EmailTemplate[titles.size()];
		for (int i = 0; i < titles.size(); i++) {
			templates[i] = new EmailTemplate((String) titles.elementAt(i),
					(String) texts.elementAt(i));
		}

		return templates;
	}

	public static boolean createEmailTemplate(String title, String text) {
		Vector titles = (Vector) getProperty("templateTitles");
		Vector texts = (Vector) getProperty("templateValues");

		// Cannot create two templates with the same name
		if (titles.contains(title)) {
			return false;
		} else {
			titles.addElement(title);
			texts.addElement(text);
			return true;
		}
	}

	public static boolean updateEmailTemplate(String oldTitle, String newTitle,
			String text) {
		Vector titles = (Vector) getProperty("templateTitles");
		Vector texts = (Vector) getProperty("templateValues");

		int index = titles.indexOf(oldTitle);
		if (index != -1) {
			if (oldTitle.equals(newTitle)) {
				// No title change so just update the text
				texts.removeElementAt(index);
				texts.insertElementAt(text, index);
				return true;
			} else {
				// Title has changed...make sure the new title isn't already in use
				if (titles.contains(newTitle)) {
					return false;
				} else {
					// New title is different from old title and new title
					// is not in use. Delete old, insert new
					titles.removeElementAt(index);
					texts.removeElementAt(index);

					titles.insertElementAt(newTitle, index);
					texts.insertElementAt(text, index);
					return true;
				}
			}
		}

		return false;
	}

	public static void moveTemplateUp(String title) {
		Vector titles = (Vector) getProperty("templateTitles");
		Vector texts = (Vector) getProperty("templateValues");

		// Move the title up
		int index = titles.indexOf(title);
		String toMoveDown = (String) titles.elementAt(index - 1);
		titles.removeElementAt(index - 1);
		titles.insertElementAt(toMoveDown, index);

		// Move the template up
		toMoveDown = (String) texts.elementAt(index - 1);
		texts.removeElementAt(index - 1);
		texts.insertElementAt(toMoveDown, index);
	}

	public static void moveTemplateDown(String title) {
		Vector titles = (Vector) getProperty("templateTitles");
		Vector texts = (Vector) getProperty("templateValues");

		// Move the title up
		int index = titles.indexOf(title);
		String toMoveUp = (String) titles.elementAt(index);
		titles.removeElementAt(index);
		titles.insertElementAt(toMoveUp, index + 1);

		// Move the template up
		toMoveUp = (String) texts.elementAt(index);
		texts.removeElementAt(index);
		texts.insertElementAt(toMoveUp, index + 1);
	}

	public static void deleteEmailTemplate(String title) {
		Vector titles = (Vector) getProperty("templateTitles");
		Vector texts = (Vector) getProperty("templateValues");

		int index = titles.indexOf(title);
		if (index != -1) {
			titles.removeElementAt(index);
			texts.removeElementAt(index);
		}
	}

	private static void setProperty(String key, Object value) {
		PersistenceManager.getInstance().getDataStore().put(key, value);
	}

	private static Object getProperty(String key) {
		return PersistenceManager.getInstance().getDataStore().get(key);
	}
}