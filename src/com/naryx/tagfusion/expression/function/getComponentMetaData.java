/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: getComponentMetaData.java 2103 2012-05-21 23:56:45Z alan $
 */

package com.naryx.tagfusion.expression.function;

import com.bluedragon.plugin.ObjectCFC;
import com.bluedragon.plugin.PluginManager;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.util.dummyServletResponse;

public class getComponentMetaData extends functionBase {
	private static final long serialVersionUID = 1L;

	public getComponentMetaData() {
		min = 0;
		max = 1;
		setNamedParams( new String[]{ "component" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"the path to the component.  This is a dot [.] notation."
		};
	}

	public java.util.Map getInfo() {
		return makeInfo("engine", "Returns back meta information for a component", ReturnType.STRUCTURE);
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String cfcPath	= getNamedStringParam(argStruct, namedParams.get(0), null);
		if ( cfcPath == null )
			throwException(_session, "please specify the component path to introspect");

		try {
			cfSession tmpSession	= new cfSession(	_session.REQ, 
																							new dummyServletResponse(), 
																							cfEngine.thisServletContext);
			
			ObjectCFC cfc = PluginManager.getPlugInManager().createCFC( tmpSession, cfcPath);
			return cfc.getComponentCFC().getMetaData();
		} catch (Exception e) {
			throwException(_session, "GetComponentData(" + cfcPath + ") error: " + e.getLocalizedMessage() );
			return null;
		}
	}
}
