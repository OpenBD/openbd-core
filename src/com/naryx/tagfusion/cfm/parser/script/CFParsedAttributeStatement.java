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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFException;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.ParseException;


abstract public class CFParsedAttributeStatement extends CFParsedStatement implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, CFExpression> attributes;


	protected CFParsedAttributeStatement( Token _t, Map<String, CFExpression> _a ) {
		super( _t );
		attributes = _a;
	}


	// utility method used for outputting the attributes to string
	protected void DecompileAttributes( StringBuilder sb ) {
		Iterator<String> attrIt = attributes.keySet().iterator();
		while ( attrIt.hasNext() ) {
			sb.append( " " );
			String nextKey = attrIt.next();
			sb.append( nextKey );
			sb.append( "=" );
			sb.append( attributes.get( nextKey ).Decompile( 0 ) );
		}
	}


	protected boolean containsAttribute( String _k ) {
		return attributes.containsKey( _k );
	}


	/*
	 * checks that all the attributes are in the allowed set, throwing a ParseException
	 * if an unrecognized attribute is found
	 */
	protected void validateAttributes( Token _t, HashSet<String> _allowedKeys ) {

		Iterator<String> it = attributes.keySet().iterator();

		while ( it.hasNext() ) {
			String nextKey = it.next();
			if ( !_allowedKeys.contains( nextKey ) ) {
				throw new ParseException( _t, "Invalid attribute " + nextKey );
			}
		}

	}


	/*
	 * checks that all the attributes are in the allowed set, throwing a CFException with the passed in message
	 * if an unrecognized attribute is found
	 */
	protected void validateAttributesRuntime( CFContext _context, HashSet<String> _allowedKeys, String _msg ) throws CFException {
		Iterator<String> it = attributes.keySet().iterator();

		while ( it.hasNext() ) {
			String nextKey = it.next();
			if ( !_allowedKeys.contains( nextKey ) ) {
				throw new CFException( _msg, _context );
			}
		}
	}


	/*
	 * Returns the value of the named attribute as a String. If the attribute is not present, the _default
	 * is returned.
	 */
	protected String getAttributeValueString( CFContext _context, String _name, String _default ) throws dataNotSupportedException, cfmRunTimeException {
		cfData valueData = getAttributeValue( _context, _name, false );
		if ( valueData != null ) {
			return valueData.getString();
		} else {
			return _default;
		}
	}


	/*
	 * Returns the value of the named attribute as a string. Throws a cfmRunTimeException if the attribute is
	 * not present or cannot be treated as a string
	 */
	protected String getAttributeValueString( CFContext _context, String _name ) throws dataNotSupportedException, cfmRunTimeException {
		return getAttributeValue( _context, _name ).getString();
	}


	/*
	 * Returns the value of the named attribute as a boolean. If the attribute is not present, the default value is return
	 * Throws a cfmRunTimeException if the attribute cannot be treated as a string
	 */
	protected boolean getAttributeValueBoolean( CFContext _context, String _name, boolean _default ) throws dataNotSupportedException, cfmRunTimeException {
		cfData valueData = getAttributeValue( _context, _name, false );
		if ( valueData != null ) {
			return valueData.getBoolean();
		} else {
			return _default;
		}
	}


	/*
	 * Returns the value of the named attribute as an int. If the attribute is not present, the default value is return
	 * Throws a cfmRunTimeException if the attribute cannot be treated as an int
	 */
	protected int getAttributeValueInt( CFContext _context, String _name, int _default ) throws dataNotSupportedException, cfmRunTimeException {
		cfData valueData = getAttributeValue( _context, _name, false );
		if ( valueData != null ) {
			return valueData.getInt();
		} else {
			return _default;
		}
	}


	protected cfData getAttributeValue( CFContext _context, String _name ) throws cfmRunTimeException {
		return ( (CFExpression) attributes.get( _name ) ).EvalFully( _context );
	}


	/*
	 * evaluates the CFExpression for the given attribute and returns the cfData value
	 * returns null if the named attribute doesn't exist
	 */
	protected cfData getAttributeValue( CFContext _context, String _name, boolean _error ) throws cfmRunTimeException {
		CFExpression exp = attributes.get( _name );
		if ( exp == null ) {
			if ( _error ) {
				throw new CFException( "Missing " + _name + " attribute is required", _context );
			}

			return null;
		}

		return exp.EvalFully( _context );
	}


	protected Iterator<String> getAttributeKeyIterator() {
		return attributes.keySet().iterator();
	}

}
