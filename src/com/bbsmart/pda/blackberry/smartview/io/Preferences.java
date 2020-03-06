package com.bbsmart.pda.blackberry.smartview.io;

import com.bbsmart.pda.blackberry.smartview.SmartView;
import com.bbsmart.pda.blackberry.smartview.ui.util.UiUtilities;
import com.bbsmart.pda.blackberry.smartview.io.WAPGatewayProvider;

import net.rim.device.api.ui.Color;
import net.rim.device.api.util.Persistable;

/**
 * User preferences. These are saved to persistent storage.
 * 
 * @author Neil Sainsbury
 */
public final class Preferences implements Persistable {
	public static final int CONN_METH_ENT = 1;

	public static final int CONN_METH_TCP = 2;

	public static final int CONN_METH_WAP = 3;

	public static final int CACHE_CLEAR_DAY = 0;

	public static final int CACHE_CLEAR_2DAYS = 1;

	public static final int CACHE_CLEAR_WEEK = 2;

	public static final int CACHE_CLEAR_MONTH = 3;

	public static final int CACHE_CLEAR_NEVER = 4;

	private int connectionMethod;

	private WAPGatewayProvider provider;

	private boolean displayImagesInEmail;

	private boolean defaultDisplaySmartView;

	private boolean dontAskMeAgain;

	private boolean showFullDetails;

	private boolean firstTime;

	private long firstTimeRun;

	private int cacheClearFreq;

	private long lastCacheClearTime;

	private boolean showFullLinks;

	private String fontSize;

	private int fontColor;

	private int fontBgColor;

	private String fontStyle;

	private boolean lastConnSuccessful;

	private boolean displayedConnHelp;

	private boolean autoSetTaskSubject;
	
	private boolean autoAddTaskNotes;
	
	private boolean autoSetCalendarSubject;
	
	private boolean autoAddCalendarNotes;

	private boolean trial;

	private boolean registered;

	public Preferences() {
		setDisplayImagesInEmail(true);

		setDefaultDisplaySmartView(true);
		
		setDontAskMeAgain(false);
		
		// OS 4.3 does not handle the "Don't ask me again" dialog properly
		// so we set this property to true to ensure it never appears
		/* //[s430]
		setDontAskMeAgain(true);
		 //[e430] */

		// default font preferences
		setFontSize("3");
		setFontColor(Color.BLACK);
		setFontStyle("Comic Sans MS (Bold)");
		setFontBgColor(Color.LEMONCHIFFON);

		setTrial(SmartView.DEFAULT_TRIAL);
		setRegistered(SmartView.DEFAULT_REGISTERED);

		setAutoAddTaskNotes(true);
		setAutoSetTaskSubject(true);
		setAutoSetCalendarSubject(true);
		setAutoAddCalendarNotes(false);
		setShowFullDetails(false);
		setFirstTime(true);
		setShowFullLinks(false);
		setLastConnSuccessful(false);
		setDisplayedConnHelp(false);
		setConnectionMethod(CONN_METH_ENT);
		setCacheClearFreq(CACHE_CLEAR_WEEK);
		updateLastCacheClearTime();
	}

	public void setDisplayImagesInEmail(boolean displayImagesInEmail) {
		this.displayImagesInEmail = displayImagesInEmail;
	}

	public boolean isDisplayImagesInEmail() {
		return displayImagesInEmail;
	}

	public void setDontAskMeAgain(boolean dontAskMeAgain) {
		this.dontAskMeAgain = dontAskMeAgain;
	}

	public boolean isDontAskMeAgain() {
		return dontAskMeAgain;
	}

	public void setDefaultDisplaySmartView(boolean defaultDisplaySmartView) {
		this.defaultDisplaySmartView = defaultDisplaySmartView;
	}

	public boolean isDefaultDisplaySmartView() {
		return defaultDisplaySmartView;
	}

	public void setShowFullDetails(boolean showFullDetails) {
		this.showFullDetails = showFullDetails;
	}

	public boolean isShowFullDetails() {
		return showFullDetails;
	}

	public void setFirstTime(boolean firstTime) {
		this.firstTime = firstTime;
	}

	public boolean isFirstTime() {
		return firstTime;
	}

	public void setConnectionMethod(int connectionMethod) {
		this.connectionMethod = connectionMethod;
	}

	public int getConnectionMethod() {
		return connectionMethod;
	}

	public void setProvider(WAPGatewayProvider provider) {
		this.provider = provider;
	}

	public WAPGatewayProvider getProvider() {
		return provider;
	}

	public String getConnectionSuffix() {
		String suffix = UiUtilities.EMPTY_STRING;

		switch (connectionMethod) {
		case CONN_METH_ENT:
			suffix = ";deviceside=false";
			break;
		case CONN_METH_TCP:
			suffix = ";deviceside=true";
			break;
		case CONN_METH_WAP:
			suffix = provider.getConnectionSuffix();
			break;
		}

		return suffix;
	}

	public void setCacheClearFreq(int cacheClearFreq) {
		this.cacheClearFreq = cacheClearFreq;
	}

	public int getCacheClearFreq() {
		return cacheClearFreq;
	}

	public void updateLastCacheClearTime() {
		this.lastCacheClearTime = System.currentTimeMillis();
	}

	public long getLastCacheClearTime() {
		return lastCacheClearTime;
	}

	public void setFirstTimeRun(long firstTimeRun) {
		this.firstTimeRun = firstTimeRun;
	}

	public long getFirstTimeRun() {
		return firstTimeRun;
	}

	public void setShowFullLinks(boolean showFullLinks) {
		this.showFullLinks = showFullLinks;
	}

	public boolean isShowFullLinks() {
		return showFullLinks;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public String getFontSize() {
		return fontSize;
	}

	public void setFontColor(int fontColor) {
		this.fontColor = fontColor;
	}

	public int getFontColor() {
		return fontColor;
	}

	public void setFontStyle(String fontStyle) {
		this.fontStyle = fontStyle;
	}

	public String getFontStyle() {
		return fontStyle;
	}

	public void setFontBgColor(int fontBgColor) {
		this.fontBgColor = fontBgColor;
	}

	public int getFontBgColor() {
		return fontBgColor;
	}

	public void setLastConnSuccessful(boolean lastConnSuccessful) {
		this.lastConnSuccessful = lastConnSuccessful;
	}

	public boolean isLastConnSuccessful() {
		return lastConnSuccessful;
	}

	public void setDisplayedConnHelp(boolean displayedConnHelp) {
		this.displayedConnHelp = displayedConnHelp;
	}

	public boolean isDisplayedConnHelp() {
		return displayedConnHelp;
	}

	public void setTrial(boolean trial) {
		this.trial = trial;
	}

	public boolean isTrial() {
		return trial;
	}

	public void setRegistered(boolean registered) {
		this.registered = registered;
	}

	public boolean isRegistered() {
		return registered;
	}

	public void setAutoSetTaskSubject(boolean autoSetTaskSubject) {
		this.autoSetTaskSubject = autoSetTaskSubject;
	}

	public boolean isAutoSetTaskSubject() {
		return autoSetTaskSubject;
	}

	public void setAutoAddTaskNotes(boolean autoAddTaskNotes) {
		this.autoAddTaskNotes = autoAddTaskNotes;
	}

	public boolean isAutoAddTaskNotes() {
		return autoAddTaskNotes;
	}

	public void setAutoSetCalendarSubject(boolean autoSetCalendarSubject) {
		this.autoSetCalendarSubject = autoSetCalendarSubject;
	}

	public boolean isAutoSetCalendarSubject() {
		return autoSetCalendarSubject;
	}

	public void setAutoAddCalendarNotes(boolean autoAddCalendarNotes) {
		this.autoAddCalendarNotes = autoAddCalendarNotes;
	}

	public boolean isAutoAddCalendarNotes() {
		return autoAddCalendarNotes;
	}
}