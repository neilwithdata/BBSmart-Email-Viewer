package com.bbsmart.pda.blackberry.smartview.util;

import java.util.Date;
import java.util.Vector;

import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.BodyPart;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.Multipart;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.device.api.ui.Color;
import net.rim.device.api.util.StringUtilities;

/**
 * Utility class for operating on messages. Essentially, a clean wrapper that
 * sits around the RIM Message class but provides specific functionality for the
 * EmailViewing application
 * 
 * @author Neil
 */
public final class MessageUtils {
	private Message message;

	private Address[] toRecips, ccRecips, bccRecips;
	
	private boolean preferAddress;

	public MessageUtils(Message message) {
		this.message = message;

		try {
			toRecips = cleanRecips(message
					.getRecipients(Message.RecipientType.TO));
			ccRecips = cleanRecips(message
					.getRecipients(Message.RecipientType.CC));
			bccRecips = cleanRecips(message
					.getRecipients(Message.RecipientType.BCC));
		} catch (MessagingException me) {
			UiUtilities.alertDialog("Error reading recipients: "
					+ me.getMessage());
		}
	}
	
	public void setPreferAddress(boolean preferAddress) {
		this.preferAddress = preferAddress;
	}

	public String getFrom() {
		try {
			return getDisplayableEmail(message.getFrom());
		} catch (MessagingException me) {
			return UiUtilities.EMPTY_STRING;
		}
	}

	public String getShortRecip() {
		int recipSum = toRecips.length + ccRecips.length + bccRecips.length;

		Address[] recips = toRecips;
		if (toRecips.length == 0) {
			recips = ccRecips;
			if (ccRecips.length == 0) {
				recips = bccRecips;
				if (bccRecips.length == 0) {
					recips = null;
				}
			}
		}

		if (recips != null) {
			String recip = getDisplayableEmail(recips[0]);
			if (recipSum > 1) {
				recip = recip + ", ... (" + recipSum + ")";
			}
			return recip;
		} else {
			return UiUtilities.EMPTY_STRING;
		}
	}

	public String getShortRecipType() {
		String type = "To: ";
		if (toRecips.length == 0) {
			type = "Cc: ";
			if (ccRecips.length == 0) {
				type = "Bcc: ";
				if (bccRecips.length == 0) {
					type = "To: ";
				}
			}
		}

		return type;
	}
	
	public int getPriorityColor() {
		switch (message.getPriority()) {
		case Message.Priority.HIGH:
			return Color.RED;
		case Message.Priority.NORMAL:
			return Color.GREEN;
		case Message.Priority.LOW:
			return Color.LIGHTBLUE;
		default:
			return Color.GREEN;
		}
	}
	
	public String getPriority() {
		switch (message.getPriority()) {
		case Message.Priority.LOW:
			return "Low";
		case Message.Priority.NORMAL:
			return "Normal";
		case Message.Priority.HIGH:
			return "High";
		default:
			return "Normal";
		}
	}

	public String getMessageStatus() {
		String friendlyStatus;

		switch (message.getStatus()) {
		case Message.Status.RX_ERROR:
			friendlyStatus = "Error Receiving";
			break;
		case Message.Status.RX_RECEIVED:
			friendlyStatus = "Received";
			break;
		case Message.Status.RX_RECEIVING:
			friendlyStatus = "Receiving";
			break;
		case Message.Status.TX_COMPOSING:
			friendlyStatus = "Composing";
			break;
		case Message.Status.TX_COMPRESSING:
			friendlyStatus = "Compressing";
			break;
		case Message.Status.TX_DELIVERED:
			friendlyStatus = "Delivered";
			break;
		case Message.Status.TX_ENCRYPTING:
			friendlyStatus = "Encrypting";
			break;
		case Message.Status.TX_ERROR:
		case Message.Status.TX_GENERAL_FAILURE:
			friendlyStatus = "Error Sending";
			break;
		case Message.Status.TX_MAILBOXED:
			friendlyStatus = "Mailboxed";
			break;
		case Message.Status.TX_PENDING:
			friendlyStatus = "Pending Send";
			break;
		case Message.Status.TX_READ:
			friendlyStatus = "Read";
			break;
		case Message.Status.TX_RETRIEVING_KEY:
			friendlyStatus = "Retrieving Key";
			break;
		case Message.Status.TX_SENDING:
			friendlyStatus = "Sending";
			break;
		case Message.Status.TX_SENT:
			friendlyStatus = "Sent";
			break;
		default:
			friendlyStatus = UiUtilities.EMPTY_STRING;
			break;
		}

		return friendlyStatus;
	}

	public String getReplyTo() {
		try {
			Address[] replyTo = message.getReplyTo();

			if (replyTo != null && replyTo.length > 0) {
				return getDisplayableEmail(replyTo[0]);
			} else {
				return UiUtilities.EMPTY_STRING;
			}
		} catch (MessagingException me) {
			return UiUtilities.EMPTY_STRING;
		}
	}
	
	public String makeFullToString() {
		return joinAddresses(toRecips);
	}

	public String makeFullCcString() {
		return joinAddresses(ccRecips);
	}

	public String makeFullBccString() {
		return joinAddresses(bccRecips);
	}

	private String joinAddresses(Address[] addresses) {
		StringBuffer addressStr = new StringBuffer();

		int numAddresses = addresses.length;
		for (int i = 0; i < numAddresses; i++) {
			addressStr.append(getDisplayableEmail(addresses[i]));

			if (i != numAddresses - 1) {
				addressStr.append(", ");
			}
		}

		return addressStr.toString();
	}

	public String getSentDate() {
		if (message.getStatus() == Message.Status.TX_COMPOSING) {
			return UiUtilities.EMPTY_STRING;
		}

		Date sentDate = message.getSentDate();

		if (sentDate == null) {
			return "Unknown";
		}

		String[] parts = StringUtilities.stringToWords(sentDate.toString());

		StringBuffer newDate = new StringBuffer();

		newDate.append(parts[0]); // dow
		newDate.append(' ');
		newDate.append(parts[1]); // mon
		newDate.append(' ');

		// Remove any leading '0' from dd
		String dd = parts[2];
		if (dd.charAt(0) == '0') {
			dd = dd.substring(1);
		}

		newDate.append(dd); // dd
		newDate.append(", "); // ,

		// Format the time display nicely and in 12hr time
		String hourStr = parts[3];
		int hour = Integer.parseInt(hourStr);
		String meridian;
		if (hour == 0) {
			hour = 12;
			meridian = "AM";
		} else if (hour < 12) {
			meridian = "AM";
		} else if (hour == 12) {
			meridian = "PM";
		} else {
			meridian = "PM";
			hour = hour % 12;
		}

		newDate.append(hour);
		newDate.append(':');
		newDate.append(parts[4]);
		newDate.append(" ");
		newDate.append(meridian);

		return newDate.toString();
	}

	private String getDisplayableEmail(Address addr) {
		if (addr == null) {
			return UiUtilities.EMPTY_STRING;
		}

		if (preferAddress) {
			return addr.getAddr();
		} else {
			// Return the address only if the name does not exist
			String name = addr.getName();
			if (name == null) {
				name = addr.getAddr();
				if (name == null) {
					name = UiUtilities.EMPTY_STRING;
				}
			}
			return name;
		}
	}

	public boolean hasAttachments() {
		return (message.getContent() instanceof Multipart);
	}

	public Vector getAttachments() {
		Multipart parts = (Multipart) message.getContent();
		int numParts = parts.getCount();

		Vector attachments = new Vector(numParts);
		BodyPart part;
		for (int i = 0; i < numParts; i++) {
			part = parts.getBodyPart(i);

			if (!(part instanceof TextBodyPart)) {
				attachments.addElement(part);
			}
		}

		return attachments;
	}
	
	private Address[] cleanRecips(Address[] addresses) {
		if (addresses == null) {
			return new Address[0];
		}

		Vector cleaned = new Vector(addresses.length);

		for (int i = 0; i < addresses.length; i++) {
			if (addresses[i] != null) {
				if (!(addresses[i].getAddr().trim().length() == 0)) {
					cleaned.addElement(addresses[i]);
				}
			}
		}

		// Convert the vector of cleaned addresses to an array and return
		Address[] cleanedAddresses = new Address[cleaned.size()];
		for (int j = 0; j < cleaned.size(); j++) {
			cleanedAddresses[j] = (Address) cleaned.elementAt(j);
		}

		return cleanedAddresses;
	}
	
	/**
	 * Returns the TextBodyPart of the email or null if it has none
	 */
	public TextBodyPart getTextBodyPart() {
		Object content = message.getContent();

		if (content instanceof BodyPart) {
			return (TextBodyPart) content;
		} else {
			// Must be a MultiPart - search through for the TextBodyPart
			Multipart mp = (Multipart) content;
			for (int i = 0; i < mp.getCount(); i++) {
				if (mp.getBodyPart(i) instanceof TextBodyPart) {
					return (TextBodyPart) mp.getBodyPart(i);
				}
			}
		}

		return null;
	}
	
	/**
	 * Returns the TextBodyPart of the email or null if it has none
	 */
	public static TextBodyPart getTextBodyPart(Message m) {
		Object content = m.getContent();

		if (content instanceof BodyPart) {
			return (TextBodyPart) content;
		} else {
			// Must be a MultiPart - search through for the TextBodyPart
			Multipart mp = (Multipart) content;
			for (int i = 0; i < mp.getCount(); i++) {
				if (mp.getBodyPart(i) instanceof TextBodyPart) {
					return (TextBodyPart) mp.getBodyPart(i);
				}
			}
		}

		return null;
	}
}