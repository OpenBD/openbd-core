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
 * This class represents a string expression.
 * Note that since it returns the same cfStringData every time
 * on evaluation it is not safe to operate on it.
 */
 
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
 
public class stringConcatExpression extends expression {

	expression exp1;
	expression exp2;
	
	boolean isAggregateFunction(){
		return false;
	}
	
	// _str is the string with the enclosing quotes
	public stringConcatExpression( expression _str1, expression _str2 ){
		exp1 = _str1;
		exp2 = _str2;
		
	}// stringExpression()

	
	/**
	 * Note that since it returns the same cfStringData every time
   * on evaluation it is not safe to operate on it.
	 * @throws cfmRunTimeException 
	 * @throws dataNotSupportedException 
	 */
	public cfData evaluate( rowContext _rowContext, List<cfData> _preparedData ) throws dataNotSupportedException, cfmRunTimeException{
		return new cfStringData( exp1.evaluate( _rowContext, _preparedData ).getString() + exp2.evaluate( _rowContext, _preparedData ).getString() );
	}

	public cfData evaluate( ResultRow _row, List<cfData> _preparedData, Map<String, Integer> _indexLookup ) throws cfmRunTimeException {
		return new cfStringData( exp1.evaluate( _row, _preparedData, null ).getString() + exp2.evaluate( _row, _preparedData, null ).getString() );
	}
	
	public String toString(){
		return exp1.toString() + " + " + exp2.toString();
	}
	
}// expression
