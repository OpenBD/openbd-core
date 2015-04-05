/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
 *
 *  This file is part of Open BlueDragon (OpenBD) CFML Server Engine.
 *  
 *  OpenBD is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *  
 *  OpenBD is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with OpenBD.  If not, see http://www.gnu.org/licenses/
 *  
 *  Additional permission under GNU GPL version 3 section 7
 *  
 *  If you modify this Program, or any covered work, by linking or combining 
 *  it with any of the JARS listed in the README.txt (or a modified version of 
 *  (that library), containing parts covered by the terms of that JAR, the 
 *  licensors of this Program grant you additional permission to convey the 
 *  resulting work. 
 *  README.txt @ http://www.openbluedragon.org/license/README.txt
 *  
 *  http://www.openbluedragon.org/
 */

/*
 * Created on May 18, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.naryx.tagfusion.cfm.xml.ws;

import com.naryx.tagfusion.cfm.engine.cfWSParameters;

public class CallParameters implements ICallParameters {
	public static final int DEFAULT_PROXYPORT = 8080;

	private int timeout = -1;
	private int proxyPort = -1;
	private String username = null;
	private String password = null;
	private String proxyServer = null;
	private String proxyUser = null;
	private String proxyPassword = null;

	public CallParameters() {
		this.timeout = 0;
	}

	public void convertFrom(cfWSParameters op) {
		if (op != null) {
			String[] names = op.getNamesArray();
			Object[] vals = op.getValuesArray();
			Boolean[] omitted = op.getOmittedArray();
			for (int i = 0; i < names.length; i++) {
				if (!omitted[i].booleanValue()) {
					if (names[i].equalsIgnoreCase("TIMEOUT")) {
						try {
							setTimeout(Integer.parseInt(vals[i].toString()));
						} catch (NumberFormatException ex) {
						}
					} else if (names[i].equalsIgnoreCase("USERNAME")) {
						setUsername(vals[i].toString());
					} else if (names[i].equalsIgnoreCase("PASSWORD")) {
						setPassword(vals[i].toString());
					} else if (names[i].equalsIgnoreCase("PROXYSERVER")) {
						setProxyServer(vals[i].toString());
					} else if (names[i].equalsIgnoreCase("PROXYPORT")) {
						try {
							setProxyPort(Integer.parseInt(vals[i].toString()));
						} catch (NumberFormatException ex) {
						}
					} else if (names[i].equalsIgnoreCase("PROXYUSER")) {
						setProxyUser(vals[i].toString());
					} else if (names[i].equalsIgnoreCase("PROXYPASSWORD")) {
						setProxyPassword(vals[i].toString());
					}
				}
			}
		}
	}

	/**
	 * @return
	 */
	public int getDefaultProxyPort() {
		return DEFAULT_PROXYPORT;
	}

	/**
	 * @return
	 */
	public int getProxyPort() {
		return proxyPort;
	}

	/**
	 * @param proxyPort
	 */
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	/**
	 * @return
	 */
	public String getProxyServer() {
		return proxyServer;
	}

	/**
	 * @param proxyServer
	 */
	public void setProxyServer(String proxyServer) {
		this.proxyServer = proxyServer;
	}

	/**
	 * @return
	 */
	public String getProxyUser() {
		return proxyUser;
	}

	/**
	 * @param proxyUser
	 */
	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	/**
	 * @return
	 */
	public String getProxyPassword() {
		return proxyPassword;
	}

	/**
	 * @param proxyPassword
	 */
	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	/**
	 * @return
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param username
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}
