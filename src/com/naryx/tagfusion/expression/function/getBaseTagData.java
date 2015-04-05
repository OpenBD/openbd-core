/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  $Id: $
 */

package com.naryx.tagfusion.expression.function;

import org.aw20.collections.FastStack;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.customTagVariableWrapper;

public class getBaseTagData extends functionBase {
	private static final long serialVersionUID = 1L;

	public getBaseTagData() {
		min = 1;
		max = 2;
		setNamedParams( new String[]{ "tagname", "depth" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"tagname",
			"depth - defaults to 1"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"date", 
				"Returns back the data associated with the parent tag, at the given depth level back", 
				ReturnType.STRUCTURE );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		int depth = getNamedIntParam( argStruct, "depth", 1 );
		String tagName = getNamedStringParam( argStruct, "tagname", null );

		FastStack<customTagVariableWrapper> vStack = _session.getBaseTagData();
		int loopDepth = 1;

		for (int x = vStack.size(); x > 0; x--) {
			customTagVariableWrapper cTW = vStack.elementAt(x - 1);
			if (cTW.tagName.equalsIgnoreCase(tagName)) {
				if (loopDepth == depth) {
					return cTW.variables;
				} else
					loopDepth++;
			}
		}

		throwException(_session, "Could not locate base tag named \"" + tagName + "\"");

		// keeps compiler happy, exception is thrown above
		return new cfStructData();
	}
}
