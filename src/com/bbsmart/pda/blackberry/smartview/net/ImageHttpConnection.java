package com.bbsmart.pda.blackberry.smartview.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.HttpConnection;

public final class ImageHttpConnection implements HttpConnection {
	private String url;

	private String mimeType;

	private String content;

	private ByteArrayInputStream is;

	public void setContent(String content) {
		this.content = content;
		is = new ByteArrayInputStream(this.content.getBytes());
	}

	public void setURL(String url) {
		this.url = url;
		determineMimeType();
	}

	private void determineMimeType() {
		if (url.endsWith(".gif")) {
			mimeType = "image/gif";
		} else if (url.endsWith(".png")) {
			mimeType = "image/x-png";
		} else if (url.endsWith(".jpeg") || url.endsWith(".jpg")
				|| url.endsWith(".jpe")) {
			mimeType = "image/jpeg";
		}
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
			return mimeType;
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
		return mimeType;
	}

	public String getURL() {
		return url;
	}

	public DataInputStream openDataInputStream() throws IOException {
		return new DataInputStream(is);
	}

	public DataOutputStream openDataOutputStream() throws IOException {
		return null;
	}

	public InputStream openInputStream() throws IOException {
		is.reset();
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
