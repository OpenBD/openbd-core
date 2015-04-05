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
 *  
 *  $Id: cfLOOP.java 2227 2012-08-05 15:06:16Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfLoopNumberData;
import com.naryx.tagfusion.cfm.engine.cfLoopStringData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.cfm.xml.cfXmlDataArray;

/**
 * This class provides processing for the CFLOOP tag
 */

public class cfLOOP extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	int loopType = 0;
	CFExpression conditionExpr;

	private final static int INDEX_TYPE = 1;
	private final static int QUERY_TYPE = 2;
	private final static int CONDITION = 3;
	private final static int LIST = 4;
	private final static int COLLECTION = 5;
	private final static int FILE = 6;
	private final static int ARRAY = 7;

  public java.util.Map getInfo(){
  	return createInfo("control", "Provides a number of mechanisms to loop over data structures.");
  }
  
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo("[Array] FROM", 	"The start index in the array to start the loop from",  "", true ),
   			createAttInfo("[Array] TO", 		"The end index in the array to end the loop at",  "", true ),
   			createAttInfo("[Array] INDEX",	"The name of the variable that will contain the current position",  "", true ),
  			createAttInfo("[Array] STEP",		"The number to step through",  "1", false ),
  			
  			createAttInfo("[List] LIST",				"Defined if wishing to loop over a list",  "", true ),
  			createAttInfo("[List] DELIMITERS",	"The list of delimiters of this list",  ",", true ),
   			createAttInfo("[List] INDEX",				"The name of the variable that will contain the current element in the list",  "", true ),
   			
  			createAttInfo("[Query] QUERY",			"The name of the Query to loop over",  "", true ),
  			createAttInfo("[Query] STARTROW",		"The starting row of the query",  ",", true ),
   			createAttInfo("[Query] ENDROW",			"The end row of the query",  "", true ),
   			
   			createAttInfo("[Condition] CONDITION",		"The CONDITION that must be evaluated to TRUE to continue the loop",  "", true ),

   			createAttInfo("[Collection] COLLECTION",	"The name of the collection/structure that will looped over",  "", true ),
   			createAttInfo("[Collection] ITEM",				"The name of the key of the collection on each iteration",  "", true ),
   			
   			createAttInfo("[Array Obj] ARRAY",				"The array object to iterate over",  "", true ),
   			createAttInfo("[Array Obj] INDEX",				"The element (or index) of the array (if INDEX and ITEM are specified then the INDEX is the number of the array, and the ITEM is the element in the array)",  "", true ),
   			createAttInfo("[Array Obj] ITEM",					"The element of the array",  "", true ),
				
   			createAttInfo("[File] FILE",				"The path to the file that will iterated over",  "", true ),
   			createAttInfo("[File] INDEX",				"The name of the variable that will contain the characters from the file",  "", true ),
   			createAttInfo("[File] CHARACTERS",	"The number of characters that will be read for each iteration.  The default is to read the file line-by-line",  "", false ),
   			createAttInfo("[File] CHARSET",			"The character set to use for reading the file. Defaults to that of the system",  "", false )
   			
  	};
  }
  	
	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);

		// Determine the type and ensure all the parameters exist
		if (containsAttribute("FROM")) {
			loopType = INDEX_TYPE;
			if (!containsAttribute("TO"))
				throw newBadFileException("Invalid attribute","Must contain the TO field");
			else if (!containsAttribute("INDEX"))
				throw newBadFileException("Invalid attribute","Must contain the INDEX field");

			if (!containsAttribute("STEP"))
				defaultAttribute("STEP", 1);

		} else if (containsAttribute("CONDITION")) {
			loopType = CONDITION;
			if (properties.size() != 1) {
				throw newBadFileException("Invalid attribute","Must contain valid CONDITION");
			}
			// We need to remove any escaped quotes but to do that we first need to know what the 
			// surrounding quote is (i.e. single or double)
			String condition = getConstant("CONDITION", false);
			if (condition.length() > 1) {
				int lastCharIndex = condition.length() - 1;
				if (condition.charAt(0) == '\'' && condition.charAt(lastCharIndex) == '\'') {	
					condition = string.replaceString(condition.substring(1,	lastCharIndex), "''", "'");
				} else if (condition.charAt(0) == '"' && condition.charAt(lastCharIndex) == '"') {
					condition = string.replaceString(condition.substring(1,	lastCharIndex), "\"\"", "\"");
				}
			}
			
			conditionExpr = CFExpression.getCFExpression( condition );

		} else if (containsAttribute("QUERY")){
			loopType = QUERY_TYPE;
		} else if (containsAttribute("LIST")) {
			loopType = LIST;
			if (!containsAttribute("INDEX"))
				throw newBadFileException("Invalid attribute","Must contain the INDEX field");

			if (!containsAttribute("DELIMITERS"))
				defaultAttribute("DELIMITERS", ",");

		} else if (containsAttribute("COLLECTION")) {
			loopType = COLLECTION;

			if (!containsAttribute("ITEM"))
				throw newBadFileException("Invalid attribute","Must contain the ITEM field");

		} else if ( containsAttribute("FILE") ){
			loopType	= FILE;
			
			if (!containsAttribute("INDEX"))
				throw newBadFileException("Invalid attribute","Must contain the INDEX field");

		} else if ( containsAttribute("ARRAY") ){
			loopType	= ARRAY;
			
			if (!containsAttribute("INDEX"))
				throw newBadFileException("Invalid attribute","Must contain the INDEX field");

		} else
			throw newBadFileException("Invalid attribute","Must contain at least one; QUERY/FROM/CONDITION/LIST");
	}

	public String getEndMarker() {
		return "</CFLOOP>";
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {

		if (loopType == INDEX_TYPE)
			return renderIndex(_Session);
		else if (loopType == CONDITION)
			return renderCondition(_Session);
		else if (loopType == QUERY_TYPE)
			return renderQuery(_Session);
		else if (loopType == LIST)
			return renderList(_Session);
		else if (loopType == COLLECTION)
			return renderCollection(_Session);
		else if (loopType == FILE )
			return renderFile(_Session);
		else if (loopType == ARRAY )
			return renderArray(_Session);
			
		throw newRunTimeException( "Invalid CFLOOP type: " + loopType );
	}

	private cfTagReturnType renderCollection( cfSession _Session ) throws cfmRunTimeException {
		cfStructData structData = null;
		try {
			structData = (cfStructData)getDynamic( _Session, "COLLECTION" );
		} catch ( ClassCastException e ) {
			// fall through to next statement for error handling
		}

		if ( structData == null ) {
			throw newRunTimeException( "Invalid COLLECTION: " + getConstant( "COLLECTION" ) );
		}

		String item = getDynamic( _Session, "ITEM" ).getString();
		Object[] sKeys = null;
		
		if (structData instanceof cfXmlData){
			cfXmlDataArray arr = (cfXmlDataArray)((cfXmlData)structData).getData("XmlChildren");
			if (arr != null){
				String[] strArr = new String[arr.size()];
				for ( int i = 0; i < arr.size(); i ++ )	{	  
					strArr[i] = ((cfXmlData)arr.getElement(i+1)).getName();
				}
				sKeys = strArr;
			}else{
				cfXmlData root = (cfXmlData)((cfXmlData)structData).getData("XmlRoot");
				if (root != null){
					sKeys = new String[] {root.getName()};
				}else{
					sKeys = new String[0];
				}
			}
		}else{
			sKeys = structData.keys();
		}
		
		cfLoopStringData loopIndex = new cfLoopStringData( "" );
		_Session.setData( item, loopIndex );
		
		for ( int i = 0; i < sKeys.length; i++ ) {
			loopIndex.setString( (String)sKeys[ i ] );
			if ( !loopIndex.isValidLoopIndex() ) {
				_Session.setData( item, loopIndex );
			}
			cfTagReturnType rt = super.render( _Session );
			if ( rt.isContinue() ) {
				continue;
			} else if ( rt.isBreak() ) {
				break;
			} else if ( !rt.isNormal() ) {
				return rt;
			}
		}
		
		return cfTagReturnType.NORMAL;
	}

	private cfTagReturnType renderList( cfSession _Session ) throws cfmRunTimeException {
		String DELIMITERS = getDynamic( _Session, "DELIMITERS" ).getString();
		String INDEX = getDynamic( _Session, "INDEX" ).getString();

		List<String> valist = string.split( getDynamic( _Session, "LIST" ).getString(), DELIMITERS );
		
		cfLoopStringData loopIndex = new cfLoopStringData( "" );
		_Session.setData( INDEX, loopIndex );
		
		for ( int i = 0; i < valist.size(); i++ ) {
			loopIndex.setString( (String)valist.get( i ) );
			if ( !loopIndex.isValidLoopIndex() ) {
				_Session.setData( INDEX, loopIndex );
			}
			cfTagReturnType rt = super.render( _Session );
			if ( rt.isContinue() ) {
				continue;
			} else if ( rt.isBreak() ) {
				break;
			} else if ( !rt.isNormal() ) {
				return rt;
			}
		}
		
		return cfTagReturnType.NORMAL;
	}
	
	private double getDouble( cfSession _Session, String _attr ) throws cfmRunTimeException{
		cfData attrVal = getDynamic(_Session, _attr ); 
		try{
			return attrVal.getDouble();
		}catch( cfmRunTimeException e ){
			try{
				// attempt to convert it to a date first
				return attrVal.getDateData().getDouble();
			}catch( cfmRunTimeException e2 ){
				throw e;
			}
		}
	}

	private cfTagReturnType renderIndex( cfSession _Session ) throws cfmRunTimeException {
		double iTo = getDouble( _Session, "TO" ); 
		double iFrom = getDouble( _Session, "FROM" ); 
		double iStep = getDynamic(_Session, "STEP").getDouble();
		String INDEX = getDynamic(_Session, "INDEX").getString();

		if (iStep > 0 && iFrom > iTo)
			return cfTagReturnType.NORMAL;
		else if (iStep < 0 && iFrom < iTo)
			return cfTagReturnType.NORMAL;
		else if (iStep == 0)
			return cfTagReturnType.NORMAL;
		
		cfLoopNumberData loopIndex = new cfLoopNumberData( iFrom );
		_Session.setData( INDEX, loopIndex );
		
		// if going upwards, or _iFrom and iTo are equals and iStep is not negative
		if (iFrom < iTo || (iFrom == iTo && iStep > 0)) {
			for (; iFrom <= iTo; iFrom += iStep) {
				loopIndex.set( iFrom );
				if ( !loopIndex.isValidLoopIndex() ) {
					_Session.setData( INDEX, loopIndex );
				}
				cfTagReturnType rt = super.render( _Session );
				if ( rt.isContinue() ) {
					continue;
				} else if ( rt.isBreak() ) {
					break;
				} else if ( !rt.isNormal() ) {
					return rt;
				}
			}
			// else if going downwards 
		} else {
			for (; iFrom >= iTo; iFrom += iStep) {
				loopIndex.set( iFrom );
				if ( !loopIndex.isValidLoopIndex() ) {
					_Session.setData( INDEX, loopIndex );
				}
				cfTagReturnType rt = super.render( _Session );
				if ( rt.isContinue() ) {
					continue;
				} else if ( rt.isBreak() ) {
					break;
				} else if ( !rt.isNormal() ) {
					return rt;
				}
			}
		}

		// for consistency with CF5/MX, leave index one step past end
		loopIndex.set( iFrom );
		if ( !loopIndex.isValidLoopIndex() ) {
			_Session.setData( INDEX, loopIndex );
		}
		
		return cfTagReturnType.NORMAL;
	}

	private cfTagReturnType renderCondition(cfSession _Session) throws cfmRunTimeException {
		if ( conditionExpr == null ) {
			throw newRunTimeException( "Error evaluating CONDITION" );
		}
		
		while ( runTime.runExpression( _Session, conditionExpr ).getBoolean() ) {
			cfTagReturnType rt = super.render( _Session );
			if ( rt.isContinue() ) {
				continue;
			} else if ( rt.isBreak() ) {
				break;
			} else if ( !rt.isNormal() ) {
				return rt;
			}
		}
		
		return cfTagReturnType.NORMAL;
	}

	private cfTagReturnType renderQuery(cfSession _Session) throws cfmRunTimeException {

		String QUERY = getDynamic(_Session, "QUERY").getString();
		cfData queryDataTmp = runTime.runExpression(_Session, QUERY); // used til we're sure of it's type
		cfQueryResultData queryData = null;

		//--[ ------------------------------
		//--[ Begin the output
		if (queryDataTmp != null) {

			if (queryDataTmp instanceof cfQueryResultData) {
				queryData = (cfQueryResultData) queryDataTmp;
			} else {
				throw newRunTimeException("The specified QUERY "
						+ QUERY + " is not a valid query type.");
			}

			int startRow = 1, endRow = -1;

			if (containsAttribute("STARTROW"))
				startRow = getDynamic(_Session, "STARTROW").getInt() + 1;

			if (containsAttribute("ENDROW"))
				endRow = getDynamic(_Session, "ENDROW").getInt() + 1;

			int rowCount = 1;

			int currentRow = queryData.getCurrentRow();
			queryData.reset();

			_Session.pushQuery(queryData);

			try {
				while (queryData.nextRow()) {
					rowCount++;

					//--[ Get ourselves up to the start
					if (rowCount < startRow)
						continue;

					//--[ Send the data out
					queryData.absolute(rowCount - 1);
					cfTagReturnType rt = super.render( _Session );
					if ( rt.isContinue() ) {
						continue;
					} else if ( rt.isBreak() ) {
						break;
					} else if ( !rt.isNormal() ) {
						return rt;
					}

					//--[ Determine if the maximum rows have been reached
					if (endRow != -1 && rowCount == endRow)
						break;
				}
			} finally {
				queryData.finishQuery();
				queryData.setCurrentRow(currentRow);
				_Session.popQuery();
			}
		} else {
			throw newRunTimeException("The specified QUERY " + QUERY + " does not exist.");
		}
		
		return cfTagReturnType.NORMAL;
	}
	
	
	private cfTagReturnType renderFile( cfSession _Session ) throws cfmRunTimeException {
		int characters = -1;
		String charset	= cfEngine.getDefaultCharset();
		if ( containsAttribute("CHARSET") )
			charset	= getDynamic( _Session, "CHARSET" ).getString();
		
		if ( containsAttribute("CHARACTERS") )
			characters = getDynamic( _Session, "CHARACTERS" ).getInt();
		
		String INDEX 				= getDynamic( _Session, "INDEX" ).getString();
		String outFileName	= getDynamic( _Session, "FILE" ).getString();

		//Try to open up the file
		InputStream			is = null;
		BufferedReader	bufferedReader = null;
		try{
			File FILE				= new File( outFileName );
			is							= new FileInputStream( FILE );
			bufferedReader	= new BufferedReader( new InputStreamReader( is, charset ) );	
		}catch(Exception e){
			throw newRunTimeException("The specified FILE, " + outFileName + " has a problem (" + e.getMessage() + ")" );
		}
		
		
		try{
			cfLoopStringData loopIndex = new cfLoopStringData( "" );
			if ( characters == -1 ){
				
				/* Read the file line-by-line */
				String inLine;
				while ( (inLine=bufferedReader.readLine()) != null ){
					
					loopIndex.setString( inLine );
					_Session.setData( INDEX, loopIndex );
					
					cfTagReturnType rt = super.render( _Session );
					if ( rt.isContinue() ) {
						continue;
					} else if ( rt.isBreak() ) {
						break;
					} else if ( !rt.isNormal() ) {
						return rt;
					}
				}
				
			}else{
				
				/* Read the file, characters at a time */
				char[]	inBuffer = new char[ characters ];
				int noRead;
				while ( (noRead = bufferedReader.read(inBuffer, 0, characters )) != -1 ){
					
					loopIndex.setString( new String( inBuffer, 0, noRead ) );
					_Session.setData( INDEX, loopIndex );
					
					cfTagReturnType rt = super.render( _Session );
					if ( rt.isContinue() ) {
						continue;
					} else if ( rt.isBreak() ) {
						break;
					} else if ( !rt.isNormal() ) {
						return rt;
					}
				}
			}
		
		} catch (IOException ioe){
			throw newRunTimeException("The specified FILE, " + outFileName + " developed a problem (" + ioe.getMessage() + ")" );
		}finally{
			try{ bufferedReader.close(); } catch(IOException ioeIgnoreClose){}
			try{ is.close(); } catch(IOException ioeIgnoreClose){}
		}
		
		return cfTagReturnType.NORMAL;
	}

	
	
	/**
	 * Special feature here; if ITEM *and* INDEX are specified then we it will be the right way around
	 * 
	 * @param _Session
	 * @return
	 * @throws cfmRunTimeException
	 */
	private cfTagReturnType renderArray( cfSession _Session ) throws cfmRunTimeException {
		
		String INDEX 	= getDynamic( _Session, "INDEX" ).getString();
		String ITEM		= null;
		cfLoopNumberData loopIndex = null;
		
		if ( containsAttribute("ITEM") ){
			ITEM	= getDynamic( _Session, "ITEM" ).getString();
			
			loopIndex = new cfLoopNumberData( 1 );
			_Session.setData( INDEX, loopIndex );
		}

		cfData dataObj 	= getDynamic( _Session, "ARRAY" );
		
		if ( dataObj instanceof cfArrayData ) {
			
			cfArrayData	arrayObj = (cfArrayData)dataObj;
			
			for ( int x=0; x < arrayObj.size(); x++ ){
				
				if ( ITEM == null )
					_Session.setData( INDEX, arrayObj.getElement(x+1) );
				else{
					_Session.setData( ITEM, arrayObj.getElement(x+1) );
					loopIndex.set(x+1);
				}
				
				cfTagReturnType rt = super.render( _Session );
				if ( rt.isContinue() ) {
					continue;
				} else if ( rt.isBreak() ) {
					break;
				} else if ( !rt.isNormal() ) {
					return rt;
				}
	
			}
		} else if ( dataObj instanceof cfBinaryData ) {
			
			cfBinaryData arrayObj = (cfBinaryData)dataObj;
			
			for ( int x=0; x < arrayObj.getLength(); x++ ){
				
				if ( ITEM == null )
					_Session.setData( INDEX, new cfNumberData( arrayObj.getByteAt(x) ) );
				else{
					_Session.setData( ITEM, new cfNumberData( arrayObj.getByteAt(x) ) );
					loopIndex.set(x+1);
				}

				cfTagReturnType rt = super.render( _Session );
				if ( rt.isContinue() ) {
					continue;
				} else if ( rt.isBreak() ) {
					break;
				} else if ( !rt.isNormal() ) {
					return rt;
				}
	
			}
			
		} else {
			throw newRunTimeException("The specified ARRAY is not a CF Array or CF Binary data type" );
		}
		
		return cfTagReturnType.NORMAL;
	}
}
