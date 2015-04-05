/* 
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.document;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfOptionalBodyTag;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.cfm.tag.tagLocator;
import com.naryx.tagfusion.cfm.tag.tagReader;

public class CFDOCUMENTSECTION extends cfTag implements cfOptionalBodyTag, Serializable{

	static final long serialVersionUID = 1;
	
  private static final String TAG_NAME = "CFDOCUMENTSECTION";
	protected String endMarker = null;
	
	
	public String getEndMarker(){
		return endMarker;  
	}
	
  public void setEndTag() {
    endMarker = null;
  }

  public void lookAheadForEndTag(tagReader inFile) {
    endMarker = (new tagLocator(TAG_NAME, inFile)).findEndMarker();
  }

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute( "USERAGENT", "OpenBD" );
		
		parseTagHeader(_tag);
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException
	{
		DocumentContainer doc = (DocumentContainer) _Session.getDataBin( cfDOCUMENT.CFDOCUMENT_KEY );
		if ( doc == null ){
			throw newRunTimeException("CFDOCUMENTSECTION must be nested within a CFDOCUMENT tag" );
		}

		doc.setInSection( true );
		DocumentSection newSection = new DocumentSection();

		// default the header/footer to the parent ones (if set)
		newSection.setHeader( doc.getMainHeader(), doc.getMainHeaderAlign() );
		newSection.setFooter( doc.getMainFooter(), doc.getMainFooterAlign() );
		
		appendSectionAttributes( _Session, newSection );
		doc.addSection( newSection ); // needs to be done before renderToString() call so that headers/footers are appended
		
		if ( containsAttribute( "SRC" ) && containsAttribute( "SRCFILE" ) ){
			throw newRunTimeException( "Invalid attribute combination. Either the SRC or SRCFILE attribute must be specified but not both" );
		}

		String renderedBody = renderToString( _Session ).getOutput(); 
		if ( renderedBody.length() == 0 && !( containsAttribute( "SRC" ) || containsAttribute( "SRCFILE" ) ) ){
			throw newRunTimeException( "Cannot create a PDF from an empty document!" );	
		}
		
		String src = containsAttribute( "SRC" ) ? getDynamic( _Session, "SRC" ).getString() : null;
		String srcFile = containsAttribute( "SRCFILE" ) ? getDynamic( _Session, "SRCFILE" ).getString() : null;

		newSection.setSources( src, srcFile, renderedBody );

		doc.setInSection( false );
		return cfTagReturnType.NORMAL;
	}
	
	private void appendSectionAttributes( cfSession _Session, DocumentSection _section ) throws cfmRunTimeException{
		if ( containsAttribute( "MIMETYPE" ) )
			_section.setMimeType( getDynamic( _Session, "MIMETYPE" ).toString() );

		if ( containsAttribute( "NAME" ) )
			_section.setName( getDynamic( _Session, "NAME" ).toString() );

		if ( containsAttribute( "MARGINTOP" ) )
			_section.setMarginTop( getDynamic( _Session, "MARGINTOP" ).toString() );
		if ( containsAttribute( "MARGINBOTTOM" ) )
			_section.setMarginBottom( getDynamic( _Session, "MARGINBOTTOM" ).toString() );
		if ( containsAttribute( "MARGINLEFT" ) )
			_section.setMarginLeft( getDynamic( _Session, "MARGINLEFT" ).toString() );
		if ( containsAttribute( "MARGINRIGHT" ) )
			_section.setMarginRight( getDynamic( _Session, "MARGINRIGHT" ).toString() );
		
		_section.setUserAgent( getDynamic( _Session, "USERAGENT" ).getString() );
		
		if ( containsAttribute( "AUTHPASSWORD" ) && containsAttribute( "AUTHUSER" ) ){
			_section.setAuthentication( getDynamic( _Session, "AUTHUSER" ).getString(), getDynamic( _Session, "AUTHPASSWORD" ).getString() );
		}

	}
}
