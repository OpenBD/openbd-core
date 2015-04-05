/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  
 *  $Id: SearchTag.java 1638 2011-07-31 16:08:50Z alan $
 */

package com.bluedragon.search.search;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;


public class SearchTag extends cfTag implements Serializable  {
	private static final long	serialVersionUID	= 1L;
	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
	}

	public java.util.Map[] getAttInfo(){
		java.util.Map[]	tagParams	= makeAttInfoFromFunction( new SearchFunction(), 2 );
				
		// Add in the one we need for the tag
		tagParams[tagParams.length-1] = createAttInfo( "NAME", "the name of the parameter to return the query results as", 	"", true );
		tagParams[tagParams.length-2] = createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false );
		
		return tagParams;
	}
	
  public java.util.Map getInfo(){
  	return getInfo( new SearchFunction() );
  }

	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData	attributes = setAttributeCollection(_Session);
		
   	SearchFunction	sF	= new SearchFunction();
   	cfArgStructData functionArgs = getFunctionArgsFromAttributes( _Session, sF.getFormals(), attributes );
   	if ( !containsAttribute(attributes, "name") )
   		throw newRunTimeException("must specify a NAME attribute");
  	
   	// Execute the function
   	cfData results = sF.execute(_Session, functionArgs);

   	// Tag version requires that we set a variable
   	String name	= getDynamic(attributes, _Session, "name").getString();
    _Session.setData( name, results );
   	
  	return cfTagReturnType.NORMAL;
	}
}
