package com.bbsmart.pda.blackberry.smartview.io;

import java.util.Enumeration;

import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;

import net.rim.device.api.util.MultiMap;
import net.rim.device.api.util.Persistable;

public final class ImageCache implements Persistable {
	// The number of images held in the cache
	private int numImages;

	// The total size of the cache (in bytes)
	private int cacheSize;

	// The actual cache itself: url(key) -> data(value) mapping
	private MultiMap cache;

	// Holds the images which are "always in cache" (predefined)
	private MultiMap predefinedCache;

	public ImageCache() {
		numImages = 0;
		cacheSize = 0;
		cache = new MultiMap();

		initPredefinedCache();
		addPredefinedCache();
	}

	private void initPredefinedCache() {
		// Initialise the predefined cache
		predefinedCache = new MultiMap(2, 1);

		String image;
		if (UiUtilities.DEVICE_240W) {
			image = new String(UiUtilities
					.getImageBytes("original-follows-text_240x12.png"));
		} else {
			image = new String(UiUtilities
					.getImageBytes("original-follows-text_320x12.png"));
		}
		predefinedCache.add("http://localhost/original.png", image);

		image = new String(UiUtilities.getImageBytes("separator_320x4.jpg"));
		predefinedCache.add("http://localhost/line.jpg", image);

		image = new String(UiUtilities.getImageBytes("lock.jpg"));
		predefinedCache.add("http://localhost/lock.jpg", image);

		// Insert Smileys into cache
		image = new String(UiUtilities.getImageBytes("smileys/smile.gif"));
		predefinedCache.add("http://localhost/smile.gif", image);

		image = new String(UiUtilities.getImageBytes("smileys/tongue.gif"));
		predefinedCache.add("http://localhost/tongue.gif", image);

		image = new String(UiUtilities.getImageBytes("smileys/sad.gif"));
		predefinedCache.add("http://localhost/sad.gif", image);

		image = new String(UiUtilities.getImageBytes("smileys/laugh.gif"));
		predefinedCache.add("http://localhost/laugh.gif", image);

		image = new String(UiUtilities.getImageBytes("smileys/wink.gif"));
		predefinedCache.add("http://localhost/wink.gif", image);

		image = new String(UiUtilities.getImageBytes("smileys/love.gif"));
		predefinedCache.add("http://localhost/love.gif", image);
		
		image = new String(UiUtilities.getImageBytes("smileys/mad.gif"));
		predefinedCache.add("http://localhost/mad.gif", image);
		
		image = new String(UiUtilities.getImageBytes("smileys/ohmy.gif"));
		predefinedCache.add("http://localhost/ohmy.gif", image);
	}

	public boolean addImageToCache(String url, String contents) {
		if (!cache.containsKey(url)) {
			if (cache.add(url, contents)) {
				numImages++;
				cacheSize += contents.getBytes().length;
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public String getImage(String url) {
		if (cache.containsKey(url)) {
			return (String) cache.elements(url).nextElement();
		} else {
			return null;
		}
	}

	public boolean containsImage(String url) {
		return cache.containsKey(url);
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public MultiMap getPredefinedCache() {
		return predefinedCache;
	}

	private void addPredefinedCache() {
		Enumeration elements = predefinedCache.elements();
		Enumeration keys = predefinedCache.keys();
		while (keys.hasMoreElements()) {
			cache.add((String) keys.nextElement(), (String) elements
					.nextElement());
		}
	}

	public void clear() {
		cache.clear();
		numImages = 0;
		cacheSize = 0;
		addPredefinedCache();
	}

	/**
	 * Automatically clears the cache out if a certain amount of time has past
	 * (as specified by the preferences). This method is invoked every time an
	 * email is opened using the Email Viewer.
	 */
	public void autoClear() {
		PersistenceManager persistManager = PersistenceManager.getInstance();
		Preferences prefs = persistManager.getPreferences();

		long currentTime = System.currentTimeMillis();
		long lastClearTime = prefs.getLastCacheClearTime();

		// The number of milliseconds in a day
		long millisInDay = 1000 * 60 * 60 * 24;

		boolean clearCache = false;

		switch (prefs.getCacheClearFreq()) {
		case Preferences.CACHE_CLEAR_DAY:
			clearCache = currentTime > (lastClearTime + millisInDay);
			break;
		case Preferences.CACHE_CLEAR_2DAYS:
			clearCache = currentTime > (lastClearTime + millisInDay * 2);
			break;
		case Preferences.CACHE_CLEAR_WEEK:
			clearCache = currentTime > (lastClearTime + millisInDay * 7);
			break;
		case Preferences.CACHE_CLEAR_MONTH:
			clearCache = currentTime > (lastClearTime + millisInDay * 30);
			break;
		case Preferences.CACHE_CLEAR_NEVER:
			clearCache = false;
			break;
		}

		if (clearCache) {
			clear();
			prefs.updateLastCacheClearTime();

			persistManager.savePreferences(prefs);
			persistManager.saveImageCache(this);
		}
	}

	public String getDisplayableCacheSize() {
		final int KB = 1024;
		final int MB = 1048576;

		if (cacheSize < KB) {
			return cacheSize + "b";
		} else if (cacheSize < MB) {
			return (cacheSize / KB) + "KB";
		} else {
			return (cacheSize / MB) + "MB";
		}
	}

	public static boolean isImage(String filename) {
		return (filename.endsWith(".jpg") || filename.endsWith(".jpeg")
				|| filename.endsWith(".gif") || filename.endsWith(".png") || filename
				.endsWith(".bmp"));
	}
}