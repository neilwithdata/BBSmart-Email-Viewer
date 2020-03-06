package com.bbsmart.pda.blackberry.smartview.util;

/**
 * Utility class for handling Emails
 * 
 * @author Neil Sainsbury
 * 
 */
public final class EmailUtils {
	
	public static String getLocalPart(String emailAddy) {
		return emailAddy.substring(0, emailAddy.indexOf('@'));
	}
	
	public static int getEmailStartIndx(String body, int atSymbolIndx) {
		// Walk backwards until we encounter an illegal local-part character
		int startIndx = atSymbolIndx - 1;
		while ((startIndx >= 0) && isValidLocalPartChar(body.charAt(startIndx))) {
			startIndx--;
		}

		startIndx++; // set it back to the true email start index

		// Though this isn't technically correct, Outlook sometimes inserts
		// 's around the email address. If a local part contains a dash as
		// the first char (shouldn't really happen much in real life (TM))
		// then let's just scrap it
		if (body.charAt(startIndx) == '\'') {
			startIndx++;
		}

		if (startIndx == atSymbolIndx) {
			return -1; // local-part cannot have 0 length
		}

		if (body.charAt(startIndx) == '.'
				|| body.charAt(atSymbolIndx - 1) == '.') {
			return -1; // local-part cannot start or end with a '.'
		}
		

		return startIndx;
	}

	public static int getEmailEndIndx(String body, int atSymbolIndx) {
		boolean dotFound = false;

		// Walk forwards until we encounter an illegal domain-part character
		int endIndx = atSymbolIndx + 1;
		while ((endIndx < body.length())
				&& isValidDomainPartChar(body.charAt(endIndx))) {
			if (body.charAt(endIndx) == '.') {
				dotFound = true;
			}
			endIndx++;
		}

		endIndx--; // set it back to the true email end index
		
		// Though this isn't technically correct, Outlook sometimes inserts
		// 's around the email address. If a domain-part contains a dash as
		// the last char (shouldn't really happen much in real life (TM))
		// then let's just scrap it
		if (body.charAt(endIndx) == '\'') {
			endIndx--;
		}

		if (atSymbolIndx == endIndx) {
			return -1; // domain part cannot have length 0
		}

		if (body.charAt(atSymbolIndx + 1) == '.' || body.charAt(endIndx) == '.') {
			return -1; // domain-part cannot start or end with a '.'
		}

		if (!dotFound) {
			return -1; // domain-part must have at least one dot
		}

		return endIndx;
	}

	// According to RFC 2822
	public static final String VALID_LOCAL_CHARS = "!#$%&'*+-/=?^_`{|}~.";
	public static boolean isValidLocalPartChar(char c) {
		if (Character.isDigit(c)) {
			return true;
		}

		// If uppercase or lowercase letter
		if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122)) {
			return true;
		}

		// Some other valid character
		if (VALID_LOCAL_CHARS.indexOf(c) != -1) {
			return true;
		}

		// Didn't match any of the conditions
		return false;
	}

	// According to RFC 2822
	public static boolean isValidDomainPartChar(char c) {
		if (Character.isDigit(c)) {
			return true;
		}
		
		// Some other valid local chars
		if (VALID_LOCAL_CHARS.indexOf(c) != -1) {
			return true;
		}

		// If uppercase or lowercase letter
		if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122)) {
			return true;
		}

		if (c == '-' || c == '.') {
			return true;
		}

		// No match for any other conditions
		return false;
	}
}
