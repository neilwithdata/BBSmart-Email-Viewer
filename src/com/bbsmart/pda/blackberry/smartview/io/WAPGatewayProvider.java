package com.bbsmart.pda.blackberry.smartview.io;

import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.Persistable;

/**
 * Container class for WAP Gateway Provider information
 * 
 * @author Neil Sainsbury
 */
public final class WAPGatewayProvider implements Persistable, Comparator {
	public static final int CUSTOM_ID = -1;
	public static final String CUSTOM_NAME = "Custom";
	public static final String DEFAULT_PORT = "9201";
	
	private int id;
	private String name;
	private String apn;
	private String ip;
	private String port;
	private String username;
	private String password;
	
	public WAPGatewayProvider() {
		
	}
	
	public WAPGatewayProvider(int id, String name, String apn, String ip) {
		this.id = id;
		this.name = name;
		this.apn = apn;
		this.ip = ip;
		this.port = DEFAULT_PORT;
	}

	public WAPGatewayProvider(int id, String name, String apn, String ip,
			String port) {
		this.id = id;
		this.name = name;
		this.apn = apn;
		this.ip = ip;
		this.port = port;
	}

	public WAPGatewayProvider(int id, String name, String apn, String ip,
			String username, String password) {
		this(id, name, apn, ip);
		this.username = username;
		this.password = password;
	}

	public void setApn(String apn) {
		this.apn = apn;
	}

	public String getApn() {
		return apn;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getPort() {
		return port;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return this.name;
	}

	/**
	 * Returns the String required to append to the end of a connection
	 * URL when establishing an external HTTP connection over WAP.
	 * 
	 * @return The connection suffix
	 */
	public String getConnectionSuffix() {
		StringBuffer suffix = new StringBuffer();

		// Append mandatory wap parameters
		suffix.append(";WapGatewayIP=");
		suffix.append(getIp());
		suffix.append(";WapGatewayAPN=");
		suffix.append(getApn());

		// Append optional wap parameters
		if (port != null && port.length() > 0) {
			suffix.append(";WapGatewayPort=");
			suffix.append(port);
		}

		if (username != null && username.length() > 0) {
			suffix.append(";TunnelAuthUsername=");
			suffix.append(username);
		}

		if (password != null && password.length() > 0) {
			suffix.append(";TunnelAuthPassword=");
			suffix.append(password);
		}

		return suffix.toString();
	}

	public boolean equals(Object obj) {
		if (obj instanceof WAPGatewayProvider) {
			return ((WAPGatewayProvider) obj).getName() == this.name;
		}
		return false;
	}

	public int compare(Object o1, Object o2) throws ClassCastException {
		return ((WAPGatewayProvider) o1).getName().compareTo(
				((WAPGatewayProvider) o2).getName());
	}
}