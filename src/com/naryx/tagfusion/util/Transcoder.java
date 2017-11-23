/* 
 *  Copyright (C) 2017 CodeArcs Inc.
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
 *  $Id:$
 */
package com.naryx.tagfusion.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class Transcoder {

	/** Decode, uncompress and get the bytes from a Base64 string. */
	public static Object fromString( String s ) {
		if ( s == null ) {
			return null;
		}

		byte[] bytes = Base64.getDecoder().decode( s );

		try ( ByteArrayInputStream bais = new ByteArrayInputStream( bytes ); GZIPInputStream gis = new GZIPInputStream( bais ); ObjectInputStream ois = new ObjectInputStream( gis ) ) {

			return ois.readObject();

		} catch ( IOException | ClassNotFoundException e ) {
			throw new RuntimeException( "fromString() failed", e );
		}
	}


	/** Serialise, compress and write the given object to a Base64 string. */
	public static String toString( Serializable o ) {
		if ( o == null ) {
			return null;
		}

		try ( ByteArrayOutputStream baos = new ByteArrayOutputStream(); GZIPOutputStream gzos = new GZIPOutputStream( baos ); ObjectOutputStream oos = new ObjectOutputStream( gzos ) ) {

			oos.writeObject( o );

			/*
			 * We must to call close() on the GZIPOutputStream
			 * For some reason the try / catch resource management is not enough.
			 * Deleting this close causes fromString to fail.
			 */
			close( gzos );

			return Base64.getEncoder().encodeToString( baos.toByteArray() );

		} catch ( IOException e ) {
			throw new RuntimeException( "toString( Serializable o ) failed: ", e );
		}

	}


	/**
	 * Close a closeable.
	 */
	private static void close( Closeable closeable ) {
		if ( closeable != null ) {
			try {
				closeable.close();
			} catch ( Exception e ) {
				throw new RuntimeException( "Unable to close " + closeable + "\n" + e );
			}
		}
	}


}
