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

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmAbortException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * This class implements the CFSILENT tag.  This tag consumes the output and discards it
 */

public class cfSILENT extends cfTag implements Serializable {

	static final long serialVersionUID = 1;
	
	public java.util.Map getInfo(){
		return createInfo("control", "Stops any browser output within the scope of this tag.");
	}

	public String getEndMarker() {
		return "</CFSILENT>";
	}

	public cfTagReturnType render( cfSession _Session )
			throws cfmRunTimeException {
		try {
			return renderToString( _Session, ( cfTag.SUPPRESS_OUTPUT_AFTER_ABORT | cfTag.HONOR_CF_SETTING ) );
		} catch ( cfmAbortException ae ) {
			if ( ae.flushOutput() ) {
				_Session.clearCfSettings();
				_Session.write( ae.getOutput().trim() );
			}
			throw ae;
		}
	}
}
