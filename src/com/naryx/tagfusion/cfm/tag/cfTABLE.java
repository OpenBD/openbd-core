/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

import com.naryx.tagfusion.cfm.engine.cfQueryInterface;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfTableData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.runTime;

/**
 * This class works in conjunction with the cfCOL class. 
 * Both classes share an instance of cftableData.
 *
 * Two differences between this class and CF.
 * 
 *  o It will not render the column headers if there are no results from the query. 
 *  o If the table is set to be non-HTML, no HTML tags will be rendered.
 */

public class cfTABLE extends cfTag implements Serializable {

	static final long	serialVersionUID	= 1;

  public static final String DATA_BIN_KEY = "CFTABLEDATA";

	// --[ Load cycle
	/**
	 * The CFTABLE must have an end tag
	 * 
	 * @returns string
	 */
	public String getEndMarker() {
		return "</CFTABLE>";
	}

	protected void defaultParameters( String _tag ) throws cfmBadFileException {

		defaultAttribute( "COLSPACING", 2 );
		defaultAttribute( "HEADERLINES", 2 );
		defaultAttribute( "STARTROW", 0 );
		defaultAttribute( "MAXROWS", -1 );

		parseTagHeader( _tag );

		if ( !containsAttribute( "QUERY" ) )
			throw newBadFileException( "Missing ATTRIBUTE",
					"You need to provide a QUERY attribute" );
	}

	// ---------------------------------------------------------------------------------------------------------
	// --[ Run cycle

	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		boolean processingCfOutput = _Session.setProcessingCfOutput( true );
		boolean whitespace = _Session.setSuppressWhiteSpace( false );
		
		cfTableData tableData = new cfTableData();
		_Session.setDataBin( DATA_BIN_KEY, tableData );
		
		tableData.colSpacing = getDynamic( _Session, "COLSPACING" ).getInt();
		tableData.headerLines = getDynamic( _Session, "HEADERLINES" ).getInt();

		if ( containsAttribute( "HTMLTABLE" ) )
			tableData.HTML = true;

		if ( containsAttribute( "COLHEADERS" ) )
			tableData.colHeaders = true;

		if ( tableData.HTML ) {
			_Session.write( "<TABLE " );

			if ( containsAttribute( "BORDER" ) )
				_Session.write( " BORDER " );

			_Session.write( ">" );
		} else
			_Session.write( "<PRE>" + "\n" );

		// ---------------------------------------------------------------------------------------------------------
		// --[ Deal with the query

		cfQueryInterface queryData = (cfQueryInterface) runTime.runExpression( _Session, getDynamic( _Session, "QUERY" ).getString() );

		if ( queryData != null ) {
			queryData.reset();

			int startRow = getDynamic( _Session, "STARTROW" ).getInt();
			int maxRows = getDynamic( _Session, "MAXROWS" ).getInt();
			int rowCount = 0;

			_Session.pushQuery( (cfQueryResultData) queryData );

			// --[ If there's no ResultSet from the QUERY, print the headers anyway
			int numOfRows = 0;

			// --[ For every row
			// ---------------------------------------------------------------
			while (queryData.nextRow()) {

				rowCount++;

				// --------[ Send the data out for the COLHEADERS
				if ( tableData.colHeaders ) {
					renderColumnsOnly( _Session );
				}

				if ( tableData.HTML && tableData.colHeaders )
					_Session.write( "</TR>" );

				else if ( tableData.colHeaders ) {
					for (int x = 0; x < tableData.headerLines; x++)
						_Session.write( "\n" );
				}

				// --[ Ok we've done the HEADERS, move on and set colHeaders to false;
				tableData.colHeaders = false;

				// --[ Get ourselves up to the start
				if ( rowCount < startRow )
					continue;

				numOfRows++;

				if ( tableData.HTML )
					_Session.write( "<TR>" );

				// --[ Send the data out
				renderColumnsOnly( _Session );

				if ( tableData.HTML )
					_Session.write( "</TR>" );
				else
					_Session.write( "\n" );

				// --[ Determine if the maximum rows have been reached
				if ( maxRows != -1 && numOfRows == maxRows )
					break;
			}
			// --[ -------------------------------------------------------------------------------

			queryData.finishQuery();
			_Session.popQuery();
			_Session.deleteDataBin( DATA_BIN_KEY );

			if ( tableData.HTML )
				_Session.write( "</TABLE>" );
			else
				_Session.write( "\r\n</PRE>" );
		}

		_Session.setProcessingCfOutput( processingCfOutput );
		_Session.setProcessingCfOutput( whitespace );
		
		return cfTagReturnType.NORMAL;
	}

	private void renderColumnsOnly( cfSession _Session ) throws cfmRunTimeException {
		for (int i = 0; i < childTagList.length; i++) {
			cfTag childTag = childTagList[i];

			if ( childTag instanceof cfCOL )
				childTag.render( _Session );
			else
				throw newRunTimeException( "An invalid tag was found" );
		}
	}
}
