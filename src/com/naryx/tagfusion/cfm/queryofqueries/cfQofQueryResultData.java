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

package com.naryx.tagfusion.cfm.queryofqueries;

import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.sql.preparedData;


public class cfQofQueryResultData extends cfSQLQueryData implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public cfQofQueryResultData(){
		super( "Query of Queries" );
	}

	public void execute( cfSession _Session ) throws cfmRunTimeException {    
		long executeStart = System.currentTimeMillis();
    
		// process the queryParams
		List<cfData> queryParamValues = new ArrayList<cfData>();
		preparedData nextData;
		if ( preparedDataList != null ){
			for ( int i = 0; i < preparedDataList.size(); i++ ){
				nextData = (preparedData) preparedDataList.get(i);
				for ( int j = 0; j < nextData.getSize(); j++ ){
					queryParamValues.add( nextData.getData(j) );
				}
			}
		}
		
		selectStatement select = qoqCache.getStatement( getQueryString(), queryParamValues.size() );
    cfQueryResultData result = select.execute( _Session, queryParamValues );
    
    init( result.getColumnList(), result.getColumnTypes(), "Query of Queries" );
    List<List<cfData>> resultData = result.getQueryTableData();
    if ( maxRows > 0 && resultData.size() > maxRows ){
      int resultSize = resultData.size();
      for ( int i = resultSize - 1; i >= maxRows; i-- ){
        resultData.remove( i );
      }
    }
    setQueryData( resultData );

    this.executeTime = System.currentTimeMillis() - executeStart;
    
	}

	public boolean hasResultSet(){
		return true;
	}
	
	protected String getExtraInfo( boolean _isLong ){
		return "";
	}
	
}
