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
 * Definition of expression tree for a unary expression.
 */

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class CFUnaryExpression extends CFExpression implements
    java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private int kind;
	private CFExpression sub;
	
	public CFUnaryExpression( org.antlr.runtime.Token _t, CFExpression _sub ) {
		super(_t);
		kind = _t.getType();
		sub = _sub;
	}

	public byte getType() {
		return CFExpression.UNARY;
	}

	public cfData Eval( CFContext context ) throws cfmRunTimeException {
		cfData subVal;
		cfData val = null;
		cfLData ldata = null;

		setLineCol(context);
		subVal = sub.Eval(context);
		if ( subVal.getDataType() == cfData.CFLDATA ) {
			ldata = (cfLData) subVal;
			subVal = ((cfLData) subVal).Get(context);
		}
		setLineCol(context);

		switch (kind) {
			case CFMLLexer.MINUS:
				// if subVal is a numeric then we know it's safe to apply the MINUS
				if ( subVal.getDataType() == cfData.CFNUMBERDATA ) {
					if ( ((cfNumberData) subVal).isInt() ) {
						val = new cfNumberData(subVal.getInt() * -1);
					} else {
						val = new cfNumberData(subVal.getDouble() * -1.0);
					}
	
					// else just try creating the number
				} else {
					try {
						val = cfData.createNumber_Validate("-" + subVal.getString(), false);
					} catch (Exception e) {
						throw new CFException(
						    "Invalid expression. The '-' operator cannot be applied to a value of this type.",
						    context);
					}
				}
				break;
			case CFMLLexer.NOT:
			case CFMLLexer.NOTOP:
				try {
					val = cfBooleanData.getcfBooleanData(!subVal.getBoolean());
				} catch (Exception e) {
					throw new CFException(
					    "Invalid expression. The NOT operator cannot be applied to a value of this type.",
					    context);
				}
				break;
			case CFMLLexer.PLUS:
				val = sub.Eval(context);
				break;
			case CFMLLexer.PLUSPLUS:
				val = update(context, subVal, ldata, 1, false);
				break;
			case CFMLLexer.MINUSMINUS:
				val = update(context, subVal, ldata, -1, false);
				break;
			case CFMLLexer.POSTPLUSPLUS:
				val = update(context, subVal, ldata, 1, true);
				break;
			case CFMLLexer.POSTMINUSMINUS:
				val = update(context, subVal, ldata, -1, true);
				break;
	
			default:
				throw new CFException("Unknown unary operator (" + String.valueOf(kind)
				    + ").", context);
		}
		return context._lastExpr = val;
	}

	private cfData update( CFContext _context, cfData _currentVal,
	    cfLData _sourceLData, int _additive, boolean _post )
	    throws cfmRunTimeException {
		cfNumberData numVal = _currentVal.getNumber();
		cfData returnVal;
		cfNumberData newValue;
		if ( numVal.isInt() ) {
			newValue = new cfNumberData(numVal.getInt() + _additive);
		} else {
			newValue = new cfNumberData(numVal.getDouble() + (double) _additive);
		}

		if ( _post ) {
			returnVal = numVal;
			if ( _sourceLData != null ) {
				_sourceLData.Set(newValue, _context);
			}
		} else {
			returnVal = newValue;
			if ( _sourceLData != null ) {
				_sourceLData.Set(newValue, _context);
			}
		}

		return returnVal;
	}

	public String Decompile( int indent ) {
		StringBuilder sb = new StringBuilder();
		
		switch( kind ){
			case CFMLLexer.MINUS:
				sb.append( '-' );
				sb.append( sub.Decompile( 0 ) );
				break;
			case CFMLLexer.NOT:
				sb.append( "NOT " );
				sb.append( sub.Decompile( 0 ) );
				break;
			case CFMLLexer.NOTOP:
				sb.append( '!' );
				sb.append( sub.Decompile( 0 ) );
				break;
			case CFMLLexer.PLUS:
				sb.append( '+' );
				sb.append( sub.Decompile( 0 ) );
				break;
			case CFMLLexer.PLUSPLUS:
				sb.append( "++" );
				sb.append( sub.Decompile( 0 ) );
				break;
			case CFMLLexer.MINUSMINUS:
				sb.append( "--" );
				sb.append( sub.Decompile( 0 ) );
				break;
			case CFMLLexer.POSTPLUSPLUS:
				sb.append( sub.Decompile( 0 ) );
				sb.append( "--" );
				break;
			case CFMLLexer.POSTMINUSMINUS:
				sb.append( sub.Decompile( 0 ) );
				sb.append( "--" );
				break;
		}
		
		return sb.toString();
	}

}
