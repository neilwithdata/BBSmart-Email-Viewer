package com.bbsmart.pda.blackberry.smartview.ui.customfield;

import java.util.Timer;
import java.util.TimerTask;

import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.BitmapField;

public final class GenericLoadingAnimation extends BitmapField {
	private static final int WIDTH = Graphics.getScreenWidth() / 2;

	private static final int HEIGHT;

	static {
		if (UiUtilities.DEVICE_240W) {
			HEIGHT = 12;
		} else {
			HEIGHT = 16;
		}
	}

	private static final int UPDATE_DELAY = 50; // ms

	private static final int UPDATE_INTERVAL = 100; // ms

	public static final int PERCENT_INCREMENT = 5;

	private Timer imageUpdateTimer;

	private Bitmap loadingBitmap;

	private float percentUILen;

	private volatile int percentComplete;

	private String loadingMessage;

	public GenericLoadingAnimation(String loadingMessage) {
		loadingBitmap = UiUtilities.getLoadingBar();

		this.loadingMessage = loadingMessage;

		percentUILen = (float) WIDTH / (float) 100;
		percentComplete = 0;
	}

	public int getPreferredWidth() {
		return WIDTH;
	}

	public int getPreferredHeight() {
		return HEIGHT;
	}

	protected void onDisplay() {
		imageUpdateTimer = new Timer();
		imageUpdateTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				percentComplete += PERCENT_INCREMENT;
				if (percentComplete > 100) {
					percentComplete = 0;
				}

				invalidate();
			}
		}, UPDATE_DELAY, UPDATE_INTERVAL);
	}

	protected void paint(Graphics graphics) {
		super.paint(graphics);

		graphics.setFont(graphics.getFont().derive(Font.BOLD, HEIGHT));
		graphics.setColor(Color.BLACK);

		graphics.drawBitmap(0, 0, (int) (percentUILen * percentComplete),
				HEIGHT, loadingBitmap, 0, 0);
		graphics.drawRect(0, 0, getPreferredWidth(), HEIGHT);

		graphics.drawText(loadingMessage, 0, 0);
	}

	public void cancel() {
		imageUpdateTimer.cancel();
	}
}
