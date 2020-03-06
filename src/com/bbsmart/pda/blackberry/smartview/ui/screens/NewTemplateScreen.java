package com.bbsmart.pda.blackberry.smartview.ui.screens;

import java.util.Vector;

import com.bbsmart.pda.blackberry.smartview.io.GeneralStoreInterface;
import com.bbsmart.pda.blackberry.smartview.io.PersistenceManager;
import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;
import com.bbsmart.pda.blackberry.smartview.util.EmailTemplate;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

public class NewTemplateScreen extends MainScreen {
	// 0 = New, 1 = View/Edit, 2 = Create from sent email
	private int mode;
	private EmailTemplate emailTemplate;
	private String text;

	private AutoTextEditField titleField;
	private AutoTextEditField textField;

	private boolean clearedTemplateText;

	public NewTemplateScreen() {
		mode = 0;
		init();
	}

	public NewTemplateScreen(EmailTemplate emailTemplate) {
		mode = 1;
		this.emailTemplate = emailTemplate;
		init();
	}

	public NewTemplateScreen(String text) {
		mode = 2;
		this.text = text;
		init();
	}

	private void init() {
		displayHeading();

		String defaultTitle = null, defaultText = null;
		switch (mode) {
		case 0:
			defaultTitle = UiUtilities.EMPTY_STRING;
			defaultText = "<Enter Template Text Here>";
			break;
		case 1:
			defaultTitle = emailTemplate.title;
			defaultText = emailTemplate.text;
			break;
		case 2:
			defaultTitle = UiUtilities.EMPTY_STRING;
			defaultText = text;
			break;
		}

		titleField = new AutoTextEditField("Title: ", defaultTitle);

		clearedTemplateText = false;
		textField = new AutoTextEditField(UiUtilities.EMPTY_STRING, defaultText) {
			protected void onFocus(int arg0) {
				if (mode == 0 && !clearedTemplateText) {
					setText(UiUtilities.EMPTY_STRING);
					clearedTemplateText = true;
				}
			}
		};

		add(titleField);
		add(new SeparatorField());
		add(textField);
	}

	private void displayHeading() {
		Bitmap penBitmap = Bitmap.getBitmapResource("img/pencil.gif");
		Bitmap headingBitmap = UiUtilities.getHeading();
		Graphics g = new Graphics(headingBitmap);

		String headingText = (mode == 1) ? "VIEW/EDIT..." : "CREATE NEW...";

		g.setColor(Color.FIREBRICK);
		if (UiUtilities.DEVICE_240W) {
			g.setFont(getFont().derive(Font.BOLD, 16));
			int len = g.drawText(headingText, 90, 8);
			g.drawBitmap(90 + len, 0, penBitmap.getWidth(), penBitmap
					.getHeight(), penBitmap, 0, 0);
		} else {
			g.setFont(getFont().derive(Font.BOLD, 18));
			int len = g.drawText(headingText, 120, 13);
			g.drawBitmap(120 + len, 6, penBitmap.getWidth(), penBitmap
					.getHeight(), penBitmap, 0, 0);
		}

		add(new BitmapField(headingBitmap));
		add(new SeparatorField());
	}

	protected void makeMenu(Menu menu, int instance) {
		super.makeMenu(menu, instance);

		menu.add(new MenuItem("Save and Exit", 0, 0) {
			public void run() {
				if (onSave()) {
					close();
				}
			}
		});
	}

	protected boolean onSave() {
		if (titleField.getText().length() == 0) {
			Dialog.alert("No Title Entered!");
			return false;
		} else if (textField.getText().length() == 0
				|| (mode == 0 && !clearedTemplateText)) {
			Dialog.alert("No Template Text Entered!");
			return false;
		} else {
			if (mode == 1) {
				if (!GeneralStoreInterface.updateEmailTemplate(
						emailTemplate.title, titleField.getText(), textField
								.getText())) {
					Dialog.alert("A template with this title already exists!");
					return false;
				}
			} else {
				// Creating a whole new template
				if (!GeneralStoreInterface.createEmailTemplate(titleField
						.getText(), textField.getText())) {
					Dialog.alert("A template with this title already exists!");
					return false;
				}
			}

			return true;
		}
	}
}
