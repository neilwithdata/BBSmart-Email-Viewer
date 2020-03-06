package com.bbsmart.pda.blackberry.smartview.ui.customfield;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.VerticalFieldManager;

public final class ImageLoadingManager extends VerticalFieldManager {
	public void updateDisplay() {
		synchronized (UiApplication.getEventLock()) {
			updateLayout();
		}
	}
}
