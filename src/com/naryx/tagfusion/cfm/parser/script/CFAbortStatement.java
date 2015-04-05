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

package com.naryx.tagfusion.cfm.parser.script;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFExpression;

public class CFAbortStatement extends CFParsedStatement implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private CFExpression message;
	
	public CFAbortStatement( org.antlr.runtime.Token t ) {
		this(t, null);
	}

	public CFAbortStatement( org.antlr.runtime.Token t, CFExpression _message ) {
		super(t);
		message = _message;
	}

	@Override
	public CFStatementResult Exec( CFContext context ) throws cfmRunTimeException {
		setLineCol(context);
		
		if ( message != null ){
			String messageStr = message.EvalFully( context ).getString();
			throw new cfmRunTimeException( catchDataFactory.runtimeException( this.getHostTag(), messageStr ) );
		}else{
			context.getSession().abortPageProcessing(true); // throws cfmAbortException, but flushing the output
		}
		
		return null;
	}

	@Override
	public String Decompile( int indent ) {
		if ( message != null ){
			return "abort \"" + message + "\"";
		}else{
			return "abort";
		}
	}
	
}
