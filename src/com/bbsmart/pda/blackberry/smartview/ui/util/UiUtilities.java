package com.bbsmart.pda.blackberry.smartview.ui.util;

import java.io.InputStream;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

public final class UiUtilities {
	public static final String EMPTY_STRING = "";

	public static final String DEVICE_NAME = DeviceInfo.getDeviceName();

	public static final boolean DEVICE_240W = (DEVICE_NAME.startsWith("7") || DEVICE_NAME
			.startsWith("81"));

	public static final boolean DEVICE_SURETYPE = (DEVICE_NAME.startsWith("71") || DEVICE_NAME
			.startsWith("81"));

	public static final String HEADING_ICON_320 = "heading_320x34.jpg";

	public static final String HEADING_ICON_240 = "heading_240x26.jpg";

	public static final String INFOBAR_320x13 = "infobar_320x13.jpg";

	public static final String INFOBAR_240x13 = "infobar_240x13.jpg";

	public static final String INFOBAR_BOTTOM_320x4 = "infobar_bottom_320x4.jpg";

	public static final String MORE_ON_320x14 = "more_on_320x14.jpg";

	public static final String MORE_ON_240x14 = "more_on_240x14.jpg";

	public static final String MORE_OFF_320x14 = "more_off_320x14.jpg";

	public static final String MORE_OFF_240x14 = "more_off_240x14.jpg";

	public static final String LOADING_BAR_ERR_320x32 = "loadingbar_err_320x32.png";

	public static final String LOADING_BAR_ERR_240x24 = "loadingbar_err_240x24.png";

	public static final String LOADING_BAR_ERR_ICON_15x15 = "img_err_15x15.png";

	public static final String LOADING_BAR_ERR_ICON_11x11 = "img_err_11x11.png";

	public static final String LOADING_BAR_320x16 = "loadingbar_320x16.jpg";

	public static final String LOADING_BAR_DONE_320x16 = "loadingbar_done_320x16.jpg";

	public static void informDialog(final String message) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Dialog.inform(message);
			}
		});
	}

	public static void alertDialog(final String message) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Dialog.alert(message);
			}
		});
	}

	public static void setFocus(final Field field) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				field.setFocus();
			}
		});
	}

	public static Bitmap getMoreOnBar() {
		return getImage(DEVICE_240W ? MORE_ON_240x14 : MORE_ON_320x14);
	}

	public static Bitmap getMoreOffBar() {
		return getImage(DEVICE_240W ? MORE_OFF_240x14 : MORE_OFF_320x14);
	}

	public static Bitmap getInfoBar() {
		return getImage(DEVICE_240W ? INFOBAR_240x13 : INFOBAR_320x13);
	}

	public static Bitmap getInfoBarBottom() {
		return getImage(INFOBAR_BOTTOM_320x4);
	}

	public static Bitmap getLoadingBar() {
		return getImage(LOADING_BAR_320x16);
	}

	public static Bitmap getDoneLoadingBar() {
		return getImage(LOADING_BAR_DONE_320x16);
	}

	public static Bitmap getErrLoadingBar() {
		return getImage(DEVICE_240W ? LOADING_BAR_ERR_240x24
				: LOADING_BAR_ERR_320x32);
	}

	public static Bitmap getErrLoadingIcon() {
		return getImage(DEVICE_240W ? LOADING_BAR_ERR_ICON_11x11
				: LOADING_BAR_ERR_ICON_15x15);
	}

	public static Bitmap getSmallHeading() {
		return getImage(DEVICE_240W ? HEADING_ICON_240 : HEADING_ICON_320, 2);
	}

	public static Bitmap getHeading() {
		return getImage(DEVICE_240W ? HEADING_ICON_240 : HEADING_ICON_320);
	}

	public static Bitmap getImage(String imageName) {
		return getImage(imageName, 1);
	}

	public static Bitmap getImage(String imageName, int scaleFactor) {
		InputStream input;
		try {
			input = Class.forName(
					"com.bbsmart.pda.blackberry.smartview.SmartView")
					.getResourceAsStream("/img/" + imageName);
			byte[] data = new byte[input.available()];
			input.read(data);

			EncodedImage image = EncodedImage.createEncodedImage(data, 0,
					data.length);
			image.setScale(scaleFactor);
			return image.getBitmap();
		} catch (Exception e) {
			return null;
		}
	}

	public static byte[] getImageBytes(String imageName) {
		InputStream input;
		try {
			input = Class.forName(
					"com.bbsmart.pda.blackberry.smartview.SmartView")
					.getResourceAsStream("/img/" + imageName);
			byte[] data = new byte[input.available()];
			input.read(data);
			return data;
		} catch (Exception e) {
			return null;
		}
	}
}
