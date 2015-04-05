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

package com.naryx.tagfusion.cfm.tag.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfEngine;

public class cfEXECUTECommandRunner extends Thread implements Serializable {
	private static final long serialVersionUID = 1L;

	String command = null;
	String args = null;
	String output = null;
	String error = null;
	File outFile = null;
	File errFile = null;

	Exception problem = null;

	public cfEXECUTECommandRunner(String[] commandPlusArgs, File _outFile, File _errFile) {
		command = commandPlusArgs[0];
		args = commandPlusArgs[1];
		outFile = _outFile;
		errFile = _errFile;
	}

	public void run() {
		InputStream in = null;
		OutputStream out = null;
		InputStream errIn = null;
		try {
			// Process command line to preserve blanks in quoted strings.
			List<String> tokens = string.split(args);
			int tCnt = tokens.size();
			String[] cmd = new String[1 + tCnt];
			cmd[0] = command;
			int i = 1;
			StringBuilder tok = new StringBuilder(128);
			String tok2 = null;
			boolean done = false;

			// If token starts with double-quote("), then combine remaining tokens
			// until
			// the matching double-quote is found. Don't forget to check the current
			// token for match.
			int tokenIndx = 0;
			while (tokenIndx < tokens.size()) {
				tok.append(tokens.get(tokenIndx++).toString());
				if ((tok.charAt(0) == '"') && (tok.toString().indexOf("\"", 1) < 0)) {
					while (!done && tokenIndx < tokens.size()) {
						tok2 = tokens.get(tokenIndx++).toString();
						if (tok2.indexOf("\"") >= 0) {
							done = true;
						}
						tok.append(" " + tok2);
					}
				}

				cmd[i] = tok.toString();
				i++;
				tok.setLength(0);
				done = false;
			}

			// We could set the remaining args to " " but that doesn't always work
			// (see bug #2525)
			// so if we've come across any quote delimited args we'll need to copy to
			// a smaller array
			if (i <= tCnt) {
				String[] tmp = new String[i];
				for (int c = 0; c < i; c++) {
					tmp[c] = cmd[c];
				}
				cmd = tmp;
			}

			// --[ get runtime object
			Runtime RT = Runtime.getRuntime();
			Process PR = RT.exec(cmd);
			in = PR.getInputStream();
			errIn = PR.getErrorStream();
			out = PR.getOutputStream();

			StreamConsumer inConsumer = new StreamConsumer(in, outFile);
			StreamConsumer errConsumer = new StreamConsumer(errIn, errFile);

			inConsumer.start();
			errConsumer.start();

			PR.waitFor();

			// Wait for the consumer threads to complete
			inConsumer.join();
			errConsumer.join();

			output = inConsumer.getOutput();
			error = errConsumer.getOutput();

		} catch (Exception e) {
			handleProblem(e);
		} finally {
			// the StreamConsumer classes will close the other streams
			if (out != null)
				try {
					out.close();
				} catch (IOException ignored) {
				}
		}
	}// end run()

	public String getOutput() {
		return output;
	}

	public String getError() {
		return error;
	}

	/**
	 * This method and it's use is part of the fix for bug #2115
	 * 
	 */
	private void handleProblem(Exception e) {
		cfEngine.log("CFEXECUTE failed for: " + command + "; " + e.getMessage());
		problem = e;
	}

	public Exception getProblem() {
		return problem;
	}

	class StreamConsumer extends Thread {
		private InputStream in;

		private String output;

		private File outFile;

		StreamConsumer(InputStream _in, File _file) {
			in = _in;
			outFile = _file;
		}

		public String getOutput() {
			return output;
		}

		public void run() {
			InputStreamReader iReader = null;
			BufferedReader bReader = null;
			String lineIn;
			Writer writer = null;
			try {
				if (outFile == null) {
					writer = new StringWriter();
				} else {
					writer = new FileWriter(outFile);
				}
				iReader = new InputStreamReader(in);
				bReader = new BufferedReader(iReader);
				while ((lineIn = bReader.readLine()) != null) {
					writer.append(lineIn);
					writer.append("\r\n");
				}

				if (outFile == null)
					output = ((StringWriter) writer).toString();
			} catch (IOException e) {
				handleProblem(e);
			} finally {
				if (bReader != null)
					try {
						bReader.close();
					} catch (Exception ignored) {
					}
				if (iReader != null)
					try {
						iReader.close();
					} catch (Exception ignored) {
					}
				if (in != null)
					try {
						in.close();
					} catch (Exception ignored) {
					}
				if (writer != null)
					try {
						writer.close();
					} catch (Exception ignored) {
					}
			}
		}

	}
}
