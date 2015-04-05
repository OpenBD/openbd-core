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
 *  $Id: FtpList.java 2275 2012-09-04 00:20:26Z alan $
 */
package com.naryx.tagfusion.cfm.tag.net.ftp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTPFile;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class FtpList extends FtpClose {
	private static final long serialVersionUID = 1L;

	public FtpList() {
		min = 2; max = 4;
		setNamedParams( new String[]{ "ftpdata", "directory", "passive", "stoponerror" } );
	}
	

	public String[] getParamInfo(){
		return new String[]{
			"the connection object as defined from FtpOpen()",
			"the remote directory to list",
			"flag to determine if the transfer is in passive mode or not",
			"if something goes wrong, throw an exception. defaults to true"
		};
	}
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"ftp", 
				"Lists the directory", 
				ReturnType.QUERY );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		cfFTPData	ftpdata	= getFTPData(_session, argStruct);

		boolean soe				= getNamedBooleanParam(argStruct, "stoponerror", true);
		boolean passive		= getNamedBooleanParam(argStruct, "passive", false);
		String	directory	= getNamedStringParam(argStruct, "directory", "/" );
		if ( !directory.endsWith("/") )
			directory	+= "/";
		

		// Perform the directory listing
		FTPFile[]	files;
		try{
			ftpdata.lock();
			ftpdata.setPassive(passive);
			files = ftpdata.listFiles(directory);
			if ( soe && !ftpdata.isSucceeded() )
				throwException( _session, ftpdata.getErrorText() );
			
		}finally{
			ftpdata.unlock();
		}
		
		
		// Returning a query
		cfQueryResultData queryFile = new cfQueryResultData(new String[] { "name", "path", "url", "length", "lastmodified", "attributes", "isdirectory", "mode" }, "CFFTP");
		
		if ( !ftpdata.isSucceeded() || files.length == 0 )
			return queryFile;
		
		// Fill out the query
		if ( !directory.startsWith("/") )
			directory = "/" + directory;
		
		List<Map<String, cfData>> resultQuery = new ArrayList<Map<String, cfData>>(files.length);
		for ( FTPFile ftpfile : files ){
			
			Map<String, cfData> HM = new FastMap<String, cfData>();

			HM.put("name", 					new cfStringData(ftpfile.getName()));
			HM.put("length", 				new cfNumberData( ftpfile.getSize() ));
			HM.put("lastmodified", 	new cfDateData( ftpfile.getTimestamp().getTime().getTime() ));
			HM.put("isdirectory", 	cfBooleanData.getcfBooleanData( ftpfile.isDirectory() ));
		
			HM.put("path", 					new cfStringData( directory + ftpfile.getName() ) );
			HM.put("url", 					new cfStringData( "ftp://" + ftpdata.getServer() + directory + ftpfile.getName() ) );
			HM.put("attributes", 		new cfStringData( ftpfile.isDirectory() ? "Directory" : "Normal" ));

			String mode 	= getMode(ftpfile, FTPFile.USER_ACCESS );
			mode 	+= getMode(ftpfile, FTPFile.GROUP_ACCESS );
			mode 	+= getMode(ftpfile, FTPFile.WORLD_ACCESS );
			HM.put("mode", new cfStringData(mode));
			
			resultQuery.add(HM);
		}
		
		queryFile.populateQuery(resultQuery);		
		return queryFile;
	}

	
	private String getMode( FTPFile ftpfile, int access ){
		int octet = 0;

		if ( ftpfile.hasPermission(access, FTPFile.READ_PERMISSION) )
			octet += 4;
		if ( ftpfile.hasPermission(access, FTPFile.WRITE_PERMISSION) )
			octet += 2;
		if ( ftpfile.hasPermission(access, FTPFile.SYMBOLIC_LINK_TYPE) || ftpfile.hasPermission(access, FTPFile.EXECUTE_PERMISSION ) )
			octet += 1;

		return octet + "";
	}
	
}