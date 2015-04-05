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

public class cfARGUMENT extends cfTag implements Serializable {
	static final long serialVersionUID = 1;
	
	public java.util.Map getInfo(){
		return createInfo("control", "Used in cffunction definitions to indicate the expected/required parameters for the function.");
	}
  
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo("NAME", "Provide a name for the argument, so it can be later referenced within the function.", "", true ),
				createAttInfo("TYPE", "By providing a specific type to the expected argument (e.g. QUERY, STRUCT), an error will be thrown if the wrong type is passed in.", "ANY", false ),
				createAttInfo("REQUIRED", "If true then this argument must be provided when the function is called otherwise an error will be thrown.", "NO", false ),
				createAttInfo("HINT", "A small description for what this argument.  Used in the meta data", "NO", false ),
				createAttInfo("DEFAULT", "If no parameter is present, then this is the default value for the parameter", "NO", false )
		};

	}

	protected void defaultParameters( String tagName ) throws cfmBadFileException {
		// default attributes
		defaultAttribute( "TYPE", "ANY" );
		defaultAttribute( "REQUIRED", "NO" );
		
		parseTagHeader( tagName );
		
		// required attributes
		requiredAttribute( "NAME" );
		
		// constant attributes
		constantAttribute( "NAME" );
		constantAttribute( "TYPE" );
		constantAttribute( "REQUIRED" );
	}
	
	/**
	 * This is for files that have been loaded from a BDA. This ensures
	 * all the tagLoadedComplete() methods are called to initialise any
	 * values.
	 */
	public void reInitialiseTags() {
		// we don't want to invoke tagLoadingComplete() for CFARGUMENT
		// because that will cause argument metadata to get added to the
		// parent cfFUNCTION more than once
		return;
	}
	
	protected void tagLoadingComplete() throws cfmBadFileException {
		// CFARGUMENT is only allowed within CFFUNCTION
		if ( !isSubordinate( "CFFUNCTION" ) ) {
			throw newBadFileException( "Illegal Nesting", "CFARGUMENT must be nested within CFFUNCTION" );			
		}
		
		try {
			// give argument metadata to the parent CFFUNCTION tag
			((cfFUNCTION)parentTag).addArgument( getConstant( "NAME" ), super.getMetaData(false) );
		} catch ( IllegalArgumentException e ) {
			throw newBadFileException( "Duplicate argument name", "The NAME attribute must be unique for each CFARGUMENT tag" );
		}
	}

	public cfTagReturnType render( cfSession session ) {
		// Because of UDF forward references, the cfFUNCTION.render() method might not be
		// invoked before the function is executed, which means this method won't be invoked
		// either. Therefore, this method should never do anything.
		return cfTagReturnType.NORMAL;
	}
}
