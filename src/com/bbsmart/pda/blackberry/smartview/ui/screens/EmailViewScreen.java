package com.bbsmart.pda.blackberry.smartview.ui.screens;

import java.util.Vector;

import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.ToDo;
import javax.microedition.pim.ToDoList;

import com.bbsmart.pda.blackberry.smartview.BuildVersion;
import com.bbsmart.pda.blackberry.smartview.SmartView;
import com.bbsmart.pda.blackberry.smartview.io.EmailColorPreference;
import com.bbsmart.pda.blackberry.smartview.io.GeneralStoreInterface;
import com.bbsmart.pda.blackberry.smartview.io.ImageCache;
import com.bbsmart.pda.blackberry.smartview.io.PersistenceManager;
import com.bbsmart.pda.blackberry.smartview.io.Preferences;
import com.bbsmart.pda.blackberry.smartview.io.SmartNoteManager;
import com.bbsmart.pda.blackberry.smartview.net.ImageConnectionManager;
import com.bbsmart.pda.blackberry.smartview.ui.customfield.HTMLField;
import com.bbsmart.pda.blackberry.smartview.ui.customfield.ImageLoadingDialog;
import com.bbsmart.pda.blackberry.smartview.ui.customfield.SmartNoteListField;
import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;
import com.bbsmart.pda.blackberry.smartview.util.EmailFormatter;
import com.bbsmart.pda.blackberry.smartview.util.MessageUtils;
import com.bbsmart.pda.blackberry.smartview.util.MoreListener;
import com.bbsmart.pda.blackberry.smartview.util.MoreRetriever;
import com.bbsmart.pda.blackberry.smartview.util.StringUtils;

import net.rim.blackberry.api.invoke.CalendarArguments;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.invoke.TaskArguments;
import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.BodyPart;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.SupportedAttachmentPart;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.blackberry.api.mail.UnsupportedAttachmentPart;
import net.rim.device.api.browser.field.RenderingException;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public final class EmailViewScreen extends MainScreen implements MoreListener {
	public Font boldFont;

	public Font plainFont;

	private int headerFontHeight;

	private VerticalFieldManager fullInfoManager;

	private VerticalFieldManager shortInfoManager;

	private HTMLField htmlField;

	private Field htmlContentField;

	private boolean simpleDetails;

	private MessageUtils msgUtils;

	private EmailFormatter emailFormatter;

	private Message message;

	private NullField topFocus;

	private NullField endFocus;

	private Bitmap infoBar;

	private Bitmap infoBarBottom;

	private BitmapField infoBarField;

	private BitmapField infoBarBottomField;

	private Bitmap moreBar;

	private BitmapField moreBarField;

	private NullField[] moreFocusFields;

	private PersistenceManager persistManager;

	private Preferences prefs;

	private MoreRetriever moreRetriever;

	// Flag to indicate whether images are currently displayed in the email
	private boolean displayingImages;

	// Flag to indicate to prefer the address (rather than the name) when
	// displaying email addresses
	private boolean preferAddress;

	private boolean retrievingMore;

	private Screen mailScreen;

	private Menu mailScreenMenu;
	
	private Vector attachmentFields;
	
	private String usingEmailAddress;
	
	private SmartNoteManager snManager;
	public SmartNoteListField snListField;
	
	private int headerBackgroundColor;

	public EmailViewScreen(Message message, EmailFormatter formatter) {
		super(VERTICAL_SCROLL | VERTICAL_SCROLLBAR);

		this.message = message;
		this.emailFormatter = formatter;

		attachmentFields = new Vector();

		persistManager = PersistenceManager.getInstance();
		prefs = persistManager.getPreferences();

		initialiseSmartNotes();

		moreRetriever = new MoreRetriever();
		retrievingMore = false;

		msgUtils = new MessageUtils(this.message);

		preferAddress = false; // By default, always prefer name
		displayingImages = prefs.isDisplayImagesInEmail();
		msgUtils.setPreferAddress(preferAddress);

		mailScreen = UiApplication.getUiApplication().getActiveScreen();
		mailScreenMenu = mailScreen.getMenu(0);
		getSentReceivedUsing();

		initialiseDisplay();

		persistManager.getImageCache().autoClear();
	}

	private void initialiseSmartNotes() {
		snManager = persistManager.getSmartNoteManager();
		snListField = new SmartNoteListField(snManager, message);
	}

	private void getSentReceivedUsing() {
		Field first = mailScreen.getField(0);
		
		// OS 4.3 changed the layout of the email screen and so we must grab
		// the "Received Using" field in a different way
		/* //[s430]
		if (first instanceof VerticalFieldManager) {
			first = ((VerticalFieldManager) first).getField(0);
		}
		 //[e430] */		

		if (first instanceof EditField) {
			EditField usingField = (EditField) first;
			if (usingField.getLabel().startsWith("Received Using:")) {
				usingEmailAddress = usingField.getText();
			} else if (usingField.getLabel().startsWith("Sent Using:")) {
				usingEmailAddress = usingField.getText();
			} else {
				usingEmailAddress = null;
			}
		}

		if (usingEmailAddress == null) {
			headerBackgroundColor = Color.WHITE;
			return;
		}

		// Decide on the appropriate header background color
		// based on this email address
		Vector cprefs = persistManager.getEmailColorPrefs();
		for (int i = 0; i < cprefs.size(); i++) {
			EmailColorPreference cpref = (EmailColorPreference) cprefs
					.elementAt(i);

			if (cpref.emailAddress.equalsIgnoreCase(usingEmailAddress)
					|| usingEmailAddress.indexOf(cpref.emailAccountName) != -1) {
				headerBackgroundColor = cpref.color;
				return;
			}
		}

		headerBackgroundColor = Color.WHITE;
	}
	
	private void initialiseDisplay() {
		moreFocusFields = new NullField[2];

		initFonts();

		// A field to focus on when the user requests the top of the email ('t')
		topFocus = new NullField();
		add(topFocus);

		endFocus = new NullField();

		if (prefs.isShowFullDetails()) {
			simpleDetails = false;
			writeFullInfo(preferAddress);
		} else {
			simpleDetails = true;
			writeSimpleInfo(preferAddress);
		}
		displaySmartNotes();
		displayEmailBrowser();
	}

	public void displayEmailBrowser() {
		ImageConnectionManager.getInstance().reset();

		String content = emailFormatter.getFormattedContent();
		if (content.trim().length() == 0) {
			// No need to add a browser field if there is no content
			return;
		}

		try {
			htmlField = new HTMLField();
			htmlField.setContent(content);
			try {
				htmlContentField = htmlField.getHTMLField();
			} catch (RenderingException re) {
				// Never happens
			}
			add(htmlContentField);

			try {
				delete(endFocus);
			} catch (Exception npe) {
				// Do nothing - end focus hasn't been added yet
			}
			add(endFocus);

			addMoreRetriever();
		} catch (Exception e) {
			UiUtilities.alertDialog(e.getMessage());
		}
	}

	private MenuItem findDefaultMenuItem(String name) {
		final int M_SIZE = mailScreenMenu.getSize();
		MenuItem item;

		for (int i = 0; i < M_SIZE; i++) {
			item = mailScreenMenu.getItem(i);
			if (item.toString().equals(name)) {
				return item;
			}
		}

		return null;
	}

	private MenuItem matchDefaultMenuItem(String name) {
		final int M_SIZE = mailScreenMenu.getSize();
		MenuItem item;

		for (int i = 0; i < M_SIZE; i++) {
			item = mailScreenMenu.getItem(i);
			if (item.toString().startsWith(name)) {
				return item;
			}
		}

		return null;
	}

	public void notifySuccess(BodyPart bp) {
		if (bp instanceof TextBodyPart) {
			// Retrieve more of the text body part succeeded
			emailFormatter.formatEmail();

			retrievingMore = false;

			synchronized (UiApplication.getEventLock()) {
				delete(moreBarField);
				displayEmailBrowser();
				htmlContentField.setFocus();
				if (emailFormatter.getNumImagesInEmail() > 0) {
					UiApplication.getUiApplication().pushScreen(
							new ImageLoadingDialog(emailFormatter
									.getNumImagesInEmail()));
				}
			}
		}
	}

	public void notifyFailure(BodyPart bp, String errorMessage) {
		retrievingMore = false;

		if (bp instanceof TextBodyPart) {
			synchronized (UiApplication.getEventLock()) {
				delete(moreBarField);
				addMoreRetriever();
				UiUtilities.alertDialog(errorMessage);
				moreBarField.setFocus();
			}
		}
	}

	private void addMoreRetriever() {
		final TextBodyPart tbp = msgUtils.getTextBodyPart();
		final MoreListener listener = this;

		if (MoreRetriever.hasMore(tbp)) {
			moreBar = UiUtilities.getMoreOffBar();
			moreBarField = new BitmapField(moreBar, Field.FOCUSABLE);
			moreFocusFields[0] = new NullField();

			moreFocusFields[1] = new NullField() {
				protected void onFocus(int direction) {
					if (!retrievingMore) {
						retrievingMore = true;
						updateMoreRetriever();
						moreRetriever.retrieveMore(message, tbp, listener);
					}
				}
			};

			Graphics g = new Graphics(moreBar);
			g.setFont(getFont().derive(Font.BOLD, 13));
			g.setColor(Color.DARKBLUE);
			g.drawText("Retrieve More...", 0, 0);

			add(moreBarField);
			add(moreFocusFields[0]);
			add(moreFocusFields[1]);
		}
	}

	/**
	 * Displays the "Retrieving More" bar at the bottom of the screen when the
	 * user requests more.
	 */
	private void updateMoreRetriever() {
		synchronized (UiApplication.getEventLock()) {
			delete(moreBarField);
			delete(moreFocusFields[0]);
			delete(moreFocusFields[1]);

			moreBar = UiUtilities.getMoreOnBar();
			moreBarField = new BitmapField(moreBar, Field.FOCUSABLE);

			Graphics g = new Graphics(moreBar);
			g.setFont(getFont().derive(Font.BOLD, 13));
			g.setColor(Color.DARKBLUE);
			g.drawText("Retrieving More...", 0, 0);
			add(moreBarField);
			moreBarField.setFocus();
		}
	}

	private void initFonts() {
		String preference = GeneralStoreInterface.getHeaderFontSize();

		if (preference.equals("Small")) {
			headerFontHeight = 12;
		} else if (preference.equals("Medium")) {
			headerFontHeight = 14;
		} else {
			headerFontHeight = 16;
		}

		if (!UiUtilities.DEVICE_240W) {
			headerFontHeight += 4;
		}

		boldFont = getFont().derive(Font.BOLD, headerFontHeight);
		plainFont = getFont().derive(Font.PLAIN, headerFontHeight);
	}

	/**
	 * Writes out the basic information about an email in a small section at the
	 * top of the screen. Subject, from and time
	 * 
	 * @param g
	 */
	private void writeSimpleInfo(boolean preferAddress) {
		synchronized (UiApplication.getEventLock()) {

			// Occurs when changing to address not name
			if (getFieldCount() > 1 && getField(1).equals(shortInfoManager)) {
				delete(shortInfoManager);
			}

			// Remove the full info display (if visible)
			if (!simpleDetails) {
				delete(fullInfoManager);
			}

			shortInfoManager = new VerticalFieldManager() {
				protected void paint(Graphics graphics) {
					graphics.setBackgroundColor(headerBackgroundColor);
					graphics.clear();
					super.paint(graphics);
				}
			};

			infoBar = UiUtilities.getInfoBar();
			infoBarField = new BitmapField(infoBar);

			// Display the heading & text
			Graphics g = new Graphics(infoBar);
			g.setFont(getFont().derive(Font.BOLD, 10));
			g.setColor(Color.DARKBLUE);
			g.drawText("Summary", 0, 0);
			shortInfoManager.add(infoBarField);

			int offset = boldFont.getAdvance("Subject: ");

			// SUBJECT
			HorizontalFieldManager subjectManager = new HorizontalFieldManager();
			writeField(subjectManager, "Subject: ", message.getSubject(),
					offset);
			shortInfoManager.add(subjectManager);

			// FROM/TO
			if (message.isInbound()) {
				HorizontalFieldManager fromManager = new HorizontalFieldManager();
				writeField(fromManager, "From: ", msgUtils.getFrom(), offset);
				shortInfoManager.add(fromManager);
			} else {
				HorizontalFieldManager toManager = new HorizontalFieldManager();
				writeField(toManager, msgUtils.getShortRecipType(), msgUtils
						.getShortRecip(), offset);
				shortInfoManager.add(toManager);
			}

			// SENT
			boolean composing = (message.getStatus() == Message.Status.TX_COMPOSING);
			HorizontalFieldManager sentManager = new HorizontalFieldManager();
			writeField(sentManager, "Sent: ", (composing ? "" : msgUtils
					.getSentDate()), offset);
			shortInfoManager.add(sentManager);

			if (msgUtils.hasAttachments()) {
				displayAttachments(shortInfoManager);
			} else {
				infoBarBottom = UiUtilities.getInfoBarBottom();
				infoBarBottomField = new BitmapField(infoBarBottom);
				shortInfoManager.add(infoBarBottomField);
			}
			
			insert(shortInfoManager, 1);
			simpleDetails = true;
		}
	}

	private void writeFullInfo(boolean preferAddress) {
		synchronized (UiApplication.getEventLock()) {

			// Occurs when changing to address not name
			if (getFieldCount() > 1 && getField(1).equals(fullInfoManager)) {
				delete(fullInfoManager);
			}

			// Remove the simple info display (if currently visible)
			if (simpleDetails) {
				delete(shortInfoManager);
			}

			fullInfoManager = new VerticalFieldManager() {
				protected void paint(Graphics graphics) {
					graphics.setBackgroundColor(headerBackgroundColor);
					graphics.clear();
					super.paint(graphics);
				}
			};

			infoBar = UiUtilities.getInfoBar();
			infoBarField = new BitmapField(infoBar);

			// Display the heading & text
			Graphics g = new Graphics(infoBar);
			g.setFont(getFont().derive(Font.BOLD, 10));
			g.setColor(Color.DARKBLUE);
			g.drawText("Full Details", 0, 0);
			fullInfoManager.add(infoBarField);

			int len = boldFont.getAdvance("Reply-To: ");
			
			if (usingEmailAddress != null) {
				HorizontalFieldManager usingManager = new HorizontalFieldManager();
				writeField(usingManager, "Using: ", usingEmailAddress, len);
				fullInfoManager.add(usingManager);
			}

			HorizontalFieldManager statusManager = new HorizontalFieldManager();
			writeField(statusManager, "Status: ", msgUtils.getMessageStatus(),
					len);
			fullInfoManager.add(statusManager);

			HorizontalFieldManager priorityManager = new HorizontalFieldManager();
			writeColoredField(priorityManager, "Priority: ", msgUtils
					.getPriority(), len, msgUtils.getPriorityColor());
			fullInfoManager.add(priorityManager);

			HorizontalFieldManager subjectManager = new HorizontalFieldManager();
			writeField(subjectManager, "Subject: ", message.getSubject(), len);
			fullInfoManager.add(subjectManager);

			if (message.isInbound()) {
				HorizontalFieldManager fromManager = new HorizontalFieldManager();
				writeField(fromManager, "From: ", msgUtils.getFrom(), len);
				fullInfoManager.add(fromManager);
			}

			HorizontalFieldManager toManager = new HorizontalFieldManager();
			writeField(toManager, "To: ", msgUtils.makeFullToString(), len);
			fullInfoManager.add(toManager);

			String replyTo = msgUtils.getReplyTo();
			if (replyTo.length() > 0) {
				HorizontalFieldManager replyToManager = new HorizontalFieldManager();
				writeField(replyToManager, "Reply-To: ", replyTo, len);
				fullInfoManager.add(replyToManager);
			}

			String cc = msgUtils.makeFullCcString();
			if (cc.length() > 0) {
				HorizontalFieldManager ccManager = new HorizontalFieldManager();
				writeField(ccManager, "Cc: ", cc, len);
				fullInfoManager.add(ccManager);
			}

			String bcc = msgUtils.makeFullBccString();
			if (bcc.length() > 0) {
				HorizontalFieldManager bccManager = new HorizontalFieldManager();
				writeField(bccManager, "Bcc: ", bcc, len);
				fullInfoManager.add(bccManager);
			}

			HorizontalFieldManager sentManager = new HorizontalFieldManager();
			writeField(sentManager, "Sent: ", msgUtils.getSentDate(), len);
			fullInfoManager.add(sentManager);

			if (msgUtils.hasAttachments()) {
				displayAttachments(fullInfoManager);
			} else {
				infoBarBottom = UiUtilities.getInfoBarBottom();
				infoBarBottomField = new BitmapField(infoBarBottom);
				fullInfoManager.add(infoBarBottomField);
			}
			
			insert(fullInfoManager, 1);
			simpleDetails = false;
		}
	}

	public void displaySmartNotes() {
		Vector smartNotes = snManager.getSmartNotes(message);
		if (smartNotes.size() > 0) {
			// If there is at least one, display the field
			snListField.update();

			if (snListField.getIndex() == -1) {
				// Not attached to any manager at the moment
				insert(snListField, 2);
				insert(new BitmapField(UiUtilities.getInfoBarBottom()), 3);
			}
		} else {
			// There are no smart notes - remove the list field if necessary
			int indx = snListField.getIndex();
			if (indx != -1) {
				// Is current displayed
				snListField.getManager().deleteRange(indx, 2);
				topFocus.setFocus();
			}
		}
	}

	private void displayAttachments(Manager detailsManager) {
		// Add a single line at the top
		infoBarBottom = UiUtilities.getInfoBarBottom();
		infoBarBottomField = new BitmapField(infoBarBottom);
		detailsManager.add(infoBarBottomField);

		// Display the attachments
		Vector attachments = msgUtils.getAttachments();
		for (int i = 0; i < attachments.size(); i++) {
			BodyPart part = (BodyPart) attachments.elementAt(i);

			if (part instanceof SupportedAttachmentPart) {
				SupportedAttachmentPart sa = (SupportedAttachmentPart) part;
				addAttachmentField(detailsManager, sa.getName());
			} else if (part instanceof UnsupportedAttachmentPart) {
				UnsupportedAttachmentPart usa = (UnsupportedAttachmentPart) part;
				addAttachmentField(detailsManager, usa.getName());
			}
		}

		// Add the closing line
		infoBarBottom = UiUtilities.getInfoBarBottom();
		infoBarBottomField = new BitmapField(infoBarBottom);
		detailsManager.add(infoBarBottomField);
	}

	private void addAttachmentField(Manager m, String filename) {
		HorizontalFieldManager hm = new HorizontalFieldManager();

		// Display the appropriate icon next to the filename
		if (filename.endsWith(".doc")) {
			hm.add(new BitmapField(Bitmap
					.getBitmapResource("img/icons/icon_doc.gif")));
		} else if (filename.endsWith(".pdf")) {
			hm.add(new BitmapField(Bitmap
					.getBitmapResource("img/icons/icon_pdf.gif")));
		} else if (filename.endsWith(".xls") || filename.endsWith(".csv")) {
			hm.add(new BitmapField(Bitmap
					.getBitmapResource("img/icons/icon_xls.gif")));
		} else if (filename.endsWith(".ppt")) {
			hm.add(new BitmapField(Bitmap
					.getBitmapResource("img/icons/icon_ppt.gif")));
		} else if (ImageCache.isImage(filename)) {
			hm.add(new BitmapField(Bitmap
					.getBitmapResource("img/icons/icon_image.gif")));
		} else if (filename.endsWith(".txt")) {
			hm.add(new BitmapField(Bitmap
					.getBitmapResource("img/icons/icon_txt.gif")));
		} else if (filename.endsWith(".zip") || filename.endsWith(".rar")) {
			hm.add(new BitmapField(Bitmap
					.getBitmapResource("img/icons/icon_zip.gif")));
		} else {
			hm.add(new BitmapField(Bitmap
					.getBitmapResource("img/icons/icon_generic.gif")));
		}

		if (GeneralStoreInterface.isHeaderBoldFont()) {
			setFont(boldFont);
		} else {
			setFont(plainFont);
		}
		
		HighlightedEditField field = new HighlightedEditField(filename,
				BasicEditField.DEFAULT_MAXCHARS, BasicEditField.READONLY);
		hm.add(field);
		m.add(hm);
		
		attachmentFields.addElement(field);
	}

	private void writeColoredField(Manager m, String title, String text,
			int len, final int color) {
		Bitmap b = new Bitmap(len, headerFontHeight);
		BitmapField bField = new BitmapField(b);

		Graphics g = new Graphics(b);
		g.setColor(headerBackgroundColor);
		g.fillRect(0, 0, len, headerFontHeight);
		g.setColor(Color.BLACK);
		g.setFont(boldFont);
		g.drawText(title, 0, 0);

		m.add(bField);

		if (GeneralStoreInterface.isHeaderBoldFont()) {
			setFont(boldFont);
		} else {
			setFont(plainFont);
		}
		
		BasicEditField field = new BasicEditField(UiUtilities.EMPTY_STRING,
				text, BasicEditField.DEFAULT_MAXCHARS, BasicEditField.READONLY
						| BasicEditField.NON_FOCUSABLE) {
			protected void paint(Graphics graphics) {
				graphics.setBackgroundColor(headerBackgroundColor);
				graphics.setColor(color);
				graphics.clear();
				super.paint(graphics);
			}
		};
		m.add(field);
	}

	private void writeField(Manager m, String title, String text, int len) {
		writeColoredField(m, title, text, len, Color.BLACK);
	}

	private static String showDetailsMenuItemText = UiUtilities.DEVICE_SURETYPE ? "Show Full Details (G)"
			: "Show Full Details (Q)";

	private MenuItem showDetailsMenuItem = new MenuItem(
			showDetailsMenuItemText, 0, 0) {
		public void run() {
			writeFullInfo(preferAddress);
			UiUtilities.setFocus(topFocus);
		}
	};

	private static String hideDetailsMenuItemText = UiUtilities.DEVICE_SURETYPE ? "Hide Full Details (G)"
			: "Hide Full Details (Q)";

	private MenuItem hideDetailsMenuItem = new MenuItem(
			hideDetailsMenuItemText, 0, 0) {
		public void run() {
			writeSimpleInfo(preferAddress);
			UiUtilities.setFocus(topFocus);
		}
	};

	private MenuItem showImagesMenuItem = new MenuItem("Show Images", 0, 0) {
		public void run() {
			// TODO: Fix up
			/*displayingImages = true;
			prefs.setDisplayImagesInEmail(true);
			persistManager.savePreferences(prefs);
			UiApplication.getUiApplication().popScreen(
					UiApplication.getUiApplication().getActiveScreen());
			UiApplication.getUiApplication()
					.pushScreen(
							new EmailLoadingDialog(message, new EmailFormatter(
									message))); */
		}
	};

	private MenuItem hideImagesMenuItem = new MenuItem("Hide Images", 0, 0) {
		public void run() {
			// TODO: Fix up
			/* displayingImages = false;
			prefs.setDisplayImagesInEmail(false);
			persistManager.savePreferences(prefs);
			UiApplication.getUiApplication().popScreen(
					UiApplication.getUiApplication().getActiveScreen());
			UiApplication.getUiApplication()
					.pushScreen(
							new EmailLoadingDialog(message, new EmailFormatter(
									message))); */
		}
	};

	private MenuItem showAddressesMenuItem = new MenuItem("Show Addresses", 0,
			0) {
		public void run() {
			preferAddress = true;
			msgUtils.setPreferAddress(true);

			if (simpleDetails) {
				hideDetailsMenuItem.run();
			} else {
				showDetailsMenuItem.run();
			}
		}
	};

	private MenuItem hideAddressesMenuItem = new MenuItem("Hide Addresses", 0,
			0) {
		public void run() {
			preferAddress = false;
			msgUtils.setPreferAddress(false);

			if (simpleDetails) {
				hideDetailsMenuItem.run();
			} else {
				showDetailsMenuItem.run();
			}
		}
	};
	
	private MenuItem addSmartNoteMenuItem = new MenuItem("Add SmartNote", 0, 0) {
		public void run() {
			UiApplication.getUiApplication().pushScreen(new SmartNoteScreen(message));
		}
	};

	private MenuItem addAsTaskMenuItem = new MenuItem("Add as Task", 0, 0) {
		public void run() {
			/* //[s402]
			 String message = "Sorry, your BlackBerry does not support this feature.\n"
			 + "You must have, at minimum, an OS version of 4.2.0";
			 
			 UiUtilities.alertDialog(message);
			 return;
			 //[e402] */

			/* //[s410]
			 String message = "Sorry, your BlackBerry does not support this feature.\n"
			 + "You must have, at minimum, an OS version of 4.2.0";
			 
			 UiUtilities.alertDialog(message);
			 return;
			 //[e410] */

			/* //[s420]
			 try {
			 PIM pim = PIM.getInstance();

			 ToDoList tdList = (ToDoList) pim
			 .openPIMList(PIM.TODO_LIST, PIM.READ_WRITE);

			 ToDo task = tdList.createToDo();
			 if (prefs.isAutoAddTaskNotes()) {
			 task.addString(ToDo.NOTE, 0, "\n\n" + message.getBodyText());
			 }
			 if (prefs.isAutoSetTaskSubject()) {
			 task.addString(ToDo.SUMMARY, 0, message.getSubject());
			 }
			 TaskArguments tArgs = new TaskArguments(TaskArguments.ARG_NEW,
			 task);

			 Invoke.invokeApplication(Invoke.APP_TYPE_TASKS, tArgs);
			 } catch (Exception e) {
			 UiUtilities
			 .alertDialog("Sorry, an error was encountered adding this email as a task");	
			 }
			 //[e420] */
			
			/* //[s430]
			 try {
			 PIM pim = PIM.getInstance();

			 ToDoList tdList = (ToDoList) pim
			 .openPIMList(PIM.TODO_LIST, PIM.READ_WRITE);

			 ToDo task = tdList.createToDo();
			 if (prefs.isAutoAddTaskNotes()) {
			 task.addString(ToDo.NOTE, 0, "\n\n" + message.getBodyText());
			 }
			 if (prefs.isAutoSetTaskSubject()) {
			 task.addString(ToDo.SUMMARY, 0, message.getSubject());
			 }
			 TaskArguments tArgs = new TaskArguments(TaskArguments.ARG_NEW,
			 task);

			 Invoke.invokeApplication(Invoke.APP_TYPE_TASKS, tArgs);
			 } catch (Exception e) {
			 UiUtilities
			 .alertDialog("Sorry, an error was encountered adding this email as a task");	
			 }
			 //[e430] */
		}
	};

	private MenuItem addToCalendarMenuItem = new MenuItem("Add to Calendar", 0,
			0) {
		public void run() {
			/* //[s402]
			String message = "Sorry, your BlackBerry does not support this feature.\n"
			 + "You must have, at minimum, an OS version of 4.1.0";
			 
			 UiUtilities.alertDialog(message);
			 return;
			 //[e402] */
			
			/* //[s410]
			try {
			EventList evList = (EventList) PIM.getInstance().openPIMList(
					PIM.EVENT_LIST, PIM.READ_WRITE);
			Event e = evList.createEvent();
			e.addDate(Event.START, 0, System.currentTimeMillis());
			e.addDate(Event.END, 0, System.currentTimeMillis() + 3600000L);
			
			if (prefs.isAutoSetCalendarSubject()) {
				e.addString(Event.SUMMARY, 0, message.getSubject());
			}
			if (prefs.isAutoAddCalendarNotes()) {
				e.addString(Event.NOTE, 0, "\n\n" + message.getBodyText());
			}
			 CalendarArguments args = new CalendarArguments(
					CalendarArguments.ARG_NEW, e);

			Invoke.invokeApplication(Invoke.APP_TYPE_CALENDAR, args);
			} catch (Exception e) {
			 UiUtilities
			 .alertDialog("Sorry, an error was encountered adding this email as a calendar event");	
			 }
			 //[e410] */
			
			/* //[s420]
			try {
			EventList evList = (EventList) PIM.getInstance().openPIMList(
					PIM.EVENT_LIST, PIM.READ_WRITE);
			Event e = evList.createEvent();
			e.addDate(Event.START, 0, System.currentTimeMillis());
			e.addDate(Event.END, 0, System.currentTimeMillis() + 3600000L);
			if (prefs.isAutoSetCalendarSubject()) {
				e.addString(Event.SUMMARY, 0, message.getSubject());
			}
			if (prefs.isAutoAddCalendarNotes()) {
				e.addString(Event.NOTE, 0, "\n\n" + message.getBodyText());
			}
			 CalendarArguments args = new CalendarArguments(
					CalendarArguments.ARG_NEW, e);

			Invoke.invokeApplication(Invoke.APP_TYPE_CALENDAR, args);
			} catch (Exception e) {
			 UiUtilities
			 .alertDialog("Sorry, an error was encountered adding this email as a calendar event");	
			 }
			//[e420] */
			
			/* //[s430]
			try {
			EventList evList = (EventList) PIM.getInstance().openPIMList(
					PIM.EVENT_LIST, PIM.READ_WRITE);
			Event e = evList.createEvent();
			e.addDate(Event.START, 0, System.currentTimeMillis());
			e.addDate(Event.END, 0, System.currentTimeMillis() + 3600000L);
			if (prefs.isAutoSetCalendarSubject()) {
				e.addString(Event.SUMMARY, 0, message.getSubject());
			}
			if (prefs.isAutoAddCalendarNotes()) {
				e.addString(Event.NOTE, 0, "\n\n" + message.getBodyText());
			}
			 CalendarArguments args = new CalendarArguments(
					CalendarArguments.ARG_NEW, e);

			Invoke.invokeApplication(Invoke.APP_TYPE_CALENDAR, args);
			} catch (Exception e) {
			 UiUtilities
			 .alertDialog("Sorry, an error was encountered adding this email as a calendar event");	
			 }
			//[e430] */
		}
	};

	private static String replyMenuItemText = UiUtilities.DEVICE_SURETYPE ? "Reply (Q)"
			: "Reply (R)";

	private MenuItem replyMenuItem = new MenuItem(replyMenuItemText, 0, 0) {
		public void run() {
			findDefaultMenuItem("Reply").run();
			if (UiApplication.getUiApplication().getActiveScreen()
					.getScreenBelow() != mailScreen) {
				close();
			}
		}
	};

	private static String replyToAllMenuItemText = UiUtilities.DEVICE_SURETYPE ? "Reply To All (A)"
			: "Reply To All (L)";

	private MenuItem replyToAllMenuItem = new MenuItem(replyToAllMenuItemText,
			0, 0) {
		public void run() {
			findDefaultMenuItem("Reply To All").run();
			if (UiApplication.getUiApplication().getActiveScreen()
					.getScreenBelow() != mailScreen) {
				close();
			}
		}
	};

	private static String forwardMenuItemText = UiUtilities.DEVICE_SURETYPE ? "Forward (O)"
			: "Forward (F)";

	private MenuItem forwardMenuItem = new MenuItem(forwardMenuItemText, 0, 0) {
		public void run() {
			findDefaultMenuItem("Forward").run();
			if (UiApplication.getUiApplication().getActiveScreen()
					.getScreenBelow() != mailScreen) {
				close();
			}
		}
	};

	private MenuItem forwardAsMenuItem = new MenuItem("Forward As", 0, 0) {
		public void run() {
			findDefaultMenuItem("Forward As").run();
			if (UiApplication.getUiApplication().getActiveScreen()
					.getScreenBelow() != mailScreen) {
				close();
			}
		}
	};

	private MenuItem deleteMenuItem = new MenuItem("Delete", 0, 0) {
		public void run() {
			findDefaultMenuItem("Delete").run();

			if (UiApplication.getUiApplication().getActiveScreen()
					.getScreenBelow() != mailScreen) {
				snManager.deleteSmartNotes(message);
				close();
			}
		}
	};

	private MenuItem editMenuItem = new MenuItem("Edit", 0, 0) {
		public void run() {
			findDefaultMenuItem("Edit").run();
			if (UiApplication.getUiApplication().getActiveScreen()
					.getScreenBelow() != mailScreen) {
				close();
			}
		}
	};

	private MenuItem resendMenuItem = new MenuItem("Resend", 0, 0) {
		public void run() {
			findDefaultMenuItem("Resend").run();
			if (UiApplication.getUiApplication().getActiveScreen()
					.getScreenBelow() != mailScreen) {
				close();
			}
		}
	};

	private static String previousMenuItemText = UiUtilities.DEVICE_SURETYPE ? "Previous Item (D)"
			: "Previous Item (P)";

	private MenuItem previousMenuItem = new MenuItem(previousMenuItemText, 0, 0) {
		public void run() {
			final MenuItem previous = findDefaultMenuItem("Previous Item");
			if (previous == null)
				return;

			synchronized (UiApplication.getEventLock()) {
				close();
				previous.run();
			}
		}
	};

	private static String nextMenuItemText = UiUtilities.DEVICE_SURETYPE ? "Next Item (J)"
			: "Next Item (N)";

	private MenuItem nextMenuItem = new MenuItem("Next Item (N)", 0, 0) {
		public void run() {
			final MenuItem next = findDefaultMenuItem("Next Item");
			if (next == null)
				return;

			synchronized (UiApplication.getEventLock()) {
				close();
				next.run();
			}
		}
	};

	private static String nextUnopenedMenuItemText = UiUtilities.DEVICE_SURETYPE ? "Next Unopened Item (*)"
			: "Next Unopened Item (U)";

	private MenuItem nextUnopenedMenuItem = new MenuItem(
			nextUnopenedMenuItemText, 0, 0) {
		public void run() {
			final MenuItem nextU = findDefaultMenuItem("Next Unopened Item");
			if (nextU == null)
				return;

			synchronized (UiApplication.getEventLock()) {
				close();
				nextU.run();
			}
		}
	};

	private MenuItem activateTrialMenuItem = new MenuItem("Buy/Activate", 0, 0) {
		public void run() {
			UiApplication.getUiApplication().pushScreen(
					new RegisterScreen());
		}
	};

	private MenuItem saveMenuItem = new MenuItem("Save", 0, 0) {
		public void run() {
			final MenuItem save = findDefaultMenuItem("Save");
			if (save == null)
				return;

			synchronized (UiApplication.getEventLock()) {
				save.run();

				// Update the previous screen menu
				mailScreenMenu = mailScreen.getMenu(0);
			}
		}
	};

	private MenuItem unopenedMenuItem = new MenuItem("Mark Unopened", 0, 0) {
		public void run() {
			final MenuItem unopened = findDefaultMenuItem("Mark Unopened");
			if (unopened == null)
				return;

			synchronized (UiApplication.getEventLock()) {
				unopened.run();

				// Update the previous screen menu
				mailScreenMenu = mailScreen.getMenu(0);
			}
		}
	};

	private MenuItem openedMenuItem = new MenuItem("Mark Opened", 0, 0) {
		public void run() {
			final MenuItem opened = findDefaultMenuItem("Mark Opened");
			if (opened == null)
				return;

			synchronized (UiApplication.getEventLock()) {
				opened.run();

				// Update the previous screen menu
				mailScreenMenu = mailScreen.getMenu(0);
			}
		}
	};
	
	private MenuItem fileMenuItem = new MenuItem("File (I)", 0, 0) {
		public void run() {
			final MenuItem file = findDefaultMenuItem("File");
			if (file == null)
				return;

			file.run();
		}
	};

	private MenuItem viewContactMenuItem = new MenuItem("View Contact", 0, 0) {
		public void run() {
			final MenuItem contact = findDefaultMenuItem("View Contact");
			if (contact == null)
				return;

			synchronized (UiApplication.getEventLock()) {
				contact.run();

				// Update the previous screen menu
				mailScreenMenu = mailScreen.getMenu(0);
			}
		}
	};

	private MenuItem addressMenuItem = new MenuItem("Add To Address Book", 0, 0) {
		public void run() {
			final MenuItem address = findDefaultMenuItem("Add To Address Book");
			if (address == null)
				return;

			synchronized (UiApplication.getEventLock()) {
				address.run();

				// Update the previous screen menu
				mailScreenMenu = mailScreen.getMenu(0);
			}
		}
	};

	private MenuItem viewAttachments = new MenuItem("View Attachment(s)", 0, 0) {
		public void run() {
			final MenuItem attach = findDefaultMenuItem("Open Attachment");
			if (attach == null)
				return;

			attach.run();
		}
	};
	
	private MenuItem regularView = new MenuItem("Regular View", 0, 0) {
		public void run() {
			UiApplication.getUiApplication().popScreen(
					UiApplication.getUiApplication().getActiveScreen());
		}
	};
	
	private void toggleFullDetails() {
		if (simpleDetails) {
			showDetailsMenuItem.run();
		} else {
			hideDetailsMenuItem.run();
		}
	}

	protected void makeMenu(Menu menu, int instance) {
		super.makeMenu(menu, instance);
		MenuItem m;

		if ((m = findDefaultMenuItem("Open with RepliGo")) != null) {
			m.setPriority(0);
			m.setOrdinal(0);
			menu.add(m);
		}

		if ((m = findDefaultMenuItem("Open with BeamBerry")) != null) {
			m.setPriority(0);
			m.setOrdinal(0);
			menu.add(m);
		}
		
		if ((m = findDefaultMenuItem("Save as Template")) != null) {
			m.setPriority(0);
			m.setOrdinal(0);
			menu.add(m);
		}
		
		menu.add(MenuItem.separator(0));

		menu.add(regularView);

		if (simpleDetails) {
			menu.add(showDetailsMenuItem);
		} else {
			menu.add(hideDetailsMenuItem);
		}

		if (displayingImages) {
			menu.add(hideImagesMenuItem);
		} else {
			menu.add(showImagesMenuItem);
		}

		Field focusField = getFieldWithFocus();
		if (focusField.equals(topFocus)) {
			if (preferAddress) {
				menu.add(hideAddressesMenuItem);
			} else {
				menu.add(showAddressesMenuItem);
			}
		}

		if ((m = findDefaultMenuItem("Open Attachment")) != null) {
			menu.add(viewAttachments);
		}

		menu.add(MenuItem.separator(0));

		if ((m = findDefaultMenuItem("Reply")) != null) {
			menu.add(replyMenuItem);
		}

		if ((m = findDefaultMenuItem("Reply To All")) != null) {
			menu.add(replyToAllMenuItem);
		}

		if ((m = findDefaultMenuItem("Forward")) != null) {
			menu.add(forwardMenuItem);
		}

		if ((m = findDefaultMenuItem("Forward As")) != null) {
			menu.add(forwardAsMenuItem);
		}

		menu.add(MenuItem.separator(0));

		if ((m = findDefaultMenuItem("Save")) != null) {
			menu.add(saveMenuItem);
		}

		if ((m = findDefaultMenuItem("Delete")) != null) {
			menu.add(deleteMenuItem);
		}

		if ((m = findDefaultMenuItem("Edit")) != null) {
			menu.add(editMenuItem);
		}

		if ((m = findDefaultMenuItem("Resend")) != null) {
			menu.add(resendMenuItem);
		}

		if ((m = findDefaultMenuItem("Mark Unopened")) != null) {
			menu.add(unopenedMenuItem);
		}

		if ((m = findDefaultMenuItem("Mark Opened")) != null) {
			menu.add(openedMenuItem);
		}

		if ((m = findDefaultMenuItem("File")) != null) {
			menu.add(fileMenuItem);
		}

		if ((m = findDefaultMenuItem("View Folder")) != null) {
			m.setPriority(0);
			m.setOrdinal(0);
			menu.add(m);
		}

		menu.add(MenuItem.separator(0));
		menu.add(addSmartNoteMenuItem);
		menu.add(addAsTaskMenuItem);
		menu.add(addToCalendarMenuItem);
		menu.add(MenuItem.separator(0));

		if ((m = findDefaultMenuItem("Previous Item")) != null) {
			menu.add(previousMenuItem);
		}

		if ((m = findDefaultMenuItem("Next Item")) != null) {
			menu.add(nextMenuItem);
		}

		if ((m = findDefaultMenuItem("Next Unopened Item")) != null) {
			menu.add(nextUnopenedMenuItem);
		}

		menu.add(MenuItem.separator(0));

		if ((m = findDefaultMenuItem("Add To Address Book")) != null) {
			menu.add(addressMenuItem);
		}

		if ((m = findDefaultMenuItem("View Contact")) != null) {
			menu.add(viewContactMenuItem);
		}

		if ((m = matchDefaultMenuItem("Email ")) != null) {
			m.setOrdinal(0);
			m.setPriority(0);
			menu.add(m);
		}
		
		if ((m = matchDefaultMenuItem("PIN ")) != null) {
			m.setOrdinal(0);
			m.setPriority(0);
			menu.add(m);
		}

		if ((m = matchDefaultMenuItem("MMS ")) != null) {
			m.setOrdinal(0);
			m.setPriority(0);
			menu.add(m);
		}

		if ((m = matchDefaultMenuItem("Call ")) != null) {
			m.setOrdinal(0);
			m.setPriority(0);
			menu.add(m);
		}

		if ((m = matchDefaultMenuItem("SMS ")) != null) {
			m.setOrdinal(0);
			m.setPriority(0);
			menu.add(m);
		}

		menu.add(MenuItem.separator(0));

		if (prefs.isTrial()) {
			menu.add(activateTrialMenuItem);
		}
		menu.add(MenuItem.separator(0));
		
		// If focused on an attachment field
		Field leaffocusField = getLeafFieldWithFocus();
		if (attachmentFields.contains(leaffocusField)) {
			menu.setDefault(viewAttachments);
		}
	}

	private void exitToMessageList() {
		// Close this and the previously opened
		// email view screen
		UiApplication.getUiApplication().popScreen(
				UiApplication.getUiApplication().getActiveScreen());
		UiApplication.getUiApplication().popScreen(
				UiApplication.getUiApplication().getActiveScreen());
	}

	public boolean onClose() {
		if (this.getScreenBelow() == mailScreen) {
			exitToMessageList();
		} else {
			close();
		}
		return true;
	}

	private void scrollScreen(boolean page, boolean up) {
		synchronized (UiApplication.getEventLock()) {
			int amount = page ? 6 : 1;
			for (int i = 0; i < amount; i++) {
				trackwheelRoll(up ? -1 : 1, 0, 0);
			}
		}
	}
	
	protected boolean keyDown(int keycode, int time) {
		final int SURE_SYMBOL_KEY = 1310720;

		if (keycode == SURE_SYMBOL_KEY) {
			nextUnopenedMenuItem.run();
		}

		return false;
	}

	protected boolean keyChar(char c, int status, int time) {
		if (getLeafFieldWithFocus() == snListField) {
			return snListField.keyChar(c, status, time);
		}
		
		switch (c) {
		case Characters.ENTER: // Clicks a hyperlink if currently selected
			MenuItem i = getMenu(0).getDefault();
			if (i != null && i.toString().equals("Get Link")) {
				i.run();
				return true;
			}
			return false;
		case 8: // DELETE KEY
			deleteMenuItem.run();
			return true;
		case 'd':
			if (UiUtilities.DEVICE_SURETYPE) {
				previousMenuItem.run();
			}
			return true;
		case 'i':
			fileMenuItem.run();
			return true;
		case 'j':
			if (UiUtilities.DEVICE_SURETYPE) {
				nextMenuItem.run();
			}
			return true;
		case 'r':
			replyMenuItem.run();
			return true;
		case 'l':
			replyToAllMenuItem.run();
			return true;
		case 'f':
			forwardMenuItem.run();
			return true;
		case 't':
			if (UiUtilities.DEVICE_SURETYPE) {
				// Line up
				scrollScreen(false, true);
			} else {
				UiUtilities.setFocus(topFocus);
			}
			return true;
		case 'b':
			if (UiUtilities.DEVICE_SURETYPE) {
				// Line down
				scrollScreen(false, false);
			} else {
				UiUtilities.setFocus(endFocus);
			}
			return true;
		case ' ':
			scrollScreen(true, (status == 1));
			return true;
		case 'u':
			if (UiUtilities.DEVICE_SURETYPE) {
				scrollScreen(true, true);
			} else {
				nextUnopenedMenuItem.run();
			}
			return true;
		case 'm':
			if (UiUtilities.DEVICE_SURETYPE) {
				scrollScreen(true, false);
			}
			return true;
		case 'n':
			nextMenuItem.run();
			return true;
		case 'p':
			previousMenuItem.run();
			return true;
		case '0':
			if (UiUtilities.DEVICE_SURETYPE) {
				scrollScreen(true, true);
			}
			return true;
		case 'e':
			if (UiUtilities.DEVICE_SURETYPE) {
				UiUtilities.setFocus(topFocus);
			}
			return true;
		case 'c':
			if (UiUtilities.DEVICE_SURETYPE) {
				UiUtilities.setFocus(endFocus);
			}
			return true;
		case 'q': // REPLY ON SURE-TYPE DEVICES
			if (UiUtilities.DEVICE_SURETYPE) {
				replyMenuItem.run();
				return true;
			} else {
				// Toggle full details on non-suretype devices
				toggleFullDetails();
			}
			return true;
		case 'g':
			if (UiUtilities.DEVICE_SURETYPE) {
				// Toggle full details on sure type devices
				toggleFullDetails();
			}
			return true;
		case 'a': // REPLY-TO-ALL ON SURE-TYPE DEVICES
			if (UiUtilities.DEVICE_SURETYPE) {
				replyToAllMenuItem.run();
				return true;
			}
			return true;
		case 'o': // FORWARD ON SURE-TYPE DEVICES
			if (UiUtilities.DEVICE_SURETYPE) {
				forwardMenuItem.run();
				return true;
			}
			return true;
		default:
			return super.keyChar(c, status, time);
		}
	}
	
	class HighlightedEditField extends BasicEditField {
		private int backgroundColor = Color.WHITE;

		public HighlightedEditField(String initialValue, int maxNumChars,
				long style) {
			super("", initialValue, maxNumChars, style);
		}

		protected void onFocus(int arg0) {
			backgroundColor = Color.LIGHTBLUE;
			super.onFocus(arg0);
			invalidate();
		}

		protected void onUnfocus() {
			backgroundColor = Color.WHITE;
			super.onUnfocus();
			invalidate();
		}

		protected void paint(Graphics graphics) {
			graphics.setBackgroundColor(backgroundColor);
			graphics.clear();
			super.paint(graphics);
		}
	}
}