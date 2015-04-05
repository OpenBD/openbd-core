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


package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfMP3Data;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfMP3 extends cfTag implements Serializable
{

  static final long serialVersionUID = 1;

	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "URIDIRECTORY", "NO" );        
		parseTagHeader( _tag );
		
		if ( !containsAttribute("FILE") )
			throw newBadFileException( "Missing FILE", "You need to provide a FILE" );
	}

	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		
		cfMP3Data	mp3File	= new cfMP3Data( _Session );
		boolean bURI = getDynamic(_Session, "URIDIRECTORY").getBoolean();

		_Session.deleteData("MP3");
		_Session.setData( "MP3", mp3File );
		
    String file = getDynamic(_Session,"FILE").getString();

		if ( bURI )
			mp3File.load( com.nary.io.FileUtils.getRealPath( _Session.REQ, file ) );
		else 
			mp3File.load( file );
		
		return cfTagReturnType.NORMAL;
	}
}
