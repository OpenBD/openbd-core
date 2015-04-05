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
 *  $Id: CFComponentDeclStatement.java 2527 2015-02-26 16:08:24Z andy $
 */

package com.naryx.tagfusion.cfm.parser.script;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.tag.cfTag;


public class CFComponentDeclStatement extends CFParsedStatement implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private ArrayList<CFScriptStatement> body;
	private Map<String, CFExpression> attributes;


	public CFComponentDeclStatement( Token token, Map<String, CFExpression> attributes, ArrayList<CFScriptStatement> body ) {
		super( token );
		this.body = body;
		this.attributes = attributes;
	}


	public Map<String, CFExpression> getAttributes() {
		return attributes;
	}


	@Override
	public CFStatementResult Exec( CFContext context ) throws cfmRunTimeException {
		for ( CFScriptStatement s : body )
			s.Exec( context );

		return null;
	}


	@Override
	public void setHostTag( cfTag _parentTag ) {
		super.setHostTag( _parentTag );
		for ( CFScriptStatement b : body ) {
			b.setHostTag( _parentTag );
		}
	}


	@Override
	public String Decompile( int indent ) {
		return null;
	}


	/**
	 * This method will return all the UDF's for this script block; it will remove
	 * the functions from the main 'body' so it can run through the main block quicker
	 * 
	 * @return
	 */
	public List<userDefinedFunction> getUDFs() {
		ArrayList<userDefinedFunction> udfs = new ArrayList<userDefinedFunction>( 5 );

		Iterator<CFScriptStatement> it = body.iterator();
		while ( it.hasNext() ) {
			CFScriptStatement s = it.next();

			if ( s instanceof CFFuncDeclStatement ) {
				udfs.add( ( (CFFuncDeclStatement) s ).getUDF() );
				it.remove();
			}
		}

		return udfs;
	}

}