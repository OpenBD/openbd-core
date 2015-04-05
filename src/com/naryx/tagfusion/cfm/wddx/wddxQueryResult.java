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

package com.naryx.tagfusion.cfm.wddx;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;

/**
 * wrapper for queryResults in WDDX2CFML and WDDX2JS
 */

public class wddxQueryResult extends Object {
  String name;
	cfQueryResultData data;

	public wddxQueryResult( String _name, cfQueryResultData _data ){
		data = _data;
		name = _name;
	}

	public String getName(){ return name; }

	public cfQueryResultData getData(){ return data; }

	public void setCell( int rowNo, String columnName, cfData cellData ){
		data.setCell( rowNo, columnName, cellData );
	}

	public String getJSData(){ 
		StringBuilder JSString = new StringBuilder(256);
		cfData value;
		String columns[] = data.getColumnList();
    
    String objectName = "WddxRecordset";
    
		JSString.append(name + " = new " + objectName + "();\n");
		for(int cols=0; cols<data.getNoColumns(); cols++){
		 	JSString.append( "col" + cols +" = new Array();\n");
		 
		 	for ( int rows=0; rows < data.getSize(); rows++ ){	
			  value = data.getCell(rows+1, cols+1, false);
				
			  if ( value.getDataType() == cfData.CFSTRINGDATA ){
			  
			  	JSString.append( "col" + cols + "[" + rows + "]=" + wddxDataTypes.getRHSData((cfStringData)value) + ";\n");
			 	
			  }else if ( value.getDataType() == cfData.CFNUMBERDATA ){
				
				  JSString.append( "col" + cols + "[" + rows + "]=" + wddxDataTypes.getRHSData((cfNumberData)value) + ";\n");
				
			  }else if ( value.getDataType() == cfData.CFBOOLEANDATA ){
				
				  JSString.append( "col" + cols + "[" + rows + "]=" + wddxDataTypes.getRHSData((cfBooleanData)value) + ";\n");
				
				}else if( value.getDataType() == cfData.CFDATEDATA ){
					JSString.append( "col" + cols + "[" + rows + "] = " ); 
					JSString.append( wddxDataTypes.getRHSData((cfDateData)value) );
					JSString.append( "\n");
				}else if ( value.getDataType() == cfData.CFNULLDATA ){
          
          JSString.append( "col" + cols + "[" + rows + "]=null;\n");
        }
		 	}
		 
			JSString.append( name + "['" + columns[cols].toLowerCase() + "']=" + "col" + cols + ";\n");
		 	JSString.append( "col" + cols + " = null;\n");
		}
		return JSString.toString();
	}
}// wddxQueryResult
