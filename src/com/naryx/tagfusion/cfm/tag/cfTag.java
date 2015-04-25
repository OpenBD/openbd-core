/*
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 *  $Id: cfTag.java 2486 2015-01-22 03:22:37Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.nary.util.CaseSensitiveMap;
import com.nary.util.EmptyFastMap;
import com.nary.util.FastMap;
import com.nary.util.charVector;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmAbortException;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmExitException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.CFMLCache;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.expression.function.functionBase;

/**
 * This is the base class for all tags in the system.
 */

public class cfTag implements java.io.Serializable {
  static final long serialVersionUID = 1;
  static final CaseSensitiveMap<String, String> emptyProperties = new EmptyFastMap<String, String>();
  
  
  protected CaseSensitiveMap<String, String> properties;

  public cfTag					parentTag;          	// The tag that this tag belongs in
  private cfFile				parentFile;						// The cfFile instant that holds this tag
  public List<cfTag>		tagList;            	// List of tags this page has; only available during parse
  public List<Integer>	commentList;        	// List of the line numbers for comments this page has
  protected char[]			tagContents = null;  	// Buffer contents
  private boolean				flushable = true;			// True=we can send this tag out when we have data
  protected String			tagName;

  public int posLine=1, posColumn=1;        	// Position of this tag
  public int posEndLine=0, posEndColumn=0;   	// Position of this end tag; 0 if no end tag 

  protected char[][]				tagBody;
  protected cfTag[]					childTagList;
  protected CFExpression[]	expressionList;
  protected byte[]					controlList;
  protected int[]     			expressionPos;

  
  // don't use 0 or negative numbers for these constants
  private static final char	CHAR_POUND = '#';
  public static final byte COMMENT_MARKER = 4;
  public static final byte TAG_MARKER = 3;
  public static final byte EXP_MARKER = 1;
  public static final byte CHR_MARKER = 2;
  
  public static final char[] emptyArray 							= new char[0];
  public static final char[][] emptyTagBody						= new char[0][];
  public static final cfTag[]	emptyTagList						= new cfTag[0];
  public static final CFExpression[] emptyExpression 	= new CFExpression[0];
  
	public cfTag(cfFile _parentFile) {
		this();
		parentFile = _parentFile;
	}

	public cfTag() {
		parentTag 		= null;
		parentFile 		= null;
		tagList 			= new ArrayList<cfTag>(1);
		commentList 	= new ArrayList<Integer>(1);
		properties 		= new FastMap<String, String>( FastMap.CASE_INSENSITIVE );
		tagContents 	= emptyArray;
	}

  /**
   * Sets value for parent tag.
   */
  public void setParentTag( cfTag _parentTag ){
		parentTag	= _parentTag;
  }

  public void setParentFile( cfFile _parentFile ) {
  	parentFile = _parentFile;
  }
  
  public void setProperties(CaseSensitiveMap<String, String> _properties){
  	properties = _properties;
  }

	public cfFile getFile(){
		// Returns the cfFile inwhich this tag belongs
		if ( parentFile != null ) {
			return parentFile;
		}
		if ( parentTag != null ) {
			return parentTag.getFile();
		}
		return null;

	}

  /**
   * Return the end tag marker.
   */
  public String getEndMarker(){
  	return null;
  }

  /**
   * This method allows you set default attribute values for the specified tag.
   *
   * @param _tag - the tag that store defualt value for.
   */
  protected void defaultParameters( String _tag ) throws cfmBadFileException {}

  protected void tagLoadingComplete() throws cfmBadFileException {}

  protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
  	cfData attributeCollection = getDynamic(_Session, "ATTRIBUTECOLLECTION");
    if (attributeCollection != null && attributeCollection.getDataType() == cfData.CFSTRUCTDATA) {
    	return (cfStructData)attributeCollection;
    }else
    	return null;
  }
  
	// return the tag attributes in a cfStructData
  protected cfStructData getMetaData( boolean _preEval ) throws cfmBadFileException {
  	cfStructData metaData = new cfStructData();
  	
  	if ( !properties.isEmpty() ){
	  	Iterator<String> iter = properties.keySet().iterator();
	  	while ( iter.hasNext() ) {
	  		String key = iter.next();
	  		metaData.setData( key, _preEval ? getEvaluatedConstant( key ) : new cfStringData( getConstant(key) ) );
	  	}
  	}
  	return metaData;
  }


	/**
	 * Determines if this tag can be flushable. This is used to determine whether or not we can 
	 * send data out in chunks or the more inefficient route of one block.
	 * 
	 * @param _flushable
	 *          - the boolean value, true if a tag is flushable.
	 */
	protected void setFlushable(boolean _flushable) {
		flushable = _flushable;
	}

  /**
   * Returns true if a tag can be flushable, else return false.
   */
  protected boolean flushable(){
  	return flushable;
  }

  /**
   * Returns true if all tags in the tag list are flushable, else return false.
   */
  public boolean isFlushable(){
		return false;
  }

  /**
   * Returns true if the tag contains embedded pound signs ( the default is false,
   * subclasses should override to return true when needed)
   */
  public boolean doesTagHaveEmbeddedPoundSigns() {
	return false;
  }

  public int getExpressionPosition( int _i ){
  	// check expressionPos isn't null. It could be if an older version of cfTag was deserialized
  	if ( expressionPos == null || _i < 0 || _i >= expressionPos.length )
  		return 0;
  	return this.expressionPos[_i];
  }

  //-------------------------------------------------------------------------------

  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
	  return coreRender( _Session );
  }

  public cfTagReturnType coreRender( cfSession _Session ) throws cfmRunTimeException {
		int t = 0, e = 0, s = 0;
		for ( int x = 0; x < controlList.length; x++ ) {
			if ( _Session.isStopped() ) {
				_Session.abortPageProcessing();
			}else if ( controlList[ x ] == CHR_MARKER ) {
				_Session.write( tagBody[ s++ ] );
			} else if ( controlList[ x ] == TAG_MARKER ) {
				_Session.pushTag( childTagList[ t ] );
				cfTagReturnType rt = childTagList[ t++ ].render( _Session );
				_Session.popTag();
				if ( !rt.isNormal() ) {
					return rt;
				}
			} else if ( controlList[ x ] == EXP_MARKER ) {
				_Session.setActiveExpression( e );
				renderExpression( _Session, expressionList[ e++ ] );
			}
		}
		return cfTagReturnType.NORMAL;
	}

	protected void renderExpression( cfSession _Session, CFExpression expr )throws cfmRunTimeException, dataNotSupportedException	{
		boolean oldEscapeSingleQuotes = _Session.isEscapeSingleQuotes();
		// save the escapeSingleQuotes flag to restore after runExpression;
		// the PreserveSingleQuotes method sets this flag to false if executed
		_Session.debugger.runExpression( expr );
		
		
		_Session.getCFContext().setLineCol( 0,0 );
		
		
		cfData token = runTime.runExpression( _Session, expr );

		
		if ( ( token != null ) && ( token.getDataType() != cfData.UNKNOWN ) ) {
			if ( _Session.isEscapeSingleQuotes() && ( token.getDataType() == cfData.CFSTRINGDATA ) 
					&& !( (cfStringData) token).isDateConvertible( false ) 
					&& ( expr.getType() != CFExpression.FUNCTION || expr.isEscapeSingleQuotes() ) ) {
				_Session.write( string.replaceString( token.getString(), "'", "''" ) );
			} else {
				_Session.write( token.getString() );
			}
		}
		
		_Session.setEscapeSingleQuotes( oldEscapeSingleQuotes );
	}

	public static final int DEFAULT_OPTIONS				= 0;
	public static final int ESCAPE_SINGLE_QUOTES 		= 1;
	public static final int CF_OUTPUT_ONLY				= 2;
	public static final int HONOR_CF_SETTING			= 4;
	public static final int SUPPRESS_WHITESPACE			= 8;
	public static final int SUPPRESS_OUTPUT_AFTER_ABORT	= 16;

	private static boolean isSet( int options, int mask ) {
		return ( ( options & mask ) == mask );
	}

	public cfTagReturnType renderToString( cfSession _Session ) throws cfmRunTimeException {
  		return renderToString( _Session, cfTag.DEFAULT_OPTIONS );
	}

	public cfTagReturnType renderToString( cfSession _Session, int options ) throws cfmRunTimeException {
		cfSession offlineSession = new cfSession( _Session, isSet( options, ESCAPE_SINGLE_QUOTES ) );
		if ( isSet( options, CF_OUTPUT_ONLY ) ) {
			offlineSession.setProcessingCfOutput( false );
		} else if ( !isSet( options, HONOR_CF_SETTING ) ) {
			offlineSession.clearCfSettings();
		}
		
    offlineSession.setSuppressWhiteSpace( isSet( options, SUPPRESS_WHITESPACE ) );
		CFContext context = _Session.getCFContext();

		try {
			context.setSession( offlineSession );				// have to set the session context so that
			cfTagReturnType rt = coreRender( offlineSession );	// writeOutput writes to the right session
			if ( rt.isExitOrReturn() ) {
				rt.setOutput( offlineSession.getOutputAsString() );
				return rt;
			}else if ( rt.isNormal() ){
				return cfTagReturnType.newNormal( offlineSession.getOutputAsString() );
			}
			return rt;
		} catch ( cfmRunTimeException rte ) {
			//this is part of the fix for NA bug #3282
			rte.setOutput( offlineSession.getOutputAsString() );
			throw rte;
		} catch ( cfmExitException ae ) {
			ae.setOutput( offlineSession.getOutputAsString() );
			throw ae;
		} catch ( cfmAbortException ae ) {
			String output = offlineSession.getOutputAsString();
 			ae.setOutput( output );
 			if ( !isSet( options, SUPPRESS_OUTPUT_AFTER_ABORT ) && ae.flushOutput() ) {
 				_Session.clearCfSettings();
 				_Session.write( output );
 			}
			throw ae;
		} finally {
			_Session.setActiveExpression( offlineSession.getActiveExpression() );
			_Session.setBufferReset( offlineSession.hasBufferReset() );
			_Session.setLocale( offlineSession.getLocale() );
			context.setSession( _Session ); // reset the session back to the original
		}
	}

  //-------------------------------------------------------------------------------

	public int getNoOfAttributes(){
		return properties.size();
	}

	public CaseSensitiveMap<String, String> getProperties(){
		return properties;
	}
	
  public String getConstant( String _attribute ){
    return getConstant( _attribute, true );
  }

  public boolean getConstantAsBoolean( String _attribute ){
  	String t = getConstant( _attribute, true );

  	if ( t.equalsIgnoreCase( "YES" ) ||
			   t.equalsIgnoreCase( "TRUE" ) ||
			   t.equals( "1" ) ){
  		return true;
  	}else
  		return false;
  }

  public cfData getEvaluatedConstant( String _attribute ) throws cfmBadFileException{
    String constant = getConstant( _attribute, false );
    if ( constant != null ){
    	try{
    		return runTime.runExpression( constant );
    	}catch( cfmRunTimeException e ){
    		throw newBadFileException( "Error evaluating attribute [" + _attribute + "]", e.getMessage() );
    	}
    }
    return null; // keep compiler happy
  }

  public String getConstant( String _attribute, boolean _stripQuotes ){
 		if ( properties.containsKey( _attribute ) ){
 			String rhs = properties.get( _attribute );
      if ( _stripQuotes && rhs.length() >= 2 && ((rhs.charAt(0) == '\"' && rhs.charAt(rhs.length()-1) == '\"') || (rhs.charAt(0) == '\'' && rhs.charAt(rhs.length()-1) == '\'')) )
			  return rhs.substring( 1, rhs.length()-1 );
      else
        return rhs;
    }else
 			return null;
  }

  // checks that the attribute exists, throws an exception if not
  public void requiredAttribute( String _attribute ) throws cfmBadFileException {
	  if ( !properties.containsKey( _attribute ) ) {
		  throw newBadFileException( "Missing Attribute", "The " + _attribute.toUpperCase() + " attribute must be specified" );
	  }
  }

  // checks that the attribute contains a constant value, throws an exception if not
  public void constantAttribute( String _attribute ) throws cfmBadFileException {
	  if ( properties.containsKey( _attribute ) && getConstant( _attribute, true ).indexOf( '#' ) >= 0 ) {
		  throw newBadFileException( "Invalid Expression", "The " + _attribute.toUpperCase() + " attribute must contain a constant value" );
	  }
  }

  // checks that the attribute contains a constant boolean value, throws an exception is not
  public void booleanAttribute( String _attribute ) throws cfmBadFileException {
	  if ( properties.containsKey( _attribute ) ) {
		  String attrValue = getConstant( _attribute, true );
		  if ( !attrValue.equalsIgnoreCase( "YES" ) &&
			   !attrValue.equalsIgnoreCase( "TRUE" ) &&
			   !attrValue.equalsIgnoreCase( "NO" ) &&
			   !attrValue.equalsIgnoreCase( "FALSE" ) &&
			   !attrValue.equals( "1" ) &&
			   !attrValue.equals( "0" ) )
		  {
			  throw newBadFileException( "Invalid Attribute", "Cannot convert the " + _attribute.toUpperCase() + " attribute to a boolean value" );
		  }
	  }
  }

  
  /**
   * Designed specifically for tags that are merely wrappers to the function versions of themselves.
   * This function wraps up the tag attributes and creates a functionArgs structure for it to use.
   * The key is that we need to know which params we want
   * 
   * @param session
   * @param params
   * @param attributes
   * @return
   * @throws cfmRunTimeException
   */
	public cfArgStructData getFunctionArgsFromAttributes( cfSession session, List<String> params, cfStructData attributes ) throws cfmRunTimeException {
		cfArgStructData	argData	= new cfArgStructData(true);
		
		Iterator<String>	it = params.iterator();
		while ( it.hasNext() ){
  		String	key = it.next();
  		cfData	val = getDynamic( attributes, session, key );
  		if ( val != null )
  			argData.setData(key.toLowerCase(), val);
  	}
  	
  	return argData;
	}

  
  public String getDynamicAsString( cfStructData attributes, cfSession _Session, String _attribute ) throws cfmRunTimeException {
		if ( attributes != null && attributes.containsKey( _attribute )){
			return attributes.getData(_attribute).getString();
		}else
			return getDynamic(_Session,_attribute).getString();
  }

  
  public cfData getDynamic( cfStructData attributes, cfSession _Session, String _attribute ) throws cfmRunTimeException {
		if ( attributes != null && attributes.containsKey( _attribute )){
			return attributes.getData(_attribute);
		}else
			return getDynamic(_Session,_attribute);
  }
  
  public cfData getDynamic( cfSession _Session, String _attribute ) throws cfmRunTimeException {
		if ( !properties.containsKey( _attribute ) )
			return null;

		String attr = properties.get( _attribute );
		return getDynamicAttribute( _Session, attr );
  }

  public static cfData getDynamicAttribute( cfSession _Session, String attr ) throws cfmRunTimeException {
	  if ( attr.length() == 0 )
	    return null;
	
	  char  firstChar = attr.charAt(0);
	  char  lastChar  = attr.charAt(attr.length() - 1);
	
	  if ( ( firstChar != '"' && lastChar != '"' ) &&
	       ( firstChar != '\'' && lastChar != '\'' ) &&
	       ( firstChar != '#' && lastChar != '#' ) ){
	      attr = "\"" + attr + "\"";
	  }
        cfData data = runTime.runExpression( _Session, attr );
		// duplicate simple values passed as tag attributes (see bug #3073)
      if ( cfData.isSimpleValue( data ) ) {
      	data = data.duplicate();
		}
		return data;
  }

  public void defaultAttribute( String _key, String _value ){
    properties.put( _key, "\"" + _value + "\"" );
  }

  public void defaultAttribute( String _key, int _value ){
    properties.put( _key, Integer.toString( _value ) );
  }

  public boolean containsAttribute( String _key ){
		return properties.containsKey( _key );
  }

  public void removeAttribute( String _key ){
    properties.remove( _key );
  }

	public void removeAttributes(){
		properties.clear();
	}

  public boolean containsAttribute( cfStructData attributes, String _key ){
  	return (attributes != null && attributes.containsKey(_key)) || properties.containsKey( _key );
  }

	
	/**
	 * The vast majority of tags never modify the Attributes once initialised.  However
	 * some do.  Instead of keeping around multiple references to an empty attributes map
	 * that will never change, we point them all to the same one.  However for those that do
	 * need to change it at run time, must ensure they have their own version of it.
	 * 
	 * This method will check to see if they are using the default empty one and then give them
	 * their own.
	 */
	public void unclipAttributes(){
		if ( emptyProperties == properties )
			properties	= new FastMap<String, String>();
	}

	
	/**
	 * Pop the tag stack until this tag is at the top
	 */
	protected void restoreTagStack( cfSession _Session ) {
		while ( _Session.activeTag() != this )
			_Session.popTag();
	}

  //-------------------------------------------------------------------------------

  protected void parseTagHeader( String tag ) throws cfmBadFileException {
		parseTagHeader( tag, false );
	}

  protected void parseTagHeader( String tag, boolean oneParameter ) throws cfmBadFileException {
		try{
			new tagParameters( tag, oneParameter, properties );
  	}catch(Exception E){
			throw new cfmBadFileException( catchDataFactory.invalidTagAttributeException( this, E.getMessage() ) );
  	}
  }

 	//--[ ----------------------------------------------------------
 	//--[ RunTime exception methods
  public cfmRunTimeException newRunTimeException( String _ErrMessage ) {
		return new cfmRunTimeException( catchDataFactory.runtimeException( this, _ErrMessage ) );
  }

  public static cfmRunTimeException newRunTimeException( cfCatchData	catchData ) {
		return new cfmRunTimeException( catchData );
	}

 	//--[ ----------------------------------------------------------
 	//--[ BadFile exception methods
  public cfmBadFileException newBadFileException( String _type, String _ErrMessage ) {
		return new cfmBadFileException( catchDataFactory.invalidAttributeException( this, "runtime.general", new String[]{_type +": " + _ErrMessage} ) );
  }

	protected cfmBadFileException invalidAttributeException( String detail, String values[] ){
		return new cfmBadFileException( catchDataFactory.invalidAttributeException( this, detail, values ) );
	}

	protected cfmBadFileException missingAttributeException( String detail, String values[] ){
		return new cfmBadFileException( catchDataFactory.missingAttributeException( this, detail, values ) );
	}

	protected cfmBadFileException invalidExpressionException( String detail, int _subline ){
		return new cfmBadFileException( catchDataFactory.invalidExpressionException( this, detail, _subline ) );
	}

  public String getTagName(){
	  if ( tagName == null ) {
		  tagName = tagUtils.getLastToken( this.getClass().getName() ).toUpperCase();
	  }
	  return tagName;
  }

  public cfTag[] getTagList(){
  	return childTagList;
  }

  public char[] getStaticBody(){
  	return tagBody[0];
  }

  public void normalise( boolean isFileEncodingSearch ) throws cfmBadFileException {
    normalise( new normaliseTracker(), isFileEncodingSearch );

    if ( !isFileEncodingSearch )
    	tagLoadingComplete();
  }

  private void normaliseStatic(normaliseTracker nTracker) throws cfmBadFileException {
		//--[ Normalise the Vector of tags into an Object Array
		if ( tagList.size() > 0 )
			throw newBadFileException( "Bad Tag", "This tag is not permitted to have any child tags" );

		tagBody					= new char[1][];
		controlList			= new byte[1];
		controlList[0]	= CHR_MARKER;

		for (int x=0; x < tagContents.length; x++)
			tagBody[0] = tagContents;
  }

  private void normalise( normaliseTracker nTracker, boolean isFileEncodingSearch ) throws cfmBadFileException {

  	//--[ Normalise the Vector of tags into an Object Array
  	childTagList	= new cfTag[ tagList.size() ];
  	for( int x=0; x < tagList.size(); x++ )	childTagList[x] = tagList.get(x);

  	//--[ Normalise the tag contents
  	List<Object>	workingList	= new ArrayList<Object>();
  	List<Integer>	expressionPositions = new ArrayList<Integer>();
  	int							currentChildTag = 0;
  	int							currentCommentTag = 0;
		CharArrayWriter	currentBlock = null;

  	int							bracketCount=0,sqBracketCount=0;
  	char						lastImpChar = 0;
  	charVector			endMarkers = new charVector(5);
  	StringBuilder		expression = null;
  	boolean					possibleEscape = false;
  	boolean					inString = false;

  	int							totalExpressions = 0, totalChars = 0;
  	int             lineCount = 0;

  	for (int x=0; x < tagContents.length; x++){
  		if ( tagContents[x] == TAG_MARKER ){
  			if ( currentBlock != null ){
					workingList.add( currentBlock );
					totalChars++;
					currentBlock = null;
  			}

  			//---[ Determine the type of the tag this is.
  			if ( childTagList[currentChildTag] instanceof ContentTypeStaticInterface ){

          childTagList[currentChildTag].normaliseStatic(nTracker);

          //- The tag has now been parsed
          if ( !isFileEncodingSearch )
          	childTagList[currentChildTag].tagLoadingComplete();

        }else{

  				nTracker.startTag( childTagList[currentChildTag] );

					childTagList[currentChildTag].normalise(nTracker, isFileEncodingSearch);

					//- The tag has now been parsed
					if ( !isFileEncodingSearch )
						childTagList[currentChildTag].tagLoadingComplete();

					nTracker.endTag( childTagList[currentChildTag] );

  			}

				// update the lineCount based on this child tag position
  			lineCount = childTagList[currentChildTag].posLine - this.posLine;
				//---[ Add the tag to the list
  			workingList.add( childTagList[currentChildTag] );
  			currentChildTag++;

  		}else if ( tagContents[x] == COMMENT_MARKER ){
				// update the lineCount based on comment lines
  			lineCount = this.commentList.get( currentCommentTag ).intValue() - this.posLine;

  			currentCommentTag++;
  		}else{

  			if ( nTracker.isFiltered() ){

					//--[ We are now looking for #
					char thisChar = tagContents[x];

					//--[ increment lineCount if necessary
					if ( thisChar == '\n' ){
						lineCount++;
					}else if ( thisChar == '\r' ){
						if ( !( x+1 < tagContents.length && tagContents[x+1] == '\n') ){
							lineCount++;
						}// if this is a \r\n sequence only increment on the \n
					}

  				if ( thisChar == CHAR_POUND && bracketCount == 0 && sqBracketCount == 0 && endMarkers.size() == 0 ){
  					if (expression == null){	//-- Start of the expression
							if ( currentBlock != null ){
								workingList.add( currentBlock );
								totalChars++;
								currentBlock = null;
							}

  						expression = new StringBuilder();
  					}else if ( expression.length() == 0 ){

  						//--[ The expression is empty, therefore it was escaped
							currentBlock = new CharArrayWriter();
							currentBlock.write( '#' );
							workingList.add( currentBlock );
							totalChars++;
							currentBlock = null;
							expression = null;

  					}else{

  						//--[ Expression was found, parse to make sure all is well.  Will catch initial bugs
  						try{
								CFMLCache.getExpression( expression.toString() );
  						}catch(Exception E){
								throw newBadFileException( "Bad Expression", "[" + expression.toString() + "]: " + E.getMessage() );
  						}

							workingList.add( expression.toString() );
							totalExpressions++;
							expressionPositions.add( new Integer( lineCount ) );
							expression = null;
  					}
  				}else if ( thisChar == CHAR_POUND && !inString && ( bracketCount != 0 || sqBracketCount != 0 ) && endMarkers.size() == 0 ){
  					throw this.invalidExpressionException( "Bad Expression [#" + expression.toString() + "#]", lineCount );
  				}else if ( expression != null ){

						// update the variables so we know when the expression has finished
						if ( possibleEscape && thisChar == lastImpChar ){ // was an escape char
							inString = true;
							if ( thisChar != CHAR_POUND ){
								endMarkers.add( thisChar );
							}
							possibleEscape = false;
						}else if ( inString ){
							possibleEscape = false;
							if ( thisChar == CHAR_POUND ){
								lastImpChar = CHAR_POUND;
								inString = false;
								possibleEscape = true;
							}else if ( thisChar == endMarkers.getLast() ){
								// assume this is the end of the string but store
								// the char in case next char is the escape char for
								// this single/double quote
								lastImpChar = thisChar;
								inString = false;
								possibleEscape = true;
								endMarkers.removeLast();
							}
							// else, don't do anything
						}else{
							possibleEscape = false;
							switch( thisChar ){
								case CHAR_POUND:
									inString = true;
									break;
								case '(':
									bracketCount++;
									break;
								case ')':
									bracketCount--;
									break;
								case '[':
									sqBracketCount++;
									break;
								case ']':
									sqBracketCount--;
									break;
								case '"':
								case '\'':
									inString = true;
									endMarkers.add( thisChar );
									break;
							}
						}

			  		expression.append( thisChar );

  				}else{
						if ( currentBlock == null ) currentBlock = new CharArrayWriter();
						currentBlock.write( thisChar );
  				}

  			}else{
					if ( currentBlock == null ) currentBlock = new CharArrayWriter();
					currentBlock.write( tagContents[x] );
  			}
  		}
  	}

  	//--[ We are finished with the tagContents
		tagContents = null;

  	//--[ Make sure that if this was filtered and the expression
  	if ( nTracker.isFiltered() && expression != null )
			throw newBadFileException( "Bad Expression", "[" + expression.toString() + "] was not closed properly" );


  	//--[ At the end of the loop this one will be caught
		if ( currentBlock != null ){	workingList.add( currentBlock ); totalChars++;}

	 	//--[ We now have a Vector of all the elements in a row
		expressionList 	= new CFExpression[ totalExpressions ];
		expressionPos   = new int[ totalExpressions ];
		
		tagBody					= (totalChars==0) ? emptyTagBody : new char[totalChars][];
		
		controlList			= new byte[ workingList.size() ];

		int	e=0, t=0;
  	for ( int x=0; x < controlList.length; x++ ){
  		if ( workingList.get(x) instanceof CharArrayWriter ){
				tagBody[t++] 		= ((CharArrayWriter)workingList.get(x)).toCharArray();
				controlList[x]	= CHR_MARKER;
  		}else if ( workingList.get(x) instanceof cfTag ){
				controlList[x]	= TAG_MARKER;
  		}else{
				expressionList[e]	= CFExpression.getCFExpression( (String)workingList.get(x) );
				expressionPos[e] = expressionPositions.get(e).intValue();
        e++;
				controlList[x]	= EXP_MARKER;
  		}
  	}
  	
  	// Clean up the unused vars
  	wrapUnsedVars();
 	}
  
  
  /**
   * Convience method to let us construct a tag from scratch that has an inner tag
   * @param tag
   */
  public void setChildTag( cfTag tag ){
  	childTagList	= new cfTag[]{tag};
  	controlList		= new byte[]{TAG_MARKER};
  	wrapUnsedVars();
  }
  

  protected void	wrapUnsedVars(){
  	// Clean up the variables
		commentList = null;
		tagList			= null;

		// Use the static empty array if there are no child tags
		if ( childTagList != null && childTagList.length == 0 )
			childTagList	= emptyTagList;
		
		if ( expressionList != null && expressionList.length == 0 )
			expressionList = emptyExpression;
		
		// If this attribute scope is blank then remove the current one and point to the static empty one
		if ( properties.size() == 0 )
			properties	= emptyProperties;
  }
  
 	//-------------

 	class normaliseTracker {
		private int cfFilter = 0;

		public void startTag(cfTag inTag){
			if ( inTag.doesTagHaveEmbeddedPoundSigns() ){
				cfFilter += 1;
			}
		}

		public void endTag(cfTag inTag){
			if ( inTag.doesTagHaveEmbeddedPoundSigns() ){
				cfFilter -= 1;
			}
		}

 		public boolean isFiltered(){
 			return (cfFilter > 0);
 		}
 	}


 	//
 	public boolean isSubordinate( String parentTagName, boolean immediateParent ) {
		cfTag parent = this.parentTag;
		if ( parent != null ) {
			if ( immediateParent ) {
				return parent.getTagName().equalsIgnoreCase( parentTagName );
			}
			while ( parent != null ) {
				if ( parent.getTagName().equalsIgnoreCase( parentTagName ) ) {
					return true;
				}
				parent = parent.parentTag;
			}
		}
		return false;
	}

 	public boolean isSubordinate(String parentTagName)	{
 		return isSubordinate(parentTagName, true);
 	}

 	// Some tags have subordinates that must be at the beginning of the body block
 	// before any other tag.  This method allows for the checking of that condition.
 	public boolean areSubordinatesFirst(String subordinateTagName, boolean mustBePresent)	{
 		int idx, length;
 		cfTag[] subordinates = null;
 		boolean lastMatchingTagFound = false;

 		// get list of subordinates
 		subordinates = getTagList();
 		length = subordinates.length;

 		if(length > 0){
 			if(!subordinates[0].getTagName().equalsIgnoreCase(subordinateTagName)){
 				if(mustBePresent){
 					return false;
 				}else{
 					lastMatchingTagFound = true;
 				}
 			}

 			for(idx = 1; idx < length; idx++)	{
 				if(!subordinates[idx].getTagName().equalsIgnoreCase(subordinateTagName)){
 					lastMatchingTagFound = true;
 				}else{
 					if(lastMatchingTagFound){
 						return false;
 					}
 				}
 			}
 		}

 		return true;
 	}

 	// Overload specifying that tags need not be present.
	public boolean areSubordinatesFirst(String subordinateTagName){
		return areSubordinatesFirst(subordinateTagName, false);
	}

	public boolean containsTag(String tagName){
		return containsTag(this, tagName);
	}

	public boolean containsTag(cfTag startTag, String tagName){
		boolean ret = false;
		int idx, length;
		cfTag[] grandChildren = null;
		cfTag child = null;

		length = startTag.childTagList.length;
		for(idx = 0; idx < length; idx++){
			child = startTag.childTagList[idx];
			if(child.getTagName().equalsIgnoreCase(tagName)){
				return true;
			}

			grandChildren = child.getTagList();
			if(grandChildren != null && grandChildren.length > 0)	{
				ret = containsTag(child, tagName);
				if(ret == true){
					return ret;
				}
			}
		}

		return false;
	}

	/**
	 * This is for files that have been loaded from a BDA. This ensures
	 * all the tagLoadedComplete() methods are called to initialise any
	 * values.
	 */
	public void reInitialiseTags() throws cfmBadFileException {
		if ( childTagList != null ) {
			// Go through the child tags first of all
			for ( int i = 0; i < childTagList.length; i++ ) {
				childTagList[ i ].reInitialiseTags();
			}
		}
		tagLoadingComplete();
	}


	/*
	 * MetaData Routines
	 */
	public java.util.Map<String,String> getInfo(){
		return createInfo( "unknown", "no documentation available" );
	}

	public java.util.Map[] getAttInfo(){
		return new java.util.Map[0];
	}

	protected	java.util.Map<String,String> createInfo( String category, String summary ){
		java.util.HashMap<String,String> map = new java.util.HashMap<String,String>();
		map.put("category", category.toLowerCase() );
		map.put("summary", 	summary );
		return map;
	}

	protected	java.util.Map<String,String> createAttInfo( String name, String summary ){
		return createAttInfo( name, summary, "", false );
	}

	protected	java.util.Map<String,String> createAttInfo( String name, String summary, String defaultValue, boolean required ){
		java.util.HashMap<String,String> map = new java.util.HashMap<String,String>();
		map.put("name", 		name.toUpperCase() );
		map.put("summary", 	summary );
		map.put("default", 	defaultValue);
		map.put("required", String.valueOf(required) );
		return map;
	}
	
	public java.util.Map getInfo(functionBase func){
  	java.util.Map	map = func.getInfo();
  	return createInfo( (String)map.get("category"), (String)map.get("summary") );
  }
	
	protected java.util.Map[] makeAttInfoFromFunction(functionBase func, int extraAtts){
		List<String> params  	= func.getFormals();
		String[] paramInfo		= func.getParamInfo();
			
		java.util.Map[]	tagParams	= new java.util.Map[params.size()+extraAtts];
		
		for ( int x=0; x < params.size(); x++ ){
			tagParams[x]	= new HashMap();
			tagParams[x].put( "name", params.get(x).toUpperCase() );
			tagParams[x].put( "summary", paramInfo[x] );
			tagParams[x].put( "default", "" );
			
			if ( x < func.getMin() )
				tagParams[x].put("required", "true" );
			else
				tagParams[x].put("required", "false" );
		}
		return tagParams;
	}
}
