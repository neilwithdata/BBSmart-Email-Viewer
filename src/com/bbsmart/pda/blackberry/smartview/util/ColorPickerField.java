/**
 * ColorPickerField.java
 */
package com.bbsmart.pda.blackberry.smartview.util;

import java.util.Vector;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.bbsmart.pda.blackberry.smartview.util.ColorChoices.ColorChoice;
import com.bbsmart.pda.blackberry.smartview.util.ColorPickerField.ColorPickerScreen.ColorChoiceLabel;

public class ColorPickerField extends HorizontalFieldManager {
	private int fontColor = Color.BLACK; // Default

	private String choicesValue;

	private int bgColor = Color.WHITE; // Default

	private Font font;

	private Font italicFont;

	private ColorPickerInnerField innerCPF;

	private HorizontalFieldManager innerHFM;

	private FieldChangeListener changeListener;

	private Field thisField;

	private Vector choiceLabels = new Vector();

	/**
	 * Allows a change listener to identify the field firing off a change.
	 * Useful if you have the same change listener for multiple fields of this
	 * type.
	 */
	private int context;

	/**
	 * Indicates if this color picker is changing the background color, or the
	 * font color. true for background, false for font.
	 */
	private boolean backgroundPicker;

	/**
	 * ColorPickerField allows the choosing of background and font colors.
	 * 
	 * @param aBackgroundPicker
	 *            true iff this picker picks the background color, false to pick
	 *            font color
	 * @param aFont
	 *            The font to display in the dialogs.
	 * @param aFontColor
	 *            The initial font color to display.
	 * @param aBgColor
	 *            The initial background color to display.
	 * @param aChoicesValue
	 *            The value to display in the choice boxes.
	 * @param aChangeListener
	 *            The listener who cares about changes.
	 * @param aContext
	 *            The context for the field change event context argument.
	 */
	public ColorPickerField(boolean aBackgroundPicker, Font aFont,
			int aFontColor, int aBgColor, String aChoicesValue,
			FieldChangeListener aChangeListener, int aContext) {
		super();
		backgroundPicker = aBackgroundPicker;
		context = aContext;
		thisField = this;
		changeListener = aChangeListener;
		choicesValue = aChoicesValue;
		font = aFont;

		int fontMask = (font.isBold() ? Font.BOLD : 0)
				| (font.isPlain() ? Font.PLAIN : 0)
				| (font.isUnderlined() ? Font.UNDERLINED : 0) | Font.ITALIC;

		italicFont = font.derive(fontMask, font.getHeight(), Ui.UNITS_px,
				Font.ANTIALIAS_STANDARD, 0);

		bgColor = aBgColor;
		fontColor = aFontColor;

		innerHFM = new HorizontalFieldManager();
		innerHFM.setPadding(1, 1, 1, 1);
		innerCPF = new ColorPickerInnerField();
		innerHFM.add(innerCPF);
		add(innerHFM);
	}

	public int getBgColor() {
		return bgColor;
	}

	public int getFontColor() {
		return fontColor;
	}

	public void setBgColor(int color) {
		bgColor = color;
		innerCPF.updateText();
		invalidate();
	}

	public void setFontColor(int color) {
		fontColor = color;
		innerCPF.updateText();
		invalidate();
	}

	/**
	 * @see net.rim.device.api.ui.container.HorizontalFieldManager#subpaint(net.rim.device.api.ui.Graphics)
	 */
	protected void subpaint(Graphics aGraphics) {
		aGraphics.setBackgroundColor(bgColor);
		aGraphics.clear();
		super.subpaint(aGraphics);
	}

	private ColorChoiceLabel updateFocus() {
		int vecSize = choiceLabels.size();
		for (int i = 0; i < vecSize; i++) {
			ColorChoiceLabel label = (ColorChoiceLabel) choiceLabels
					.elementAt(i);
			if (backgroundPicker && (label.getColor() == bgColor)) {
				label.setFocus();
				return label;
			} else if (!backgroundPicker && (label.getColor() == fontColor)) {
				label.setFocus();
				return label;
			}
		}
		return null;
	}

	private class ColorPickerInnerField extends LabelField implements
			FocusChangeListener {
		private ColorPickerScreen colorPickerScreen;

		private ColorPickerInnerField() {
			super("", LabelField.FOCUSABLE);
			setFocusListener(this);
			setFont(font);
			updateText();
		}

		protected void updateText() {
			String colorText = ColorChoices
					.getColorText(backgroundPicker ? bgColor : fontColor);
			setText(colorText);
		}

		protected void paint(Graphics aGraphics) {
			if (isFocus()) {
				setFont(italicFont);
			} else {
				setFont(font);
			}

			aGraphics.setBackgroundColor(bgColor);
			aGraphics.clear();
			aGraphics.setColor(fontColor);
			super.paint(aGraphics);
		}

		protected boolean trackwheelClick(int status, int time) {
			if (colorPickerScreen == null) {
				VerticalFieldManager vfg = new VerticalFieldManager(
						VerticalFieldManager.VERTICAL_SCROLL
								| VerticalFieldManager.VERTICAL_SCROLLBAR);
				colorPickerScreen = new ColorPickerScreen(vfg, this);
			}

			Ui.getUiEngine().pushScreen(colorPickerScreen);

			ColorChoiceLabel focused = updateFocus();
			if (focused != null) {
				int size = choiceLabels.size();
				int index = 0;
				for (int i = 0; i < size; i++) {
					ColorChoiceLabel label = (ColorChoiceLabel) choiceLabels
							.elementAt(i);
					if (label.getColor() == focused.getColor()) {
						index = i;
						break;
					}
				}

				if (index < (size - 3) && (index > 3)) {
					ColorChoiceLabel label = (ColorChoiceLabel) choiceLabels
							.elementAt(index + 2);
					label.setFocus();
					label = (ColorChoiceLabel) choiceLabels
							.elementAt(index - 2);
					label.setFocus();
					focused.setFocus();
				}
			}
			return true;
		}

		public void focusChanged(Field aField, int aEventType) {
			switch (aEventType) {
			case FOCUS_LOST:
				invalidate();
				break;
			default:
				break;
			}
		}
	}

	protected class ColorPickerScreen extends PopupScreen {
		public ColorPickerScreen(Manager delegate, ColorPickerInnerField aField) {
			super(delegate, PopupScreen.DEFAULT_MENU
					| PopupScreen.DEFAULT_CLOSE | PopupScreen.VERTICAL_SCROLL);
			addColorRows();
		}

		private void addColorRows() {
			Object[] colorChoices = ColorChoices.getColorChoices();
			for (int i = 0; i < colorChoices.length; i++) {
				ColorChoice choice = (ColorChoice) colorChoices[i];
				addChoiceRow(choice);
			}
		}

		private void addChoiceRow(ColorChoice aChoice) {
			ColorChoiceHFM mgr = new ColorChoiceHFM(aChoice);
			String choiceName = choicesValue == null ? aChoice.getName()
					: choicesValue;
			ColorChoiceLabel choiceField = new ColorChoiceLabel(aChoice
					.getColorValue(), choiceName, LabelField.FOCUSABLE
					| LabelField.ELLIPSIS);
			choiceField.setFocusListener(mgr);
			choiceLabels.addElement(choiceField);

			HorizontalFieldManager iMgr = new HorizontalFieldManager();
			iMgr.setPadding(9, 9, 9, 9);
			iMgr.add(choiceField);
			mgr.add(iMgr);
			add(mgr);
		}

		public boolean keyChar(char key, int status, int time) {
			switch (key) {
			case Characters.ESCAPE:
				onClose();
				return true;
			}
			return super.keyChar(key, status, time);
		}

		protected class ColorChoiceLabel extends LabelField {
			private int myColor;

			public ColorChoiceLabel(int color, String text, long style) {
				super(text, style);
				myColor = color;
			}

			protected int getColor() {
				return myColor;
			}

			protected void paint(Graphics aGraphics) {
				aGraphics.setBackgroundColor(backgroundPicker ? myColor
						: bgColor);
				aGraphics.clear();
				aGraphics.setColor(backgroundPicker ? fontColor : myColor);
				super.paint(aGraphics);
			}

			protected boolean trackwheelClick(int status, int time) {
				if (backgroundPicker) {
					bgColor = myColor;
				} else {
					fontColor = myColor;
				}
				innerCPF.updateText();
				innerHFM.invalidate();
				changeListener.fieldChanged(thisField, context);
				getUiEngine().popScreen(getScreen());
				return true;
			}

			public int getPreferredWidth() {
				return ((Graphics.getScreenWidth() / 3) * 2) - 18;
			}

			protected void layout(int aWidth, int aHeight) {
				int width = ((Graphics.getScreenWidth() / 3) * 2) - 18;
				setExtent(width, aHeight);
				super.layout(width, aHeight);
			}

		}

		private class ColorChoiceHFM extends HorizontalFieldManager implements
				FocusChangeListener {
			private int myBgColor = 0;

			private Graphics graphics;

			private boolean inFocus;

			public ColorChoiceHFM(ColorChoice aColorChoice) {
				myBgColor = aColorChoice.getColorValue();
			}

			protected void subpaint(Graphics aGraphics) {
				int newBgColor = backgroundPicker ? myBgColor : bgColor;
				graphics = aGraphics;
				int color = graphics.getColor();
				aGraphics.setBackgroundColor(newBgColor);
				aGraphics.clear();
				int bwidth = getExtent().width;
				int bheight = getExtent().height;
				if (inFocus) {
					graphics.setColor(Color.RED);
					graphics.drawRect(0, 0, bwidth, bheight);
					graphics.drawRect(1, 1, bwidth - 2, bheight - 2);
				} else {
					graphics.setColor(Color.WHITE);
					graphics.drawRect(0, 0, bwidth, bheight);
					graphics.drawRect(1, 1, bwidth - 2, bheight - 2);
					graphics.drawRect(2, 2, bwidth - 4, bheight - 4);
					graphics.drawRect(3, 3, bwidth - 6, bheight - 6);
					graphics.drawRect(4, 4, bwidth - 8, bheight - 8);
					graphics.drawRect(5, 5, bwidth - 10, bheight - 10);
				}

				graphics.setColor(color);
				super.subpaint(aGraphics);
			}

			public void focusChanged(Field aField, int aEventType) {
				switch (aEventType) {
				case FOCUS_GAINED:
					inFocus = true;
					break;
				case FOCUS_LOST:
					inFocus = false;
					break;
				default:
					break;
				}
				invalidate();
			}
		}
	}
}