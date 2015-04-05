/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.engine;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import com.nary.util.string;
import com.nary.util.date.dateTimeTokenizer;

/**
 * This class represents string data
 */

public class cfStringData extends cfJavaObjectData {

	static final long serialVersionUID = 1;
  
	public static cfStringData	EMPTY_STRING = new cfStringDataStatic("");
	
	// WARNING! There should be no references to the "data" attribute except in
	// the constructors, the getString() method, and the getLength() method; all
	// other references to "data" should be done via the getString() method
	//
	// The concept here is that if we construct a cfStringData instance using a
	// byte[], we're going to defer converting that byte[] to a string until
	// someone actually needs it by invoking getString(); this saves an enormous
	// amount of processing time for TEXT, LONGVARCHAR, and CLOB columns
	//
	// Note: one of "data" or "dataBytes" will always be null, but never both
	private String data;
	private byte[] dataBytes;
	private char[] dataChars;
    
	private transient cfDateData dateData; // the date representation of this string
	private boolean maybeDateConvertible = true; // assume true until proven otherwise
    
	private transient cfNumberData numberData; // the numeric representation of this string
	private boolean maybeNumberConvertible = true; // assume true until proven otherwise
    
	private transient cfBooleanData booleanData; // the boolean representation of this string
	private boolean maybeBooleanConvertible = true; // assume true until proven otherwise
    
	public cfStringData( String _data ){
		super( _data );
		data = ( _data == null ? "" : _data );
	}
    
	public cfStringData( byte[] _dataBytes ) {
		super( null );
		dataBytes = _dataBytes;
		if ( _dataBytes == null )
			data = "";
	}
    
	public cfStringData( char[] _dataChars ) {
		super( null );
		dataChars = _dataChars;
		if ( _dataChars == null )
			data = "";
	}

	public void setString( String _data ) {
		super.setInstance( _data );
		data = ( _data == null ? "" : _data );
		dataBytes = null;
		dataChars = null;
		
		maybeDateConvertible = true; // assume true until proven otherwise
		dateData = null;
		
		maybeNumberConvertible = true; // assume true until proven otherwise
		numberData = null;
		
		maybeBooleanConvertible = true; // assume true until proven otherwise
		booleanData = null;
	}

	public static cfStringData getString( InputStream in ) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte temp[] = new byte[ 2048 ];
		int bytesRead;
        
		while ( ( bytesRead = in.read( temp ) ) != -1 )
			out.write( temp, 0, bytesRead );
        
		return new cfStringData( out.toByteArray() );
	}

	public static cfStringData getString( Reader in ) throws IOException {
		CharArrayWriter out = new CharArrayWriter();
		char temp[] = new char[ 2048 ];
		int charsRead;
        
		while ( ( charsRead = in.read( temp ) ) != -1 )
			out.write( temp, 0, charsRead );
        
		return new cfStringData( out.toCharArray() );
	}

	public byte getDataType(){ return cfData.CFSTRINGDATA; }
	public String getDataTypeName() { return "string"; }
    
	public String getString() {
		if ( data == null ) {
			// if data is null, one of dataBytes or dataChars will always be non-null
			if ( dataChars != null ) {
				setString( new String( dataChars ) );
			} else {
				setString( new String( dataBytes ) );
			}
        }
        return data;
    }
    
	public int getLength() {
		// data, dataBytes, and dataChars will never all be null
		if ( data != null ) {
			return data.length();
		} else if ( dataChars != null ) {
			return dataChars.length;
		} else {
			return dataBytes.length;
		}
    }
	
	/*
		trimTrailing

		Trims the trailing whitespace from a string.
	*/
	private static String trimTrailing( String s )
	{
		if ( s == null || s.length() == 0 )
			return s;
		
		int pos = s.length() - 1;
		while ( pos > 0 )
		{
			char ch = s.charAt( pos );
			if ( ch != ' ' && ch != '\t' && ch != '\n' && ch != '\r' )
				break;
			pos--;
		}
		
		if ( pos == s.length() - 1 )
			return s;
				
		return s.substring( 0, pos+1 );
	}
			
	public boolean getBoolean() throws dataNotSupportedException {
		if ( isBooleanConvertible() ) {
			return booleanData.getBoolean();
		}
		throw new dataNotSupportedException( "value [" + getString() + "] cannot be converted to a boolean" );
	}
	
	public boolean isBooleanConvertible() {
		return isBooleanConvertible( true );
	}
	
	// has the side-effect of setting the booleanData attribute
	private boolean isBooleanConvertible( boolean convertNumbers ) {
		if ( maybeBooleanConvertible ) {
			if ( booleanData != null ) {
				return true;
			}
			// CFMX will trim any trailing whitespace characters before trying to convert to a boolean
			// so let's do the same.  Refer to bug #2298.
			String data = trimTrailing( getString() );
			String lcaseData = data.toLowerCase();
			if ( lcaseData.equals( "1" ) || lcaseData.equals( "true" ) || lcaseData.equals( "yes" ) ) {
				booleanData = cfBooleanData.TRUE;
				return true; // boolean convertible
			} else if ( lcaseData.equals( "0" ) || lcaseData.equals( "false" ) || lcaseData.equals( "no" ) ) {
				booleanData = cfBooleanData.FALSE;
				return true; // boolean convertible
			} else if ( convertNumbers && isNumberConvertible( false ) ) {
				booleanData = cfBooleanData.getcfBooleanData( numberData.getDouble() != 0.0 );
				return true; // boolean convertible
			}
			maybeBooleanConvertible = false; // not convertible
		}
		return false;
	}
	
	public int getInt() throws dataNotSupportedException {
		return getNumber().getInt();
	}

	public double getDouble() throws dataNotSupportedException {
		return getNumber().getDouble();
	}

	public long getLong() throws dataNotSupportedException {
		return getNumber().getLong();
	}

	public cfNumberData getNumber() throws dataNotSupportedException {
		if ( isNumberConvertible() ) {
			return numberData;
		}
		throw new dataNotSupportedException( "value [" + getString() + "] is not a number" );
	}
	
	// returns true if this string can be converted to a number
	public boolean isNumberConvertible() {
		return isNumberConvertible( true );
	}
	
	// has the side-effect of setting the numberData attribute
	private boolean isNumberConvertible( boolean convertBooleans ) {
		if ( maybeNumberConvertible ) {
			if ( numberData != null ) {
				return true;
			}
			String str = getString().trim();
			if ( string.isNumber( str ) ) {
				numberData = (cfNumberData)cfData.createNumber( str, true );
				return true;
			}
			
			// if can be converted to boolean, then can be converted to number
			if ( convertBooleans && isBooleanConvertible( false ) ) {
				numberData = new cfNumberData( booleanData.getBoolean() ? 1 : 0 );
				return true;
			}
			maybeNumberConvertible = false; // can't be converted
		}
		return false;
	}
	
	public cfData duplicate() {
		return new cfStringData( getString() );
	}
	
	public String toString(){return getString();}
	public String getName(){ return "\"" + getString() + "\""; }
	
	public cfDateData getDateData() throws dataNotSupportedException {
		if ( isDateConvertible() ) {
			return dateData;
		}
		throw new dataNotSupportedException( "Invalid date/time string: " + getString() );	
	}
	
	public long getDateLong() throws dataNotSupportedException {
		if ( isDateConvertible() ) {
			return dateData.getLong();
		}
		return ( getLong() * 86400000 ); // throws dataNotSupportedException if not number convertible
	}
	
	// has the side-effect of setting the dateData attribute
	public boolean isDateConvertible() {
		return isDateConvertible( true );
	}
	
	public boolean isDateConvertible( boolean convertNumbers ) {
		if ( maybeDateConvertible ) {
			if ( dateData != null ) {
				return true;
			}
			String thisString = getString();
			if ( thisString.length() > 0 ) {
				java.util.Date d = dateTimeTokenizer.getUSDate( thisString ); 
				// this could be made more efficient if a more flexible method is added to DTT 
				if ( d == null ) {
					d = dateTimeTokenizer.getUKDate( thisString );
				}
				if ( d == null ) {
					d = dateTimeTokenizer.getNeutralDate( thisString );
				}
				if ( d != null ) {
					dateData = new cfDateData( d.getTime() );
					return true;
				}
				
				// if can be converted to number, then can be converted to date
				if ( convertNumbers && isNumberConvertible( false ) )
				{
					int asInt = numberData.getInt();
					if ( asInt > -693595 && asInt < 2958464)
					{
						dateData = numberData.getDateData();
						return true;
					}
				}
			}
			maybeDateConvertible = false; // can't be converted
		}
        return false;
	}
	
	// this version of equals() is for use by the CFML expression engine
	public boolean equals( cfData _data ) throws cfmRunTimeException{
		if ( _data.getDataType() == cfData.CFSTRINGDATA ){
			return ( (cfStringData) _data ).getString().equals( getString() );
		}else if ( _data.getDataType() == cfData.CFNUMBERDATA ){
			return ( (cfNumberData) _data ).getString().equals( getString() );
		}else{
			return super.equals( _data ); // throw unsupported exception
		}
	}
	
	// this version of equals() is for use by generic Colletions classes
	public boolean equals( Object o )
	{
		if ( o instanceof cfStringData ){
			return ((cfStringData)o).getString().equals( getString() );
		}else if ( o instanceof cfNumberData ){
			return ( (cfNumberData) o ).getString().equals( getString() );
		}
		
		return false;
	}
	
	public int hashCode() {
		return ( getString().hashCode() );
	}

  public void dump( java.io.PrintWriter out, String _label, int _top ){
    dump( out );
  }

	public void dump( java.io.PrintWriter out ) {
        String thisString = getString();
		out.print( thisString.length() > 0 ? com.nary.util.string.escapeHtml( thisString ) : "[empty string]" );
	}
 
	public void dumpWDDX( int version, java.io.PrintWriter out ){
    if ( version > 10 )
      out.write( "<s>" );
    else
      out.write( "<string>" );
    
		char [] chars = getString().toCharArray();
		
		for ( int i = 0; i < chars.length; i++ ){
			switch ( chars[i] ){
				case '<':
					out.write( "&lt;" );
					break;
				case '>':
				  out.write( "&gt;" );
				  break;
				case '&':
			  	out.write( "&amp;" );
			  	break;

				default:
					if ( chars[i] <= 0x1f && chars[i] >= 0 ){
            if ( version > 10 )
              out.write( "<c c='" );
            else
              out.write( "<char code='" );
            
            String hexStr = Integer.toHexString( chars[i] );
						out.write( hexStr.length() == 1 ? "0" + hexStr : hexStr );
						out.write( "'/>" );
					}else{
						out.write( chars[i] );
					}
			}
		}

    if ( version > 10 )
      out.write( "</s>" );
    else
      out.write( "</string>" );
	}
	
	/**
	 * 	private subclass for static instances
	 */
	private static class cfStringDataStatic extends cfStringData {

		private static final long serialVersionUID = 1L;

		public cfStringDataStatic( String s ) {
			super( s );
		}
		
		/**
		 * The following methods are not allowed to be invoked for static instances.
		 */
		public void setQueryTableData( List<List<cfData>> queryTableData, int queryColumn ) {
			throw new UnsupportedOperationException( "static instance" );
		}
		
		public void setExpression( boolean exp ) {
			throw new UnsupportedOperationException( "static instance" );
		}
		
	  protected void setImplicit( boolean implicit ) {
			throw new UnsupportedOperationException( "static instance" );
		}
	}
	
	synchronized protected void createInstance() throws cfmRunTimeException {
   	// another thread may have executed this method whilst this thread waited queued on this method
   	if ( instance != null )
   		return;

		instance = data;
		setCfmlPageContext( this );
	}

	public Class<String> getInstanceClass() {
		return String.class;
	}
		
}
