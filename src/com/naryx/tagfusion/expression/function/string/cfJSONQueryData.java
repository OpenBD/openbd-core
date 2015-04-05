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
package com.naryx.tagfusion.expression.function.string;

import org.json.JSONArray;
import org.json.JSONObject;

import com.naryx.tagfusion.cfm.engine.cfQueryInterface;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;

public class cfJSONQueryData extends cfQueryResultData implements cfQueryInterface, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	public cfJSONQueryData( JSONObject jsonObject ) throws Exception {
		super("JSON");
		
		executeTime	= System.currentTimeMillis();
		
		/* Sort out the columns */
		JSONArray columnArray = null;
		try{
			columnArray = jsonObject.getJSONArray("COLUMNS");
		}catch(Exception e){
			columnArray = jsonObject.getJSONArray("columns");
		}
		
		String[] cols = new String[ columnArray.length() ];
		for ( int x=0; x < cols.length; x++ )
			cols[x] = columnArray.getString(x);
		
		init( cols, null, "JSON" );
		setQueryString("{CFQUERY.JSON}");
		
		
		/* Determine which type of query */
		try{
			jsonObject.getInt("ROWCOUNT");
		}catch(Exception e){
			try{
				jsonObject.getInt("rowcount");
			}catch(Exception ee){
				extractTypeOne( jsonObject );
				return;
			}
		}
		
		extractTypeTwo( jsonObject );
	}

	
	
	private void extractTypeTwo(JSONObject jsonObject) throws Exception {
		boolean bUsingUCase = true;
		JSONObject jsonRows;
		try{
			jsonRows = jsonObject.getJSONObject("DATA");
		}catch(Exception e){
			jsonRows = jsonObject.getJSONObject("data");
			bUsingUCase = false;
		}
		
		
		/* Create the table */
		if ( bUsingUCase )
			addRow( jsonObject.getInt("ROWCOUNT") );
		else
			addRow( jsonObject.getInt("rowcount") );
		
		
		/* Lets loop around the data and update the values */
		String[] colArray = getColumnList();
		for ( int c=0; c < colArray.length; c++ ){
			JSONArray	columnArrayJson = jsonRows.getJSONArray( colArray[c] );
			for ( int r=0; r < columnArrayJson.length(); r++ ){
				setCell( r+1, c+1, new cfStringData( columnArrayJson.getString(r) ) );
			}
		}
		
		executeTime = System.currentTimeMillis() - executeTime;
	}
	
	
	
	private void extractTypeOne(JSONObject jsonObject) throws Exception {
		JSONArray jsonRows;
		try{
			jsonRows = jsonObject.getJSONArray("DATA");
		}catch(Exception e){
			jsonRows = jsonObject.getJSONArray("data");
		}
		
		/* for each row */
		for ( int r=0; r < jsonRows.length(); r++ ){
			addRow(1);
			setCurrentRow( getSize() );
			
			JSONArray jsonColumnData	= jsonRows.getJSONArray(r);
			for ( int c=0; c<jsonColumnData.length();c++){
				setCell( c+1, new cfStringData( jsonColumnData.getString(c) ) );
			}
		}
		
		executeTime = System.currentTimeMillis() - executeTime;
	}

}
