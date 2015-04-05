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
 * This class represents a comparison condition.
 *  - expression ( <= | >= | != | = | < | > ) expression
 */
 
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
 
class comparisonCondition extends condition{

	expression expr1, expr2;
	int operator;
	
	comparisonCondition( expression _expr, expression _expr2, Token _op ){
		expr1 = _expr;
		expr2 = _expr2;
		operator = _op.kind;
		
	}// comparisonCondition()
	
	
	boolean evaluate( rowContext _rowContext, List<cfData> _pData ) throws cfmRunTimeException{
		cfData val1 = expr1.evaluate( _rowContext, _pData );
		cfData val2 = expr2.evaluate( _rowContext, _pData );
    
  	return compareResult( val1, val2 );
	}// evaluate()

	boolean evaluate( ResultRow _row, List<cfData> _pData, Map<String, Integer> _indxLookup ) throws cfmRunTimeException {
		cfData val1 = expr1.evaluate( _row, _pData, _indxLookup );
		cfData val2 = expr2.evaluate( _row, _pData, _indxLookup );
    
  	return compareResult( val1, val2 );
	}
	
	private boolean compareResult( cfData _val1, cfData _val2 ) throws cfmRunTimeException{
	  int compareResult = cfData.compare( _val1, _val2 );
		switch ( operator ){
			case selectSQLParserConstants.LTE:
        return compareResult <= 0;
			case selectSQLParserConstants.LT:
				return compareResult < 0;
			case selectSQLParserConstants.GTE:
        return compareResult >= 0;
			case selectSQLParserConstants.GT:
				return compareResult > 0;
			case selectSQLParserConstants.NEQ:
			case selectSQLParserConstants.NEQ2:
				return compareResult != 0;
			case selectSQLParserConstants.EQ:
        return compareResult == 0;
			default:
				throw new cfmRunTimeException(); // won't happen
		}
			
	}

}// comparisonCondition
