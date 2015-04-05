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

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfTableData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class cfCOL extends cfTag implements Serializable {
	static final long	serialVersionUID	= 1;


  @SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
  	return createInfo("output", "Defines the column header.  Must be inside a CFTABLE");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
   			createAttInfo( "ALIGN", "The alignment of the column", 	"left", false ),
   			createAttInfo( "WIDTH", "The width of the column", 	"20", false ),
   			createAttInfo( "TEXT", 	"The text to display", 	"", true ),
   			createAttInfo( "HEADER", "Header text for the column", 	"", false )
  	};
  }

	
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "ALIGN", "left" );
		defaultAttribute( "WIDTH", "20" );
		parseTagHeader( _tag );

		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;
		
		if ( !containsAttribute( "TEXT" ) )
			throw newBadFileException("Missing ATTRIBUTE","the following required attributes have not been provided: (TEXT) for the CFCOL tag" );
	}

  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if ( !containsAttribute( attributes, "TEXT" ) )
			throw newBadFileException("Missing ATTRIBUTE","the following required attributes have not been provided: (TEXT) for the CFCOL tag" );
    return	attributes;
	}

	
	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		cfTableData tableData = (cfTableData) _Session.getDataBin( cfTABLE.DATA_BIN_KEY );

		if ( tableData == null ) {
			throw newRunTimeException( "CFCOL must be nested inside tag CFTABLE" );
		} else {
	  	cfStructData attributes = setAttributeCollection(_Session);

			
			String justifyStr = getDynamic(attributes, _Session, "ALIGN" ).getString().toLowerCase();
			byte justify = string.LEFT;
			
			if ( justifyStr.equals( "right" ) ){
				justify = string.RIGHT;
			}else if ( justifyStr.equals( "center" ) ){
				justify = string.CENTER;
			}
			
			// --[ If the column tag has HEADER
			if ( tableData.colHeaders ) {

				if ( containsAttribute( attributes,"HEADER" ) ) {

					if ( tableData.HTML ){
						_Session.write( "<th align='"
								+ getDynamic(attributes, _Session, "ALIGN" ).getString() + "'>"
								+ getDynamic(attributes, _Session, "HEADER" ).getString() + "</th>" );
					}else{
						//--[ Non-HTML table

						String str = getDynamic(attributes, _Session, "HEADER" ).getString();
						int width = getDynamic(attributes, _Session, "WIDTH" ).getInt();

						if ( str.length() > width ){
							str = substringNonHTML( str, width );
						}

						_Session.write( string.justify( justify, width, str ) );

						for (int x = 0; x < tableData.colSpacing; x++)
							_Session.write( " " );

					}
				}
				//--[ If there's no header info for the CFCOL tag and it's an HTML table, insert an empty <TH>
				else {
					if ( tableData.HTML )
						_Session.write( "<th></th>" );
				}
			}

			//[------------------------------------------
			//[-- The rest of the rows
			else {

				//--[ If it's HTML
				if ( tableData.HTML ) {
					_Session.write( "<td" );

					_Session.write( " align='" + getDynamic( attributes,_Session, "ALIGN" ).getString() + "'>" );

					if ( containsAttribute( "TEXT" ) ){
						_Session.write( getDynamic(attributes, _Session, "TEXT" ).getString() );
					}
					
					_Session.write( "</td>" );

				} else { //--[ If it's not HTML
					String str = getDynamic(attributes, _Session, "TEXT" ).getString();
					int width = getDynamic(attributes, _Session, "WIDTH" ).getInt();

					if ( str.length() > width ){
						str = substringNonHTML( str, width );
					}
					
					_Session.write( string.justify( justify, width, str ) );

					for (int x = 0; x < tableData.colSpacing; x++)
						_Session.write( " " );
				}
			}
		}
		
		return cfTagReturnType.NORMAL;
	}
	
	// reduces the length of the string to the provided width but ignoring html elements in the string
	private static String substringNonHTML( String _in, int _width ){
		StringBuilder sb = new StringBuilder();
		char [] chars = _in.toCharArray();
		int newLen = 0;
		boolean inTag = false;
		for ( int i = 0; i < chars.length; i++ ){
			
			if ( inTag ){
				if ( chars[i] == '>' ){
					inTag = false;
				}
				sb.append( chars[i] );
			}else if ( chars[i] == '<' ){
				inTag = true;
				sb.append( chars[i] );
				
			}else if ( newLen < _width ){
				sb.append( chars[i] );
				newLen++;
			}
		}
		
		return sb.toString();
	}
}
