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

package com.naryx.tagfusion.cfm.tag.ext.thread;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

import com.nary.io.FileUtils;
import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfHttpServletResponse;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfThreadData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.parser.CFCall;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.tag.cfFUNCTION;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.util.dummyServletRequest;
import com.naryx.tagfusion.util.dummyServletResponse;

public class cfTHREAD extends cfTag implements Serializable {

	static final long serialVersionUID = 1;
	
	public java.util.Map getInfo(){
		return createInfo("system", "Allows threads to be created and controlled.");
	}
  
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo("NAME", "Provide a name for this thread or the names of threads to perform an action on. This is a required attribute when ACTION = \"JOIN\" or  \"TERMINATE\".", "", false ),
				createAttInfo("ATTRIBUTECOLLECTION", "A structure of variables to be passed in for access during the execution of this thread.", "", false ),
				createAttInfo("OUTPUT", "If true, data will be stored about the processing of this thread such as how long it took and its status for future reference.", "TRUE", false ),
				createAttInfo("ACTION", "The action to perform on the thread(s). Valid options include: \"RUN\", \"JOIN\", \"SLEEP\" and \"TERMINATE\".", "RUN", false ),
				createAttInfo("PRIORITY", "Set this thread execution priority. Valid options include: \"HIGH\", \"NORMAL\" and \"LOW\".", "NORMAL", false ),
				createAttInfo("TIMEOUT", "The time in milliseconds that this thread will wait for other joined threads to finish before proceeding. A value of 0 indicates waiting for all joined threads to finish.", "0", false ),
				createAttInfo("DURATION", "How long should this thread sleep for. This attribute is only required if ACTION = \"SLEEP\".", "", false )
		};

	}

	static HashSet<String> attributes;
	
	static{
		attributes = new HashSet<String>();
		attributes.add( "NAME" );
		attributes.add( "ATTRIBUTECOLLECTION" );
		attributes.add( "OUTPUT" );
		attributes.add( "ACTION" );
		attributes.add( "PRIORITY" );
		attributes.add( "TIMEOUT" );
		attributes.add( "DURATION" );
	}
	
	private static FastMap<String,cfData> DEFAULTS = new FastMap<String,cfData>( FastMap.CASE_INSENSITIVE );

	static{
		DEFAULTS.put( "OUTPUT",  cfBooleanData.TRUE );
		DEFAULTS.put( "ACTION",  new cfStringData( "run" ) );
		DEFAULTS.put( "PRIORITY",  new cfStringData("normal") );
		DEFAULTS.put( "TIMEOUT",  new cfNumberData(0) );
	}
	
	public String getEndMarker() {
		return "</CFTHREAD>"; 
	}

	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		// Note: we do not default attributes in this tag due to the ATTRIBUTECOLLECTION tag
		parseTagHeader( _tag );
	}

	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		cfStructData attribs = getAttributeCollection( _Session );
		String action = getAttribute( _Session, attribs, "ACTION" ).getString();
		
		if ( action.equalsIgnoreCase( "RUN" ) ){
			return renderRun( _Session, attribs );
		}else if ( action.equalsIgnoreCase( "SLEEP" ) ){
			return renderSleep( _Session, attribs );
		}else if ( action.equalsIgnoreCase( "JOIN" ) ){
			return renderJoin( _Session, attribs );
		}else if ( action.equalsIgnoreCase( "TERMINATE" ) ){
			return renderTerminate( _Session, attribs );
		}else{
			throw newRunTimeException( "Invalid ACTION attribute value: " + action + ". Valid values include \"JOIN\", \"RUN\", \"SLEEP\" and \"TERMINATE\"" );
		}
	}

	/* the following 2 methods facilitate getting the tag attributes whether passed in individually or in the attributeCollection */
	private cfData getAttribute( cfSession _Session, cfStructData _argCollection, String _key ) throws cfmRunTimeException{
		cfData value = null;
		if ( _argCollection != null ){
			value = _argCollection.getData( _key );
		}else{
			value = getDynamic( _Session, _key );
		}
		
		if ( value == null ){
			value = DEFAULTS.get( _key );
		}
		return value;
	}

	private boolean containsAttribute( cfSession _Session, cfStructData _argCollection, String _key ) throws cfmRunTimeException{
		if ( _argCollection != null ){
			return _argCollection.containsKey( _key );
		}else{
			return containsAttribute( _key );
		}
	}

	private cfStructData getAttributeCollection( cfSession _Session ) throws cfmRunTimeException{
		if ( containsAttribute( "ATTRIBUTECOLLECTION" ) ){
			if ( getNoOfAttributes() > 1 ){
				throw newRunTimeException( "When using ATTRIBUTECOLLECTION, other attributes may not be specified" );
			}
			cfData attribs = getDynamic( _Session, "ATTRIBUTECOLLECTION" );
			if ( attribs.getDataType() != cfData.CFSTRUCTDATA ){
				throw newRunTimeException( "Invalid data type for ATTRIBUTECOLLECTION value. A value of type struct is required." ); 
			}
			return (cfStructData) attribs;
		}
		return null;
	}
	
	private cfTagReturnType renderSleep( cfSession _Session, cfStructData _attribs ) throws cfmRunTimeException{
		if ( !containsAttribute( _Session, _attribs, "DURATION" ) ){
			throw newRunTimeException( "Missing DURATION attribute is required when action=\"SLEEP\"" );
		}
		
		// get duration (no of millisecs to sleep)
		int duration = getAttribute( _Session, _attribs, "DURATION" ).getInt();
		if ( duration <= 0 ){
			throw newRunTimeException( "Invalid DURATION attribute value. The specified value must be greater than 0" );
		}

		try{
			Thread.sleep( duration );
		}catch( InterruptedException ignore ){}
		
		return cfTagReturnType.NORMAL;
	}
	
	/*
	 * Join the named thread(s)
	 */
	private cfTagReturnType renderJoin( cfSession _Session, cfStructData _attribs ) throws cfmRunTimeException{
		
		if ( !containsAttribute( _Session, _attribs, "NAME" ) ) {
			throw newRunTimeException( "Missing NAME attribute is required when action=\"JOIN\"" );
		}
		int timeout = getAttribute( _Session, _attribs, "TIMEOUT" ).getInt();
		if ( timeout < 0 ){
			throw newRunTimeException( "Invalid TIMEOUT attribute value. The specified value must not be negative" );
		}
		
		// multiple thread names can be specified in the NAME attribute
		String name = getAttribute( _Session, _attribs, "NAME" ).getString();
		join( _Session, name, timeout );
		return cfTagReturnType.NORMAL;
	}

	public static void join( cfSession _Session, String _names, int _timeout ) throws cfmRunTimeException{
		String [] threadNames = _names.split( "," );
		
		for ( int i = 0; i < threadNames.length; i++ ){
			cfThreadData thread = getThread( _Session, threadNames[i] );
			try {
				if ( _timeout == 0 ){
					thread.getThread().joinThread();
				}else{
					thread.getThread().joinThread( _timeout );
				}
			}catch ( InterruptedException e ) {}
		}
	}
	
	
	/*
	 * Terminate the named threads
	 */
	private cfTagReturnType renderTerminate( cfSession _Session, cfStructData _attribs ) throws cfmRunTimeException{
		
		if ( !containsAttribute( _Session, _attribs, "NAME" ) ) {
			throw newRunTimeException( "Missing NAME attribute is required when action=\"TERMINATE\"" );
		}
		
		terminate( _Session, getAttribute( _Session, _attribs, "NAME" ).getString() );
		return cfTagReturnType.NORMAL;
	}
	
	public static void terminate( cfSession _Session, String _name ) throws cfmRunTimeException{
		cfThreadData thread = getThread( _Session, _name );
		thread.getThread().stopThread();
	}

	/*
	 * Create a new thread
	 */
	private cfTagReturnType renderRun( cfSession _Session, cfStructData _attribs ) throws cfmRunTimeException{
		String name = null;

		if ( containsAttribute( _Session, _attribs, "NAME" ) ) {
			name = getAttribute( _Session, _attribs, "NAME" ).getString();
		
			// check if thread already exists
			cfData existingThread = _Session.getQualifiedData( variableStore.CFTHREAD_SCOPE ).getData( name );
			if ( existingThread instanceof cfThreadData ){
				throw newRunTimeException( "Invalid NAME attribute value. You cannot create multiple threads with the same name in a single request" );
			}
		}
		
		// get priority of thread
		int priority = getPriority( _Session, getAttribute( _Session, _attribs, "PRIORITY" ).getString().toLowerCase() );
    
		cfStructData attributes = packageUpAttributes( _Session, _attribs );
		boolean keepOutput = ( getAttribute( _Session, _attribs, "OUTPUT" ).getBoolean() && ( name != null ) );
		cfSession threadSession = createVirtualSession( _Session, name, attributes, keepOutput );
		cfThreadRunner thread = new cfThreadTagRunner( this, name, threadSession, priority );
		if ( name != null ) { // named threads have associated data
			cfStructData threadScope = _Session.getQualifiedData( variableStore.CFTHREAD_SCOPE );
			threadScope.setData( name, thread.getThreadData() );
			CFCall call = threadSession.enterCFThread( attributes, thread.getThreadData() );
			// copy the attributes to the local scope
			call.putAll( attributes, null );
		}
		thread.start();

		return cfTagReturnType.NORMAL;
	}

	public static int getPriority( cfSession _Session, String _priorityStr ) throws cfmRunTimeException{
		int priority;
		if ( _priorityStr.equals( "normal" ) ){
			priority = Thread.NORM_PRIORITY;
		}else if ( _priorityStr.equals( "high" ) ){
			priority = Thread.MAX_PRIORITY;
		}else if ( _priorityStr.equals( "low" ) ){
			priority = Thread.MIN_PRIORITY;
		}else{ 
			cfCatchData catchData = catchDataFactory.generalException( _Session, cfCatchData.TYPE_APPLICATION, 
					"Invalid PRIORITY attribute value. Valid values include \"HIGH\", \"LOW\" and \"NORMAL\"", "" );
			throw new cfmRunTimeException( catchData );
		}
		
		return priority;
	}
	
	/**
	 * Return the cfthread with the given thread name
	 * 
	 * @throws cfmRunTimeException if the given thread does not exist or the named variable is not a thread 
	 */
	private static cfThreadData getThread( cfSession _Session, String _threadName ) throws cfmRunTimeException{
		cfData data;
		try{
			data = runTime.runExpression( _Session, _threadName );
			if ( data != null && data instanceof cfThreadData ) {
				return (cfThreadData)data;
			}else{
				data = _Session.getQualifiedData( variableStore.CFTHREAD_SCOPE ).getData( _threadName );
				if ( data != null && data instanceof cfThreadData )
					return (cfThreadData)data;

				cfCatchData catchData = catchDataFactory.generalException( _Session, cfCatchData.TYPE_APPLICATION, 
						"The specified THREAD " + _threadName + " is not a valid thread variable", "" );
				throw new cfmRunTimeException( catchData );
			}
		} catch ( cfmRunTimeException e ) {
			
			data = _Session.getQualifiedData( variableStore.CFTHREAD_SCOPE ).getData( _threadName );
			if ( data != null && data instanceof cfThreadData )
				return (cfThreadData)data;

			cfCatchData catchData = catchDataFactory.generalException( _Session, cfCatchData.TYPE_APPLICATION, 
					"The specified THREAD " + _threadName + " does not exist", "" );
			throw new cfmRunTimeException( catchData );
		}
	}
	
	public static cfSession createVirtualSession( cfSession _Session, String name, cfStructData _attribs, boolean _keepOutput ) throws cfmRunTimeException {
		//-- We need to create a dummy session
		cfSession tmpSession = new cfSession( new dummyServletRequest( FileUtils.getRealPath( _Session.REQ, "/" ) ), new dummyServletResponse(), cfEngine.thisServletContext );
		
		tmpSession.setBufferSize( _keepOutput ? cfHttpServletResponse.UNLIMITED_SIZE : 0 );
		
		tmpSession.pushActiveFile( _Session.activeFile() );
		
		//-- set up for Application.cfc and CFERROR error handling
		tmpSession.setErrorHandling( _Session );
		
		// Package up the variables/attributes  [CFTHREAD is much like a customtag in this respect, except they don't get the parents caller scope]
		cfStructData newVariables	= new cfStructData();
		newVariables.setData(variableStore.ATTRIBUTES_SCOPE_NAME, _attribs );
		tmpSession.setQualifiedData( variableStore.VARIABLES_SCOPE, newVariables );

		
		//-- Copy over the reference to the APPLICATION scope if available
		cfApplicationData appScope = _Session.getApplicationData();
		if ( appScope != null ) {
			tmpSession.setQualifiedData( variableStore.APPLICATION_SCOPE, appScope );
		}
		cfStructData sessionScope = _Session.getQualifiedData( variableStore.SESSION_SCOPE );
		if ( sessionScope != null ){
			tmpSession.setQualifiedData( variableStore.SESSION_SCOPE, sessionScope );
		}
		cfStructData clientScope = _Session.getQualifiedData( variableStore.CLIENT_SCOPE );
		if ( clientScope != null ){
			tmpSession.setQualifiedData( variableStore.CLIENT_SCOPE, clientScope );
		}
		
		tmpSession.setQualifiedData( variableStore.SERVER_SCOPE, _Session.getQualifiedData( variableStore.SERVER_SCOPE ) );
		tmpSession.setQualifiedData( variableStore.REQUEST_SCOPE, _Session.getQualifiedData( variableStore.REQUEST_SCOPE ) );
		tmpSession.setQualifiedData( variableStore.CGI_SCOPE, _Session.getQualifiedData( variableStore.CGI_SCOPE ) );
		tmpSession.setQualifiedData( variableStore.FORM_SCOPE, _Session.getQualifiedData( variableStore.FORM_SCOPE ) );
		tmpSession.setQualifiedData( variableStore.URL_SCOPE, _Session.getQualifiedData( variableStore.URL_SCOPE ) );
		tmpSession.setQualifiedData( variableStore.COOKIE_SCOPE, _Session.getQualifiedData( variableStore.COOKIE_SCOPE ) );
		tmpSession.setQualifiedData( variableStore.FILE_SCOPE, _Session.getQualifiedData( variableStore.FILE_SCOPE ) );
		tmpSession.setQualifiedData( variableStore.CFTHREAD_SCOPE, _Session.getQualifiedData( variableStore.CFTHREAD_SCOPE ) );
		
		cfComponentData componentData = _Session.getActiveComponentData();
		if ( componentData != null ) {
			tmpSession.pushComponentData( componentData, cfFUNCTION.EMPTY_FUNCTION );
			tmpSession.setQualifiedData( variableStore.SUPER_SCOPE, componentData.getSuperComponent() ); // set up SUPER scope
		}
		
		return tmpSession;
	}
  
	private cfStructData packageUpAttributes( cfSession _Session, cfStructData _attribs ) throws cfmRunTimeException {
		cfStructData attributeValues = new cfStructData();

		// Look to see if the ATTRIBUTECOLLECTION has been passed through
		// Note that the ATTRIBUTECOLLECTION is handled first so that specified
		// parameters can override those also supplied in the ATTRIBUTECOLLECTION
		if ( _attribs != null ) {
			Object[] keys = _attribs.keys();
			for ( int i = 0; i < keys.length; i++ ) {
			String key = (String)keys[ i ];
				cfData data = _attribs.getData( key ).duplicate();
				attributeValues.setData( key, data );
			}
		}else{
			Iterator<String> iter = properties.keySet().iterator();
			while ( iter.hasNext() ) {
				String key = iter.next();
				if ( !attributes.contains( key.toUpperCase() ) ) {
					cfData data = getDynamic( _Session, key ).duplicate();
					attributeValues.setData( key, data );
				}
			}
		}	
		return attributeValues;
	}
}
