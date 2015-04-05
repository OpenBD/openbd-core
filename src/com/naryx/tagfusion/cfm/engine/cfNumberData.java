/* 
 *  Copyright (C) 2000 - 2014 TagServlet Ltd
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
 *  $Id: cfNumberData.java 2429 2014-03-30 19:44:57Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

import com.nary.util.NumberUtils;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.parser.CFContext;

/**
 * The cfNumberData class represents numbers in tagServlet.
 */

public class cfNumberData extends cfJavaObjectData {

	static final long							serialVersionUID	= 1;

	private double								doubleNo;
	private int										intNo;
	private boolean								bIntNo						= false;
	private boolean								modified					= false;
	private boolean								isJavaNumeric			= false;

	private String								image;

	private transient cfDateData	dateData;



	/**
	 * In all instances of the constructor a call to super("") is made.
	 * This is because the returned String from a call to getClass().getName()
	 * on a cfNumber has to return "java.lang.String".
	 */
	public cfNumberData( double _data ) {
		this( _data, null );
	}



	public cfNumberData( double _data, String _image ) {
		super( string.EMPTY_STRING );
		doubleNo = _data;
		bIntNo = false;
		image = _image;
	}



	/*
	 * When the float constructor was added, long values would call it instead of the
	 * double constructor causing the value to be truncated. This constructor was added
	 * to force a long to be cast to a double.
	 */
	public cfNumberData( long _data ) {
		super( string.EMPTY_STRING );
		doubleNo = _data;
		bIntNo = false;
		this.image = String.valueOf( _data );
	}



	public cfNumberData( Long _data ) {
		super( _data );
		isJavaNumeric = true;
		doubleNo = _data.longValue();
		bIntNo = false;
		this.image = String.valueOf( _data );
	}



	/*
	 * This constructor causes a float to be more accurately coerced to a double.
	 */
	public cfNumberData( Float _data ) {
		super( _data );
		isJavaNumeric = true;
		doubleNo = ( new Double( String.valueOf( _data.floatValue() ) ) ).doubleValue();
		bIntNo = false;
	}



	public cfNumberData( Double _data ) {
		super( _data );
		isJavaNumeric = true;
		doubleNo = _data.doubleValue();
		bIntNo = false;
	}



	public cfNumberData( Integer _data ) {
		super( _data );
		isJavaNumeric = true;
		intNo = _data.intValue();
		bIntNo = true;
	}



	public cfNumberData( Short _data ) {
		super( _data );
		isJavaNumeric = true;
		intNo = _data.shortValue();
		bIntNo = true;
	}



	public cfNumberData( int _data ) {
		this( _data, null );
	}



	public cfNumberData( int _data, String _image ) {
		super( string.EMPTY_STRING );
		intNo = _data;
		bIntNo = true;
		image = _image;
	}



	public cfNumberData( cfNumberData _data ) {
		super( string.EMPTY_STRING );
		bIntNo = _data.bIntNo;
		intNo = _data.intNo;
		doubleNo = _data.doubleNo;
		instance = _data.instance;
	}



	public byte getDataType() {
		return cfData.CFNUMBERDATA;
	}



	public String getName() {
		return getString();
	}



	public String getDataTypeName() {
		return "number";
	}



	public boolean isInt() {
		return bIntNo;
	}



	public boolean isJavaNumeric() {
		return isJavaNumeric;
	}



	public void add( int inc ) {
		set( intNo + inc );
	}



	public void add( double inc ) {
		set( doubleNo + inc );
	}



	public boolean isBooleanConvertible() {
		return true;
	}



	public boolean getBoolean() {
		if ( ( bIntNo && intNo == 0 ) || ( !bIntNo && doubleNo == 0 ) )
			return false;
		else
			return true;
	}



	public boolean isDateConvertible() {
		return true;
	}



	public cfDateData getDateData() {
		if ( dateData == null ) {
			dateData = cfDateData.createDateFromDays( getDouble() );
		}
		return dateData;
	}



	public long getDateLong() {
		return ( long ) ( ( bIntNo ? intNo : doubleNo ) * 86400000 );
	}



	public boolean isNumberConvertible() {
		return true;
	}



	public double getDouble() {
		return ( bIntNo ? ( double )intNo : doubleNo );
	}



	public long getLong() {
		return ( bIntNo ? ( long )intNo : ( long )doubleNo );
	}



	public int getInt() {
		return ( bIntNo ? intNo : ( int )doubleNo );
	}



	public void set( int _newValue ) {
		intNo = _newValue;
		bIntNo = true;
		modified = true;
		dateData = null;
		image = null;
	}



	public void set( double _newValue ) {
		doubleNo = _newValue;
		bIntNo = false;
		modified = true;
		dateData = null;
		image = null;
	}



	public cfData duplicate() {
		if ( bIntNo )
			return new cfNumberData( intNo, image );
		else
			return new cfNumberData( doubleNo, image );
	}



	public String getString() {
		if ( image != null ) {
			return image;
		} else if ( bIntNo ) {
			return String.valueOf( intNo );
		} else {
			return NumberUtils.getString( doubleNo );
		}
	}



	public cfNumberData getNumber() {
		return this;
	}



	public static cfNumberData getNumber( Number _n ) {
		return new cfNumberData( _n.doubleValue() );
	}



	public static cfNumberData getNumber( Long _l ) {
		return new cfNumberData( _l.intValue() );
	}



	public static cfNumberData getNumber( Float _f ) {
		return new cfNumberData( _f.doubleValue() );
	}



	// this version of equals() is for use by the CFML expression engine
	public boolean equals( cfData _data ) throws cfmRunTimeException {
		if ( _data.getDataType() == cfData.CFNUMBERDATA ) {
			cfNumberData d2 = ( cfNumberData )_data;
			if ( d2.isInt() ) {
				return d2.getInt() == ( bIntNo ? intNo : doubleNo );
			} else {
				return d2.getDouble() == ( bIntNo ? intNo : doubleNo );
			}
		} else if ( _data.getDataType() == cfData.CFSTRINGDATA ) {
			return getString().equals( _data.getString() );
		} else {
			return super.equals( _data ); // throw an exception
		}
	}



	// this version of equals() is for use by generic Collections classes
	public boolean equals( Object o ) {
		if ( o instanceof cfNumberData ) {
			cfNumberData d2 = ( cfNumberData )o;
			if ( d2.isInt() ) {
				return d2.getInt() == ( bIntNo ? intNo : doubleNo );
			} else {
				return d2.getDouble() == ( bIntNo ? intNo : doubleNo );
			}
		} else if ( o instanceof cfStringData ) {
			return getString().equals( ( ( cfStringData )o ).getString() );
		}

		return false;
	}



	public int hashCode() {
		return ( bIntNo ? new Integer( intNo ).hashCode() : new Double( doubleNo ).hashCode() );
	}



	/**
	 * The following methods override their corresponding methods from cfJavaObjectData
	 * allowing for the instance attribute to be updated prior to the call.
	 * This means that the instance field is only being updated when a Java method
	 * is called which is highly unlikely.
	 */

	public cfData getJavaData( javaMethodDataInterface _method, CFContext _context ) throws cfmRunTimeException {
		if ( modified || instance == null || instance.equals( string.EMPTY_STRING ) )
			instance = bIntNo ? ( String.valueOf( intNo ) ) : ( String.valueOf( doubleNo ) );
		return super.getJavaData( _method, _context );
	}



	public cfData getJavaData( String _methodOrField ) throws cfmRunTimeException {
		if ( modified || instance == null || instance.equals( string.EMPTY_STRING ) )
			instance = bIntNo ? ( String.valueOf( intNo ) ) : ( String.valueOf( doubleNo ) );
		return super.getJavaData( _methodOrField );
	}



	public cfData getJavaData( cfData _Field ) throws cfmRunTimeException {
		if ( modified || instance == null || instance.equals( string.EMPTY_STRING ) )
			instance = bIntNo ? ( String.valueOf( intNo ) ) : ( String.valueOf( doubleNo ) );
		return super.getJavaData( _Field );
	}



	public void dump( java.io.PrintWriter out, String _label, int _top ) {
		out.print( this.getString() );
	}



	public String toString() {
		return getString();
	}



	public void dumpWDDX( int version, java.io.PrintWriter out ) {
		if ( version > 10 ) {
			out.write( "<n>" );
			out.write( getString() );
			out.write( "</n>" );
		} else {
			out.write( "<number>" );
			out.write( getString() );
			out.write( "</number>" );
		}
	}



	public Class<?> getInstanceClass() {
		// in the instance where this cfNumberData is precompiled then the instanceClass will be null
		// therefore we need to check if it is, and if so return String.class (note that isJavaNumeric
		// will be false invariably in this case hence String.class can safely be returned)
		Class<?> instCls = super.getInstanceClass();
		if ( instCls == null )
			return String.class;
		else
			return instCls;
	}

}
