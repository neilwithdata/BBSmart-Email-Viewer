package com.bbsmart.pda.blackberry.smartview.util;

import java.util.Vector;

import com.bbsmart.pda.blackberry.smartview.SmartView;
import com.bbsmart.pda.blackberry.smartview.io.PersistenceManager;
import com.bbsmart.pda.blackberry.smartview.io.Preferences;

import net.rim.blackberry.api.mail.Message;
import net.rim.device.api.util.MultiMap;

public final class EmailFormatter {
	private Message message;

	private int lastCharPos;

	// A list of email addresses detected in the email
	private Vector emailList;

	private PersistenceManager persistManager;

	private Preferences prefs;

	private int numImagesInEmail;

	private String lastFormattedContent;

	public EmailFormatter(Message m) {
		this.message = m;
		lastCharPos = 0;
		emailList = new Vector();
		lastFormattedContent = null;

		persistManager = PersistenceManager.getInstance();
		prefs = persistManager.getPreferences();
	}

	public void formatEmail() {
		int lastPosition = getLastFormatPosition();
		StringBuffer newMessageBody = new StringBuffer(message.getBodyText()
				.substring(lastCharPos, lastPosition));

		// Reset the stored emails
		emailList.removeAllElements();
		numImagesInEmail = 0;

		// Remove any weird HTML characters (if any)
		fromHTMLTag(newMessageBody);

		// Strip out "blocked::" which for some odd reason (known only to RIM)
		// appears at the beginning of some http links
		newMessageBody = new StringBuffer(StringUtils.replace(newMessageBody
				.toString(), "blocked::", ""));

		// Removes those pesky "mailto:" bits from the email
		newMessageBody = removeMailTo(newMessageBody);

		// Put a clean line in place of "Original Message"
		cleanOriginalMessage(newMessageBody);

		// Replace any sequence of underscores with a clean line
		cleanRepeatedUnderScores(newMessageBody);

		// Extract all the emails in the message
		extractEmailLinks(newMessageBody);

		// Clean up stray characters around the emails
		cleanAroundEmails(newMessageBody);

		// Modify the appearance of emails in the message
		neatenEmailLinks(newMessageBody);

		// Perform final parsing of email for links & images
		neatenLinks(newMessageBody, "http://");
		neatenLinks(newMessageBody, "https://");

		// Insert smileys in the email
		newMessageBody = insertSmileys(newMessageBody);

		// Insert newlines
		newMessageBody = shootBlanks(newMessageBody);

		// Clean up any remaining HTML characters
		newMessageBody = fixHTMLChars(newMessageBody);
		
		// Format email text with display preferences
		newMessageBody.insert(0, "<body bgcolor=\"" + "#"
				+ Integer.toHexString(prefs.getFontBgColor()) + "\">"
				+ (prefs.getFontStyle().endsWith("(Bold)") ? "<b>" : "")
				+ "<font face=\"" + prefs.getFontStyle() + "\" size=\""
				+ prefs.getFontSize() + "\" color=\"" + "#"
				+ Integer.toHexString(prefs.getFontColor()) + "\">");

		// Remember that we have already scanned up to here in the email so
		// this 2K chunk is never re-scanned
		lastCharPos = lastPosition;

		lastFormattedContent = newMessageBody.toString();
	}
	
	public int getLastFormatPosition() {
		if (MessageUtils.getTextBodyPart(message).hasMore()) {
			// Walk backwards from the last element until we find a space
			// (or a newline)
			String bodyText = message.getBodyText();
			int endIndx = bodyText.length() - 1;
			while (endIndx >= 0) {
				if (bodyText.charAt(endIndx) == '\n'
						|| bodyText.charAt(endIndx) == ' ') {
					break;
				}
				endIndx--;
			}

			return endIndx + 1;
		} else {
			return message.getBodyText().length();
		}
	}

	public String getFormattedContent() {
		return lastFormattedContent;
	}

	public int getNumImagesInEmail() {
		return numImagesInEmail;
	}

	public StringBuffer insertSmileys(StringBuffer newMessageBody) {
		String content = newMessageBody.toString();

		content = StringUtils.replace(content, ":)",
				"<img src=http://localhost/smile.gif>");

		content = StringUtils.replace(content, ":P",
				"<img src=http://localhost/tongue.gif>");

		content = StringUtils.replace(content, ":(",
				"<img src=http://localhost/sad.gif>");

		content = StringUtils.replace(content, ":D",
				"<img src=http://localhost/laugh.gif>");

		content = StringUtils.replace(content, ";)",
				"<img src=http://localhost/wink.gif>");

		content = StringUtils.replace(content, ":x",
				"<img src=http://localhost/love.gif>");

		content = StringUtils.replace(content, ":@",
				"<img src=http://localhost/mad.gif>");

		content = StringUtils.replace(content, ":O",
				"<img src=http://localhost/ohmy.gif>");

		return new StringBuffer(content);
	}
	
	/**
	 * This method is called at a very critical time so every trick in the book
	 * is pulled out here to make it as absolutely fast as possible...Be warned,
	 * it's not easy on the eyes.
	 * 
	 * @param body
	 * @return
	 */
	private StringBuffer fixHTMLChars(StringBuffer body) {
		final String[] validStarts = { "br>", "hr>", "/a>", "a href", "img src" };
		final int LEN = validStarts.length;

		StringBuffer newMessageBody = new StringBuffer(
				(int) (body.length() * 1.3));
		String original = body.toString();

		String url;
		int lastIndx = 0;
		int matchIndx = 0;
		int foundIndx;
		int linkStartIndx;
		int cntr;
		int closing;
		while ((matchIndx = original.indexOf('<', lastIndx)) != -1) {
			linkStartIndx = matchIndx + 1;

			// Format and store everything up till here
			newMessageBody.append(forHTMLTag(original.substring(lastIndx,
					matchIndx)));

			closing = original.indexOf('>', linkStartIndx) + 1;
			if (closing == 0) {
				// Wasn't actually the start of a link
				newMessageBody.append("&lt;");
				lastIndx = linkStartIndx;
				continue;
			}

			url = original.substring(linkStartIndx, closing);
			foundIndx = -1;
			for (cntr = 0; cntr < LEN; cntr++) {
				if (url.startsWith(validStarts[cntr])) {
					foundIndx = cntr;
					break;
				}
			}

			if (foundIndx != -1) {
				// Is actually a true link - append as is
				newMessageBody.append('<');
				newMessageBody.append(url);
				lastIndx = closing;
			} else {
				// Wasn't actually the start of a link
				newMessageBody.append("&lt;");
				lastIndx = linkStartIndx;
			}
		}

		// Now append the rest of the email
		return newMessageBody.append(forHTMLTag(original.substring(lastIndx)));
	}

	/**
	 * Replaces all occurences of '\n' within the email with the proper HTML
	 * newline tag: <br>
	 * 
	 * @param body
	 * @return
	 */
	private StringBuffer shootBlanks(StringBuffer body) {
		return new StringBuffer(StringUtils.replace(body.toString(), '\n',
				"<br>"));
	}

	/**
	 * Scans through the email for links - upon finding them, removes any < or >
	 * signs around it and in its place, inserts a proper hyperlink.
	 * 
	 * @param body
	 */
	private void neatenLinks(StringBuffer body, String linkType) {
		int endIndx;
		int startIndx = 0;
		char c;
		boolean embeddedLink;
		int lastPos = 0;
		String lastUrl = null;

		MultiMap pI = persistManager.getImageCache().getPredefinedCache();
		
		String longURL, shortURL;

		while ((startIndx = StringUtils.indexOfIgnoreCase(body.toString(),
				linkType, startIndx)) != -1) {
			embeddedLink = false;

			// Check prior
			if (startIndx - 1 >= 0 && body.charAt(startIndx - 1) == '<') {
				embeddedLink = true;
				body.deleteCharAt(startIndx - 1);
				startIndx--;
			}

			// Follow this yellow brick road till we come to a >, newline or a
			// space
			endIndx = startIndx + 1;
			while (endIndx < body.length()) {
				c = body.charAt(endIndx);
				if (c == '>' || c == '\n' || c == ' ' || c == ')') {
					if (embeddedLink && (c == '\n' || c == ' ')) {
						// let it go just this once
						endIndx++;
						continue;
					}
					break;
				}
				endIndx++;
			}

			// Check after
			if (endIndx < body.length() && body.charAt(endIndx) == '>') {
				body.deleteCharAt(endIndx);
			}

			// Out with the old in with the new
			String url = body.toString().substring(startIndx, endIndx);
			body.delete(startIndx, endIndx);

			longURL = null;
			shortURL = null;
			if (lastUrl != null) {
				if (lastUrl.length() > url.length()) {
					longURL = lastUrl;
					shortURL = url;
				} else {
					longURL = url;
					shortURL = lastUrl;
				}
			}

			if (startIndx - lastPos <= 1
					&& StringUtils.indexOfIgnoreCase(longURL, shortURL, 0) != -1) {
				// Copy-cat URL - delete it and insert no replacement
			} else {
				StringBuffer newLink = new StringBuffer(url.length() * 2);
				if (isImageLink(url)) {
					if (pI.containsKey(url)) {
						newLink.append("<img src=\"");
						newLink.append(url);
						newLink.append("\">");
					} else if (prefs.isDisplayImagesInEmail()) {
						numImagesInEmail++;

						newLink.append("<img src=\"");
						newLink.append(url);
						newLink.append("\">");
					}
				} else {
					newLink.append("<a href=\"");
					newLink.append(url);
					newLink.append("\">");
					newLink.append(getLinkURLDisplayName(url));
					newLink.append(" (link)</a>");

					if (linkType.equals("https://")) {
						newLink.append("<img src=http://localhost/lock.jpg>");
					}
				}

				body.insert(startIndx, newLink.toString());
				startIndx += newLink.length();
			}
			lastPos = startIndx;
			lastUrl = url;
		}
	}
	
	private boolean isImageLink(String url) {
		return (url.endsWith(".gif") || url.endsWith(".jpg")
				|| url.endsWith(".png") || url.endsWith(".bmp"));
	}

	private String getLinkURLDisplayName(String url) {
		if (prefs.isShowFullLinks()) {
			return url;
		}
		
		int firstDot = url.indexOf('.');
		int secondDot = url.indexOf('.', firstDot + 1);

		if (firstDot == -1 || secondDot == -1) {
			return "link";
		} else {
			return url.substring(firstDot + 1, secondDot);
		}
	}

	/**
	 * Scans through the email looking for any of the email addresses which were
	 * detected earlier. Once found, replace the email address as it appears in
	 * the message with a neater and condensed version.
	 * 
	 * @param body
	 */
	private void neatenEmailLinks(StringBuffer body) {
		StringBuffer neatLink;
		String email;
		int lastPos;
		int index;

		for (int i = 0; i < emailList.size(); i++) {
			email = (String) emailList.elementAt(i);
			lastPos = 0;

			neatLink = new StringBuffer();
			neatLink.append("<a href=\"mailto:");
			neatLink.append(email);
			neatLink.append("\">");
			neatLink.append(EmailUtils.getLocalPart(email));
			neatLink.append(" (email)");
			neatLink.append("</a>");

			while ((index = body.toString().indexOf(email, lastPos)) != -1) {
				// Out with the old and in with the new
				body.delete(index, index + email.length());
				body.insert(index, neatLink.toString());
				lastPos = index + neatLink.length();
			}
		}
	}

	private StringBuffer removeMailTo(StringBuffer body) {
		return new StringBuffer(StringUtils.removeIgnoreCase(body.toString(),
				"mailto:"));
	}

	private void extractEmailLinks(StringBuffer body) {
		int matchIndx = 0;
		int startIndx;
		int endIndx;

		String bodyText = body.toString();
		while ((matchIndx = bodyText.indexOf('@', matchIndx)) != -1) {

			endIndx = EmailUtils.getEmailEndIndx(bodyText, matchIndx);
			if (endIndx == -1) { // Not a valid email address
				matchIndx += 1;
				continue;
			}

			startIndx = EmailUtils.getEmailStartIndx(bodyText, matchIndx);
			if (startIndx == -1) { // Not a valid email address
				matchIndx += 1;
				continue;
			}

			String emailAddress = bodyText.substring(startIndx, endIndx + 1);
			// Add the email to the collection
			if (!emailList.contains(emailAddress)) {
				emailList.addElement(emailAddress);
			}

			matchIndx = endIndx + 1;
		}
	}

	private void cleanAroundEmails(StringBuffer body) {
		String email;
		int lastPos = 0;
		int index;
		char c;

		for (int i = 0; i < emailList.size(); i++) {
			email = (String) emailList.elementAt(i);
			lastPos = 0;

			while ((index = body.toString().indexOf(email, lastPos)) != -1) {
				// Check prior
				if (index - 1 >= 0) { // boundary check
					c = body.charAt(index - 1);
					if (c == '<' || c == '[' || c == '\'' || c == '"'
							|| c == '(') {
						body.deleteCharAt(index - 1);
						index--;
					}
				}

				index += email.length();

				// Check after
				if (index < body.length()) { // boundary check
					c = body.charAt(index);
					if (c == '>' || c == ']' || c == '\'' || c == '"'
							|| c == ')') {
						body.deleteCharAt(index);
					}
				}

				// Removes duplicate inserted by the server when it detects
				// an HTML email address
				int endDuplicate = index + 4 + email.length();
				if (endDuplicate <= body.length()) {
					if (body.charAt(index) == ':') {
						String remainder = body.toString().substring(index,
								endDuplicate);
						if (remainder.indexOf(email, 0) != -1) {
							// Remainder contains duplicate email - safe to
							// delete
							body.delete(index, endDuplicate);
						}
					}
				}

				// Removes duplicate email address inserted by some other client
				int priorStart = index - (email.length() * 2 + 1);
				int priorEnd = index - email.length();
				if (priorStart >= 0) {
					if (body.toString().substring(priorStart, priorEnd)
							.indexOf(email) != -1) {
						// Duplicate found - delete it
						body.delete(priorStart, priorEnd);
						index -= email.length() + 1;
					}
				}
				lastPos = index;
			}
		}
	}

	/**
	 * Scans through the provided body looking for the text "---Original
	 * Message---" (number of dashes is arbitrary) and replaces this with a nice
	 * horizontal line
	 * 
	 * @param body
	 * @return
	 */
	private void cleanOriginalMessage(StringBuffer body) {
		final String MSG = "Original Message";
		final String MSG_FOLLOW = "Original Message Follows";
		final int MSG_LEN = MSG.length();
		final int MSG_FOLLOW_LEN = MSG_FOLLOW.length();
		int matchIndx = 0;
		int startIndx, endIndx;
		char c;

		while ((matchIndx = body.toString().indexOf(MSG, matchIndx)) != -1) {

			startIndx = matchIndx - 1;
			endIndx = matchIndx + MSG_LEN;

			if (body.toString().indexOf(MSG_FOLLOW, matchIndx) == matchIndx) {
				endIndx = matchIndx + MSG_FOLLOW_LEN;
			}

			int numDashesBefore = 0;
			int numDashesAfter = 0;
			// first find the beginning - step backwards until encountering
			// something that is not a dash and is a displayable character
			while (true) {
				if (startIndx < 0) {
					startIndx = 0;
					break; // hit beginning - break out
				}

				c = body.charAt(startIndx);

				if (c == '-') {
					numDashesBefore++;
				} else if (c >= 33) {
					startIndx++;
					break; // found a regular character - break out
				}

				startIndx--;
			}

			// Have now found the beginning - check to make sure there were
			// at least two -'s before otherwise this could just be regular
			// text
			if (numDashesBefore < 2) {
				matchIndx++;
				continue;
			}

			// next find the end - step forwards until encountering
			// something that is not a dash and is a displayable character
			while (true) {
				if (endIndx >= body.length()) {
					endIndx = body.length() - 1;
					break; // hit end - break out
				}

				c = body.charAt(endIndx);

				if (c == '-') {
					numDashesAfter++;
				} else if (c >= 33) {
					endIndx--;
					break; // found a regular character - break out
				}

				endIndx++;
			}

			// Have now found the end - check to make sure there were
			// at least two -'s before otherwise this could just be regular
			// text
			if (numDashesAfter < 2) {
				matchIndx++;
				continue;
			}

			// If we got to here, we are quite confident that we are looking
			// at a legitimate "Original Message" text portion and the indices
			// are good.
			body.delete(startIndx, endIndx + 1);
			body.insert(startIndx, " http://localhost/original.png ");
		}
	}

	/**
	 * Scans through the email and replaces any repeated sequence of underscore
	 * characters with a line.
	 * 
	 * @param body
	 */
	private void cleanRepeatedUnderScores(StringBuffer body) {
		String underscores = "___";
		String dashes = "---";

		int matchIndx = 0;
		int endIndx;

		while ((matchIndx = body.toString().indexOf(underscores, matchIndx)) != -1) {
			endIndx = matchIndx + underscores.length();
			while (endIndx < body.length()) {
				if (body.charAt(endIndx) != '_') {
					break;
				}

				endIndx++;
			}

			body.delete(matchIndx, endIndx);
			body.insert(matchIndx, " http://localhost/line.jpg ");
		}

		matchIndx = 0;
		while ((matchIndx = body.toString().indexOf(dashes, matchIndx)) != -1) {
			endIndx = matchIndx + dashes.length();
			while (endIndx < body.length()) {
				if (body.charAt(endIndx) != '-') {
					break;
				}

				endIndx++;
			}

			body.delete(matchIndx, endIndx);
			body.insert(matchIndx, " http://localhost/line.jpg ");
		}
	}

	/**
	 * Scans through the provided body and replaces all detected HTML looking
	 * format indicators (eg. &lt;, etc.) with their regular text equivalents.
	 * 
	 * @param body
	 */
	private void fromHTMLTag(StringBuffer body) {
		final String[] tags = { "&lt;", "&gt;", "&quot;", "&#039;", "&#092;",
				"&amp;" };
		final char[] replacements = { '<', '>', '"', '\'', '\\', '&' };

		int lastPos = 0;
		int index;
		String tag;

		for (int i = tags.length - 1; i >= 0; i--) {
			tag = tags[i];
			lastPos = 0;

			while ((index = body.toString().indexOf(tag, lastPos)) != -1) {
				body.delete(index, index + tag.length());
				body.insert(index, replacements[i]);
				lastPos = index + 1;
			}
		}
	}

	private String forHTMLTag(String aTagFragment) {
		final int LEN = aTagFragment.length();
		StringBuffer result = new StringBuffer((int) (LEN * 1.3));

		char character;
		for (int i = 0; i < LEN; i++) {
			character = aTagFragment.charAt(i);

			switch (character) {
			case '<':
				result.append("&lt;");
				break;
			case '>':
				result.append("&gt;");
				break;
			case '\"':
				result.append("&quot;");
				break;
			case '\'':
				result.append("&#039;");
				break;
			case '\\':
				result.append("&#092;");
				break;
			case '&':
				result.append("&amp;");
				break;
			case '\n':
				result.append("<br>");
				break;
			case '€':
				result.append("&euro;");
				break;
			case '¡':
				result.append("&iexcl;");
				break;
			case '¿':
				result.append("&iquest;");
				break;
			case '«':
				result.append("&laquo;");
				break;
			case '»':
				result.append("&raquo;");
				break;
			case '¢':
				result.append("&cent;");
				break;
			case '©':
				result.append("&copy;");
				break;
			case '÷':
				result.append("&divide;");
				break;
			case 'µ':
				result.append("&#181;");
				break;
			case '·':
				result.append("&middot;");
				break;
			case '¶':
				result.append("&para;");
				break;
			case '±':
				result.append("&plusmn;");
				break;
			case '£':
				result.append("&pound;");
				break;
			case '®':
				result.append("&reg;");
				break;
			case '§':
				result.append("&sect;");
				break;
			case '¥':
				result.append("&yen;");
				break;
			case 'á':
				result.append("&aacute;");
				break;
			case 'Á':
				result.append("&Aacute;");
				break;
			case 'à':
				result.append("&agrave;");
				break;
			case 'À':
				result.append("&Agrave;");
				break;
			case 'â':
				result.append("&acirc;");
				break;
			case 'Â':
				result.append("&Acirc;");
				break;
			case 'å':
				result.append("&aring;");
				break;
			case 'Å':
				result.append("&Aring;");
				break;
			case 'ã':
				result.append("&atilde;");
				break;
			case 'Ã':
				result.append("&Atilde;");
				break;
			case 'ä':
				result.append("&auml;");
				break;
			case 'Ä':
				result.append("&Auml;");
				break;
			case 'æ':
				result.append("&aelig;");
				break;
			case 'Æ':
				result.append("&AElig;");
				break;
			case 'ç':
				result.append("&ccedil;");
				break;
			case 'Ç':
				result.append("&Ccedil;");
				break;
			case 'é':
				result.append("&eacute;");
				break;
			case 'É':
				result.append("&Eacute;");
				break;
			case 'è':
				result.append("&egrave;");
				break;
			case 'È':
				result.append("&Egrave;");
				break;
			case 'ê':
				result.append("&ecirc;");
				break;
			case 'Ê':
				result.append("&Ecirc;");
				break;
			case 'ë':
				result.append("&euml;");
				break;
			case 'Ë':
				result.append("&Euml;");
				break;
			case 'í':
				result.append("&iacute;");
				break;
			case 'Í':
				result.append("&Iacute;");
				break;
			case 'ì':
				result.append("&igrave;");
				break;
			case 'Ì':
				result.append("&Igrave;");
				break;
			case 'î':
				result.append("&icirc;");
				break;
			case 'Î':
				result.append("&Icirc;");
				break;
			case 'ï':
				result.append("&iuml;");
				break;
			case 'Ï':
				result.append("&Iuml;");
				break;
			case 'ñ':
				result.append("&ntilde;");
				break;
			case 'Ñ':
				result.append("&Ntilde;");
				break;
			case 'ó':
				result.append("&oacute;");
				break;
			case 'Ó':
				result.append("&Oacute;");
				break;
			case 'ò':
				result.append("&ograve;");
				break;
			case 'Ò':
				result.append("&Ograve;");
				break;
			case 'ô':
				result.append("&ocirc;");
				break;
			case 'Ô':
				result.append("&Ocirc;");
				break;
			case 'ø':
				result.append("&oslash;");
				break;
			case 'Ø':
				result.append("&Oslash;");
				break;
			case 'õ':
				result.append("&otilde;");
				break;
			case 'Õ':
				result.append("&Otilde;");
				break;
			case 'ö':
				result.append("&ouml;");
				break;
			case 'Ö':
				result.append("&Ouml;");
				break;
			case 'ß':
				result.append("&szlig;");
				break;
			case 'ú':
				result.append("&uacute;");
				break;
			case 'Ú':
				result.append("&Uacute;");
				break;
			case 'ù':
				result.append("&ugrave;");
				break;
			case 'Ù':
				result.append("&Ugrave;");
				break;
			case 'û':
				result.append("&ucirc;");
				break;
			case 'Û':
				result.append("&Ucirc;");
				break;
			case 'ü':
				result.append("&uuml;");
				break;
			case 'Ü':
				result.append("&Uuml;");
				break;
			case 'ÿ':
				result.append("&yuml;");
				break;
			case '´':
				result.append("&#180;");
				break;
			case '’':
				result.append("&#39;");
				break;
			case '`':
				result.append("&#96;");
				break;
			case '“':
				result.append("&quot;");
				break;
			case '”':
				result.append("&quot;");
				break;
			case '‘':
				result.append("&#96;");
				break;
			case '–':
				result.append("&ndash;");
				break;
			default:
				result.append(character);
				break;
			}
		}
		return result.toString();
	}
}
