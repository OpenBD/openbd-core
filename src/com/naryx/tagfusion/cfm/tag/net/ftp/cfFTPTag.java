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
 *  $Id: cfFTPTag.java 2275 2012-09-04 00:20:26Z alan $
 */

package com.naryx.tagfusion.cfm.tag.net.ftp;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.expression.function.functionBase;

public class cfFTPTag extends cfTag implements Serializable {
	static final long serialVersionUID = 1;
	 
	public java.util.Map getInfo(){
		return createInfo("network", "Ability to interact with a remote FTP server.");
	}
  
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo("ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
				
				createAttInfo("CONNECTION", "The the name of the connection to which we refer to the FTP connection", "", true ),

				createAttInfo("SERVER", 			"the remote server to connect", "", false ),
				createAttInfo("PORT", 				"Port to connect to", "21", false ),
				createAttInfo("USERNAME", 		"username to login to the server with", "", false ),
				createAttInfo("PASSWORD", 		"password to login to the server with", "", false ),
				createAttInfo("TIMEOUT", 			"the timeout in secords for data transfers", "30", false ),
				createAttInfo("STOPONERROR", 	"if something goes wrong, throw an exception", "true", false ),

				createAttInfo("PASSIVE", "Flag to determine if we should be in PASSIVE mode", "false", false ),

				createAttInfo("ACTION", "The type of action you wish to perform on the FTP server", "", true ),

				createAttInfo("[ACTION='OPEN']", "used to open the connection to the server", "", true ),
				createAttInfo("[ACTION='CLOSE']", "used to close the connection to the server", "", true ),
				
				createAttInfo("[ACTION='CHANGEDIR'] DIRECTORY", "the name of the directory to change the remote server to", "", true ),
				createAttInfo("[ACTION='CREATEDIR'] DIRECTORY", "the name of the directory to create on the remote server", "", true ),
				createAttInfo("[ACTION='REMOVEDIR'] DIRECTORY", "the name of the directory to remove from the remote server", "", true ),
				createAttInfo("[ACTION='EXISTSDIR'] DIRECTORY", "the name of the directory to check to see if exists", "", true ),
				createAttInfo("[ACTION='EXISTSFILE'] REMOTEFILE", "the name of the file to check to see if exists", "", true ),
				createAttInfo("[ACTION='EXISTS'] ITEM", "the name of the file/directory to check to see if exists", "", true ),
				createAttInfo("[ACTION='GETCURRENTDIR']", "returns the current directory of the remote server", "", true ),
				createAttInfo("[ACTION='GETCURRENTURL']", "returns the current url directory of the remote server", "", true ),
				createAttInfo("[ACTION='REMOVE'] ITEM", "the name of the file to remove from the server", "", true ),
				createAttInfo("[ACTION='RENAME'] EXISTING", "the name of the file to rename", "", true ),
				createAttInfo("[ACTION='RENAME'] NEW", "the name of the new file to rename it too", "", true ),

				createAttInfo("[ACTION='LISTDIR'] DIRECTORY", "the name of the directory to list", "", true ),
				createAttInfo("[ACTION='LISTDIR'] NAME", "the name of the variable to place the query object containing the ftp listing", "", true ),
				
				createAttInfo("[ACTION='GETFILE'] LOCALFILE", "the local file to which we will save the remote file into", "", true ),
				createAttInfo("[ACTION='GETFILE'] REMOTEFILE", "the remote file to fetch", "", true ),
				createAttInfo("[ACTION='GETFILE'] FAILIFEXISTS", "if the local file already exists then throw exception, otherwise delete it", "true", true ),
				createAttInfo("[ACTION='GETFILE'] TRANSFERMODE", "the transfer mode to use; AUTO, BINARY or ASCII", "auto", true ),
				createAttInfo("[ACTION='GETFILE'] ASCIIEXTENSIONLIST", "if transfer mode is AUTO then this is the extension list (;) of the files that are considered ascii", "", true ),
				
				createAttInfo("[ACTION='PUTFILE'] LOCALFILE", "the local file to which we will send to the remote server", "", true ),
				createAttInfo("[ACTION='PUTFILE'] REMOTEFILE", "the name of the remote file", "", true ),
				createAttInfo("[ACTION='PUTFILE'] TRANSFERMODE", "the transfer mode to use; AUTO, BINARY or ASCII", "auto", true ),
				createAttInfo("[ACTION='PUTFILE'] ASCIIEXTENSIONLIST", "if transfer mode is AUTO then this is the extension list (;) of the files that are considered ascii", "", true ),
		};
	}
	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		
		if ( !containsAttribute(attributes, "ACTION") )
			throw newRunTimeException("Missing ACTION attribute");
		
		// Need to get a connection sorted out
		String ACTION = getDynamic(attributes, _Session, "ACTION").getString().toLowerCase();

		if (ACTION.equals("open")) {
			if (!containsAttribute(attributes, "CONNECTION"))
				throw newRunTimeException("When ACTION=OPEN you must provide a CONNECTION attribute");

			try {
				openNewConnection(attributes, _Session);
			} catch (SecurityException secExc) {
				throw newRunTimeException("CFFTP is not supported if SocketPermission is not enabled for the FTP server.");
			}

		} else if (ACTION.equals("close")) {

			if (!containsAttribute(attributes, "CONNECTION"))
				throw newRunTimeException("When ACTION=CLOSE you must provide a CONNECTION attribute");

			closeConnection(attributes, _Session);

		} else {
			cfFTPData	ftpCon;
			
			try {
				ftpCon = getConnection(attributes, _Session);
			} catch (SecurityException secExc) {
				throw newRunTimeException("CFFTP is not supported if SocketPermission is not enabled for the FTP server.");
			}
			
			if (!ftpCon.isSucceeded()) {
				handleError(attributes, _Session, ftpCon);
				return cfTagReturnType.NORMAL;
			}

			cfData	result = null;
			
			if (ACTION.equals("changedir"))
				result = actionChangeDir(_Session, attributes, ftpCon);
			else if (ACTION.equals("createdir"))
				result = actionCreateDir(_Session, attributes, ftpCon);
			else if (ACTION.equals("removedir"))
				result = actionRemoveDir(_Session, attributes, ftpCon);
			else if (ACTION.equals("existsdir"))
				result = actionDirExists(_Session, attributes, ftpCon);
			else if (ACTION.equals("getcurrentdir"))
				result = actionGetCurrentDir(_Session, attributes, ftpCon);
			else if (ACTION.equals("getcurrenturl"))
				result = actionGetCurrentURL(_Session, attributes, ftpCon);
			else if (ACTION.equals("existsfile"))
				result = actionFileExists(_Session, attributes, ftpCon);
			else if (ACTION.equals("exists"))
				result = actionExists(_Session, attributes, ftpCon);
			else if (ACTION.equals("remove"))
				result = actionRemove(_Session, attributes, ftpCon);
			else if (ACTION.equals("rename"))
				result = actionRename(_Session, attributes, ftpCon);
			else if (ACTION.equals("putfile"))
				result = actionPutFile(_Session, attributes, ftpCon);
			else if (ACTION.equals("getfile"))
				result = actionGetFile(_Session, attributes, ftpCon);
			else if (ACTION.equals("listdir"))
				result = actionListDir(_Session, attributes, ftpCon);

			handleError(attributes, _Session, ftpCon, result );
		}
		return cfTagReturnType.NORMAL;
	}

	// -----------------------------------------------------------------

	private cfFTPData getConnection(cfStructData attributes, cfSession _Session) throws cfmRunTimeException {
		// If this method is called then they must provide a CONNECTION attribute
		if (!containsAttribute("CONNECTION"))
			throw newRunTimeException("You must provide a CONNECTION attribute");

		return getConnection(attributes, _Session, getDynamic(_Session, "CONNECTION").getString().toLowerCase());
	}

	private cfFTPData getConnection(cfStructData attributes, cfSession _Session, String _connection) throws cfmRunTimeException {
		cfFTPData ftpData = null;

		cfData data = runTime.runExpression(_Session, _connection, false);
		if ((data.getDataType() == cfData.CFLDATA) && ((cfLData) data).exists()) {
			ftpData = (cfFTPData) ((cfLData) data).Get(_Session.getCFContext());
		}

		if (ftpData != null) {
			if (!ftpData.isOpen())
				ftpData.open();

		} else {
			ftpData = openConnection(attributes, _Session);
			_Session.setData(_connection, ftpData);
		}

		return ftpData;
	}

	// -----------------------------------------------------------------

	private void openNewConnection(cfStructData attributes, cfSession _Session) throws cfmRunTimeException {
		String connection = getDynamic(_Session, "CONNECTION").getString().toLowerCase();

		cfFTPData ftpData = getConnection(attributes, _Session, connection);
		if (ftpData == null) {
			ftpData = openConnection(attributes, _Session);
			_Session.setData(connection, ftpData);
		} else {
			if (!ftpData.isOpen())
				ftpData.open();
		}
		

		handleError( attributes, _Session, ftpData);
	}

	private void closeConnection(cfStructData attributes, cfSession _Session) throws cfmRunTimeException {
		cfFTPData ftpData = null;
		
		cfData fd = getDynamic(attributes, _Session, "CONNECTION");

		try {
			if ( !(fd instanceof cfFTPData) )
				ftpData = (cfFTPData)runTime.runExpression(_Session, fd.getString() );
			else
				ftpData	= (cfFTPData)fd;

			if (ftpData != null)
				ftpData.close();

		} catch (cfmRunTimeException rte) {
			ftpData = new cfFTPData(false, 0, "Connection did not exist");
		}

		handleError(attributes, _Session, ftpData);
	}

	// -----------------------------------------------------------------

	private cfFTPData openConnection(cfStructData attributes, cfSession _Session) throws cfmRunTimeException {

		// Opens a clean connection
		if (!containsAttribute(attributes,"SERVER"))
			throw newRunTimeException("Missing the SERVER attribute");
		if (!containsAttribute(attributes,"USERNAME"))
			throw newRunTimeException("Missing the USERNAME attribute");
		if (!containsAttribute(attributes,"PASSWORD"))
			throw newRunTimeException("Missing the PASSWORD attribute");

		int port	= 21;
		if ( containsAttribute(attributes,"PORT") )
			port	= getDynamic(attributes, _Session, "PORT").getInt();
		
		cfFTPData ftpCon = new cfFTPData(getDynamic(attributes,_Session, "SERVER").getString(), port, getDynamic(attributes,_Session, "USERNAME").getString(), getDynamic(attributes,_Session, "PASSWORD").getString());

		// Set any optional secondary parameters
		if (containsAttribute(attributes,"TIMEOUT"))
			ftpCon.setTimeout(getDynamic(attributes,_Session, "TIMEOUT").getInt());

		// Attempt to open the connection
		ftpCon.open();
		return ftpCon;
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private void handleError(cfStructData attributes, cfSession _Session, cfFTPData ftpCon) throws cfmRunTimeException {
		handleError( attributes, _Session, ftpCon, null );
	}
	
	private void handleError(cfStructData attributes, cfSession _Session, cfFTPData ftpCon, cfData result ) throws cfmRunTimeException {
		cfStructData error = new cfStructData();

		error.setData("succeeded", cfBooleanData.getcfBooleanData(ftpCon.isSucceeded()));
		error.setData("errorcode", new cfNumberData(ftpCon.getErrorCode()));
		error.setData("errortext", new cfStringData(ftpCon.getErrorText()));

		if (result != null)
			error.setData("returnvalue", result );

		if (containsAttribute(attributes, "RESULT")) {
			_Session.setData(getDynamic(attributes, _Session, "RESULT").getString(), error);
		} else {
			_Session.setData("cfftp", error);
		}
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private cfData actionChangeDir(cfSession _Session, cfStructData attributes, cfFTPData ftpCon) throws cfmRunTimeException {

		if (!containsAttribute(attributes, "DIRECTORY"))
			throw newRunTimeException("Missing the DIRECTORY attribute");

		functionBase	func	= new FtpSetCurrentDir();
		cfArgStructData	args = new cfArgStructData(true);

		args.setData("ftpdata", 	ftpCon );
		if (containsAttribute(attributes, "STOPONERROR"))
			args.setData("stoponerror", getDynamic(_Session, "STOPONERROR") );

		args.setData("directory", getDynamic(attributes, _Session, "DIRECTORY") );
		
		return func.execute(_Session, args);
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private cfData actionCreateDir(cfSession _Session, cfStructData attributes, cfFTPData ftpCon) throws cfmRunTimeException {
		if (!containsAttribute(attributes, "DIRECTORY"))
			throw newRunTimeException("Missing the DIRECTORY attribute");

		functionBase	func	= new FtpCreateDir();
		cfArgStructData	args = new cfArgStructData(true);

		args.setData("ftpdata", 	ftpCon );
		if (containsAttribute(attributes, "STOPONERROR"))
			args.setData("stoponerror", getDynamic(_Session, "STOPONERROR") );

		args.setData("directory", getDynamic(attributes, _Session, "DIRECTORY") );
		
		return func.execute(_Session, args);
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private cfData actionRemoveDir(cfSession _Session, cfStructData attributes, cfFTPData ftpCon) throws cfmRunTimeException {
		if (!containsAttribute(attributes, "DIRECTORY"))
			throw newRunTimeException("Missing the DIRECTORY attribute");

		functionBase	func	= new FtpRemoveDir();
		cfArgStructData	args = new cfArgStructData(true);

		args.setData("ftpdata", 	ftpCon );
		if (containsAttribute(attributes, "STOPONERROR"))
			args.setData("stoponerror", getDynamic(_Session, "STOPONERROR") );

		args.setData("directory", getDynamic(attributes, _Session, "DIRECTORY") );
		
		return func.execute(_Session, args);
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private cfData actionDirExists(cfSession _Session, cfStructData attributes, cfFTPData ftpCon) throws cfmRunTimeException {
		if (!containsAttribute(attributes, "DIRECTORY"))
			throw newRunTimeException("Missing the DIRECTORY attribute");

		functionBase	func	= new FtpExistsDir();
		cfArgStructData	args = new cfArgStructData(true);

		args.setData("ftpdata", 	ftpCon );
		if (containsAttribute(attributes, "STOPONERROR"))
			args.setData("stoponerror", getDynamic(_Session, "STOPONERROR") );
		if (containsAttribute(attributes, "PASSIVE"))
			args.setData("passive", getDynamic(_Session, "PASSIVE") );

		args.setData("directory", getDynamic(attributes, _Session, "DIRECTORY") );
		
		return func.execute(_Session, args);
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private cfData actionGetCurrentDir(cfSession _Session, cfStructData attributes, cfFTPData ftpCon) throws cfmRunTimeException  {
		functionBase	func	= new FtpGetCurrentDir();
		cfArgStructData	args = new cfArgStructData(true);

		args.setData("ftpdata", ftpCon );
		if (containsAttribute(attributes, "STOPONERROR"))
			args.setData("stoponerror", getDynamic(_Session, "STOPONERROR") );

		return func.execute(_Session, args);
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private cfData actionGetCurrentURL(cfSession _Session, cfStructData attributes, cfFTPData ftpCon) throws cfmRunTimeException  {
		functionBase	func	= new FtpGetCurrentUrl();
		cfArgStructData	args = new cfArgStructData(true);

		args.setData("ftpdata", ftpCon );
		if (containsAttribute(attributes, "STOPONERROR"))
			args.setData("stoponerror", getDynamic(_Session, "STOPONERROR") );

		return func.execute(_Session, args);
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private cfData actionFileExists(cfSession _Session, cfStructData attributes, cfFTPData ftpCon) throws cfmRunTimeException {
		if (!containsAttribute(attributes, "REMOTEFILE"))
			throw newRunTimeException("Missing the REMOTEFILE attribute");

		functionBase	func	= new FtpExistsDir();
		cfArgStructData	args = new cfArgStructData(true);

		args.setData("ftpdata", 	ftpCon );
		if (containsAttribute(attributes, "STOPONERROR"))
			args.setData("stoponerror", getDynamic(_Session, "STOPONERROR") );
		if (containsAttribute(attributes, "PASSIVE"))
			args.setData("passive", getDynamic(_Session, "PASSIVE") );

		args.setData("file", getDynamic(attributes, _Session, "REMOTEFILE") );
		
		return func.execute(_Session, args);
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private cfData actionExists(cfSession _Session, cfStructData attributes, cfFTPData ftpCon) throws cfmRunTimeException {
		if (!containsAttribute(attributes, "ITEM"))
			throw newRunTimeException("Missing the ITEM attribute");

		functionBase	func	= new FtpExistsDir();
		cfArgStructData	args = new cfArgStructData(true);

		args.setData("ftpdata", 	ftpCon );
		if (containsAttribute(attributes, "STOPONERROR"))
			args.setData("stoponerror", getDynamic(_Session, "STOPONERROR") );
		if (containsAttribute(attributes, "PASSIVE"))
			args.setData("passive", getDynamic(_Session, "PASSIVE") );

		args.setData("file", getDynamic(attributes, _Session, "ITEM") );
		
		return func.execute(_Session, args);
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private cfData actionRemove(cfSession _Session, cfStructData attributes, cfFTPData ftpCon) throws cfmRunTimeException {
		if (!containsAttribute(attributes, "ITEM"))
			throw newRunTimeException("Missing the ITEM attribute");

		functionBase	func	= new FtpRemove();
		cfArgStructData	args = new cfArgStructData(true);

		args.setData("ftpdata", 	ftpCon );
		if (containsAttribute(attributes, "STOPONERROR"))
			args.setData("stoponerror", getDynamic(_Session, "STOPONERROR") );
		if (containsAttribute(attributes, "PASSIVE"))
			args.setData("passive", getDynamic(_Session, "PASSIVE") );

		args.setData("file", getDynamic(attributes, _Session, "ITEM") );
		
		return func.execute(_Session, args);
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private cfData actionRename(cfSession _Session, cfStructData attributes, cfFTPData ftpCon) throws cfmRunTimeException {
		if (!containsAttribute(attributes, "EXISTING"))
			throw newRunTimeException("Missing the EXISTING attribute");
		if (!containsAttribute(attributes, "NEW"))
			throw newRunTimeException("Missing the NEW attribute");

		functionBase	func	= new FtpRename();
		cfArgStructData	args = new cfArgStructData(true);

		args.setData("ftpdata", 	ftpCon );
		if (containsAttribute(attributes, "STOPONERROR"))
			args.setData("stoponerror", getDynamic(_Session, "STOPONERROR") );
		if (containsAttribute(attributes, "PASSIVE"))
			args.setData("passive", getDynamic(_Session, "PASSIVE") );

		args.setData("oldfile", getDynamic(attributes, _Session, "EXISTING") );
		args.setData("newfile", getDynamic(attributes, _Session, "NEW") );
		
		return func.execute(_Session, args);
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private cfData actionPutFile(cfSession _Session, cfStructData attributes, cfFTPData ftpCon) throws cfmRunTimeException {
		if (!containsAttribute(attributes, "REMOTEFILE"))
			throw newRunTimeException("Missing the REMOTEFILE attribute");
		if (!containsAttribute(attributes, "LOCALFILE"))
			throw newRunTimeException("Missing the LOCALFILE attribute");

		functionBase	func	= new FtpPutFile();
		cfArgStructData	args = new cfArgStructData(true);

		args.setData("ftpdata", 	ftpCon );
		if (containsAttribute(attributes, "STOPONERROR"))
			args.setData("stoponerror", getDynamic(_Session, "STOPONERROR") );
		if (containsAttribute(attributes, "PASSIVE"))
			args.setData("passive", getDynamic(_Session, "PASSIVE") );
		if (containsAttribute(attributes, "TRANSFERMODE"))
			args.setData("transfermode", getDynamic(_Session, "TRANSFERMODE") );
		if (containsAttribute(attributes, "ASCIIEXTENSIONLIST"))
			args.setData("asciiextensionlist", getDynamic(_Session, "ASCIIEXTENSIONLIST") );

		args.setData("remotefile", getDynamic(attributes, _Session, "REMOTEFILE") );
		args.setData("localfile", getDynamic(attributes, _Session, "LOCALFILE") );
		
		return func.execute(_Session, args);
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private cfData actionGetFile(cfSession _Session, cfStructData attributes, cfFTPData ftpCon) throws cfmRunTimeException {
		if (!containsAttribute(attributes, "REMOTEFILE"))
			throw newRunTimeException("Missing the REMOTEFILE attribute");
		if (!containsAttribute(attributes, "LOCALFILE"))
			throw newRunTimeException("Missing the LOCALFILE attribute");

		functionBase	func	= new FtpGetFile();
		cfArgStructData	args = new cfArgStructData(true);

		args.setData("ftpdata", 	ftpCon );
		if (containsAttribute(attributes, "STOPONERROR"))
			args.setData("stoponerror", getDynamic(_Session, "STOPONERROR") );
		if (containsAttribute(attributes, "PASSIVE"))
			args.setData("passive", getDynamic(_Session, "PASSIVE") );
		if (containsAttribute(attributes, "FAILIFEXISTS"))
			args.setData("failifexists", getDynamic(_Session, "FAILIFEXISTS") );
		if (containsAttribute(attributes, "TRANSFERMODE"))
			args.setData("transfermode", getDynamic(_Session, "TRANSFERMODE") );
		if (containsAttribute(attributes, "ASCIIEXTENSIONLIST"))
			args.setData("asciiextensionlist", getDynamic(_Session, "ASCIIEXTENSIONLIST") );

		args.setData("remotefile", getDynamic(attributes, _Session, "REMOTEFILE") );
		args.setData("localfile", getDynamic(attributes, _Session, "LOCALFILE") );
		
		return func.execute(_Session, args);
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private cfData actionListDir(cfSession _Session, cfStructData attributes, cfFTPData ftpCon) throws cfmRunTimeException {
		if (!containsAttribute(attributes, "DIRECTORY"))
			throw newRunTimeException("Missing the DIRECTORY attribute");
		if (!containsAttribute(attributes, "NAME"))
			throw newRunTimeException("Missing the NAME attribute");

		functionBase	func	= new FtpList();
		cfArgStructData	args = new cfArgStructData(true);

		args.setData("ftpdata", 	ftpCon );
		if (containsAttribute(attributes, "STOPONERROR"))
			args.setData("stoponerror", getDynamic(_Session, "STOPONERROR") );
		if (containsAttribute(attributes, "PASSIVE"))
			args.setData("passive", getDynamic(_Session, "PASSIVE") );

		args.setData("directory", getDynamic(attributes, _Session, "DIRECTORY") );
		
		cfData result = func.execute(_Session, args);
		
		_Session.setData( getDynamic(attributes, _Session, "NAME").getString(), result);
		return cfBooleanData.TRUE;
	}
}