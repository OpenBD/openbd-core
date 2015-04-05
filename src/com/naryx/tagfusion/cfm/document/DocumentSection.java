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

package com.naryx.tagfusion.cfm.document;

import java.io.File;
import java.net.MalformedURLException;

import com.naryx.tagfusion.cfm.engine.cfSession;

public class DocumentSection {

	private String marginBottom;
	private String marginLeft;
	private String marginRight;
	private String marginTop;
	
	private String authUser;
	private String authPassword;
	
	private String mimeType;
	private String name;
	
	private String src;
	private String srcFile;
	private String body;
	
	private String header;
	private String footer;
	private String headerAlign;
	private String footerAlign;
	
	private String userAgent;
	
	private int totalSectionPageCount;
	private int totalPageCount;
	
	public DocumentSection(){}

	public void setTotalSectionPageCount( int totalSectionPageCount ) {
		this.totalSectionPageCount = totalSectionPageCount;
	}
	
	public int getTotalSectionPageCount() {
		return totalSectionPageCount;
	}

	public void setTotalPageCount( int totalPageCount ) {
		this.totalPageCount = totalPageCount;
	}
	
	public int getTotalPageCount() {
		return totalPageCount;
	}
	
	public void setUserAgent( String userAgent ) {
	  this.userAgent = userAgent;
  }

	public String getUserAgent() {
	  return userAgent;
  }

	public void setName( String name ) {
	  this.name = name;
  }

	public String getName() {
	  return name;
  }
	
	public String getMarginBottom() {
  	return marginBottom;
  }

	public void setMarginBottom( String marginBottom ) {
  	this.marginBottom = marginBottom;
  }

	public String getMarginLeft() {
  	return marginLeft;
  }

	public void setMarginLeft( String marginLeft ) {
  	this.marginLeft = marginLeft;
  }

	public String getMarginRight() {
  	return marginRight;
  }

	public void setMarginRight( String marginRight ) {
  	this.marginRight = marginRight;
  }

	public String getMarginTop() {
  	return marginTop;
  }

	public void setMarginTop( String marginTop ) {
  	this.marginTop = marginTop;
  }

	public String getMimeType() {
  	return mimeType;
  }

	public void setMimeType( String mimeType ) {
  	this.mimeType = mimeType;
  }

	public String getAuthUser() {
  	return authUser;
  }

	public String getAuthPassword() {
  	return authPassword;
  }

	public void setAuthentication( String _user, String _password ) {
		authUser = _user;
		authPassword = _password;
  }

	public void setSources( String _src, String _srcFile, String _body ) {
		src = _src;
		srcFile = _srcFile;
		body = _body.trim();
	
		//TODO: validation
		if ( src == null && srcFile == null ){
			
		}
  }

	public String getBody(){
		return body;
	}
	
	public String getHeader() {
  	return header;
  }

	public String getHeaderAlign() {
  	return headerAlign;
  }

	public void setHeader( String header, String alignment ) {
  	this.header = header;
  	this.headerAlign = alignment;
  }

	public String getFooter() {
  	return footer;
  }

	public String getFooterAlign() {
  	return footerAlign;
  }

	public void setFooter( String footer, String alignment ) {
  	this.footer = footer;
  	this.footerAlign = alignment;
  }
	
	public String getSrc(){
		return this.src;
	}
	
	public String getSrcFile(){
		return this.srcFile;
	}
	
	@SuppressWarnings("deprecation")
	public String getBaseUrl( cfSession _Session ){
		String baseUrl = "";  
		if ( src != null ){
			baseUrl = src;
			int endIndx = baseUrl.lastIndexOf( '/' );
			if ( endIndx > 8 ){ // looking for slash beyond the protocol
				baseUrl = baseUrl.substring( 0, endIndx+1 );
			}
		}else if ( srcFile != null ){
			File file = new File( srcFile );
			if (!file.exists()){
				// If the file doesn't exist then most likely it's a relative path
				// and not an absolute path to a file.
				file = new File(_Session.getPresentDirectory(), srcFile);
			}
			try {
	      baseUrl = file.getParentFile().toURL().toString();
			} catch ( MalformedURLException e ) { // shouldn't happen
      	baseUrl = "";
      }
		}else{
			try{
				baseUrl = ( new File( _Session.getPresentDirectory() ) ).toURL().toString();
      } catch ( MalformedURLException e ) { // shouldn't happen
      	baseUrl = "";
      }
		}
		
		return baseUrl;
	}
	
	/*
	 * pageCounterConflict
	 * 
	 * OpenBD doesn't support currentpagenumber and currentsectionpagenumber in the
	 * same section so return true if they are both present.
	 */
	public boolean pageCounterConflict(){

		if ( usesCurrentPageNumber() && usesCurrentSectionPageNumber() )
			return true;
		
		return false;
	}
	
	/*
	 * usesCurrentPageNumber
	 * 
	 * Returns true if the section uses currentpagenumber.
	 */
	public boolean usesCurrentPageNumber(){

		if ( (header != null) && (header.indexOf("BD:CURRENTPAGENUMBER") != -1) )
			return true;

		if ( (footer != null) && (footer.indexOf("BD:CURRENTPAGENUMBER") != -1) )
			return true;
		
		return false;
	}
	
	/*
	 * usesCurrentSectionPageNumber
	 * 
	 * Returns true if the section uses currentsectionpagenumber.
	 */
	public boolean usesCurrentSectionPageNumber(){

		if ( (header != null) && (header.indexOf("BD:CURRENTSECTIONPAGENUMBER") != -1) )
			return true;

		if ( (footer != null) && (footer.indexOf("BD:CURRENTSECTIONPAGENUMBER") != -1) )
			return true;
		
		return false;
	}
}
