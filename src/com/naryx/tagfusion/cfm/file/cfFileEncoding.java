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

package com.naryx.tagfusion.cfm.file;

/**
 * This class is for handling source files that may or may not
 * use an encoding other than the jvm default.
 *
 * Create an instance with the File/InputStream that you wish to use,
 * then call getReader() with the same File
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.vfs.FileObject;

import com.nary.util.Localization;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;

public class cfFileEncoding {

	private byte offset = 0;

	private String encoding;

	private boolean foundBOM;

	public cfFileEncoding( File _f ) throws cfmBadFileException, IOException{
		init( _f );
	}

	public cfFileEncoding( File _f, boolean _s ) throws cfmBadFileException, IOException{
		init( _f, _s );
	}

	public cfFileEncoding(FileObject _f) throws cfmBadFileException, IOException {
		init(_f.getContent().getInputStream());
	}

	public cfFileEncoding(FileObject _f, boolean _s) throws cfmBadFileException, IOException {
		init(_f.getContent().getInputStream(), _s);
	}

	public cfFileEncoding(InputStream _i) throws cfmBadFileException {
		init(_i);
	}

	public boolean containsBOM() {
		return foundBOM;
	}

	public String getEncoding() {
		return encoding;
	}

	private void init(File _file) throws cfmBadFileException, FileNotFoundException {
		init(_file, true);
	}

	private void init(File _file, boolean _s) throws cfmBadFileException, FileNotFoundException {
		FileInputStream fileIn = new FileInputStream(_file);
		init(fileIn, _s);
	}

	private void init(InputStream _fileIn) throws cfmBadFileException {
		init(_fileIn, true);
	}

	/**
	 * Initialise the class with the specified file
	 *
	 * @param _fileIn
	 *          - the file to get the encoding from
	 * @param _search
	 *          - if true, the search for the file encoding will include looking
	 *          at the first 4096 bytes for the presence of a
	 *          cfprocessingdirective tag with pageencoding specified.
	 * @throws cfmBadFileException
	 */
	private void init(InputStream _fileIn, boolean _search) throws cfmBadFileException {

		encoding = System.getProperty("file.encoding");

		int buffersize = 4096;
		byte[] buffer = new byte[4096];
		foundBOM = true;

		try {
			int bytesread = _fileIn.read(buffer, 0, buffersize);

			// read BOM (byte order mark)
			if (buffer[0] == (byte) 0xef && buffer[1] == (byte) 0xbb && buffer[2] == (byte) 0xbf) { // utf-8
				encoding = Localization.convertCharSetToCharEncoding("utf-8");
				offset = 3;
			} else if (buffer[0] == (byte) 0xff && buffer[1] == (byte) 0xfe) { // ucs-2le,
																																					// ucs-4le,
																																					// and
																																					// ucs-16le
				encoding = Localization.convertCharSetToCharEncoding("utf-16LE");
				offset = 2;
			} else if (buffer[0] == (byte) 0xfe && buffer[1] == (byte) 0xff) { // utf-16
																																					// and
																																					// ucs-2
				encoding = Localization.convertCharSetToCharEncoding("utf-16BE");
				offset = 2;
			}
			/*
			 * utf-32BE/LE not currently supported else if ( buffer[0] == 0 &&
			 * buffer[1] == 0 && buffer[2] == (byte)0xfe && buffer[3] == (byte)0xff){
			 * // utf-32BE encoding = "utf-32BE"; ignoreBytes = 4; }else if (
			 * buffer[0] == (byte)0xff && buffer[1] == (byte)0xfe && buffer[2] == 0 &&
			 * buffer[3] == 0){ // utf-32LE encoding = "utf-32LE"; ignoreBytes = 4; }
			 */
			else {
				foundBOM = false;
			}

			// don't need to do the searching
			if (!_search)
				return;

			// now try looking for cfprocessingdirective
			String body = bytesread > 0 ? new String(buffer, 0, bytesread, encoding) : "";
			String foundEnc = findProcessingDirective(body);

			// if the page encoding was discovered in a cfprocessingdirective and not
			// a BOM
			if (!foundBOM && foundEnc.length() != 0) {
				encoding = foundEnc;
				// else if it was discovered in both cfprocessingdirective and BOM, and
				// they didn't match
			} else if (foundBOM && foundEnc.length() != 0 && !encoding.equalsIgnoreCase(foundEnc)) {
				cfCatchData catchData = catchDataFactory.badEncodingException("The page encoding specified [" + encoding + "] via the CFPROCESSINGDIRECTIVE tag does not match the Byte Order Mark (BOM) of the file.");
				throw new cfmBadFileException(catchData);
			}

		} catch (IOException i) {
			cfCatchData catchData = catchDataFactory.badEncodingException("Failed to determine encoding due to error reading file [" + i.getMessage() + "]");
			throw new cfmBadFileException(catchData);
		} finally {
			try {
				_fileIn.close();
			} catch (Exception ignored) {
			}
		}

	}

	public BufferedReader getReader(String _filename) throws cfmBadFileException, FileNotFoundException, IOException {
		return getReader(new File(_filename));
	}

	public BufferedReader getReader(File _file) throws FileNotFoundException, cfmBadFileException {
		FileInputStream fileIn = new FileInputStream(_file); // have to re-init since some has been read
		return new BufferedReader( getReader( fileIn ) );
	}

	public InputStreamReader getReader( FileObject _file ) throws IOException, cfmBadFileException {
		  return getReader( _file.getContent().getInputStream() ); // input stream is already buffered
	  }

	public InputStreamReader getReader(InputStream _in) throws cfmBadFileException {

		if (offset > 0) {
			try {
				_in.read(new byte[offset]); // read in the BOM since we don't want to
																		// render that part
			} catch (IOException ignored) {
			} // if somethings gone wrong let someone else deal with it
		}

		try {
			return new InputStreamReader( _in, encoding );
		} catch (UnsupportedEncodingException u) {
			cfCatchData catchData = catchDataFactory.badEncodingException("The page encoding specified [" + encoding + "] is not supported.");
			throw new cfmBadFileException(catchData);
		}
	}

	// this searches for the cfprocessingdirective tag
	private static String findProcessingDirective(String _body) throws cfmBadFileException {
		cfFile file = new cfFile(_body);
		String pageEncoding = file.getEncoding();
		return (pageEncoding != null ? pageEncoding : "");
	}
}
