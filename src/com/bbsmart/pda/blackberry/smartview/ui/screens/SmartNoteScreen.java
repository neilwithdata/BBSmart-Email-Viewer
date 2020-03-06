package com.bbsmart.pda.blackberry.smartview.ui.screens;

import com.bbsmart.pda.blackberry.smartview.io.PersistenceManager;
import com.bbsmart.pda.blackberry.smartview.io.SmartNote;
import com.bbsmart.pda.blackberry.smartview.io.SmartNoteManager;
import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;

import net.rim.blackberry.api.mail.Message;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public final class SmartNoteScreen extends PopupScreen {
	private EditField notesField;
	private ButtonField saveAndExit;
	private ObjectChoiceField priorityField;
	private SmartNote sn;
	private Message m;
	private final EmailViewScreen emailViewScreen;
	
	public SmartNoteScreen(Message m) {
		this(null, m);
	}

	public SmartNoteScreen(final SmartNote sn, final Message m) {
		super(new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL
				| VerticalFieldManager.VERTICAL_SCROLLBAR),
				PopupScreen.VERTICAL_SCROLL | PopupScreen.DEFAULT_MENU
						| PopupScreen.DEFAULT_CLOSE);

		this.sn = sn;
		this.m = m;

		emailViewScreen = (EmailViewScreen) UiApplication.getUiApplication()
				.getActiveScreen();

		notesField = new EditField(UiUtilities.EMPTY_STRING,
				(sn == null) ? UiUtilities.EMPTY_STRING : sn.text);

		if (sn == null) {
			notesField.setCursorPosition(0);
		}

		Bitmap heading = Bitmap.getBitmapResource("loadingbar_320x16.jpg");
		BitmapField headingField = new BitmapField(heading);

		// Display the lightbulb
		Graphics g = new Graphics(heading);
		Bitmap lightbulb = Bitmap.getBitmapResource("smartnote-on-16.png");
		g.drawBitmap(0, 0, lightbulb.getWidth(), lightbulb.getHeight(),
				lightbulb, 0, 0);

		Font f = g.getFont();
		f = f.derive(Font.BOLD, 14);
		g.setFont(f);

		g.drawText((sn == null) ? "Add new SmartNote..."
				: "View/Edit SmartNote...", lightbulb.getWidth(), 2);

		add(headingField);
		priorityField = new ObjectChoiceField("Priority: ", new Object[] {
				"High", "Normal", "Low" }, (sn == null) ? "Normal"
				: sn.priority);
		add(priorityField);
		add(new BitmapField(Bitmap
				.getBitmapResource("infobar_bottom_320x4.jpg")));
		add(notesField);

		add(new BitmapField(Bitmap
				.getBitmapResource("infobar_bottom_320x4.jpg")));
		add(new BitmapField(Bitmap.getBitmapResource("line.png")));
		saveAndExit = new ButtonField("Save and Exit",
				ButtonField.FIELD_HCENTER | ButtonField.CONSUME_CLICK);

		saveAndExit.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				saveAndExit();
			}
		});

		add(saveAndExit);
		
		// Initially focus on the text entry field
		notesField.setFocus();
	}

	public boolean onClose() {
		if (isDirty()) {
			Dialog d = new Dialog(Dialog.D_YES_NO, "Changes made. Save?",
					Dialog.YES, Bitmap.getPredefinedBitmap(Bitmap.QUESTION), 0,
					false);
			d.doModal();

			if (d.getSelectedValue() == Dialog.YES) {
				saveAndExit();
				return false;
			}
		}
		close();
		return true;
	}
	
	protected void makeMenu(Menu menu, int instance) {
		menu.add(new MenuItem("Help", 0, 0) {
			public void run() {
				String msg = "With SmartNotes, you can easily link notes (that only you can see) to an email.\n\n";
				msg += "When you send an email with SmartNotes, those notes aren't sent - they are only for";
				msg += " you to see. They are your personal place to store information you might want to remember -";
				msg += " for example, you might add a SmartNote that you have to follow-up on the email.\n\nSmartNotes";
				msg += " are similar to Tasks in that way, but offer tighter integration with emails.";
				
				UiUtilities.informDialog(msg);
			}
		});
		super.makeMenu(menu, instance);
	}

	private void saveAndExit() {
		if (notesField.getText().trim().length() == 0) {
			UiUtilities.alertDialog("No text entered!");
		} else {
			SmartNoteManager snm = PersistenceManager.getInstance()
					.getSmartNoteManager();

			if (sn == null) { // Creating a new SmartNote
				SmartNote n = new SmartNote();
				n.text = notesField.getText();
				n.priority = (String) priorityField.getChoice(priorityField
						.getSelectedIndex());
				snm.addSmartNote(m, n);
			} else { // Editing an existing SmartNote
				sn.text = notesField.getText();
				sn.priority = (String) priorityField.getChoice(priorityField
						.getSelectedIndex());
			}

			PersistenceManager.getInstance().saveSmartNoteManager(snm);
			emailViewScreen.displaySmartNotes();
			close();
		}
	}

	protected void paint(Graphics graphics) {
		graphics.setBackgroundColor(Color.WHITE);
		graphics.clear();
		super.paint(graphics);
	}

	protected void paintBackground(Graphics graphics) {
		graphics.setBackgroundColor(Color.WHITE);
		graphics.clear();
		super.paintBackground(graphics);
	}
}