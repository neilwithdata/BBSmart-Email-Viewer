package com.bbsmart.pda.blackberry.smartview.ui.screens;

import com.bbsmart.pda.blackberry.smartview.ui.customfield.HTMLField;
import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;

import net.rim.device.api.browser.field.RenderingException;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.container.MainScreen;

public final class CommsHelpScreen extends MainScreen {
	public CommsHelpScreen() {
		// Write the heading...
		Bitmap headingBitmap = UiUtilities.getHeading();
		writeHeading(new Graphics(headingBitmap));
		add(new BitmapField(headingBitmap));
		
		add(new NullField()); // for focus

		HTMLField htmlField = new HTMLField();
		htmlField.setContent(getHelpScreenContent());

		try {
			add(htmlField.getHTMLField());
		} catch (RenderingException re) {
			// Never happens
		}

		ButtonField closeField = new ButtonField("Setup My Connection!",
				ButtonField.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
		closeField.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				close();
			}
		});

		add(closeField);
	}
	
	private void writeHeading(Graphics g) {
		g.setColor(Color.FIREBRICK);

		if (UiUtilities.DEVICE_240W) {
			g.setFont(getFont().derive(Font.BOLD, 16));
			g.drawText("CONNECTION HELP", 90, 8);
		} else {
			g.setFont(getFont().derive(Font.BOLD, 18));
			g.drawText("CONNECTION HELP", 120, 13);
		}
	}
	
	public String getHelpScreenContent() {
		StringBuffer content = new StringBuffer();

		content.append("<img src=http://localhost/line.jpg>");
		content.append("In order to retrieve and display images in your email, <b>BBSmart Email Viewer</b> ");
		content.append("needs to establish a connection to the Internet. For this to happen, you first need to configure");
		content.append(" your connection settings.<br><br>");
		
		content.append("Please note that if you do not wish to view images in your emails, this functionality can be disabled from the Options screen.<br><br>");
		content.append("Also note that if you are unable to configure your connection settings, you will still be able to view emails using <b>BBSmart Email Viewer</b>");
		content.append(" however will not be able to see any images in the emails.<br><br>");
		
		content.append("On the Connection Setup screen, please select how you think you connect to the Internet and test your connection. ");
		content.append("If you are having trouble getting your connection settings to work, please first try to contact your ");
		content.append(" Wireless Network Provider or BlackBerry Network Administrator who may be able to provide assistance. ");
		content.append("<br><br><b>For any support queries, please email us at: support@blackberrysmart.com</b><img src=http://localhost/line.jpg><br><br>");

		return content.toString();
	}
}
