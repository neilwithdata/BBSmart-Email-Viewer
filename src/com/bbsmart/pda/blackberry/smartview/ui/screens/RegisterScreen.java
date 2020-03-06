package com.bbsmart.pda.blackberry.smartview.ui.screens;

import com.bbsmart.pda.blackberry.smartview.io.PersistenceManager;
import com.bbsmart.pda.blackberry.smartview.io.Preferences;
import com.bbsmart.pda.blackberry.smartview.ui.customfield.HTMLField;
import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;
import com.bbsmart.pda.blackberry.smartview.util.RPNString;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.browser.field.RenderingException;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

public final class RegisterScreen extends MainScreen {
	private String buyNowURL = "https://www.mobihand.com/mobilecart/mc1.asp?posid=16&pid=12362&did="
			+ Integer.toHexString(DeviceInfo.getDeviceId()).toUpperCase();
	
	public RegisterScreen() {
		// Write the heading...
		Bitmap headingBitmap = UiUtilities.getHeading();
		writeHeading(new Graphics(headingBitmap));
		add(new BitmapField(headingBitmap));

		add(new NullField()); // for focus

		HTMLField htmlField = new HTMLField();
		htmlField.setContent(getRegisterContent());

		try {
			add(htmlField.getHTMLField());
		} catch (RenderingException re) {
			// Never happens
		}
		
		final BasicEditField keyField = new BasicEditField("Key: ", "", 10,
				BasicEditField.FILTER_NUMERIC);
		add(keyField);
		
		HorizontalFieldManager buttonFieldManager = new HorizontalFieldManager(
				Field.FIELD_HCENTER);

		ButtonField activateButton = new ButtonField("Activate",
				ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		activateButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (keyField.getText().trim().length() == 0) {
					UiUtilities
							.alertDialog("No activation key entered. Please enter your activation key in the \"Key\" field first.");
					return;
				}

				if (activateProduct(keyField.getText())) {
					Preferences p = PersistenceManager.getInstance()
							.getPreferences();
					p.setTrial(false);
					p.setRegistered(true);
					PersistenceManager.getInstance().savePreferences(p);
					UiUtilities
							.alertDialog("Thank you for purchasing the full version of BBSmart Email Viewer!");
					close();
				} else {
					UiUtilities
							.alertDialog("Invalid Key Entered.\nIf you have purchased a valid key, please contact support@blackberrysmart.com for assistance");
				}
			}
		});
		
		ButtonField buyNowButton = new ButtonField("Buy Now!",
				ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		buyNowButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				Browser.getDefaultSession().displayPage(buyNowURL);
			}
		});
		
		buttonFieldManager.add(activateButton);
		buttonFieldManager.add(buyNowButton);
		add(buttonFieldManager);
	}
	
	public boolean activateProduct(String key) {
		String basePIN = Integer.toHexString(DeviceInfo.getDeviceId());
		String devicePINUpper = basePIN.toUpperCase();
		String devicePINLower = basePIN.toLowerCase();

		boolean success = false;
		String out = RPNString.apply("key 33 * c +", devicePINUpper);
		success = out.equals(key);

		if (success) {
			return true;
		} else {
			// Uppercase device PIN didn't work - let's try lowercase too just in case
			out = RPNString.apply("key 33 * c +", devicePINLower);
			return out.equals(key);
		}
	}
	
	public boolean onClose() {
		close();
		return true;
	}
	
	private void writeHeading(Graphics g) {
		g.setColor(Color.FIREBRICK);

		if (UiUtilities.DEVICE_240W) {
			g.setFont(getFont().derive(Font.BOLD, 16));
			g.drawText("ACTIVATION SCREEN", 90, 8);
		} else {
			g.setFont(getFont().derive(Font.BOLD, 18));
			g.drawText("ACTIVATION SCREEN", 120, 13);
		}
	}
	
	public String getRegisterContent() {
		StringBuffer content = new StringBuffer();
		
		String devicePIN = Integer.toHexString(DeviceInfo.getDeviceId())
				.toUpperCase();

		content.append("<img src=http://localhost/line.jpg>");
		content.append("<b><font face=\"Comic Sans MS\">");
		content.append("To activate the full version of this software, please enter your product activation key below and click \"Activate\".<br><br>");
		content.append("If you do not have an activation key, one can be purchased at <a href=\"http://www.handango.com/\">Handango (www.handango.com)</a> ");
		content.append("Alternatively, you can buy one online <u>right now</u> by clicking the <a href=\"" + buyNowURL + "\">Buy Now</a> button below!<br><br>");
		content.append("Your device PIN is: " + devicePIN);
		content.append("<img src=http://localhost/line.jpg>");

		return content.toString();
	}
}
