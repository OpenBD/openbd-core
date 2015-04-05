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

import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * This class represents the value of a <CFQUERYPARAM> when used within a query of queries.
 */

/* TODO: This class can be refactored along with preparedData. It uses much of preparedData functionality
 * but doesn't require a number of methods e.g. prepareStatement. 
 */
public class qoqPreparedData extends preparedData implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	public qoqPreparedData() throws cfmRunTimeException {
		super();
	}
	
	public String toString(){
		return "Type=" + sqlType + "; Data=" + data + ";";
	}

	
	//----------------------------------------------------

	/*public String getDataString() throws cfmRunTimeException
	{
		if ( passAsNull )
			return "NULL";

		StringBuffer sb = new StringBuffer();
		Iterator iter = data.iterator();
		
		while ( iter.hasNext() )
		{
			cfData _data = (cfData)iter.next();
			
			switch ( cfSqlType )
			{	
				case CF_SQL_VARCHAR :
				case CF_SQL_CHAR :
				case CF_SQL_LONGVARCHAR :
					String tmp = _data.getString();
					if ( ( maxLength != -1 ) && ( tmp.length() > maxLength ) ) {
						throw newRunTimeException( "The field was type CF_SQL_VARCHAR, but was longer than the maxLength" );
					}
					sb.append( "'" );
					sb.append( tmp );
					sb.append( "'" );
					break;
					
				case CF_SQL_SMALLINT :
				case CF_SQL_INTEGER :
				case CF_SQL_TINYINT :
					sb.append( _data.getInt() );
					break;
					
				case CF_SQL_BLOB :
				case CF_SQL_CLOB :
					if ( _data.getDataType() != cfData.CFBINARYDATA ) {
						throw newRunTimeException( "The field is not a BINARY" );
					}
					//TODOAW: add handlign sb.append( _data.)
					break;
					
				case CF_SQL_BIGINT :
					sb.append( _data.getLong() );
					break;
					
				case CF_SQL_DOUBLE :
				case CF_SQL_DECIMAL :
				case CF_SQL_FLOAT :
				case CF_SQL_REAL :
				case CF_SQL_NUMERIC :
					sb.append( _data.getDouble() );
					break;
					
				case CF_SQL_DATE :
				case CF_SQL_TIME :
				case CF_SQL_TIMESTAMP :
					//data.set( data.indexOf( _data ), _data.getDateData() );
					//TODOAW: add handling
					sb.append( _data.getString() );
					break;
			}
			
			if ( iter.hasNext() ) sb.append( ",");
		}
		return sb.toString();
	}*/

		
}
