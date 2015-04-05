/*
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 */

/*
 * Created on 02-Jan-2005
 *
 * BlueDragon only tag for use in creating CAPTCHA's
 *
 *
 */
package com.naryx.tagfusion.cfm.tag.awt;

import java.io.Serializable;
import java.util.HashMap;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfCAPTCHA  extends cfTag implements Serializable {

  static final long serialVersionUID = 1;
  public static RenderCaptchaImage  renderEngine = null;

  public java.util.Map getInfo(){
  	return createInfo("output", "Used to produce a captcha image, to provide a mechanism for preventing non human entry");
  }

  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
  		createAttInfo("WIDTH", "The Width of the outputed captcha image", "", true),
  		createAttInfo("HEIGHT", "The height of the outputed captcha image", "", true),
  		createAttInfo("DISPLAYSTRING", "The string you wish to appear within the captcha image", "", true),
  		createAttInfo("BORDER", "A numeric value of the width of the border to be applied to the captcha image, applied to the border attribute of the img tag", "1", false),
  		createAttInfo("STYLE", "CSS Styles that are to be applied to the img tag, these are adding to the style attribute of the img tag", "", false)
  	};
  }

  protected void defaultParameters( String _tag ) throws cfmBadFileException {
  	defaultAttribute( "BORDER", "1" );
  	defaultAttribute( "STYLE", 	"" );

  	parseTagHeader( _tag );

    if ( !containsAttribute("WIDTH") )
    	throw newBadFileException( "Missing Attribute", "You must provide a WIDTH" );

    if ( !containsAttribute("HEIGHT") )
    	throw newBadFileException( "Missing Attribute", "You must provide a HEIGHT" );

    if ( !containsAttribute("DISPLAYSTRING") )
    	throw newBadFileException( "Missing Attribute", "You must provide the DISPLAYSTRING" );
  }

  public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {

  	//-- Initialise the renderEngine
    if ( renderEngine == null )
    	renderEngine  = new RenderCaptchaImage( _Session );

    HashMap	data	= new HashMap();

    int 	width		= getDynamic( _Session, "WIDTH" ).getInt();
    int 	height	= getDynamic( _Session, "HEIGHT" ).getInt();

    data.put( "DISPLAYSTRING", getDynamic( _Session, "DISPLAYSTRING").getString() );
    data.put( "WIDTH", 	new Integer(width) );
    data.put( "HEIGHT", new Integer(height) );

    String iD	= renderEngine.setClientData( data );

    _Session.write( "<img src='"+ renderEngine.getURL(_Session) + "?id=" + iD
    								+ "' width='" + width
										+ "' height='" + height
										+ "' border='"+ getDynamic(_Session,"BORDER").getString()
										+ "' style='"+ getDynamic(_Session,"STYLE").getString()
										+"'>");

    return cfTagReturnType.NORMAL;
  }
}
