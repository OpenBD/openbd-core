/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: urlFromStruct.java 2387 2013-06-17 02:07:50Z alan $
 */

package com.naryx.tagfusion.expression.function;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.string.serializejson;

public class urlFromStruct extends functionBase {

	private static final long serialVersionUID = 1L;

	public urlFromStruct() {
		min = 1; max = 2;
		setNamedParams( new String[]{ "struct", "encoding" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"struct to which to create the URL key=value from",
			"encoding to use, defaults to that of the request"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"format", 
				"Creates a URL key/value encoded string from the structure, with key1=value1&key2=value2.  If the structure contains a rich object, it will be JSON encoded.", 
				ReturnType.STRING );
	}
  
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		cfData	data	= getNamedParam(argStruct, "struct", null );
		if ( data == null || data.getDataType() != cfData.CFSTRUCTDATA )
			throwException(_session, "Missing or invalid parameter 'struct'");
		
		String encoding = getNamedStringParam(argStruct, "encoding", _session.RES.getCharacterEncoding() );

		
		cfStructData	sdata	= (cfStructData)data;
		StringBuilder	sb	= new StringBuilder( 2056 );
		
		try{
			Iterator<String>	it	= sdata.keySet().iterator();
			while ( it.hasNext() ){
				String key	= it.next();
				
				sb.append( com.nary.net.http.urlEncoder.encode(key, encoding) );
				sb.append( "=" );
				
				cfData value	= sdata.getData(key);
				if ( cfData.isSimpleValue(value) )
					sb.append( com.nary.net.http.urlEncoder.encode( value.getString(), encoding) );
				else{
					StringBuilder buffer = new StringBuilder(5000);
					new serializejson().encodeJSON(buffer, value, false, serializejson.getCaseType(null), serializejson.getDateType(null) );
					sb.append( com.nary.net.http.urlEncoder.encode( buffer.toString(), encoding) );
				}
	
				sb.append("&");
			}
		}catch(UnsupportedEncodingException e){
			throwException(_session, e.getMessage());
		}

		if ( sb.length() > 0 )
			sb.deleteCharAt( sb.length()-1 );
		
		return new cfStringData( sb.toString() );
	}
}
