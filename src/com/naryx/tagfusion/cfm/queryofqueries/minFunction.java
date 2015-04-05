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
 * This class represents the min function.
 * On each call to the evaluate function the min function
 * will update the result taking into account all the rowContexts
 * it has witnessed so far.
 * It will maintain the reference to the same instance that it
 * returned as the result therefore it is important that the
 * result returned is not altered.
 */
 
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
 
public class minFunction extends aggregateFunction {
	
	expression expr = null;
	boolean all = false;
	boolean distinct = false;
	
	boolean init = false;
	cfData min;
		
	minFunction( expression e ){
		expr = e;
		min = new cfStringData( "" );
	}// minFunction()
	
	minFunction( expression e, boolean _all, boolean _distinct ){
		this( e );
		all = _all;
		distinct = _distinct;
	
	}// minFunction()
	
	
	public cfData evaluate( rowContext _rowContext, List<cfData> _preparedData ) throws cfmRunTimeException{
		try{
			cfData data = expr.evaluate( _rowContext, _preparedData );
		
			if ( data.getDataType() != cfData.CFNULLDATA ){
				if ( !init  ){
					min = data;
					init = true;
				}else{
					if ( cfData.compare( data, min ) < 0 ) {
						min = data;
					}
				}
			}
		}catch( dataNotSupportedException ignored ){}
		
		return min;
	}// evaluate()

	public void reset(){
		init = false;
	}// reset()
	
	
	expression copy(){
		return new minFunction( expr.copy(), all, distinct );
	}// copy()
	
	
	public String toString(){
		return "MIN ( " + expr + " )";	
	}// toString()
	
} // minFunction
