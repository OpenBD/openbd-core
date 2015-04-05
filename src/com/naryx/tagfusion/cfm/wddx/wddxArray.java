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

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * wrapper for arrays in WDDX2CFML
 */

public class wddxArray extends Object {
	String name;
	cfArrayData data;

	public wddxArray( String _name, cfArrayData _data ){
		data = _data;
		name = _name;
	}

	public String getName(){ return name; }

	public cfArrayData getData(){ return data; }
	
	public void addElement( cfData _data ) throws cfmRunTimeException { data.addElement( _data ); }

	public String getJSData(){
		StringBuilder JSString = new StringBuilder(256);
		cfData value;

		JSString.append( name + " = new Array();\n" );
		for(int i=0; i<data.size(); i++){
			value  = data.getElement(i+1);
			
			if ( value == null ){
				
				JSString.append( name + "[" + i + "]=\"\";\n" );
				
			}	else if (value.getDataType() == cfData.CFSTRINGDATA){

				JSString.append( name + "[" + i + "]=" );
				JSString.append( wddxDataTypes.getRHSData((cfStringData)value) );
				JSString.append( ";\n" );	

			} else if (value.getDataType() == cfData.CFNUMBERDATA){
			
				JSString.append( name + "[" + i + "]=" );
				JSString.append( wddxDataTypes.getRHSData((cfNumberData)value) );
				JSString.append( ";\n" );

			} else if (value.getDataType() == cfData.CFBOOLEANDATA){
			
				JSString.append( name + "[" + i + "]=" );
				JSString.append( wddxDataTypes.getRHSData((cfBooleanData)value) );
				JSString.append( ";\n" );

			} else if (value.getDataType() == cfData.CFSTRUCTDATA){

				JSString.append( new wddxStruct( name + "["+i+"]", (cfStructData)value ).getJSData() );

			} else if (value.getDataType() == cfData.CFARRAYDATA){

				JSString.append( new wddxArray( name + "["+i+"]", (cfArrayData)value ).getJSData() );

			} else if(value.getDataType() == cfData.CFQUERYRESULTDATA){

				JSString.append( new wddxQueryResult( name + "["+i+"]", (cfQueryResultData)value ).getJSData() );

			} else if (value.getDataType() == cfData.CFDATEDATA){
				
				JSString.append( name + "[" + i + "]=" ); 
				JSString.append( wddxDataTypes.getRHSData((cfDateData)value) );
				JSString.append( ";\n");
				
		 	}
	  }

		return JSString.toString();
	} 

}// wddxArray
