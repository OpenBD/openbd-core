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

package com.naryx.tagfusion.expression.function;

import java.io.UnsupportedEncodingException;

import com.nary.util.Localization;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class CharsetDecode extends functionBase {

  private static final long serialVersionUID = 1L;
	
  public CharsetDecode(){  
  	min = max = 2;
  	setNamedParams( new String[]{ "string", "encoding" } );
  }


	public String[] getParamInfo(){
		return new String[]{
			"string to decode",
			"encoding format"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Returns the binary object represented by the encoded string", 
				ReturnType.BINARY );
	}
  
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
  	String encoding = getNamedStringParam( argStruct, "encoding","" );
  	String str = getNamedStringParam( argStruct, "string","" );

  	try{
  		return new cfBinaryData( str.getBytes( Localization.convertCharSetToCharEncoding( encoding ) ) );
		} catch (UnsupportedEncodingException e) {
			throwException( _session, "Unsupported encoding: " + encoding );		
		}
		
		return null; // keep compiler happy

  }
}
