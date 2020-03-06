package com.bbsmart.pda.blackberry.smartview.ui.customfield;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.container.PopupScreen;
import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;

public final class ImageLoadingDialog extends PopupScreen {
	public ImageLoadingDialog(int totalNumImages) {
		super(new ImageLoadingManager());

		final ImageLoadingAnimation loadingAnim = new ImageLoadingAnimation();
		loadingAnim.setNumImages(totalNumImages);
		loadingAnim.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						close();
					}
				});
			}
		});

		add(new BitmapField(UiUtilities.getSmallHeading()));
		add(new BitmapField(Bitmap.getBitmapResource("img/line.png")));
		add(loadingAnim);
		add(new BitmapField(Bitmap.getBitmapResource("img/line.png")));

		ButtonField cancelButton = new ButtonField("Cancel",
				Field.FIELD_HCENTER);
		cancelButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						loadingAnim.cancel();
						close();
					}
				});
			}
		});
		add(cancelButton);
	}
}
