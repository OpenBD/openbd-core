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
 *  http://www.openbluedragon.org/
 *  $Id: SessionActive.java 2164 2012-07-08 15:44:26Z alan $
 */
package com.bluedragon.vision.functions;

import java.util.List;

import com.bluedragon.vision.engine.CoreServer;
import com.bluedragon.vision.engine.VisionLiveSession;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class SessionActive extends functionBase {
	private static final long serialVersionUID = 1L;

	public SessionActive(){
		min = max = 1;
	}
	
	public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException {
	  int sessionid = parameters.get(0).getInt();
	  VisionLiveSession	ds = CoreServer.thisInst.getActiveSession( sessionid );
	  
	  if ( ds != null ){
	  	return cfBooleanData.TRUE;
	  }else{
	  	return cfBooleanData.FALSE;
	  }
	}	
}
