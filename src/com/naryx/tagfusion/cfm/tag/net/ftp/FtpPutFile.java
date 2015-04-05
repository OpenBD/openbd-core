/* 
 *  Copyright (C) 2012 TagServlet Ltd
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
 *  $Id: FtpPutFile.java 2275 2012-09-04 00:20:26Z alan $
 */
package com.naryx.tagfusion.cfm.tag.net.ftp;

import java.io.File;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class FtpPutFile extends FtpGetFile {
	private static final long serialVersionUID = 1L;

	public FtpPutFile() {
		min = 3; max = 7;
		setNamedParams( new String[]{ "ftpdata", "remotefile", "localfile", "transfermode", "asciiextensionlist", "passive", "stoponerror" } );
	}
	

	public String[] getParamInfo(){
		return new String[]{
			"the connection object as defined from FtpOpen()",
			"the remote file to download",
			"the local file to downlaod to",
			"the transfer mode to use; AUTO, BINARY or ASCII.  Defaults to AUTO",
			"if transfer mode is AUTO then this is the extension list (;) of the files that are considered ascii",
			"flag to determine if the transfer is in passive mode or not",
			"if something goes wrong, throw an exception. defaults to true"
		};
	}
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"ftp", 
				"Uploads the local file to the remote file there", 
				ReturnType.BOOLEAN );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		cfFTPData	ftpdata	= getFTPData(_session, argStruct);

		boolean soe						= getNamedBooleanParam(argStruct, "stoponerror", true);
		boolean passive				= getNamedBooleanParam(argStruct, "passive", false);
		String remoteFile			= getNamedStringParam(argStruct, "remotefile", null );
		String localFile			= getNamedStringParam(argStruct, "localfile", null );
		String transfermode		= getNamedStringParam(argStruct, "transfermode", "auto" );
		String asciiextlist		= getNamedStringParam(argStruct, "asciiextensionlist", null );
		
		// Check we have the minimum parameters
		if ( remoteFile == null )
			throwException(_session, "parameter 'remoteFile' is missing" );
		if ( localFile == null )
			throwException(_session, "parameter 'localFile' is missing" );
		
		// Check the local file is not ready
		File	fileLocal	= new File(localFile);
		if ( !fileLocal.isFile() ){
			throwException(_session, "LocalFile does not exists: " + fileLocal.getAbsolutePath() );
		}

		try{
			ftpdata.lock();
			
			ftpdata.setPassive(passive);
			ftpdata.setTransferMode( getTransferMode(localFile,transfermode,asciiextlist) );
			
			// Download the file
			ftpdata.uploadFile( fileLocal, remoteFile );

			if ( soe && !ftpdata.isSucceeded() )
				throwException( _session, ftpdata.getErrorText() );

			return cfBooleanData.getcfBooleanData( ftpdata.isSucceeded() );
		}finally{
			ftpdata.unlock();
		}
	}

}