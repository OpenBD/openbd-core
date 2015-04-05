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
 * This class represents a numeric expression 
 *  - ( numeric_term ( PLUS | MINUS ) numeric_expression ).
 */
 
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
 
public class numericExpression extends expression {

	final static byte PLUS = 0, MINUS = 1;
	private byte op = PLUS;

	expression numTerm;
	expression numExp;
	
	public numericExpression( expression _nt, expression _ne, byte _op ){
		numTerm = _nt;
		numExp = _ne;
		op = _op;
		
	}// numericExpression()

	protected boolean isAggregateFunction(){
		return numTerm.isAggregateFunction() || numExp.isAggregateFunction();
	}
	
	void reset(){
		numTerm.reset();
		numExp.reset();
	}
	
	public cfData evaluate( rowContext _rowContext, List<cfData> _preparedData ) throws cfmRunTimeException{
		try{
			if ( op == PLUS ){
				try{
					return new cfNumberData( numTerm.evaluate( _rowContext, _preparedData ).getDouble() 
							+ numExp.evaluate( _rowContext, _preparedData ).getDouble() );
				}catch( dataNotSupportedException e ){
					return new cfStringData( numTerm.evaluate( _rowContext, _preparedData ).getString() + numExp.evaluate( _rowContext, _preparedData ).getString() );
				}
			}else{
				return new cfNumberData( numTerm.evaluate( _rowContext, _preparedData ).getDouble() 
					- numExp.evaluate( _rowContext, _preparedData ).getDouble() );
			}
		
		}catch( dataNotSupportedException e ){
			cfmRunTimeException cfmEx = new cfmRunTimeException();
			cfmEx.getCatchData().setMessage( "Cannot perform this operation on these data types." );
			throw cfmEx;
		}
		
	}// evaluate()
	
	
	public cfData evaluate( ResultRow _row, List<cfData> data, Map<String, Integer> lookup ) throws cfmRunTimeException {
		try{
			if ( op == PLUS ){
				try{
					return new cfNumberData( numTerm.evaluate( _row, data, lookup ).getDouble() 
						+ numExp.evaluate( _row, data, lookup ).getDouble() );
				}catch( dataNotSupportedException e ){
					return new cfStringData( numTerm.evaluate( _row, data, null ).getString() + numExp.evaluate( _row, data, null ).getString() );
				}
			}else{
				return new cfNumberData( numTerm.evaluate( _row, data, lookup ).getDouble() 
					- numExp.evaluate( _row, data, lookup ).getDouble() );
			}
		
		}catch( dataNotSupportedException e ){
			cfmRunTimeException cfmEx = new cfmRunTimeException();
			cfmEx.getCatchData().setMessage( "Cannot perform this operation on these data types." );
			throw cfmEx;
		}

	}

	public String toString(){
		if ( op == PLUS ){
			return numTerm.toString() + " + " + numExp.toString();
		}else{
			return numTerm.toString() + " - " + numExp.toString();
		}
	}// toString()
	
	
}// numericExpression
