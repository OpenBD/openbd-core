/*
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *	$Id: cfVIDEOPLAYER.java 1999 2012-03-24 22:22:50Z alan $
 *
 *  http://openbd.org/
 *  $Id: cfVIDEOPLAYER.java 1999 2012-03-24 22:22:50Z alan $
 */


package com.naryx.tagfusion.cfm.tag.ext.video;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfArrayListData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

/*
 * This is a wrapper around the popular GPL player 
 * 
 * http://www.flowplayer.org/
 * 
 */
public class cfVIDEOPLAYER extends cfTag implements Serializable  {
	static final long serialVersionUID = 1;
	
	
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "HEIGHT", 					"200" );
		defaultAttribute( "WIDTH", 						"300" );
		defaultAttribute( "CLASS", 						"" );
		defaultAttribute( "ALLOWFULLSCREEN", 	"false" );
		
		defaultAttribute( "AUTOPLAY", 	"true" );
		defaultAttribute( "LINKWINDOW",	"_self" );
		
		defaultAttribute( "PLAY",				"true" );
		defaultAttribute( "VOLUME",			"true" );
		defaultAttribute( "MUTE",				"true" );
		defaultAttribute( "TIME",				"true" );
		defaultAttribute( "STOP",				"false" );
		defaultAttribute( "PLAYLIST",		"false" );
		
		parseTagHeader( _tag );
	}
		
	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		if ( !containsAttribute("VIDEO") ){
			throw newRunTimeException( "Please supply a VIDEO attribute that is either a single URL or an Array of URLs" );
		}
		
		/* Get the properties */
		String width 						= getDynamic(_Session, "WIDTH" ).getString();
		String height 					= getDynamic(_Session, "HEIGHT" ).getString();
		boolean	allowFullScreen = getDynamic( _Session, "ALLOWFULLSCREEN").getBoolean();
		
		StringBuilder configBlock = new StringBuilder( 2000 );
		configBlock.append("{");
		
		/* Build the clip */
		configBlock.append( "'clip':{'autoPlay':" + getDynamic( _Session, "AUTOPLAY").getBoolean() );
		configBlock.append("},");
		
		
		if ( containsAttribute("LINKURL") ){
			configBlock.append( "'linkUrl':'" + getDynamic(_Session, "LINKURL" ).getString() + "'," );
			configBlock.append( "'linkWindow':'" + getDynamic(_Session, "LINKWINDOW" ).getString() + "'," );
		}
		
		/* Build the playlist */
		List<String>	videoClips = new ArrayList<String>();
		cfData videoData = getDynamic(_Session, "VIDEO" );
		if ( videoData.getDataType() == cfData.CFSTRINGDATA ){
			videoClips.add( videoData.getString() );
		}else if ( videoData.getDataType() == cfData.CFARRAYDATA ){
			cfArrayListData cfArr = (cfArrayListData)videoData;
			for ( int x=0; x < cfArr.size(); x++ ){
				videoClips.add( cfArr.getElement(x+1).getString() );
			}
		}else{
			throw newRunTimeException( "The VIDEO attribute must be either a single URL or an Array of URLs" );
		}
		
		configBlock.append( "'playlist':[");
		for ( int x=0; x < videoClips.size(); x++ ){
			configBlock.append( "'" + videoClips.get(x) + "'");
			if ( x < videoClips.size()-1)
				configBlock.append( ",");
		}
		configBlock.append( "]");

		
		/* Build the plugins */
		configBlock.append( ",'plugins':{");
		
		/* Build the controls */
		configBlock.append( "'controls':{");
		configBlock.append( "'url':'load.cfres?f=/flowplayer/flowplayer.controls-3.2.8.swf'," );
		
		configBlock.append( "'play':" + getDynamic( _Session, "PLAY").getBoolean() + "," );
		configBlock.append( "'volume':" + getDynamic( _Session, "VOLUME").getBoolean() + "," );
		configBlock.append( "'mute':" + getDynamic( _Session, "MUTE").getBoolean() + "," );
		configBlock.append( "'time':" + getDynamic( _Session, "TIME").getBoolean() + "," );
		configBlock.append( "'playlist':" + getDynamic( _Session, "PLAYLIST").getBoolean() + "," );
		configBlock.append( "'fullscreen':" + allowFullScreen );
		configBlock.append("}"); // close off the controls playlist
		
		configBlock.append("}"); // close off the playlist

		configBlock.append("}");
	
		
		/* Build out the HTML block */
		StringBuilder sb = new StringBuilder( 500 + configBlock.length() );
		
		if ( _Session.getDataBin("swfobject.js") == null ){
			sb.append( "<script src='load.cfres?f=/js/swfobject-2.1.js'></script>\r\n" );
			_Session.setDataBin( "swfobject.js", "" );
		}

		String nextId	= (String)_Session.getDataBin("cfvideoplayer");
		if ( nextId == null )	nextId = "1";

		_Session.setDataBin("cfvideoplayer", nextId + "1" );
		
		sb.append( "<div id='cfvideoplayer"+ nextId +"' class='cfmlVideoPlayer " + getDynamic( _Session, "CLASS" ).getString() + "'></div>" );

		sb.append( "<script language='JavaScript'>" );
		sb.append( "\r\n var cfg = \"" + configBlock.toString() + "\";" );
		sb.append( "\r\n swfobject.embedSWF('load.cfres?f=/flowplayer/flowplayer-3.2.8.swf', 'cfvideoplayer"+ nextId +"', '" + width + "', '" + height + "', '9.0.0', null, {'config':cfg} );" );
		sb.append( "</script>" );
		
		_Session.write( sb.toString() );
		return cfTagReturnType.NORMAL;
	}
		
}
