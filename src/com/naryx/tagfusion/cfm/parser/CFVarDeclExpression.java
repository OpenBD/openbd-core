/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.parser;

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class CFVarDeclExpression extends CFExpression {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private CFExpression init;			// null if none
  
	public CFVarDeclExpression( Token _t, CFIdentifier _var, CFExpression _init) {
		super( _t );
		name = _var.getName();
		init = _init;
	}
  
	public cfData Eval( CFContext context) throws cfmRunTimeException {
		cfData val = null;

		/* Removing the check
		 * http://code.google.com/p/openbluedragon/issues/detail?id=352
		 * 
		if ( context.containsLocal( name ) ) {
			System.out.println("twice");
			throw new CFException("The local variable " + name + " cannot be declared twice.", context);
		}
		*/
		
		cfLData lval = context.getLocal( name );
		if ( lval == null ) {
			// throw exception since there is no local scope
			throw new CFException("Cannot declare local variables outside a function body.",context);
		}

		if( init != null ) {
			val = init.Eval(context);
			if ( val.getDataType() == cfData.CFLDATA ){
				val = ( (cfLData) val ).Get( context );
			}
		}else {
			val = CFUndefinedValue.UNDEFINED;
		}

		if ( val.isLoopIndex() ){
			val = val.duplicate();
		}else if ( val.getDataType() == cfData.CFARRAYDATA && !com.naryx.tagfusion.cfm.engine.cfEngine.isStrictPassArrayByReference() ){
			val = ( (cfArrayData) val).duplicate( false );
		}

		lval.Set(val, context);
   
		return context._lastExpr = val;
  }

	public String Decompile(int indent) {
		StringBuilder s = new StringBuilder( Indent(indent) );
		s.append( "var " );
    
		s.append( name );
		if( init != null) {
    	s.append( " = " );
    	s.append( init.Decompile(indent+2) );
    }
    
    return s.toString();
	}
}
