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
 *  - ( numericFactor ( ASTERISK | SLASH ) numericTerm ).
 */
 
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
 
public class numericTerm extends expression {

	final static byte ASTERISK = 0, SLASH = 1;
	private byte op = ASTERISK;

	expression numFactr;
	expression numTerm;
	
	public numericTerm( expression _nf, expression _nt, byte _op ){
		numTerm = _nt;
		numFactr = _nf;
		op = _op;
		
	}// numericTerm()

	protected boolean isAggregateFunction(){
		return numTerm.isAggregateFunction() || numFactr.isAggregateFunction();
	}

	
	public cfData evaluate( rowContext _rowContext, List<cfData> _preparedData ) throws cfmRunTimeException{
		if ( op == ASTERISK ){
			return new cfNumberData( numFactr.evaluate( _rowContext, _preparedData ).getDouble() 
				* numTerm.evaluate( _rowContext, _preparedData ).getDouble() );
		}else{ // SLASH
			return new cfNumberData( numFactr.evaluate( _rowContext, _preparedData ).getDouble() 
				/ numTerm.evaluate( _rowContext, _preparedData ).getDouble() );
		}
		
	}// evaluate()
	
	cfData evaluate( ResultRow _row, List<cfData> data, Map<String, Integer> lookup ) throws cfmRunTimeException {
		if ( op == ASTERISK ){
			return new cfNumberData( numFactr.evaluate( _row, data, lookup ).getDouble() 
				* numTerm.evaluate( _row, data, lookup ).getDouble() );
		}else{ // SLASH
			return new cfNumberData( numFactr.evaluate( _row, data, lookup ).getDouble() 
				/ numTerm.evaluate( _row, data, lookup ).getDouble() );
		}
	}

	public String toString(){
		if ( op == ASTERISK ){
			return numFactr.toString() + " * " + numTerm.toString();
		}else{
			return numFactr.toString() + " / " + numTerm.toString();
		}
	}// toString
	

	void reset() {
		numFactr.reset();
		numTerm.reset();
	}

}// numericTerm
