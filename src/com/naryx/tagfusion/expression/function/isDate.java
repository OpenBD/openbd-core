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

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class isDate extends functionBase {

	private static final long serialVersionUID = 1L;

	public isDate(){
		min = max = 1;
	}
  
  public String[] getParamInfo(){
		return new String[]{
			"object"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"decision", 
				"Determines if the object is a date object", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, List<cfData> parameters ) {
		cfData d1 = parameters.get( 0 );
		byte dataType = d1.getDataType();
		
		// in most cases, numbers can be converted to dates, and strings that can be
		// converted to numbers can be converted to dates; however, this function returns
		// false for both of those, so we can't simply invoke cfData.isDateConvertible()

		if ( dataType == cfData.CFDATEDATA )
			return cfBooleanData.TRUE;
		else if ( dataType == cfData.CFNUMBERDATA )
			return cfBooleanData.FALSE;
		else if ( dataType == cfData.CFSTRINGDATA )
			return cfBooleanData.getcfBooleanData( ((cfStringData)d1).isDateConvertible( false ) );

		try {
			d1.getDateData();
			return cfBooleanData.TRUE;
		} catch ( cfmRunTimeException e ) {
			//--[ if the format was bad return false
			return cfBooleanData.FALSE;
		}
	} 
}
