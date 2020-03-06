package com.bbsmart.pda.blackberry.smartview.util;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.blackberry.api.mail.BodyPart;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.Transport;
import net.rim.blackberry.api.mail.event.MessageEvent;
import net.rim.blackberry.api.mail.event.MessageListener;

public final class MoreRetriever {
	public static final String MORE_ERR = "Sorry. More could not be retrieved.\n\nPlease check your connection status and try again later";

	// Used by the message listener to determine when more has been retrieved
	private int oldContentLength;

	// The listener to notify if the retrieval was successful
	private MoreListener listener;

	private BodyPart bp;

	private Message msg;

	private MessageListener moreMessageListener;

	// Flag to indicate whether we are currently retrieving more or not
	private boolean retrievingMore = false;

	// The thread to poll until new content comes in
	private Thread updatePoller;

	// The timeout timer which kicks in after a specified time
	// to halt the more retrieval process
	private Timer moreWatchdog;

	public static boolean hasMore(BodyPart bp) {
		return bp.hasMore();
	}

	public void retrieveMore(Message msg, BodyPart bp, MoreListener listener) {
		if (retrievingMore) {
			return;
		}

		this.listener = listener;
		this.msg = msg;
		this.bp = bp;

		if (bp.hasMore() && !bp.moreRequestSent()) {
			try {
				// Construct a new message listener (to notify us when new
				// content comes in)
				oldContentLength = bp.getContent().toString().length();

				moreMessageListener = new MessageMoreListener();
				msg.addMessageListener(moreMessageListener);

				Transport.more(bp, false);
				retrievingMore = true;
				initMoreTimeoutListener();
			} catch (Exception e) {
				retrievingMore = false;
				msg.removeMessageListener(moreMessageListener);
				listener.notifyFailure(bp, MORE_ERR);
			}
		} else {
			retrievingMore = false;
			listener.notifyFailure(bp, MORE_ERR);
		}
	}

	private void initMoreTimeoutListener() {
		// Timeout attempting to retrieve more after 50 seconds...
		final int MORE_TIMEOUT = 50000;

		moreWatchdog = new Timer();
		moreWatchdog.schedule(new TimerTask() {
			public void run() {
				if (updatePoller != null && updatePoller.isAlive()) {
					updatePoller.interrupt();
				}

				// Perform a cleanup
				msg.removeMessageListener(moreMessageListener);
				retrievingMore = false;
				listener.notifyFailure(bp, MORE_ERR);

			}
		}, MORE_TIMEOUT);
	}

	class MessageMoreListener implements MessageListener {

		public void changed(MessageEvent e) {
			final Message msg = e.getMessage();

			if (retrievingMore) { // Already in the process of retrieving more
				return;
			}

			retrievingMore = true;

			updatePoller = new Thread() {
				public void run() {
					// Continue sleeping until we receive more of the email
					while (msg.getBodyText().length() <= oldContentLength) {
						try {
							Thread.sleep(1500);
						} catch (InterruptedException ie) {
							// Notified to stop polling - exit
							return;
						}
					}

					// Call of the timeout watchdog - we did our job
					if (moreWatchdog != null) {
						moreWatchdog.cancel();
					}

					msg.removeMessageListener(moreMessageListener);
					retrievingMore = false;
					listener.notifySuccess(bp);
				}
			};
			updatePoller.start();
		}
	}
}