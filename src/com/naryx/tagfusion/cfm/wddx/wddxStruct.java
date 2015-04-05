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

import java.util.Iterator;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;

/**
 * wrapper for structs in WDDX2CFML
 */

public class wddxStruct extends Object {
	String name;
	Map data;

	public wddxStruct( String _name, Map _data ){
		name	= _name;
		data	= _data;
	}
	
	public wddxStruct( String _name, cfStructData _data ){
		this( _name, _data.copy() );
	}	

	public String getName(){ return name; }

	public Map getData(){ return data; }
	
	public void put( String _name, cfData _data ){ data.put( _name.toLowerCase(), _data ); }
		
	public String getJSData() { 
		StringBuilder JSString = new StringBuilder(256);
		
		JSString.append( name + " = new Object();\n" );
		
		cfData temp;
		String var;
		Iterator keys = data.keySet().iterator();
		
		while(keys.hasNext()){
			var  = (String)keys.next();
			temp = (cfData)data.get(var);
			
			var		= var.toLowerCase();
			
			if (temp.getDataType() == cfData.CFSTRINGDATA){

				JSString.append( name + "[\"" + var + "\"]=" );
				JSString.append( wddxDataTypes.getRHSData((cfStringData)temp) );
				JSString.append( ";\n" );

			} else if (temp.getDataType() == cfData.CFNUMBERDATA){
				JSString.append( name + "[\"" + var + "\"]=" );
				JSString.append( wddxDataTypes.getRHSData((cfNumberData)temp) );
				JSString.append( ";\n" );

			} else if (temp.getDataType() == cfData.CFBOOLEANDATA){

				JSString.append( name + "[\"" + var + "\"]=" );
				JSString.append( wddxDataTypes.getRHSData((cfBooleanData)temp) );
				JSString.append( ";\n" );

			} else if (temp.getDataType() == cfData.CFDATEDATA){

				JSString.append( name + "[\"" + var + "\"]=" );
				JSString.append( wddxDataTypes.getRHSData((cfDateData)temp) );
				JSString.append( "\n" );

			}	else if (temp.getDataType() == cfData.CFSTRUCTDATA) {

				JSString.append( new wddxStruct( name + "[\"" + var + "\"]", (cfStructData)temp ).getJSData() );

			} else if (temp.getDataType() == cfData.CFARRAYDATA){

				JSString.append( new wddxArray( name + "[\"" + var + "\"]", (cfArrayData)temp ).getJSData() );
				
			} else if (temp.getDataType() == cfData.CFQUERYRESULTDATA){
				
				JSString.append( new wddxQueryResult( name + "[\"" + var + "\"]", (cfQueryResultData)temp ).getJSData() );

			}
	  }

		return JSString.toString();
	}

}// wddxStruct
