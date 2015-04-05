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
 *  $Id: OpenBDArchive.java 2426 2014-03-30 18:53:18Z alan $
 */
package com.naryx.tagfusion.cfm.file.mapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.aw20.io.ByteArrayOutputStreamRaw;
import org.aw20.io.StreamUtil;
import org.aw20.io.FileUtil;

public class OpenBDArchive {

	
	/**
	 * Loads the given resource from the zip file
	 * 
	 * @param archiveFile
	 * @param fileEntry
	 * @return
	 */
	public static StringReader	getReader( String archiveFile, String fileEntry ) throws Exception {
		
		FileInputStream fis = null;
		ZipInputStream zin 	= null;
		
		try {
      zin = new ZipInputStream(new FileInputStream(archiveFile));
      ZipEntry entry;

      while ((entry = zin.getNextEntry()) != null) {
      	String entryName	= "/" + entry.getName();
      	
        if ( entryName.equals(fileEntry) ) {
        	ByteArrayOutputStreamRaw	bos	= new ByteArrayOutputStreamRaw( 32000 );
        	
        	StreamUtil.copyTo(zin, bos);
          
          return new StringReader( new String( SecureUtils.decryptData( SecureUtils.DEFAULT_KEY, bos.toByteArray() ) ) );
        }

        zin.closeEntry();
      }

      throw new IOException( "NotFoundInArchive: " + fileEntry );
      
    } finally {
    	StreamUtil.closeStream(fis);
    	StreamUtil.closeStream(zin);
    }
	}
	
	
	
	public static void	createArchive( File archiveFile, File directory ) throws Exception {
		ZipOutputStream zos=null;
		try{
			zos = new ZipOutputStream(new FileOutputStream(archiveFile));
			zipDir(directory, zos, "");
		}finally{
			StreamUtil.closeStream(zos);	
		}
	}
	

	
	private static void zipDir( File zipDir, ZipOutputStream zos, String path) throws Exception {
		
		// get a listing of the directory content
		String[] dirList = zipDir.list();
		
		// loop through dirList, and zip the files
		for (int i = 0; i < dirList.length; i++) {
			File f = new File(zipDir, dirList[i]);
			
			if (f.isDirectory()) {
				
				if ( !f.getName().startsWith(".") )
					zipDir( new File(f.getPath()), zos, path + f.getName() + "/");
				
				continue;
			}
			
			String fname	= f.getName().toLowerCase();
			if ( fname.endsWith(".cfm") 
					|| fname.endsWith(".cfc")
					|| fname.endsWith(".htm")
					|| fname.endsWith(".html")
					|| fname.endsWith(".inc")
					){

				ZipEntry anEntry = new ZipEntry(path + f.getName());
				zos.putNextEntry(anEntry);

				byte[] fileDataBytes	= SecureUtils.encryptData( SecureUtils.DEFAULT_KEY, FileUtil.readBytes( f, 4096 ) );
				zos.write(fileDataBytes, 0, fileDataBytes.length);

			}

		}
	}
	
}