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

package com.naryx.tagfusion.cfm.queryofqueries;

import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;

public class ResultRow {

	private List<cfData> selectedCols; // holds the values for the selected columns
	private List<cfData> orderByCols;  // holds the values to be ordered by
	
	public ResultRow( List<cfData> _selCols, List<cfData> _orderCols ){
		selectedCols = _selCols;
		orderByCols = _orderCols;
	}
	
	public List<cfData> getSelectedColumns(){
		return selectedCols;
	}

	public List<cfData> getOrderByColumns(){
		return orderByCols;
	}
	
	public cfData get( Map<String, Integer> _indexLookup, String _table, String _col ){
		return selectedCols.get( _indexLookup.get( _table + "." + _col ).intValue() );
	}
	
	public cfData get( Map<String, Integer> _indexLookup, String _col ){
		Integer indx = _indexLookup.get( _col );
		if ( indx != null )
			return selectedCols.get( indx.intValue() );
		else
			return null;
	}
}
