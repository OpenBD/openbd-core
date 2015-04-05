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

package com.naryx.tagfusion.cfm.parser;

/**
 * Abstract class that takes care of the line and column positions of parsed
 * elements.
 */

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.script.CFStatementResult;

public abstract class CFParsedStatement implements CFStatement, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private int line;
	private int col;

	public CFParsedStatement( int _line, int _col ) {
		line = _line;
		col = _col + 1;
	}

	public CFParsedStatement( Token t ) {
		this(t.getLine(), t.getCharPositionInLine());
	}

	public abstract CFStatementResult Exec( CFContext context )
	    throws cfmRunTimeException;

	public abstract String Decompile( int indent );

	public void checkIndirectAssignments( String[] scriptSource ) {
		// default behavior: do nothing
	}

	protected void setLineCol( CFContext context ) {
		context.setLineCol(line, col);
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return col;
	}

	public String Indent( int indent ) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			sb.append( " " );
		}
		return sb.toString();
	}

}
