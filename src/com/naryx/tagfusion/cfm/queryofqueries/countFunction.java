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
 * This class represents the count function.
 * On each call to the evaluate function the count function
 * will update the result taking into account all the rowContexts
 * it has witnessed so far.
 * It will maintain the reference to the same instance that it
 * returned as the result therefore it is important that the
 * result returned is not altered.
 */
 
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
 
public class countFunction extends aggregateFunction {
	
	expression expr; // countParam
	cfNumberData count;
	
	countFunction(){
		// * ASTERISK
		count = new cfNumberData( 0.0 );
	}// countFunction()
	
	
	countFunction( expression _e ){
		this();
		expr = _e;
	}// countFunction()
		
	
	public cfData evaluate( rowContext _rowContext, List<cfData> _preparedData ){
		count.add( 1.0 );
		return count;
		
	}// evaluate()

	
	public void reset(){
		count.set( 0.0 );
	}// reset()
	
	expression copy(){
		countFunction copy = new countFunction();
		copy.expr = expr;
		return copy;
	}// copy()
	
	
	public String toString(){
		return "COUNT ( " + ( expr == null ? "*" : expr.toString() ) + " )";
	}// toString()


} // countFunction
