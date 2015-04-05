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
 * This class represents the sum function.
 * On each call to the evaluate function the sum function
 * will update the result taking into account all the rowContexts
 * it has witnessed so far.
 * It will maintain the reference to the same instance that it
 * returned as the result therefore it is important that the
 * result returned is not altered.
 */
 
import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
 
public class sumFunction extends aggregateFunction {
	
	expression expr = null;
	boolean all = false;
	boolean distinct = false;
	
	cfNumberData sum;
	
	// maintains all the values that have already been 'used' when distinct = true
	List<Double> distinctList; 
	
	sumFunction( expression _e ){
		expr = _e;
		sum = new cfNumberData( 0.0 );
	}// sumFunction()
	
	
	sumFunction( expression _e, boolean _all, boolean _distinct ){
		this( _e );
		all = _all;
		distinct = _distinct;
		if ( distinct ){
			distinctList = new ArrayList<Double>();
		}
	}// sumFunction()
	
	
	public cfData evaluate( rowContext _rowContext, List<cfData> _preparedData ) throws cfmRunTimeException{
		cfData val = expr.evaluate( _rowContext, _preparedData );

		if ( val.getDataType() == cfData.CFNULLDATA || ( val.getDataType() == cfData.CFSTRINGDATA && val.getString().length() == 0 ) ){
			return sum;
		}

		double doubleVal;

		// check that the data is a number, 
		if ( val.getDataType() == cfData.CFNUMBERDATA ){
			 doubleVal = val.getDouble();
			
		}else if ( val.getDataType() == cfData.CFSTRINGDATA ){ // also permit strings that can be converted to numerics
			try{
				doubleVal = val.getDouble();
			}catch( dataNotSupportedException e ){
				throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
					"queryofqueries.invalidSum", 
					null ) );
			}
		
		}else{ // else throw an exception
			throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
					"queryofqueries.invalidSum", 
					null ) );

		}
		
		try{
			if ( distinct ){
				// only include the value in the total if it hasn't already been included
				Double wrapper = new Double( doubleVal );
				if ( !distinctList.contains( wrapper ) ){
					sum.add( doubleVal ); // update the total
					// add the value to the list so it doesn't get included again
					distinctList.add( wrapper ); 
				}
			}else{
				sum.add( doubleVal );
			}
			
		}catch( Exception ignored ){}
		
		return sum;	
	}// evaluate()
	
	public void reset(){
		sum.set( 0.0 );
		if ( distinct ){
			distinctList.clear();
		}
	}// reset()
	
	
	expression copy(){
		return new sumFunction( expr.copy(), all, distinct );
	}// copy()
	
	
	public String toString(){
		return "SUM ( " + expr + " )";	
	}// toString()
	
} // sumFunction
