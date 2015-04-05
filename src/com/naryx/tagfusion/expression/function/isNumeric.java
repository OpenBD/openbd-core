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
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class isNumeric extends functionBase {

	private static final long serialVersionUID = 1L;

	public isNumeric() {
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
				"Determines if the object represents a numeric", 
				ReturnType.BOOLEAN );
	}

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		cfData a = parameters.get(0);
		switch (a.getDataType()) {
		case cfData.CFNUMBERDATA:
			return cfBooleanData.TRUE;
		case cfData.CFDATEDATA:
			return cfBooleanData.FALSE;
		case cfData.CFBOOLEANDATA:
			return cfBooleanData.FALSE;
		case cfData.CFNULLDATA:
			return cfBooleanData.FALSE;
		case cfData.CFSTRINGDATA:
			// note: cfStringData.getDouble() returns true for boolean strings (e.g
			// "true")
			// which is wrong so we need to try creating a number from the string
			// only.
			try {
				cfData.createNumber_Validate(a.getString(), true);
				return cfBooleanData.TRUE;
			} catch (dataNotSupportedException e) {
				return cfBooleanData.FALSE;
			}
		default:
			try {
				a.getDouble();
				return cfBooleanData.TRUE;

			} catch (dataNotSupportedException E) {
				return cfBooleanData.FALSE;
			}
		}

	}
}
