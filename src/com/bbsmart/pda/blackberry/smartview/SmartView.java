package com.bbsmart.pda.blackberry.smartview;

import com.bbsmart.pda.blackberry.smartview.io.PersistenceManager;
import com.bbsmart.pda.blackberry.smartview.io.Preferences;
import com.bbsmart.pda.blackberry.smartview.ui.customfield.ImageLoadingDialog;
import com.bbsmart.pda.blackberry.smartview.ui.screens.CommsScreen;
import com.bbsmart.pda.blackberry.smartview.ui.screens.EmailViewScreen;
import com.bbsmart.pda.blackberry.smartview.ui.screens.NewTemplateScreen;
import com.bbsmart.pda.blackberry.smartview.ui.screens.OptionsScreen;
import com.bbsmart.pda.blackberry.smartview.ui.screens.RegisterScreen;
import com.bbsmart.pda.blackberry.smartview.ui.screens.SmartSayScreen;
import com.bbsmart.pda.blackberry.smartview.ui.screens.TrialEndedScreen;
import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;
import com.bbsmart.pda.blackberry.smartview.util.EmailFormatter;

import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.event.MessageEvent;
import net.rim.blackberry.api.mail.event.ViewListener;
import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ActiveAutoTextEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.VerticalFieldManager;

public final class SmartView extends Application implements ViewListener {
	private PersistenceManager persistManager;

	private Preferences prefs;

	// Flag to indicate whether this is a trial version
	public static boolean DEFAULT_TRIAL = false;
	public static final long TRIAL_DURATION_DAYS = 4; // In days
	public static final String VERSION_STRING = "2.3";
	public static final String VERSION = "v" + VERSION_STRING;

	// Flag to indicate whether the full version has been registered
	public static boolean DEFAULT_REGISTERED = true;

	public static void main(String[] args) {
		SmartView sv = new SmartView();
		sv.enterEventDispatcher();
	}

	public SmartView() {
		// On startup, register the Message View menu items
		ApplicationMenuItemRepository repository = ApplicationMenuItemRepository
				.getInstance();

		BBSmartViewMenuItem viewMenuItem = new BBSmartViewMenuItem();
		BBSmartOptionsMenuItem optionsMenuItem = new BBSmartOptionsMenuItem();
		SmartSayMenuItem smartSayMenuItem = new SmartSayMenuItem();
		CreateTemplateMenuItem createTemplateMenuItem = new CreateTemplateMenuItem();

		// Add "BBSmart View" to all view and edit email screens
		repository
				.addMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_VIEW,
						viewMenuItem);
		repository
				.addMenuItem(ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT,
						viewMenuItem);

		// Add "BBSmart Options" to all email screens
		repository.addMenuItem(
				ApplicationMenuItemRepository.MENUITEM_MESSAGE_LIST,
				optionsMenuItem);
		repository.addMenuItem(
				ApplicationMenuItemRepository.MENUITEM_EMAIL_VIEW,
				optionsMenuItem);
		repository.addMenuItem(
				ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT,
				optionsMenuItem);

		// Add "SmartSay" menu options
		repository.addMenuItem(
				ApplicationMenuItemRepository.MENUITEM_EMAIL_EDIT,
				smartSayMenuItem);
		repository.addMenuItem(
				ApplicationMenuItemRepository.MENUITEM_EMAIL_VIEW,
				createTemplateMenuItem);

		// Also register listeners for message opened events
		Session.addViewListener(this);

		// Enable key up events for this application
		Application.getApplication().enableKeyUpEvents(true);

		persistManager = PersistenceManager.getInstance();
		prefs = PersistenceManager.getInstance().getPreferences();

		if (prefs.isTrial()) {
			// Record the time the application was first run
			// Used to determine when trial version has expired
			if (prefs.isFirstTime()) {
				prefs.setFirstTimeRun(System.currentTimeMillis());
				persistManager.savePreferences(prefs);
			}
		}
	}

	public void open(MessageEvent e) {
		Message m = e.getMessage();
		if (m.getStatus() == Message.Status.TX_COMPOSING) {
			return;
		}

		if (prefs.isDontAskMeAgain()) {
			if (prefs.isDefaultDisplaySmartView()) {
				if (prefs.isFirstTime()) {
					// On first time running, pop up the Comms Setup Screen
					UiApplication.getUiApplication().pushModalScreen(
							new CommsScreen());
				}

				// Display the SmartView application
				displayEmailViewer(m);
			} else {
				// Don't do anything - user does not want to see smartview by
				// default
			}
		} else {
			Dialog d = new Dialog(Dialog.D_YES_NO,
					"View with BBSmart Email Viewer?", Dialog.NO, Bitmap
							.getPredefinedBitmap(Bitmap.QUESTION), 0, true);
			d.doModal();

			prefs.setDontAskMeAgain(d.getDontAskAgainValue());
			prefs
					.setDefaultDisplaySmartView(d.getSelectedValue() == Dialog.YES);
			persistManager.savePreferences(prefs);

			if (d.getSelectedValue() == Dialog.YES) {
				if (prefs.isFirstTime()) {
					// On first time running, pop up the Comms Setup Screen
					UiApplication.getUiApplication().pushModalScreen(
							new CommsScreen());
				}

				// Display the SmartView application
				displayEmailViewer(m);
			} else {
				// Don't do anything - user does not want to see smartview by
				// default
			}
		}
	}

	public void close(MessageEvent e) {
		// Do nothing on email close (don't care)
	}

	private void displayEmailViewer(final Message m) {
		if (hasExpired()) {
			UiApplication.getUiApplication().pushScreen(new TrialEndedScreen());
		} else {
			if (!prefs.isTrial() && !prefs.isRegistered()) {
				// Recently purchased full copy
				UiApplication.getUiApplication().pushScreen(
						new RegisterScreen());
			} else {
				if (prefs.isDisplayImagesInEmail()
						&& !prefs.isLastConnSuccessful()) {
					prefs.setDisplayImagesInEmail(false);
					persistManager.savePreferences(prefs);

					Dialog d = new Dialog(
							Dialog.D_OK,
							"BBSmart Email Viewer has detected that your "
									+ "connection settings may not be correct and has disabled images from displaying.\n\n"
									+ "To configure your connection settings, please go to the Connection Setup screen ("
									+ "available from the BBSmart Options screen) and test your connection.\n\n"
									+ "Images can be re-enabled from the Options Screen.",
							Dialog.OK, Bitmap
									.getPredefinedBitmap(Bitmap.EXCLAMATION), 0);
					d.doModal();
				}

				// format the email prior to launching the new display
				EmailFormatter formatter = new EmailFormatter(m);
				formatter.formatEmail();
				UiApplication.getUiApplication().pushScreen(
						new EmailViewScreen(m, formatter));

				if (formatter.getNumImagesInEmail() > 0) {
					UiApplication.getUiApplication().pushScreen(
							new ImageLoadingDialog(formatter
									.getNumImagesInEmail()));
				}
			}
		}
	}

	public boolean hasExpired() {
		if (prefs.isTrial()) {
			final long MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

			long currentTime = System.currentTimeMillis();
			long startTime = prefs.getFirstTimeRun();

			if (currentTime < startTime) { // Catch out funny business...
				return true;
			}

			if (currentTime - startTime >= MILLIS_IN_DAY * TRIAL_DURATION_DAYS) {
				// Trial period has expired
				return true;
			}
		}

		return false;
	}

	class BBSmartViewMenuItem extends ApplicationMenuItem {
		public BBSmartViewMenuItem() {
			super(0);
		}

		public Object run(Object context) {
			if (context == null || !(context instanceof Message)) {
				UiUtilities
						.alertDialog("No email selected. Please select an email first");
				return null;
			} else {
				if (PersistenceManager.getInstance().getPreferences()
						.isFirstTime()) {
					// On first time running, pop up the Comms Setup Screen
					UiApplication.getUiApplication().pushModalScreen(
							new CommsScreen());
				}

				Message message = (Message) context;
				displayEmailViewer(message);
				return message;
			}
		}

		public String toString() {
			return "BBSmart View";
		}
	}

	class BBSmartOptionsMenuItem extends ApplicationMenuItem {
		public BBSmartOptionsMenuItem() {
			super(0);
		}

		public Object run(Object context) {
			UiApplication.getUiApplication().pushScreen(new OptionsScreen());
			return context;
		}

		public String toString() {
			return "BBSmart Options";
		}
	}

	class SmartSayMenuItem extends ApplicationMenuItem {
		public SmartSayMenuItem() {
			super(0);
		}

		public Object run(Object context) {
			if (hasExpired()) {
				UiApplication.getUiApplication().pushScreen(
						new TrialEndedScreen());
			} else {
				Screen s = UiApplication.getUiApplication().getActiveScreen();

				// Due to layout changes in OS 4.3, need to grab the edit
				// field to insert template text in a different way
				if (BuildVersion.OS_VERSION.equals("4.3.0")) {
					Field f = s.getLeafFieldWithFocus();

					if (f instanceof ActiveAutoTextEditField) {
						UiApplication.getUiApplication()
								.pushScreen(
										new SmartSayScreen(
												(ActiveAutoTextEditField) f));
					} else {
						// Did not have the text entry field highlighted
						UiUtilities
								.alertDialog("Please position your cursor where you would like the template inserted first");
					}
				} else {
					VerticalFieldManager vfm = (VerticalFieldManager) s
							.getDelegate().getField(0);

					ActiveAutoTextEditField editField;
					try {
						editField = (ActiveAutoTextEditField) vfm.getField(vfm
								.getFieldCount() - 1);
					} catch (Exception e) {
						editField = (ActiveAutoTextEditField) vfm.getField(vfm
								.getFieldCount() - 2);
					}

					UiApplication.getUiApplication().pushScreen(
							new SmartSayScreen(editField));
				}
			}
			return context;
		}

		public String toString() {
			return "SmartSay...";
		}
	}

	class CreateTemplateMenuItem extends ApplicationMenuItem {
		public CreateTemplateMenuItem() {
			super(0);
		}

		public Object run(Object context) {
			if (hasExpired()) {
				UiApplication.getUiApplication().pushScreen(
						new TrialEndedScreen());
			} else {
				if (context != null && context instanceof Message) {
					Message m = (Message) context;

					UiApplication.getUiApplication().pushScreen(
							new NewTemplateScreen(m.getBodyText()));
				}
			}

			return context;
		}

		public String toString() {
			return "Save as Template";
		}
	}
}