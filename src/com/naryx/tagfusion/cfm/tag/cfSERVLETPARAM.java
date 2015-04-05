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

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfSERVLETPARAM extends cfTag implements Serializable{

  static final long serialVersionUID = 1;

	protected void defaultParameters( String _tag ) throws cfmBadFileException{
		parseTagHeader( _tag );
		
		if ( !containsAttribute("NAME") )
			throw newBadFileException( "Missing Attribute", "You must specify a NAME");
		
		if ( !containsAttribute("VALUE") && !containsAttribute("VARIABLE") )
			throw newBadFileException( "Missing Attribute", "You must specify either a VALUE or VARIABLE");
			
		if ( containsAttribute("VARIABLE") && !containsAttribute("TYPE") )
			defaultAttribute( "TYPE", "String" );
	}
	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		cfStructData	servletData	= (cfStructData)_Session.getData( "cfservlet" );
		if ( servletData == null )
			throw newRunTimeException( "You can only use this tag inside a CFSERVLET tag" );
		
		//---[ Do the switch depending on which type of data we are passing
		if ( containsAttribute("VALUE") ){
			cfStructData	valueData	= (cfStructData)servletData.getData("value");
			valueData.setData( getDynamic(_Session,"NAME").getString(), getDynamic(_Session,"VALUE") );
		}else{
			throw newRunTimeException( "The NAME/VARIABLE feature is not yet supported" );			
		}
		
		return cfTagReturnType.NORMAL;
	}
}	
