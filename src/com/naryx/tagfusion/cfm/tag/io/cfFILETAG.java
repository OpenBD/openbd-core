/*
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.tag.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.aw20.io.StreamUtil;

import com.nary.io.FileUtils;
import com.nary.util.FastMap;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.file.cfFileEncoding;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.expression.function.file.FileUpload;
import com.naryx.tagfusion.expression.function.file.FileUploadAll;

public class cfFILETAG extends cfTag implements Serializable {

	static final long serialVersionUID = 1;

	public java.util.Map getInfo() {
		return createInfo("file", "Provides many operations that manage file input/output.");
	}

	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] { 
			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
			createAttInfo("ACTION", "The following actions can be performed using CFFILE: \"delete\", \"copy\", \"move\", \"rename\", \"read\", \"readbinary\", \"upload\", \"uploadall\", \"write\" and \"append\".", "", true),
			createAttInfo("FILE", "Defines the file to perform an operation on. This should be a full path although if the \"uridirectory\" attribute is set to true it can be a relative path to the file. This is a required attribute when ACTION = \"delete\", \"read\", \"readbinary\", \"write\" or \"append\".", "", false),
			createAttInfo("SOURCE", "The source for file operations. This is a required attribute when ACTION = \"copy\", \"move\" or \"rename\".", "", false), createAttInfo("DESTINATION", "The destination for file operations. This is a required attribute when ACTION = \"upload\", \"copy\", \"move\" or \"rename\".", "", false),
			createAttInfo("VARIABLE", "The variable name to read a file into. This is a required attribute when ACTION = \"read\" or \"readbinary\".", "", false), createAttInfo("CHARSET", "Used to specify a character set to be used for file operations.", "", false),
			createAttInfo("OUTPUT", "The data to be written to a file. This is a required attribute when ACTION = \"write\" or \"append\".", "", false), createAttInfo("FILEFIELD", "The name of the field that should be used to select the filename. This is a required attribute when ACTION = \"upload\".", "", false),
			createAttInfo("ACCEPT", "Allows only the MIME types specified in this attribute to be accepted during an \"upload\" operation.", "", false), createAttInfo("RESULT", "Specifies a variable that will recieve the results of an upload operation.", "", false),
			createAttInfo("ATTRIBUTES", "The following values can be specified in this attribute to alter the permissions on a file: \"archive\", \"hidden\", \"normal\", \"readonly\" and \"system\".", "", false), createAttInfo("URIDIRECTORY", "Specify if the file location is a full path or a relative path.", "NO", false),
			createAttInfo("ADDNEWLINE", "Specifies wether to add a new line to the end of a file during \"write\" and \"append\" operations.", "YES", false), createAttInfo("NAMECONFLICT", "Specifies the action to be taken when a file operation has a name conflict, possible options include: \"error\", \"overwrite\", \"makeunique\" or \"skip\".", "ERROR", false) };

	}

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("URIDIRECTORY", 	"NO");
		defaultAttribute("ADDNEWLINE", 		"YES");
		defaultAttribute("NAMECONFLICT", 	"ERROR");
		setFlushable(false);

		parseTagHeader(_tag);
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;

		if (!containsAttribute("ACTION"))
			throw newBadFileException("Missing ACTION", "Must contain an ACTION attribute");
	}

	 
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if (!containsAttribute(attributes,"ACTION"))
			throw newBadFileException("Missing ACTION", "Must contain an ACTION attribute");

		return	attributes;
	}

	public String getTagName() {
		return "CFFILE";
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		
		String ACTION = getDynamic(attributes,_Session, "ACTION").getString().toLowerCase();
		boolean bURI = getDynamic(attributes,_Session, "URIDIRECTORY").getBoolean();

		if (ACTION.equals("delete"))
			deleteFile(attributes,_Session, bURI);
		else if (ACTION.equals("copy"))
			copyFile(attributes,_Session, bURI);
		else if (ACTION.equals("move"))
			moveFile(attributes,_Session, bURI);
		else if (ACTION.equals("rename"))
			renameFile(attributes,_Session, bURI);
		else if (ACTION.equals("read"))
			readTextFile(attributes,_Session, bURI);
		else if (ACTION.equals("readbinary"))
			readBinaryFile(attributes,_Session, bURI);
		else if (ACTION.equals("upload"))
			uploadFile(attributes,_Session, bURI);
		else if (ACTION.equals("uploadall"))
			uploadFileAll(attributes,_Session, bURI);
		else if (ACTION.equals("write"))
			writeFile(attributes,_Session, bURI);
		else if (ACTION.equals("append"))
			appendFile(attributes,_Session, bURI);
		else
			throw newRunTimeException("Invalid ACTION [" + ACTION + "]. Valid values: WRITE,DELETE,COPY,MOVE,RENAME,READ,READBINARY,UPLOAD,APPEND");

		return cfTagReturnType.NORMAL;
	}

	private void deleteFile(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {

		if (!containsAttribute(attributes,"FILE"))
			throw newBadFileException("Missing FILE", "Must contain a FILE attribute if ACTION=DELETE");

		deleteFile(attributes, _Session, getDynamic(_Session, "FILE").getString(), bURI);
	}

	private void deleteFile(cfStructData attributes, cfSession _Session, String _file, boolean bURI) throws cfmRunTimeException {

		if (_file.length() == 0)
			throw newRunTimeException("ACTION=DELETE contents no FILE/SOURCE name");

		File thisFile = FileUtils.getFile(_Session, _file, bURI);

		if (!thisFile.exists())
			throw newRunTimeException("ACTION=DELETE. The file specified by " + _file + " not exists");
		else
			thisFile.delete();
	}

	private void copyFile(cfStructData attributes, cfSession _Session, boolean bURI, String _action, boolean bMove) throws cfmRunTimeException {

		File srcFile = FileUtils.getFile(_Session, getDynamic(attributes,_Session, "SOURCE").getString(), bURI);

		if (srcFile.isDirectory())
			throw newRunTimeException("ACTION=" + _action + ". The file specified is a directory: " + srcFile);
		else if (!srcFile.exists())
			throw newRunTimeException("ACTION=" + _action + ". The file specified no longer exists: " + srcFile);

		String filepath = FileUtils.getCleanFilePath(_Session, getDynamic(attributes,_Session, "DESTINATION").getString(), bURI);
		File destFile = new File(filepath);
		if (!destFile.isAbsolute()) {
			destFile = new File(srcFile.getParent(), filepath);
		}

		if (destFile.isDirectory())
			destFile = new File(destFile, srcFile.getName());

		// If the destination file is the same as the source file then just return.
		// NOTE: this is the fix for bug #2762.
		if (destFile.equals(srcFile))
			return;

		// --[ -------------------------------------------------
		// --[ create input, output stream and copy the file
		try {
			BufferedInputStream inFile = new BufferedInputStream(new FileInputStream(srcFile));
			BufferedOutputStream outFile = new BufferedOutputStream( cfEngine.thisPlatform.getFileIO().getFileOutputStream(destFile) );

			StreamUtil.copyTo(inFile, outFile);

		} catch (Exception E) {
			throw newRunTimeException("ACTION=" + _action + ". Exception: " + E);
		}

		FileUtils.removeFromFileCache(_Session, filepath);
		updatePermissions(attributes,_Session, destFile);

		// need to retain the last modified date from the original file
		destFile.setLastModified(srcFile.lastModified());

		// If the action is a move then we need to delete the source file
		if (bMove)
			deleteFile(attributes, _Session, getDynamic(attributes,_Session, "SOURCE").getString(), bURI);
	}

	private void copyFile(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {

		if (!containsAttribute(attributes,"SOURCE"))
			throw newBadFileException("Missing SOURCE", "Must contain a SOURCE attribute if ACTION=COPY");
		if (!containsAttribute(attributes,"DESTINATION"))
			throw newBadFileException("Missing DESTINATION", "Must contain a DESTINATION attribute if ACTION=COPY");

		copyFile(attributes,_Session, bURI, "COPY", false);
	}

	private void moveFile(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {

		if (!containsAttribute(attributes,"SOURCE"))
			throw newBadFileException("Missing SOURCE", "Must contain a SOURCE attribute if ACTION=MOVE");
		if (!containsAttribute(attributes,"DESTINATION"))
			throw newBadFileException("Missing DESTINATION", "Must contain a DESTINATION attribute if ACTION=MOVE");

		copyFile(attributes,_Session, bURI, "MOVE", true);
	}

	private void renameFile(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {

		if (!containsAttribute(attributes,"SOURCE"))
			throw newBadFileException("Missing SOURCE", "Must contain a SOURCE attribute if ACTION=RENAME");
		if (!containsAttribute(attributes,"DESTINATION"))
			throw newBadFileException("Missing DESTINATION", "Must contain a DESTINATION attribute if ACTION=RENAME");

		File srcFile = FileUtils.getFile(_Session, getDynamic(attributes,_Session, "SOURCE").getString(), bURI);

		if (srcFile.isDirectory())
			throw newRunTimeException("ACTION=RENAME. The file specified is a directory: " + srcFile);
		else if (!srcFile.exists())
			throw newRunTimeException("ACTION=RENAME. The file specified no longer exists: " + srcFile);

		String filepath = FileUtils.getCleanFilePath(_Session, getDynamic(attributes,_Session, "DESTINATION").getString(), bURI);
		File destFile = new File(filepath);
		if (!destFile.isAbsolute()) {
			destFile = new File(srcFile.getParent(), filepath);
		}

		if (destFile.isDirectory())
			throw newRunTimeException("ACTION=RENAME. No new file name has been specified: " + destFile);

		srcFile.renameTo(destFile);
		FileUtils.removeFromFileCache(_Session, filepath);
		updatePermissions(attributes,_Session, destFile);
	}

	// -------------------------------------------------------

	private void readTextFile(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {

		if (!containsAttribute(attributes,"FILE"))
			throw newBadFileException("Missing FILE", "Must contain a FILE attribute if ACTION=READ");
		if (!containsAttribute(attributes,"VARIABLE"))
			throw newBadFileException("Missing VARIABLE", "Must contain a VARIABLE attribute if ACTION=READ");

		String FILE = getDynamic(attributes,_Session, "FILE").getString();
		String VAR = getDynamic(attributes,_Session, "VARIABLE").getString();

		String CHARSET = containsAttribute(attributes,"CHARSET") ? getDynamic(attributes,_Session, "CHARSET").getString() : null;
		if (CHARSET != null) {
			CHARSET = com.nary.util.Localization.convertCharSetToCharEncoding(CHARSET);
		}

		if (FILE == null || VAR == null || FILE.length() == 0 || VAR.length() == 0)
			throw newRunTimeException("ACTION=READ. Not enough Parameters");

		File srcFile = FileUtils.getFile(_Session, FILE, bURI);

		if (srcFile.isDirectory())
			throw newRunTimeException("ACTION=READ. The file specified is a directory: " + srcFile);
		else if (!srcFile.exists())
			throw newRunTimeException("ACTION=READ. The file specified no longer exists: " + srcFile);

		try {
			CharArrayWriter buffer = new CharArrayWriter();

			BufferedReader reader = null;

			// if CHARSET not specified then try determining it from a BOM
			if (CHARSET == null) {
				cfFileEncoding fileEnc = new cfFileEncoding(srcFile, false);
				reader = fileEnc.getReader(srcFile);

			} else {
				cfFileEncoding fileEnc = new cfFileEncoding(srcFile, false);
				if (fileEnc.containsBOM()) {
					if (!(fileEnc.getEncoding().equalsIgnoreCase(CHARSET))) {
						throw newRunTimeException("The specified CHARSET [" + CHARSET + "] does not match the BOM (Byte Order Mark) of the file.");
					} else {
						reader = fileEnc.getReader(srcFile);
					}

				} else {
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile), CHARSET));
				}
			}

			StreamUtil.copyTo(reader, buffer );
			
			_Session.setData(VAR, new cfStringData(buffer.toString()));

		} catch (UnsupportedEncodingException u) {
			throw newRunTimeException("The specified CHARSET [" + CHARSET + "] is not supported.");
		} catch (cfmRunTimeException c) {
			throw c;
		} catch (Exception E) {
			throw newRunTimeException("ACTION=READ. Error occurred reading the file:" + srcFile);
		}
	}

	// -------------------------------------------------------

	private void writeFile(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {
		if (!containsAttribute(attributes,"FILE"))
			throw newBadFileException("Missing FILE", "Must contain a FILE attribute if ACTION=WRITE");
		if (!containsAttribute(attributes,"OUTPUT"))
			throw newBadFileException("Missing OUTPUT", "Must contain an OUTPUT attribute if ACTION=WRITE");

		String _file = getDynamic(attributes,_Session, "FILE").getString();

		String CHARSET = containsAttribute(attributes,"CHARSET") ? getDynamic(attributes,_Session, "CHARSET").getString() : null;
		if (CHARSET != null) {
			CHARSET = com.nary.util.Localization.convertCharSetToCharEncoding(CHARSET);
		}

		File outFile;
		cfData outputData = getDynamic(attributes,_Session, "OUTPUT");

		OutputStream outStream = null;
		OutputStreamWriter writer = null;

		try {
			outFile = FileUtils.getFile(_Session, _file, bURI);
			String filepath = outFile.getAbsolutePath();

			FileUtils.removeFromFileCache(_Session, filepath);

			outStream = cfEngine.thisPlatform.getFileIO().getFileOutputStream(outFile);

			// --[ firstly just write to the stream if we're writing binary data
			if (outputData.getDataType() == cfData.CFBINARYDATA) {
				outStream.write(((cfBinaryData) outputData).getByteArray());

			} else {

				// this extra bit of work is to make things i18n friendly
				if (CHARSET == null) {
					writer = new OutputStreamWriter(outStream);
				} else {
					writer = new OutputStreamWriter(outStream, CHARSET);
				}

				writer.write(outputData.getString().toCharArray());

				if (getDynamic(attributes,_Session, "ADDNEWLINE").getBoolean())
					writer.write(System.getProperty("line.separator"));
			}

		} catch (UnsupportedEncodingException u) {
			throw newRunTimeException("The specified CHARSET [" + CHARSET + "] is not supported.");
		} catch (cfmRunTimeException E) {
			throw E;
		} catch (Exception E) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "cffile.writeFile", new String[] { _file }));
		} finally {
			try {
				if (writer != null)
					writer.flush();
			} catch (IOException ignored) {
			}

			// expanding the fix for bug #3260 to include writing new files (not just
			// appending to existing ones)
			StreamUtil.closeStream(writer);
			StreamUtil.closeStream(outStream);
		}

		if (outFile != null)
			updatePermissions(attributes,_Session, outFile);
	}

	// -------------------------------------------------------

	private void appendFile(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {
		if (!containsAttribute(attributes,"FILE"))
			throw newBadFileException("Missing FILE", "Must contain a FILE attribute if ACTION=APPEND");
		if (!containsAttribute(attributes,"OUTPUT"))
			throw newBadFileException("Missing OUTPUT", "Must contain an OUTPUT attribute if ACTION=APPEND");

		String _file = getDynamic(attributes,_Session, "FILE").getString();

		String CHARSET = containsAttribute(attributes,"CHARSET") ? getDynamic(attributes,_Session, "CHARSET").getString() : null;
		if (CHARSET != null) {
			CHARSET = com.nary.util.Localization.convertCharSetToCharEncoding(CHARSET);
		}

		RandomAccessFile outFile = null;
		cfData outputData = getDynamic(attributes,_Session, "OUTPUT");

		File tempFile = FileUtils.getFile(_Session, _file, bURI);
		String filePath = tempFile.getAbsolutePath();

		try {
			outFile = new RandomAccessFile(filePath, "rw");

			outFile.seek(outFile.length());

			// --[ firstly just write to the stream if we're writing binary data
			if (outputData.getDataType() == cfData.CFBINARYDATA) {
				outFile.write(((cfBinaryData) outputData).getByteArray());

				// --[ otherwise we have to create an OutputStreamWriter
			} else {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				OutputStreamWriter writer;

				if (CHARSET == null) {
					writer = new OutputStreamWriter(bos);
				} else {
					writer = new OutputStreamWriter(bos, CHARSET);
				}

				String outputStr = outputData.getString();
				if (containsAttribute(attributes,"ADDNEWLINE") && getDynamic(attributes,_Session, "ADDNEWLINE").getBoolean()) {
					outputStr += System.getProperty("line.separator");
				}

				try {
					writer.write(outputStr);
					writer.flush();
					outFile.write(bos.toByteArray());
				} finally // here is the fix for bug #3260
				{
					StreamUtil.closeStream(bos);
					StreamUtil.closeStream(writer);
				}
			}

		} catch (UnsupportedEncodingException u) {
			throw newRunTimeException("The specified CHARSET [" + CHARSET + "] is not supported.");
		} catch (Exception E) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "cffile.writeFile", new String[] { _file }));
		} finally {
			try {
				if (outFile != null)
					outFile.close();
			} catch (IOException ignored) {
			}
		}

		FileUtils.removeFromFileCache(_Session, filePath);
		updatePermissions(attributes,_Session, new File(filePath));
	}

	private void readBinaryFile(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {

		if (!containsAttribute(attributes,"FILE"))
			throw newBadFileException("Missing FILE", "Must contain a FILE attribute if ACTION=READBINARY");
		if (!containsAttribute(attributes,"VARIABLE"))
			throw newBadFileException("Missing VARIABLE", "Must contain a VARIABLE attribute if ACTION=READBINARY");

		String FILE = getDynamic(attributes,_Session, "FILE").getString();
		String VAR = getDynamic(attributes,_Session, "VARIABLE").getString();
		if (FILE == null || VAR == null || FILE.length() == 0 || VAR.length() == 0)
			throw newRunTimeException("ACTION=READBINARY. Not enough Parameters. Need FILE & VARIABLE");

		File srcFile = FileUtils.getFile(_Session, FILE, bURI);

		if (srcFile.isDirectory())
			throw newRunTimeException("ACTION=READBINARY. The file specified is a directory: " + srcFile);
		else if (!srcFile.exists())
			throw newRunTimeException("ACTION=READBINARY. The file specified no longer exists: " + srcFile);

		FileInputStream fileStream = null;
		try {
			fileStream = new FileInputStream(srcFile);
			_Session.setData(VAR, new cfBinaryData(fileStream));
		} catch (Exception E) {
			throw newRunTimeException("ACTION=READBINARY. Error occurred reading the file:" + srcFile);
		} finally {
			if (fileStream != null) {
				try {
					fileStream.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	private void uploadFile(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {
		if (!containsAttribute(attributes,"DESTINATION"))
			throw newBadFileException("Missing DESTINATION", "Must contain a DESTINATION attribute if ACTION=UPLOAD");

		FileUpload fileUploadFunction = new FileUpload();
		cfArgStructData functionArgs = new cfArgStructData(true);

		functionArgs.setData("destination", getDynamic(attributes,_Session, "DESTINATION"));
		functionArgs.setData("uri", cfBooleanData.getcfBooleanData(bURI));
		functionArgs.setData("nameconflict", getDynamic(attributes,_Session, "NAMECONFLICT"));

		if (containsAttribute(attributes,"ACCEPT"))
			functionArgs.setData("accept", getDynamic(attributes,_Session, "ACCEPT"));

		if (containsAttribute(attributes,"FILEFIELD"))
			functionArgs.setData("filefield", getDynamic(attributes,_Session, "FILEFIELD"));

		cfStructData fileData = (cfStructData) fileUploadFunction.execute(_Session, functionArgs);

		if (containsAttribute(attributes,"RESULT")) {
			String result = getDynamic(attributes,_Session, "RESULT").getString();
			if (result.length() == 0) {
				throw newRunTimeException("Invalid RESULT attribute value. The name given must be at least 1 character in length.");
			}
			_Session.setData(result, fileData);
		} else {
			_Session.setQualifiedData(variableStore.FILE_SCOPE, fileData);
		}

	}

	private void uploadFileAll(cfStructData attributes, cfSession _Session, boolean bURI) throws cfmRunTimeException {
		if (!containsAttribute(attributes,"DESTINATION"))
			throw newBadFileException("Missing DESTINATION", "Must contain a DESTINATION attribute if ACTION=UPLOAD");
		if (!containsAttribute(attributes,"RESULT"))
			throw newBadFileException("Missing RESULT", "Must contain a RESULT attribute if ACTION=UPLOADALL");

		FileUploadAll fileUploadAllFunction = new FileUploadAll();
		cfArgStructData functionArgs = new cfArgStructData(true);

		functionArgs.setData("destination", getDynamic(attributes,_Session, "DESTINATION"));
		functionArgs.setData("uri", cfBooleanData.getcfBooleanData(bURI));
		functionArgs.setData("nameconflict", getDynamic(attributes,_Session, "NAMECONFLICT"));

		if (containsAttribute("ACCEPT"))
			functionArgs.setData("accept", getDynamic(attributes,_Session, "ACCEPT"));

		cfData fileData = fileUploadAllFunction.execute(_Session, functionArgs);

		String result = getDynamic(attributes,_Session, "RESULT").getString();
		if (result.length() == 0) {
			throw newRunTimeException("Invalid RESULT attribute value. The name given must be at least 1 character in length.");
		}
		_Session.setData(result, fileData);
	}

	private void updatePermissions(cfStructData attributes, cfSession _Session, File _file) throws cfmRunTimeException {
		// if MODE specified and running on UNIX/Linux/Mac OS X
		if (containsAttribute(attributes,"MODE") && !cfEngine.WINDOWS) {
			String mode = getDynamic(attributes,_Session, "MODE").getString();
			if (mode.length() != 3) {
				throw newRunTimeException("Invalid MODE specified: " + mode + ". Expected a three digit octal value e.g. 755.");
			}
			Process p = null;
			try {
				p = Runtime.getRuntime().exec("chmod " + mode + " " + _file.getAbsolutePath());
				p.waitFor();
			} catch (IOException ioe) {
				throw newRunTimeException("Failed to modify file attributes. " + ioe.getMessage());
			} catch (InterruptedException ignored) {
			} finally {
				if (p != null) {
					// here is the fix for NA#3241
					try {
						p.getInputStream().close();
					} catch (IOException ignored) {
					}
					try {
						p.getOutputStream().close();
					} catch (IOException ignored) {
					}
					try {
						p.getErrorStream().close();
					} catch (IOException ignored) {
					}
				}
			}
		}

		if (containsAttribute(attributes,"ATTRIBUTES") && cfEngine.WINDOWS) {
			FastMap attrMap = new FastMap();
			attrMap.put("archive", "+A");
			attrMap.put("hidden", "+H");
			attrMap.put("normal", "-H -S -R -A");
			attrMap.put("readonly", "+R");
			attrMap.put("system", "+S");
			attrMap.put("temporary", "-H -S -R -A"); // same as normal

			String attributesStr = getDynamic(attributes,_Session, "ATTRIBUTES").getString();
			String[] attribs = string.convertToList(attributesStr.toLowerCase(), ',');

			StringBuilder attribArgs = new StringBuilder();
			for (int i = 0; i < attribs.length; i++) {
				if (attrMap.containsKey(attribs[i])) {
					attribArgs.append(" " + attrMap.get(attribs[i]));
				} else {
					throw newRunTimeException("Invalid ATTRIBUTE value [" + attribs[i] + "]. Valid values include archive, hidden, normal, readonly, and system.");
				}
			}
			Process p = null;
			try {
				p = Runtime.getRuntime().exec("attrib" + attribArgs.toString() + " " + _file.getAbsolutePath());
				p.waitFor();
			} catch (SecurityException sec) {
				throw newRunTimeException("file attributes cannot be modified when the SecurityPermission UnmanagedCode flag is not set. " + sec.getMessage());
			} catch (IOException ioe) {
				throw newRunTimeException("Failed to modify file attributes. " + ioe.getMessage());
			} catch (InterruptedException ignored) {
			} finally {
				if (p != null) {
					// here is the fix for NA#3241
					try {
						p.getInputStream().close();
					} catch (IOException ignored) {
					}
					try {
						p.getOutputStream().close();
					} catch (IOException ignored) {
					}
					try {
						p.getErrorStream().close();
					} catch (IOException ignored) {
					}
				}
			}
		}

	}

}
