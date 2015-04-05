/* 
 * Copyright (C) 2000 - 2008 TagServlet Ltd
 *
 * This file is part of the BlueDragon Java Open Source Project.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.naryx.tagfusion.cfm.parser.script;

import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmExitException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFException;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.tag.cfEXIT;
import com.naryx.tagfusion.cfm.tag.cfMODULE;

public class CFExitStatement extends CFParsedStatement implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;

	private CFExpression methodArg;
	
	public CFExitStatement( org.antlr.runtime.Token t, CFExpression _method ) {
		super(t);
		methodArg = _method;
	}

	@Override
	public CFStatementResult Exec( CFContext context ) throws cfmRunTimeException {
		setLineCol(context);
		
		String method = null;
		if ( methodArg == null ){
			method = cfEXIT.METHOD_EXITTAG;
		}else{
			method = methodArg.EvalFully( context ).getString();
			if ( !cfEXIT.isValidMethod( method ) ){
				throw new CFException( "Invalid exit method", context );
			}
		}
		
		/*
		 * BlueDragon addition; method="request".  This will effectively be like a
		 * CFABORT but it suppresses the output to the browser if inside a CFSILENT
		 */
		if ( method.equalsIgnoreCase( cfEXIT.METHOD_REQUEST ) ) {
			context.getSession().abortPageProcessing( false );
		}
		
		cfStructData thisTag = (cfStructData) context.getSession().getData( cfMODULE.THISTAG_SCOPE );
		
		if ( thisTag == null ) { // executing within base template, not custom tag
			if ( method.equalsIgnoreCase( cfEXIT.METHOD_LOOP ) ) {
				throw new CFException( "CFEXIT METHOD=LOOP can only be used within custom tags", context );
			} else {
				// from the CFMX docs: If this tag is encountered outside the context of a custom tag,
				// for example in the base page or an included page, it executes in the same way as cfabort;
				// there are three exceptions to this: within Application.cfm, cfinclude, and cffunction
				throw new cfmExitException();
			}
		}
		
		return null;
	}

	@Override
	public String Decompile( int indent ) {
		if ( methodArg != null ){
			return "exit \"" + methodArg + "\"";
		}else{
			return "exit";
		}
	}
}
