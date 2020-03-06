package com.bbsmart.pda.blackberry.smartview.ui.screens;

import com.bbsmart.pda.blackberry.smartview.io.GeneralStoreInterface;
import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;
import com.bbsmart.pda.blackberry.smartview.util.EmailTemplate;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ActiveAutoTextEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

public class SmartSayScreen extends MainScreen {
	// The default email composer edit field (when in insert mode)
	private ActiveAutoTextEditField editField;
	private ButtonField createNewButton;
	private ObjectListField templatesList;

	public SmartSayScreen() {
		// In Configure Mode...
		editField = null;
		init();
	}

	public SmartSayScreen(ActiveAutoTextEditField editField) {
		// In Insert Mode...
		this.editField = editField;
		init();
	}

	private void init() {
		displayHeading();
		initList();

		createNewButton = new ButtonField("Create New", Field.FIELD_HCENTER
				| ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		createNewButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				UiApplication.getUiApplication().pushModalScreen(
						new NewTemplateScreen());

				// Refresh the templates list since a new one may have been
				// created on the new template screen
				templatesList.set(GeneralStoreInterface.getEmailTemplates());
			}
		});
		add(new SeparatorField());
		add(createNewButton);
	}

	private void initList() {
		templatesList = new ObjectListField();
		templatesList.set(GeneralStoreInterface.getEmailTemplates());
		add(templatesList);
	}

	private void displayHeading() {
		Bitmap headingBitmap = UiUtilities.getHeading();
		Graphics g = new Graphics(headingBitmap);

		g.setColor(Color.FIREBRICK);
		if (UiUtilities.DEVICE_240W) {
			g.setFont(getFont().derive(Font.BOLD, 16));
			g.drawText("EMAIL TEMPLATES", 90, 8);
		} else {
			g.setFont(getFont().derive(Font.BOLD, 18));
			g.drawText("EMAIL TEMPLATES", 120, 13);
		}

		add(new BitmapField(headingBitmap));
		add(new SeparatorField());
	}

	private MenuItem deleteTemplate = new MenuItem("Delete", 0, 0) {
		public void run() {
			int selectedIndx = templatesList.getSelectedIndex();

			if (selectedIndx != -1 && getFieldWithFocus() == templatesList) {
				EmailTemplate selected = (EmailTemplate) templatesList.get(
						templatesList, templatesList.getSelectedIndex());

				if (Dialog.ask(Dialog.D_YES_NO, "Delete Template '"
						+ selected.title + "'?", Dialog.YES) == Dialog.YES) {
					GeneralStoreInterface.deleteEmailTemplate(selected.title);
					templatesList.delete(selectedIndx);
				}
			}
		}
	};

	private MenuItem viewEdit = new MenuItem("View/Edit", 0, 0) {
		public void run() {
			int selectedIndx = templatesList.getSelectedIndex();

			if (selectedIndx != -1 && getFieldWithFocus() == templatesList) {
				EmailTemplate selected = (EmailTemplate) templatesList.get(
						templatesList, templatesList.getSelectedIndex());
				UiApplication.getUiApplication().pushModalScreen(
						new NewTemplateScreen(selected));

				// Refresh the templates list since the title of the selected
				// one may have changed
				templatesList.set(GeneralStoreInterface.getEmailTemplates());
			}
		}
	};

	private MenuItem createNew = new MenuItem("Create New", 0, 0) {
		public void run() {
			UiApplication.getUiApplication().pushModalScreen(
					new NewTemplateScreen());

			// Refresh the templates list since a new one may have been
			// created on the new template screen
			templatesList.set(GeneralStoreInterface.getEmailTemplates());
		}
	};

	private MenuItem insert = new MenuItem("Insert in Email", 0, 0) {
		public void run() {
			int selectedIndx = templatesList.getSelectedIndex();

			if (selectedIndx != -1 && getFieldWithFocus() == templatesList) {
				EmailTemplate selected = (EmailTemplate) templatesList.get(
						templatesList, templatesList.getSelectedIndex());

				editField.insert(selected.text);
				close();
			}
		}
	};

	private MenuItem moveUp = new MenuItem("Move Up (U)", 0, 0) {
		public void run() {
			int selectedIndx = templatesList.getSelectedIndex();

			if (selectedIndx > 0 && getFieldWithFocus() == templatesList) {
				String selectedTitle = ((EmailTemplate) templatesList.get(
						templatesList, selectedIndx)).title;
				GeneralStoreInterface.moveTemplateUp(selectedTitle);

				// Refresh the templates list...
				templatesList.set(GeneralStoreInterface.getEmailTemplates());
				templatesList.setSelectedIndex(selectedIndx - 1);
			}
		}
	};

	private MenuItem moveDown = new MenuItem(
			UiUtilities.DEVICE_SURETYPE ? "Move Down (M)" : "Move Down (D)", 0,
			0) {
		public void run() {
			int selectedIndx = templatesList.getSelectedIndex();

			if (selectedIndx != -1
					&& selectedIndx < templatesList.getSize() - 1
					&& getFieldWithFocus() == templatesList) {
				String selectedTitle = ((EmailTemplate) templatesList.get(
						templatesList, selectedIndx)).title;
				GeneralStoreInterface.moveTemplateDown(selectedTitle);

				// Refresh the templates list...
				templatesList.set(GeneralStoreInterface.getEmailTemplates());
				templatesList.setSelectedIndex(selectedIndx + 1);
			}
		}
	};

	protected void makeMenu(Menu menu, int instance) {
		super.makeMenu(menu, instance);

		menu.add(createNew);
		if (templatesList.getSelectedIndex() != -1) {
			if (editField != null) {
				menu.add(insert);
			}
			menu.add(viewEdit);
			menu.add(deleteTemplate);
		}

		menu.add(MenuItem.separator(0));
		if (templatesList.getSelectedIndex() > 0) {
			menu.add(moveUp);
		}

		if (templatesList.getSelectedIndex() < templatesList.getSize() - 1) {
			menu.add(moveDown);
		}
		menu.add(MenuItem.separator(0));

		if (editField == null) {
			// In configure mode - default selection should be "View/Edit"
			menu.setDefault(viewEdit);
		} else {
			// In insert mode - default should be "Insert" option
			menu.setDefault(insert);
		}
	}

	protected boolean keyChar(char c, int status, int time) {
		switch (c) {
		case 8: // DEL key
			deleteTemplate.run();
			return true;
		case 'u':
			moveUp.run();
			return true;
		case 'm':
			if (UiUtilities.DEVICE_SURETYPE) {
				moveDown.run();
				return true;
			} else {
				return false;
			}
		case 'd':
			if (!UiUtilities.DEVICE_SURETYPE) {
				moveDown.run();
				return true;
			} else {
				return false;
			}
		default:
			return super.keyChar(c, status, time);
		}
	}
}