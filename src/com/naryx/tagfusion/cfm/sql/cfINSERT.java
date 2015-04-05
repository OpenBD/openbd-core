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

package com.naryx.tagfusion.cfm.sql;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nary.db.metaColumn;
import com.nary.db.metaDatabase;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfINSERT extends cfUPDATE implements Serializable{
	
	static final long serialVersionUID = 1;
  
  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  
  	//--[ Create the cfDataSource
	String sDATASOURCE;
	cfDataSource dataSource = null;
	if ( containsAttribute("DATASOURCE") )
	{
		sDATASOURCE = getDynamic(_Session, "DATASOURCE").getString();
		dataSource = new cfDataSource(sDATASOURCE, _Session);
	}
	else
	{
		sDATASOURCE = "DYNAMIC";
		dataSource = new cfDynamicDataSource(sDATASOURCE, _Session, getDynamic(_Session, "CONNECTSTRING").getString());
	}
  
 	//--[ Set any additional parameters
  	//--[ Get the data connection
    if ( containsAttribute("USERNAME") )
			dataSource.setUsername( getDynamic( _Session, "USERNAME" ).getString() );
    
    if ( containsAttribute("PASSWORD") )
			dataSource.setPassword( getDynamic( _Session, "PASSWORD" ).getString() );
   
	 
		Connection dataConnection	= setupDataConnection( dataSource );

	
		//--[ Get the information regarding the database table
		String tableName	 = getDynamic( _Session, "TABLENAME" ).getString();
		
		Map<String, metaColumn> HT = metaDatabase.getColumns( dataConnection, dataSource.getCatalog(), tableName );
		if ( HT == null || HT.size() == 0 ){
			dataSource.returnConnection( dataConnection );		  
      if ( !metaDatabase.tableExist( dataConnection, dataSource.getCatalog(), tableName ) ){
    	if ( dataSource.getCatalog() != null )
    	  throw newRunTimeException( "Database table [" + tableName + "] in catalog [" + dataSource.getCatalog() + "] does not exist." );
    	else
    	  throw newRunTimeException( "Database table [" + tableName + "] does not exist." );
      }else{
        throw newRunTimeException( sDATASOURCE + " database does not fully support JDBC" );
      }
    }

		//--[ Build and validate the list of parameters
    String fieldList[]  = buildFieldList( HT, getDynamic( _Session, "FORMFIELDS").getString() );
		HT = populateData( _Session, HT, fieldList, true );
		List<metaColumn> columnList	= metaDatabase.sortColumns( HT );

		
		//--[ Build up Query String
		try{
			String updateString	= prepareInsertQueryString( columnList, tableName );
			PreparedStatement	pStatmt	= prepareStatement( dataConnection, updateString, columnList );
			pStatmt.executeUpdate();
			pStatmt.close();
			//--[ debug reporting
			_Session.recordInsert( getFile(), sDATASOURCE, updateString);
		}catch(Exception E){
			dataSource.returnConnection( dataConnection );
			throw newRunTimeException( sDATASOURCE + ", caused an error: " + E );		
		}

		//--[ Push the connection back to the database
		dataSource.returnConnection( dataConnection );
		
		return cfTagReturnType.NORMAL;
  }

	protected String prepareInsertQueryString( List<metaColumn> fieldList, String tableName ){
		StringBuilder SB	= new StringBuilder( 32 );
		SB.append( "INSERT INTO " );
		SB.append( tableName );
		SB.append( " (" );

		metaColumn MC;
		String VALUES = "", Q = "";
		
		Iterator<metaColumn> iter = fieldList.iterator();
		while ( iter.hasNext() ){
			MC	= iter.next();
			VALUES += MC.NAME + ",";
			Q += "?,";
		}
		
		if ( VALUES.length() > 0 )
			VALUES	= VALUES.substring( 0, VALUES.length()-1 );
			
		if ( Q.length() > 0 )
			Q	= Q.substring( 0, Q.length()-1 );

		SB.append( VALUES + ") VALUES (" );
		SB.append( Q + ")" );
		return SB.toString();
	}
}
