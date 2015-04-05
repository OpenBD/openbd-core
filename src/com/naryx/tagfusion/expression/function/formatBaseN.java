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


import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class formatBaseN extends functionBase {

	private static final long serialVersionUID = 1L;

	public formatBaseN() {
		max = min = 2;
		setNamedParams( new String[]{ "number", "base"  } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"number",
			"base (2-36)"
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"conversion", 
				"Converts the given number to the new given base", 
				ReturnType.NUMERIC );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		long a = getNamedLongParam( argStruct, "number" , 0 );
		int b = getNamedIntParam( argStruct, "base" , 0 );

		if (b < 2 || b > 36)
			throwException(_session, "the radix must be in the range 2 and 36");

		// ---[ Special case for base10
		if (b == 10)
			return new cfStringData(String.valueOf(a));

		return new cfStringData(convert(a, b));
	}

	private static String convert(long _num, int _base) {
		String s;
		int num = (int) _num;
		int base = _base;
		int rem;

		if (num > 0) {
			s = "";
			while (num > 0) {
				rem = num % base;
				s = digits[rem] + s;
				num = num / base;
			}
			return s;
		} else if (num < 0) {
			long lnum = (long) Integer.MIN_VALUE + (long) Integer.MIN_VALUE - (long) num;
			s = "";
			while (lnum < 0) {
				rem = (int) (lnum % base);
				s = digits[rem * -1] + s;
				lnum = lnum / base;
			}
			return s;
		} else {
			return "0";
		}
	}

	private static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
}
