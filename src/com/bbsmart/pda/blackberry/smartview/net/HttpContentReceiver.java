package com.bbsmart.pda.blackberry.smartview.net;

/**
 * Interface to receive responses from http requests that run in another thread.
 * 
 * @author Neil Sainsbury
 */
public interface HttpContentReceiver {

	/**
	 * Content from http request was received successfully, and here it is.
	 * 
	 * @param thread
	 * @param content
	 */
	public void receivedHttpContent(HttpConnectionThread thread, String content);

	/**
	 * There was a problem loading the http content.
	 * 
	 * @param thread
	 * @param error
	 */
	public void receivedHttpError(HttpConnectionThread thread, String error);
}