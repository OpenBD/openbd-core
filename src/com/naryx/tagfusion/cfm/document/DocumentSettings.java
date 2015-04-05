/* 
 * Copyright (C) 2000 - 2008 TagServlet Ltd
 *
 * This file is part of the BlueDragon Java Open Source Project.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.naryx.tagfusion.cfm.document;

public class DocumentSettings {

	private String marginLeft;
	private String marginRight;
	private String marginTop;
	private String marginBottom;
	
	private String userAgent;
	
	private String proxyHost;
	private int proxyPort;
	private String proxyUser;
	private String proxyPassword;
	
	private String pageSize;
	private String unit;
	
	public void setProxyDetails( String _host, int _port, String _user, String _password ){
		proxyHost = _host;
		proxyPort = _port;
		proxyUser = _user;
		proxyPassword = _password;
	}

	public String getProxyHost() {
	  return proxyHost;
  }

	public int getProxyPort() {
	  return proxyPort;
  }

	public String getProxyUser() {
	  return proxyUser;
  }

	public String getProxyPassword() {
	  return proxyPassword;
  }

	public void setUserAgent( String userAgent ) {
	  this.userAgent = userAgent;
  }

	public String getUserAgent() {
	  return userAgent;
  }

	public void setMarginBottom( String marginBottom ) {
	  this.marginBottom = marginBottom;
  }

	public String getMarginBottom() {
	  return marginBottom;
  }

	public void setMarginTop( String marginTop ) {
	  this.marginTop = marginTop;
  }

	public String getMarginTop() {
	  return marginTop;
  }

	public void setMarginRight( String marginRight ) {
	  this.marginRight = marginRight;
  }

	public String getMarginRight() {
	  return marginRight;
  }

	public void setMarginLeft( String marginLeft ) {
	  this.marginLeft = marginLeft;
  }

	public String getMarginLeft() {
	  return marginLeft;
  }

	public String getPageSize(){
		return pageSize;
	}
	
	public void setPageSize( String pageSize ) {
		this.pageSize = pageSize;
  }

	public void setUnit( String unit ){
		this.unit = unit;
	}
	
	public String getUnit(){
		return unit;
	}

/*	public String getHeader(){
		return header;
	}
	
	public void setHeader( String header ) {
		this.header = header; 
  }

	public String getFooter(){
		return footer;
	}
	
	public void setFooter( String footer ) {
		this.footer = footer;
  }
  */
}
