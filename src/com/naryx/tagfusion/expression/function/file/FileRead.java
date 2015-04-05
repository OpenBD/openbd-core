/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  
 *  $Id: FileRead.java 2312 2013-01-26 12:03:14Z alan $
 */


package com.naryx.tagfusion.expression.function.file;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;

import org.aw20.io.ByteArrayOutputStreamRaw;

import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;
import com.naryx.tagfusion.expression.function.functionBase;

public class FileRead extends functionBase {
  private static final long serialVersionUID = 1L;

  public FileRead(){ min = 1; max = 2; }
  
	public String[] getParamInfo(){
		return new String[]{
			"srcFile / file object",
			"readsize"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"file", 
				"If a file object is passed in, will read the number of bytes given.  If a path to a file name will read in the full file into memory.  For reading text based files", 
				ReturnType.STRING );
	}
  
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
  	
  	cfVFSData	fileObj = null;
  	boolean bReadFullFile = false;
  	int bufSize = -1;
  	
  	try{
  	
  		/* Setup the file */
	  	if ( parameters.size() == 1 ){
	  		
	  		if ( parameters.get(0) instanceof cfVFSData ){
	  			fileObj = (cfVFSData)parameters.get(0);
	  		}else{
	  			String src = parameters.get(0).getString();
	  			
	  			if ( src.startsWith("http") )
	  				return readFromUrl(src,null);
	  			
	  			fileObj	= new cfVFSData( src, "read", null );
	  			bReadFullFile	= true;
	  		}
	  		
	  	}else{
	  		
	  		if ( parameters.get(1) instanceof cfVFSData ){
	  			fileObj = (cfVFSData)parameters.get(1);
	  			bufSize	= parameters.get(0).getInt();
	  		}else{
	  			String src 			= parameters.get(1).getString();
	  			String charset 	= parameters.get(0).getString();
	  			
	  			if ( src.startsWith("http") )
	  				return readFromUrl(src,charset);
	  			
	  			fileObj	= new cfVFSData( src, "read", charset );
	  			bReadFullFile	= true;
	  		}
	  	}

	  	
	  	/* Check to make sure the file is opened in read only mode */
	  	if ( !fileObj.isReadable() ){
	  		throwException( _session, "this file object is not opened in read mode" );
	  	}

	  	
	  	/* Read the file */
	  	if ( bReadFullFile || bufSize == -1 ){
	  		if ( fileObj.isBinaryMode() )
	  			return new cfBinaryData( readFullFileBinary(fileObj) );
	  		else
	  			return new cfStringData( readFullFileAscii(fileObj) );
	  	}else{

	  		if ( fileObj.isBinaryMode() ){
	  			byte[] bf	= new byte[ bufSize ];
	  			int no = fileObj.getStreamReader().read( bf, 0, bufSize);
	  			if ( no == -1 ){
	  				fileObj.setEOF();
	  				return new cfBinaryData( new byte[0] );
	  			}else{
	  				return new cfBinaryData( bf, no );	
	  			}
	  			
	  		}else{
	  			char[] ch = new char[ bufSize ];
		  		int no = fileObj.getReader().read(ch);
		  		if ( no == -1 ){
	  				fileObj.setEOF();
	  				return cfStringData.EMPTY_STRING;
		  		}else		  		
		  			return new cfStringData( new String( ch, 0, no ) );
	  		}
	  		
	  	}

  	}catch(Exception e){
  		throwException( _session, "FileRead cause an error (" + e.getMessage() + ")" );
  		return null;
  	}finally{
  		if ( bReadFullFile ){
  			try{ fileObj.close(); }catch(Exception ignoreE){}
  		}
  	}
  }
  
  
  private byte[] readFullFileBinary( cfVFSData	fileObj ) throws IOException {
    @SuppressWarnings("resource")
		ByteArrayOutputStreamRaw Buffer = new ByteArrayOutputStreamRaw(32000);
		
		byte in[]	= new byte[ 2048 ];
		int c1;
		BufferedInputStream	bis = fileObj.getStreamReader(); 

		while ( (c1=bis.read(in,0,2048))!=-1 )
			Buffer.write( in, 0, c1 );
		
		fileObj.setEOF();
   	return Buffer.toByteArray();
  }

  
  private String readFullFileAscii( cfVFSData	fileObj ) throws IOException {
  	StringBuilder sb	= new StringBuilder( 32000 );
  	
  	char[]	ch = new char[1024];
  	int no = 0;
  	while ( (no=fileObj.getReader().read(ch))!=-1 ){
  		sb.append( ch, 0, no );
  	}
  	
  	fileObj.setEOF();
  	return sb.toString();
  }
  
  
  private cfData readFromUrl(String src, String charSet) throws Exception {
		org.aw20.net.HttpResult res = org.aw20.net.HttpGet.doGet(src);

		if ( res.getResponseCode() != 200 )
			throw new Exception( res.getResponseCode() + " " + res.getResponseMessage() + "; " + src );
		
		if ( res.getContentType().startsWith("text/") ){
			if ( charSet == null )
				return new cfStringData( new String(res.getBody()) );
			else
				return new cfStringData( new String(res.getBody(), charSet ) );
		}else
			return new cfBinaryData( res.getBody() );
	}
  
}