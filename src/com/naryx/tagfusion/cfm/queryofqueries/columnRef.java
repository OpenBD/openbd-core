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
 * This class represents a columnRef.
 * It is slightly different to selectColumn as it could occur in a function.
 */
 
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
 
public class columnRef extends expression {
	
	String table = null;
	String colName = null;
	boolean tableNamed = false;
	
	columnRef( String _col ){
		colName = _col;
	}// columnRef()
	
	columnRef( String _table, String _col ){
		table = _table;
		colName = _col;
		tableNamed = true;
	}// columnRef()
		
	
	public cfData evaluate( rowContext _rowContext, List<cfData> _preparedData ){
		if ( !tableNamed ){
			return _rowContext.get( colName );
		}else{
			return _rowContext.get( table, colName );
		}
		
	}// evaluate()

	public cfData evaluate( ResultRow _row, List<cfData> data, Map<String, Integer> _indexLookup ) throws cfmRunTimeException {
		if ( !tableNamed ){
			return _row.get( _indexLookup, colName );
		}else{
			return _row.get( _indexLookup, table, colName );
		}
	}

	
	public boolean equals( Object _obj ){
		return this.toString().equals( _obj.toString() );
	}
	
	public String toString(){
		if ( table == null ){
			return colName;
		}else{
			return table + "." + colName;
		}
		
	}// toString()
	
} // columnRef
