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
 *  $Id: MultiPartUploadedFile.java 2526 2015-02-26 15:58:34Z alan $
 */
package com.nary.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * This class represents the file that was uploaded as part of a file request
 */
public class MultiPartUploadedFile extends Object {

	public String formName;
	public String contentType;
	public String remoteDirectory;
	public String filename;
	public File tempFile;

	public File getTempFile( File tempDirectory ) throws IOException {
		tempFile = File.createTempFile("upld", ".tmp", tempDirectory);
		return tempFile;
	}

	public void setFilename( String line ){
		int c1 		= line.indexOf( "filename=" );
		int c2 		= line.indexOf( "\"", c1+10 );
		filename	= line.substring( c1+10, c2 );

		filename = filename.replace( '\\', '/' );
		c1	= filename.lastIndexOf( '/' );
		if ( c1 != -1 ){
			remoteDirectory	= filename.substring( 0, c1+1 );
			filename				= filename.substring( c1+1 );
		}else
			remoteDirectory	= "";
	}

	public void setContentType( String line ){
		int c1 			= line.indexOf( "-Type:" );
		contentType	= line.substring( c1+7 ).trim();
	}

	public void deleteTempFile(){
		if ( tempFile != null ) {
			tempFile.delete();
			tempFile = null;
		}
	}

	@Override
	protected void finalize() throws Throwable{
		if ( tempFile != null )
			tempFile.delete();
	}

	@Override
	public String toString(){
		return 	"formName=" + formName +
					 	"; contentType=" + contentType +
						"; remoteDirectory=" + remoteDirectory +
						"; filename=" + filename +
						"; realFile=" + tempFile;
	}

	public void copyFileTo(File newFile) throws IOException {
		FileInputStream inFile = null;
		FileOutputStream outFile = null;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		try {
			inFile = new FileInputStream(tempFile);
			outFile = new FileOutputStream(newFile);

			in = new BufferedInputStream(inFile, 32000);
			out = new BufferedOutputStream(outFile, 32000);
			byte[] buffer = new byte[32000];
			int bytesRead;

			while ((bytesRead = in.read(buffer)) != -1)
				out.write(buffer, 0, bytesRead);

			out.flush();
		} finally {
			try {
				in.close();
			} catch (Exception e) {
			}
			try {
				out.close();
			} catch (Exception e) {
			}
			try {
				inFile.close();
			} catch (Exception e) {
			}
			try {
				outFile.close();
			} catch (Exception e) {
			}
		}
	}
}