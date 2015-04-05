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

import java.io.RandomAccessFile;

public class cfMP3Data extends cfStructData implements java.io.Serializable{
  
  static final long serialVersionUID = 1;


	public cfMP3Data( cfSession _parent ){
	}	
	
	public void load( String _fileName ){
	
		String fileName	= _fileName;
	
		try{
			RandomAccessFile	mFile	= new RandomAccessFile( fileName, "r" );
			mFile.seek( mFile.length() - 128 );
			byte tmp[]	= new byte[128];
			
			mFile.read( tmp, 0, 128 );
			mFile.close();
			
			String header	= new String( tmp );
			setData( "title", new cfStringData( header.substring( 3, 33 ).trim()) );
			setData( "artist", new cfStringData( header.substring( 33, 63 ).trim() ));
			setData( "album", new cfStringData( header.substring( 63, 93 ).trim() ));
			setData( "year", new cfStringData( header.substring( 93, 97 ).trim() ));
			setData( "comment", new cfStringData( header.substring( 97, 126 ).trim() ));
			setData( "genre", new cfNumberData( (int)header.charAt( 127 ) ) );
			setData( "trackid", new cfNumberData( (int)header.charAt( 126 ) ) );
	
		}catch( Exception E ){
			clear();
		}
		
		setData( "fileName", new cfStringData( fileName ) );
	}
}
