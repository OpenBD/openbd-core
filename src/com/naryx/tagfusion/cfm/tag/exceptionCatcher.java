/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  $Id: exceptionCatcher.java 2390 2013-06-23 16:34:12Z alan $
 */
package com.naryx.tagfusion.cfm.tag;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.aw20.collections.FastStack;

import com.naryx.tagfusion.cfm.engine.cfCatchClause;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfErrorData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.CFCallStack;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.util.debugRecorder;

/*
 * Handles the catching of exceptions for cftry and try (in cfscript) 
 */
public class exceptionCatcher extends Object {
  
  private cfTag parentTag;
  private List<cfCatchClause> catchList;

  private transient debugRecorder recorderSnap;
  
  private transient FastStack<cfFile> filesSnap;
  private transient FastStack<cfTag> tagSnap;
  private transient FastStack<cfComponentData> componentDataSnap;
  private transient FastStack<cfFUNCTION> componentTagSnap;

  private transient FastStack<cfStructData> superSnap;
  private transient FastStack<cfStructData> callerSnap;
  private transient FastStack<cfStructData> variableSnap;
  private transient FastStack<Stack<cfQueryResultData>> querySnap;
  
  private transient CFCallStack callStack;
  private transient Stack<CFCallStack> callStackStack;
  
  
  public exceptionCatcher( cfTag _parent, List<cfCatchClause> _catch ){
    parentTag = _parent;
    catchList = _catch;
  }
  
  
  public cfCatchClause processRuntimeException( cfSession _Session, cfFile activeFile, userDefinedFunction _activeUDF, cfmRunTimeException rte) throws cfmRunTimeException {
    // keep a note of the active file where the exception originates from
    cfFile activeF 	= _Session.activeFile();
    cfTag activeT 	= _Session.activeTag();
    
    cfCatchClause handler = getMatchingCatch( _Session, rte.getCatchData() );
    if ( handler == null )
      throw rte;
    
    prepareSession( _Session, activeFile, _activeUDF );
    
    // exception was caught
    if ( !rte.isRethrow() ) // don't record rethrown exception twice
    	_Session.recordException(rte, activeF, activeT);
    
    return handler;
  }
  

  private void prepareSession( cfSession _Session, cfFile tryActiveFile, userDefinedFunction _activeUDF ){
    saveSessionSnapshot(_Session);

    // Roll back the active file
    while (_Session.activeFile() != tryActiveFile)
      _Session.popActiveFile();

    
    // Roll back the tag, variable, and component stacks
    cfTag activeTag;
    while ((activeTag = _Session.activeTag()) != parentTag) {
      _Session.popTag();

      if (activeTag instanceof cfFUNCTION) {
      	
        if (( (cfFUNCTION)activeTag).isRunning() )
          _Session.leaveUDF();

        if (_Session.getActiveComponentTag() == activeTag )
          _Session.popComponentData();
        
      }  else if (activeTag instanceof cfSCRIPT &&
      		!(activeTag.parentTag instanceof cfFUNCTION)
      		&& !_Session.isUserDefinedFunctionEmpty() ) {

      	/* 
      	 * If this cfscript block is the actual function, then we want to unwrap it; otherwise we leave it alone.
      	 * We can tell this situation if the parent tag of the activeTag is a cffunction
      	 */
     		_Session.leaveUDF();
     		_Session.popComponentData();
     		_Session.popUserDefinedFunction();
      	 
      } else if (activeTag instanceof cfMODULE) {
        
      	if (((cfMODULE) activeTag).isRendering())
          _Session.leaveCustomTag();
        
      } else if ( activeTag instanceof cfCOMPONENT ) {
    	  // rendering CFC pseudo-constructor
    	  _Session.leaveUDF();
    	  _Session.popComponentData();
      }
    }
    
    
    
    /**
     * [Feb13] 
     * We still may be out of alignment.  The active UDF may not be correct if the exception was thrown in another UDF
     * inside the same CFC.  This would cause the variable stack to be completely out of sync.  We want to roll back to 
     * the current activeUDF
     */
    if ( _activeUDF == null )
    	return;
    
    userDefinedFunction peekUDF = _Session.peekUserDefinedFunction();
    
    while ( peekUDF != null && _activeUDF != peekUDF ){
    	_Session.leaveUDF();
   		_Session.popUserDefinedFunction();

   		peekUDF = _Session.peekUserDefinedFunction();
    }
  }

  
  
  /**
   * returns true if the error was handled by a cfcatch
   * 
   * @param _Session
   * @param catchData
   * @return
   * @throws cfmRunTimeException
   */
  private cfCatchClause getMatchingCatch( cfSession _Session, cfCatchData catchData ) throws cfmRunTimeException {
  
    cfData DD = catchData.getData("type");
    String type = "Application";
    if (DD != null)
      type = DD.getString();

    // Before we do this, we need to see if there is any MONITOR CFERROR
    cfErrorData errorData = (cfErrorData) _Session.getDataBin( cfERROR.DATA_BIN_KEY );
    if (errorData != null)
      errorData.handleMonitorError(_Session, catchData);
    

    // figure out which CFCATCH tag to render
    cfData javaexc = catchData.getData("javaexception");
    boolean isJavaExc = ( javaexc != null );
    Iterator<cfCatchClause> iter = catchList.iterator();
    while (iter.hasNext()) {
      cfCatchClause clause = iter.next();

      // note: must check custom type only if javaexception is null else 
      // a custom type "java.lang" would catch "java.lang.Exception" which is wrong
      if ( ( isJavaExc && clause.checkException(javaexc.getString() ) )
                || ( isJavaExc && clause.checkType("object") )
                || clause.checkType(type)
                || ( !isJavaExc && clause.checkCustomType(type) ) ){
        return clause; 
      }
    }

    return null;
  }

  
  private void saveSessionSnapshot(cfSession _Session) {
    recorderSnap 			= _Session.getDebugRecorder().copy();
    filesSnap 				= _Session.getFileStack();
    tagSnap 					= _Session.getTagStack();
    componentDataSnap = _Session.getComponentDataStack();
    componentTagSnap 	= _Session.getComponentTagStack();

    superSnap 			= _Session.getSuperStack();
    callerSnap 			= _Session.getCallerStack();
    variableSnap 		= _Session.getVariableStack();
    querySnap 			= _Session.getQueryStack();
    
    callStack 			= _Session.getCFCallStack();
    callStackStack 	= _Session.getCallStackStack();
  }


  public void restoreSessionSnapshot(cfSession _Session) {
    _Session.registerDebugRecorder( recorderSnap );
    _Session.setFileStack(filesSnap);
    _Session.setTagStack(tagSnap);
    _Session.setComponentDataStack(componentDataSnap);
    _Session.setComponentTagStack(componentTagSnap);

    _Session.setSuperStack(superSnap);
    _Session.setCallerStack(callerSnap);
    _Session.setVariableStack(variableSnap);
    _Session.setQueryStack(querySnap);
    
    _Session.setCFCallStack( callStack );
    _Session.setCallStackStack( callStackStack );
    
  }

}