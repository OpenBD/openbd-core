/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.queryofqueries;

/**
 * This class represents a test condition.
 * condition IS (NOT)? TRUE | FALSE | UNKNOWN
 */
 
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
 
class testCondition extends condition{

	condition cond;
	boolean not;
	int kind;
	
	testCondition( condition _c1, Token _t, boolean _not ){
		cond = _c1;
		not = _not;
		kind = _t.kind;
	}// notCondition()
	
	
	boolean evaluate( rowContext _rowContext, List<cfData> _pData ) throws cfmRunTimeException{
		return evaluate( cond.evaluate( _rowContext, _pData ) ); 
	}// evaluate()


	public boolean evaluate( ResultRow _row, List<cfData> data, Map<String, Integer> lookup ) throws cfmRunTimeException {
		return evaluate( cond.evaluate( _row, data, lookup ) );
	}
	
	private boolean evaluate( boolean _val ){
		switch ( kind ){
		case selectSQLParserConstants.TRUE:
			return _val == true;
		case selectSQLParserConstants.FALSE:
			return _val == false;
		case selectSQLParserConstants.UNKNOWN:
			return false; /// FIX
		default:
			break;
	}
	return false; 

		
	}
		
}// testCondition
