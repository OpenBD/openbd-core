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
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.tag.cffeed;

import java.io.File;

import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfFEED extends cfTag implements java.io.Serializable {
	static final long serialVersionUID = 1;

	
	@SuppressWarnings("unchecked")
	public java.util.Map getInfo() {
		return createInfo("remote", "CFFEED is a tag that allows you to create and read RSS feeds.");
	}



	@SuppressWarnings("unchecked")
	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] { 
				createAttInfo("ATTRIBUTECOLLECTION", "A structure containing the tag attributes", "", false), 
				createAttInfo("ACTION", "The action to be taken by the CFFEED tag. Valid values are 'CREATE','READ'.", "CREATE", true), 
				createAttInfo("NAME", "A structure of data to create the feed. Required if QUERY is blank.", "", false), 
				createAttInfo("QUERY", "A query object to create the feed.  Required if NAME is blank", "", false), 
				createAttInfo("PROPERTIES", "", "", true), 
				createAttInfo("OUTPUTFILE", "The file to store the output. Required if XMLVAR is blank.", "", false), 
				createAttInfo("OVERWRITE", "Boolean True / False to indicate whether to overwrite the OUTPUTFILE.", "", false), 
				createAttInfo("XMLVAR", "A variable name to output store the output. Required if OUTPUT file is blank.", "", false), 
				createAttInfo("COLUMNMAP", "", "", true)

		};
	}	
	
	
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		parseTagHeader( _tag );
		
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;
 
		
		if ( !containsAttribute( "ACTION" ) )
			throw newBadFileException( "No Action", "ACTION must be 'read' or 'create' 1" );
	}
	
	
	
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if ( !containsAttribute(attributes, "ACTION" ) )
			throw newBadFileException( "No Action", "ACTION must be 'read' or 'create' 2" );

		return attributes;
	}

	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		
		String action = getDynamic(attributes, _Session, "ACTION").getString();
		
		if ( action.equalsIgnoreCase("CREATE") ){
			checkActionCreateParams( attributes, _Session );
			
			CreateFeed createFeed = new CreateFeed();
			
			/* Create the Feed */
			if ( containsAttribute(attributes,"NAME") )
				createFeed.createViaStruct( this, (cfStructData)getDynamic(attributes, _Session,"NAME") );
			else if ( containsAttribute(attributes,"QUERY") )
				createFeed.createViaQuery( this, (cfQueryResultData)getDynamic(attributes, _Session,"QUERY"), (cfStructData)getDynamic(attributes, _Session,"PROPERTIES"), (cfStructData)getDynamic(attributes, _Session,"COLUMNMAP") );

			/* Produce the Feed */
			String feedStr = null;
			try{
				feedStr = createFeed.publishFeedToString();
			}catch(Exception e){
				throw newRunTimeException( "Error trying to publish feed: " + e.getMessage() );
			}

			/* Save the feed out */
			saveFeed( attributes, _Session, feedStr );

		} else if ( action.equalsIgnoreCase("READ") ) {
			
			throw newRunTimeException( "not yet supported -- hud on people give us a chance!");
			
		} else {
			throw newRunTimeException( "ACTION must be one of 'read' or 'create'");
		}

		return cfTagReturnType.NORMAL;
	}
	
	
	
	/*
	 * Responsible for writing the data out to the necessary structure required 
	 */
	private void saveFeed( cfStructData attributes, cfSession _Session, String feedContent ) throws cfmRunTimeException {
		
		/* Write to file if specified */
		if ( containsAttribute(attributes,"OUTPUTFILE") ){
			boolean bOverwriteFile = false;
			if ( containsAttribute(attributes,"OVERWRITE") )
				bOverwriteFile = getDynamic( attributes, _Session,"OVERWRITE").getBoolean();

			
			String outFileName = getDynamic(attributes, _Session,"OUTPUTFILE").getString();
			try{
				File	outFile	= new File( outFileName );

				if ( !bOverwriteFile && outFile.exists() )
					throw newRunTimeException( "File, " + outFile + ", already exists" );

				FileUtils.writeFile( outFile, feedContent );

			} catch (cfmRunTimeException rte ){
				throw rte;
			}catch(Exception e){
				throw newRunTimeException( "Failed to write, " + outFileName + "; " + e.getMessage() );
			}
			
		}
		
		/* Output to a variable */
		if ( containsAttribute(attributes,"XMLVAR") ){
			_Session.setData( getDynamic( attributes, _Session,"XMLVAR").getString(), new cfStringData(feedContent) );
		}
	}
	
	
	
	/*
	 * Checks the necessary attributes, for their existance and the correct data formats
	 */
	private void checkActionCreateParams( cfStructData attributes, cfSession _Session ) throws cfmRunTimeException {
		if ( !containsAttribute(attributes,"OUTPUTFILE") && !containsAttribute(attributes,"XMLVAR") )
			throw newRunTimeException( "You must specify at least the OUTPUTFILE or XMLVAR attributes");
		
		if ( containsAttribute(attributes,"NAME") && containsAttribute(attributes,"QUERY") )
			throw newRunTimeException( "You can not specify both NAME and QUERY attributes");

		if ( containsAttribute(attributes,"NAME") && containsAttribute(attributes,"PROPERTIES") )
			throw newRunTimeException( "You can not specify both NAME and PROPERTIES attributes");
		
		if ( (containsAttribute(attributes,"QUERY") && !containsAttribute(attributes,"PROPERTIES")) || (!containsAttribute(attributes,"QUERY") && containsAttribute(attributes,"PROPERTIES")) )
			throw newRunTimeException( "You must specify both QUERY and PROPERTIES attributes");
		
		if ( containsAttribute(attributes,"NAME") && getDynamic(attributes, _Session,"NAME").getDataType() != cfData.CFSTRUCTDATA )
			throw newRunTimeException( "NAME must be a structure");

		if ( containsAttribute(attributes,"QUERY") && getDynamic(attributes, _Session,"QUERY").getDataType() != cfData.CFQUERYRESULTDATA )
			throw newRunTimeException( "QUERY must be a query object");
		
		if ( containsAttribute(attributes,"COLUMNMAP") && getDynamic(attributes, _Session,"COLUMNMAP").getDataType() != cfData.CFSTRUCTDATA )
			throw newRunTimeException( "COLUMNMAP must be a query object");

		if ( containsAttribute(attributes,"PROPERTIES") && getDynamic(attributes, _Session,"PROPERTIES").getDataType() != cfData.CFSTRUCTDATA )
			throw newRunTimeException( "PROPERTIES must be a structure");
	}
}
