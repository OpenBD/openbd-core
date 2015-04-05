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
 *  $Id: cfQueryDataFromJSon.java 1549 2011-04-24 17:39:09Z alan $
 */
package com.naryx.tagfusion.expression.function.string;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfQueryInterface;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;


public class cfQueryDataFromJSon extends cfQueryResultData implements cfQueryInterface, java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public cfQueryDataFromJSon( cfArrayData _columns, cfArrayData _data ) throws dataNotSupportedException, cfmRunTimeException{
		super("JSON");
		
		// Setup the columns
		String[] cols = new String[ _columns.size() ];
		for ( int x=0; x < cols.length; x++ )
			cols[x] = _columns.getData(x+1).getString();
		
		init( cols, null, "JSON" );
		setQueryString("{CFQUERY.JSON}");

		
		/* for each row */
		for ( int r=0; r < _data.size(); r++ ){
			addRow(1);
			setCurrentRow( getSize() );
			
			cfArrayData jsonColumnData	= (cfArrayData)_data.getData(r+1);
			for ( int c=0; c<jsonColumnData.size();c++){
				setCell( c+1, jsonColumnData.getData(c+1) );
			}
		}
	}
	
	
	
	public cfQueryDataFromJSon( cfArrayData _columns, cfStructData _data, int _rowCount ) throws dataNotSupportedException, cfmRunTimeException{
		super("JSON");

		// Setup the columns
		String[] cols = new String[ _columns.size() ];
		for ( int x=0; x < cols.length; x++ )
			cols[x] = _columns.getData(x+1).getString();
		
		init( cols, null, "JSON" );
		setQueryString("{CFQUERY.JSON}");
	
		addRow(_rowCount);
		
		/* Lets loop around the data and update the values */
		String[] colArray = getColumnList();
		for ( int c=0; c < colArray.length; c++ ){
			cfArrayData	columnArrayJson = (cfArrayData)_data.getData( colArray[c] );
			for ( int r=0; r < columnArrayJson.size(); r++ ){
				setCell( r+1, c+1, columnArrayJson.getData(r+1) );
			}
		}
	}
}
