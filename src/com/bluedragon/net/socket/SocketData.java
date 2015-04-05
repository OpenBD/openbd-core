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
 *  http://openbd.org/
 *  $Id: SocketData.java 1903 2011-12-30 20:46:20Z alan $
 */

package com.bluedragon.net.socket;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructReadOnlyData;

public class SocketData extends cfStructReadOnlyData {
	private static final long serialVersionUID = 1L;

	private Socket socket;
	private DataInputStream	br;
	private BufferedOutputStream	bw;
	private boolean throwOnError;

	public SocketData( Socket socket, boolean throwOnError ) throws IOException{
		this.socket 			= socket;
		this.throwOnError	= throwOnError;
		
		br	= new DataInputStream( this.socket.getInputStream() );
		bw	= new BufferedOutputStream( this.socket.getOutputStream() );
		
		setPrivateData("remoteip", 	new cfStringData( this.socket.getInetAddress().getHostAddress() ) );
		setPrivateData("localip", 	new cfStringData( this.socket.getLocalAddress().getHostAddress() ) );
		setPrivateData("connected", cfBooleanData.TRUE );
	}
	
	
	public cfBinaryData	readBytes( int maxSize, int timeout ) throws Exception{
		try {
			socket.setSoTimeout(timeout);
		} catch (SocketException e) {
			disconnect();
			if ( throwOnError )
				throw new Exception(e.getMessage());
			return null;
		}

		try {

			byte[]	bytearr	= new byte[maxSize];
			for ( int x=0; x < maxSize; x++ ){
				try{
					bytearr[x]	= br.readByte();
				}catch ( SocketTimeoutException ste ){
					if ( x > 0 )
						return new cfBinaryData( bytearr, x );
					else
						return new cfBinaryData( new byte[0] );
				}
			}

			return new cfBinaryData( bytearr );
			
		} catch (IOException e) {
			disconnect();
			if ( throwOnError )
				throw new Exception(e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * Reads the read from the remote socket waiting for the given timeout
	 * @param timeout
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public String readLine( int timeout ) throws Exception{
		try {
			socket.setSoTimeout(timeout);
		} catch (SocketException e) {
			disconnect();
			if ( throwOnError )
				throw new Exception(e.getMessage());
			return null;
		}
		
		try {
			return br.readLine();
		} catch ( SocketTimeoutException ste ){
			if ( throwOnError )
				throw new Exception(ste.getMessage());
			return null;
		} catch (IOException e) {
			disconnect();
			if ( throwOnError )
				throw new Exception(e.getMessage());
			return null;
		}
	}

	
	
	/**
	 * Sends a new line
	 * @param line
	 * @return
	 */
	public boolean sendBinaryData( cfBinaryData bindata ) throws Exception{
		try {
			bw.write( bindata.getByteArray() );
			bw.flush();
			return true;
		} catch (IOException e) {
			disconnect();
			if ( throwOnError )
				throw new Exception(e.getMessage());
			return false;
		}
	}
	
	
	
	/**
	 * Sends a new line
	 * @param line
	 * @return
	 */
	public boolean sendString( String line ) throws Exception{
		try {
			bw.write( line.getBytes() );
			bw.flush();
			return true;
		} catch (IOException e) {
			disconnect();
			if ( throwOnError )
				throw new Exception(e.getMessage());
			return false;
		}
	}
	
	
	/**
	 * Sends a new line followed by the carriage return
	 * @param line
	 * @return
	 */
	public boolean sendLine( String line ) throws Exception{
		try {
			bw.write( line.getBytes() );
			bw.write( '\r' );
			bw.write( '\n' );
			bw.flush();
			return true;
		} catch (IOException e) {
			disconnect();
			if ( throwOnError )
				throw new Exception(e.getMessage());
			return false;
		}
	}
	
	
	/**
	 * Disconnects from the remote socket
	 */
	public void disconnect(){
		try {
			br.close();
			bw.close();
			socket.close();
		} catch (Exception e) {}
		socket = null;
		br = null;
		bw = null;
		setPrivateData("connected", cfBooleanData.FALSE );
	}


	public boolean isConnected() {
		return socket != null;
	}
}
