/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: MultipartRequestDecode.java 2526 2015-02-26 15:58:34Z alan $
 */

package com.nary.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * This class will manage all the decoding of the input stream, including the saving
 * of any uploaded files to a temporary directory, and also the inclusion of parameters.
 *
 * The class has been optimized with respect to the choice of buffer size.
 */
public class MultipartRequestDecode extends Object {

	List<MultiPartUploadedFile>	uploadedFiles;
	Hashtable						formParameters;
	Hashtable						rawFormParameters;
	BufferedInputStream	In;
	String							delimitor;
	byte								buffer[];
	boolean							endOfPostData;
	MultiPartUploadedFile				currentFile;



	public MultipartRequestDecode(String contentType, InputStream requestData, File tempDirectory, String _encoding) throws Exception {
		uploadedFiles 	= new ArrayList<MultiPartUploadedFile>(1);
		formParameters 	= new Hashtable();

		rawFormParameters = new Hashtable();

		setupConnection(contentType, requestData);
		decodeInputStream(tempDirectory, _encoding);
	}


	private void setupConnection(String contentType, InputStream requestData) throws Exception {
		In = new BufferedInputStream(requestData);
		delimitor = contentType;

		if (delimitor.indexOf("boundary=") != -1) {
			delimitor = delimitor.substring(delimitor.indexOf("boundary=") + 9, delimitor.length());
			delimitor = "--" + delimitor;
		}

		buffer = new byte[1024];
		currentFile = null;
		endOfPostData = false;
	}



	private void decodeInputStream(File tempDirectory, String _encoding) throws Exception {
		String param;

		while ((param = getNextParameter(_encoding)) != null) {

			if (currentFile != null) {

				File tmpFile	= currentFile.getTempFile(tempDirectory);
				getParameter( new FileOutputStream( tmpFile ) );

				uploadedFiles.add(currentFile);

				currentFile = null;
			} else {
				String nextRawParameter = getParameter();
				addRawFormParameter(param, nextRawParameter);
				addFormParameter(param, new String(nextRawParameter.getBytes("ISO-8859-1"), _encoding));
			}
		}

		currentFile = null;
		buffer = null;
	}



	private String getParameter() {
		String Line = "";
		String val = "";
		for (;;) {
			Line = readLine();
			if (Line == null)
				continue;
			else if (Line.startsWith(delimitor))
				break;
			else {
				val += Line;
			}

		}

		if (val.length() > 1 && val.charAt(val.length() - 1) == '\n') {
			val = val.substring(0, val.length() - 2); // remove CRLF
		}
		return val;
	}



	private String getNextParameter(String _encoding) {

		if (endOfPostData)
			return null;

		try {
			String LineIn = null, paramName = null;

			while ((LineIn = readLine()) != null) {
				if (LineIn.indexOf(delimitor + "--") == 0)
					return null;

				if (LineIn.indexOf("name=") != -1) {
					int c1 = LineIn.indexOf("name=");
					int c2 = LineIn.indexOf("\"", c1 + 6);
					paramName = LineIn.substring(c1 + 6, c2);

					if (LineIn.indexOf("filename=") != -1) {
						currentFile = new MultiPartUploadedFile();
						currentFile.formName = paramName;
						currentFile.setFilename(new String(LineIn.getBytes("ISO-8859-1"), _encoding));
					}

					// - Move the pointer to the start of the data
					LineIn = readLine();
					if (LineIn.indexOf("Content-Type") != -1) {
						if (currentFile != null)
							currentFile.setContentType(LineIn);

						LineIn = readLine(); // --[ clean up the last line
						while (!LineIn.equals("\r\n"))
							LineIn = readLine();
					}

					return paramName;
				}
			}
		} catch (Exception E) {
		}
		return null;
	}



	private boolean getParameter(OutputStream _Out) {
		try {
			int noData, noData2 = -1;
			String tester;
			boolean bFirst = true;
			byte buffer1[] = new byte[64000];
			byte buffer2[] = new byte[64000];
			int delimLen = delimitor.length();
			int delimPos;

			while ((noData = read(buffer1, 0, buffer1.length)) != -1) {

				/*
				 * Generally the file post is ended by a \r\n followed by the boundary
				 * but if the file content ends in a blank line then we have a single \n
				 * or \r followed by the boundary.
				 */
				if ((buffer1[0] == '-' || buffer1[0] == '\n' || buffer1[0] == '\r') && noData >= delimLen) {

					tester = new String(buffer1, 0, noData, "ISO-8859-1");
					delimPos = tester.indexOf(delimitor);
					if (tester.length() >= delimLen && delimPos != -1) {
						if (noData2 >= 2)
							noData2 -= 2;

						_Out.write(buffer2, 0, noData2);

						// if boundary isn't at the start of the line write out the chars
						// that appear before it
						if (delimPos != 0)
							_Out.write(buffer1, 0, delimPos);

						// Check to see if this was the end of the stream
						if (tester.indexOf(delimitor + "--") == 0)
							endOfPostData = true;

						break;
					}
				}

				if (!bFirst)
					_Out.write(buffer2, 0, noData2);
				else
					bFirst = false;

				for (int x = 0; x < noData; x++)
					buffer2[x] = buffer1[x];

				noData2 = noData;
				_Out.flush();

			}

			_Out.flush();
			return true;
		} catch (Exception E) {
		}finally{
			try {
				_Out.close();
			} catch (IOException e) {}
		}

		return false;
	}



	private String readLine() {
		try {
			byte b[] = new byte[1024];
			int noData = read(b, 0, b.length);

			if (noData != -1) {
				return new String(b, 0, noData, "ISO-8859-1");
			}
		} catch (Exception E) {
		}
		return null;
	}



	private int read(byte b[], int off, int len) throws IOException {
		// This method reads up to the current buffer, or the next line.

		int inChar;
		int noChars = -1;

		while ((noChars < len) && ((inChar = In.read()) != -1)) {
			if (noChars == -1)
				noChars = 0;

			noChars++;
			b[noChars + off - 1] = (byte) inChar;

			if (noChars > 1 && (((char) b[noChars + off - 2] == '\r' && (char) b[noChars + off - 1] == '\n') || ((char) b[noChars + off - 2] == '\n' && (char) b[noChars + off - 1] == '\r'))) {
				break;
			}

			if (noChars == len)
				break;
		}

		return noChars;
	}



	private void addRawFormParameter(String key, String _data) {
		Object data = _data;
		if (rawFormParameters.containsKey(key)) {
			data = rawFormParameters.get(key);
			if (data instanceof List) {
				((List) data).add(_data);
			} else {
				ArrayList newList = new ArrayList();
				newList.add(data);
				newList.add(_data);
				data = newList;
			}
		}
		rawFormParameters.put(key, data);
	}



	private void addFormParameter(String key, String data) {
		if (formParameters.containsKey(key))
			data = (String) formParameters.get(key) + "," + data;

		formParameters.put(key, data);
	}


	public List<MultiPartUploadedFile> getFiles() {
		return uploadedFiles;
	}



	public Hashtable getRawParameters() {
		return rawFormParameters;
	}



	public Enumeration getParameterNames() {
		return formParameters.keys();
	}



	public String getParameter(String param) {
		return (String) formParameters.get(param);
	}
}
