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
 * This class holds all the information pertaining to a mail part 
 */
 
package com.naryx.tagfusion.cfm.mail;

public class cfMailPartData extends Object {

	private String	mimeType;
	private String	content;
	private String	charSet;
	private int			wrapText = -1;
	
	public cfMailPartData(){
	}

	public String getCharSet() {
		return charSet;
	}

	public String getContent(int wrap) {
		if ( this.wrapText != -1 )
			return com.nary.util.string.wrap( content, this.wrapText, false );
		else if ( wrap != -1 )
			return com.nary.util.string.wrap( content, wrap, false );
		else
			return content;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setCharSet(String string) {
		charSet = string;
	}

	public void setContent(String string) {
		content = string;
	}

	public void setMimeType(String string) {
		mimeType = string.toLowerCase().trim();
	}

	public void wrapContent( int columns ){
		//-- Wraps the content
		wrapText	= columns;
	}
}
