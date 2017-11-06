/* 
 *  Copyright (C) 2012 TagServlet Ltd
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
 *  
 *  $Id: CFTernaryExpression.java 2080 2012-05-06 20:54:58Z andy $
 */
package com.naryx.tagfusion.cfm.parser;

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class CFTernaryExpression extends CFExpression {

	private static final long serialVersionUID = 1L;

	private CFExpression main;
	private CFExpression ifTrue;
	private CFExpression ifFalse;


	public CFTernaryExpression( Token _t, CFExpression _main, CFExpression _ifTrue, CFExpression _ifFalse ) {
		super( _t );
		main = _main;
		ifTrue = _ifTrue;
		ifFalse = _ifFalse;
	}


	@Override
	public cfData Eval( CFContext context ) throws cfmRunTimeException {
		cfData mainData = main.Eval( context );
		if ( mainData.getDataType() == cfData.CFLDATA ) {
			mainData = ( (cfLData) mainData ).Get( context );
		}
		if ( mainData.getBoolean() ) {
			return ifTrue.Eval( context );
		} else {
			return ifFalse.Eval( context );
		}
	}


	@Override
	public String Decompile( int indent ) {
		return main.Decompile( indent ) + " ? " + ifTrue.Decompile( indent ) + " : " + ifFalse.Decompile( indent );
	}

}
