package com.bbsmart.pda.blackberry.smartview.ui.customfield;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.HttpConnection;

import com.bbsmart.pda.blackberry.smartview.net.ImageConnectionManager;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.browser.field.BrowserContent;
import net.rim.device.api.browser.field.Event;
import net.rim.device.api.browser.field.RenderingApplication;
import net.rim.device.api.browser.field.RenderingException;
import net.rim.device.api.browser.field.RenderingSession;
import net.rim.device.api.browser.field.RequestedResource;
import net.rim.device.api.browser.field.UrlRequestedEvent;
import net.rim.device.api.ui.Field;

public final class HTMLField {
	CustomHTTPConnection httpConn;

	public void setContent(String content) {
		httpConn = new CustomHTTPConnection();
		httpConn.setContent(content);
	}

	public Field getHTMLField() throws RenderingException {
		BrowserContent bc = RenderingSession.getNewInstance()
				.getBrowserContent(httpConn, new RenderHelper(), 0);
		new RenderingThread(bc).start();
		Field htmlField = bc.getDisplayableContent();

		if (httpConn != null) {
			try {
				httpConn.close();
			} catch (Exception e) {
				// Some exception closing the connection..ignore
			}
		}

		return htmlField;
	}
}

class RenderHelper implements RenderingApplication {
	private ImageConnectionManager iManager = ImageConnectionManager
			.getInstance();

	public Object eventOccurred(Event event) {
		if (event instanceof UrlRequestedEvent) {
			UrlRequestedEvent urlEvent = (UrlRequestedEvent) event;
			Browser.getDefaultSession().displayPage(urlEvent.getURL());
		}
		return null;
	}

	public int getAvailableHeight(BrowserContent browserContent) {
		return 0;
	}

	public int getAvailableWidth(BrowserContent browserContent) {
		return 0;
	}

	public int getHistoryPosition(BrowserContent browserContent) {
		return 0;
	}

	public String getHTTPCookie(String url) {
		return null;
	}

	public HttpConnection getResource(final RequestedResource resource,
			final BrowserContent referrer) {
		if (resource.isCacheOnly()) {
			return iManager.requestCachedImage(resource);
		} else {
			iManager.requestImage(resource, referrer);
			return null;
		}
	}

	public void invokeRunnable(Runnable runnable) {
	}
}

class CustomHTTPConnection implements HttpConnection {
	private String content;

	private ByteArrayInputStream is;

	public void setContent(String content) {
		this.content = content;
	}

	public void close() throws IOException {
		if (is != null) {
			is.close();
		}
	}

	public long getDate() throws IOException {
		return 0;
	}

	public String getEncoding() {
		return null;
	}

	public long getExpiration() throws IOException {
		return 0;
	}

	public String getFile() {
		return null;
	}

	public String getHeaderField(int n) throws IOException {
		return null;
	}

	public String getHeaderField(String name) throws IOException {
		if (name.equals("content-type")) {
			return "text/html";
		} else {
			return null;
		}
	}

	public long getHeaderFieldDate(String name, long def) throws IOException {
		return 0;
	}

	public int getHeaderFieldInt(String name, int def) throws IOException {
		return 0;
	}

	public String getHeaderFieldKey(int n) throws IOException {
		return null;
	}

	public String getHost() {
		return "localhost";
	}

	public long getLastModified() throws IOException {
		return 0;
	}

	public long getLength() {
		return 0;
	}

	public int getPort() {
		return 0;
	}

	public String getProtocol() {
		return null;
	}

	public String getRef() {
		return null;
	}

	public String getRequestMethod() {
		return null;
	}

	public String getRequestProperty(String key) {
		return null;
	}

	public int getResponseCode() throws IOException {
		return HTTP_OK;
	}

	public String getResponseMessage() throws IOException {
		return null;
	}

	public String getType() {
		return "text/html";
	}

	public String getURL() {
		return "http://localhost";
	}

	public DataInputStream openDataInputStream() throws IOException {
		is = new ByteArrayInputStream(content.getBytes());

		return new DataInputStream(is);
	}

	public DataOutputStream openDataOutputStream() throws IOException {
		return null;
	}

	public InputStream openInputStream() throws IOException {
		is = new ByteArrayInputStream(content.getBytes());
		return is;
	}

	public OutputStream openOutputStream() throws IOException {
		return null;
	}

	public void setRequestMethod(String method) throws IOException {
	}

	public void setRequestProperty(String key, String value) throws IOException {
	}

	public String getQuery() {
		return null;
	}
}

class RenderingThread extends Thread {
	private BrowserContent _browserField;

	RenderingThread(BrowserContent field) {
		_browserField = field;
	}

	public void run() {
		try {
			_browserField.finishLoading();
		} catch (RenderingException e) {
			// Do nothing
		}
	}
}