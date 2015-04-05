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
 *  http://openbd.org/
 *  $Id: cfOUTPUT.java 2217 2012-07-27 12:38:10Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryInterface;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.runTime;

public class cfOUTPUT extends cfTag implements Serializable {
  static final long serialVersionUID = 1;
  
	public java.util.Map getInfo(){
		return createInfo("output", "Inside a CFOUTPUT tag, all CFML variables will be processed for output to the browser, and can also be used to loop a query for output of each row.");
	}
  
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo("ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
				createAttInfo("QUERY", "Providing a query to this tag will allow it to act like a CFLOOP and iterate over the query for outputting its values.", "", false ),
				createAttInfo("GROUP", "The column name to be used for grouping the records of a query. This attribute is only used if a 'QUERY' has been provided.", "", false ),
				createAttInfo("STARTROW", "The first row of a query to be output. This attribute is only used if a 'QUERY' has been provided.", "1", false ),
				createAttInfo("MAXROWS", "The maximum number of query rows to output. This attribute is only used if a 'QUERY' has been provided.", "-1", false ),
				createAttInfo("GROUPCASESENSITIVE", "If set to 'YES' then the case will be considered when grouping records of a query. This attribute is only used if the 'GROUP' attribute has been provided.", "YES", false )
		};
	}

	public boolean doesTagHaveEmbeddedPoundSigns() {
		return true;
	}

  protected void defaultParameters( String _tag ) throws cfmBadFileException {
    parseTagHeader( _tag );
  }
  
  public String getEndMarker(){	return "</CFOUTPUT>";  }

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		
		boolean processingCfOutput = _Session.setProcessingCfOutput(true);

		try {
			// This may be a CFOUTPUT for a query
			cfQueryInterface queryData = null;
			cfData queryDataTmp = null; // used till we can determine the query data is the right type
			String QUERY = null;
			
			if ( containsAttribute(attributes,"QUERY") ) { // INVARIANT : on exiting this if, queryData != null
				QUERY = getDynamic(attributes,_Session, "QUERY").getString();


				try {
					queryDataTmp = runTime.runExpression(_Session, QUERY);
				} catch (cfmRunTimeException ignored) {
				} // queryDataTmp doesn't exist

				
				// throw exceptions if the query doesn't exist or is not the right type. Cast it if it is.
				if (queryDataTmp == null) {
					throw newRunTimeException("The specified QUERY " + QUERY + " does not exist.");
				} else if (queryDataTmp instanceof cfQueryInterface) {
					queryData = (cfQueryInterface) queryDataTmp;
				} else {
					throw newRunTimeException("The specified QUERY " + QUERY + " is not a valid query type.");
				}
			}

			if (queryData != null) {
				int resetRow = queryData.getCurrentRow();
				int startRow = containsAttribute("STARTROW") ? getDynamic(attributes,_Session, "STARTROW").getInt() : 1;
				int maxRows	 = containsAttribute("MAXROWS") ? getDynamic(attributes,_Session, "MAXROWS").getInt() : -1;
				int rowCount = 0;

				queryData.reset();
				_Session.pushQuery((cfQueryResultData) queryData);

				boolean isGroupBy = containsAttribute(attributes,"GROUP");

				// Check for the GROUPBY stuff
				if (isGroupBy) {
					boolean groupSensitive = containsAttribute(attributes, "GROUPCASESENSITIVE") ? getDynamic(attributes,_Session, "GROUPCASESENSITIVE").getBoolean() : true; 
					queryData.setGroupBy(getDynamic(attributes,_Session, "GROUP").getString(), groupSensitive );
				}

				while (queryData.nextRow()) {
					rowCount++;

					// Get ourselves up to the start
					if (rowCount < startRow)
						continue;

					// Send the data out
					cfTagReturnType rt = super.render( _Session );
					if ( rt.isBreak() || rt.isReturn() ) {
						cleanupQueryData(_Session, queryData, resetRow, isGroupBy);
						return rt;
					}

					// Determine if the maximum rows have been reached
					if ( maxRows != -1 && rowCount == (startRow + maxRows - 1) )
						break;
				}

				cleanupQueryData(_Session, queryData, resetRow, isGroupBy);

			} else { // queryData == null (i.e. QUERY attribute not specified)

				queryData = _Session.peekQuery();
				if (queryData != null && queryData.isGrouped()) {
					if (containsAttribute("GROUP")) {
						int resetRow = queryData.getCurrentRow();
						boolean groupSensitive = containsAttribute(attributes, "GROUPCASESENSITIVE") ? getDynamic(attributes,_Session, "GROUPCASESENSITIVE").getBoolean() : true; 
						
						queryData.setGroupBy(getDynamic(attributes,_Session, "GROUP").getString(), groupSensitive );
						do {
							cfTagReturnType rt = super.render( _Session );
							if ( rt.isBreak() || rt.isReturn() ) {
								queryData.removeGroupBy();
								queryData.setCurrentRow( resetRow == 0 ? 1 : resetRow );
								return rt;
							}
						} while (queryData.nextRow());

						queryData.removeGroupBy();
						queryData.setCurrentRow( resetRow == 0 ? 1 : resetRow );
					} else {
						return renderQueryGrouped(_Session, queryData);
					}
				} else {
					return super.render(_Session);
				}
			}
		} finally {
			_Session.setProcessingCfOutput(processingCfOutput);
		}
		
		return cfTagReturnType.NORMAL;
	}
	
	public static void cleanupQueryData(cfSession _Session, cfQueryInterface queryData, int resetRow, boolean _isGroupBy) {
		queryData.finishQuery();
		_Session.popQuery();
		
		if ( _isGroupBy )
			queryData.removeGroupBy();
		
		queryData.setCurrentRow( resetRow == 0 ? 1 : resetRow );
	}

	private cfTagReturnType renderQueryGrouped( cfSession _Session, cfQueryInterface queryData ) throws cfmRunTimeException {
		queryData.startGroupOutput();
		
		while ( queryData.nextRowInGroup() ) {
			cfTagReturnType rt = super.render( _Session );
			if ( rt.isBreak() || rt.isReturn() ) {
				queryData.endGroupOutput();
				return rt;
			}
		}
		
		queryData.endGroupOutput();
		
		return cfTagReturnType.NORMAL;
	}
}
