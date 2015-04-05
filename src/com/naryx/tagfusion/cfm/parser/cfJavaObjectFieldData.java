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


/**
 * This class holds a reference to a field in a Java object
 * instance allowing for the getting and setting of the field.
 *
 */

package com.naryx.tagfusion.cfm.parser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class cfJavaObjectFieldData extends cfLData implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private cfJavaObjectData object = null;
	private Field field = null;

	private String fieldname;
	private Method getMethod;
	private Method setMethod;

	public cfJavaObjectFieldData( cfJavaObjectData _object, String _field,
	    Method _getMethod, Method _setMethod ) {
		super( null );
		object = _object;
		fieldname = _field;
		getMethod = _getMethod;
		setMethod = _setMethod;
		if ( _getMethod == null ) {
			exists = false;
		} else {
			exists = true;
		}
	}

	public cfJavaObjectFieldData( cfJavaObjectData _object, Field _field ) {
		super( null );
		object = _object;
		field = _field;
		exists = true;
	}

	// returns a cfData version of the value the field holds
	public cfData Get( CFContext context ) throws cfmRunTimeException {
		if ( field != null ) {
			try {
				return tagUtils.convertToCfData( field.get( getInstance( field ) ) );
			} catch ( IllegalAccessException iae ) {
				String msg = "Failed to access Object field. " + iae.getMessage();
				throw new CFException( msg, context );
			}
		} else if ( getMethod != null ) {
			try {
				return tagUtils.convertToCfData( getMethod.invoke( getInstance( getMethod ), null ) );

				// the IllegalArgumentException/IllegalAccessException exceptions
				// shouldn't occur but log them if they do
				// and throw an exception as though the field doesn't exist
			} catch ( IllegalArgumentException e ) {
				cfEngine.log( "Unexpected IllegalArgumentException when attempting to invoke java bean get method \""
				        + getMethod.getName() + "\"." );
			} catch ( IllegalAccessException e ) {
				cfEngine.log( "Unexpected IllegalArgumentException when attempting to invoke java bean get method \""
				        + getMethod.getName() + "\"." );
			} catch ( InvocationTargetException e ) {
				throw new CFException( "Exception thrown when setting Object field using javabean get method. "
				        + e.getTargetException().getClass().getName() + ": "
				        + e.getTargetException().getMessage(), context );
			}
		}

		throw new CFException( "No such field " + fieldname, context );

	}

	/**
	 * Throws an exception if try and set the field to a value that cannot be
	 * converted to a valid value for the given type of this field
	 */

	public void Set( cfData val, CFContext context ) throws cfmRunTimeException {
		if ( field != null ) {
			Object javaArg = tagUtils.convertCFtoJava( val, field.getType() );
			if ( javaArg == null ) {
				throw new CFException( "Failed to convert CF data type to the required type.", context );
			}
			try {
				field.set( getInstance( field ), javaArg );
			} catch ( Exception e ) {
				throw new CFException( "Failed to set Object field. (reason = "
				    + e.getMessage() + ")", context );
			}
		} else if ( setMethod != null ) {
			try {
				Object arg = tagUtils.convertCFtoJava( val, setMethod.getParameterTypes()[0] );
				if ( arg == null ) {
					throw new CFException(
					    "Failed to convert CF data type to the required type.", context );
				}
				setMethod.invoke( getInstance( setMethod ), new Object[] { arg } );
				// the IllegalArgumentException/IllegalAccessException exceptions
				// shouldn't occur but log them if they do
				// and throw an exception as though the field doesn't exist
			} catch ( IllegalArgumentException e ) {
				cfEngine.log( "Unexpected IllegalArgumentException when attempting to invoke java bean set method \""
				        + setMethod.getName() + "\"." );
			} catch ( IllegalAccessException e ) {
				cfEngine.log( "Unexpected IllegalArgumentException when attempting to invoke java bean set method \""
				        + setMethod.getName() + "\"." );
			} catch ( InvocationTargetException e ) {
				throw new CFException(
				    "Exception thrown when setting Object field using javabean set method. "
				        + e.getTargetException().getClass().getName() + ": "
				        + e.getTargetException().getMessage(), context );
			}
		} else {
			throw new CFException( "No such field " + fieldname, context );
		}

	}

	private Object getInstance( Field _f ) throws cfmRunTimeException {
		if ( !Modifier.isStatic( _f.getModifiers() ) ) {
			return object.getInstance();
		} else {
			return null;
		}
	}

	private Object getInstance( Method _m ) throws cfmRunTimeException {
		if ( !Modifier.isStatic( _m.getModifiers() ) ) {
			return object.getInstance();
		} else {
			return null;
		}
	}

	public void Delete( CFContext context ) throws cfmRunTimeException {
	}

}
