/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: cfQUERYPARAM.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.sql;

import java.io.Serializable;
import java.util.Stack;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.queryofqueries.cfQofQueryResultData;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfQUERYPARAM extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

  public java.util.Map<String,String> getInfo(){
  	return createInfo(
  			"database", 
  			"For use within CFQUERY/CFSTOREDPROC tags to describe dynamic variable data that is passed to the query");
  }
  
  @SuppressWarnings("rawtypes")
	public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
  		createAttInfo("VALUE", 				"The value that this data represents", "", true ),
  		createAttInfo("CFSQLTYPE",		"The type this data should be translated to in the database (CF_SQL_BIGINT, CF_SQL_BIT, CF_SQL_CHAR, CF_SQL_BLOB, CF_SQL_CLOB, CF_SQL_DATE, CF_SQL_DECIMAL, CF_SQL_DOUBLE, CF_SQL_FLOAT, CF_SQL_IDSTAMP, CF_SQL_INTEGER, CF_SQL_LONGVARCHAR, CF_SQL_MONEY, CF_SQL_MONEY4, CF_SQL_NUMERIC, CF_SQL_REAL, CF_SQL_REFCURSOR, CF_SQL_SMALLINT, CF_SQL_TIME, CF_SQL_TIMESTAMP, CF_SQL_TINYINT, CF_SQL_VARCHAR, CF_SQL_BINARY, CF_SQL_VARBINARY, CF_SQL_NCLOB, CF_SQL_NCHAR, CF_SQL_NVARCHAR)", "", true ),
 			createAttInfo("NULL", 				"Flag to determine if this represents a database NULL", "false", false ),
 			
 			createAttInfo("LIST", 				"Flag to determine if this represents a list", "false", false ),
 			createAttInfo("SEPARATOR",		"If this is a list, then this is the delimiter for the list", ",", false ),
 			createAttInfo("DEFAULTLIST",	"If the list is empty this then this is the default value", "", false ),

 			createAttInfo("MAXLENGTH",		"The maximum length this data should be; throws an error if greater than", "-1", false ),
 			createAttInfo("PADDING",			"The number of characters this data is padded out to", "", false ),
 			createAttInfo("SCALE",				"The decimal precision of this data if it is a number", "0", false ),
  	};
  }

	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
	}

	@SuppressWarnings("rawtypes")
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		// Get the query data in which this relates to
		Stack queryStack = (Stack) _Session.getDataBin(cfQUERY.DATA_BIN_KEY);
		if (queryStack == null || queryStack.size() == 0)
			throw newRunTimeException("CFQUERYPARAM must be used inside a CFQUERY");

		Object queryData = queryStack.peek();
		if (queryData instanceof cfQofQueryResultData) {
			renderQofQueries(_Session, (cfQofQueryResultData) queryData);
		} else {
			renderQuery(_Session, (cfSQLQueryData) queryData);
		}
		return cfTagReturnType.NORMAL;
	}

	private void renderQuery(cfSession _Session, cfSQLQueryData queryData) throws cfmRunTimeException {
		// Get the data
		preparedData pData = new preparedData();
		pData.setIN();
		
		boolean bNull	= ( containsAttribute("NULL") ? getDynamic(_Session, "NULL").getBoolean() : false );

		// Look to see if this is a call to do a NULL set
		if (bNull) {
			pData.setPassAsNull(true);

			if (containsAttribute("CFSQLTYPE"))
				pData.setDataType(getDynamic(_Session, "CFSQLTYPE").getString());

			pData.setData(cfNullData.NULL);

		} else {

			if (!containsAttribute("VALUE"))
				throw newRunTimeException("VALUE attribute missing for CFQUERYPARAM");
			
			cfData value = getDynamic(_Session, "VALUE");
			pData.setData(value);

			if (containsAttribute("CFSQLTYPE"))
				pData.setDataType(getDynamic(_Session, "CFSQLTYPE").getString());

			if (containsAttribute("PADDING"))
				pData.setPadding(getDynamic(_Session, "PADDING").getInt());

			if ( containsAttribute("SCALE") )
				pData.setScale(getDynamic(_Session, "SCALE").getInt());
			else
				pData.setScale(0);
				
			if ( containsAttribute("MAXLENGTH") )
				pData.setMaxLength(getDynamic(_Session, "MAXLENGTH").getInt());
			else
				pData.setMaxLength(-1);

			// LIST must be set after VALUE and SEPARATOR!
			boolean isList	= containsAttribute("LIST") ? getDynamic(_Session, "LIST").getBoolean() : false; 
			
			if ( isList ){
				cfData	defaultList = null;
				
				if ( containsAttribute("DEFAULTLIST") )
					defaultList = getDynamic(_Session, "DEFAULTLIST");
				
				String separator = ",";
				if ( containsAttribute("SEPARATOR") )
					separator = getDynamic(_Session, "SEPARATOR").getString();
				
				pData.setList( separator, defaultList );
			}

			// Validate the data
			pData.validateData(_Session);
		}

		queryData.addPreparedData(pData);

		// Finally write out the string
		_Session.write(pData.getQueryString());
	}

	private void renderQofQueries(cfSession _Session, cfQofQueryResultData queryData) throws cfmRunTimeException {
		preparedData pData = new preparedData();
		boolean bNull	= ( containsAttribute("NULL") ? getDynamic(_Session, "NULL").getBoolean() : false );
		
		// Look to see if this is a call to do a NULL set
		if (bNull) {
			pData.setPassAsNull(true);
			pData.setData(cfNullData.NULL);
		} else {

			if (!containsAttribute("VALUE")) {
				throw newRunTimeException("VALUE attribute missing for CFQUERYPARAM");
			}

			if (containsAttribute("CFSQLTYPE"))
				pData.setDataType(getDynamic(_Session, "CFSQLTYPE").getString());

			pData.setData(getDynamic(_Session, "VALUE"));
			
			if ( containsAttribute("SCALE") )
				pData.setScale(getDynamic(_Session, "SCALE").getInt());
			else
				pData.setScale(0);
			
			if ( containsAttribute("MAXLENGTH") )
				pData.setMaxLength(getDynamic(_Session, "MAXLENGTH").getInt());
			else
				pData.setMaxLength(-1);

			// LIST must be set after VALUE and SEPARATOR!
			boolean isList	= containsAttribute("LIST") ? getDynamic(_Session, "LIST").getBoolean() : false; 
			
			if ( isList ){
				cfData	defaultList = null;
				
				if ( containsAttribute("DEFAULTLIST") )
					defaultList = getDynamic(_Session, "DEFAULTLIST");
				
				String separator = ",";
				if ( containsAttribute("SEPARATOR") )
					separator = getDynamic(_Session, "SEPARATOR").getString();
				
				pData.setList( separator, defaultList );
			}
			
			// Validate the data
			pData.validateData(_Session);
		}

		queryData.addPreparedData(pData);

		// Finally write out the string
		_Session.write(pData.getQueryString());
	}
}
