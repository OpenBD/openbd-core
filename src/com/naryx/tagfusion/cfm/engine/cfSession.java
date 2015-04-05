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
 *  http://openbd.org/
 *  $Id: cfSession.java 2486 2015-01-22 03:22:37Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

/**
 * This class represents the session between the client and the server for one
 * request.  This is not the session as in the CFML session scope, nor is it a
 * session in the Servlet context.
 */

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aw20.collections.FastStack;

import com.nary.io.FileUtils;
import com.nary.util.FastMap;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.application.cfAPPLICATION;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;
import com.naryx.tagfusion.cfm.file.cfmlURI;
import com.naryx.tagfusion.cfm.parser.CFCall;
import com.naryx.tagfusion.cfm.parser.CFCallStack;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFGlobalScope;
import com.naryx.tagfusion.cfm.parser.CFUndefinedValue;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.parser.script.CFParsedStatement;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.sql.cfTRANSACTION;
import com.naryx.tagfusion.cfm.sql.cfTransactionCache;
import com.naryx.tagfusion.cfm.sql.preparedData;
import com.naryx.tagfusion.cfm.sql.resultSetHolder;
import com.naryx.tagfusion.cfm.tag.cfERROR;
import com.naryx.tagfusion.cfm.tag.cfFLUSH;
import com.naryx.tagfusion.cfm.tag.cfFUNCTION;
import com.naryx.tagfusion.cfm.tag.cfMODULE;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.expression.function.GetHttpStatusLabel;
import com.naryx.tagfusion.util.debugRecorder;
import com.naryx.tagfusion.util.debuggerListener;
import com.naryx.tagfusion.util.fullRecorder;
import com.naryx.tagfusion.util.nullDebugger;
import com.naryx.tagfusion.util.nullRecorder;

public class cfSession {

	// key for storing a cfSession instance in the request scope for CFINCLUDE
	public static final String ATTR_NAME = "com.naryx.tagfusion.cfm.engine.cfSession";

	/*
	 * ATTENTION! Don't initialize any instance attributes that are initialized in
	 * the "copy" constructor cfSession( cfSession _oldSession, boolean
	 * _escapeSingleQuotes ).
	 */

	public int	sessionID;
	
	public HttpServletRequest REQ;
	public cfHttpServletResponse RES;
	public ServletContext CTX;

	private boolean bForwardRedirect;			// forwarded or redirected
	private boolean bPostRequest;

	private variableStore dataStore;
	private CFContext cfContext;
	private cfSession topLevelSession;

	private FastStack<cfFile> fileStack;
	private FastStack<cfOutputFilter> filterStack;
	private FastStack<cfTag> tagStack;

	private FastStack<cfComponentData> _componentDataStack;
	private FastStack<cfFUNCTION> _componentTagStack;
	private FastStack<userDefinedFunction> userDefinedFunctionStack;

	private FastStack<cfStructData> superStack;
	private FastStack<cfStructData> callerStack;
	private FastStack<cfStructData> variableStack;
	private FastStack<Stack<cfQueryResultData>> queryStack;

	private FastStack<customTagVariableWrapper> baseTagDataStack;
	private Map<String, cfFile> fileCache;
	private cfOutputFilter outputFilter;

	private int activeExpression;

	private Locale locale;
	private Map<String, Object> dataBin;

	private boolean bProcessingCFOUTPUT;	// These two variables support the
	private int cfSettingCounter;					// the CFSETTING feature

	private boolean escapeSingleQuotes;

	private boolean hasBufferReset;

	public debuggerListener debugger;	// A debugger instance
	private debugRecorder recorder;			// A debugRecorder instance

	private Map<String, Random> randoms = null;
	private long totalPageOut;

	// These members are used to store information about the last called cfflush tag.
	private boolean	cfFlushCalled = false;
	private String cfFlushFilePath;
	private int cfFlushLine;
	private int cfFlushColumn;

	// per-request mappings from CFMAPPING
	private Map<String, String> mappings;

	// for aborting threads externally (from another thread)
	private boolean threadStopped;

	// per-request database connections
	private Map<String, Connection> connections;
	
	// metric collection
	private int	metricTotalQuery=0; 
	private long metricTotalQueryTime=0;
	
	public boolean isStopped() {
		return topLevelSession.threadStopped;
	}

	public void stopThread() {
		threadStopped = true;
	}

	public int	getSessionID(){
		return sessionID;
	}
	
	/**
	 * Create a session from a parent session. This constructor is used by
	 * cfTag.renderToString() for rendering tag bodies as strings (such as for
	 * CFQUERY, for example). Make sure to buffer the entire output--don't send
	 * any to the client! Also, it's expected that no changes will be made to
	 * cookies, response headers, response content type, etc.
	 */
	public cfSession(cfSession _oldSession, boolean _escapeSingleQuotes) {
		escapeSingleQuotes = _escapeSingleQuotes;

		REQ = _oldSession.REQ;
		RES = _oldSession.RES.createChild(this);
		CTX = _oldSession.CTX;

		dataStore = _oldSession.dataStore;
		cfContext = _oldSession.cfContext;

		topLevelSession = _oldSession.topLevelSession;
		fileStack 			= _oldSession.fileStack;
		filterStack 		= _oldSession.filterStack;
		tagStack 				=	_oldSession.tagStack;

		_componentDataStack 			= _oldSession._componentDataStack;
		_componentTagStack 				= _oldSession._componentTagStack;
		userDefinedFunctionStack	= _oldSession.userDefinedFunctionStack;

		superStack 		= _oldSession.superStack;
		callerStack 	= _oldSession.callerStack;
		variableStack = _oldSession.variableStack;
		queryStack 		= _oldSession.queryStack;

		fileCache 				= _oldSession.fileCache;
		baseTagDataStack 	= _oldSession.baseTagDataStack;

		outputFilter 			= _oldSession.outputFilter;
		locale 						= _oldSession.locale;

		dataBin 					= _oldSession.dataBin;
		bPostRequest 			= _oldSession.bPostRequest;

		bProcessingCFOUTPUT = _oldSession.bProcessingCFOUTPUT;
		cfSettingCounter 		= _oldSession.cfSettingCounter;

		debugger 	= _oldSession.debugger;
		recorder 	= _oldSession.recorder;
		sessionID	= _oldSession.sessionID;
		
		if ( _oldSession.randoms != null ){
			randoms = new FastMap<String, Random>();
			randoms.putAll( _oldSession.randoms );
		}

		totalPageOut	= _oldSession.totalPageOut;
		mappings			= _oldSession.mappings;
		connections 	= _oldSession.connections;
	}

	/**
	 * Creates a cfSession instance for the specified request
	 *
	 * @param req -
	 *          the client's request
	 * @param res -
	 *          the server's response to this request
	 */
	public cfSession(HttpServletRequest req, HttpServletResponse res, ServletContext ctx) {
		REQ 					= req;
		RES 					= new cfHttpServletResponse(this, res);
		CTX 					= ctx;
		dataStore 		= new variableStore(this);
		cfContext 		= new CFContext(new CFGlobalScope(this), this);
		bPostRequest 	= REQ.getMethod().equalsIgnoreCase( "POST" );

		fileStack	 		= new FastStack<cfFile>();
		filterStack 	= new FastStack<cfOutputFilter>();
		tagStack 			= new FastStack<cfTag>(40);

		_componentDataStack 			= new FastStack<cfComponentData>();
		_componentTagStack 				= new FastStack<cfFUNCTION>();
		userDefinedFunctionStack	= new FastStack<userDefinedFunction>();

		superStack 			= new FastStack<cfStructData>();
		callerStack 		= new FastStack<cfStructData>();
		variableStack 	= new FastStack<cfStructData>(10);
		queryStack 			= new FastStack<Stack<cfQueryResultData>>(2);

		fileCache				= new FastMap<String, cfFile>();
		topLevelSession = this;
		locale 					= Locale.getDefault();
		dataBin 				= new FastMap<String, Object>(2);
		debugger 				= nullDebugger.staticInstance;
		sessionID				= cfEngine.sessionCounter.getAndIncrement();

		boolean debug 	= cfEngine.isDebuggerOutputEnabled() && checkDebugIP(req.getRemoteAddr());
		
		// if debug enabled and request is from valid IP
		if ( debug ) {
			this.registerDebugRecorder( new fullRecorder() );
			recorder.startRequest();
		} else {
			this.registerDebugRecorder( nullRecorder.staticInstance );
		}

		mappings 		= new FastMap<String, String>();
		connections = new FastMap<String, Connection>();
	}

	public boolean setSuppressWhiteSpace( boolean suppress ) {
		boolean temp = RES.isSuppressWhiteSpace();
		RES.setSuppressWhiteSpace( suppress );
		return temp;
	}

	public boolean isEscapeSingleQuotes() {
		return escapeSingleQuotes;
	}

	public String getRemoteIP(){
		return REQ.getRemoteAddr();
	}
	
	public void setEscapeSingleQuotes( boolean escape ) {
		escapeSingleQuotes = escape;
	}

	public String getEncoding() {
		return RES.getCharacterEncoding();
	}

	/** ---------------------------------------------------------------
	 *  We keep track of the active UserDefiniedFunction.  This will us 
	 *  intelligently unwrap to the right point in the event of an exception ocurring.
	 *  
	 *  This is used in the userDefinedFunction.execute() and the exceptionCatcher.prepareSession() methods
	 * 
	 * @param udf
	 */
	public void pushUserDefinedFunction(userDefinedFunction udf){
		userDefinedFunctionStack.push(udf);
	}

	public userDefinedFunction popUserDefinedFunction(){
		return userDefinedFunctionStack.pop();
	}

	public boolean isUserDefinedFunctionEmpty(){
		return userDefinedFunctionStack.empty();
	}
	
	public userDefinedFunction peekUserDefinedFunction(){
		if ( !userDefinedFunctionStack.empty() )
			return userDefinedFunctionStack.peek();
		else
			return null;
	}
	
	// ----------------------------------------------------------------

	public Object getDataBin( String key ) {
		return dataBin.get( key );
	}

	public void setDataBin( String key, Object value ) {
		dataBin.put( key, value );
	}

	public void deleteDataBin( String key ) {
		dataBin.remove( key );
	}

	public CFContext getCFContext() {
		return cfContext;
	}

	// used by CFTRY for snapshot
	public CFCallStack getCFCallStack() {
		CFCallStack copy = new CFCallStack();
		copy.addAll( cfContext.getCallStack() );
		return copy;
	}

	public void setCFCallStack( CFCallStack callStack ) {
		CFCallStack _callStack = cfContext.getCallStack();
		_callStack.removeAllElements();
		_callStack.addAll( callStack );
	}

	public Stack<CFCallStack> getCallStackStack() {
		Stack<CFCallStack> copy = new Stack<CFCallStack>();
		copy.addAll( cfContext.getCallStackStack() );
		return copy;
	}

	public void setCallStackStack( Stack<CFCallStack> _callStackStack ) {
		Stack<CFCallStack> callStackStack = cfContext.getCallStackStack();
		callStackStack.removeAllElements();
		callStackStack.addAll( _callStackStack );
	}

	// ----------------------------------------------------------------

	public void setActiveExpression( int _e ){
		activeExpression = _e;
	}

	public int getActiveExpression(){
		return activeExpression;
	}

	public FastStack<customTagVariableWrapper> getBaseTagData() {
		return baseTagDataStack;
	}

	// used by CFTRY for snapshot
	@SuppressWarnings("unchecked")
	public FastStack<cfComponentData> getComponentDataStack() {
		return (FastStack<cfComponentData>)_componentDataStack.clone();
	}

	public void setComponentDataStack( FastStack<cfComponentData> data ) {
		_componentDataStack.replaceWith( data );
	}

	@SuppressWarnings("unchecked")
	public FastStack<cfFUNCTION> getComponentTagStack() {
		return (FastStack<cfFUNCTION>)_componentTagStack.clone();
	}

	public void setComponentTagStack( FastStack<cfFUNCTION> data ) {
		_componentTagStack.replaceWith( data );
	}

	// returns null if no active component
	public cfComponentData getActiveComponentData() {
		cfComponentData componentData = null;
		if ( !_componentDataStack.empty() ) {
			componentData = _componentDataStack.peek();
		}
		return componentData;
	}

	/**
	 * If there's an active component, return the fully qualified path to the
	 * component in dotted notation, minus the component name. Returns null if no
	 * active component.
	 */
	public String getActiveComponentPath() {
		cfComponentData componentData = getActiveComponentData();
		if ( componentData != null ) {
			return componentData.getComponentPackagePath();
		}
		return null;
	}

	// returns null if no active component
	public cfFUNCTION getActiveComponentTag() {
		cfFUNCTION componentTag = null;
		if ( !_componentTagStack.empty() ) {
			componentTag = _componentTagStack.peek();
		}
		return componentTag;
	}

	public void pushComponentData( cfComponentData data, cfFUNCTION tag ) {
		_componentDataStack.push(data);
		_componentTagStack.push(tag);
	}

	public cfComponentData popComponentData() {
		cfComponentData data = _componentDataStack.pop();
		_componentTagStack.pop();
		return data;
	}

	// replace the cfFUNCTION tag at the top of the _componentTagStack with the
	// one specified
	public void replaceComponentTag( cfFUNCTION oldTag, cfFUNCTION newTag ) {
		if ( !_componentTagStack.empty() && ( _componentTagStack.peek() == oldTag ) ) {
			_componentTagStack.pop();
			_componentTagStack.push( newTag );
		}
	}

	/**
	 * Set-up for tag-based UDF function call.
	 */
	public CFCall enterUDF( cfStructData newArguments, cfComponentData newSuper, boolean _includeQueryStack ) {
		// set component instances if present
		cfStructData newVariables = null;

		cfComponentData componentData = this.getActiveComponentData();
		if ( componentData != null ) {
			newVariables = componentData.getVariablesScope();
			if ( newSuper == null ) {
				newSuper = componentData.getSuperComponent();
			}
		}

		return this.enterUDF( newArguments, newSuper, newVariables, _includeQueryStack );
	}

	public CFCall enterUDF( cfStructData newArguments, cfStructData newSuper,cfStructData newVariables, boolean _includeQueryStack ) {
		// function locals
		CFCall call = new CFCall( newArguments );
		cfContext.pushCall( call );

		cfStructData oldVariables = pushVariables( _includeQueryStack );

		// super
		if ( newSuper != null ) {
			dataStore.setQualifiedData( variableStore.SUPER_SCOPE, newSuper );
		}

		// variables (use old variables if new variables not provided)
		dataStore.setQualifiedData( variableStore.VARIABLES_SCOPE, newVariables != null ? newVariables : oldVariables );

		return call;
	}

	public boolean executingUDF() {
		return !cfContext.isCallEmpty();
	}

	public void leaveUDF() {
		popVariables();
		cfContext.popCall();
	}

	public CFCall enterCFThread( cfStructData _attribs, cfStructData threadData ) throws cfmRunTimeException{
		CFCall call = new CFCall();
		call.put( "attributes", _attribs, cfContext );
		call.put( "thread", threadData, cfContext );
		cfContext.pushCall( call );

		return call;
	}


	// for use by CFDUMP
	public cfStructData getLocalScope() {
		Map<String, cfData> localScope = cfContext.getLocalScope();
		return ( localScope == null ? null : new cfStructData( localScope ) );
	}

	public void enterCustomTag( cfStructData thisTag, cfStructData attributes, String tagName ) {
		enterCustomTag(thisTag, attributes, tagName, new cfStructData(), true );
	}

	public void enterCustomTag( cfStructData thisTag, cfStructData attributes, String tagName, cfStructData newVariables ) {
		enterCustomTag(thisTag, attributes, tagName, newVariables, true );
	}

	public void enterCustomTag( cfStructData thisTag, cfStructData attributes, String _tagName, cfStructData newVariables, boolean _addBaseTagData ) {
		cfCallerData variables = new cfCallerData(this, pushVariables( false ));

		newVariables.setData(cfMODULE.THISTAG_SCOPE, thisTag);
		newVariables.setData(variableStore.ATTRIBUTES_SCOPE_NAME, attributes);
		newVariables.setData(variableStore.CALLER_SCOPE_NAME, variables);

		if ( _addBaseTagData ) {
			if ( baseTagDataStack == null )
				baseTagDataStack = new FastStack<customTagVariableWrapper>();

			baseTagDataStack.push(new customTagVariableWrapper(_tagName, newVariables));
		}

		dataStore.setQualifiedData( variableStore.VARIABLES_SCOPE, newVariables );
		dataStore.setQualifiedData( variableStore.CALLER_SCOPE, variables );

		cfContext.enterCustomTag();
		
		// if rendering custom tag within component function, "forget" about
		// the fact that we're within the component (see bug #2170)
		pushComponentData( null, null );
	}

	// restores the variables, caller, and arguments scopes to the state they
	// were in prior to entering the custom tag
	public cfStructData leaveCustomTag() {
		popComponentData();
		cfContext.leaveCustomTag();
		return popVariables();
	}

	// called at the end of custom tag processing
	public void clearCustomTag() {
		leaveCustomTag();
		baseTagDataStack.pop();
	}

	/**
	 * pushVariables
	 *
	 * Remove the following scopes in preparation to entering a UDF or custom tag:
	 * function locals, arguments, caller, variables. Save these on a stack to be
	 * restored later. Returns the saved variables scope.
	 */
	private cfStructData pushVariables( boolean _pushQueryStack ) {
		callerStack.push(dataStore.deleteQualifiedData(variableStore.CALLER_SCOPE));
		superStack.push(dataStore.deleteQualifiedData(variableStore.SUPER_SCOPE));
		variableStack.push(dataStore.deleteQualifiedData(variableStore.VARIABLES_SCOPE));
		queryStack.push( _pushQueryStack ? dataStore.getQueryStack() : dataStore.removeQueryStack() );

		return (cfStructData)variableStack.peek();
	}

	/**
	 * popVariables
	 *
	 * Restores the following scopes after returning from a UDF or custom tag:
	 * arguments, caller, variables. Returns the old variables scope.
	 */
	public cfStructData popVariables() {
		// caller
		dataStore.deleteQualifiedData(variableStore.CALLER_SCOPE);
		cfStructData poppedCaller = callerStack.pop();
		if ( poppedCaller != null ) {
			dataStore.setQualifiedData(variableStore.CALLER_SCOPE, poppedCaller);
		}

		// super
		dataStore.deleteQualifiedData(variableStore.SUPER_SCOPE);
		cfStructData poppedSuper = superStack.pop();
		if ( poppedSuper != null ) {
			dataStore.setQualifiedData(variableStore.SUPER_SCOPE, poppedSuper);
		}

		// variables
		cfStructData oldVariables = (cfStructData) dataStore.deleteQualifiedData(variableStore.VARIABLES_SCOPE);
		cfStructData poppedVariables = (cfStructData)variableStack.pop();
		if ( poppedVariables != null ) {
			dataStore.setQualifiedData(variableStore.VARIABLES_SCOPE, poppedVariables);
		} else {
			// this should never happen!
			dataStore.setQualifiedData(variableStore.VARIABLES_SCOPE,new cfStructData());
		}

		// query stack
		dataStore.setQueryStack( queryStack.pop() );

		return oldVariables;
	}

	// these methods are to save and restore the variable stacks for CFTRY/CFCATCH
	public FastStack<cfStructData> getSuperStack() {
		return (FastStack<cfStructData>)superStack.clone();
	}

	public FastStack<cfStructData> getCallerStack() {
		return (FastStack<cfStructData>)callerStack.clone();
	}

	public FastStack<cfStructData> getVariableStack() {
		return (FastStack<cfStructData>)variableStack.clone();
	}

	public FastStack<Stack<cfQueryResultData>> getQueryStack() {
		return (FastStack<Stack<cfQueryResultData>>)queryStack.clone();
	}

	public void setSuperStack( FastStack<cfStructData> supStack ) {
		superStack.replaceWith(supStack);
	}

	public void setCallerStack( FastStack<cfStructData> callStack ) {
		callerStack.replaceWith(callStack);
	}

	public void setVariableStack( FastStack<cfStructData> varStack ) {
		variableStack.replaceWith(varStack);
	}

	public void setQueryStack( FastStack<Stack<cfQueryResultData>> qryStack ) {
		queryStack.replaceWith(qryStack);
	}

	// ----------------------------------------------------------------
	// --[ Methods required for debugging
	// ----------------------------------------------------------------

	public void setShowDebugOutput( boolean _show ) {
		recorder.setShow(_show);
	}

	public boolean getShowDebugOutput() {
		return recorder.getShow();
	}

	public boolean getShowDBActivity() {
		return recorder.getShowDBActivity();
	}

	public boolean isDebugEnabled() {
		return !(recorder == nullRecorder.staticInstance);
	}

	public void recordException( cfmRunTimeException e, cfFile f, cfTag t ) {
		recorder.exceptionThrown(e, f, t);
	}

	public void recordQuery( cfFile f, String _name, cfSQLQueryData _q, List<preparedData> _qParams ) {
		// If the filename is null then use the URI.
		String template = f.getName();
		if ( template == null )
			template = f.getCfmlURI().getURI();
		recorder.queryRan(template, _name, _q, _qParams);
	}

	public void recordUpdate( cfFile f, String _datasrc, String _q ) {
		// If the filename is null then use the URI.
		String template = f.getName();
		if ( template == null )
			template = f.getCfmlURI().getURI();
		recorder.updateRan(template, _datasrc, _q);
	}

	public void recordInsert( cfFile f, String _datasrc, String _q ) {
		// If the filename is null then use the URI.
		String template = f.getName();
		if ( template == null )
			template = f.getCfmlURI().getURI();
		recorder.insertRan(template, _datasrc, _q);
	}

	public void storedProcRan( cfFile f, String _datasrc, String _procName,
			long _execTime, List<preparedData> _params, List<resultSetHolder> _results ) {
		// If the filename is null then use the URI.
		String template = f.getName();
		if ( template == null )
			template = f.getCfmlURI().getURI();
		recorder.storedProcRan(template, _datasrc, _procName, _execTime, _params,
				_results);
	}

	public void recordTracepoint( String tracePoint ) {
		recorder.recordTracepoint(tracePoint);
	}

	public void recordTimer( String timing ) {
		recorder.recordTimer(timing);
	}

	// ----------------------------------------------------------------
	// --[ Methods for handling the present cfFile
	// ----------------------------------------------------------------

	public void pushActiveFile( cfFile svrFile ) {
		fileStack.push(svrFile);
		debugger.startFile(svrFile);
		recorder.startFile(svrFile);
	}

	public cfFile popActiveFile() {
		cfFile f = fileStack.pop();
		debugger.endFile(f);
		recorder.endFile(f);
		return f;
	}

	public cfFile activeFile() {
		if ( fileStack.size() == 0 || fileStack.empty() )
			return null;
		else
			return fileStack.peek();
	}

	public Enumeration<cfFile> fileStackEnumeration() {
		return fileStack.elements();
	}

	// used by cftry to snapshot
	@SuppressWarnings("unchecked")
	public FastStack<cfFile> getFileStack() {
		return (FastStack<cfFile>)fileStack.clone();
	}

	// used by cftry to restore snapshot
	public void setFileStack( FastStack<cfFile> _ht ) {
		fileStack.replaceWith(_ht);
	}

	public String getBaseTemplatePath() {
		cfFile f = (cfFile) fileStack.firstElement();
		return f.getName();
	}

	public File getCurrentFile(){
		cfFile file = (com.naryx.tagfusion.cfm.file.cfFile) fileStack.peek();
		return file.getCfmlURI().getFile();
	}

	// ----------------------------------------------------------------
	// --[ Tag Stack access functions
	// ----------------------------------------------------------------

	public void pushTag( cfTag thisTag ) {
		tagStack.push(thisTag);
		debugger.startTag(thisTag);
	}

	public void popTag() {
		cfTag tag = tagStack.pop();
		debugger.endTag(tag);
	}

	public void replaceTag( cfTag newTag ) {
		tagStack.pop();
		tagStack.push(newTag);
	}

	public void clearTagStack() {
		tagStack.removeAllElements();
	}

	public cfTag activeTag() {
		return (tagStack.empty() ? null : tagStack.peek());
	}

	// used by cftry to snapshot
	public FastStack<cfTag> getTagStack() {
		return (FastStack<cfTag>)tagStack.clone();
	}

	// used by cftry to snapshot
	public void setTagStack(FastStack<cfTag> _tg ) {
		tagStack.replaceWith(_tg);
	}

	public Enumeration<cfTag> getTagElements() {
		return tagStack.elements();
	}

	public int getTagStackSize() {
		return tagStack.size();
	}

	public cfTag getTagElement( int x ) {
		return tagStack.elementAt(x);
	}

	public void startScriptStatement( CFParsedStatement _statement ){
		debugger.startScriptStatement( _statement );
	}
	
	// ----------------------------------------------------------------

	public Random getRandom( String _alg ) throws NoSuchAlgorithmException {
		Random random = null;
		String algorithm = _alg.toUpperCase();

		if ( randoms == null ){
			randoms = new FastMap<String, Random>();
		}else{
			random = randoms.get( algorithm );
		}

		if ( random == null ) {
			if ( algorithm.equals( "CFMX_COMPAT" ) ){
				random = new Random();
			}else{
				random = java.security.SecureRandom.getInstance( algorithm );
			}
			randoms.put( algorithm, random );
		}
		return random;
	}

	public void setRandomSeed( cfData seed, String _alg ) throws dataNotSupportedException, NoSuchAlgorithmException {
		Random rand = getRandom( _alg );
		rand.setSeed(seed.getLong());
	}

	// ----------------------------------------------------------------

	public Locale getLocale() {
		return locale;
	}

	public String getLocaleDisplayName() {
		return locale.getDisplayName( Locale.US );
	}

	public void setLocale( Locale _newLocale ) {
		locale = _newLocale;
	}

	/**
	 * This is called from the cfEngine serviceCfcMethod/service methods to optionally decode the incoming
	 * POST request, which includes the upload file forms.
	 * 
	 * @param bProcessLegacyFormValidation
	 * @throws cfmRunTimeException
	 */
	public void checkAndDecodePost( boolean bProcessLegacyFormValidation ) throws cfmRunTimeException {
		if ( !bPostRequest )
			return;
		
		cfDecodedInput.checkAndDecodePost(this);
		
		if ( bProcessLegacyFormValidation )
			((cfFormData) dataStore.getData("form")).validateFormFields(this);
	}


	/**
	 * If manage client data is required, this method be called to save the
	 * session data
	 */
	public void closeClient() throws cfmRunTimeException {
		com.naryx.tagfusion.cfm.application.cfAPPLICATION.closeClient(this);
	}

	public Map<String, cfStructData> getDataStore() {
		return dataStore.getDataStore();// Used only for CFDUMPSESSION 
	}

	public void setDataInSecurityStore( String key, Map<String, String> val, long rolesLifespan ) {
		variableStore.getSecurityStore().add(key, val, rolesLifespan);
	}

	public Map<String, String> getDataFromSecurityStore( String key ) {
		Map<String, String> data = null;
		try {
			data = variableStore.getSecurityStore().get(key);
			if ( data != null && data.isEmpty() )
				data = null; // don't hand out an empty Map
		} catch (Exception e) {
		}

		return data;
	}

	public void removeDataFromSecurityStore( String key ) {
		variableStore.getSecurityStore().remove(key);
	}

	/**
	 * ATTENTION! Calls to setQualifiedData() and getQualifiedData() must always
	 * be made using the constants defined in the variableStore class as keys.
	 */
	public cfStructData getQualifiedData( int _key ) {
		return dataStore.getQualifiedData(_key);
	}

	public void setQualifiedData( int _key, cfStructData _cfData ) {
		dataStore.setQualifiedData(_key, _cfData);
	}

	public void setData( String _key, cfData _cfData ) throws cfmRunTimeException {
		cfData var = runTime.runExpression( this, _key, false );
		// always set variables via cfLData.Set() to insure loop index invalidation
		((cfLData)var).Set( _cfData, cfContext );
	}

	public cfData getData( String _key ) {
		return dataStore.getData( _key, true, true );
	}

	public cfData getData( String _key, boolean _doQuerySearch ) {
		return dataStore.getData( _key, _doQuerySearch, true );
	}

	public cfData getData( String _key, boolean _doQuerySearch, boolean _doVarSearch ) {
		return dataStore.getData( _key, _doQuerySearch, _doVarSearch );
	}


	public cfApplicationData getApplicationData() {
		cfData appData = getQualifiedData(variableStore.APPLICATION_SCOPE);
		if ( (appData != null) && (appData instanceof cfApplicationData) ) {
			return (cfApplicationData) appData;
		}
		return null;
	}

	public void deleteData( String _key ) throws cfmRunTimeException {
		dataStore.deleteData( _key );
	}

	public void pushQuery( cfQueryResultData _query ) {
		dataStore.pushQuery(_query);
	}

	public cfQueryResultData popQuery() {
		return dataStore.popQuery();
	}

	public cfQueryResultData peekQuery() {
		return dataStore.peekQuery();
	}

	// ----------------------------------------------------------------
	// --[ Output methods

	public void close() {

		// This is the last method that is called Look for Java objects
		if ( dataBin != null ){
			Iterator<Map.Entry<String, Object>> iter = dataBin.entrySet().iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if ( obj instanceof cfJavaObjectData )
					((cfJavaObjectData) obj).removeCfmlPageContext();
			}
	
			dataBin.clear();
		}
		fileCache.clear();
	}

	public boolean hasBufferReset(){
		return hasBufferReset;
	}

	public void setBufferReset( boolean _reset ){
		hasBufferReset = _reset;
	}

	/**
	 * Reset the entire response, including headers and output buffer.
	 */
	public void reset() {
		try {
			RES.reset();
			hasBufferReset = true;
		} catch (IllegalStateException ignore) {
			// IllegalStateException is thrown if the response is already committed;
			// reset() is invoked primarily for error handling, so just ignore it
		}
	}

	/**
	 * Reset the output buffer, but not the response headers.
	 */
	public void resetBuffer() throws cfmRunTimeException {
		try {
			RES.resetBuffer();
			hasBufferReset = true;
		} catch (IllegalStateException e) {
			StringBuilder detail = new StringBuilder("Unable to reset response buffer because the page has been flushed");
			appendCfFlushInfo(detail);

			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail(detail.toString());
			catchData.setMessage("Cannot reset response buffer");
			throw new cfmRunTimeException(catchData);
		}
	}

	public void setStatus( int statusCode ) throws cfmRunTimeException {
		try {
			RES.setStatus(statusCode);
			debugger.setHTTPStatus(statusCode, "");
		} catch (IllegalStateException e) {
			StringBuilder detail = new StringBuilder("Unable to set HTTP response status code because the page has been flushed");
			appendCfFlushInfo(detail);

			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail(detail.toString());
			catchData.setMessage("Cannot set HTTP response status code");
			throw new cfmRunTimeException(catchData);
		}
	}

	public void setStatus( int statusCode, String value )
			throws cfmRunTimeException {
		try {
			RES.setStatus(statusCode, value);
			debugger.setHTTPStatus(statusCode, value);
		} catch (IllegalStateException e) {
			StringBuilder detail = new StringBuilder("Unable to set HTTP response status code because the page has been flushed");
			appendCfFlushInfo(detail);

			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail(detail.toString());
			catchData.setMessage("Cannot set HTTP response status code");
			throw new cfmRunTimeException(catchData);
		}
	}

	public void setHeader( String _name, String _value )
			throws cfmRunTimeException {
		try {
			RES.addHeader(_name, _value);
			debugger.setHTTPHeader(_name, _value);
		} catch (IllegalStateException e) {
			StringBuilder detail = new StringBuilder("Unable to set HTTP response header because the page has been flushed");
			appendCfFlushInfo(detail);

			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail(detail.toString());
			catchData.setMessage("Cannot set HTTP response header");
			throw new cfmRunTimeException(catchData);
		}
	}

	public void setContentType( String _contentType ) throws cfmRunTimeException {
		try {
			RES.setContentType(_contentType);
		} catch (IllegalStateException e) {
			StringBuilder detail = new StringBuilder("Unable to set HTTP response content type because the page has been flushed");
			appendCfFlushInfo(detail);

			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail(detail.toString());
			catchData.setMessage("Cannot set HTTP response content type");
			throw new cfmRunTimeException(catchData);
		}
	}

	public void setBufferSize( int size ) throws cfmRunTimeException {
		try {
			RES.setBufferSize(size);
		} catch (IllegalStateException e) {
			StringBuilder detail = new StringBuilder("Unable to set page buffer size because the page has been flushed");
			appendCfFlushInfo(detail);

			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail(detail.toString());
			catchData.setMessage("Cannot set page buffer size");
			throw new cfmRunTimeException(catchData);
		}
	}

	private void appendCfFlushInfo( StringBuilder detail ) {
		if ( cfFlushCalled ) {
			detail.append(" by a CFFLUSH tag at line ");
			detail.append(cfFlushLine);
			detail.append("  column ");
			detail.append(cfFlushColumn);
			detail.append(" in file ");
			detail.append(cfFlushFilePath);
		}
	}

	// ---------------------------------------------------------
	// used by tags to flag that output should be sent to page
	// regardless of whether <CFPROCESSINGDIRECTIVE ENABLECFOUPUTONLY=true>
	//
	// anyone who invokes this method should save the old value (returned by
	// this method) and restore it when finished
	public boolean setProcessingCfOutput( boolean _yes ) {
		boolean temp = bProcessingCFOUTPUT;
		bProcessingCFOUTPUT = _yes;
		return temp;
	}

	public boolean getProcessingCfOutput() {
		return bProcessingCFOUTPUT;
	}

	public void clearCfSettings() {
		cfSettingCounter = 0;
	}

	public void enableCfSettings( boolean _yes ) {
		if ( _yes )
			cfSettingCounter += 1;
		else
			cfSettingCounter -= 1;

		if ( cfSettingCounter < 0 )
			cfSettingCounter = 0;
	}

	public void suspendFilter() {
		filterStack.push(outputFilter);
		outputFilter = null;
	}

	public void unsuspendFilter() {
		outputFilter = filterStack.pop();
	}

	public boolean isFiltered() {
		if ( outputFilter != null )
			return true;
		else
			return false;
	}

	public boolean setOutputFilter( cfOutputFilter _newFilter ) {

		if ( outputFilter != null ) {
			if ( _newFilter != null ) {
				if ( outputFilter.shouldWeIgnore() )
					return true;
				else
					return false;
			} else {
				outputFilter = null;
				return true;
			}
		}

		outputFilter = _newFilter;
		return true;
	}

	public void setHeadElement( String str, boolean append ) throws cfmRunTimeException {
		try {
			RES.setHeadElement( str, append);
		} catch (IllegalStateException e) {
			StringBuilder detail = new StringBuilder("Unable to add text to HTML <head> tag because the page has been flushed");
			appendCfFlushInfo(detail);

			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail(detail.toString());
			catchData.setMessage("Cannot add text to HTML <head> tag");
			throw new cfmRunTimeException(catchData);
		}
	}

	public void setBodyElement( String str, boolean append ) throws cfmRunTimeException {
		try {
			RES.setBodyElement( str, append);
		} catch (IllegalStateException e) {
			StringBuilder detail = new StringBuilder("Unable to add text to HTML <body> tag because the page has been flushed");
			appendCfFlushInfo(detail);

			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail(detail.toString());
			catchData.setMessage("Cannot add text to HTML <body> tag");
			throw new cfmRunTimeException(catchData);
		}
	}

	public void write( String line ) {
		if ( !bProcessingCFOUTPUT && cfSettingCounter > 0 )
			return;

		RES.write( line );
		totalPageOut += line.length();
	}

	public void forceWrite( String line ) {
		RES.write( line );
		totalPageOut += line.length();
	}

	public void write( char[] buffer ) {
		if ( !bProcessingCFOUTPUT && cfSettingCounter > 0 )
			return;

		RES.write(buffer, 0, buffer.length);
		totalPageOut += buffer.length;
	}

	public void write( byte[] buf ) throws cfmRunTimeException {
		try {
			RES.write(this,buf);
		} catch (IllegalStateException e) {
			StringBuilder detail = new StringBuilder("Unable to write response data because the page has been flushed");
			appendCfFlushInfo(detail);

			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail(detail.toString());
			catchData.setMessage("Cannot write response data");
			throw new cfmRunTimeException(catchData);
		} catch (IOException ignore) {
			// this should only happen when the user hits the browser "Stop" button
			// and breaks the connection
		}
	}

	public void write( byte[] buf, int off, int len ) throws cfmRunTimeException {
		try {
			RES.write(this,buf, off, len);
		} catch (IllegalStateException e) {
			StringBuilder detail = new StringBuilder(	"Unable to write response data because the page has been flushed");
			appendCfFlushInfo(detail);

			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail(detail.toString());
			catchData.setMessage("Cannot write response data");
			throw new cfmRunTimeException(catchData);
		} catch (IOException ignore) {
			// this should only happen when the user hits the browser "Stop" button
			// and breaks the connection
		}
	}

	public String getOutputAsString() {
		return RES.getOutputAsString();
	}

  public void abortPageProcessing() throws cfmAbortException {
    throw new cfmAbortException( false );
  }

  public void abortPageProcessing( boolean flushOutput ) throws cfmAbortException {
    throw new cfmAbortException( flushOutput );
  }

	public void sendRedirect( String absURL ) throws cfmRunTimeException {
		sendRedirect( absURL, true, 302 );
	}

	public void sendRedirect( String absURL, boolean abort ) throws cfmRunTimeException {
		sendRedirect( absURL, abort, 302 );
	}

	public void sendRedirect( String absURL, boolean abort, int statusCode ) throws cfmRunTimeException {
		try {
			// note this is to workaround a bug in WebSphere (see bug #2862)
			if ( absURL.toLowerCase().startsWith("mailto:") ){
				RES.setStatus( statusCode );
				RES.setHeader("Location", absURL );
			}else{
				
				if ( statusCode == 302 )
					RES.sendRedirect(absURL);
				else{
					RES.setStatus( statusCode, GetHttpStatusLabel.getStatusLabel(statusCode) );
					RES.setHeader("Location", absURL );
				}
			}
			
		} catch (IllegalStateException e) {
			StringBuilder detail = new StringBuilder("Unable to perform redirect because the page has been flushed");
			appendCfFlushInfo(detail);

			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail(detail.toString());
			catchData.setMessage("Cannot perform redirect");
			throw new cfmRunTimeException(catchData);
		} catch (IOException e) {
			cfCatchData catchData = new cfCatchData();
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setDetail("Redirect failed due to IO Exception: " + e);
			catchData.setMessage("Redirect failed due to IO Exception");
			throw new cfmRunTimeException(catchData);
		}

		bForwardRedirect = true;
		if ( abort ) {
			throw new cfmAbortException();
		}
	}

	
	
	public String getRequestURIPath() {
		String tmp = getRequestURI();

		if ( tmp.charAt(tmp.length() - 1) == '/' )
			return tmp;

		int c1 = tmp.lastIndexOf("/");
		if ( c1 == -1 )
			return "/";
		else
			return tmp.substring(0, c1 + 1);
	}

	
	
	public String getRequestURI() {
		String uriName = (String) REQ.getAttribute("javax.servlet.include.servlet_path");
		if ( uriName == null )
			uriName = REQ.getServletPath();

		if ( uriName.indexOf("?") != -1 )
			uriName = uriName.substring(0, uriName.indexOf("?"));

		if ( uriName.charAt(uriName.length() - 1) == '/' )
			uriName = REQ.getPathInfo();

		return uriName;
	}

	
	
	public cfFile getRequestFile() throws cfmBadFileException {
		String uriName = getRequestURI();
		String uriNameUpper = uriName.toUpperCase();

		// Check the application.cfm and onrequestend.cfm page
		if ( uriNameUpper.endsWith("/APPLICATION.CFM")
				|| uriNameUpper.endsWith("/APPLICATION.CFC")
				|| uriNameUpper.endsWith("/ONREQUESTEND.CFM") 
				|| uriNameUpper.endsWith("/SERVER.CFC")		
			) {
			throw new cfmBadFileException(catchDataFactory.applicationRequestFileException(uriName.substring(uriName.lastIndexOf("/") + 1)));
		} else
			return getUriFile(uriName);
	}

	
	
	public cfmlURI getFilePath( String _uri ) {
		return new cfmlURI(REQ, cleanURI(_uri));
	}

	
	private String cleanURI( String _uri ) {
		String URI = _uri.replace('\\', '/'); // normalize to remove Windows file separator chars

		if ( URI.charAt(0) != '/' )
			URI = getPresentURI() + URI;

		return com.nary.io.FileUtils.cleanPath(URI);
	}

	
	
	/**
	 * If you already have a cfmlURI instance, it's better to invoke getFile( cfmlURI )
	 * directly instead of getUriFile( cfmlURL.getURI() )
	 */
	public cfFile getUriFile( String URI ) throws cfmBadFileException {
		cfFile cffile = getFile( new cfmlURI( REQ, cleanURI( URI ) ) );
		if ( cffile.getURI() == null ) {
			cffile.setURI( URI );
		}
		return cffile;
	}

	
	
	/**
	 * If you already have a cfmlURI instance, it's better to invoke getFile( cfmlURI )
	 * directly instead of getRealFile( cfmlURL.getRealPath() )
	 */
	public cfFile getRealFile( String realPath ) throws cfmBadFileException {
		return getFile( new cfmlURI( realPath, true ) );
	}

	
	
	public cfFile getFile( cfmlURI uFile ) throws cfmBadFileException {
		String key = uFile.getCacheKey();
		
		if ( fileCache.containsKey( key ) )
			return fileCache.get( key );
		
		
		cfFile thisFile = cfmlFileCache.getCfmlFile( CTX, uFile, REQ );
		fileCache.put( key, thisFile );
		return thisFile;
	}

	
	
	public void removeFromFileCache( String _file ) {
		Iterator<String> it = fileCache.keySet().iterator();
		while (it.hasNext()) {
			String nextKey = it.next();
			if ( nextKey.endsWith(_file) ) {
				it.remove();
			}
		}
	}


	
	public String getPresentURI() {
		String URI = "";
		cfFile thisFile;

		for (int x = fileStack.size() - 1; x >= 0; --x) {
			thisFile = fileStack.elementAt(x);
			if ( thisFile.getURI() != null ) {
				URI = thisFile.getURI();
				break;
			}
		}

		// Special case, for the application.cfm / onrequestend.cfm files
		if ( fileStack.size() == 0 ) {
			URI = REQ.getServletPath();
			if ( URI.charAt(URI.length() - 1) == '/' )
				URI = REQ.getPathInfo();
		}

		// --[ Clean up the URI
		int c1 = URI.lastIndexOf("/");
		if ( c1 != -1 ) {
			if ( URI.charAt(0) == '/' )
				URI = URI.substring(0, c1 + 1);
			else
				URI = "/" + URI.substring(0, c1 + 1);
		} else
			URI = "/";

		return URI;
	}

	
	
	public String getPresentURIPath() {
		String tmp = getPresentURI();

		if ( tmp.charAt(tmp.length() - 1) == '/' )
			return tmp;

		int c1 = tmp.lastIndexOf("/");
		if ( c1 == -1 )
			return "/";
		else
			return tmp.substring(0, c1 + 1);
	}

	
	
	/**
	 * Returns a file path suitable for use in constructing a cfmlURI; that is, UNIX/Linux
	 * physical paths are preceded with "$"; therefore, do not use the return value from
	 * this method for constructing a java.io.File instance--use getPresentPhysicalPath()
	 * instead
	 */
	public String getPresentFilePath() {
		String presentPath = getPresentDirectory();
		if ( ( presentPath != null ) && ( presentPath.charAt(0) == '/' ) ) {
			// make sure the path is interpreted as a real path on UNIX/Linux, not a URL path
			presentPath = "$" + presentPath;
		}
		return presentPath;
	}

	
	
	/**
	 * Returns a physical path suitable for constructing a java.io.File instance
	 */
	public String getPresentDirectory() {
		String presentPath = null;
		if ( fileStack.size() > 0 ) {
			presentPath = fileStack.elementAt(fileStack.size() - 1).getPath();
			if ( presentPath != null ) {
				int c1 = presentPath.lastIndexOf('/');
				if ( c1 != -1 )
					presentPath = presentPath.substring(0, c1 + 1);
			}
		}
		return presentPath;
	}

	
	
	/*
	 * Returns the physical path of the calling PAGE template; ie the first element in the stack
	 */
	public String getPhyscialPathOfCallingTemplate(){
		String presentPath = null;
		if ( fileStack.size() > 0 ) {
			presentPath = fileStack.firstElement().getPath();
			if ( presentPath != null ) {
				int c1 = presentPath.lastIndexOf('/');
				if ( c1 != -1 )
					presentPath = presentPath.substring(0, c1 + 1);
			}
		}
		return presentPath;
	}

	// ----------------------------------------------------------------
	// ---] Page Processing
	// ----------------------------------------------------------------

	// returns true if the repsonse has been flushed
	public boolean isFlushed() {
		return RES.isCommitted();
	}

	public void pageFlush() {
		RES.flush();
	}

	public void pageFlush( cfFLUSH tag ) {
		RES.flush();

		cfFlushCalled = true;
		cfFlushFilePath = tag.getFile().getName();
		cfFlushLine = tag.posLine;
		cfFlushColumn = tag.posColumn;
	}

	public void pageEnd() {
		if ( !bForwardRedirect )
			recorder.dump(this);

		// in case there is any cfTransaction blocks outstanding
		// do this before closing the session to avoid problems writing client
		// variables to database
		cfTransactionCache tCache = (cfTransactionCache) getDataBin(cfTRANSACTION.DATA_BIN_KEY);
		if ( tCache != null )
			tCache.close();

		// Close the session
		try {
			closeClient();
		} catch (cfmRunTimeException ignore) {}

		// This call must be made after closeClient() since it will use a database
		// connection if client data is being stored in a database.
		closeAllConnections();

		if ( !bForwardRedirect )
			pageFlush();

		// Clear up any temporary files
		cfDecodedInput DI = (cfDecodedInput) getDataBin(cfDecodedInput.DATA_BIN_KEY);
		if ( DI != null )
			DI.deleteFiles();

		// Tell the debugger this is it
		debugger.endSession();
		recorder.endRequest();
	}

	/*
	 * sessionEnd
	 *
	 * This method is called by code that creates a session that is not used
	 * during normal page processing. In this case pageEnd() is not called so
	 * sessionEnd() must be called to clean up the session.
	 * NOTE: refer to bug NA#3174.
	 */
	public void sessionEnd() {
		closeAllConnections();
	}

	/* ----------------------------------------------------------------
	 * Debugger
	 */
	public void registerDebugger( debuggerListener newDebugger ) {
		debugger = newDebugger;
		debugger.registerSession(this);
	}

	public void deRegisterDebugger() {
		debugger = nullDebugger.staticInstance;
	}
	
	public boolean isDebuggerRegistered(){
		return !(debugger == nullDebugger.staticInstance);
	}

	
	/* ----------------------------------------------------------------
	 * DebugRecorder
	 */
	public void registerDebugRecorder( debugRecorder newRecorder ) {
		recorder = newRecorder;
	}

	public void deRegisterDebugRecorder() {
		recorder = nullRecorder.staticInstance;
	}

	public debugRecorder getDebugRecorder() {
		return recorder;
	}

	// ----------------------------------------------------------------

	public void writeAndClose( byte[] dataToSend ) {
		/*
		 * This method is only called from the graphingEngine class. It is for any
		 * class that requires to dump raw data to the client stream, such as an
		 * image.
		 * 
		 * The caller of this method MUST set the content type via 
		 * cfSession.RES.setContentType() before invoking this method!
		 */
		try {
			RES.getOutputStream().write(dataToSend);
			RES.flushBuffer();
		} catch (Exception e) {
			cfEngine.log("Error writing byte[] response: " + e);
		}
	}

	
	public String encodeURL(String urlToEncode) throws cfmRunTimeException {

		// See if there is an application first of all to encode, if not return
		cfApplicationData appData = getApplicationData();
		if (appData == null)
			return urlToEncode;

		if (!appData.isCookiesWorking() && (appData.isClientEnabled() || (appData.isSessionEnabled() && !appData.isJ2EESessionEnabled()))) {
			// Need to add the CFID/CFTOKEN pairs onto this URL before encoding
			cfData urltoken = getQualifiedData(appData.isClientEnabled() ? variableStore.CLIENT_SCOPE : variableStore.SESSION_SCOPE).getData("urltoken");

			if (urltoken != null) {
				if (urlToEncode.indexOf("?") == -1)
					urlToEncode += "?" + urltoken.getString();
				else
					urlToEncode += "&" + urltoken.getString();
			}
		}

		if (appData.isJ2EESessionEnabled())
			return RES.encodeURL(urlToEncode);

		return urlToEncode;
	}

	/**
	 * @_ipAddr An IP address to check against the list of IPs that are configured
	 *          to view Debug output.
	 *
	 * @return whether or not debug output should be enabled based on the ip
	 *         address of the request (true if the passed in client IP should be
	 *         allowed to see debug output, else false is returned.
	 */
	public static boolean checkDebugIP( String _ipAddr ) {
		// if no IP specified then
		int[]	debugIPS	= cfEngine.getDebugIPs();
		if ( debugIPS.length == 0 )
			return false;
		else
			return containsIpMatch( DecodeIPs( _ipAddr ), debugIPS );
	}

	
	public static int[] DecodeIPs( String ipString ) {
		if ( ipString == null )
			return null;

		List<String> tokens = string.split( ipString, ", " );
		int[] result = new int[ tokens.size() * 4 ];

		int index = 0;
		for ( int i = 0; i < tokens.size(); i++ ) {
			String ip = tokens.get( i ).toString();
			List<String> nums = string.split( ip, "." );
			for ( int j = 0; j < 4; j++ ) {
				if ( j < nums.size() ) {
					String num = nums.get( j ).toString();

					if ( num.equals( "*" ) ) {
						result[ index + j ] = -1;
					} else {
						try {
							result[ index + j ] = Integer.parseInt( num );
						} catch ( Exception e ) {
							result[ index + j ] = -1;
						}
					}
				} else {
					result[ index + j ] = -1;
				}
			}
			index += 4;
		}
		return result;
	}

	/**
	 * -1 is used to represent the wildcard ('*') value
	 *
	 * @param ip
	 *            4 digits representing a single IP address
	 * @param ips
	 *            multiple IP addresses
	 * @return true if ip is found to match any ip in ips, else false is
	 *         returned
	 */
	private static boolean containsIpMatch( int[] ip, int[] ips ) {
		boolean result = false;
		if ( ip != null && ip.length == 4 && ips != null && ips.length >= 4 ) {
			for ( int i = 0; i + 3 < ips.length; i += 4 ) {
				if ( ips[ i ] != -1 && ip[ 0 ] != ips[ i ] )
					continue;
				if ( ips[ i + 1 ] != -1 && ip[ 1 ] != ips[ i + 1 ] )
					continue;
				if ( ips[ i + 2 ] != -1 && ip[ 2 ] != ips[ i + 2 ] )
					continue;
				if ( ips[ i + 3 ] != -1 && ip[ 3 ] != ips[ i + 3 ] )
					continue;

				result = true;
				break;
			}
		}
		return result;
	}

	public void abortAfterForward() throws cfmAbortException {
		bForwardRedirect = true;
		throw new cfmAbortException();
	}

	public boolean isWindowsOrMacUser() {
		String userAgent = REQ.getHeader("User-Agent");
		if ( userAgent != null ) {
			return ((userAgent.indexOf("Windows") >= 0) || (userAgent.indexOf("Macintosh") >= 0));
		}
		return true; // more likely to be Windows than not
	}

	public int getBytesSent() {
		return (int) totalPageOut;
	}

	
	/**
	 * TODO: I *think* there is a bug here, that if there a mapping is defined in the Application.cfc then
	 * we are missing the 'mappings' page that was custom.  i think it should include both thisMappings+mappings
	 * @return
	 */
	public Map<String, String> getCFMappings() {
		if ( dataBin.size() == 0 )
			return mappings;
		
		cfStructData thisMappings = (cfStructData) dataBin.get( cfAPPLICATION.MAPPINGS );
		if ( thisMappings != null && thisMappings.size() > 0 ){
			Map<String,String> allMappings = new FastMap<String,String>( thisMappings );
			Iterator it = thisMappings.keySet().iterator();
			while ( it.hasNext() ){
				String next = it.next().toString();
				if ( next.startsWith( "/" ) ){ // ignore mappings that don't start with /
					try {
						allMappings.put( next, ( (cfData) thisMappings.getData( next ) ).getString() );
					} catch (dataNotSupportedException ignored) {} // we don't need to error in this case, just ignore it
				}
			}
			return allMappings;

		}else{
			return mappings;
		}
	}

	public void setCFMapping( String logicalPath, String directoryPath ) {
		mappings.put(logicalPath, directoryPath);
	}

	public Connection getConnection( String key ) {
		return connections.get( key );
	}

	public void putConnection( String key, Connection con ) {
		connections.put( key, con );
	}
	
	public void removeConnection( String key ){
		connections.remove( key );
	}

	private void closeAllConnections() {
		Iterator<Connection> iter = connections.values().iterator();
		while ( iter.hasNext() ) {
			try {
				iter.next().close();
			} catch ( SQLException e ) {
				cfEngine.log( "Error closing connection: " + e );
			}
			iter.remove();
		}
	}

	/**
	 * Used by CFTHREAD to copy error handling data from a "parent" cfSession
	 * to the virtual cfSession used by the spawned thread.
	 */
	public void setErrorHandling( cfSession parent ) {
		// for Application.cfc onError() event handler
		this.applicationCfc = parent.applicationCfc;

		// for CFERROR processing
		Object cfErrorData = parent.getDataBin( cfERROR.DATA_BIN_KEY );
		if ( cfErrorData != null ) {
			this.setDataBin( cfERROR.DATA_BIN_KEY, cfErrorData );
		}
	}

	/***********************************************************************
	 *                                                                     *
	 *  Application.cfm, OnRequestEnd.cfm, and Application.cfc processing  *
	 *                                                                     *
	 ************************************************************************/

	public static final String APPLICATION_CFC		= "Application.cfc";
	public static final String APPLICATION_CFM		= "Application.cfm";
	public static final String ON_REQUEST_END_CFM	= "OnRequestEnd.cfm";

	private static final String ON_REQUEST_START 		= "onRequestStart";
	public  static final String ON_REQUEST 					= "onRequest";
	public  static final String ON_CFCREQUEST 			= "onCFCRequest";
	private static final String ON_REQUEST_END 			= "onRequestEnd";
	private static final String ON_MISSING_TEMPLATE = "onMissingTemplate";

	// these only matter to the top-level session, so don't copy in "copy" constructor
	private cfFile onRequestEndFile;
	private cfComponentData applicationCfc;

  	/**
  	 * Render either the Application.cfc or Application.cfm for this request.
  	 * This method never throws a file-not-found exception.
  	 */
	public void onRequestStart( cfFile requestFile ) throws cfmRunTimeException {
		pushActiveFile( requestFile );
		cfFile applicationFile = findApplicationFile( requestFile );
		if ( applicationFile != null ) {
			if ( applicationFile.getName().endsWith( "m" ) ) {
				renderApplicationCfm( applicationFile );
			} else {
				applicationFile.setComponentName( ComponentFactory.normalizeComponentName( getPresentURIPath() + "Application" ) );
				applicationCfc = new cfComponentData( this, applicationFile, false ); // false = don't allow abstract

				// initialize application, session, and client scopes (equivalent to CFAPPLICATION tag)
				// invoke the onApplicationStart() and onSessionStart() methods if appropriate
				cfAPPLICATION.getAppManager().loadApplication( applicationCfc, this );

				onRequestStart( requestFile.getURI() ); // invoke the onRequestStart method
			}
		}
	}

	/**
	 * Render the Application.cfm file.
	 */
	private void renderApplicationCfm( cfFile appFile ) throws cfmRunTimeException {
		pushActiveFile( appFile );
		try {
			appFile.render( this );
		} catch ( cfmExitException EF ) {
			// processing continues if CFEXIT appears within Application.cfm
			clearTagStack();
		}
		popActiveFile();

		try {
			// return OnRequestEnd.cfm file, or null if it doesn't exist
			if ( appFile.getCfmlURI().isRealFile() ) { // do physical path lookup
				String onRequestEndFileName = FileUtils.getOnRequestEndCfm( appFile.getPath() );
				if ( onRequestEndFileName != null ) {
					onRequestEndFile = getRealFile( onRequestEndFileName );
				}
			} else { // URI lookup for packed WARs and BDAs
				onRequestEndFile = getFile( new cfmlURI( appFile.getCfmlURI().getParentURI(), ON_REQUEST_END_CFM ) );
			}
		} catch ( cfmBadFileException BF ) {
			if ( !BF.fileNotFound() )
				throw BF;
		}
	}

	/**
	 * Render the Application.cfc and invoke the onRequestStart() method
	 */
	private void onRequestStart( String requestUri ) throws cfmRunTimeException {
		try {
			List<cfStringData> args = new ArrayList<cfStringData>();
			args.add( new cfStringData( requestUri ) ); // targetPage

			cfcMethodData methodData = new cfcMethodData( this, ON_REQUEST_START, args );
			cfData returnCode = applicationCfc.invokeApplicationFunction( this, methodData );
			if ( ( returnCode != null ) && ( returnCode != CFUndefinedValue.UNDEFINED ) && ( returnCode.getDataType() != cfData.CFNULLDATA ) &&	!returnCode.getBoolean() ) {// returned false
				abortPageProcessing();
			}
		}  catch ( cfmRunTimeException e ) {
			invokeOnError( applicationCfc, e, ON_REQUEST_START );
			abortPageProcessing();
		}
	}

	/**
	 * Find Application.cfc or Application.cfm; return null if not found.
	 */
	private cfFile findApplicationFile( cfFile requestFile ) throws cfmBadFileException {
		String realPath = requestFile.getPath(); // null if BDA or packed WAR
		if ( realPath != null ) {
			// physical file path search
			String appFilePath = FileUtils.findApplicationFile( realPath );
			return ( appFilePath == null ? null : getRealFile( appFilePath ) );
		}

		// URI path search for packed WARs and BDAs
		return findApplicationFile( requestFile.getURI() );
	}

	private cfFile findApplicationFile( String uriPath ) throws cfmBadFileException {
		cfFile appFile = null;

		do {
			int c1 = uriPath.lastIndexOf( "/" );
			if ( c1 == -1 ) {
				return null;
			}
			uriPath = uriPath.substring( 0, c1 );
			appFile = getApplicationFile( uriPath );
		} while ( appFile == null );

		return appFile;
	}

	private cfFile getApplicationFile( String uriPath ) throws cfmBadFileException {
		try {
			return getUriFile( uriPath + "/" + APPLICATION_CFC );
		} catch ( cfmBadFileException BFE ) {
			if ( !BFE.fileNotFound() )
				throw BFE;
		}

		try {
			return getUriFile( uriPath + "/" + APPLICATION_CFM );
		} catch ( cfmBadFileException BFE ) {
			if ( !BFE.fileNotFound() )
				throw BFE;
		}

		return null;
	}
	

	public void onRequest( cfFile requestFile ) throws cfmRunTimeException {
		try {
			// Does the ApplicationCFC exist
			if ( applicationCfc != null ) {
				List<cfStringData> args = new ArrayList<cfStringData>();
				args.add( new cfStringData( requestFile.getURI() ) );
		
				cfcMethodData methodData = new cfcMethodData( this, ON_REQUEST, args );
				cfData returnCode = applicationCfc.invokeApplicationFunction( this, methodData );
				if ( returnCode != null )
					return;
			}
			
			requestFile.render( this );
			
		} catch ( cfmRunTimeException e ) {
			invokeOnError( applicationCfc, e, applicationCfc == null ? e.getMessage() : ON_REQUEST );
			abortPageProcessing();
		}
	}

	
	public void onCFCRequest( String uri, String compName, String methodName, cfArgStructData arguments, String formatMethod, boolean serializeQueryByColumns, String jsonpCallback, String jsonCase  ) throws cfmRunTimeException {
		try {
			cfData rsp;
			cfComponentData comp;
			cfcMethodData invocationData;
			
			// Does the ApplicationCFC exist
			if ( applicationCfc != null && applicationCfc.isMethodAvailable(ON_CFCREQUEST) ){
				
				// Little clean up of the uri which would come in as /a/b/c/d.cfc
				int pos = uri.lastIndexOf(".");
				if ( pos != -1 )	uri	= uri.substring(0,pos);
				uri	= uri.replace('/', '.');
				if ( uri.charAt(0) == '.' )	uri = uri.substring(1);

				// Remove the method from the original one
				arguments.removeData("method");

				// Package the arguments
				cfArgStructData appcfcArguments = new cfArgStructData();
				appcfcArguments.setData("cfcname", 		new cfStringData(uri) );
				appcfcArguments.setData("cfcmethod", 	new cfStringData(methodName) );
				appcfcArguments.setData("args", 			arguments );

				invocationData = new cfcMethodData(this, ON_CFCREQUEST, appcfcArguments);
				comp	= applicationCfc;

			}else{
				
				// Invoke the original method
				comp = new cfComponentData(this, compName );
				invocationData = new cfcMethodData(this, methodName, arguments);
				if (!comp.isRemoteable(invocationData)){
					cfCatchData catchData = new cfCatchData();
					catchData.setMessage("an invalid request was attempted");
					throw new cfmRunTimeException(catchData);
				}
			}

			// Invoke the component
			rsp = comp.invokeComponentFunction( this, invocationData);
			
			if (rsp.getDataType() != cfData.CFNULLDATA) {
				boolean processingCfOutput = setProcessingCfOutput(true);
      	resetBuffer();
			  write( invocationData.formatReturnData( this, comp, rsp, formatMethod, serializeQueryByColumns, jsonpCallback, jsonCase ) );
				setProcessingCfOutput(processingCfOutput);
			}
			
		} catch ( cfmRunTimeException e ) {
			invokeOnError( applicationCfc, e, applicationCfc == null ? e.getMessage() : ON_CFCREQUEST );
			abortPageProcessing();
		}
	}

	
	public void onError( cfmRunTimeException e, String eventHandler ) throws cfmRunTimeException {
		invokeOnError( applicationCfc, e, eventHandler );
	}

	public void invokeOnError( cfComponentData _applicationCfc, cfmRunTimeException e, String eventHandler ) throws cfmRunTimeException {
		if ( _applicationCfc == null ) {
			throw e;
		}

		write( e.getOutput() ); //this is part of the fix for NA bug #3282

		List<cfJavaObjectData> args = new ArrayList<cfJavaObjectData>();
		args.add( catchDataFactory.eventHandlerException( eventHandler, e.getCatchData() ) ); // Exception
		args.add( new cfStringData( eventHandler ) ); // EventName

		cfcMethodData methodData = new cfcMethodData( this, "onError", args );
		cfData returnCode = _applicationCfc.invokeApplicationFunction( this, methodData );
		if ( returnCode == null ) { // onError not implemented
			throw e;
		}
	}

	public void onRequestEnd( String requestUri ) throws cfmRunTimeException {
		if ( onRequestEndFile != null ) {
			renderOnRequestEnd();
		} else if ( applicationCfc != null ) {
			invokeOnRequestEnd( requestUri );
		}
		popActiveFile(); // pop the request file
	}

	private void renderOnRequestEnd() throws cfmRunTimeException {
		pushActiveFile( onRequestEndFile );
		onRequestEndFile.render( this );
		popActiveFile();
	}

	private void invokeOnRequestEnd( String requestUri ) throws cfmRunTimeException {
		try {
			List<cfStringData> args = new ArrayList<cfStringData>();
			args.add( new cfStringData( requestUri ) ); // targetPage

			cfcMethodData methodData = new cfcMethodData( this, ON_REQUEST_END, args );
			applicationCfc.invokeApplicationFunction( this, methodData );
		} catch ( cfmRunTimeException e ) {
			invokeOnError( applicationCfc, e, ON_REQUEST_END );
		}
	}

	public boolean onMissingTemplate() throws cfmRunTimeException {
		String requestUri = getRequestURI();
		cfFile applicationFile = findApplicationFile( requestUri );
		if ( applicationFile != null ) {
			if ( applicationFile.getName().endsWith( "m" ) ) {
				renderApplicationCfm( applicationFile );
				return false; // Application.cfm must CFABORT, or file-not-found exception will be rethrown
			} else {
				applicationFile.setComponentName( ComponentFactory.normalizeComponentName( getPresentURIPath() + "Application" ) );
				applicationCfc = new cfComponentData( this, applicationFile, false ); // false = don't allow abstract

				cfAPPLICATION.getAppManager().loadApplication( applicationCfc, this );
				
				List<cfStringData> args = new ArrayList<cfStringData>();
				args.add( new cfStringData( requestUri ) ); // targetPage

				cfcMethodData methodData = new cfcMethodData( this, ON_MISSING_TEMPLATE, args );
				cfData returnCode = applicationCfc.invokeApplicationFunction( this, methodData );
				if ( returnCode == null ) { // onMissingTemplate not implemented
					return false;
				} else if ( returnCode.getDataType() == cfData.CFNULLDATA ) { // returned null
					return true;
				} else {
					return returnCode.getBoolean(); // returned boolean
				}
			}
		}
		return false;
	}

	
	/**
	 * Used to track how long it took to render each query
	 * @param ms
	 */
	public void metricQueryTimeAdd(long ms){
		topLevelSession.metricTotalQuery++;
		topLevelSession.metricTotalQueryTime += ms;
	}
	
	/**
	 * Returns the total queries ran
	 * @return
	 */
	public int getMetricQuery(){
		return topLevelSession.metricTotalQuery;
	}
	
	/**
	 * Returns the total time in ms that querys took
	 * @return
	 */
	public long getMetricQueryTotalTime(){
		return topLevelSession.metricTotalQueryTime;
	}
}
