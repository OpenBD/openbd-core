/* 
 * Copyright (C) 2012 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: Javacast.java 2212 2012-07-27 00:34:22Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

import java.lang.reflect.Array;
import java.math.BigDecimal;

public class Javacast {

	public static enum Datatype { NULL, INT, LONG, BOOLEAN, DOUBLE, FLOAT, STRING, BIGDECIMAL, 
		BYTE, CHAR, SHORT, CUSTOM	};

	public static final Javacast NULL = new Javacast( Datatype.NULL );
	public static final Javacast INT = new Javacast( Datatype.INT );
	public static final Javacast LONG = new Javacast( Datatype.LONG );
	public static final Javacast DOUBLE = new Javacast( Datatype.DOUBLE );
	public static final Javacast FLOAT = new Javacast( Datatype.FLOAT );
	public static final Javacast BOOLEAN = new Javacast( Datatype.BOOLEAN );
	public static final Javacast STRING = new Javacast( Datatype.STRING );
	public static final Javacast BIGDECIMAL = new Javacast( Datatype.BIGDECIMAL );
	public static final Javacast BYTE = new Javacast( Datatype.BYTE );
	public static final Javacast CHAR = new Javacast( Datatype.CHAR );
	public static final Javacast SHORT = new Javacast( Datatype.SHORT );

	public static final Javacast INT_ARRAY = new Javacast( Datatype.INT, true );
	public static final Javacast LONG_ARRAY = new Javacast( Datatype.LONG, true );
	public static final Javacast DOUBLE_ARRAY = new Javacast( Datatype.DOUBLE, true );
	public static final Javacast FLOAT_ARRAY = new Javacast( Datatype.FLOAT, true );
	public static final Javacast BOOLEAN_ARRAY = new Javacast( Datatype.BOOLEAN, true );
	public static final Javacast STRING_ARRAY = new Javacast( Datatype.STRING, true );
	public static final Javacast BIGDECIMAL_ARRAY = new Javacast( Datatype.BIGDECIMAL, true );
	public static final Javacast BYTE_ARRAY = new Javacast( Datatype.BYTE, true );
	public static final Javacast CHAR_ARRAY = new Javacast( Datatype.CHAR, true );
	public static final Javacast SHORT_ARRAY = new Javacast( Datatype.SHORT, true );

	
	private Datatype type;
	private boolean isArray;
	private Class customClass;


	private Javacast( Datatype _type ) {
		this( _type, false ); 
	}
	

	private Javacast( Datatype _type, boolean _isArray ) {
		type = _type;
		isArray = _isArray;
	}

	public Javacast( Class _class, boolean _isArray ) {
		type = Datatype.CUSTOM;
		customClass = _class;
		isArray = _isArray;
	}

	
	public boolean isArray(){
		return isArray;
	}

	
	public Datatype getDatatype(){
		return type;
	}

	public Class getCustomClass(){
		return customClass;
	}
	
	public Class getCastClass(){
		switch ( type ){
			case STRING:
				if ( isArray )
					return String[].class;
				else
					return String.class;
			case INT:
				if ( isArray )
					return int[].class;
				else
					return int.class;
			case LONG:
				if ( isArray )
					return long[].class;
				else
					return long.class;
			case BOOLEAN:
				if ( isArray )
					return boolean[].class;
				else
					return boolean.class;
			case DOUBLE:
				if ( isArray )
					return double[].class;
				else
					return double.class;
			case FLOAT:
				if ( isArray )
					return float[].class;
				else
					return float.class;
			case BIGDECIMAL:
				if ( isArray )
					return BigDecimal[].class;
				else
					return BigDecimal.class;
			case BYTE:
				if ( isArray )
					return byte[].class;
				else
					return byte.class;
			case CHAR:
				if ( isArray )
					return char[].class;
				else
					return char.class;
			case SHORT:
				if ( isArray )
					return short[].class;
				else
					return short.class;
			case CUSTOM:
				if ( isArray )
					return Array.newInstance( customClass, 0 ).getClass();
				else
					return customClass;
			default:
				break;
		}
		
		return null;
	}

}
