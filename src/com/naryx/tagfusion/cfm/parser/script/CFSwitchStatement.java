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

/**
 * This class represents a switch statement. 
 */

import java.util.List;

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.tag.cfTag;

public class CFSwitchStatement extends CFParsedStatement implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private List<CFCase> cases;
	private CFExpression variable;

	public CFSwitchStatement( Token _token, CFExpression _variable,
	    List<CFCase> _cases ) {
		super(_token);
		cases = _cases;
		variable = _variable;
	}

	public void checkIndirectAssignments( String[] scriptSource ) {
		for (int i = 0; i < cases.size(); i++) {
			cases.get(i).checkIndirectAssignments(scriptSource);
		}
	}

	public void setHostTag( cfTag _parentTag ){
		super.setHostTag( _parentTag );
		for ( int i = 0; i < cases.size(); i++ ){
			cases.get( i ).setHostTag( _parentTag );
		}
	}
	
	public CFStatementResult Exec( CFContext context ) throws cfmRunTimeException {
		setLineCol(context);
		
		// this method probably could be more efficient
		cfData value = variable.Eval(context);
		if ( value.getDataType() == cfData.CFLDATA )
			value = ((cfLData) value).Get(context);

		boolean isBooleanCase = false;
		boolean isNumberCase = false;

		if ( value.getDataType() == cfData.CFBOOLEANDATA ) {
			isBooleanCase = true;
		} else if ( value.getDataType() == cfData.CFNUMBERDATA ) {
			isNumberCase = true;
		}

		boolean execute = false; // set to true when the first case is matched
		// it means that the following cases will be executed while there is no
		// "break;"
		CFStatementResult result = null;

		int defaultIndex = -1; // hold the index of the defaultIndex

		CFCase nextCase;
		for (int i = 0; i < cases.size(); i++) {
			nextCase = cases.get(i);
			if ( !execute ) {
				if ( nextCase.isDefault() )
					defaultIndex = i;
				else {
					if ( isBooleanCase ) {
						boolean boolVal = false;
						try {
							boolVal = nextCase.getConstant(context).getBoolean();
						} catch (dataNotSupportedException ignored) { /* go with default */
						}
						if ( ((cfBooleanData) value).getBoolean() == boolVal )
							execute = true;
					} else if ( isNumberCase ) {
						double doubleVal;
						try {
							doubleVal = nextCase.getConstant(context).getDouble();
							if ( ((cfNumberData) value).getDouble() == doubleVal )
								execute = true;
						} catch (dataNotSupportedException ignored) {
						}
					} else {
						String strVal = "";
						try {
							strVal = nextCase.getConstant(context).getString();
							if ( value.getString().equalsIgnoreCase(strVal) )
								execute = true;
						} catch (dataNotSupportedException ignored) {
						}
					}

				}
			}

			if ( execute ) {
				result = nextCase.Exec(context);
				if ( result != null ) {
					return (result.isReturn() ? result : null);
				}
			}
		}

		// if a value wasn't found then execute from the default case if one
		// exists.
		if ( !execute && defaultIndex != -1 ) {
			for (int i = defaultIndex; i < cases.size(); i++) {
				nextCase = cases.get(i);
				result = nextCase.Exec(context);
				if ( result != null ) {
					return (result.isReturn() ? result : null);
				}
			}
		}

		return result;
	}

	public String Decompile( int _indent ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "switch (" );
		sb.append( variable.Decompile( 0 ) );
		sb.append( "){\n" );
		for ( int i = 0; i < cases.size(); i++ ){
			sb.append( ((CFCase) cases.get(i) ).Decompile( 0 ) );
		}
		sb.append( "\n}" );
		return sb.toString();
	}

}
