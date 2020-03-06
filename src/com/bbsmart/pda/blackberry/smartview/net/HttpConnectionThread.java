package com.bbsmart.pda.blackberry.smartview.net;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Retrieve content via http/https, and notify the receiver of the raw data when
 * it's all received.
 * 
 * @author Neil Sainsbury
 */
public final class HttpConnectionThread extends Thread {
	// 15 second connection timeout
	public static final int TIMEOUT = 15000; // in ms

	private String url;

	private HttpContentReceiver receiver;

	private boolean cancelled = false;

	public boolean completed = false;
	
	// Flag to indicate for this connection thread to retrieve the
	// clean connection data (perform no post-processing)
	private boolean connOnly;

	private Timer timeoutHandler;

	public HttpConnectionThread(String url, HttpContentReceiver receiver,
			boolean connOnly) {
		this.url = url;
		this.receiver = receiver;
		this.connOnly = connOnly;
	}

	/**
	 * Cancel the current http request. This may not actually cancel the
	 * request, just stop the receiver from getting responses after this call.
	 */
	public synchronized void cancel() {
		this.cancelled = true;
		timeoutHandler.cancel();
	}

	/**
	 * Has this request been cancelled?
	 */
	public synchronized boolean isCancelled() {
		return cancelled;
	}

	/**
	 * The main loop to retrieve the data.
	 */
	public void run() {
		String content = null;

		// Initiate the timeout countdown
		completed = false;
		timeoutHandler = new Timer();
		timeoutHandler.schedule(new TimeoutHandler(this), TIMEOUT);

		try {
			content = HttpHelper.getUrl(url, !connOnly);
		} catch (Exception e) {
			if (receiver != null && !isCancelled()) {
				receiver.receivedHttpError(this, e.getMessage());
			}
			return;
		} finally {
			timeoutHandler.cancel();
			completed = true;
		}

		if (receiver != null && !isCancelled()) {
			receiver.receivedHttpContent(this, content);
		}
	}

	public HttpContentReceiver getReceiver() {
		return receiver;
	}

	/**
	 * The URL for the current request.
	 * 
	 * @return
	 */
	public String getUrl() {
		return url;
	}
}

class TimeoutHandler extends TimerTask {
	private HttpConnectionThread httpConnThread;

	public TimeoutHandler(HttpConnectionThread httpConnThread) {
		this.httpConnThread = httpConnThread;
	}

	public void run() {
		if (!httpConnThread.completed && !httpConnThread.isCancelled()) {
			httpConnThread.cancel();
			if (httpConnThread.getReceiver() != null) {
				httpConnThread.getReceiver().receivedHttpError(httpConnThread,
						"Timeout");
			}
		}
	}
}