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

import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class randomRange extends functionBase {

	private static final long serialVersionUID = 1L;

	public randomRange() {
		min = 2;
		max = 3;
	}

	public String[] getParamInfo(){
		return new String[]{
			"number1",
			"number2",
			"random generator - use any of the installed Java random generators"
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"math", 
				"Returns a random number between the two numbers using the given optional random generator", 
				ReturnType.NUMERIC );
	}
	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		String algorithm = random.DEFAULT_ALGORITHM;
		if (parameters.size() > 2) {
			algorithm = ((cfData) parameters.get(0)).getString();
		}

		int low = getInt(_session, parameters.get(parameters.size() - 1));
		int high = getInt(_session, parameters.get(parameters.size() - 2));

		if (low > high)
			return new cfNumberData(rand(_session, algorithm, high, low));
		else
			return new cfNumberData(rand(_session, algorithm, low, high));
	}

	private int rand(cfSession _session, String _alg, int lo, int hi) throws cfmRunTimeException {
		try {
			return _session.getRandom(_alg).nextInt(hi - lo + 1) + lo;
		} catch (NoSuchAlgorithmException e) {
			throwException(_session, "Unsupported algorithm: " + _alg);
			return -1; // keep compiler happy
		}
	}

	public String getName() {
		return "randrange";
	};
}
