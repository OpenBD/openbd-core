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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfEXECUTE extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	public String getEndMarker() {
		return "</CFEXECUTE>";
	}

  @SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
  	return createInfo("system", "Executes a shell command on the local server");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
   			createAttInfo( "NAME", "The full path to the executable you wish to run", 	"", true ),
   			createAttInfo( "ARGUMENTS", "An array of command line arguments to pass into the executable", 	"", true ),
   			createAttInfo( "OUTPUTFILE", "The name of the file to store the output of the command.  Cannot be used when VARIABLE is specified", 	"", false ),
   			createAttInfo( "VARIABLE", "The name of the variable to store the output of the command.  Cannot be used when FILE is specified", 	"", false ),
   			createAttInfo( "ERRORFILE", "The name of the file where the error stream is sent to", 	"", false ),
   			createAttInfo( "ERRORVARIABLE", "The name of the variable where the error stream is sent to", 	"", false ),
   			createAttInfo( "TIMEOUT", "The timeout to wait for the command to complete.  If 0, then it will wait indefinitely for the command to finish", 	"0", false ),
   			createAttInfo( "URIDIRECTORY", "Is the path to the file relative to the document root", 	"false", false ),
   					
  	};
  }
  	
	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("NAME", "");
		defaultAttribute("TIMEOUT", 0);
		defaultAttribute("URIDIRECTORY", "NO");

		parseTagHeader(_tag);
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;
		

		if (!containsAttribute("NAME"))
			throw newBadFileException("Missing Attribute", "You need to specify the NAME of the process you wish to run");

		if (containsAttribute("OUTPUTFILE") && containsAttribute("VARIABLE"))
			throw newBadFileException("Attribute Validatation Error", "You cannot specify both the \'outputFile\' attribute and the \'variable\' attribute");
	}
	
  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if (!containsAttribute(attributes,"NAME"))
			throw newBadFileException("Missing Attribute", "You need to specify the NAME of the process you wish to run");

		if (containsAttribute(attributes,"OUTPUTFILE") && containsAttribute(attributes,"VARIABLE"))
			throw newBadFileException("Attribute Validatation Error", "You cannot specify both the \'outputFile\' attribute and the \'variable\' attribute");

		return	attributes;
	}


	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
  	cfStructData attributes = setAttributeCollection(_Session);

  	int timeOut 		= getDynamic(attributes,_Session, "TIMEOUT").getInt();
		boolean uriDir 	= getDynamic(attributes,_Session, "URIDIRECTORY").getBoolean();
		File outFile 		= getFile(attributes,_Session, "OUTPUTFILE", uriDir);
		File errFile 		= getFile(attributes,_Session, "ERRORFILE", uriDir);

		// --[ create new process
		cfEXECUTECommandRunner commandRunner = new cfEXECUTECommandRunner(formatCommand(attributes,_Session), outFile, errFile);
		commandRunner.start();

		if (timeOut > 0) {
			try {
				commandRunner.join(timeOut * 1000);
			} catch (InterruptedException ignore) {
			}
		}

		Exception problem = commandRunner.getProblem(); // using "problem" here is part of the fix for bug #2115
		if (problem != null) {
			cfCatchData rtException = new cfCatchData(_Session);
			cfStringData rtExceptionType = cfCatchData.TYPE_ANY;
			String msg = problem.getMessage();
			if (msg != null) {
				msg = msg.toLowerCase() + " : " + problem.toString();
			} else {
				msg = problem.toString();
			}

			/*
			 * On Windows, the Exception message contains either error=2 or error=5.
			 * On RHEL 3.0, the Exception message does not contain those strings, and
			 * instead contains either "not found" or "cannot execute".
			 */
			if (msg.indexOf("error=2") != -1 || msg.indexOf("not found") != -1) {
				rtExceptionType = cfCatchData.TYPE_APPLICATION;
				rtException.setDetail("File Not Found");
			} else if (msg.indexOf("error=5") != -1 || msg.indexOf("cannot execute") != -1) {
				rtExceptionType = cfCatchData.TYPE_SECURITY;
			}

			rtException.setType(rtExceptionType);
			rtException.setMessage(msg);
			rtException.setDetail("Check bluedragon.log for additional information");
			rtException.setJavaException(problem);

			throw new cfmRunTimeException(rtException);
		}

		String output = commandRunner.getOutput();

		if (output != null && outFile == null) {
			if (containsAttribute(attributes,"VARIABLE")) {
				// expose the output to the page using the variable name that was specified
				String variableName = getDynamic(attributes,_Session, "VARIABLE").getString();
				_Session.setData(variableName, new cfStringData(output)); 
			} else {
				_Session.write(output);
			}
		}

		String error = commandRunner.getError();
		if (error != null && errFile == null && containsAttribute("ERRORVARIABLE")) {
			String errVar = getDynamic(attributes,_Session, "ERRORVARIABLE").getString();
			_Session.setData(errVar, new cfStringData(error));
		}

		return cfTagReturnType.NORMAL;
	}

	private File getFile(cfStructData attributes, cfSession _Session, String _attrib, boolean _uriDir) throws cfmRunTimeException {
		File outFile = null;
		if (containsAttribute(attributes,_attrib)) {
			String outputfile = getDynamic(attributes,_Session, _attrib).getString();

			if (_uriDir)
				outFile = com.nary.io.FileUtils.getRealFile(_Session.REQ, outputfile);
			else
				outFile = new File(outputfile);

			if (!outFile.isAbsolute()) {
				outFile = new File(cfEngine.thisPlatform.getFileIO().getTempDirectory(), outputfile);
			}

			if (!outFile.exists()) {
				try {
					File parent = outFile.getParentFile();
					if (parent != null && !parent.exists()) {
						parent.mkdirs();
					}
					outFile.createNewFile();
				} catch (IOException e) {
					throw newRunTimeException(e.toString());
				}
			}
		}
		return outFile;
	}

	
	
	/**
	 * 
	 * @param _Session
	 * @return a String array of length = 2. The 1st element is the command, the
	 *         2nd will either be null or a String representing any arguments that
	 *         were specified by the "arguments" attribute.
	 * @throws cfmRunTimeException
	 */
	private String[] formatCommand(cfStructData attributes, cfSession _Session) throws cfmRunTimeException {
		String cmd = getDynamic(attributes,_Session, "NAME").getString();
		String argsAsString = "";

		if (containsAttribute(attributes,"ARGUMENTS")) {
			cfData args = getDynamic(attributes,_Session, "ARGUMENTS");

			if (args.getDataType() == cfData.CFARRAYDATA)
				argsAsString = ((cfArrayData) args).createList(" ", "\"");
			else
				argsAsString = args.getString();
		}

		String[] commandPlusArgs = { cmd, argsAsString };
		return commandPlusArgs;
	}
}
