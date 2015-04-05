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

package com.naryx.tagfusion.cfm.wddx;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Stack;
import java.util.TimeZone;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.nary.util.FastMap;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

/**
 *  Handler used in WDDX2CFML for handling SAX events
 */

public class wddxHandler extends DefaultHandler {

	cfSession session;
	String current;
	String columnList[];
	String varName = null;
	String columnName = null;
	wddxStruct struct = null;
	String boolValue;
	char[] globalBuffer;
	int globalOffset, globalLen;

	wddxArray array, innerArray, outerArray = null;
	int rowNum = 0;
	Stack context;
	wddxQueryResult resultSet;
	wddxStruct innerStruct, outerStruct;
	StringBuilder content;

	cfData result = null;
  
	/**
	 *  Constructor for the wddxHandler object
	 *
	 *@param  _Session  Description of Parameter
	 *@param  _output   Description of Parameter
	 */
	public wddxHandler( cfSession _Session ){
		session = _Session;
		context = new Stack();
		content = new StringBuilder();
	}
	
    public cfData getResult(){
      return result;
    }
    
	/**
	 *  Description of the Method
	 *
	 *@param  namespaceURI      Description of Parameter
	 *@param  eName             Description of Parameter
	 *@param  qName             Description of Parameter
	 *@param  attrs             Description of Parameter
	 *@exception  SAXException  Description of Exception
	 */
	public void startElement( String namespaceURI, String eName, String qName, Attributes attrs ) throws SAXException {
		current = qName.toLowerCase();
		
		if ( !qName.equals( "char" ) ){
			content.setLength( 0 );
			rowNum++;
		}
		
		if ( qName.equals( "struct" ) ) {
			struct = new wddxStruct( varName, new FastMap( FastMap.CASE_INSENSITIVE ) );
			context.push( struct );
		} else if ( qName.equals( "recordset" ) ) {

			List<String> tokens = string.split( attrs.getValue( "fieldNames" ), "," );
			this.columnList = new String[tokens.size()];
			int counter = 0;

			for ( int i = 0; i < tokens.size(); i++ ) {
				columnList[counter] = tokens.get(i).toString();
				counter++;
			}
			cfQueryResultData queryData = new cfQueryResultData( columnList, "SQL Query" );
      int rowCount = com.nary.util.string.convertToInteger( attrs.getValue( "rowCount" ), 0 );
			if ( rowCount > 0 ) 
        queryData.addRow( rowCount );

			resultSet = new wddxQueryResult( varName, queryData );
			context.push( resultSet );
		} else if ( qName.equals( "field" ) ) {
			rowNum = 0;
			columnName = attrs.getValue( "name" );
		} else if ( qName.equals( "array" ) ) {
			array = new wddxArray( varName, cfArrayData.createArray( 1 ) );
			context.push( array );
		} else if ( qName.equals( "var" ) ) {
			varName = attrs.getValue( "name" );
		} else if ( qName.equals( "boolean" ) ) {
			boolValue = attrs.getValue( "value" );

			if ( !context.empty() ) {
				if ( context.peek() instanceof wddxStruct ) {
					struct = ( wddxStruct ) context.pop();
					struct.put( varName, cfBooleanData.getcfBooleanData( boolValue ) );
					context.push( struct );
					varName = null;
				} else if ( context.peek() instanceof wddxArray ) {
					outerArray = ( wddxArray ) context.pop();
					try {
						outerArray.addElement( cfBooleanData.getcfBooleanData( boolValue ) );
					} catch (cfmRunTimeException ex) { throw new SAXException (ex); }
					context.push( outerArray );
				} else if ( context.peek() instanceof cfQueryResultData ) {
					resultSet = ( wddxQueryResult ) context.pop();
					resultSet.setCell( rowNum, columnName, cfBooleanData.getcfBooleanData( boolValue ) );
					context.push( resultSet );
				}
			} else {
				result = cfBooleanData.getcfBooleanData( boolValue );
			}
		} else if ( qName.equals( "char") ){
			 String code = attrs.getValue( "code" );
			 try{
			 	int chCode = Integer.parseInt( code, 16 );
			 	content.append( (char) chCode );
			 }catch( NumberFormatException e ){
			 	 throw new SAXException( new Exception( "Invalid character code specified in char element: " + code + ".") );	
			 }
    }else if ( qName.equals( "null" ) ){
      
		// If current is not in the list of valid names then throw an exception.
		}else {
			if ( !current.equals( "wddxpacket" ) && !current.equals( "string" ) && !current.equals( "datetime" ) && !current.equals( "number" ) && !current.equals( "header" ) && !current.equals( "binary" ) && !current.equals( "datetime" ) && !current.equals( "data" ) ) {
				throw new SAXException( new Exception( current + " is not a valid WDDX element." ) );
			}
		}

	}


	//--------------------------------------------------------------------------------
	// Called everytime characters between tags are encountered
	//--------------------------------------------------------------------------------

	/**
	 *  Description of the Method
	 *
	 *@param  buf               Description of Parameter
	 *@param  offset            Description of Parameter
	 *@param  len               Description of Parameter
	 *@exception  SAXException  Description of Exception
	 */
	public void characters( char buf[], int offset, int len ) {
		globalBuffer = buf;
		if ( !current.equals( "char" ) ){
			content.append( buf, offset, len );
		}
		globalOffset = offset;
		globalLen = len;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  namespaceURI      The Namespace URI, or the empty string if the element has no Namespace URI 
	 * 													or if Namespace processing is not being performed.
	 *@param  sName             The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 *@param  qName             The qualified XML 1.0 name (with prefix), or the empty string if qualified names are not available.
	 *@exception  SAXException  
	 */
	public void endElement( String namespaceURI, String sName, String qName ) throws SAXException {

    current = qName.toLowerCase();
    
		if ( globalBuffer != null ) {
			getCharacterData( content );
			globalBuffer = null;
		} else {
			if ( qName.equalsIgnoreCase( "string" ) || current.equals( "null" ) ) {
				getCharacterData( content );
			}
		}

		if ( qName.equalsIgnoreCase( "recordset" ) ) {
			if ( !context.empty() ) {
      	resultSet = ( wddxQueryResult ) context.pop();

      	if ( !context.empty() ) {

      		if ( context.peek() instanceof wddxArray ) {
      			outerArray = ( wddxArray ) context.pop();
      			try {
      				outerArray.addElement( resultSet.getData() );
				} catch (cfmRunTimeException ex) { throw new SAXException (ex); }      				
      			context.push( outerArray );
      		} else if ( context.peek() instanceof wddxStruct ) {
      			outerStruct = ( wddxStruct ) context.pop();
      			outerStruct.put( resultSet.getName(), resultSet.getData() );
      			context.push( outerStruct );
      		}
      	} else {
      		result = resultSet.getData();
      	}
      }
		}else	if ( qName.equalsIgnoreCase( "array" ) ) {
			if ( !context.empty() ) {
      	innerArray = ( wddxArray ) context.pop();

      	if ( !context.empty() ) {

      		if ( context.peek() instanceof wddxStruct ) {
      			outerStruct = ( wddxStruct ) context.pop();
      			outerStruct.put( innerArray.getName(), innerArray.getData() );
      			context.push( outerStruct );
      		} else if ( context.peek() instanceof wddxArray ) {
      			outerArray = ( wddxArray ) context.pop();
      			try {
      				outerArray.addElement( innerArray.getData() );
				} catch (cfmRunTimeException ex) { throw new SAXException (ex); }
      			context.push( outerArray );
      		}
      	} else {
      		result = innerArray.getData();
      	}
      }
		}else	if ( qName.equalsIgnoreCase( "struct" ) ) {
			if ( !context.empty() ) {
      	innerStruct = ( wddxStruct ) context.pop();
      	if ( !context.empty() ) {

      		if ( context.peek() instanceof wddxStruct ) {
      			outerStruct = ( wddxStruct ) context.pop();
      			outerStruct.put( innerStruct.getName(), new cfStructData( innerStruct.getData() ) );
      			context.push( outerStruct );
      		} else if ( context.peek() instanceof wddxArray ) {
      			outerArray = ( wddxArray ) context.pop();
      			try {
      				outerArray.addElement( new cfStructData( innerStruct.getData() ) );
				} catch (cfmRunTimeException ex) { throw new SAXException (ex); }
      			context.push( outerArray );
      		}
      	} else {
      		result = new cfStructData( innerStruct.getData() );
      	}
      }
		}else	if ( qName.equalsIgnoreCase( "char" ) ) {
			current = "string";
		}
	}


	/**
	 *  Gets the characterData attribute of the wddxHandler object
	 *
	 *@param  buf               Description of Parameter
	 *@exception  SAXException  Description of Exception
	 */
	private void getCharacterData( StringBuilder buf ) throws SAXException {
		cfData data = null;
    boolean usedData = false; // set to true if anything useful was obtained from the buffer

		if ( current.equals( "number" ) ) {
			try {
				data = new cfStringData( buf.toString() ).getNumber();
        usedData = true;
			} catch ( dataNotSupportedException de ) {
				throw new SAXException( de );
			}
		}

		if ( current.equals( "binary" ) ) {
			try {
				String temp = buf.toString();
				byte b[] = temp.getBytes();
				data = new cfBinaryData( com.nary.net.Base64.base64Decode( b ) );
        usedData = true;
			} catch ( Exception e ) {
				throw new SAXException( e );
			}
		}

		if ( current.equals( "string" ) ) {
			data = new cfStringData( buf.toString() );
      usedData = true;
		} else if ( current.equals( "datetime" ) ) {
			try {
				data = dateFromWddx( buf.toString() );
        usedData = true;
			} catch ( Exception e ) {
				throw new SAXException( e );
			}
		}else if ( current.equals("null") ){
      data = new cfStringData("");
      usedData = true;
    }

		if ( !context.empty() && usedData ){ 

			if ( context.peek() instanceof wddxStruct ) {
				struct = ( wddxStruct ) context.pop();
				struct.put( varName, data );
				context.push( struct );
				varName = null;
			} else if ( context.peek() instanceof wddxArray ) {
				array = ( wddxArray ) context.pop();
				try {
				array.addElement( data );
				} catch (cfmRunTimeException ex) { throw new SAXException (ex); }
				context.push( array );
			} else if ( context.peek() instanceof wddxQueryResult ) {
				resultSet = ( wddxQueryResult ) context.pop();
				resultSet.setCell( rowNum, columnName, data );
				context.push( resultSet );
			}
		} else {
          try{
				if ( current.equals( "string" ) ) {
					result = new cfStringData( data.getString() );
				} else if ( current.equals( "number" ) ) {
					result = new cfNumberData( data.getNumber() );
				} else if ( current.equals( "binary" ) ) {
					result = data;
				} else if ( current.equals( "datetime" ) ) {
					result = dateFromWddx( buf.toString() );
				}
			} catch ( cfmRunTimeException e ) {
				throw new SAXException( e );
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  _date                          Description of Parameter
	 *@return                                Description of the Returned Value
	 *@exception  dataNotSupportedException  Description of Exception
	 *@exception  cfmRunTimeException        Description of Exception
	 */
	private static cfDateData dateFromWddx( String _date ) {
		int firstSeparator = _date.indexOf( "-" );
		int secondSeparator = _date.indexOf( "-", firstSeparator + 1 );
		int thirdSeparator = _date.indexOf( ":" );
		int fourthSeparator = _date.indexOf( ":", thirdSeparator + 1 );
		int fifthSeparator = _date.length();
		int T = _date.indexOf( "T" );

		int offsetFactor = 1;
		if ( _date.indexOf( "+" ) != -1 ) {
			fifthSeparator = _date.indexOf( "+" );
		} else if ( _date.lastIndexOf( "-" ) != secondSeparator ) {
			fifthSeparator = _date.lastIndexOf( "-" );
			offsetFactor = -1;
		}

		int year = Integer.parseInt( _date.substring( 0, firstSeparator ) );
		int month = Integer.parseInt( _date.substring( firstSeparator + 1, secondSeparator ) );
		int day = Integer.parseInt( _date.substring( secondSeparator + 1, T ) );
		int hours = Integer.parseInt( _date.substring( T + 1, thirdSeparator ) );
		int minutes = Integer.parseInt( _date.substring( thirdSeparator + 1, fourthSeparator ) );
		int seconds = Integer.parseInt( _date.substring( fourthSeparator + 1, fifthSeparator ) );

		GregorianCalendar gc = new GregorianCalendar( year, month - 1, day, hours, minutes, seconds );

		if ( fifthSeparator != _date.length() ){
			String [] options = _date.substring( fifthSeparator+1 ).split(":");
			int offsetHours = Integer.parseInt( options[0] );
			int offsetMins = Integer.parseInt( options[1] );
			
			int totalOffset = ( (offsetHours * 60) + offsetMins ) * 60 * 1000 * offsetFactor;
	
			// Get current timezone offset
			int currentTimezoneOffset = TimeZone.getDefault().getRawOffset();
	
			// Calculate net timezone offset
			int netTimezoneOffset = currentTimezoneOffset - totalOffset;
			gc.add( Calendar.MILLISECOND, netTimezoneOffset );
		}
		return new cfDateData( gc.getTime() );
	}
	
  public void ignorableWhitespace( char[] arg0, int arg1, int arg2 ) {
    //super.ignorableWhitespace(arg0, arg1, arg2);
  }
}
