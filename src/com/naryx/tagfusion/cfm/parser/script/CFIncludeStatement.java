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

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.tag.cfINCLUDE;

public class CFIncludeStatement extends CFParsedStatement implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	
	private CFExpression template;
	
	public CFIncludeStatement( Token _t, CFExpression _template ){
		super( _t );
		template = _template;
	}


	public CFStatementResult Exec(CFContext context) throws cfmRunTimeException {
		setLineCol(context);
		
		String templatePath	= template.EvalFully( context ).getString();

		cfSession session = context.getSession();
		cfFile templateFile	= cfINCLUDE.loadTemplate( session, templatePath );
		session.write( templateFile.renderToString( session ).getOutput() );
		
		return null;
	}

	public String Decompile(int indent) {
		return "include " + template.Decompile(0);
	}

}
