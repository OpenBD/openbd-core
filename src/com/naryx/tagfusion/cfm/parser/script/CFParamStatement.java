/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.parser.script;

import java.util.HashSet;
import java.util.Map;

import org.apache.oro.text.regex.MalformedPatternException;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFException;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.CFUndefinedValue;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.tag.cfPARAM;

public class CFParamStatement extends CFParsedAttributeStatement implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;

	private static HashSet<String> validAttributes;
	
	static{
		validAttributes = new HashSet<String>();
		validAttributes.add( "DEFAULT" );
		validAttributes.add( "TYPE" );
		validAttributes.add( "NAME" );
		validAttributes.add( "MAX" );
		validAttributes.add( "MIN" );
		validAttributes.add( "PATTERN" );
	}
	
	public CFParamStatement( org.antlr.runtime.Token t, Map<String,CFExpression> _attributes ) {
		super(t,_attributes);
		validateAttributes( t, validAttributes );
	}

	@Override
	public CFStatementResult Exec( CFContext context ) throws cfmRunTimeException {
		setLineCol(context);
		
		String name = getAttributeValue( context, "NAME", true ).getString();
 		if ( name.length() == 0 )
 			throw new CFException( "Invalid NAME attribute", context );

 		cfData data = runTime.runExpression( context.getSession(), name, false );
		if ( ((cfLData)data).exists() ) {
			cfData val = ((cfLData)data).Get( context );
			if ( val != CFUndefinedValue.UNDEFINED ) {
				checkDataType( context, val, name, false );
				return null;
			}
		}

		// variable doesn't exist or is undefined
		if ( !containsAttribute( "DEFAULT" ) ) {
			throw new CFException( "Required variable [" + name + "] is not available", context );
		} else {
			cfData defaultVal = getAttributeValue( context, "DEFAULT" );
			checkDataType( context, defaultVal, name, true );
			((cfLData)data).Set( defaultVal, context );
		}
		

		return null;
	}

	
	private void checkDataType( CFContext _context, cfData data, String _name, boolean isDefault ) throws cfmRunTimeException {
		cfData typeData = getAttributeValue( _context, "TYPE", false ); 
		 
		if ( typeData != null ){
			String type = typeData.getString();
			if ( type.equals( "regex" ) || type.equals( "regular_expression" ) ){ 
				String re = getAttributeValue( _context, "PATTERN", false ).getString();
				try {
					if ( !cfPARAM.validateRE( re, data ) ){
						throw new CFException( ( isDefault ? "Default value" : "Variable" ) + " [" + _name 
								+ "] does not match the regular expression [" + re + "]", _context );
					}
				} catch (MalformedPatternException e) {
					throw new CFException( "Invalid regular expression [" + re + "]", _context );
				}
			
			}else if ( type.equals( "range" ) ){
				validateRange( _context, data);
			
			}else{
				try{
					if ( !cfPARAM.checkDataType( type, data ) ){
						throw new CFException( "Variable [" + _name + "] is not of type [" + type + "]", _context );
					}
				}catch( IllegalArgumentException e ){
					throw new CFException( e.getMessage(), _context );
				}
			}
		}
	}

	/*
	 * Checks that the given value is within the range specified by the min and max params.
	 * If _min is null then only the upper limit will be checked. Similarly if _max is null
	 * only the lower limit will be checked.
	 */
	
	public void validateRange( CFContext _context, cfData _value ) throws cfmRunTimeException{
		
		cfNumberData min = getAttributeValue( _context, "MIN", false ).getNumber();
		cfNumberData max = getAttributeValue( _context, "MAX", false ).getNumber();
		if ( min == null && max == null ){
			throw new CFException( "Missing attributes \"MAX\" and/or \"MIN\" are required when TYPE=\"range\".", _context );
		}
		
		if ( min != null && max != null && cfData.compare( min, max ) > 0 ){
			throw new CFException( "Invalid range: the maximum value must be greater than the minimum value.", _context );
		}
		
		if ( min != null && cfData.compare( _value, min ) < 0 ){
			throw new CFException( "Value must be greater than or equal to " + min.getString(), _context );
		}
		
		if ( max != null && cfData.compare( _value, max ) > 0 ){
			throw new CFException( "Value must be less than or equal to " + max.getString(), _context );
		}
		
	}
  
	@Override
	public String Decompile( int indent ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "param" );
		DecompileAttributes( sb );
		return sb.toString();
	}
}
