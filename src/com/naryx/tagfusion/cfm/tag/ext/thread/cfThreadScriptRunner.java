/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmAbortException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.parser.script.CFScriptStatement;
import com.naryx.tagfusion.cfm.parser.script.CFStatementResult;

public class cfThreadScriptRunner extends cfThreadRunner{
	
	private CFScriptStatement threadBody;
	private CFContext context;
	
	public cfThreadScriptRunner( CFScriptStatement _body, String threadName, CFContext _context, int _priority ){
		super( threadName, _context.getSession(), _priority );
		
		context = _context;
		threadBody = _body;
	}

	public void run(){
		executionStatus = RUNNING;
		try {
			CFStatementResult result = threadBody.Exec( context );
			
			if ( threadData != null ) {
				if ( result != null && result.isReturn() ) {
					cfData returnValue = result.getReturnValue();
	            	if ( ( returnValue != null ) && ( returnValue.getDataType() == cfData.CFLDATA ) ) {
	                	returnValue = ((cfLData)returnValue).Get( threadSession.getCFContext() );
	            	}
	            	threadData.setReturnVariable( returnValue );
				}
				threadData.setGeneratedContent( threadSession.getOutputAsString() );
			}
			
			if ( executionStatus != TERMINATED ){
				executionStatus = COMPLETED;
			}
		} catch ( cfmAbortException ae ) {
			if ( threadData != null ) {
				threadData.setGeneratedContent( threadSession.getOutputAsString() );
			}
		} catch ( cfmRunTimeException rte ) {
			try {
				// invoke Application.cfc onError handler
				threadSession.onError( rte, "cfthread" );
			} catch ( cfmRunTimeException e ) {
				e.handleException( threadSession );
			}
			if ( threadData != null ) {
				// signal the parent that an error happened?
				threadData.setException( rte.getCatchData() );
			}
		} catch ( Throwable t ) {
			t.printStackTrace();
			new cfmRunTimeException( threadSession, t ).handleException( threadSession );
		} finally {
			if ( executionStatus != COMPLETED ){
				executionStatus = TERMINATED;		
			}
			// Make sure per request connections are closed (bug NA#2752)
			threadSession.sessionEnd();
		}
		
		this.runningTime = System.currentTimeMillis() - startTime;

	}
}
