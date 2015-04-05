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
 * This class represents an in condition.
 * - expression ( NOT )? IN ( expressionlist )
 */
 
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
 
class inCondition extends condition{

	expression expr1;
	expression [] exprList;
	boolean not;
	
	inCondition( expression _e1, List<expression> _exps, boolean _not ){
		expr1 = _e1;
		Object [] expObjs = _exps.toArray();
		exprList = new expression[ expObjs.length ];
		for ( int i = 0; i < expObjs.length; i++ ){
			exprList[i] = (expression) expObjs[i];
		}
		not = _not;
		
	}// inCondition()
	
	/*
   * boolean
   * string 
   * number (int/double)
   * date
   *  
   *  
   *  (non-Javadoc)
   * @see com.naryx.tagfusion.cfm.queryofqueries.condition#evaluate(com.naryx.tagfusion.cfm.queryofqueries.rowContext, com.nary.util.VectorArrayList)
	 */
  
	boolean evaluate( rowContext _rowContext, List<cfData> _pData ) throws cfmRunTimeException{
		// return whether the value of expr1 in the list
		cfData val = expr1.evaluate( _rowContext, _pData );
		for ( int h = 0; h < exprList.length; h++ ){
			cfData nextVal = exprList[h].evaluate( _rowContext, _pData );
      if ( ( val.getDataType() == cfData.CFNUMBERDATA && val.getDouble() == nextVal.getDouble() )
          || ( val.getDataType() == cfData.CFSTRINGDATA && val.getString().equals( nextVal.getString() ) )
          || ( val.getDataType() == cfData.CFBOOLEANDATA && val.getBoolean() == nextVal.getBoolean() )
          || ( val.getDataType() == cfData.CFDATEDATA && val.equals( nextVal.getDateData() ) ) ){
          return !not; // if NOT used then will return false
      }
		}
		return not; 

  }// evaluate()
		
	public boolean evaluate( ResultRow _row, List<cfData> data, Map<String, Integer> lookup ) throws cfmRunTimeException {
		// return whether the value of expr1 in the list
		cfData val = expr1.evaluate( _row, data, lookup );
		for ( int h = 0; h < exprList.length; h++ ){
			cfData nextVal = exprList[h].evaluate( _row, data, lookup );
      if ( ( val.getDataType() == cfData.CFNUMBERDATA && val.getDouble() == nextVal.getDouble() )
          || ( val.getDataType() == cfData.CFSTRINGDATA && val.getString().equals( nextVal.getString() ) )
          || ( val.getDataType() == cfData.CFBOOLEANDATA && val.getBoolean() == nextVal.getBoolean() )
          || ( val.getDataType() == cfData.CFDATEDATA && val.equals( nextVal.getDateData() ) ) ){
          return !not; // if NOT used then will return false
      }
		}
		return not; 
	}
	

}// betweenCondition
