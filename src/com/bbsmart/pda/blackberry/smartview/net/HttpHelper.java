package com.bbsmart.pda.blackberry.smartview.net;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import com.bbsmart.pda.blackberry.smartview.io.PersistenceManager;

import net.rim.device.api.system.RadioInfo;

public final class HttpHelper {
	public static final String NET_ERR_MSG = "A problem was encountered connecting to the requested server."
			+ "\n\nPlease check your blackberry has network coverage and try again later"
			+ "\n\nIf the problem persists, the resource you are requesting may no longer exist";

	public static final String COVER_ERR_MSG = "Device has no network coverage. "
			+ "Please ensure the radio is turned on and your blackberry has network coverage";

	public static String getUrl(String url, boolean unifyEOLs) throws Exception {
		HttpConnection httpConn = null;

		if (PersistenceManager.getInstance().getPreferences()
				.getConnectionMethod() == 4) {
			url += ";deviceside=false;ConnectionType=mds-public";
		} else {
			url += PersistenceManager.getInstance().getPreferences()
					.getConnectionSuffix();
		}

		if (RadioInfo.getSignalLevel() == RadioInfo.LEVEL_NO_COVERAGE) {
			// Device has no coverage - do not even attempt a connection
			throw new Exception(COVER_ERR_MSG);
		}

		try {
			httpConn = (HttpConnection) Connector.open(url, Connector.READ,
					true);

			int status = httpConn.getResponseCode();
			if (status == HttpConnection.HTTP_OK) {
				// Read the data returned from the server
				StringBuffer raw = readHttpData(httpConn);
				if (unifyEOLs) {
					HttpHelper.unifyEols(raw);
				}
				return raw.toString();
			} else {
				throw new Exception(NET_ERR_MSG);
			}
		} catch (Exception e) {
			throw new Exception(NET_ERR_MSG);
		} finally {
			if (httpConn != null) {
				httpConn.close();
			}
		}
	}

	private static StringBuffer readHttpData(HttpConnection httpConn)
			throws IOException {
		InputStream input = null;
		StringBuffer raw = new StringBuffer();
		int len = 0;
		try {
			input = httpConn.openInputStream();

			byte[] data = new byte[256];
			while (-1 != (len = input.read(data))) {
				raw.append(new String(data, 0, len));
			}
		} finally {
			if (input != null) {
				input.close();
			}
		}

		return raw;
	}

	/**
	 * remove all the \r's out of the string buffer. This should just remove
	 * \r's before \n's, but it currently just removes them all.
	 * 
	 * @param sb
	 */
	public static void unifyEols(StringBuffer sb) {
		for (int i = sb.length(); --i >= 0;)
			if (sb.charAt(i) == '\r')
				sb.deleteCharAt(i);
	}
}