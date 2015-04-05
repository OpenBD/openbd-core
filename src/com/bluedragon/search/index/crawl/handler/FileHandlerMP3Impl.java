/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  
 *  $Id: FileHandlerMP3Impl.java 1638 2011-07-31 16:08:50Z alan $
 */

package com.bluedragon.search.index.crawl.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.farng.mp3.MP3File;
import org.farng.mp3.id3.ID3v1;

import com.bluedragon.search.DocumentWrap;


public class FileHandlerMP3Impl extends AbstractFileHandler {

	private static Set<String> extensions = new HashSet<String>( Arrays.asList("mp3","mp4") );
	private static Set<String> mimetypes = new HashSet<String>( Arrays.asList("audio/mpeg") );

	public FileHandlerMP3Impl(boolean bStoreBody) {
		super(bStoreBody);
	}

	public Set<String> getExtensions() {
		return extensions;
	}

	public Set<String> getMimeTypes() {
		return mimetypes;
	}
	
	public DocumentWrap crawl(String uriroot, File file) throws CrawlException {
		DocumentWrap	document	= new DocumentWrap();
		
		try{
			MP3File mp3file = new MP3File(file);

			document.setAttribute("bitrate", 				String.valueOf(mp3file.getBitRate()) );
			
			if ( mp3file.hasID3v1Tag() ){
				ID3v1 id = mp3file.getID3v1Tag();

				document.setAttribute("album", 				id.getAlbum() );
				document.setAttribute("artist", 			id.getArtist() );
				document.setAttribute("leadartist",	 	id.getLeadArtist() );
				document.setAttribute("comment", 			id.getComment() );
				document.setAttribute("year", 				id.getYearReleased() );
				document.setAttribute("trackno", 			id.getTrackNumberOnAlbum() );
				document.setName( id.getTitle() );
			}
			
			// Setup the document
	    document.setSize( (int)file.length() );
	    document.setType( "audio/mp3" );
    	document.setId( file.getCanonicalPath() );

	    if ( uriroot != null )
	    	document.setURL( getUrl( uriroot, file ) );

		} catch (FileNotFoundException e) {
			throw new CrawlException("File not found: " + file, e);
		} catch (IOException e) {
			throw new CrawlException("File: " + file, e);
		} catch (Exception e) {
			throw new CrawlException("File: " + file, e);
		}
		
    return document;
	}
	
}
