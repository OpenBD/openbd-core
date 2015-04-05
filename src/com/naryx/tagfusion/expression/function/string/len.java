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

package com.naryx.tagfusion.expression.function.string;


import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.expression.function.functionBase;
 
public class len extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public len(){  
  	min = max = 1;
  	setNamedParams( new String[]{ "string"} );
  }
  
	public String[] getParamInfo(){
		return new String[]{
			"string/binary/array/struct"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Returns the length of the given string, binary, struct or array object", 
				ReturnType.NUMERIC );
	}

  
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
  	cfData data = getNamedParam(argStruct, "string");
  	
  	if ( data.getDataType() ==  cfData.CFBINARYDATA )
  		return new cfNumberData( ((cfBinaryData)data).getLength() );
  	else if ( data.getDataType() ==  cfData.CFARRAYDATA )
  		return new cfNumberData( ((cfArrayData)data).size() );
  	else if ( data.getDataType() ==  cfData.CFSTRUCTDATA )
  		return new cfNumberData( ((cfStructData)data).size() );
  	else if ( !cfData.isSimpleValue( data ) && !( data instanceof cfXmlData ) && !( data instanceof cfCatchData ) )
      throwException( _session, "The len function does not support complex data types.");
  	
  	return new cfNumberData( data.getString().length() );
  } 
}
