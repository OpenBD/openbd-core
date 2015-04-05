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

package com.naryx.tagfusion.expression.function.ext;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class ThrowObject extends functionBase {
	private static final long serialVersionUID = 1L;

	public ThrowObject() {
		min = 0; max = 1;
	}

	public String[] getParamInfo(){
		return new String[]{
				"object"	
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Throws an exception based on a given object", 
				ReturnType.UNKNOWN );
	}
	
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		if ( parameters.size() == 0 ){
			throw new cfmRunTimeException( new cfCatchData(_session), false );
		}
		
		cfCatchData catchData = null;
		boolean rethrow = false;
		
		cfData obj = parameters.get(0);
		if (obj instanceof cfCatchData) {
			catchData = (cfCatchData) obj;
			rethrow = true;
		} else if (obj.getDataType() == cfData.CFJAVAOBJECTDATA && Exception.class.isInstance(((cfJavaObjectData) obj).getInstance())) {
			//	if object is a java.lang.Exception, use the message from it
			catchData = new cfCatchData(_session);
			Exception e = (Exception) ((cfJavaObjectData) obj).getInstance();
			catchData.setJavaException( e );
			catchData.setMessage(e.getMessage());
		} else { 
			// create empty catchdata
			catchData = new cfCatchData(_session);
		}
		
		throw new cfmRunTimeException( catchData, rethrow );
	}
}
