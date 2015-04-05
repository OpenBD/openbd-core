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
 *  $Id: CFForInStatement.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.parser.script;

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFException;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.CFIdentifier;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.tag.cfOUTPUT;
import com.naryx.tagfusion.cfm.tag.cfTag;

public class CFForInStatement extends CFParsedStatement implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private CFExpression variable;
	private CFExpression structure;
	private CFScriptStatement body;
	private boolean isLocal;

	public CFForInStatement( Token _t, CFExpression _key, CFExpression _structure, CFScriptStatement _body ) {
		this( _t, _key, _structure, _body, false );
	}
	
	public CFForInStatement(Token _t, CFExpression _key, CFExpression _structure, CFScriptStatement _body, boolean _isLocal) {
		super(_t);
		variable = _key;
		structure = _structure; // should be a cfstruct or array
		body = _body;
		isLocal = _isLocal;
	}

	public void setHostTag(cfTag _parentTag) {
		super.setHostTag(_parentTag);
		body.setHostTag(_parentTag);
	}

	public void checkIndirectAssignments(String[] scriptSource) {
		body.checkIndirectAssignments(scriptSource);
	}

	public CFStatementResult Exec(CFContext context) throws cfmRunTimeException {
		setLineCol(context);

		cfData temp = structure.Eval(context);
		if (temp.getDataType() == cfData.CFLDATA) {
			temp = ((cfLData) temp).Get(context);
		}

		if (temp == null || 
				!(temp.isStruct() || temp.getDataType() == cfData.CFARRAYDATA || temp.getDataType() == cfData.CFQUERYRESULTDATA)) {
			throw new CFException("Only structs/arrays may be used in a for...in.", context);
		}

		cfLData var = null;
		if ( isLocal ){
			var = context.getLocal( ((CFIdentifier) variable ).getName() );
			if ( var == null ) {
				// throw exception since there is no local scope
				throw new CFException("Cannot declare local variables outside a function body.",context);
			}
		}else{
			cfData temp2 = variable.Eval(context);
			var = (cfLData) temp2;
		}
		
				
		if (temp.isStruct()) {
			cfStructData struct = (cfStructData) temp;

			Object[] keys = struct.keys();
			for (int i = 0; i < keys.length; i++) {
				String nextStr = (String) keys[i];
				var.Set(new cfStringData(nextStr), context);

				CFStatementResult result = body.Exec(context);
				if (result != null) {
					if (result.isBreak()) {
						break;
					} else if (result.isContinue()) {
						continue;
					}
					return result;
				}
			}
		} else if (temp.getDataType() == cfData.CFARRAYDATA) {
			cfArrayData array = (cfArrayData) temp;

			int arrayLen = array.size();
			for (int i = 1; i <= arrayLen; i++) {
				var.Set(array.getData(i), context);

				CFStatementResult result = body.Exec(context);
				if (result != null) {
					if (result.isBreak()) {
						break;
					} else if (result.isContinue()) {
						continue;
					}
					return result;
				}
			}

		} else if (temp.getDataType() == cfData.CFQUERYRESULTDATA ) {

			cfQueryResultData	queryData	= (cfQueryResultData)temp;

			int resetRow = queryData.getCurrentRow();
			queryData.reset();
			context.getSession().pushQuery( queryData );
			
			while (queryData.nextRow()) {
				cfStructData	rowData	= queryData.getRowAsStruct();
				var.Set(rowData, context);

				CFStatementResult result = body.Exec(context);
				if (result != null) {
					if (result.isBreak()) {
						break;
					} else if (result.isContinue()) {
						continue;
					}

					cfOUTPUT.cleanupQueryData(context.getSession(), queryData, resetRow, false );
					return result;
				}
			}

			cfOUTPUT.cleanupQueryData(context.getSession(), queryData, resetRow, false );
		}
		return null;
	}

	public String Decompile(int indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(Indent(indent));
		sb.append("for( ");
		sb.append(variable);
		sb.append(" in ");
		sb.append(structure.Decompile(indent));
		sb.append(" ) ");
		sb.append(body.Decompile(indent + 2));
		return sb.toString();
	}

}
