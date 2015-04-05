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
  * This class represents a column within an ORDER BY.
	* It can be of either type identifier (a named column) or an
	* integer (an index to a column)
  */


public class orderByCol{

	private boolean asc; // indicates the type of ordering upon this column i.e. true => ASCENDING, false => DESCENDING
	
	private String colName = "";
	private int colIndex = -1;
	private boolean isIndex = false; // indicates whether this is an indexed column as opposed to a named one
	

	// this creates an orderByCol that is an index to a column as opposed
	// to an actual named column 
	public orderByCol( int _colIndex, boolean _asc ){
		colIndex = _colIndex;
		isIndex = true;
		asc = _asc;
	}// orderByCol

	
	// this creates an orderByCol that is an actual named column 
	public orderByCol( String _colName, boolean _asc ){
		colName = _colName.toLowerCase();
		asc = _asc;
	}// orderByCol

	
	public boolean isAscending(){
		return asc;
	}
	
	public boolean isIndex(){
		return isIndex;
	}
	
	public int getIndex(){
		return colIndex;
	}
	
	public String getColName(){
		return colName;
	}
  
  public void setColName( String _colName ){
    colName = _colName;
  }
  
  public orderByCol copy(){
    if ( isIndex ){
      return new orderByCol( colIndex, asc );
    }else{
      return new orderByCol( colName, asc );
    }
  }
	
}// orderByCol
