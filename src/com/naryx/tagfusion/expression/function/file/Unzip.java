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
 *  http://www.openbluedragon.org/
 *  $Id: $
 */

package com.naryx.tagfusion.expression.function.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.aw20.io.StreamUtil;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class Unzip extends functionBase {

	private static final long serialVersionUID = 1L;


	public Unzip() {
		min = 2;
		max = 5;
		setNamedParams( new String[] { "destination", "zipfile", "charset", "flatten", "overwrite" } );
	}


	public String[] getParamInfo() {
		return new String[] {
				"The directory into which the contents of the zip file will be extracted.",
				"The path and file name of the zip file on which the action will be performed.",
				"Used to specify a character set to be used for file operations.",
				"When extracting the contents of an existing zip file, the flatten attribute indicates whether or not to retain the directory structure of the zip file. " +
						"false: indicates the directory structure will not be retained, " + "true: indicates that the directory structure will be retained. DEFAULT:FALSE.",
				"unzip: Specifies whether to overwrite the extracted files:" +
						"true: If the extracted file exists at the destination specified, the file is overwritten." +
						"false: If the extracted file exists at the destination specified, the file is not overwritten and that entry is not extracted. The remaining entries are extracted." };
	}


	public java.util.Map getInfo() {
		return makeInfo( "file", "will unzip the content of given zipfile", ReturnType.QUERY );
	}


	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String sourceStr = getNamedStringParam( argStruct, "zipfile", null );
		if ( sourceStr == null || sourceStr.length() == 0 ) {
			throwException( _session, "Missing the ZIPFILE argument" );
		}

		String destStr = getNamedStringParam( argStruct, "destination", null );
		if ( destStr == null || destStr.length() == 0 ) {
			throwException( _session, "Missing the DESTINATION argument" );
		}

		String charset = getNamedStringParam( argStruct, "charset", System.getProperty( "file.encoding" ) );
		boolean flatten = getNamedBooleanParam( argStruct, "flatten", false );
		boolean overwrite = getNamedBooleanParam( argStruct, "overwrite", true );

		// Source file
		File src = new File( sourceStr );

		checkZipfile( _session, src );

		// Destination file
		File dest = new File( destStr );

		// Attempt to zip a file
		performUnzip( _session, src, dest, charset, flatten, overwrite );

		return cfBooleanData.TRUE;
	}


	private void performUnzip( cfSession _session, File _zipfile, File _destination, String charset, boolean _flatten, boolean overwrite ) throws cfmRunTimeException {
		ZipFile zFile = null;
		try {
			zFile = new ZipFile( _zipfile, charset );
		} catch ( IOException ze ) {
			throwException( _session, "Failed to extract zip file. Check the file is a valid zip file." );
		}

		BufferedInputStream in = null;
		InputStream zIn = null;
		FileOutputStream fout = null;
		File nextFile = null;
		String destinationFilename;
		byte[] buffer = new byte[4096];
		int read;

		try {
			Enumeration<? extends ZipArchiveEntry> files = zFile.getEntries();

			if ( files == null ) {
				throwException( _session, "Failed to extract zip file. Check the file is a valid zip file." );
			}

			ZipArchiveEntry nextEntry;

			// while unzip stuff goes here
			while ( files.hasMoreElements() ) {
				nextEntry = files.nextElement();
				destinationFilename = nextEntry.getName();
				File checkFile = new File( _destination.getAbsolutePath() + File.separatorChar + destinationFilename );

				if ( checkFile.exists() && !overwrite ) {
					throwException( _session, "File already exist" );

				} else {
					if ( !nextEntry.isDirectory() ) {

						if ( _flatten ) {
							int pathEnd = destinationFilename.lastIndexOf( '/' );
							if ( pathEnd != -1 )
								destinationFilename = destinationFilename.substring( pathEnd + 1 );
						}

						nextFile = new File( _destination.getAbsolutePath() + File.separatorChar + destinationFilename );
						try {
							nextFile = nextFile.getCanonicalFile();
						} catch ( IOException ignore ) {} // use original nextFile if getCanonicalFile() fails

						File parent = nextFile.getParentFile();
						if ( parent != null ) {
							parent.mkdirs(); // create the parent directory structure if needed
						}

						try {
							zIn = zFile.getInputStream( nextEntry );
							in = new BufferedInputStream( zIn );
							fout = new FileOutputStream( nextFile, false );
							while ( ( read = in.read( buffer ) ) != -1 ) {
								fout.write( buffer, 0, read );
							}

							fout.flush();
						} catch ( IOException ioe ) {
							throwException( _session, "Failed to extract entry [" + nextEntry.getName() + "] from zip file to " + nextFile.getAbsolutePath() + ". Check the permissions are suitable to allow this file to be written." );
						} finally {
							StreamUtil.closeStream( in );
							StreamUtil.closeStream( zIn );
							StreamUtil.closeStream( fout );
						}

					} else if ( !_flatten ) {
						destinationFilename = nextEntry.getName();
						nextFile = new File( _destination.getAbsolutePath() + File.separatorChar + destinationFilename );
						try {
							nextFile = nextFile.getCanonicalFile();
						} catch ( IOException ignore ) {
							// use original nextFile if getCanonicalFile() fails
						}
						nextFile.mkdirs();
					}
				}
			}
		} finally {
			try {
				zFile.close();
			} catch ( IOException ignored ) {}
		}

	}


	protected void checkZipfile( cfSession session, File zip ) throws cfmRunTimeException {
		if ( !zip.exists() ) {
			throwException( session, "The zip file specified does not exist [" + zip.getAbsolutePath() + "]." );
		} else if ( zip.isDirectory() ) {
			throwException( session, "Invalid zip file specified [" + zip.getAbsolutePath() + "]. A path to a file, not a directory must be provided." );
		}
	}
}
