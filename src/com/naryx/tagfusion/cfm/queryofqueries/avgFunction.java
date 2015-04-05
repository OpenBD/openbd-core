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
 * This class represents the avg function.
 * On each call to the evaluate function the average function
 * will update the result taking into account all the rowContexts
 * it has witnessed so far.
 * It will maintain the reference to the same instance that it
 * returned as the result therefore it is important that the
 * result returned is not altered.
 */
 
import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
 
public class avgFunction extends aggregateFunction {
	
	expression expr = null;
	cfNumberData result = null;
	int rowCount;
	double currentTotal;
	boolean all = false;
	boolean distinct = false;
	
	// maintains all the values that have already been 'used' when distinct = true
	List<Double> distinctList; 
	
	avgFunction( expression _e ){
		expr = _e;
		rowCount = 0;
		currentTotal = 0;
		result = new cfNumberData( 0.0 );
		
	}// avgFunction()
	
	
	avgFunction( expression _e, boolean _all, boolean _distinct ){
		this( _e );
		all = _all;
		distinct = _distinct;
		if ( distinct ){
			distinctList = new ArrayList<Double>();
		}
	}// avgFunction()
	
	
	public cfData evaluate( rowContext _rowContext, List<cfData> _preparedData ) throws cfmRunTimeException{
		cfData val = expr.evaluate( _rowContext, _preparedData );
		rowCount++; // increment row count regardless
		
		// tries to get a number from the value. If it isn't
		// a number then it will not be added to the running total
		// and hence current result doesn't need re-set.
		try{
			double doubleVal = val.getDouble();
			if ( distinct ){
				// only include the value in the total if it hasn't already been included
				Double wrapper = new Double( doubleVal );
				if ( !distinctList.contains( wrapper ) ){
					currentTotal += doubleVal; // update the total
					// add the value to the list so it doesn't get included again
					distinctList.add( wrapper ); 
				}
			}else{
				currentTotal += doubleVal;
			}
			result.set( currentTotal / rowCount );
		}catch( Exception ignored ){}
		
		return result;
		
	}// evaluate()

	
	expression copy(){
		return new avgFunction( expr.copy(), all, distinct );
	}// copy()
	
	public void reset(){
		result.set( 0.0 );
		currentTotal = 0;
		rowCount = 0;
		if ( distinct ){
			distinctList.clear();
		}
	}// reset()
	
	
	public String toString(){
		return "AVG ( " + expr + " )";	
	}// toString()

	
	
} // avgFunction
