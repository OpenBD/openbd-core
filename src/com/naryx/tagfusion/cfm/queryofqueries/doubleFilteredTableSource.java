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

/**
 * This is a tableSource that only returns rows that meet 
 * 2 condition criteria
 */
 
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
 
class doubleFilteredTableSource extends tableSource{

	condition filter1;
	condition filter2;
	
	doubleFilteredTableSource( Map<String, cfQueryResultData> _tables, condition _filter1, condition _filter2, List<cfData> _pData ){
	  super( _tables, _pData );
		filter1 = _filter1;
		filter2 = _filter2;
		
	}// filteredTableSource()
	
	
	/**
	 * this differs to its parent in that it has to work out if there are
	 * any rows left post filter 
	 * NOTE: this isn't safe really. calling hasNext() consecutively without
	 * calling nextRow() would result in a row being missed out!
	 */
	 
	boolean hasNext() throws cfmRunTimeException{
		// calculate next
		while ( super.hasNext() ){
			super.nextRow();
			if ( filter1.evaluate( currentRow, pData ) && filter2.evaluate( currentRow, pData ) ){
				return true;
			}
		}
		return false;
	} 

	
	rowContext nextRow() {
		return currentRow;
	}// nextRow()
	
	
}// doubleFilteredTableSource
