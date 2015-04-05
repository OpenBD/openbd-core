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

package com.naryx.tagfusion.cfm.tag.ext.thread;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfThreadData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfINTERRUPT extends cfTag implements Serializable {

	static final long serialVersionUID = 1;
	
	private static final String THREAD_NAME = "THREAD";

	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		parseTagHeader( _tag );
		requiredAttribute( THREAD_NAME );
	}

	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		String threadName = getDynamic( _Session, THREAD_NAME ).getString();
		cfThreadData threadData = null;

		try {
			cfData data = runTime.runExpression( _Session, threadName );
			if ( data instanceof cfThreadData ) {
				threadData = (cfThreadData)data;
			}
		} catch ( cfmRunTimeException e ) {
			throw newRunTimeException( "The specified THREAD " + threadName + " does not exist" );
		}
		
		if ( threadData == null ) {
			throw newRunTimeException( "The specified THREAD " + threadName + " is not a valid thread variable" );
		}
		
		threadData.getThread().interrupt();
		
		return cfTagReturnType.NORMAL;
	}
}
