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

package com.naryx.tagfusion.expression.function.struct;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class StructList extends functionBase {
	private static final long serialVersionUID = 1L;

	public StructList() {
		super.min = 1;
		super.max = 1;
		setNamedParams( new String[]{ "maxsize" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"the maximum number of items that this struct will hold"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"structure", 
				"Creates a new structure, that has a maximum number of items.  It will maintain the insertion order of the structure.", 
				ReturnType.STRUCTURE  );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		int maxSize	= getNamedIntParam(argStruct, "maxsize", -1 );
		if ( maxSize == -1 )
			throwException(_session, "please specify the maxsize");
		
		return new cfStructLinkData(maxSize);
	}
}
