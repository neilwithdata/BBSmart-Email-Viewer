package com.bbsmart.pda.blackberry.smartview.ui.customfield;

import java.util.Vector;

import com.bbsmart.pda.blackberry.smartview.io.PersistenceManager;
import com.bbsmart.pda.blackberry.smartview.io.SmartNote;
import com.bbsmart.pda.blackberry.smartview.io.SmartNoteManager;
import com.bbsmart.pda.blackberry.smartview.ui.screens.EmailViewScreen;
import com.bbsmart.pda.blackberry.smartview.ui.screens.SmartNoteScreen;

import net.rim.blackberry.api.mail.Message;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

public final class SmartNoteListField extends ListField implements
		ListFieldCallback {
	private Vector smartNotes;
	private Bitmap lightBulbBitmap;
	private SmartNoteManager snm;
	private Message m;

	public SmartNoteListField(SmartNoteManager snm, Message m) {
		this.snm = snm;
		this.m = m;
		lightBulbBitmap = Bitmap.getBitmapResource("img/smartnote-on-16.png");
		update();
	}

	public Object get(ListField listField, int index) {
		return smartNotes.elementAt(index);
	}

	public int getPreferredWidth(ListField listField) {
		return Graphics.getScreenWidth();
	}

	public int indexOfList(ListField arg0, String arg1, int arg2) {
		return -1; // Not implemented
	}

	public void drawListRow(ListField listField, Graphics graphics, int index,
			int y, int width) {
		SmartNote sn = (SmartNote) smartNotes.elementAt(index);

		graphics.drawBitmap(0, y, lightBulbBitmap.getWidth(), lightBulbBitmap
				.getHeight(), lightBulbBitmap, 0, 0);

		int textColor;
		if (sn.priority.equals(SmartNote.HIGH_P)) {
			textColor = Color.RED;
		} else if (sn.priority.equals(SmartNote.LOW_P)) {
			textColor = Color.LIGHTBLUE;
		} else {
			textColor = Color.GREEN;
		}
		graphics.setColor(textColor);
		graphics.setFont(graphics.getFont().derive(Font.BOLD));

		graphics.drawText(sn.text, lightBulbBitmap.getWidth(), y,
				Graphics.ELLIPSIS);
	}

	public void update() {
		this.smartNotes = snm.getSmartNotes(m);
		setSize(smartNotes.size());
	}

	protected void makeContextMenu(ContextMenu contextMenu) {
		contextMenu.clear();

		contextMenu.addItem(new MenuItem("View/Edit SmartNote", 0, 0) {
			public void run() {
				viewSmartNote();
			}
		});

		contextMenu.addItem(new MenuItem("Delete SmartNote", 0, 0) {
			public void run() {
				deleteSmartNote();
			}
		});
	}

	private void viewSmartNote() {
		SmartNote selected = (SmartNote) get(this, getSelectedIndex());
		UiApplication.getUiApplication().pushScreen(
				new SmartNoteScreen(selected, m));
	}

	private void deleteSmartNote() {
		SmartNote selected = (SmartNote) get(this, getSelectedIndex());
		EmailViewScreen emailViewScreen = (EmailViewScreen) UiApplication
				.getUiApplication().getActiveScreen();

		Dialog d = new Dialog(Dialog.D_YES_NO, "Delete selected SmartNote?",
				Dialog.YES, Bitmap.getPredefinedBitmap(Bitmap.QUESTION), 0,
				false);
		d.doModal();

		if (d.getSelectedValue() == Dialog.YES) {
			snm.deleteSmartNote(m, selected);
			PersistenceManager.getInstance().saveSmartNoteManager(snm);
			emailViewScreen.displaySmartNotes();
		}
	}

	public boolean keyChar(char key, int status, int time) {
		switch (key) {
		case 8: // DEL key
			deleteSmartNote();
			return true;
		case Characters.ENTER:
			viewSmartNote();
			return true;
		default:
			return super.keyChar(key, status, time);
		}
	}
}