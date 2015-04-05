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

package com.naryx.tagfusion.cfm.sql;

import java.util.zip.CRC32;

public class checkSum extends CRC32 {

	public checkSum()
	{
		super();
	}

	public void update( int i )
	{
		byte[] buffy = new byte[4];
		buffy[0] = (byte) ( i >>> 24 ) ;
		buffy[1] = (byte) ( i >>> 16 ) ;
		buffy[2] = (byte) ( i >>> 8 ) ;
		buffy[3] = (byte) ( i ) ;
		update( buffy );
	}

	public void update( byte b )
	{
		// I would call super.update( b ) except that if b is negative, I would be concerned that
		// the negation might get lost in the translation.
		byte[] buffy = new byte[1];
		buffy[0] = b ;
		update( buffy );
	}

	public void update( char c )
	{
		byte[] buffy = new byte[2];
		buffy[0] = (byte) ( c >>> 8 ) ;
		buffy[1] = (byte) ( c ) ;
		update( buffy );
	}

	public void update( long i )
	{
		byte[] buffy = new byte[8];
		buffy[0] = (byte) ( i >>> 56 ) ;
		buffy[1] = (byte) ( i >>> 48 ) ;
		buffy[2] = (byte) ( i >>> 40 ) ;
		buffy[3] = (byte) ( i >>> 32 ) ;
		buffy[4] = (byte) ( i >>> 24 ) ;
		buffy[5] = (byte) ( i >>> 16 ) ;
		buffy[6] = (byte) ( i >>> 8 ) ;
		buffy[7] = (byte) ( i ) ;
		update( buffy );
	}

	public void update( double d )
	{
		// convert all the bits of d to fit into a long, then have update( long ) do the hard work
		update( Double.doubleToLongBits( d ) );
	}

	public void update( float f )
	{
		// convert all the bits of f to fit into an int, then have update( int ) do the hard work
		update( Float.floatToIntBits( f ) );
	}

	public void update( boolean b )
	{
		byte byteVal = 0 ;
		if ( b )
		{
			byteVal = 1 ;

		}
		byte[] buffy = new byte[1];
		buffy[0]= byteVal ;
		update( buffy );
	}

	public void update( String s )
	{
		// copy all of the chars in s to a char array and then copy
		// both bytes of every char to a byte array.  Then work all
		// of those bytes into the CRC
		int len = s.length();
		char[] chars = new char[ len ];
		s.getChars( 0 , len , chars , 0 );
		byte[] buffy = new byte[ len * 2 ];
		int i ;
		for( i = 0 ; i < len ; i++ )
		{
			int buffyIndex = i * 2 ;
			char c = chars[ i ];
			buffy[ buffyIndex ] = (byte) ( c >>> 8 ) ;
			buffy[ buffyIndex + 1 ] = (byte) ( c ) ;
		}
		update( buffy );
	}

	public int getInt31()
	{
		return (int) ( getValue() & Integer.MAX_VALUE ) ;
	}
    
	public static int quick( String s )
	{
		checkSum crc = new checkSum();
		crc.update( s );   
		return crc.getInt31();
	}
}
