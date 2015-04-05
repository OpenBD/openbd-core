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

public class randomize extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public randomize(){
     min = 1; max = 2;
  }

	public String[] getParamInfo(){
		return new String[]{
			"number seed",
			"random generator - use any of the installed Java random generators"
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"math", 
				"Returns a random number seeding the random number generator", 
				ReturnType.NUMERIC );
	}
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
    // seeds the random number generator and returns
  	// a pseudo-random decimal number, in the range 0-1.
  	
  	String algorithm = random.DEFAULT_ALGORITHM;
  	if ( parameters.size() > 1 ){
  		algorithm = ( (cfData) parameters.get(0) ).getString();
  	}
  	
  	cfData seed = parameters.get( parameters.size() - 1 );
  	
  	try {
			_session.setRandomSeed( seed, algorithm );
	    return new cfNumberData( _session.getRandom( algorithm ).nextDouble() );
		} catch (NoSuchAlgorithmException e) {
			throwException( _session, "Unsupported algorithm: " + algorithm );
			return null; // keep compiler happy
		} catch (SecurityException secExc) {
			throwException(_session, "The randomize function is not supported in partial trust mode: " + secExc);
			return null; // keep compiler happy
		}
  } 
}
