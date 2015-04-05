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
 *  $Id: CollectionTag.java 1638 2011-07-31 16:08:50Z alan $
 */

package com.bluedragon.search.collection;

import java.io.Serializable;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.expression.function.functionBase;


public class CollectionTag extends cfTag implements Serializable  {
	private static final long	serialVersionUID	= 1L;
	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
	}
	
	public java.util.Map getInfo(){
		return createInfo("search", "Used to manage collections that are used for searching.  This is the tag version of all the CollectionXXX() functions.");
	}

	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
			createAttInfo( "ACTION", 			"Type of action to take: create|delete|list|categorylist", 	"", true ),
			createAttInfo( "COLLECTION", 	"name of the collection.  Used for ACTION=create|delete|categorylist", "", false ),
			
			createAttInfo( "STOREBODY", "a flag to determine if the body is stored as a whole in the index.  For large collections this can take up a lot of space.  Used for ACTION=create", "false", false ),
			createAttInfo( "LANGUAGE", 	"language this collection is using. defaults to 'english'. Valid: english, german, russian, brazilian, korean, chinese, japanese, czech, greek, french, dutch, danish, finnish, italian, norwegian, portuguese, spanish, swedish.  Used for ACTION=create", "english", false ),
			createAttInfo( "PATH", 			"path to where the collection will be created.  If omitted then it will be created in the working directory under the 'cfcollection' directory.  Used for ACTION=create", "", false ),
			createAttInfo( "RELATIVE", 	"a flag to determine if the 'path' attribute, if presented, is relative to the web path.   Used for ACTION=create", "false", false ),
			
			createAttInfo( "NAME", 			"The name of the variable to store the resulting data.   Used for ACTION=list|categorylist", "", true ),
			
		};
	}

	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData	attributes = setAttributeCollection(_Session);
		
		if ( !containsAttribute(attributes, "ACTION") )
			throw newRunTimeException( "missing ACTION attribute" );
		
   	String action	= getDynamic( attributes, _Session, "ACTION" ).getString().toLowerCase();
		
   	
   	if ( action.equals("create") ){
   		
   		functionBase	func	= new CollectionCreateFunction();
   		cfArgStructData functionArgs = getFunctionArgsFromAttributes( _Session, func.getFormals(), attributes );
   		func.execute(_Session, functionArgs);

   	} else if ( action.equals("delete") ){
   		
   		functionBase	func	= new CollectionDeleteFunction();
   		cfArgStructData functionArgs = getFunctionArgsFromAttributes( _Session, func.getFormals(), attributes );
   		func.execute(_Session, functionArgs);
 		
   	} else if ( action.equals("list") ){
   		
     	if ( !containsAttribute(attributes, "name") )
     		throw newRunTimeException("must specify a NAME attribute");
   		
   		functionBase	func	= new CollectionListFunction();
   		cfData result = func.execute(_Session, (List)null);
 		
   		String name	= getDynamic(attributes, _Session, "name").getString();
      _Session.setData( name, result );
   		
   	} else if ( action.equals("categorylist") ){
   		
     	if ( !containsAttribute(attributes, "name") )
     		throw newRunTimeException("must specify a NAME attribute");
   		
   		functionBase	func	= new CollectionListCategoryFunction();
   		cfData result = func.execute(_Session, (List)null);
 		
   		String name	= getDynamic(attributes, _Session, "name").getString();
      _Session.setData( name, result );

   	} else {
   		
   		throw newRunTimeException("invalid ACTION. must be one of create|delete|list|categorylist");
   		
   	}

  	return cfTagReturnType.NORMAL;
	}
}
