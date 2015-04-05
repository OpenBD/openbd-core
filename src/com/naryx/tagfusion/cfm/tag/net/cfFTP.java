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

package com.naryx.tagfusion.cfm.tag.net;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nary.io.FileUtils;
import com.nary.util.FastMap;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

/*
 * This class provides the implementation of the CFFTP
 */

public class cfFTP extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("PORT", 21);
		defaultAttribute("URIDIRECTORY", "No");
		defaultAttribute("PASSIVE", "No");
		parseTagHeader(_tag);

		if (!containsAttribute("ACTION"))
			throw newBadFileException("Missing ACTION", "You need to provide a ACTION");
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {

		// --[ Need to get a connection sorted out
		String ACTION = getDynamic(_Session, "ACTION").getString().toLowerCase();

		if (ACTION.equals("open")) {
			if (!containsAttribute("CONNECTION"))
				throw newRunTimeException("When ACTION=OPEN you must provide a CONNECTION attribute");

			try {
				openNewConnection(_Session);
			} catch (SecurityException secExc) {
				throw newRunTimeException("CFFTP is not supported if SocketPermission is not enabled for the FTP server.");
			}

		} else if (ACTION.equals("close")) {

			if (!containsAttribute("CONNECTION"))
				throw newRunTimeException("When ACTION=CLOSE you must provide a CONNECTION attribute");

			closeConnection(_Session);

		} else {
			cfFTPConnection ftpCon;
			try {
				ftpCon = getConnection(_Session);
			} catch (SecurityException secExc) {
				throw newRunTimeException("CFFTP is not supported if SocketPermission is not enabled for the FTP server.");
			}
			if (!ftpCon.didSucceed()) {
				handleError(_Session, ftpCon);
				return cfTagReturnType.NORMAL;
			}

			// --[ Put a lock on the ftpConnection to ensure it can't be reused.
			try {
				ftpCon.lock();

				if (ACTION.equals("changedir"))
					actionChangeDir(_Session, ftpCon);
				else if (ACTION.equals("createdir"))
					actionCreateDir(_Session, ftpCon);
				else if (ACTION.equals("removedir"))
					actionRemoveDir(_Session, ftpCon);
				else if (ACTION.equals("existsdir"))
					actionDirExists(_Session, ftpCon);
				else if (ACTION.equals("getcurrentdir"))
					actionGetCurrentDir(_Session, ftpCon);
				else if (ACTION.equals("getcurrenturl"))
					actionGetCurrentURL(_Session, ftpCon);
				else if (ACTION.equals("existsfile"))
					actionFileExists(_Session, ftpCon);
				else if (ACTION.equals("exists"))
					actionExists(_Session, ftpCon);
				else if (ACTION.equals("remove"))
					actionRemove(_Session, ftpCon);
				else if (ACTION.equals("rename"))
					actionRename(_Session, ftpCon);
				else if (ACTION.equals("putfile"))
					actionPutFile(_Session, ftpCon);
				else if (ACTION.equals("getfile"))
					actionGetFile(_Session, ftpCon);
				else if (ACTION.equals("listdir"))
					actionListDir(_Session, ftpCon);

			} finally {
				// --[ Release the lock
				ftpCon.unlock();
			}
			handleError(_Session, ftpCon);
		}
		return cfTagReturnType.NORMAL;
	}

	// -----------------------------------------------------------------

	private cfFTPConnection getConnection(cfSession _Session) throws cfmRunTimeException {
		// If this method is called then they must provide a CONNECTION attribute
		if (!containsAttribute("CONNECTION"))
			throw newRunTimeException("You must provide a CONNECTION attribute");

		return getConnection(_Session, getDynamic(_Session, "CONNECTION").getString().toLowerCase());
	}

	private cfFTPConnection getConnection(cfSession _Session, String _connection) throws cfmRunTimeException {
		cfFTPConnection ftpData = null;

		cfData data = runTime.runExpression(_Session, _connection, false);
		if ((data.getDataType() == cfData.CFLDATA) && ((cfLData) data).exists()) {
			ftpData = (cfFTPConnection) ((cfLData) data).Get(_Session.getCFContext());
		}
		// try{
		// ftpData = (cfFTPConnection) runTime.runExpression( _Session, _connection );
		// }catch( cfmRunTimeException ignored ){}

		if (ftpData != null) {
			if (containsAttribute("STOPONERROR"))
				ftpData.setStopOnError(getDynamic(_Session, "STOPONERROR").getBoolean());

			if (!ftpData.isConnectionOpen())
				ftpData.open();

		} else {
			ftpData = openConnection(_Session);
			_Session.setData(_connection, ftpData);
		}

		ftpData.resetError();
		return ftpData;
	}

	// -----------------------------------------------------------------

	private void openNewConnection(cfSession _Session) throws cfmRunTimeException {
		String connection = getDynamic(_Session, "CONNECTION").getString().toLowerCase();

		cfFTPConnection ftpData = getConnection(_Session, connection);
		if (ftpData == null) {
			ftpData = openConnection(_Session);
			_Session.setData(connection, ftpData);
		} else {
			if (!ftpData.isConnectionOpen())
				ftpData.open();
		}

		handleError(_Session, ftpData);
	}

	private void closeConnection(cfSession _Session) throws cfmRunTimeException {
		String connection = getDynamic(_Session, "CONNECTION").getString().toLowerCase();
		cfFTPConnection ftpData;
		try {
			ftpData = (cfFTPConnection) runTime.runExpression(_Session, connection);
			if (ftpData != null)
				ftpData.close();

		} catch (cfmRunTimeException rte) {
			ftpData = new cfFTPConnection(false, 0, "Connection did not exist");
		}

		if (containsAttribute("STOPONERROR"))
			ftpData.setStopOnError(getDynamic(_Session, "STOPONERROR").getBoolean());

		handleError(_Session, ftpData);
	}

	// -----------------------------------------------------------------

	private cfFTPConnection openConnection(cfSession _Session) throws cfmRunTimeException {

		// ---[ Opens a clean connection
		if (!containsAttribute("SERVER"))
			throw newRunTimeException("CFFTP: Missing the SERVER attribute");
		if (!containsAttribute("USERNAME"))
			throw newRunTimeException("CFFTP: Missing the USERNAME attribute");
		if (!containsAttribute("PASSWORD"))
			throw newRunTimeException("CFFTP: Missing the PASSWORD attribute");

		cfFTPConnection ftpCon = new cfFTPConnection(getDynamic(_Session, "SERVER").getString(), getDynamic(_Session, "PORT").getInt(), getDynamic(_Session, "USERNAME").getString(), getDynamic(_Session, "PASSWORD").getString());

		// --[ Set any optional secondary parameters
		if (containsAttribute("TIMEOUT"))
			ftpCon.setTimeout(getDynamic(_Session, "TIMEOUT").getInt());
		if (containsAttribute("STOPONERROR"))
			ftpCon.setStopOnError(getDynamic(_Session, "STOPONERROR").getBoolean());

		// --[ Attempt to open the connection
		ftpCon.open();
		return ftpCon;
	}

	private static String determineTransferMode(File LOCALFILE, cfData asciiListData) throws cfmRunTimeException {
		String asciiList = "txt;htm;html;cfm;cfml;shtm;shtml;css;asp;asa";
		if (asciiListData != null)
			asciiList = asciiListData.getString();

		int c1 = LOCALFILE.toString().lastIndexOf(".");
		if (c1 == -1)
			return "BINARY";

		String ext = LOCALFILE.toString().substring(c1 + 1).toLowerCase();
		List<String> tokens = string.split(asciiList, ";");
		for (int i = 0; i < tokens.size(); i++) {
			if (ext.equals(tokens.get(i)))
				return "ASCII";
		}

		return "BINARY";
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private void handleError(cfSession _Session, cfFTPConnection ftpCon) throws cfmRunTimeException {
		cfStructData error = new cfStructData();

		error.setData("succeeded", cfBooleanData.getcfBooleanData(ftpCon.didSucceed()));
		error.setData("errorcode", new cfNumberData(ftpCon.getErrorCode()));
		error.setData("errortext", new cfStringData(ftpCon.getErrorText()));

		if (!ftpCon.didSucceed() && ftpCon.stopOnError())
			throw newRunTimeException("The operation could not finish: " + ftpCon.getErrorCode() + " " + ftpCon.getErrorText());

		if (ftpCon.getReturnValue() != null)
			error.setData("returnvalue", ftpCon.getReturnValue());

		if (containsAttribute("RESULT")) {
			_Session.setData(getDynamic(_Session, "RESULT").getString(), error);
		} else {
			_Session.setData("cfftp", error);
		}
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private void actionChangeDir(cfSession _Session, cfFTPConnection ftpCon) throws cfmRunTimeException {

		if (!containsAttribute("DIRECTORY"))
			throw newRunTimeException("CFFTP.changeDir: Missing the DIRECTORY attribute");

		ftpCon.actionChangeDir(getDynamic(_Session, "DIRECTORY").getString());
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private void actionCreateDir(cfSession _Session, cfFTPConnection ftpCon) throws cfmRunTimeException {

		if (!containsAttribute("DIRECTORY"))
			throw newRunTimeException("CFFTP.changeDir: Missing the DIRECTORY attribute");

		ftpCon.actionCreateDir(getDynamic(_Session, "DIRECTORY").getString());
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private void actionRemoveDir(cfSession _Session, cfFTPConnection ftpCon) throws cfmRunTimeException {

		if (!containsAttribute("DIRECTORY"))
			throw newRunTimeException("CFFTP.changeDir: Missing the DIRECTORY attribute");

		ftpCon.actionRemoveDir(getDynamic(_Session, "DIRECTORY").getString());
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private void actionDirExists(cfSession _Session, cfFTPConnection ftpCon) throws cfmRunTimeException {

		if (!containsAttribute("DIRECTORY"))
			throw newRunTimeException("CFFTP.changeDir: Missing the DIRECTORY attribute");

		boolean bResult = ftpCon.actionDirExists(getDynamic(_Session, "DIRECTORY").getString());
		ftpCon.setReturnValue(cfBooleanData.getcfBooleanData(bResult));
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private static void actionGetCurrentDir(cfSession _Session, cfFTPConnection ftpCon) {
		String result = ftpCon.actionGetCurrentDir();
		ftpCon.setReturnValue(new cfStringData(result));
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private static void actionGetCurrentURL(cfSession _Session, cfFTPConnection ftpCon) {
		String result = ftpCon.actionGetCurrentURL();
		ftpCon.setReturnValue(new cfStringData(result));
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private void actionFileExists(cfSession _Session, cfFTPConnection ftpCon) throws cfmRunTimeException {
		if (!containsAttribute("REMOTEFILE"))
			throw newRunTimeException("CFFTP.existsfile: Missing the REMOTEFILE attribute");

		if (containsAttribute("PASSIVE"))
			ftpCon.setPassive(getDynamic(_Session, "PASSIVE").getBoolean());

		try {
			boolean bResult = ftpCon.actionFileExists(getDynamic(_Session, "REMOTEFILE").getString());
			ftpCon.setReturnValue(cfBooleanData.getcfBooleanData(bResult));
		} catch (IOException e) {
			throw newRunTimeException("CFFTP.fileexists IOException: " + e.getMessage());
		}
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private void actionExists(cfSession _Session, cfFTPConnection ftpCon) throws cfmRunTimeException {
		if (!containsAttribute("ITEM"))
			throw newRunTimeException("CFFTP.exists: Missing the ITEM attribute");

		if (containsAttribute("PASSIVE"))
			ftpCon.setPassive(getDynamic(_Session, "PASSIVE").getBoolean());

		String item = getDynamic(_Session, "ITEM").getString();
		boolean bResult;

		try {
			bResult = ftpCon.actionFileExists(item);
			if (bResult)
				ftpCon.setReturnValue(cfBooleanData.TRUE);
		} catch (IOException e) {
			throw newRunTimeException("CFFTP.exists IOException: " + e.getMessage());
		}

		bResult = ftpCon.actionDirExists(item);
		ftpCon.setReturnValue(cfBooleanData.getcfBooleanData(bResult));
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private void actionRemove(cfSession _Session, cfFTPConnection ftpCon) throws cfmRunTimeException {
		if (!containsAttribute("ITEM"))
			throw newRunTimeException("CFFTP.remove: Missing the ITEM attribute");

		ftpCon.actionRemove(getDynamic(_Session, "ITEM").getString());
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private void actionRename(cfSession _Session, cfFTPConnection ftpCon) throws cfmRunTimeException {
		if (!containsAttribute("EXISTING"))
			throw newRunTimeException("CFFTP.rename: Missing the EXISTING attribute");
		if (!containsAttribute("NEW"))
			throw newRunTimeException("CFFTP.rename: Missing the NEW attribute");

		ftpCon.actionRename(getDynamic(_Session, "EXISTING").getString(), getDynamic(_Session, "NEW").getString());
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private void actionPutFile(cfSession _Session, cfFTPConnection ftpCon) throws cfmRunTimeException {
		if (!containsAttribute("LOCALFILE"))
			throw newRunTimeException("CFFTP.putfile: Missing the LOCALFILE attribute");
		if (!containsAttribute("REMOTEFILE"))
			throw newRunTimeException("CFFTP.putfile: Missing the REMOTEFILE attribute");

		if (containsAttribute("PASSIVE"))
			ftpCon.setPassive(getDynamic(_Session, "PASSIVE").getBoolean());

		// ---[ Local File
		File localFile;
		if (getDynamic(_Session, "URIDIRECTORY").getBoolean())
			localFile = FileUtils.getRealFile(_Session.REQ, getDynamic(_Session, "LOCALFILE").getString());
		else
			localFile = new File(getDynamic(_Session, "LOCALFILE").getString());

		if (!localFile.isFile())
			throw newRunTimeException("CFFTP.putfile: Local file, " + localFile.toString() + ", doesn't exist");

		String transferMode = "AUTO";
		if (containsAttribute("TRANSFERMODE"))
			transferMode = getDynamic(_Session, "TRANSFERMODE").getString().toUpperCase();

		if (transferMode.equals("AUTO"))
			transferMode = determineTransferMode(localFile, getDynamic(_Session, "ASCIIEXTENSIONLIST"));

		try {
			ftpCon.actionPutFile(localFile, getDynamic(_Session, "REMOTEFILE").getString(), transferMode);
		} catch (IOException e) {
			throw newRunTimeException("CFFTP.putfile IOException: " + e.getMessage());
		}
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private void actionGetFile(cfSession _Session, cfFTPConnection ftpCon) throws cfmRunTimeException {
		if (!containsAttribute("REMOTEFILE"))
			throw newRunTimeException("CFFTP.getFile: Missing the REMOTEFILE attribute");
		if (!containsAttribute("LOCALFILE"))
			throw newRunTimeException("CFFTP.getfile: Missing the LOCALFILE attribute");

		if (containsAttribute("PASSIVE"))
			ftpCon.setPassive(getDynamic(_Session, "PASSIVE").getBoolean());

		// ---[ Local File
		File localFile;
		if (getDynamic(_Session, "URIDIRECTORY").getBoolean())
			localFile = FileUtils.getRealFile(_Session.REQ, getDynamic(_Session, "LOCALFILE").getString());
		else
			localFile = new File(getDynamic(_Session, "LOCALFILE").getString());

		boolean failIfExists = true;
		if (containsAttribute("FAILIFEXISTS")) {
			failIfExists = getDynamic(_Session, "FAILIFEXISTS").getBoolean();
		}

		if (failIfExists && localFile.isFile())
			throw newRunTimeException("CFFTP.getfile: Local file, " + localFile.toString() + ", already exists");

		String transferMode = "AUTO";
		if (containsAttribute("TRANSFERMODE"))
			transferMode = getDynamic(_Session, "TRANSFERMODE").getString().toUpperCase();

		if (transferMode.equals("AUTO"))
			transferMode = determineTransferMode(localFile, getDynamic(_Session, "ASCIIEXTENSIONLIST"));

		try {
			ftpCon.actionGetFile(localFile, getDynamic(_Session, "REMOTEFILE").getString(), transferMode);
		} catch (IOException e) {
			throw newRunTimeException("CFFTP.getFile IOException: " + e.getMessage());
		}
	}

	// -----------------------------------------------------------------
	// -----------------------------------------------------------------

	private void actionListDir(cfSession _Session, cfFTPConnection ftpCon) throws cfmRunTimeException {
		if (!containsAttribute("DIRECTORY"))
			throw newRunTimeException("CFFTP.listDir: Missing the DIRECTORY attribute");
		if (!containsAttribute("NAME"))
			throw newRunTimeException("CFFTP.listDir: Missing the NAME attribute");

		if (containsAttribute("PASSIVE"))
			ftpCon.setPassive(getDynamic(_Session, "PASSIVE").getBoolean());

		String name = getDynamic(_Session, "NAME").getString();
		List<fileInfo> fileList;
		try {
			fileList = ftpCon.actionListDir(getDynamic(_Session, "DIRECTORY").getString());
		} catch (IOException e) {
			throw newRunTimeException("CFFTP.listDir IOException: " + e.getMessage());
		}

		cfQueryResultData queryFile = new cfQueryResultData(new String[] { "name", "path", "url", "length", "lastmodified", "attributes", "isdirectory", "mode" }, "CFFTP");

		List<Map<String, cfData>> resultQuery = new ArrayList<Map<String, cfData>>(fileList.size());
		Iterator<fileInfo> iter = fileList.iterator();
		while (iter.hasNext()) {
			fileInfo fI = iter.next();

			Map<String, cfData> HM = new FastMap<String, cfData>();

			HM.put("name", new cfStringData(fI.getName()));
			HM.put("path", new cfStringData(fI.getPath()));
			HM.put("url", new cfStringData("ftp://" + ftpCon.getServer() + fI.getPath()));
			HM.put("length", new cfNumberData(fI.getLength()));
			HM.put("lastmodified", new cfDateData(fI.getLastModified()));
			HM.put("attributes", new cfStringData(fI.getAttributes()));
			HM.put("isdirectory", cfBooleanData.getcfBooleanData(fI.isDirectory()));
			HM.put("mode", new cfStringData(fI.getMode()));

			resultQuery.add(HM);
		}

		// --[ Set the Query
		queryFile.populateQuery(resultQuery);
		_Session.setData(name, queryFile);
	}

}
