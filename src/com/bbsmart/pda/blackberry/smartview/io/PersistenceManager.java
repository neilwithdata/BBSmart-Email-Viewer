package com.bbsmart.pda.blackberry.smartview.io;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

/**
 * Save the preferences and other items to the PDA.
 * 
 * @author Neil Sainsbury
 */
public final class PersistenceManager {
	// key is hash of string "com.bbsmart.pda.blackberry.smartview"
	private static PersistentObject store = PersistentStore
			.getPersistentObject(0xb40623371807f332L);

	private static Vector data;

	private static final int STORE_PREF_INDX = 0;
	private static final int STORE_WAP_LIST_INDX = 1;
	private static final int STORE_ICACHE_INDX = 2;
	public static final int STORE_SNOTE_INDX = 3;
	public static final int STORE_CPREF_INDX = 4;
	public static final int STORE_GDATA_INDX = 5;

	private static PersistenceManager instance;

	// Singleton Accessor
	public static PersistenceManager getInstance() {
		if (instance == null) {
			instance = new PersistenceManager();
		}
		return instance;
	}

	static {
		synchronized (store) {
			data = (Vector) store.getContents();
			if (data == null) {
				data = new Vector();

				// PERSISTENT DATA
				data.addElement(new Preferences()); // Preferences
				data.addElement(initWAPProviderList()); // WAP Provider list
				data.addElement(new ImageCache()); // Image Cache
				data.addElement(new SmartNoteManager()); // Smart Notes -- added v2.2
				data.addElement(new Vector()); // Color preferences -- added v2.2
				data.addElement(new Hashtable()); // General data store - added v2.3
				// END PERSISTENT DATA

				store.setContents(data);
				store.commit();
			} else {
				// Data is not null - if only 3 elements in list user has v2.1
				// or lower so perform an upgrade to v2.3 which now has the
				// smart note manager, email header color preferences and general data store
				if (data.size() == 3) {
					data.addElement(new SmartNoteManager());
					data.addElement(new Vector());
					data.addElement(new Hashtable());
				} else if (data.size() == 5) {
					// User had v2.2 - upgrade to v2.3 which now has general data store
					data.addElement(new Hashtable());
				}
			}

			upgradeDataStore();
		}
	}

	private static void upgradeDataStore() {
		Hashtable gData = (Hashtable) data.elementAt(STORE_GDATA_INDX);

		// Upgrade from v2.2 to v2.3 -- previously no general data store
		if (gData.size() == 0) {
			gData.put("storeVersion", "2.3");
			gData.put("headerFontSize", "Medium");
			gData.put("headerBoldFont", Boolean.TRUE);
			gData.put("templateTitles", new Vector());
			gData.put("templateValues", new Vector());

			initDefaultTemplates();
		}
	}

	private static void initDefaultTemplates() {
		Hashtable gData = (Hashtable) data.elementAt(STORE_GDATA_INDX);

		Vector titles = (Vector) gData.get("templateTitles");
		Vector templates = (Vector) gData.get("templateValues");

		titles.addElement("Business Email Signature");
		templates
				.addElement("Replace this text with your business email signature.\n\n"
						+ "Then, next time you are composing a business email select \"SmartSay...\""
						+ " from the menu, highlight this entry in the list and select \"Insert in Email\"."
						+ " Your business signature will be automatically inserted into the email!\n\n"
						+ "Less typing! Less frustration! More time to take care of what is really important...");

		titles.addElement("Personal Email Signature");
		templates
				.addElement("Replace this text with your personal email signature.\n\n"
						+ "Then, next time you are composing a personal email select \"SmartSay...\""
						+ " from the menu, highlight this entry in the list and select \"Insert in Email\"."
						+ " Your personal signature will be automatically inserted into the email!\n\n"
						+ "Less typing! Less frustration! More time to take care of what is really important...");

		titles.addElement("My Phone Number");
		templates
				.addElement("Need to email somebody your phone number? Dread grappling with the small number keys?\n\n"
						+ "Easy! Replace this text with your phone number and then the next time you are composing an email select \"SmartSay...\""
						+ " from the menu, highlight this entry in the list and select \"Insert in Email\"."
						+ " Your phone number will be automatically inserted into the email!\n\n"
						+ "Less typing! Less frustration! More time to take care of what is really important...");

	}

	public Hashtable getDataStore() {
		return (Hashtable) getStoreDataAtIndex(STORE_GDATA_INDX);
	}

	public void saveDataStore(Hashtable t) {
		setStoreDataAtIndex(STORE_GDATA_INDX, t);
	}

	// Suppress generation of public (default) constructor
	private PersistenceManager() {
	}

	public Preferences getPreferences() {
		return (Preferences) getStoreDataAtIndex(STORE_PREF_INDX);
	}

	public void savePreferences(Preferences p) {
		setStoreDataAtIndex(STORE_PREF_INDX, p);
	}

	public Vector getProviderList() {
		return (Vector) getStoreDataAtIndex(STORE_WAP_LIST_INDX);
	}

	public ImageCache getImageCache() {
		return (ImageCache) getStoreDataAtIndex(STORE_ICACHE_INDX);
	}

	public void saveImageCache(ImageCache ic) {
		setStoreDataAtIndex(STORE_ICACHE_INDX, ic);
	}

	public SmartNoteManager getSmartNoteManager() {
		return (SmartNoteManager) getStoreDataAtIndex(STORE_SNOTE_INDX);
	}

	public void saveSmartNoteManager(SmartNoteManager snm) {
		setStoreDataAtIndex(STORE_SNOTE_INDX, snm);
	}

	public Vector getEmailColorPrefs() {
		return (Vector) getStoreDataAtIndex(STORE_CPREF_INDX);
	}

	public void saveEmailColorPrefs(Vector cprefs) {
		setStoreDataAtIndex(STORE_CPREF_INDX, cprefs);
	}

	private static Vector initWAPProviderList() {
		Vector providers = new Vector(40);

		providers.addElement(new WAPGatewayProvider(1, "Amena (ESP)",
				"internet", "213.143.33.8"));
		providers.addElement(new WAPGatewayProvider(2, "AT&T Wireless (USA)",
				"proxy", "10.250.250.250"));
		providers.addElement(new WAPGatewayProvider(3, "BT Mobile (UK)",
				"mobile.bt.uk", "149.254.201.132"));
		providers.addElement(new WAPGatewayProvider(4, "Cincinnati Bell (USA)",
				"wap.gocbw.com", "216.68.79.199"));
		providers.addElement(new WAPGatewayProvider(5, "Cingular (USA)",
				"wap.cingular", "66.209.11.61", "9203"));
		providers.addElement(new WAPGatewayProvider(6, "CSL 1010 (HKG)",
				"hkcsl", "192.168.59.51"));
		providers.addElement(new WAPGatewayProvider(7, "E-Plus (GBR)",
				"wap.eplus.de", "212.23.97.9"));
		providers.addElement(new WAPGatewayProvider(8, "Fido (CAN)",
				"internet.fido.ca", "205.151.11.11", "fido", "fido"));
		providers.addElement(new WAPGatewayProvider(9, "Movistar (ESP)",
				"movistar.es", "194.179.1.100"));
		providers.addElement(new WAPGatewayProvider(10, "Omnitel (ITA)",
				"wap.omnitel.it", "10.128.201.76"));
		providers.addElement(new WAPGatewayProvider(11, "Optus (AUS)",
				"wap.optus.net.au", "202.139.83.152"));
		providers.addElement(new WAPGatewayProvider(12, "Orange (FRA)",
				"orange.fr", "192.168.10.200"));
		providers.addElement(new WAPGatewayProvider(13, "Orange (GBR)",
				"orangewap", "192.168.71.35"));
		providers.addElement(new WAPGatewayProvider(14, "O2 (GBR)",
				"wap.o2.co.uk", "193.113.200.195", "o2wap", "password"));
		providers.addElement(new WAPGatewayProvider(15, "O2 (DEU)",
				"wap.viaginterkorn.de", "195.182.114.52"));
		providers.addElement(new WAPGatewayProvider(16, "O2 (NLD)", "internet",
				"193.113.200.195"));
		providers.addElement(new WAPGatewayProvider(17, "O2 (IRL)",
				"wap.dol.ie", "192.168.90.74"));
		providers.addElement(new WAPGatewayProvider(18, "Rogers AT&T (CAN)",
				"blackberry.net", "208.200.67.150"));
		providers.addElement(new WAPGatewayProvider(19, "SFR (FRA)", "wapsfr",
				"195.115.25.129"));
		providers.addElement(new WAPGatewayProvider(20, "Sunrise (CHE)",
				"wap.sunrise.ch", "212.35.34.70"));
		providers.addElement(new WAPGatewayProvider(21, "Swisscom (CHE)",
				"gprs.swisscom.ch", "192.168.210.1"));
		providers.addElement(new WAPGatewayProvider(22,
				"Telefonica Movil (USA)", "wap.movistar.es", "192.168.80.21"));
		providers.addElement(new WAPGatewayProvider(23, "Telstra (AUS)",
				"telstra.wap", "10.1.1.150"));
		providers.addElement(new WAPGatewayProvider(24, "Telstra (USA)",
				"telstra.internet", "10.1.1.150"));
		providers.addElement(new WAPGatewayProvider(25, "Tesco (GBR)",
				"prepay.tesco-mobile.com", "193.113.200.195"));
		providers.addElement(new WAPGatewayProvider(26, "TIM (ITA)",
				"wap.tim.it", "213.26.205.1"));
		providers.addElement(new WAPGatewayProvider(27, "T-Mobile (AUT)",
				"gprswap", "10.12.0.2"));
		providers.addElement(new WAPGatewayProvider(28, "T-Mobile (DEU)",
				"wap.t-d1.de", "193.254.160.2"));
		providers.addElement(new WAPGatewayProvider(29, "T-Mobile (GBR1)",
				"general.t-mobile.uk", "149.254.1.10"));
		providers.addElement(new WAPGatewayProvider(30, "T-Mobile (GBR2)",
				"blackberry.net", "140.254.1.0"));
		providers.addElement(new WAPGatewayProvider(31, "T-Mobile (USA)",
				"wap.voicestream.com", "216.155.165.50"));
		providers.addElement(new WAPGatewayProvider(32, "Virgin Mobile (GBR)",
				"goto.virginmobile.uk", "193.30.166.3"));
		providers.addElement(new WAPGatewayProvider(33, "Vodafone (AUS)",
				"live.vodafone.com", "10.202.2.60"));
		providers.addElement(new WAPGatewayProvider(34, "Vodafone (DEU)",
				"wap.vodafone.de", "139.007.029.001"));
		providers.addElement(new WAPGatewayProvider(35, "Vodafone (ESP)",
				"airtelwap.es", "212.73.32.10"));
		providers.addElement(new WAPGatewayProvider(36, "Vodafone (GBR)",
				"wap.vodafone.co.uk", "212.183.137.12", "user@vodafone.net",
				"user"));
		providers.addElement(new WAPGatewayProvider(37, "Vodafone (IRL)",
				"live.vodafone.com", "10.24.59.100"));
		providers.addElement(new WAPGatewayProvider(38, "Vodafone (NLD)",
				"blackberry.vodafone.nl", "192.168.251.150"));
		providers.addElement(new WAPGatewayProvider(39, "Vodafone (NZL)",
				"wap.vodafone.net.nz", "172.30.38.3"));
		providers.addElement(new WAPGatewayProvider(40, "Vodafone (SWE)",
				"services.vodafone.net", "172.030.253.241"));

		return providers;
	}

	private Object getStoreDataAtIndex(int index) {
		Object o = null;
		synchronized (store) {
			try {
				data = (Vector) store.getContents();
				o = data.elementAt(index);
			} catch (Exception e) {
				return null; // Controlled access exception - hide silently
			}
		}
		return o;
	}

	private void setStoreDataAtIndex(int index, Object o) {
		synchronized (store) {
			try {
				data = (Vector) store.getContents();
				data.setElementAt(o, index);
				store.setContents(data);
				store.commit();
			} catch (Exception e) {
				// Controlled access exception - hide silently
			}
		}
	}
}
