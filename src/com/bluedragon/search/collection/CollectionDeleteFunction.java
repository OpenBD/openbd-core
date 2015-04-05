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
 *  $Id: CollectionDeleteFunction.java 1638 2011-07-31 16:08:50Z alan $
 */


package com.bluedragon.search.collection;

import java.io.IOException;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class CollectionDeleteFunction extends functionBase {
	private static final long	serialVersionUID	= 1L;

	public CollectionDeleteFunction(){
		min = 1;
		max = 1;
		setNamedParams( new String[]{ "collection" } );
	}
	
	
  public String[] getParamInfo(){
		return new String[]{
			"the name of the collection"
		};
  }
	
  
	public java.util.Map getInfo(){
		return makeInfo(
				"search", 
				"Deletes the collection.  All files will be removed and the collection will no longer be available for use", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String name	= getNamedStringParam(argStruct, "collection", null );
		if ( name == null )
			throwException(_session, "please specifiy the 'collection' attribute" );
		
		if ( !CollectionFactory.isCollection(name) )
			throwException(_session, "collection, " + name + ", does not exist");
		
		try {
			CollectionFactory.deleteCollection(name);
		} catch (IOException e) {
			throwException(_session, "problem deleting collection:" + e.getMessage() );
		}
		
		String keyPrefix = "server.cfcollection.collection[" + name.toLowerCase() + "]";
    cfEngine.getConfig().removeData(keyPrefix);
    cfEngine.writeXmlFile(cfEngine.getConfig());

		return cfBooleanData.TRUE;
	}
}
