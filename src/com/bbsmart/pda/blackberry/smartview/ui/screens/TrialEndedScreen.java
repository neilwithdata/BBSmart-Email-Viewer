package com.bbsmart.pda.blackberry.smartview.ui.screens;

import com.bbsmart.pda.blackberry.smartview.ui.customfield.HTMLField;
import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.browser.field.RenderingException;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

public final class TrialEndedScreen extends MainScreen {
	private String buyNowURL = "https://www.mobihand.com/mobilecart/mc1.asp?posid=16&pid=12362&did="
			+ Integer.toHexString(DeviceInfo.getDeviceId()).toUpperCase();
	
	public TrialEndedScreen() {
		// Write the heading...
		Bitmap headingBitmap = UiUtilities.getHeading();
		writeHeading(new Graphics(headingBitmap));
		add(new BitmapField(headingBitmap));

		add(new NullField()); // for focus

		HTMLField htmlField = new HTMLField();
		htmlField.setContent(getTrialEndedContent());

		try {
			add(htmlField.getHTMLField());
		} catch (RenderingException re) {
			// Never happens
		}

		HorizontalFieldManager buttonManager = new HorizontalFieldManager(
				Field.FIELD_HCENTER);

		ButtonField activateButton = new ButtonField("Activate!",
				ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		activateButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				close();
				UiApplication.getUiApplication().pushScreen(
						new RegisterScreen());
			}
		});

		ButtonField buyNowButton = new ButtonField("Buy Now!",
				ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		buyNowButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				Browser.getDefaultSession().displayPage(buyNowURL);
			}
		});

		buttonManager.add(buyNowButton);
		buttonManager.add(activateButton);
		add(buttonManager);
	}
	
	private void writeHeading(Graphics g) {
		g.setColor(Color.FIREBRICK);

		if (UiUtilities.DEVICE_240W) {
			g.setFont(getFont().derive(Font.BOLD, 16));
			g.drawText("TRIAL ENDED!", 90, 8);
		} else {
			g.setFont(getFont().derive(Font.BOLD, 18));
			g.drawText("TRIAL ENDED!", 120, 13);
		}
	}

	public String getTrialEndedContent() {
		StringBuffer content = new StringBuffer();

		content.append("<img src=http://localhost/line.jpg>");
		content.append("<b><font face=\"Comic Sans MS\">");
		content.append("Thanks for trying out the trial version of <b>BBSmart</b> Email Viewer!<br><br>");
		content.append("If you enjoyed using this product, and now dread going back to using the");
		content.append(" <u>bland</u> default email client, there is another way!<br><br>");
		content.append("Head on over to the <a href=\"http://www.handango.com/\">Handango website (www.handango.com)</a> and pick up a copy today! ");
		content.append("Alternatively, you can buy online <u>right now</u> by clicking the <a href=\"" + buyNowURL + "\">Buy Now</a> button below!<br><br>");
		content.append("Got some feedback you would like to give? Loved it? Hated it? Got a cool idea to make it better?");
		content.append(" Feel free to email us at support@blackberrysmart.com");
		content.append("<br><br>");
		content.append("Finally, be sure to visit us at <a href=\"www.blackberrysmart.com\">BBSmart (www.blackberrysmart.com)</a> every now and then");
		content.append(" to hear about any new exciting up and coming products!");
		content.append("<img src=http://localhost/line.jpg>");

		return content.toString();
	}
}