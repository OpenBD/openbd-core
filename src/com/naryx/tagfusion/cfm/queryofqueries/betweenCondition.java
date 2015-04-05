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
 * This class represents a between condition.
 * NOTE it is left and right INCLUSIVE
 */
 
import java.util.List;
import java.util.Map;

import com.nary.util.date.dateTimeTokenizer;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
 
class betweenCondition extends condition{

	expression expr1, expr2, expr3;
	boolean not;
	
	// these vars are for caching the date values when the value
	// being considered is a date. It works nicely if the values
	// passed in are strings e.g. BETWEEN '22-01-01' AND '2-9-02'
	// and are hence the same for every call to evaluate
	String lastDateStr1, lastDateStr2;
	long lastDateLong1, lastDateLong2;
	
	betweenCondition( expression _e1, expression _e2, expression _e3, boolean _not ){
		expr1 = _e1;
		expr2 = _e2;
		expr3 = _e3;
		not = _not;
		lastDateStr1 = "";
		lastDateStr2 = "";
		
	}// betweenCondition()
	
	
	boolean evaluate( rowContext _rowContext, List<cfData> _pData ) throws cfmRunTimeException{
		// return whether the value of expr1 is between the values of expr2 and expr3.
		return compare( expr1.evaluate( _rowContext, _pData ), expr2.evaluate( _rowContext, _pData ), expr3.evaluate( _rowContext, _pData ) );
	}// evaluate()
	
	public boolean evaluate( ResultRow _row, List<cfData> data, Map<String, Integer> lookup ) throws cfmRunTimeException {
		return compare( expr1.evaluate( _row, data, lookup ), expr2.evaluate( _row, data, lookup ), expr3.evaluate( _row, data, lookup ) );
	}

	private boolean compare( cfData _val1, cfData _val2, cfData _val3 ) throws cfmRunTimeException{
		if ( _val1.getDataType() == cfData.CFNUMBERDATA ){
			// do number comparison
			return compareNumbers( _val1, _val2, _val3 );
		
		}else if ( _val1.getDataType() == cfData.CFSTRINGDATA ){
			// do string comparison
			return compareStrings( _val1, _val2, _val3 );
			
		}else if ( _val1.getDataType() == cfData.CFBOOLEANDATA ){
			// do boolean comparison
			return compareBooleans( _val1, _val2, _val3 );
		
		}else if ( _val1.getDataType() == cfData.CFDATEDATA ){
			// do date comparison
			return compareDates( _val1, _val2, _val3 ); 
		}else{
     	throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
																																				"queryofqueries.between", 
																																				null ) );
		}
		
	}
	
	private static boolean compareNumbers( cfData _theNumber, cfData _val1, cfData _val2 ) throws cfmRunTimeException{
		double valNo = _theNumber.getDouble();
		try{
			double lowerNo = _val1.getDouble();
			double upperNo = _val2.getDouble();
			return valNo >= lowerNo && valNo <= upperNo;
		}catch( Exception e ){
     	throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
																																				"queryofqueries.between", 
																																				null ) );
		}
	}// compareNumbers()
	
	
	private static boolean compareStrings( cfData _theString, cfData _val1, cfData _val2 ) throws cfmRunTimeException{
		try{
			String val = _theString.getString();
			String lower = _val1.getString();
			String upper = _val2.getString();
			return val.compareTo( lower ) >= 0 && val.compareTo( upper ) <= 0;
		}catch( Exception e ){
     	throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
																																				"queryofqueries.between", 
																																				null ) );
		}
		
	}// compareStrings()
	
	
	private static boolean compareBooleans( cfData _theBoolean, cfData _val1, cfData _val2 ) throws cfmRunTimeException{
		try{
			String val = _theBoolean.getBoolean() ? "true" : "false";
			String lower = _val1.getString();
			String upper = _val2.getString();
			return val.compareTo( lower ) >= 0 && val.compareTo( upper ) <= 0;
		}catch( Exception e ){
     	throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
																																				"queryofqueries.between", 
																																				null ) );
		}
	}// compareBooleans()
	
	
	private boolean compareDates( cfData _theDate, cfData _val1, cfData _val2 ) throws cfmRunTimeException{
		long valDate = _theDate.getDateData().getLong();
		try{
			long lowerDate;
			if ( _val1.getDataType() == cfData.CFDATEDATA ){
				lowerDate = ( (cfDateData) _val1 ).getLong(); 
			}else{
				// check the 'one-date cache'
				String dateStr1 = _val1.getString();
				if ( lastDateStr1.equals( dateStr1 ) ){
					lowerDate = lastDateLong1;
				}else{
					lowerDate = dateTimeTokenizer.getNeutralDate( dateStr1 ).getTime();
					lastDateStr1 = dateStr1;
					lastDateLong1 = lowerDate;
				}
			}
				
			long upperDate;
			if ( _val2.getDataType() == cfData.CFDATEDATA ){
				upperDate = ( (cfDateData) _val2 ).getLong();
			}else{
				// check the 'one-date cache'
				String dateStr2 = _val2.getString();
				if ( lastDateStr2.equals( dateStr2 ) ){
					upperDate = lastDateLong2;
				}else{
					upperDate = dateTimeTokenizer.getNeutralDate( dateStr2 ).getTime();
					lastDateStr2 = dateStr2;
					lastDateLong2 = upperDate;
				}
			}
			return valDate >= lowerDate && valDate <= upperDate;
		}catch( Exception e ){
     	throw new cfmRunTimeException( catchDataFactory.generalException("errorCode.expressionError", 
																																				"queryofqueries.between", 
																																				null ) );
		}
		
	}// compareDates()
	
}// betweenCondition
