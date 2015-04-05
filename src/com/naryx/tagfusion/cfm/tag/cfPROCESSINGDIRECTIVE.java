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

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;

/**
 * This is the base class for all tags in the system.
 */

public class cfPROCESSINGDIRECTIVE extends cfTag implements java.io.Serializable 
{
	
  static final long serialVersionUID = 1;

  private String endMarker = null;
	
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
    parseTagHeader( _tag );

    if ( containsAttribute( "SUPPRESSWHITESPACE" ) ) {
        endMarker = "</CFPROCESSINGDIRECTIVE>";
    } else if ( containsAttribute( "PAGEENCODING" ) ) {
        cfFile parentFile = getFile();
        if ( parentFile.processPageEncoding() ) {
            String pageEncoding = getConstant( "PAGEENCODING" );
            if ( pageEncoding.startsWith( "#" ) ) {
            	cfmBadFileException bfe = newBadFileException( "Invalid Attribute", "The value specified for PAGEENCODING must be a constant value" );
                bfe.setPageEncodingException( true );
                throw bfe;
            }
            pageEncoding = com.nary.util.Localization.convertCharSetToCharEncoding( pageEncoding );
            String parentEncoding = parentFile.getEncoding();
            if ( ( parentEncoding != null ) && !pageEncoding.equalsIgnoreCase( parentEncoding ) ) {
                cfmBadFileException bfe = newBadFileException( "Invalid Attribute", "The value specified for PAGEENCODING does not match the value specified in a previous CFPROCESSINGDIRECTIVE tag" );
                bfe.setPageEncodingException( true );
                throw bfe;
            }
            parentFile.setEncoding( pageEncoding );
        }
    } else {
        throw newBadFileException( "Invalid Attribute", "The CFPROCESSINGDIRECTIVE tag requires either the SUPPRESSWHITESPACE or PAGEENCODING attribute" );
    }

  
	}// defaultParameters()
	

  public String getEndMarker(){ 	return endMarker;  }


  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {

    if ( containsAttribute("SUPPRESSWHITESPACE") ){
		  boolean suppress = getDynamic( _Session, "SUPPRESSWHITESPACE" ).getBoolean();
          boolean origSuppress =_Session.setSuppressWhiteSpace( suppress );
		  super.render( _Session );
		  _Session.setSuppressWhiteSpace( origSuppress );
    }
    return cfTagReturnType.NORMAL;
  }// render()
	
}// cfProcessingDirective
