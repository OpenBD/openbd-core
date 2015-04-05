/* 
 *  Copyright (C) 2011 TagServlet Ltd
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
 *  $Id: cfDBINFO.java 1590 2011-06-08 14:25:37Z alan $
 */

package com.naryx.tagfusion.cfm.sql.platform.java;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

/**
 * This tag is the tag version to the dbInfo() function
 */
public class cfDBINFO extends cfTag implements Serializable {

	static final long	serialVersionUID	= 1;

	public java.util.Map getInfo() {
		return createInfo("query", "This tag lets you find out information about database tables and columns");
	}

	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] { 
				createAttInfo("ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
				createAttInfo("DATASOURCE", "The name of the datasource to use", "", true), 
				createAttInfo("TYPE", "Type of query to make. Values are: dbnames, tables, columns, version, procedures, foreignkeys, index", "", true), 
				createAttInfo("PATTERN", "Pattern to filter results", "", false),
				createAttInfo("USERNAME", "Username for the database", "", false), 
				createAttInfo("PASSWORD", "Password for the database", "", false),
				createAttInfo("TABLE", "table name", "", false), 
				createAttInfo("DBNAME", "Database name", "", false) 
		};
	}


	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
	}



	public String getEndMarker() {
		return null;
	}


	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		
		if (!containsAttribute(attributes,"NAME"))
			throw newRunTimeException("missing NAME attribute");
		
		String	name	= getDynamic(attributes, _Session, "NAME").getString();
		
		cfArgStructData functionArgs = new cfArgStructData(true);
		
		if (containsAttribute(attributes,"DATASOURCE"))
			functionArgs.setData("datasource", getDynamic(attributes,_Session, "DATASOURCE"));
		else
			throw newRunTimeException("missing DATASOURCE attribute");
		
		if (containsAttribute(attributes,"TYPE"))
			functionArgs.setData("type", getDynamic(attributes,_Session, "TYPE"));
		else
			throw newRunTimeException("missing TYPE attribute");
		
		if (containsAttribute(attributes,"PATTERN"))
			functionArgs.setData("pattern", getDynamic(attributes,_Session, "PATTERN"));
		if (containsAttribute(attributes,"USERNAME"))
			functionArgs.setData("username", getDynamic(attributes,_Session, "USERNAME"));
		if (containsAttribute(attributes,"PASSWORD"))
			functionArgs.setData("password", getDynamic(attributes,_Session, "PASSWORD"));
		if (containsAttribute(attributes,"TABLE"))
			functionArgs.setData("table", getDynamic(attributes,_Session, "TABLE"));
		if (containsAttribute(attributes,"DBNAME"))
			functionArgs.setData("dbname", getDynamic(attributes,_Session, "DBNAME"));
		
		
		// run the command
		dbInfo	dbInfoFunc	= new dbInfo();
		cfData qryData = dbInfoFunc.execute(_Session, functionArgs);
		_Session.setData(name, qryData );
		return cfTagReturnType.NORMAL;
	}
}
