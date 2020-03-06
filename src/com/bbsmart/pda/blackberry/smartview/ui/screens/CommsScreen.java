package com.bbsmart.pda.blackberry.smartview.ui.screens;

import com.bbsmart.pda.blackberry.smartview.io.GeneralStoreInterface;
import com.bbsmart.pda.blackberry.smartview.io.PersistenceManager;
import com.bbsmart.pda.blackberry.smartview.io.Preferences;
import com.bbsmart.pda.blackberry.smartview.io.WAPGatewayProvider;
import com.bbsmart.pda.blackberry.smartview.net.HttpConnectionThread;
import com.bbsmart.pda.blackberry.smartview.net.HttpContentReceiver;
import com.bbsmart.pda.blackberry.smartview.ui.customfield.GenericLoadingDialog;
import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.ChoiceField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public final class CommsScreen extends MainScreen {
	private PersistenceManager persistManager;

	private Preferences prefs;

	private Bitmap headingBitmap;

	private BitmapField heading;

	private RadioButtonField bisRadio;

	private RadioButtonField besMDSRadio;

	private RadioButtonField tcpIPRadio;

	private RadioButtonField wapRadio;

	private Bitmap welcomeBitmap;

	private BitmapField welcomeBitmapField;

	private Bitmap wapBitmap;

	private BitmapField wapBitmapField;

	private ButtonField testConnButton;

	private ButtonField saveAndExitButton;

	// WAP Property Fields
	private VerticalFieldManager wapDetailsManager;

	private RadioButtonField wapProviderRadio;

	private NotBrokenObjectChoiceField wapProviderList;

	private RadioButtonField wapCustomRadio;

	private BasicEditField apnWAPText;

	private BasicEditField ipWAPText;

	private BasicEditField portWAPText;

	private BasicEditField userWAPText;

	private BasicEditField passWAPText;

	private int wapInsertIndx;

	public CommsScreen() {
		persistManager = PersistenceManager.getInstance();
		prefs = persistManager.getPreferences();

		// Display the heading
		headingBitmap = UiUtilities.getHeading();
		writeHeading(new Graphics(headingBitmap));
		heading = new BitmapField(headingBitmap);
		add(heading);

		if (prefs.isFirstTime()) {
			prefs.setFirstTime(false);
			persistManager.savePreferences(prefs);
			displayIntro();
		}

		displayOptions();
		registerListeners();

		// On first time visiting the comms screen, display the help screen
		if (!prefs.isDisplayedConnHelp()) {
			UiApplication.getUiApplication().pushModalScreen(
					new CommsHelpScreen());
			prefs.setDisplayedConnHelp(true);
			persistManager.savePreferences(prefs);
		}
	}

	private void displayIntro() {
		welcomeBitmap = UiUtilities.getMoreOffBar();
		welcomeBitmapField = new BitmapField(welcomeBitmap);

		Graphics g = new Graphics(welcomeBitmap);
		g.setFont(getFont().derive(Font.BOLD, 13));
		g.setColor(Color.DARKBLUE);
		g.drawText("Welcome!", 0, 1);

		add(welcomeBitmapField);

		Font introFont;
		if (UiUtilities.DEVICE_240W) {
			introFont = getFont().derive(
					Font.BOLD | Font.ITALIC | Font.ANTIALIAS_STANDARD, 13);
		} else {
			introFont = getFont().derive(
					Font.BOLD | Font.ITALIC | Font.ANTIALIAS_STANDARD, 16);
		}

		String text = "Please take a moment to configure your connection settings. If you are not sure what to choose, first try BES/MDS.";
		RichTextField introField = new RichTextField(text, new int[] { 0,
				text.length() }, new byte[] { 0 }, new Font[] { introFont },
				RichTextField.NON_FOCUSABLE | RichTextField.READONLY);
		add(introField);
	}

	private void displayOptions() {
		add(new ConnectionBitmapField(UiUtilities.getMoreOnBar()));
		int connMethod = prefs.getConnectionMethod();

		RadioButtonGroup connRadioGroup = new RadioButtonGroup();
		besMDSRadio = new RadioButtonField("BES/MDS", connRadioGroup,
				(connMethod == Preferences.CONN_METH_ENT));

		bisRadio = new RadioButtonField("BIS", connRadioGroup,
				(connMethod == 4));

		tcpIPRadio = new RadioButtonField("TCP/IP", connRadioGroup,
				(connMethod == Preferences.CONN_METH_TCP));

		wapRadio = new RadioButtonField("WAP", connRadioGroup,
				(connMethod == Preferences.CONN_METH_WAP));

		add(besMDSRadio);
		add(bisRadio);
		add(tcpIPRadio);
		add(wapRadio);

		wapInsertIndx = getFieldCount();
		if (wapRadio.isSelected()) {
			displayWAPOptions();
		}

		displayButtons();
	}

	private void displayButtons() {
		saveAndExitButton = new ButtonField("Save and Exit",
				ButtonField.FIELD_HCENTER | ButtonField.CONSUME_CLICK);

		testConnButton = new ButtonField("Test Connection",
				ButtonField.FIELD_HCENTER | ButtonField.CONSUME_CLICK);

		HorizontalFieldManager buttonManager = new HorizontalFieldManager(
				Field.FIELD_HCENTER);
		buttonManager.add(testConnButton);
		buttonManager.add(saveAndExitButton);
		add(buttonManager);
	}

	private void registerListeners() {
		FieldChangeListener radioListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (((RadioButtonField) field).isSelected()) {
					selectWAPOther();
				}
			}
		};

		besMDSRadio.setChangeListener(radioListener);
		bisRadio.setChangeListener(radioListener);
		tcpIPRadio.setChangeListener(radioListener);

		wapRadio.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (wapRadio.isSelected()) {
					displayWAPOptions();
				}
			}
		});

		saveAndExitButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (validSelections()) {
					saveSettings();
					close();
				} else {
					if (wapRadio.isSelected()) {
						UiUtilities
								.alertDialog("A WAP APN and IP must be specified");
					} else {
						UiUtilities
								.alertDialog("No BIS Service Books detected");
					}
				}
			}
		});

		testConnButton.setChangeListener(new CommunicationsTester());
	}

	private void selectWAPOther() {
		// Hide the WAP options if they are visible...
		try {
			if (wapDetailsManager != null) {
				delete(wapDetailsManager);
				prefs.setProvider(null);
			}
		} catch (IllegalArgumentException iae) {
			// Wasn't displayed...carry on
		}
	}

	private void updateWAPText(WAPGatewayProvider p) {
		apnWAPText.setText((p != null) ? p.getApn() : UiUtilities.EMPTY_STRING);
		ipWAPText.setText((p != null) ? p.getIp() : UiUtilities.EMPTY_STRING);
		portWAPText.setText((p != null) ? p.getPort()
				: UiUtilities.EMPTY_STRING);
		userWAPText.setText((p != null) ? p.getUsername()
				: UiUtilities.EMPTY_STRING);
		passWAPText.setText((p != null) ? p.getPassword()
				: UiUtilities.EMPTY_STRING);
	}

	private void displayWAPOptions() {
		wapDetailsManager = new VerticalFieldManager();

		wapBitmap = UiUtilities.getMoreOffBar();
		wapBitmapField = new BitmapField(wapBitmap);

		Graphics g = new Graphics(wapBitmap);
		g.setFont(getFont().derive(Font.BOLD, 13));
		g.setColor(Color.DARKBLUE);
		g.drawText("WAP Options", 0, 1);

		wapDetailsManager.add(wapBitmapField);

		WAPGatewayProvider provider = prefs.getProvider();

		boolean providerSelected = (provider != null && provider.getId() != WAPGatewayProvider.CUSTOM_ID);
		boolean customSelected = (provider != null && provider.getId() == WAPGatewayProvider.CUSTOM_ID);

		RadioButtonGroup wapSettRadioGroup = new RadioButtonGroup();
		wapProviderRadio = new RadioButtonField("Carrier", wapSettRadioGroup,
				providerSelected);
		initProviderList();
		wapCustomRadio = new RadioButtonField("Custom", wapSettRadioGroup,
				customSelected);

		// On changing the provider from the WAP provider list, update the
		// display text
		wapProviderList.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				ChoiceField activeField = (ChoiceField) field;

				updateWAPText((WAPGatewayProvider) activeField
						.getChoice(activeField.getSelectedIndex()));
			}
		});

		wapCustomRadio.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (wapCustomRadio.isSelected()) {
					selectWAPCustomRadio();
				}
			}
		});

		wapProviderRadio.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (wapProviderRadio.isSelected()) {
					selectWAPProviderListRadio();
				}
			}
		});

		// Indent WAP Settings
		XYEdges wapIndent = new XYEdges(0, 0, 0, 20);
		wapProviderRadio.setPadding(wapIndent);
		wapCustomRadio.setPadding(wapIndent);

		wapDetailsManager.add(wapProviderRadio);
		wapDetailsManager.add(wapProviderList);
		wapDetailsManager.add(wapCustomRadio);

		apnWAPText = new BasicEditField(UiUtilities.EMPTY_STRING,
				UiUtilities.EMPTY_STRING);
		ipWAPText = new BasicEditField(UiUtilities.EMPTY_STRING,
				UiUtilities.EMPTY_STRING);
		portWAPText = new BasicEditField(UiUtilities.EMPTY_STRING,
				UiUtilities.EMPTY_STRING);
		userWAPText = new BasicEditField(UiUtilities.EMPTY_STRING,
				UiUtilities.EMPTY_STRING);
		passWAPText = new BasicEditField(UiUtilities.EMPTY_STRING,
				UiUtilities.EMPTY_STRING);

		wapDetailsManager.add(new SeparatorField());
		addWAPField("WAP APN: ", apnWAPText, wapDetailsManager);
		addWAPField("WAP IP: ", ipWAPText, wapDetailsManager);
		addWAPField("WAP Port: ", portWAPText, wapDetailsManager);
		addWAPField("WAP Username: ", userWAPText, wapDetailsManager);
		addWAPField("WAP Password: ", passWAPText, wapDetailsManager);
		wapDetailsManager.add(new SeparatorField());

		insert(wapDetailsManager, wapInsertIndx);

		if (providerSelected) {
			wapProviderList.setEditable(true);
			clearWAPText();
			editWAPText(false);

			updateWAPText((WAPGatewayProvider) wapProviderList
					.getChoice(wapProviderList.getSelectedIndex()));
		} else if (customSelected) {
			wapProviderList.setEditable(false);
			clearWAPText();
			editWAPText(true);
			updateWAPText(prefs.getProvider());
		} else {
			// Go with the default clean slate options
			wapProviderList.setEditable(false);
			editWAPText(false);
		}
	}

	private void addWAPField(String title, BasicEditField field,
			VerticalFieldManager manager) {
		HorizontalFieldManager fieldManager = new HorizontalFieldManager();

		LabelField labelField = new LabelField(title, Field.READONLY);
		labelField.setFont(getFont().derive(Font.BOLD));
		fieldManager.add(labelField);
		fieldManager.add(field);
		manager.add(fieldManager);
	}

	private void selectWAPCustomRadio() {
		wapCustomRadio.setSelected(true);
		wapProviderList.setEditable(false);
		wapProviderList.setSelectedIndex(0);
		clearWAPText();
		editWAPText(true);
		apnWAPText.setFocus();
	}

	private void selectWAPProviderListRadio() {
		wapProviderRadio.setSelected(true);
		wapProviderList.setEditable(true);
		clearWAPText();
		editWAPText(false);

		wapProviderList.setFocus();
		ContextMenu menu = wapProviderList.getContextMenu();
		menu.getItems()[0].run();

		updateWAPText((WAPGatewayProvider) wapProviderList
				.getChoice(wapProviderList.getSelectedIndex()));
	}

	private void clearWAPText() {
		apnWAPText.setText(UiUtilities.EMPTY_STRING);
		ipWAPText.setText(UiUtilities.EMPTY_STRING);
		portWAPText.setText(UiUtilities.EMPTY_STRING);
		userWAPText.setText(UiUtilities.EMPTY_STRING);
		passWAPText.setText(UiUtilities.EMPTY_STRING);
	}

	private void editWAPText(boolean permit) {
		apnWAPText.setEditable(permit);
		ipWAPText.setEditable(permit);
		portWAPText.setEditable(permit);
		userWAPText.setEditable(permit);
		passWAPText.setEditable(permit);
	}

	private void initProviderList() {
		Vector providerList = persistManager.getProviderList();

		WAPGatewayProvider selectCarrier = new WAPGatewayProvider();
		selectCarrier.setName("Select Carrier...");

		if (!providerList.contains(selectCarrier)) {
			providerList.insertElementAt(selectCarrier, 0);
		}

		int initialIndex = providerList.indexOf(prefs.getProvider());
		if (initialIndex == -1) {
			initialIndex = 0;
		}

		Object[] providers = new Object[providerList.size()];
		providerList.copyInto(providers);

		wapProviderList = new NotBrokenObjectChoiceField("        ", providers,
				initialIndex, Field.FIELD_LEFT);
	}

	protected boolean onSave() {
		if (validSelections()) {
			saveSettings();
			return true;
		} else {
			if (wapRadio.isSelected()) {
				UiUtilities.alertDialog("A WAP APN and IP must be specified");
			} else {
				UiUtilities.alertDialog("No BIS Service Books detected");
			}
			return false;
		}
	}

	private boolean validSelections() {
		if (wapRadio.isSelected() && !wapCustomRadio.isSelected()
				&& !wapProviderRadio.isSelected()) {
			return false;
		}

		if (wapRadio.isSelected()
				&& (wapCustomRadio.isSelected() || wapProviderRadio
						.isSelected())) {
			if (apnWAPText.getText().trim().length() == 0
					|| ipWAPText.getText().trim().length() == 0) {
				return false;
			}
		}

		if (bisRadio.isSelected()) {
			if (getBISRecords().size() == 0) {
				// No BIS records on the device
				return false;
			}
		}

		return true;
	}

	private void saveSettings() {
		if (tcpIPRadio.isSelected()) { // TCP/IP
			prefs.setConnectionMethod(Preferences.CONN_METH_TCP);
			prefs.setProvider(null);
		} else if (besMDSRadio.isSelected()) { // BES/MDS
			prefs.setConnectionMethod(Preferences.CONN_METH_ENT);
			prefs.setProvider(null);
		} else if (bisRadio.isSelected()) { // BIS
			prefs.setConnectionMethod(4);

			// Store the connection UID for quick conn access later on
			Vector records = getBISRecords();
			ServiceRecord r = (ServiceRecord) records.elementAt(0);
			GeneralStoreInterface
					.setBISConnUID(";deviceside=false;connectionUID="
							+ r.getUid());

			prefs.setProvider(null);
		} else { // WAP
			prefs.setConnectionMethod(Preferences.CONN_METH_WAP);

			if (wapCustomRadio.isSelected()) {
				WAPGatewayProvider provider = new WAPGatewayProvider();
				provider.setId(WAPGatewayProvider.CUSTOM_ID);
				provider.setName(WAPGatewayProvider.CUSTOM_NAME);
				provider.setApn(apnWAPText.getText().trim());
				provider.setIp(ipWAPText.getText().trim());

				// Check optional WAP parameters
				String text = portWAPText.getText().trim();
				provider.setPort(text.length() == 0 ? null : text);

				text = userWAPText.getText().trim();
				provider.setUsername(text.length() == 0 ? null : text);

				text = passWAPText.getText().trim();
				provider.setPassword(text.length() == 0 ? null : text);

				prefs.setProvider(provider);
			} else {
				WAPGatewayProvider provider = (WAPGatewayProvider) wapProviderList
						.getChoice(wapProviderList.getSelectedIndex());

				prefs.setProvider(provider);
			}
		}

		persistManager.savePreferences(prefs);
	}

	private void writeHeading(Graphics g) {
		g.setColor(Color.FIREBRICK);

		if (UiUtilities.DEVICE_240W) {
			g.setFont(getFont().derive(Font.BOLD, 16));
			g.drawText("CONNECTION SETUP", 90, 8);
		} else {
			g.setFont(getFont().derive(Font.BOLD, 18));
			g.drawText("CONNECTION SETUP", 120, 13);
		}
	}

	private Vector getBISRecords() {
		ServiceRecord[] records = ServiceBook.getSB().getRecords();

		Vector ipppRecords = new Vector();
		for (int i = 0; i < records.length; i++) {
			if (records[i].getCid().equalsIgnoreCase("IPPP")) {
				// Found an IPPP record
				ipppRecords.addElement(records[i]);
			}
		}

		return ipppRecords;
	}

	class CommunicationsTester implements FieldChangeListener,
			HttpContentReceiver {
		public static final String TEST_URL = "http://www.blackberrysmart.com/test.html";
		private HttpConnectionThread connection;

		private GenericLoadingDialog loadingDialog;

		public void fieldChanged(Field field, int context) {
			if (validSelections()) {
				saveSettings();

				connection = new HttpConnectionThread(TEST_URL, this, false);

				loadingDialog = new GenericLoadingDialog("Testing Conn...");
				loadingDialog.setChangeListener(new FieldChangeListener() {
					public void fieldChanged(Field field, int choice) {
						if (connection != null) {
							connection.cancel();
						}
					}
				});
				UiApplication.getUiApplication().pushScreen(loadingDialog);
				connection.start();
			} else {
				if (wapRadio.isSelected()) {
					UiUtilities
							.alertDialog("A WAP APN and IP must be specified");
				} else {
					UiUtilities.alertDialog("No BIS Service Books detected");
				}
			}
		}

		public void receivedHttpContent(HttpConnectionThread thread,
				String content) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					// Remember that the last connection attempt was successful
					prefs.setLastConnSuccessful(true);
					persistManager.savePreferences(prefs);

					loadingDialog.close();
					Dialog.inform("Connection Successful!");
					close();
				}
			});
		}

		public void receivedHttpError(HttpConnectionThread thread, String error) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					// Remember that the last connection attempt was NOT
					// successful
					prefs.setLastConnSuccessful(false);
					persistManager.savePreferences(prefs);

					loadingDialog.close();
					Dialog
							.alert("Connection Failure! Please check your radio coverage and review your selections");
				}
			});
		}
	}

	private MenuItem testConnMenuItem = new MenuItem("Test Connection", 0, 0) {
		public void run() {
			// Simulate a press of the test button
			new CommunicationsTester().fieldChanged(testConnButton, 0);
		}
	};

	private MenuItem helpMenuItem = new MenuItem("Help!", 0, 0) {
		public void run() {
			UiApplication.getUiApplication().pushScreen(new CommsHelpScreen());
		}
	};

	private MenuItem saveAndExitMenuItem = new MenuItem("Save and Exit", 0, 0) {
		public void run() {
			if (validSelections()) {
				saveSettings();
				close();
			} else {
				if (wapRadio.isSelected()) {
					UiUtilities
							.alertDialog("A WAP APN and IP must be specified");
				} else {
					UiUtilities.alertDialog("No BIS Service Books detected");
				}
			}
		}
	};

	protected void makeMenu(Menu menu, int instance) {
		menu.add(helpMenuItem);
		menu.add(testConnMenuItem);
		menu.add(saveAndExitMenuItem);
		menu.add(MenuItem.separator(0));
		super.makeMenu(menu, instance);
	}

	static class NotBrokenObjectChoiceField extends ObjectChoiceField {
		public NotBrokenObjectChoiceField(String label, Object[] choices,
				int initialIndex, long style) {
			super(label, choices, initialIndex, style);
		}

		protected int getWidthOfChoice(int index) {
			return 180;
		}
	}
}

class ConnectionBitmapField extends BitmapField {
	private Timer signalStrengthTimer;

	public ConnectionBitmapField(Bitmap bitmap) {
		super(bitmap);
	}

	protected void onDisplay() {
		signalStrengthTimer = new Timer();
		signalStrengthTimer.schedule(new TimerTask() {
			public void run() {
				invalidate();
			}
		}, 1000, 1000);
	}

	protected void onUndisplay() {
		signalStrengthTimer.cancel();
	}

	protected void paint(Graphics g) {
		super.paint(g);

		g.setFont(getFont().derive(Font.BOLD, 13));
		g.setColor(Color.DARKBLUE);
		g.drawText("Connection Options", 0, 1);

		// Draw the signal strength indicator
		int screenWidth = Graphics.getScreenWidth();
		int signalLevel = RadioInfo.getSignalLevel();
		g.setColor(Color.BLUE);
		if (signalLevel > -77) {
			g.fillRect(screenWidth - 25, 9, 4, 2);
			g.fillRect(screenWidth - 20, 7, 4, 4);
			g.fillRect(screenWidth - 15, 5, 4, 6);
			g.fillRect(screenWidth - 10, 3, 4, 8);
			g.fillRect(screenWidth - 5, 1, 4, 10);
		} else if (signalLevel > -86) {
			g.fillRect(screenWidth - 25, 9, 4, 2);
			g.fillRect(screenWidth - 20, 7, 4, 4);
			g.fillRect(screenWidth - 15, 5, 4, 6);
			g.fillRect(screenWidth - 10, 3, 4, 8);
			g.drawRect(screenWidth - 5, 1, 4, 10);
		} else if (signalLevel > -92) {
			g.fillRect(screenWidth - 25, 9, 4, 2);
			g.fillRect(screenWidth - 20, 7, 4, 4);
			g.fillRect(screenWidth - 15, 5, 4, 6);
			g.drawRect(screenWidth - 10, 3, 4, 8);
			g.drawRect(screenWidth - 5, 1, 4, 10);
		} else if (signalLevel > -101) {
			g.fillRect(screenWidth - 25, 9, 4, 2);
			g.fillRect(screenWidth - 20, 7, 4, 4);
			g.drawRect(screenWidth - 15, 5, 4, 6);
			g.drawRect(screenWidth - 10, 3, 4, 8);
			g.drawRect(screenWidth - 5, 1, 4, 10);
		} else if (signalLevel > -120) {
			g.fillRect(screenWidth - 25, 9, 4, 2);
			g.drawRect(screenWidth - 20, 7, 4, 4);
			g.drawRect(screenWidth - 15, 5, 4, 6);
			g.drawRect(screenWidth - 10, 3, 4, 8);
			g.drawRect(screenWidth - 5, 1, 4, 10);
		}
	}
}