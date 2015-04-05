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
 *  $Id: cfPROCPARAM.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.sql;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

/**
 * cfSTOREDPROC implements the CFSTOREDPROC tag of ColdFusion.  
 */

public class cfPROCPARAM extends cfTag implements Serializable{

  static final long serialVersionUID = 1;

  @SuppressWarnings("rawtypes")
	public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
  		createAttInfo("TYPE", 		"Whether this variable is used for passing data in, or getting, data out. Valid values: in, out, inout", "in", true ),
  		createAttInfo("VALUE", 		"The value that this data represents", "", true ),
  		createAttInfo("CFSQLTYPE","The type this data should be translated to in the database (CF_SQL_BIGINT, CF_SQL_BIT, CF_SQL_CHAR, CF_SQL_BLOB, CF_SQL_CLOB, CF_SQL_DATE, CF_SQL_DECIMAL, CF_SQL_DOUBLE, CF_SQL_FLOAT, CF_SQL_IDSTAMP, CF_SQL_INTEGER, CF_SQL_LONGVARCHAR, CF_SQL_MONEY, CF_SQL_MONEY4, CF_SQL_NUMERIC, CF_SQL_REAL, CF_SQL_REFCURSOR, CF_SQL_SMALLINT, CF_SQL_TIME, CF_SQL_TIMESTAMP, CF_SQL_TINYINT, CF_SQL_VARCHAR, CF_SQL_BINARY, CF_SQL_VARBINARY, CF_SQL_NCLOB, CF_SQL_NCHAR, CF_SQL_NVARCHAR)", "", true ),
 			createAttInfo("NULL", 		"Flag to determine if this represents a database NULL", "false", false ),
 			createAttInfo("LIST", 		"Flag to determine if this represents a list", "false", false ),
 			createAttInfo("SEPARATOR","If this is a list, then this is the delimiter for the list", ",", false ),
 			createAttInfo("MAXLENGTH","The maximum length this data should be; throws an error if greater than", ",", false ),
 			createAttInfo("PADDING",	"The number of characters this data is padded out to", "", false ),
 			createAttInfo("SCALE",		"The decimal precision of this data if it is a number", "0", false ),
 			createAttInfo("DBVARNAME","The name of the dbvarname that this parameter is for", "", false ),
  	};
  }

  
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute("TYPE", 		"In" );
	  defaultAttribute("SCALE", 	"0" );
		defaultAttribute("NULL", 		"No" );
		defaultAttribute("LIST", 		"No");
		defaultAttribute("SEPARATOR", ",");
    parseTagHeader( _tag );

	  if ( !containsAttribute( "CFSQLTYPE" ) )
		  throw missingAttributeException( "cfprocparam.missingCfsqltype", null );
  }

  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
    cfStoredProcData storedProcData = (cfStoredProcData)_Session.getDataBin( cfSTOREDPROC.DATA_BIN_KEY );
 
    if( storedProcData == null )
      throw newRunTimeException( "CFPROCPARAM must be nested inside tag CFSTOREDPROC");

 		// Get the data and SQL Type
		preparedData pData = new preparedData();
		pData.setDataType( getDynamic( _Session, "CFSQLTYPE" ).getString() );
		
		// Get the NULL attribute
		boolean isNull = getDynamic( _Session, "NULL" ).getBoolean();
		pData.setPassAsNull( isNull );
		
		// Get the type
		String type = getDynamic( _Session, "TYPE" ).getString().toLowerCase();
		if ( type.indexOf("in") != -1 ){
			pData.setIN();
			boolean valueSet = containsAttribute("VALUE");
			if ( !valueSet && !isNull ){
				throw newRunTimeException( "When TYPE=IN or INOUT and NULL is not TRUE, you must provide a VALUE");
			}else if ( valueSet ){
				pData.setData( getDynamic( _Session, "VALUE" ) );
				
				// LIST must bet set after VALUE and SEPARATOR!
				if ( getDynamic(_Session, "LIST").getBoolean() ){
					pData.setList(getDynamic(_Session, "SEPARATOR").getString(), null);
				}
			}
		}
		
		if ( type.indexOf("out") != -1 ){
			pData.setOUT();
			if ( !containsAttribute("VARIABLE") )
				throw newRunTimeException( "When TYPE=OUT or INOUT, you must provide a VARIABLE");

			String var = getDynamic(_Session, "VARIABLE").getString();
			if ( var.length() == 0 )
				throw newRunTimeException("When TYPE=OUT or INOUT, you must provide a value for VARIABLE");

			pData.setOutVariable(var);
		}

		// Get the max Length
		if ( pData.getCfSqlType() == preparedData.CF_SQL_REFCURSOR ) {
			if ( containsAttribute("MAXROWS") )
				pData.setMaxLength( getDynamic( _Session, "MAXROWS" ).getInt() );
		} else {
			if ( containsAttribute("MAXLENGTH") )
				pData.setMaxLength( getDynamic( _Session, "MAXLENGTH" ).getInt() );
		}		

		// Get the Scale
		if ( containsAttribute("SCALE") )
			pData.setScale( getDynamic( _Session, "SCALE" ).getInt() );		

		if (containsAttribute("PADDING"))
			pData.setPadding(getDynamic(_Session, "PADDING").getInt());
		
		// Get the DBVARNAME
		if ( containsAttribute("DBVARNAME") )
			pData.setParamName( getDynamic( _Session, "DBVARNAME" ).getString() );		

		// Validate the data
		pData.validateData( _Session );
		storedProcData.addPreparedData( pData );
		
		return cfTagReturnType.NORMAL;
	}
}
