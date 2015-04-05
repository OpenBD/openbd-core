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

package com.naryx.tagfusion.cfm.http;

 /**
  * cfHttpQueryData is used to construct a query object from a file
  * returned by cfHttp.
  */


import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nary.util.FastMap;
import com.nary.util.string;
import com.nary.util.stringtokenizer;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.runTime;

public class cfHttpQueryData {

  private String errorLog;
  private final BufferedReader rawQuery;


  /**
   * creates a cfHttpQueryData object.
   * @param _recover - set to true if want to try recover from a illformatted file.
   */
  public cfHttpQueryData( cfSession _Session, String _file, String _columns,
    String _name, char _delimiter, String _qualifier, boolean _recover, boolean _useFirstLine )
    throws cfmRunTimeException{
    rawQuery = new java.io.BufferedReader(new java.io.StringReader( _file ));
    cfQueryResultData query = createQuery(_Session, _columns, _delimiter, _qualifier, _useFirstLine, _recover);

		cfData d = runTime.runExpression( _Session, _name, false );
		if ( d instanceof com.naryx.tagfusion.cfm.parser.cfLData ){
			( (com.naryx.tagfusion.cfm.parser.cfLData) d ).Set( query, _Session.getCFContext() );
		}

  }// cfHttpQueryData()


	// ensures there are no identical column headers in _cols
  private static String [] processColumns(String [] _cols){
	Map<String, String> colsUsed = new FastMap<String, String>();
    String column; // a single column header
    for (int i = 0; i < _cols.length; i++ ){

      column = _cols[ i ];
      while (colsUsed.containsKey(column)){
        column = column + "_";
      }
 	  _cols[ i ] = column;

      colsUsed.put(column, "");
    }// for

    return _cols;

  }// processCols


	private String[] getColumns(String _columns, char _delimiter, String _textQualifier, boolean _useFirstLine ) throws cfmRunTimeException{
		String columns = _columns;
		String [] headersArray = null;
		List<String> headers;
		boolean useFirstLineAsCols = false;
		boolean createCols = false;

    try{
      // if columns hasn't been set then read it from the raw query
      if (columns == null && _useFirstLine ){
        columns = rawQuery.readLine();
        useFirstLineAsCols = true;
      // else ignore the first line
      }else if ( _useFirstLine ){
		    rawQuery.readLine();
      }else if ( columns == null ){
      	createCols = true;
      }
		}catch(IOException ignored){
			throw new cfmRunTimeException( catchDataFactory.generalException(	"errorCode.runtimeError",
																																				"cfhttp.columnHeaders",
																																				null ) );
		}

		// extract the individual columns from columns
		// if textQualifier exists
		if ( !createCols ){
	    if ( useFirstLineAsCols && _textQualifier.length() == 1){

	      // note that if the COLUMNS is specified, the delimiter used is ','
	      headers = processQueryLine( columns, ( _columns != null ) ? ',' : _delimiter, _textQualifier.charAt(0) );
	      if ( headers == null ){
	        throw new cfmRunTimeException( catchDataFactory.generalException( "errorCode.runtimeError",
	                                                                                  "cfhttp.invalidQuery",
	                                                                                  new String[]{_columns} ) );
	      }

				// convert to String [] for returning
	      headersArray = new String[ headers.size() ];
	      for ( int i = 0; i < headersArray.length; i++ ){
	        headersArray[i] = headers.get(i);
	      }


			}else{ // textQualifier doesn't exist
				headersArray = com.nary.util.string.convertToList(columns,  ( _columns != null ) ? ',' : _delimiter);
	    }

	    for ( int i = 0; i < headersArray.length; i++ ){
	    	validateColumn( headersArray[i] );
	    }

	    headersArray = processColumns( headersArray );
		}
    return headersArray;
	}


	private static void validateColumn( String _colName ) throws cfmRunTimeException{
		char [] chars = _colName.toCharArray();

		if ( chars.length == 0 )
			return;

		if ( !Character.isLetter( chars[0] ) )
			throw new cfmRunTimeException( catchDataFactory.generalException(	"errorCode.runtimeError",
					"cfhttp.invalidQueryColumn",
					new String[]{_colName} ) );

		for ( int i = 1; i < chars.length; i++ ){
			char nextCh = chars[i];
			if ( !( Character.isLetter( nextCh ) || Character.isDigit( nextCh ) || nextCh == '_' ) )
				throw new cfmRunTimeException( catchDataFactory.generalException(	"errorCode.runtimeError",
						"cfhttp.invalidQueryColumn",
						new String[]{_colName} ) );
		}
		// check for non-char
	}


	private cfQueryResultData createQuery(cfSession _Session, String _columns, char _delimiter, String _textQualifier, boolean _useFirstLine, boolean _recover) throws cfmRunTimeException{
		String errorLog = ""; // logs any errors in the query data

		List<String> rowEntries;
		String rowData = null;
		int rowCount = 1;
		int numberCols;

		String [] columns = getColumns(_columns, _delimiter, _textQualifier, _useFirstLine);
		if ( columns == null ){
			try{
				rowData = rawQuery.readLine();
				int colCount = com.nary.util.string.occurrenceCount(rowData, _delimiter) + 1;
				columns = new String[ colCount ];
				for ( int i = 0; i < columns.length; i++ ){
					columns[i] = "COLUMN_" + (i+1);
				}
			}catch( IOException ioe ){
				columns = new String[0];
			}
		}

		@SuppressWarnings( "resource" )
		cfQueryResultData query = new cfQueryResultData( columns, "CFHTTP" );
		numberCols = columns.length;

		try{
			if ( rowData == null ){
				rowData = rawQuery.readLine();
			}
			while (rowData != null){
				// ignore any blank lines in the query file
				if (rowData.trim().length() == 0){
					rowData = rawQuery.readLine();
					continue;
				}

				query.addRow(1);

				int colCount;

				if ( _textQualifier.length() > 0 ){

					rowEntries = processQueryLine( rowData, _delimiter, _textQualifier.charAt(0) );
					if ( rowEntries == null ){
						throw new cfmRunTimeException( catchDataFactory.generalException(  "errorCode.runtimeError",
								"cfhttp.invalidQuery",
								new String[]{rowData} ) );
					}
				}else{
					rowEntries = new ArrayList<String>();
					stringtokenizer st = new stringtokenizer( rowData, String.valueOf( _delimiter ) );
					while ( st.hasMoreTokens() ){
						rowEntries.add( st.nextToken() );
					}
				}


				colCount = rowEntries.size();

				// if there are an invalid number of columns in the row
				if (colCount != numberCols){
					if (!_recover){
						throw new cfmRunTimeException( catchDataFactory.generalException(  "errorCode.runtimeError",
								"cfhttp.invalidQuery",
								new String[]{rowData} ) );
					}else{
						errorLog += "Invalid number of columns in row : " + rowData;
					}

				}else{ // add the row
					for (int colIndex = 0; colIndex < numberCols; colIndex++){
						query.setCell(rowCount, colIndex+1, new cfStringData( rowEntries.get(colIndex) ));
					}
					rowCount++; // note this isn't incremented when an invalid row is encountered

				}

				rowData = rawQuery.readLine();
				if (errorLog.equals("")){
					errorLog = "Query successfully parsed";
				}
			}
		}catch( IOException e ){}

		return query;
  }


  /**
   * Creates a List of Strings from the given String _values using
   * the specified _delimiter and _qualifier to determine the elemenents
   * of the list.
   * If the String is an invalid format then the method returns null
   */
	private static List<String> processQueryLine( String _values, char _delimiter, char _qualifier ){
    List<String> strs = new ArrayList<String>();

    int nextQualifier = _values.indexOf( _qualifier );
    int nextDelim = _values.indexOf( _delimiter );


    // if the line doesn't contain the txt qualifier then it's easy
    if ( nextQualifier == -1 ){
      com.nary.util.stringtokenizer st = new com.nary.util.stringtokenizer( _values, "" + _delimiter );
      while( st.hasMoreTokens() )
        strs.add( st.nextToken() );

    }else{

      int i = 0;
      StringBuilder values = new StringBuilder( _values );
      String qualifierStr = String.valueOf( _qualifier );
      String delimStr = String.valueOf( _delimiter );

      while( i < values.length() ){
        // if starts with qualifier
        if ( values.charAt(i) == _qualifier ){
          // look for valid end qualifier
          int strStart = i+1;
          do{

            nextQualifier = string.indexOf( values, qualifierStr, i+1 );
            // if reached end of string
            if ( nextQualifier == -1 )
              return null;
            if ( nextQualifier+1 < values.length() && values.charAt( nextQualifier+1 ) == _qualifier ){
              i=nextQualifier;
              values = values.deleteCharAt( nextQualifier ); // remove escape char
            }else{
              break;
            }
          }while( true );

          if ( nextQualifier+1 < values.length() && values.charAt(nextQualifier+1) != _delimiter ){
            return null;
          }

          String nextStr = values.substring( strStart, nextQualifier );
          strs.add( nextStr );
          i = nextQualifier + 2;

        }else{
          if ( nextDelim == -1 ){
            String nextStr = values.substring( i ).trim();
            strs.add( nextStr );
            break;
          }else{
            String nextStr = values.substring( i, nextDelim ).trim();
            strs.add( nextStr );
            i = nextDelim + 1;
          }
        }

        nextDelim = string.indexOf( values, delimStr, i );

      }
    }
    return strs;

  }

  public String getErrorLog(){
    return errorLog;
  }// getErrorLog()

}// cfHttpQueryData
