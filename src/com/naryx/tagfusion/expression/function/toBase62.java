/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 */

package com.naryx.tagfusion.expression.function;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * This takes in an number and attempts to convert it to Base62 for encoding.
 * 
 * http://en.wikipedia.org/wiki/Base_62
 */
public class toBase62 extends functionBase {

	private static final long serialVersionUID = 1L;

	public toBase62() {
		min = 1;
		max = 1;
		setNamedParams( new String[]{ "number" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"number"	
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"conversion", 
				"This takes in an number and attempts to convert it to Base62 for encoding", 
				ReturnType.STRING );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		return new cfStringData(encode(getNamedLongParam( argStruct, "number", 0)));
	}
	

	private static final String base62Alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private static String encode(long n) {
		if (n <= 0)
			n = n * -1;

		StringBuilder s = new StringBuilder(8);
		int base = base62Alphabet.length();
		int mod = 0;

		while (n != 0) {
			mod = (int) n % base;
			if ( mod < 0 )
				mod = mod * -1;
			
			s.append( base62Alphabet.charAt(mod) );
			n = n / base;
		}

		return s.toString();
	}
}