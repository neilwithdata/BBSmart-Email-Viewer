package com.bbsmart.pda.blackberry.smartview.ui.customfield;

import java.util.Timer;
import java.util.TimerTask;

import com.bbsmart.pda.blackberry.smartview.net.ImageConnectionManager;
import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.BitmapField;

public final class ImageLoadingAnimation extends BitmapField {
	private static final int WIDTH = Graphics.getScreenWidth() / 2;

	private static final int HEIGHT;

	static {
		if (UiUtilities.DEVICE_240W) {
			HEIGHT = 12;
		} else {
			HEIGHT = 16;
		}
	}

	private static final int POLL_INTERVAL = 300; // ms

	private volatile int numCompleteImages;

	private volatile int numErroredImages;

	private int totalNumImages;

	private float imageUiLen;

	private Timer imageCompletionPoller;

	private ImageConnectionManager iManager;

	private Bitmap loadingBitmap;

	private Bitmap completedBitmap;

	private Bitmap errBitmap;

	private Bitmap errIcon;

	private boolean finished;

	public ImageLoadingAnimation() {
		iManager = ImageConnectionManager.getInstance();
		loadingBitmap = UiUtilities.getLoadingBar();
		completedBitmap = UiUtilities.getDoneLoadingBar();
		errBitmap = UiUtilities.getErrLoadingBar();
		errIcon = UiUtilities.getErrLoadingIcon();
		finished = false;
		totalNumImages = 0;
		numCompleteImages = 0;
		numErroredImages = 0;
	}

	public int getPreferredWidth() {
		return WIDTH;
	}

	public int getPreferredHeight() {
		if (numErroredImages > 0) {
			return HEIGHT * 2;
		} else {
			return HEIGHT;
		}
	}

	public void setNumImages(int totalNumImages) {
		this.totalNumImages = totalNumImages;
		imageUiLen = (float) WIDTH / (float) totalNumImages;
	}

	protected void onDisplay() {
		imageCompletionPoller = new Timer();
		imageCompletionPoller.scheduleAtFixedRate(new UpdateTask(this),
				POLL_INTERVAL, POLL_INTERVAL);
	}

	protected void onUndisplay() {
		imageCompletionPoller.cancel();
		finished = true;
	}

	public void setTotalNumImages(int totalNumImages) {
		this.totalNumImages = totalNumImages;
		finished = false;
		imageUiLen = (float) WIDTH / (float) totalNumImages;
	}

	protected void paint(Graphics graphics) {
		super.paint(graphics);

		int done = numCompleteImages + numErroredImages;

		if (!finished) {
			graphics.setFont(graphics.getFont().derive(Font.BOLD, HEIGHT));
			graphics.setColor(Color.BLACK);

			if (numErroredImages > 0) {
				graphics.drawBitmap(0, 0, (int) (imageUiLen * done),
						HEIGHT * 2, errBitmap, 0, 0);
				graphics.drawBitmap(0, HEIGHT, errIcon.getWidth(), errIcon
						.getHeight(), errIcon, 0, 0);
				graphics.drawText(String.valueOf(numErroredImages)
						+ " image(s)", errIcon.getWidth(), HEIGHT);
			} else {
				graphics.drawBitmap(0, 0, (int) (imageUiLen * done), HEIGHT,
						loadingBitmap, 0, 0);
			}

			graphics.drawRect(0, 0, getPreferredWidth(), HEIGHT);

			graphics.drawText(" Images: (" + done + "/" + totalNumImages + ")",
					0, 0);
		} else {
			graphics.drawBitmap(0, 0, WIDTH, HEIGHT, completedBitmap, 0, 0);

			graphics.setFont(graphics.getFont().derive(Font.BOLD, HEIGHT));
			graphics.setColor(Color.BLACK);
			graphics.drawRect(0, 0, getPreferredWidth(), getPreferredHeight());

			graphics.drawText(" Images: (Done)", 0, 0);
		}
	}

	public void updateDisplay() {
		if ((numCompleteImages + numErroredImages) >= totalNumImages) {
			// Finished loading all images
			finished = true;
			iManager.cancel();
			imageCompletionPoller.cancel();
			fieldChangeNotify(0);
		}

		if (numErroredImages > 0) {
			setExtent(getPreferredWidth(), getPreferredHeight());
			((ImageLoadingManager) this.getManager()).updateDisplay();
		}

		invalidate();
	}

	public void cancel() {
		iManager.cancel();
		imageCompletionPoller.cancel();
	}

	class UpdateTask extends TimerTask {
		ImageLoadingAnimation bitmapField;

		public UpdateTask(ImageLoadingAnimation bitmapField) {
			this.bitmapField = bitmapField;
		}

		public void run() {
			int newNumCompleted = iManager.getNumCompletedRequests();
			int newNumErrored = iManager.getNumErroredRequests();

			if (newNumCompleted + newNumErrored > (numCompleteImages + numErroredImages)) {
				numCompleteImages = newNumCompleted;
				numErroredImages = newNumErrored;

				bitmapField.updateDisplay();
			}
		}
	}
}
