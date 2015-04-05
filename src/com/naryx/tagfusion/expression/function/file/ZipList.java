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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ZipList extends Unzip {

	private static final long	serialVersionUID	= 1L;

	public ZipList() {
		min = 1;
		max = 2;
		setNamedParams( new String[] { "zipfile", "charset" } );
	}


	public String[] getParamInfo() {
		return new String[] { 
				"The path and file name of the zip file on which will be listed.", 
				"Used to specify the character set of filenames in the zip." };
	}


	public java.util.Map getInfo() {
		return makeInfo( "file", "will list the content of given zipfile", ReturnType.QUERY );
	}



	public cfData execute( cfSession session, cfArgStructData argStruct ) throws cfmRunTimeException {

		String srcStr = getNamedStringParam( argStruct, "zipfile", null );
		if ( srcStr == null || srcStr.length() == 0 ) {
			throwException( session, "Missing the ZIPFILE argument" );
		}

		String charset = getNamedStringParam( argStruct, "charset", System.getProperty( "file.encoding" ) );

		cfQueryResultData list = null;

		try {
			// Source file
			File src = new File( srcStr );

			checkZipfile( session, src );

			// Attempt to extract content of a zip
			list = performZiplist( session, src, charset );

		} catch ( IOException e ) {
			throwException( session, "ZipList() caused an error (" + e.getMessage() + ")" );
		}
		return list;

	}



	private cfQueryResultData performZiplist( cfSession session, File zipfile, String charset ) throws IOException {
		ZipFile zFile = null;
		try {
			cfQueryResultData filesQuery = new cfQueryResultData( new String[] { "name", "type", "compressedsize", "size", "compressedpercent", "datelastmodified", "comment" }, "CFZIP" );
			zFile = new ZipFile( zipfile, charset );

			List<Map<String, cfData>> allResultRows = new ArrayList<Map<String, cfData>>();
			Map<String, cfData> resultRow;
			Enumeration<? extends ZipArchiveEntry> files = zFile.getEntries();
			ZipArchiveEntry nextEntry = null;
			long size;
			double compressed;

			while ( files.hasMoreElements() ) {
				nextEntry = ( ZipArchiveEntry )files.nextElement();
				resultRow = new FastMap<String, cfData>( 8 );
				resultRow.put( "name", new cfStringData( nextEntry.getName() ) );
				resultRow.put( "comment", new cfStringData( nextEntry.getComment() ) );
				resultRow.put( "datelastmodified", new cfDateData( nextEntry.getTime() ) );

				if ( nextEntry.isDirectory() ) {
					resultRow.put( "compressedsize", new cfNumberData( 0 ) );
					resultRow.put( "size", new cfNumberData( 0 ) );
					resultRow.put( "type", new cfStringData( "Dir" ) );
					resultRow.put( "compressedpercent", new cfNumberData( 0 ) );

				} else {
					size = nextEntry.getSize();
					resultRow.put( "compressedsize", new cfStringData( String.valueOf( nextEntry.getCompressedSize() ) ) );
					resultRow.put( "size", new cfStringData( String.valueOf( size ) ) );
					resultRow.put( "type", new cfStringData( "File" ) );
					if ( size != 0 ) {
						compressed = ( ( float )nextEntry.getCompressedSize() / ( float )size );
						resultRow.put( "compressedpercent", new cfStringData( String.valueOf( 100 - ( int ) ( compressed * 100 ) ) ) );
					} else {
						resultRow.put( "compressedpercent", new cfStringData( "0" ) );
					}
				}

				allResultRows.add( resultRow );
			}
			filesQuery.populateQuery( allResultRows );
			return filesQuery;
		} finally {
			try {
				zFile.close();
			} catch ( IOException ignored ) {
			}
		}
	}

}
