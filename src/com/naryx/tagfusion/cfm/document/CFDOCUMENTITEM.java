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

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfOptionalBodyTag;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.cfm.tag.tagLocator;
import com.naryx.tagfusion.cfm.tag.tagReader;

public class CFDOCUMENTITEM extends cfTag implements cfOptionalBodyTag, Serializable{

	static final long serialVersionUID = 1;
	
	private static final String TAG_NAME = "CFDOCUMENTITEM";
	protected String endMarker = null;

	public String getEndMarker() {
		return endMarker;
	}

	public void setEndTag() {
		endMarker = null;
	}

	public void lookAheadForEndTag(tagReader inFile) {
		endMarker = (new tagLocator(TAG_NAME, inFile)).findEndMarker();
	}

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute( "ALIGN", "left" );
		
		parseTagHeader(_tag);
		if (!containsAttribute("TYPE"))
			throw missingAttributeException( "CFDOCUMENTITEM tag must contain a TYPE attribute", null );
	}
	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException{
		String type = getDynamic( _Session, "TYPE" ).getString().toLowerCase();
		
		DocumentContainer doc = (DocumentContainer) _Session.getDataBin( cfDOCUMENT.CFDOCUMENT_KEY );
		
		if ( type.equals( "pagebreak" ) ){
			String mimeType = doc.getCurrentSectionMimeType();
			if ( mimeType == null || mimeType.toUpperCase().indexOf("HTML") != -1 ){
				// Insert a HTML CSS based page break into the content
				_Session.write("<div style=\"page-break-after: always\">&nbsp;</div>");
			}else{
				// Cannot implement a page break in non-HTML content
				throw newRunTimeException("CFDOCUMENTITEM type=PAGEBREAK is not support for mimetype: " + 
						mimeType );
			}
		}else if ( type.equals( "header" ) ){
			insertVariables( _Session );
			doc.setHeader( "\"" + renderToString(_Session, cfTag.HONOR_CF_SETTING).getOutput() + "\"", 
					getAlign( _Session ) );
		}else if ( type.equals( "footer" ) ){
			insertVariables( _Session );
			doc.setFooter( "\"" + renderToString(_Session, cfTag.HONOR_CF_SETTING).getOutput() + "\"",
					getAlign( _Session ) );
		}else{
			throw newRunTimeException(catchDataFactory.invalidTagAttributeException(this, 
					"TYPE attribute contains unsupported value"));
		}
		
		return cfTagReturnType.NORMAL;
	}
	
	private String getAlign( cfSession _Session ) throws cfmRunTimeException{
		String align = getDynamic( _Session, "ALIGN" ).getString().toLowerCase();
		if ( !(align.equals( "center" ) || align.equals( "left" ) || align.equals( "right" )) ){
			throw newRunTimeException(catchDataFactory.invalidTagAttributeException(this, 
				"Unsupported ALIGN attribute value. Valid values are \"center\", \"left\" and \"right\""));
		}
		return align;
	}
	
	private void insertVariables( cfSession _Session ) throws cfmRunTimeException{
		cfStructData cfdocVars = new cfStructData();
		cfdocVars.setData( "currentpagenumber", new cfStringData( "BD:CURRENTPAGENUMBER" ) );
		cfdocVars.setData( "totalpagecount", new cfStringData( "BD:TOTALPAGECOUNT" ) );
		cfdocVars.setData( "totalsectionpagecount", new cfStringData( "BD:TOTALSECTIONPAGECOUNT" ) );
		cfdocVars.setData( "currentsectionpagenumber", new cfStringData( "BD:CURRENTSECTIONPAGENUMBER" ) );
		_Session.setData( "cfdocument", cfdocVars );
	}
}
