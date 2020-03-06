package com.bbsmart.pda.blackberry.smartview.net;

import java.util.Vector;

import javax.microedition.io.HttpConnection;

import com.bbsmart.pda.blackberry.smartview.io.ImageCache;
import com.bbsmart.pda.blackberry.smartview.io.PersistenceManager;
import com.bbsmart.pda.blackberry.smartview.multi.Semaphore;

import net.rim.device.api.browser.field.BrowserContent;
import net.rim.device.api.browser.field.RequestedResource;

public final class ImageConnectionManager {
	private int completedRequests;

	private int erroredRequests;

	private Semaphore connLimiter;

	private volatile ImageCache iCache;

	private final static ImageConnectionManager instance = new ImageConnectionManager();

	private volatile Vector failedConnections;

	private volatile Vector requestedConnections;

	private boolean cancelled;

	public static ImageConnectionManager getInstance() {
		return instance;
	}

	private ImageConnectionManager() {
		reset();
		connLimiter = new Semaphore();
		iCache = PersistenceManager.getInstance().getImageCache();
	}

	public void reset() {
		cancelled = false;
		completedRequests = 0;
		erroredRequests = 0;
		failedConnections = new Vector();
		requestedConnections = new Vector();
	}

	public void cancel() {
		reset();
		this.cancelled = true;
	}

	public int getNumCompletedRequests() {
		return completedRequests;
	}

	public int getNumErroredRequests() {
		return erroredRequests;
	}

	public void requestImage(RequestedResource resource, BrowserContent referrer) {
		if (cancelled) {
			return;
		}

		try {
			connLimiter.procure();
		} catch (InterruptedException ie) {
			// Interrupted by some other means
			return;
		}

		requestedConnections.addElement(resource.getUrl());
		new HttpConnectionThread(resource.getUrl(), new ResourceReceiver(
				referrer, resource), true).start();
	}

	public HttpConnection requestCachedImage(RequestedResource resource) {
		if (cancelled) {
			return null;
		}

		// If the image exists in the cache, return it rather than establishing
		// an HTTP connection
		if (iCache.containsImage(resource.getUrl())) {
			if (!iCache.getPredefinedCache().containsKey(resource.getUrl())) {
				completedRequests++;
			}

			// This image can be returned from the cache
			ImageHttpConnection iConn = new ImageHttpConnection();
			iConn.setURL(resource.getUrl());
			iConn.setContent(iCache.getImage(resource.getUrl()));

			return iConn;
		} else {
			if (failedConnections.contains(resource.getUrl())) {
				erroredRequests++;
			}

			if (requestedConnections.contains(resource.getUrl())) {
				// This image is already being requested from another source
				// The browser is smart enough to know this and will therefore
				// take care of handling this image in its internal cache
				completedRequests++;
			}

			return null;
		}
	}

	class ResourceReceiver implements HttpContentReceiver {
		private RequestedResource resource;

		private BrowserContent referrer;

		public ResourceReceiver(BrowserContent referrer,
				RequestedResource resource) {
			this.resource = resource;
			this.referrer = referrer;
		}

		public void receivedHttpContent(HttpConnectionThread thread,
				String content) {
			completedRequests++;

			// Construct a new ImageHttpConnection with the image content loaded
			ImageHttpConnection iConn = new ImageHttpConnection();
			iConn.setURL(thread.getUrl());
			iConn.setContent(content);

			resource.setHttpConnection(iConn);

			// Inform the referrer that the resource is ready
			referrer.resourceReady(resource);

			// Content has been received - make way for another HttpConnection
			connLimiter.vacate();

			// Store the retrieved image content in cache
			if (!iCache.containsImage(thread.getUrl())) {
				iCache.addImageToCache(thread.getUrl(), content);
				PersistenceManager.getInstance().saveImageCache(iCache);
			}
		}

		public void receivedHttpError(HttpConnectionThread thread, String error) {
			erroredRequests++;

			failedConnections.addElement(thread.getUrl());

			// This connection has ended - make way for another HttpConnection
			connLimiter.vacate();
		}
	}
}