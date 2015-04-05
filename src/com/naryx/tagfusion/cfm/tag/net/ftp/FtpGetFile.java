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
 *  $Id: FtpGetFile.java 2275 2012-09-04 00:20:26Z alan $
 */
package com.naryx.tagfusion.cfm.tag.net.ftp;

import java.io.File;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class FtpGetFile extends FtpClose {
	private static final long serialVersionUID = 1L;

	public FtpGetFile() {
		min = 3; max = 8;
		setNamedParams( new String[]{ "ftpdata", "remotefile", "localfile", "failifexists", "transfermode", "asciiextensionlist", "passive", "stoponerror" } );
	}
	

	public String[] getParamInfo(){
		return new String[]{
			"the connection object as defined from FtpOpen()",
			"the remote file to download",
			"the local file to downlaod to",
			"if the local file exists, then throw an exception. defautls to true",
			"the transfer mode to use; AUTO, BINARY or ASCII.  Defaults to AUTO",
			"if transfer mode is AUTO then this is the extension list (;) of the files that are considered ascii",
			"flag to determine if the transfer is in passive mode or not",
			"if something goes wrong, throw an exception. defaults to true"
		};
	}
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"ftp", 
				"Downloads the remote file to the local file here", 
				ReturnType.BOOLEAN );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		cfFTPData	ftpdata	= getFTPData(_session, argStruct);

		boolean soe						= getNamedBooleanParam(argStruct, "stoponerror", true);
		boolean passive				= getNamedBooleanParam(argStruct, "passive", false);
		boolean failifexists	= getNamedBooleanParam(argStruct, "failifexists", true);
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
		if ( fileLocal.isFile() ){
			if ( failifexists )
				throwException(_session, "File already exists: " + fileLocal.getAbsolutePath() );
			else
				fileLocal.delete();
		}

		try{
			ftpdata.lock();
			
			ftpdata.setPassive(passive);
			ftpdata.setTransferMode( getTransferMode(localFile,transfermode,asciiextlist) );
			
			// Download the file
			ftpdata.downloadFile( fileLocal, remoteFile );
			
			if ( soe && !ftpdata.isSucceeded() )
				throwException( _session, ftpdata.getErrorText() );

			return cfBooleanData.getcfBooleanData( ftpdata.isSucceeded() );
		}finally{
			ftpdata.unlock();
		}
	}
	
	
	/**
	 * Determines the transfer type based on the local file
	 * 
	 * @param LOCALFILE
	 * @param asciiListData
	 * @return
	 */
	protected int getTransferMode(String localfile, String transfermode, String asciiListData) {
		if ( transfermode.equalsIgnoreCase("binary") )
			return FTPClient.BINARY_FILE_TYPE;
		else if ( transfermode.equalsIgnoreCase("ascii") )
			return FTPClient.ASCII_FILE_TYPE;
		
		
		String asciiList = "txt;htm;html;cfm;cfml;cfc;inc;log;shtm;shtml;css;asp;asa";
		if (asciiListData != null)
			asciiList = asciiListData;

		int c1 = localfile.lastIndexOf(".");
		if (c1 == -1)
			return FTPClient.BINARY_FILE_TYPE;

		String ext = localfile.substring(c1 + 1).toLowerCase();
		List<String> tokens = string.split(asciiList, ";");
		for (int i = 0; i < tokens.size(); i++) {
			if (ext.equals(tokens.get(i)))
				return FTPClient.ASCII_FILE_TYPE;
		}

		return FTPClient.BINARY_FILE_TYPE;
	}

}