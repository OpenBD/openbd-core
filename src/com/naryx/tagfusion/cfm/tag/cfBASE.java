/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfBASE extends cfTag implements Serializable
{
	
	static final long serialVersionUID = 1;

	@SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
		return createInfo("output", "Used to generate <BASE> tag which is used to specify " +
																"default URL and target for all links on a page.");
	}

	@SuppressWarnings("unchecked")
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo("TARGET", "The default target for a link in the page. e.g. _blank", "", false )
		};
	}
  
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		parseTagHeader( _tag );
	}

	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {

		StringBuilder buffer = new StringBuilder( 32 );
		
		buffer.append( "<BASE HREF=\"" );
		
		buffer.append( _Session.REQ.getScheme() );
		buffer.append( "://" );
		buffer.append( _Session.REQ.getServerName() );
		buffer.append( ":" );
		buffer.append( _Session.REQ.getServerPort() );
		buffer.append( _Session.REQ.getContextPath() );
		buffer.append( "/\" " );
		
		if ( containsAttribute( "TARGET" ) ){
			buffer.append( "TARGET=\"" );
			buffer.append( getConstant( "TARGET" ) );
			buffer.append( "\" " );
		}
		
		buffer.append( ">" );
		
		_Session.write( buffer.toString() );
		
		return cfTagReturnType.NORMAL;
	}
}
