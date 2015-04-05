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

package com.naryx.tagfusion.cfm.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import com.nary.util.SequencedHashMap;

/**
 * Used for reading and writing ".ini" files.
 */
public class cfProfileSectionData {
	
	private SequencedHashMap data = new SequencedHashMap( false );
	private String iniPath;
	
	public cfProfileSectionData( String filePath ) throws IOException {
		iniPath = filePath;
		
		BufferedReader fileReader = new BufferedReader( new FileReader( filePath ) );
		String sectionKey = "";
		SequencedHashMap sectionMap = new SequencedHashMap( false );
		String line;
		
		// read to the next section heading or to the end of the file; write
		// each line except the section heading to the charArrayWriter
		while ( ( line = fileReader.readLine() ) != null ) {
			if ( isSectionKey( line ) ) {
				if ( ( sectionKey.length() > 0 ) || ( sectionMap.size() > 0 ) ) {
					data.put( sectionKey, sectionMap );
				}
				line = line.trim();
				sectionKey = line.substring( 1, line.length() - 1 );
				sectionMap = new SequencedHashMap( false );
			} else {
				int equalPos = line.indexOf( '=' );
				if ( line.startsWith( "#" ) || ( equalPos == -1 ) ) {
					sectionMap.put( line, null );
				} else {
					String propertyKey = line.substring( 0, equalPos ).trim();
					String propertyValue = line.substring( equalPos + 1, line.length() ).trim();
					sectionMap.put( propertyKey, propertyValue );
				}
			}
		}
		
		data.put( sectionKey, sectionMap );
		fileReader.close();
	}

	private static boolean isSectionKey( String line ) {
		if ( ( line.length() > 1 ) && ( line.charAt( 0 ) == '[' ) ) {
			line = line.trim();
			return ( line.charAt( line.length() - 1 ) == ']' );
		}
		return false;
	}
	
	public cfStructData getProfileSectionStruct() {
		cfStructData profileSectionStruct = new cfStructData();
		Iterator iter = data.keySet().iterator();
		
		while ( iter.hasNext() ) {
			String sectionKey = (String)iter.next();
			if ( sectionKey.length() == 0 ) {
				continue;
			}
			SequencedHashMap sectionMap = (SequencedHashMap)data.get( sectionKey );
			Iterator sectionIter = sectionMap.iterator();
			StringBuilder keyList = new StringBuilder();
			
			while ( sectionIter.hasNext() ) {
				String propertyKey = sectionIter.next().toString();
				if ( sectionMap.get( propertyKey ) != null ) {
					if ( keyList.length() > 0 ) {
						keyList.append( ',' );
					}
					keyList.append( propertyKey );
				}
			}
			
			profileSectionStruct.setData( sectionKey, new cfStringData( keyList.toString() ) );
		}
		
		return profileSectionStruct;
	}

	public String getProperty( String sectionKey, String propertyKey ) {
		SequencedHashMap sectionMap = (SequencedHashMap)data.get( sectionKey );
		if ( sectionMap != null ) {
			Object property = sectionMap.get( propertyKey );
			if ( property != null ) {
				return property.toString();
			}
		}
		return "";
	}
	
	// setting property writes out a new file
	public void setProperty( String sectionKey, String propertyKey, String propertyValue ) throws IOException {
		SequencedHashMap sectionMap = (SequencedHashMap)data.get( sectionKey );
		if ( sectionMap == null ) {
			sectionMap = new SequencedHashMap( false );
			data.put( sectionKey, sectionMap );
		}
		sectionMap.put( propertyKey, propertyValue );
		
		write();
	}
	
	private synchronized void write() throws IOException {
		String tmpFileName = iniPath + ".tmp";
		BufferedWriter fileWriter = new BufferedWriter( cfEngine.thisPlatform.getFileIO().getFileWriter( new File(tmpFileName) ) );
		Iterator iter = data.keySet().iterator();
		
		while ( iter.hasNext() ) {
			String sectionKey = (String)iter.next();
			if ( sectionKey.length() > 0 ) {
				fileWriter.write( "[" + sectionKey + "]" );
				fileWriter.newLine();
			}
			SequencedHashMap sectionMap = (SequencedHashMap)data.get( sectionKey );
			Iterator sectionIter = sectionMap.iterator();
			
			while ( sectionIter.hasNext() ) {
				String propertyKey = sectionIter.next().toString();
				fileWriter.write( propertyKey );
				Object propertyValue = sectionMap.get( propertyKey );
				if ( propertyValue != null ) {
					fileWriter.write( "=" + propertyValue );
				}
				fileWriter.newLine();
			}
		}
		
		fileWriter.close();
		
		// delete the old file
		File oldFile = new File( iniPath );
		if ( !oldFile.delete() ) {
			throw new IOException( "Failed to delete " + oldFile.getCanonicalPath() );
		}
		
		// rename the new file
		File newFile = new File( tmpFileName );
		if ( !newFile.renameTo( oldFile ) ) {
			throw new IOException( "Faile to rename " + newFile.getCanonicalPath() );
		}
	}
}
