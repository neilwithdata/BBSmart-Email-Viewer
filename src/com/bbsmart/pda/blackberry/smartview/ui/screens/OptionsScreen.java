package com.bbsmart.pda.blackberry.smartview.ui.screens;

import java.util.Vector;

import com.bbsmart.pda.blackberry.smartview.BuildVersion;
import com.bbsmart.pda.blackberry.smartview.SmartView;
import com.bbsmart.pda.blackberry.smartview.io.EmailColorPreference;
import com.bbsmart.pda.blackberry.smartview.io.GeneralStoreInterface;
import com.bbsmart.pda.blackberry.smartview.io.ImageCache;
import com.bbsmart.pda.blackberry.smartview.io.PersistenceManager;
import com.bbsmart.pda.blackberry.smartview.io.Preferences;
import com.bbsmart.pda.blackberry.smartview.net.HttpConnectionThread;
import com.bbsmart.pda.blackberry.smartview.net.HttpContentReceiver;
import com.bbsmart.pda.blackberry.smartview.ui.customfield.GenericLoadingDialog;
import com.bbsmart.pda.blackberry.smartview.ui.customfield.HTMLField;
import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;
import com.bbsmart.pda.blackberry.smartview.util.ColorPickerField;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.mail.ServiceConfiguration;
import net.rim.device.api.browser.field.RenderingException;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

public final class OptionsScreen extends MainScreen implements
		FieldChangeListener {
	private Bitmap headingBitmap;

	private BitmapField heading;

	private CheckboxField allowImagesCheckbox;

	private CheckboxField setDefaultViewerCheckbox;

	private CheckboxField alwaysShowFullDetails;

	private CheckboxField showFullLinksCheckbox;

	private CheckboxField autoSetTaskTitleCheckbox;

	private CheckboxField autoAddTaskNotesCheckbox;

	private CheckboxField autoSetCalendarSubjectCheckbox;

	private CheckboxField autoAddCalendarNotesCheckbox;

	private ButtonField saveAndExitButton;

	private PersistenceManager persistManager;

	private Preferences prefs;

	private ButtonField connOptions;

	private ObjectChoiceField ccFreqField;

	private Bitmap emailOptionsBitmap;

	private BitmapField emailOptionsBitmapField;

	private Bitmap connOptionsBitmap;

	private BitmapField connOptionsBitmapField;

	private Bitmap cacheOptionsBitmap;

	private BitmapField cacheOptionsBitmapField;

	private BitmapField emailTextOptionsBitmapField;

	private Bitmap emailTextOptionsBitmap;

	private Bitmap pimOptionsBitmap;

	private BitmapField pimOptionsBitmapField;

	private Bitmap headerOptionsBitmap;

	private BitmapField headerOptionsBitmapField;

	private CheckboxField headerBold;
	private ObjectChoiceField headerFontSizeOptions;

	private ObjectChoiceField fontSizeOptions;

	private ObjectChoiceField fontFaceOptions;

	private ColorPickerField bgColorPicker;

	private ColorPickerField fontColorPicker;

	private ColorPickerField emailColorPicker;
	private ObjectChoiceField emails;

	public OptionsScreen() {
		persistManager = PersistenceManager.getInstance();
		prefs = persistManager.getPreferences();

		// Display the heading
		headingBitmap = UiUtilities.getHeading();
		writeHeading(new Graphics(headingBitmap));
		heading = new BitmapField(headingBitmap);
		add(heading);

		fontPreviewContainer = new HTMLField();

		initColorPickers();

		displayOptions();
	}

	private void initColorPickers() {
		Font font = Font.getDefault();
		int bgColor = prefs.getFontBgColor();
		int fontColor = prefs.getFontColor();
		// String choicesValue = "The quick brown fox jumped over the lazy man's
		// back.";

		bgColorPicker = new ColorPickerField(true, font, fontColor, bgColor,
				null, this, 99);

		fontColorPicker = new ColorPickerField(false, font, fontColor, bgColor,
				null, this, 100);
	}

	public void fieldChanged(Field field, int context) {
		if (context == 99) {
			int bgColor = bgColorPicker.getBgColor();
			fontColorPicker.setBgColor(bgColor);
			fontColorPicker.setFontColor(bgColorPicker.getFontColor());

			synchronized (UiApplication.getEventLock()) {
				displayFontPreviewPane();
			}
		} else if (context == 100) {
			int fontColor = fontColorPicker.getFontColor();
			bgColorPicker.setFontColor(fontColor);
			bgColorPicker.setBgColor(fontColorPicker.getBgColor());

			synchronized (UiApplication.getEventLock()) {
				displayFontPreviewPane();
			}
		} else {
			// Changed email background color - save new color preference
			saveEmailColorPrefs();
		}
	}

	private void writeHeading(Graphics g) {
		g.setColor(Color.FIREBRICK);

		if (UiUtilities.DEVICE_240W) {
			g.setFont(getFont().derive(Font.BOLD, 16));
			g.drawText("OPTIONS SCREEN", 90, 8);
		} else {
			g.setFont(getFont().derive(Font.BOLD, 18));
			g.drawText("OPTIONS SCREEN", 120, 13);
		}

		g.setFont(g.getFont().derive(g.getFont().getStyle(),
				g.getFont().getHeight() - 4));

		String trialVersionText = SmartView.VERSION
				+ (prefs.isTrial() ? " - Trial" : " - Registered");
		int trialVersionLen = g.getFont().getAdvance(trialVersionText);

		g.drawText(trialVersionText, Graphics.getScreenWidth()
				- trialVersionLen, 0);
	}

	private void displayOptions() {
		// *********DISPLAY EMAIL OPTIONS*********
		emailOptionsBitmap = UiUtilities.getMoreOffBar();
		emailOptionsBitmapField = new BitmapField(emailOptionsBitmap);

		Graphics g = new Graphics(emailOptionsBitmap);
		g.setFont(getFont().derive(Font.BOLD, 13));
		g.setColor(Color.DARKBLUE);
		g.drawText("Email Display Options", 0, 1);

		add(emailOptionsBitmapField);

		allowImagesCheckbox = new CheckboxField("Display Images in Email",
				prefs.isDisplayImagesInEmail());
		add(allowImagesCheckbox);

		setDefaultViewerCheckbox = new CheckboxField(
				"Set as Default Email Viewer", prefs
						.isDefaultDisplaySmartView());
		add(setDefaultViewerCheckbox);

		alwaysShowFullDetails = new CheckboxField(
				"Always Show Full Details in Email", prefs.isShowFullDetails());
		add(alwaysShowFullDetails);

		showFullLinksCheckbox = new CheckboxField(
				"Show Full Hyperlinks in Email", prefs.isShowFullLinks());
		add(showFullLinksCheckbox);
		// *********END DISPLAY EMAIL OPTIONS*********

		// *********HEADER DISPLAY OPTIONS*********
		headerOptionsBitmap = UiUtilities.getMoreOnBar();
		headerOptionsBitmapField = new BitmapField(headerOptionsBitmap);

		g = new Graphics(headerOptionsBitmap);
		g.setFont(getFont().derive(Font.BOLD, 13));
		g.setColor(Color.DARKBLUE);
		g.drawText("Email Header Display Options", 0, 1);

		add(headerOptionsBitmapField);

		headerBold = new CheckboxField("Use Bold Font", GeneralStoreInterface
				.isHeaderBoldFont());
		add(headerBold);

		headerFontSizeOptions = new ObjectChoiceField("Font Size: ",
				new Object[] { "Small", "Medium", "Large" },
				GeneralStoreInterface.getHeaderFontSize());
		add(headerFontSizeOptions);

		add(new SeparatorField());

		emails = new ObjectChoiceField("For Email: ",
				getServiceEmailAddresses());
		emails.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				emailColorPicker.setBgColor(getColorPref((String) emails
						.getChoice(emails.getSelectedIndex())));
				emails.setDirty(false); // Don't trigger a save prompt
			}
		});
		add(emails);

		LeftRightHorizontalFieldManager emailManager = new LeftRightHorizontalFieldManager();
		emailManager
				.add(new LabelField("Background Color: ", Field.FIELD_LEFT));

		emailColorPicker = new ColorPickerField(true, getFont(), Color.BLACK,
				getColorPref((String) emails.getChoice(emails
						.getSelectedIndex())), null, this, 101);

		emailManager.add(emailColorPicker);
		add(emailManager);
		// *********END HEADER DISPLAY OPTIONS*********

		// *********EMAIL TEXT OPTIONS*********
		emailTextOptionsBitmap = UiUtilities.getMoreOffBar();
		emailTextOptionsBitmapField = new BitmapField(emailTextOptionsBitmap);

		g = new Graphics(emailTextOptionsBitmap);
		g.setFont(getFont().derive(Font.BOLD, 13));
		g.setColor(Color.DARKBLUE);
		g.drawText("Email Text Display Options", 0, 1);

		add(emailTextOptionsBitmapField);

		fontSizeOptions = new ObjectChoiceField("Font Size: ", new Object[] {
				"1", "2", "3", "4", "5", "6", "7" }, prefs.getFontSize());

		// On changing the font size, update the text preview pane
		fontSizeOptions.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (context == 2) { // Change accepted
					synchronized (UiApplication.getEventLock()) {
						displayFontPreviewPane();
					}
				}
			}
		});

		add(fontSizeOptions);

		fontFaceOptions = new ObjectChoiceField("Font Face: ", new Object[] {
				"Arial", "Arial (Bold)", "Century Gothic",
				"Century Gothic (Bold)", "Comic Sans MS",
				"Comic Sans MS (Bold)", "Courier New", "Courier New (Bold)",
				"Helvetica", "Helvetica (Bold)", "Times New Roman",
				"Times New Roman (Bold)", "Verdana", "Verdana (Bold)" }, prefs
				.getFontStyle());

		// On changing the font face, update the text preview pane
		fontFaceOptions.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (context == 2) { // Change accepted
					synchronized (UiApplication.getEventLock()) {
						displayFontPreviewPane();
					}
				}
			}
		});

		add(fontFaceOptions);

		LeftRightHorizontalFieldManager fontColorPickerManager = new LeftRightHorizontalFieldManager();
		fontColorPickerManager.add(new LabelField("Font Color: ",
				Field.FIELD_LEFT));
		fontColorPickerManager.add(fontColorPicker);
		add(fontColorPickerManager);

		LeftRightHorizontalFieldManager bgColorPickerManager = new LeftRightHorizontalFieldManager();
		bgColorPickerManager.add(new LabelField("Background Color: ",
				Field.FIELD_LEFT));
		bgColorPickerManager.add(bgColorPicker);
		add(bgColorPickerManager);

		// Add in a special Null field which jumps over the preview area on
		// receiving focus - reason: preview area is html which will pull
		// the screen down awkwardly on receiving focus
		add(new NullField() {
			protected void onFocus(int direction) {
				connOptions.setFocus();
			}
		});

		add(new BitmapField(Bitmap.getBitmapResource("img/separator_320x1.jpg")));
		displayFontPreviewPane();
		add(new BitmapField(Bitmap.getBitmapResource("img/separator_320x1.jpg")));

		// *********END EMAIL TEXT OPTIONS*********

		// *********DISPLAY CONNECTION OPTIONS*********

		// Add in a special Null field which jumps over the preview area on
		// receiving focus - reason: preview area is html which will pull
		// the screen down awkwardly on receiving focus
		add(new NullField() {
			protected void onFocus(int direction) {
				bgColorPicker.setFocus();
			}
		});

		connOptionsBitmap = UiUtilities.getMoreOnBar();
		connOptionsBitmapField = new BitmapField(connOptionsBitmap);

		g = new Graphics(connOptionsBitmap);
		g.setFont(getFont().derive(Font.BOLD, 13));
		g.setColor(Color.DARKBLUE);
		g.drawText("Connection Options", 0, 1);

		add(connOptionsBitmapField);
		connOptions = new ButtonField("Connection Options",
				ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		connOptions.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field f, int context) {
				UiApplication.getUiApplication().pushScreen(new CommsScreen());
			}
		});
		add(connOptions);
		// *********END DISPLAY CONNECTION OPTIONS*********

		// *********DISPLAY CACHE OPTIONS*********
		cacheOptionsBitmap = UiUtilities.getMoreOffBar();
		cacheOptionsBitmapField = new BitmapField(cacheOptionsBitmap);

		g = new Graphics(cacheOptionsBitmap);
		g.setFont(getFont().derive(Font.BOLD, 13));
		g.setColor(Color.DARKBLUE);
		g.drawText("Image Cache Options", 0, 1);

		add(cacheOptionsBitmapField);

		String[] ccFreqChoices = { "1 Day", "2 Days", "Week", "Month", "Never" };
		ccFreqField = new ObjectChoiceField("Auto. Clear Cache Every:",
				ccFreqChoices, prefs.getCacheClearFreq());
		add(ccFreqField);

		final ImageCache ic = persistManager.getImageCache();
		if (ic.getCacheSize() > 0) {
			// Display the current size of the cache and a button to clear it
			final HorizontalFieldManager ccManager = new HorizontalFieldManager();

			ButtonField ccButton = new ButtonField("Clear",
					ButtonField.CONSUME_CLICK);
			ccButton.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					ic.clear();
					prefs.updateLastCacheClearTime();
					persistManager.savePreferences(prefs);
					delete(ccManager);
				}
			});
			ccManager.add(ccButton);

			ccManager.add(new LabelField(" (Current Size: "
					+ ic.getDisplayableCacheSize() + ")", Field.FIELD_VCENTER));

			add(ccManager);
		}

		// *********END DISPLAY CACHE OPTIONS*********

		// *********PIM OPTIONS*********
		pimOptionsBitmap = UiUtilities.getMoreOnBar();
		pimOptionsBitmapField = new BitmapField(pimOptionsBitmap);

		g = new Graphics(pimOptionsBitmap);
		g.setFont(getFont().derive(Font.BOLD, 13));
		g.setColor(Color.DARKBLUE);
		g.drawText("PIM Integration Options", 0, 1);

		add(pimOptionsBitmapField);

		autoSetTaskTitleCheckbox = new CheckboxField(
				"Add Subject as Task Name", prefs.isAutoSetTaskSubject());
		add(autoSetTaskTitleCheckbox);

		autoAddTaskNotesCheckbox = new CheckboxField(
				"Add Email Body as Task Note", prefs.isAutoAddTaskNotes());
		add(autoAddTaskNotesCheckbox);

		autoSetCalendarSubjectCheckbox = new CheckboxField(
				"Add Subject as Appointment Name", prefs
						.isAutoSetCalendarSubject());
		add(autoSetCalendarSubjectCheckbox);

		autoAddCalendarNotesCheckbox = new CheckboxField(
				"Add Email Body as Appointment Note", prefs
						.isAutoAddCalendarNotes());
		add(autoAddCalendarNotesCheckbox);
		// *********END PIM OPTIONS*********

		add(new SeparatorField());

		saveAndExitButton = new ButtonField("Save and Exit",
				Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		saveAndExitButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field f, int context) {
				onSave();
				close();
			}
		});
		add(saveAndExitButton);
	}

	private int getColorPref(String emailAddress) {
		String selectedEmail = (String) emails.getChoice(emails
				.getSelectedIndex());

		Vector cprefs = persistManager.getEmailColorPrefs();
		for (int i = 0; i < cprefs.size(); i++) {
			EmailColorPreference cpref = (EmailColorPreference) cprefs
					.elementAt(i);
			if (cpref.emailAddress.equalsIgnoreCase(selectedEmail)) {
				return cpref.color;
			}
		}

		return Color.WHITE;
	}

	private void saveEmailColorPrefs() {
		String selectedEmail = (String) emails.getChoice(emails
				.getSelectedIndex());
		int selectedColor = emailColorPicker.getBgColor();

		// If a colour preference already exists for the current email address,
		// update it
		Vector cprefs = persistManager.getEmailColorPrefs();
		for (int i = 0; i < cprefs.size(); i++) {
			EmailColorPreference cpref = (EmailColorPreference) cprefs
					.elementAt(i);
			if (cpref.emailAddress.equalsIgnoreCase(selectedEmail)) {
				// Update the color for this email account
				cpref.color = selectedColor;
				persistManager.saveEmailColorPrefs(cprefs);
				return;
			}
		}

		// Email doesn't yet have a color preference
		EmailColorPreference ecp = new EmailColorPreference(selectedEmail,
				getServiceAccountName(selectedEmail), selectedColor);
		cprefs.addElement(ecp);
		persistManager.saveEmailColorPrefs(cprefs);
	}

	private String getServiceAccountName(String emailAddress) {
		ServiceBook sb = ServiceBook.getSB();
		ServiceRecord[] srs = sb.findRecordsByCid("CMIME");

		if (srs != null) {
			for (int i = 0; i < srs.length; i++) {
				ServiceRecord sr = srs[i];
				ServiceConfiguration sc = new ServiceConfiguration(sr);
				if (sc.getEmailAddress().equalsIgnoreCase(emailAddress)) {
					return sc.getName();
				}
			}
		}

		return UiUtilities.EMPTY_STRING;
	}

	private String[] getServiceEmailAddresses() {
		ServiceBook sb = ServiceBook.getSB();
		ServiceRecord[] srs = sb.findRecordsByCid("CMIME");

		String[] emails = new String[srs.length];

		if (srs != null) {
			for (int i = 0; i < srs.length; i++) {
				ServiceRecord sr = srs[i];
				ServiceConfiguration sc = new ServiceConfiguration(sr);
				String serviceEmail = sc.getEmailAddress();
				emails[i] = serviceEmail;
			}
		}

		return emails;
	}

	private HTMLField fontPreviewContainer;

	private Field fontPreviewField;

	private void displayFontPreviewPane() {
		int insertIndex = -1;

		try {
			insertIndex = fontPreviewField.getIndex();
			delete(fontPreviewField);
		} catch (Exception e) {
			// Musn't have been added yet - do nothing
		}

		String selectedSize = (String) fontSizeOptions
				.getChoice(fontSizeOptions.getSelectedIndex());
		String selectedColor = "#"
				+ Integer.toHexString(fontColorPicker.getFontColor());
		String selectedFace = (String) fontFaceOptions
				.getChoice(fontFaceOptions.getSelectedIndex());
		String selectedBgColor = "#"
				+ Integer.toHexString(bgColorPicker.getBgColor());

		boolean isBold = selectedFace.endsWith("(Bold)");

		try {
			StringBuffer previewText = new StringBuffer();
			previewText.append("<body bgcolor=\"" + selectedBgColor + "\">"
					+ (isBold ? "<b>" : "") + "<font face=\"" + selectedFace
					+ "\" size=\"" + selectedSize + "\" color=\""
					+ selectedColor + "\">");
			previewText.append("The quick brown fox jumps over the lazy dog");

			fontPreviewContainer.setContent(previewText.toString());
			fontPreviewField = fontPreviewContainer.getHTMLField();

			if (insertIndex == -1) {
				add(fontPreviewField);
			} else {
				insert(fontPreviewField, insertIndex);
			}
		} catch (RenderingException re) {
			UiUtilities.alertDialog(re.getMessage());
		}
	}

	protected boolean keyChar(char c, final int status, int time) {
		switch (c) {
		case ' ':
			final Field focusField = this.getFieldWithFocus();

			// If one of the fields that should trigger a preview pane update
			if (focusField == fontSizeOptions || focusField == fontFaceOptions) {
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						changeObjectChoiceFieldSelection(
								(ObjectChoiceField) focusField,
								status == 34 ? -1 : 1);
						displayFontPreviewPane();
					}
				});
				return true;
			}
			return super.keyChar(c, status, time);
		}
		return super.keyChar(c, status, time);
	}

	private void changeObjectChoiceFieldSelection(ObjectChoiceField field,
			int dir) {
		int oldIndx = field.getSelectedIndex();
		int newIndx = oldIndx + dir;

		if (newIndx < 0) {
			newIndx = field.getSize() - 1;
		} else {
			newIndx = newIndx % field.getSize();
		}

		field.setSelectedIndex(newIndx);
	}

	protected boolean onSave() {
		prefs.setDisplayImagesInEmail(allowImagesCheckbox.getChecked());
		prefs.setDefaultDisplaySmartView(setDefaultViewerCheckbox.getChecked());
		prefs.setShowFullDetails(alwaysShowFullDetails.getChecked());
		prefs.setShowFullLinks(showFullLinksCheckbox.getChecked());
		prefs.setCacheClearFreq(ccFreqField.getSelectedIndex());
		prefs.setAutoSetTaskSubject(autoSetTaskTitleCheckbox.getChecked());
		prefs.setAutoAddTaskNotes(autoAddTaskNotesCheckbox.getChecked());
		prefs.setAutoSetCalendarSubject(autoSetCalendarSubjectCheckbox
				.getChecked());
		prefs
				.setAutoAddCalendarNotes(autoAddCalendarNotesCheckbox
						.getChecked());

		// Save the font preferences
		prefs.setFontSize((String) fontSizeOptions.getChoice(fontSizeOptions
				.getSelectedIndex()));
		prefs.setFontColor(fontColorPicker.getFontColor());
		prefs.setFontStyle((String) fontFaceOptions.getChoice(fontFaceOptions
				.getSelectedIndex()));
		prefs.setFontBgColor(bgColorPicker.getBgColor());

		GeneralStoreInterface.setHeaderBoldFont(headerBold.getChecked());
		GeneralStoreInterface.setHeaderFontSize((String) headerFontSizeOptions
				.getChoice(headerFontSizeOptions.getSelectedIndex()));

		persistManager.savePreferences(prefs);
		return true;
	}

	private MenuItem connOptionsMenuItem = new MenuItem("Connection Options",
			0, 0) {
		public void run() {
			UiApplication.getUiApplication().pushScreen(new CommsScreen());
		}
	};

	private MenuItem updateMenuItem = new MenuItem("Check for Updates", 0, 0) {
		public void run() {
			new AutoUpdater().checkForUpdates();
		}
	};

	private MenuItem saveAndExitMenuItem = new MenuItem("Save and Exit", 0, 0) {
		public void run() {
			onSave();
			close();
		}
	};

	protected void makeMenu(Menu menu, int instance) {
		menu.add(updateMenuItem);
		menu.add(connOptionsMenuItem);

		menu.add(new MenuItem("SmartSay Options", 0, 0) {
			public void run() {
				UiApplication.getUiApplication().pushScreen(
						new SmartSayScreen());
			}
		});

		// If trial version, display option to activate full version
		if (prefs.isTrial()) {
			menu.add(new MenuItem("Buy/Activate", 0, 0) {
				public void run() {
					UiApplication.getUiApplication().pushScreen(
							new RegisterScreen());
				}
			});
		}

		menu.add(saveAndExitMenuItem);
		menu.add(MenuItem.separator(0));
		super.makeMenu(menu, instance);
	}

	// Again compensating for the monkies at RIM who can't do something as
	// fucking simple as LEFT/RIGHT field alignment within a
	// HorizontalFieldManager
	class LeftRightHorizontalFieldManager extends Manager {
		private final int FIELD_HEIGHT = new LabelField().getPreferredHeight();

		public LeftRightHorizontalFieldManager() {
			super(Manager.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
		}

		public int getPreferredWidth() {
			return Graphics.getScreenWidth();
		}

		public int getPreferredHeight() {
			return FIELD_HEIGHT;
		}

		protected void sublayout(int width, int height) {
			Field first = getField(0);
			setPositionChild(first, 0, 0);
			layoutChild(first, width, height);

			Field second = getField(1);

			int x = Graphics.getScreenWidth() - second.getPreferredWidth();
			int y = 0;
			setPositionChild(second, x, y);
			layoutChild(second, width, height);

			setExtent(width, FIELD_HEIGHT);
		}
	}
}

class AutoUpdater implements HttpContentReceiver {
	private String updateURL;

	private GenericLoadingDialog loadingDialog;

	private HttpConnectionThread connection;

	public void checkForUpdates() {
		updateURL = "http://www.blackberrysmart.com/trial/ota/";
		if (BuildVersion.OS_VERSION.equals("4.3.0")) {
			updateURL += "4.3";
		} else if (BuildVersion.OS_VERSION.equals("4.2.0")) {
			updateURL += "4.2";
		} else if (BuildVersion.OS_VERSION.equals("4.1.0")) {
			updateURL += "4.1";
		} else {
			updateURL += "4.0";
		}
		updateURL += "/SmartView.jad";

		connection = new HttpConnectionThread(updateURL, this, false);

		loadingDialog = new GenericLoadingDialog("Checking...");
		loadingDialog.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int choice) {
				if (connection != null) {
					connection.cancel();
				}
			}
		});
		UiApplication.getUiApplication().pushScreen(loadingDialog);
		connection.start();
	}

	public void receivedHttpContent(final HttpConnectionThread thread,
			final String content) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				loadingDialog.close();
				int versionIndx = content.indexOf("MIDlet-Version: ");
				int numStartIndx = versionIndx + "MIDlet-Version: ".length();
				String newVersionString = content.substring(numStartIndx,
						numStartIndx + 3);

				float newVersion = Float.parseFloat(newVersionString);
				float oldVersion = Float.parseFloat(SmartView.VERSION_STRING);

				if (newVersion > oldVersion) {
					Dialog d = new Dialog(
							Dialog.D_YES_NO,
							"New version found!\n\nWould you like to install this now?",
							Dialog.YES, Bitmap
									.getPredefinedBitmap(Bitmap.QUESTION), 0,
							false);
					d.doModal();

					if (d.getSelectedValue() == Dialog.YES) {
						Browser.getDefaultSession().displayPage(updateURL);
					}
				} else {
					Dialog.inform("You are running the latest version!");
				}
			}
		});
	}

	public void receivedHttpError(final HttpConnectionThread thread,
			String error) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				loadingDialog.close();
				Dialog
						.alert("The update site could not be contacted.\n\nPlease ensure you have setup your connection settings correctly, and then try again.");
			}
		});
	}
}