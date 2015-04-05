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

package com.naryx.tagfusion.expression.function;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class numberFormat extends functionBase {

	private static final long serialVersionUID = 1L;
	
	private static byte LEFT = 0, CENTER = 1, RIGHT = 2;
	
  public numberFormat(){
     min = 1; max = 2;  
  }
  
  public String[] getParamInfo(){
		return new String[]{
			"date1",
			"format mask - default #,###"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"format", 
				"Formats a number to the given format mask", 
				ReturnType.STRING );
	}
   
  
	public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
    if ( parameters.size() == 1 ){
			return defaultExecute( _session, parameters, new DecimalFormatSymbols( Locale.US ) );
		}else{
			return mainExecute( _session, parameters, new DecimalFormatSymbols( Locale.US ), Locale.US );
		}
	}
  

	protected cfData defaultExecute( cfSession _session, List<cfData> parameters, DecimalFormatSymbols _dfs )throws cfmRunTimeException{
		double val = getFormatValue( _session, parameters.get(0) );
		DecimalFormat DF = new DecimalFormat("#,###", _dfs);
		DF.setGroupingSize( 3 );
		return new cfStringData( DF.format( val ) );
		
	}
	
	private double getFormatValue( cfSession _session, cfData _param ) throws cfmRunTimeException{
		double val = 0;
		if ( _param.getDataType() == cfData.CFJAVAOBJECTDATA && ( (cfJavaObjectData) _param ).getInstance() instanceof Number ){
			val = ( (Number) ( (cfJavaObjectData) _param ).getInstance() ).doubleValue();
		}else{
			try{
				val = _param.getDouble();
			}catch(dataNotSupportedException e){
				String s = _param.getString();
				if(s.trim().length() > 0)
					throwException(_session, "Parameter \"" + _param.getString() + "\" is not a numeric.");
			}
		}
		return val;
	}

	// use if parameters.size() == 2	
	protected cfData mainExecute( cfSession _session, List<cfData> parameters, DecimalFormatSymbols _dfs, Locale _locale )throws cfmRunTimeException{
		byte justification = RIGHT;
	
		// the mask to use for formating
  		String mask		= parameters.get(0).getString();
		
		double val = getFormatValue( _session, parameters.get(1) );
		boolean useBrackets 	= false; // set when a '(' or ')' is found in the mask
		boolean usePlus				= false; // set when a '+' is found in the mask
		boolean useMinus			= false; // set when a '-' is found in the mask
		boolean useDollar 		= false; // set when a '$' is found in the mask
		boolean useComma			= false; // set when a comma is found in the mask
		boolean foundDecimal 	= false; // indicates that the first '.' has been found 
		boolean symbolsFirst  = true; // used to decide whether to justify or apply symbols first
    	boolean foundZero     = false; // set when a 0 found
  	
		int maskLength = mask.length(); // used to pad the string with spaces at the end
		
		if ( maskLength == 0 ){
			throwException( _session, "Invalid mask. The mask provided was 0 chars in length." );
		}
		
		// preprocess - if one '0' then replace all '_' with '0' (but only if the '0' appears after any '.')
    int zeroIndx = mask.indexOf( '0' );
		if ( zeroIndx != -1 ){
      int periodIndx = mask.indexOf( '.' );
      if ( periodIndx == -1 || periodIndx > zeroIndx )
        mask = mask.replace( '_', '0' );
		}
		
		StringBuilder maskBuffer = new StringBuilder( mask );
		if ( maskBuffer.charAt( 0 ) == '_' && maskLength > 1 && 
				( maskBuffer.charAt( 1 ) == '$' || maskBuffer.charAt( 1 ) == '+' ||
					maskBuffer.charAt( 1 ) == '-' || maskBuffer.charAt( 1 ) == '(' ) ) {
			symbolsFirst = true;
		} else {
			for ( int i = 0; i < maskBuffer.length(); i++ ){
				char nextCh = maskBuffer.charAt(i);
				if ( nextCh == '_' || nextCh == '+' || nextCh == '(' ){
					symbolsFirst = false;
					break;
				}
			}
		}

		int zeroCount = 0; // keep a count of the 0's that appear before the decimal point
		int i = 0;
		while ( i < maskBuffer.length() ){
			boolean removeChar = false;
			switch ( maskBuffer.charAt(i) ){
				case '_':
					if ( foundDecimal ){
						maskBuffer.setCharAt( i, '0' );
					}else{
						maskBuffer.setCharAt( i, '#' );
					}
					break;
				case '9':
					if ( foundDecimal || foundZero ){
						maskBuffer.setCharAt( i, '0' );
						if ( !foundDecimal )
							zeroCount++;
					}else{
						maskBuffer.setCharAt( i, '#' );
					}
					break;
				case '.':
					if ( foundDecimal ){
						removeChar = true;
					}else{
						foundDecimal = true;
					}
					break;
				case '0':
     			foundZero = true;
          if ( !foundDecimal )
            zeroCount++;
					break;
				case '(':
				case ')':
					useBrackets = true;
					removeChar = true;
					break;
				case '+':
					usePlus = true;
					removeChar = true;
					break;
				case '-':
					useMinus = true;
					removeChar = true;
					break;
				case ',':
					useComma = true;
					removeChar = true;
					maskLength++; // don't want the mask length to be effected
				  break;
				case 'L':
					justification = LEFT;
					removeChar = true;
					break;
				case 'C':
					justification = CENTER;
					removeChar = true;
					break;
				case '$':
					useDollar = true;
					removeChar = true;
					break;
				case '^':
					removeChar = true;
					break;
				default:
				  throwException( _session, parameters.get(0).getString() +
						" is an invalid mask. Valid chars are '_', '9', '.', '0', '(', ')', '+', '-', ',', 'L', 'C', '$' and '^'." );
			}
			
			if ( removeChar ){
                maskBuffer = maskBuffer.deleteCharAt( i );
				maskLength--;
				continue;
			}
			i++;
		}
		
		mask = new String( maskBuffer );	
		java.text.DecimalFormat DF = null;
		try{
      DF = new java.text.DecimalFormat(mask,_dfs);
    }catch( IllegalArgumentException e ){
      throwException( _session, parameters.get(0).getString() + " is an invalid mask." );
    }

    if ( (int) val == 0 ){ 
      DF.setMinimumIntegerDigits( zeroCount>0 ? zeroCount : 1 );
    }
    
    if ( useComma ){
      DF.setGroupingUsed(true);
      DF.setGroupingSize( 3 );
    }
    
		// note absolute number is passed to format(). '-' is added in later if needed
		String formattedNum = null;
    formattedNum = DF.format( Math.abs( val ) );

    StringBuilder formattedNumBuffer = new StringBuilder( formattedNum );
		
		if ( symbolsFirst ){
			int widthBefore = formattedNumBuffer.length();
			applySymbolics( formattedNumBuffer, val, usePlus, useMinus, useDollar, useBrackets, _dfs, _locale );
			int offset = formattedNumBuffer.length() - widthBefore;
			applyJustification( formattedNumBuffer, justification, maskLength+offset );
		}else{
			applyJustification( formattedNumBuffer, justification, maskLength );
			applySymbolics( formattedNumBuffer, val, usePlus, useMinus, useDollar, useBrackets , _dfs, _locale );
		}
		formattedNum = formattedNumBuffer.toString();

    return new cfStringData( formattedNum );
  }
  
	
	private void applyJustification( StringBuilder _buffer, int _just, int _width ){
		if ( _buffer.length() < _width ){
			int padding = _width - _buffer.length();
			// apply justification
			if ( _just == CENTER ){
				centerJustify( _buffer, padding );
			}else if ( _just == LEFT ){
				leftJustify( _buffer, padding );
			}else{
				rightJustify( _buffer, padding );
			}
		}
	}// applyJustification
	
	
	// handles all the addition of +, -, $ and brackets to the number in the StringBuffer
	private static void applySymbolics( StringBuilder _buffer, double _no, boolean _usePlus,  
																boolean _useMinus, boolean _useDollar, boolean _useBrackets, 
                                DecimalFormatSymbols _dfs, Locale _locale ){
		if ( _useBrackets && _no < 0.0 ){
			_buffer.insert( 0, '(' );
			_buffer.append( ')' );
		}
		
		if ( _usePlus ){
			_buffer.insert( 0, ( _no >= 0 ? '+' : _dfs.getMinusSign() ) );
		}
		
		
		if ( _no < 0 && !_useBrackets && !_usePlus ){
			_buffer.insert( 0, _dfs.getMinusSign() );
		}else if ( _useMinus ){
			_buffer.insert( 0, ' ' );
		}
		
		if ( _useDollar ){ 
			_buffer.insert( 0, new DecimalFormatSymbols(_locale).getCurrencySymbol() );
		}
		
	}
	
	
	// pads out the stringbuffer so the contents are center justified with 
	// _padding no of spaces	
	private void centerJustify( StringBuilder _src, int _padding ){
		int padSplit = _padding / 2 + 1;
		rightJustify( _src, padSplit );
		leftJustify( _src, padSplit );
	}

	// pads out the stringbuffer so the contents are right justified with 
	// _padding no of spaces	
	private static void rightJustify( StringBuilder _src, int _padding ){
		for ( int x=0; x < _padding; x++ )
			_src.insert( 0, ' ' );
	}
	
	// pads out the stringbuffer so the contents are left justified with 
	// _padding no of spaces	
	private static void leftJustify( StringBuilder _src, int _padding ){
		for ( int x=0; x < _padding; x++ )
			_src.append( ' ' );
	}
		
  
}
