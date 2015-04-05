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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.aw20.io.StreamUtil;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.filePatternFilter;
import com.naryx.tagfusion.cfm.tag.io.cfZipItem;
import com.naryx.tagfusion.expression.function.functionBase;

public class Zip extends functionBase {

	private static final long	serialVersionUID	= 1L;

	/**
	 * Setting mandatory and optional parameters name and count
	 */
	public Zip() {
		min = 1;
		max = 10;
		setNamedParams( new String[] { "zipfile", "source", "recurse", "prefix", "compressionlevel", "filter", "overwrite", "newpath", "charset", "zipparams" } );
	}

	/*
	 * (non-Javadoc)
	 * @see com.naryx.tagfusion.expression.function.functionBase#getParamInfo()
	 * Describing functionality of each parameters
	 */
	public String[] getParamInfo() {
		return new String[] { 
				"The path or file name of the zip file on which the action will be performed.", 
				"The path or file name of the zip file on which the action will be performed.", 
				"recurse indicates whether or not the subdirectories of the directory specified in the source attribute should be included in the zip file.", 
				"Used with a create action to prepend a prefix to the path of all files in the created zip file.If the source attribute is a file as opposed to a directory", 
				"The compression level to apply when creating a zip file. " + 
						"The range is 0 (no compression) to 9 (maximum compression). DEFAULT:8", 
				"A filter to apply against the files in the source directory. For example, *.txt would include only files with a .txt extension.", 
				"Specifies whether to overwrite the contents of a ZIP or JAR file." + 
						"true: Overwrites all of the content in the ZIP or JAR file if it exists." + 
						"false: Updates existing entries and adds new entries to the ZIP or JAR file if it exists.DEFAULT:FALSE.", 
				"If the source attribute is a file as opposed to a directory, the newpath attribute can be used to specify a new path for the file being included in the created zip file. If the source attribute is a directory the newpath attribute is ignored.",
				"Used to specify a character set to be used for file operations.",
				"Array of structures {src, recurse, filter, newpath, prefix} representing the attributes of ZIPPARAM" };
	}


	/*
	 * (non-Javadoc)
	 * @see com.naryx.tagfusion.expression.function.functionBase#getInfo()
	 * Describing the purpose of Zip()
	 */
	public java.util.Map getInfo() {
		return makeInfo( "file", "will zip a given file/directory", ReturnType.BOOLEAN );
	}


	/*
	 * (non-Javadoc)
	 * @see com.naryx.tagfusion.expression.operator.expressionBase#execute(com.naryx.tagfusion.cfm.engine.cfSession, com.naryx.tagfusion.cfm.engine.cfArgStructData)
	 * THis method is execute first when zip() is called
	 */
	public cfData execute( cfSession session, cfArgStructData argStruct ) throws cfmRunTimeException {
		List<cfZipItem> items = null;
		cfZipItem zipItem;
		cfData paramData = getNamedParam( argStruct, "zipparams", null );

		// Code for function with zipparams
		if ( paramData != null ) {
			cfArrayData zipParams = ( cfArrayData )paramData;
			for ( int x = 0; x < zipParams.size(); x++ ) {
				cfData data = zipParams.getElement( x + 1 );
				if ( data.getDataType() != cfData.CFSTRUCTDATA )
					throwException( session, "params must be an array of structures; " + ( x + 1 ) + " element was not a structure" );

				cfStructData sdata = ( cfStructData )data;
				items = new ArrayList<cfZipItem>();

				// Extract all parameters
				String paramDataNewpath = null;
				String paramDataPrefixStr = "";
				boolean paramDataRecurse = true;
				String paramDatafilterStr = null;
				FilenameFilter paramDatafilter = null;
				File paramDataSrcFile = null;

				if ( sdata.containsKey( "source" ) ) {
					String paramDataSource = sdata.getData( "source" ).getString();

					// Source file
					paramDataSrcFile = new File( paramDataSource );

					// Check if src is a valid file/directory
					checkZipfile( session, paramDataSrcFile );

					if ( sdata.containsKey( ( "prefix" ) ) ) {
						paramDataPrefixStr = sdata.getData( "prefix" ).getString();
					}

					if ( sdata.containsKey( ( "newpath" ) ) ) {
						paramDataNewpath = sdata.getData( "newpath" ).getString();
						zipItem = new cfZipItem( paramDataSrcFile, paramDataPrefixStr, paramDataNewpath );
					} else {
						zipItem = new cfZipItem( paramDataSrcFile, paramDataPrefixStr );
					}

					if ( sdata.containsKey( ( "filter" ) ) ) {
						paramDatafilterStr = sdata.getData( "filter" ).getString();
						paramDatafilter = new filePatternFilter( paramDatafilterStr, true );
						zipItem.setFilter( paramDatafilter );
					}

					if ( sdata.containsKey( ( "recurse" ) ) ) {
						paramDataRecurse = sdata.getData( "recurse" ).getBoolean();
						zipItem.setRecurse( paramDataRecurse );
					}

					items.add( 0, zipItem );
				} else {
					throwException( session,  "The SOURCE value is missing for zipparams" );
				}
			}
		}
		return execute( session, argStruct, items );
	}
	


	public cfData execute( cfSession session, cfArgStructData argStruct, List<cfZipItem> zipItems ) throws cfmRunTimeException {

		String destStr = getNamedStringParam( argStruct, "zipfile", null );
		if ( destStr.length() == 0 || destStr == null ) {
			throwException( session, "Missing the ZIPFILE argument or is an empty string." );
		}

		String sourceStr = getNamedStringParam( argStruct, "source", null );
		File src = null;
		if ( sourceStr != null ) {
			if ( sourceStr.length() == 0 ) {
				throwException( session, "SOURCE specified is an empty string. It is not a valid path." );
			} else {
				src = new File( sourceStr );
				// Check if src is a valid file/directory
				checkZipfile( session, src );
			}
		}

		boolean recurse = getNamedBooleanParam( argStruct, "recurse", true );
		String prefixStr = getNamedStringParam( argStruct, "prefix", "" );
		String prefix = prefixStr.replace( '\\', '/' );
		if ( prefix.length() != 0 && !prefix.endsWith( "/" ) ) {
			prefix = prefix + "/";
		}

		String newpath = getNamedStringParam( argStruct, "newpath", null );
		if ( newpath != null ) {
			if ( newpath.length() == 0 ) {
				throwException( session, "NEWPATH specified is an empty string. It is not a valid path." );
			}
		}
		boolean overwrite = getNamedBooleanParam( argStruct, "overwrite", false );

		int compressionLevel = getNamedIntParam( argStruct, "compressionlevel", ZipArchiveOutputStream.DEFLATED );

		if ( compressionLevel < 0 || compressionLevel > 9 ) {
			throwException( session, "Invalid COMPRESSIONLEVEL specified. Please specify a number from 0-9 (inclusive)." );
		}

		String filterStr = getNamedStringParam( argStruct, "filter", null );
		FilenameFilter filter = null;
		if ( filterStr != null ) {
			filter = new filePatternFilter( filterStr, true );
		}

		String charset = getNamedStringParam( argStruct, "charset", System.getProperty( "file.encoding" ) );

		// Destination file
		File zipfile = new File( destStr );

		// OVERWRITE
		if ( zipfile.exists() && overwrite ) {
			zipfile.delete();
		} else if ( zipfile.exists() && !overwrite ) {
			throwException( session, "Cannot overwrite the existing file" );
		}

		ZipArchiveOutputStream zipOut = null;
		FileOutputStream fout = null;

		File parent = zipfile.getParentFile();

		if ( parent != null ) { // create parent directories if required
			parent.mkdirs();
		}

		try {
			fout = new FileOutputStream( zipfile );

		} catch ( IOException e ) {
			throwException( session, "Failed to create zip file: [" + zipfile.getAbsolutePath() + "]. Reason: " + e.getMessage() );
		}
		try {
			zipOut = new ZipArchiveOutputStream( fout );
			zipOut.setEncoding( charset );
			zipOut.setFallbackToUTF8( true );
			zipOut.setUseLanguageEncodingFlag( true );
			zipOut.setCreateUnicodeExtraFields( ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS );

			// Code for Zipparams
			if ( zipItems != null ) {
				Iterator<cfZipItem> srcItems = zipItems.iterator();
				cfZipItem nextItem;
				while ( srcItems.hasNext() ) {
					nextItem = srcItems.next();
					zipOperation( session, zipOut, nextItem.getFile(), nextItem.getRecurse(), nextItem.getPrefix(), compressionLevel, nextItem.getFilter(), nextItem.getNewPath() );
				}
			}

			// Code for function without zippparams
			zipOperation( session, zipOut, src, recurse, prefix, compressionLevel, filter, newpath );

		} finally {
			StreamUtil.closeStream( zipOut );
			StreamUtil.closeStream( fout );
		}

		return cfBooleanData.TRUE;

	}


	/**
	 * Performing Zip() operation
	 * 
	 * @param session
	 * @param zipfile
	 * @param src
	 * @param recurse
	 * @param prefixx
	 * @param compLvl
	 * @param filter
	 * @param overwrite
	 * @param newpath
	 * @throws cfmRunTimeException
	 * @throws IOException
	 */
	protected void zipOperation( cfSession session, ZipArchiveOutputStream zipOut, File src, boolean recurse, String prefix, int compLvl, FilenameFilter filter, String newpath ) throws cfmRunTimeException {
		if ( src != null ) {
			FileInputStream nextFileIn;
			ZipArchiveEntry nextEntry = null;
			byte[] buffer = new byte[4096];
			int readBytes;
			zipOut.setLevel( compLvl );

			try {
				List<File> files = new ArrayList<File>();
				int srcDirLen;
				if ( src.isFile() ) {
					String parentPath = src.getParent();
					srcDirLen = parentPath.endsWith( File.separator ) ? parentPath.length() : parentPath.length() + 1;
					files.add( src );

				} else {
					String parentPath = src.getAbsolutePath();
					srcDirLen = parentPath.endsWith( File.separator ) ? parentPath.length() : parentPath.length() + 1;
					getFiles( src, files, recurse, filter );
				}

				int noFiles = files.size();
				File nextFile;
				boolean isDir;
				for ( int i = 0; i < noFiles; i++ ) {
					nextFile = ( File )files.get( i );

					isDir = nextFile.isDirectory();

					if ( noFiles == 1 && newpath != null ) {
						// NEWPATH
						nextEntry = new ZipArchiveEntry( newpath.replace( '\\', '/' ) + ( isDir ? "/" : "" ) );
					} else {
						// PREFIX
						nextEntry = new ZipArchiveEntry( prefix + nextFile.getAbsolutePath().substring( srcDirLen ).replace( '\\', '/' ) + ( isDir ? "/" : "" ) );
					}

					try {
						zipOut.putArchiveEntry( nextEntry );
					} catch ( IOException e ) {
						throwException( session, "Failed to add entry to zip file [" + nextEntry + "]. Reason: " + e.getMessage() );
					}

					if ( !isDir ) {
						nextEntry.setTime( nextFile.lastModified() );
						nextFileIn = new FileInputStream( nextFile );
						try {
							while ( nextFileIn.available() > 0 ) {
								readBytes = nextFileIn.read( buffer );
								zipOut.write( buffer, 0, readBytes );
							}
							zipOut.flush();
						} catch ( IOException e ) {
							throwException( session, "Failed to write entry [" + nextEntry + "] to zip file. Reason: " + e.getMessage() );
						} finally {
							// nextEntry close
							StreamUtil.closeStream( nextFileIn );
						}
					}
					zipOut.closeArchiveEntry();
				}
			} catch ( IOException ioe ) {
				throwException( session, "Failed to create zip file: " + ioe.getMessage() );
			}
		}
	}



	/**
	 * Adding file to archive
	 * 
	 * @param dir
	 * @param result
	 * @param recurse
	 * @param filter
	 */
	private void getFiles( File dir, List<File> result, boolean recurse, FilenameFilter filter ) {
		String[] files;

		if ( filter == null ) {
			files = dir.list();
		} else {
			// FILTER
			files = dir.list( filter );
		}

		if ( files != null ) {
			File nextFile;
			for ( int i = 0; i < files.length; i++ ) {
				nextFile = new File( dir.getPath() + File.separatorChar + files[i] );
				try {
					nextFile = nextFile.getCanonicalFile();
				} catch ( IOException ignore ) {
				}
				if ( !nextFile.isDirectory() ) {
					result.add( nextFile );
				} else if ( recurse ) {
					// RECURSE
					getFiles( nextFile, result, recurse, filter );
				}
			}
		}
	}



	/**
	 * Check if source file is a valid file
	 * 
	 * @param session
	 * @param zip
	 * @throws cfmRunTimeException
	 * @throws IOException
	 */
	protected void checkZipfile( cfSession session, File zip ) throws cfmRunTimeException {
		if ( !zip.exists() ) {
			throwException( session, "The zip file specified does not exist [" + zip.getAbsolutePath() + "]." );
		}
	}

}
