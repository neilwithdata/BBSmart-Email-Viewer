package com.bbsmart.pda.blackberry.smartview.ui.customfield;

import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public final class GenericLoadingDialog extends PopupScreen {
	public GenericLoadingDialog(String loadingMessage) {
		super(new VerticalFieldManager());

		final GenericLoadingAnimation loadingAnim = new GenericLoadingAnimation(
				loadingMessage);

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
						fieldChangeNotify(0);
						close();
					}
				});
			}
		});
		add(cancelButton);
	}
}
