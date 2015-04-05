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

package com.naryx.tagfusion.cfm.engine;

import java.io.PrintWriter;
import java.util.Map;

import com.naryx.tagfusion.cfm.parser.CFCall;
import com.naryx.tagfusion.cfm.parser.CFCallScope;
import com.naryx.tagfusion.cfm.parser.CFCallStack;
import com.naryx.tagfusion.cfm.parser.CFScopeStack;
import com.naryx.tagfusion.cfm.parser.cfLData;


/**
 * This class is a wrapper class that allows the caller scope
 * to perform scope searching as well as enabling you to
 * access scopes using caller.form, caller.session, etc notation.
 */


public class cfCallerData extends cfStructData implements java.io.Serializable{

  static final long serialVersionUID = 1;

  private cfSession     session;
  private cfStructData  varscope;
  
  private CFCallScope callScope;
  
  /**
   * Construct a cfCallerData that is made up of the cfStructData
   * that is the variable scope that it points to, plus the 
   * cfSession used in scope searching.  
   */
  public cfCallerData( cfSession _session, cfStructData _var ) {
    super( null );	// null because we don't use the hashdata attribute
    session   = _session;
    varscope  = _var;
    //CFCallStack stack = (CFCallStack) session.getCFContext().getCallStack();
    CFCallStack callStack = session.getCFContext().getCallStack();
    if ( !callStack.isEmpty() ){
    	CFScopeStack scopeStack = ( (CFCall) callStack.peek() ).scopeStack();
    	callScope = (CFCallScope) scopeStack.peek();
    }
  }
  
  /**
   * The 2 getData() methods are the pivotal methods in making
   * scope searching etc work
   */  
  public cfData getData(String _key) {
    
  	cfData val = null;
  	
  	// need to check the local scope first if present
  	if ( callScope != null && callScope.containsVar( _key ) ){
  		try {
				val = callScope.get( _key, false, session.getCFContext() );
				if ( val != null && val.getDataType() == cfData.CFLDATA )
		    	val = ( (cfLData) val ).Get( session.getCFContext() );
			} catch (cfmRunTimeException e) {
				// shouldn't happen since we know containsVar returns true
			}
  	}
  	
    if ( val == null ){
    	val = varscope.getData(_key); 
	   
    	if ( val == null ){
	      if ( _key.equalsIgnoreCase( variableStore.VARIABLES_SCOPE_NAME ) ){
	        return this;
	      }else if ( _key.equalsIgnoreCase( variableStore.ATTRIBUTES_SCOPE_NAME ) ){
	        return null;
	      }
	      // we want to do the scope searching on all the scopes except the variables scope (see bug #2594) 
	      val = session.getData(_key,true,false);
	    }
    }    	
    return val;
  }

  public cfData getData(cfData arrayIndex) throws cfmRunTimeException {
    return getData(arrayIndex.getString());
  }


  /**
   * The methods below just act as proxy implementations simply returning
   * the result of a call to the same method of the 
   */
  public boolean containsKey(String _key) {
    return varscope.containsKey(_key);
  }

  public Map copy() {
    return varscope.copy();
  }

  public String getKeyList(String delimiter) {
    return varscope.getKeyList(delimiter);
  }

  public void deleteData(String _key) throws cfmRunTimeException {
    varscope.deleteData(_key);
  }

  public void dump(PrintWriter out, String _label, int _top ) {
    varscope.dump(out, _label, _top );
  }
  
  public void dump(PrintWriter out) {
    varscope.dump(out);
  }

  public void dumpWDDX(int version, PrintWriter out) {
    varscope.dumpWDDX(version,out);
  }

  public cfData duplicate() {
    return varscope.duplicate();
  }

  public byte getDataType() {
    return varscope.getDataType();
  }
  
  public String getDataTypeName() {
	return varscope.getDataTypeName();
  }

  public cfArrayData getKeyArray() throws cfmRunTimeException {
    return varscope.getKeyArray();
  }

  public boolean isEmpty() {
    return varscope.isEmpty();
  }

  public Object[] keys() {
    return varscope.keys();
  }

  public void clear() {
    varscope.clear();
  }

  public void setData(cfData _key, cfData _data) throws cfmRunTimeException {
  	if ( callScope != null && callScope.containsVar( _key.getString() ) ){
			callScope.put( _key.toString(), _data, session.getCFContext() );
  	}else{
  		varscope.setData(_key, _data);
  	}
  }

  public void setData(String _key, cfData _data) {
  	if ( callScope != null && callScope.containsVar( _key ) ){
			callScope.put( _key, _data, session.getCFContext() );
  	}else{
  		varscope.setData(_key, _data);
  	}
  }

  public int size() {
    return varscope.size();
  }

  public String toString() {
    return varscope.toString();
  }

}
