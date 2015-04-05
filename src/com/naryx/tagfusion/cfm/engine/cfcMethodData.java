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
 *  $Id: cfcMethodData.java 2398 2013-07-28 21:19:27Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.tag.cfFUNCTION;
import com.naryx.tagfusion.cfm.wddx.cfWDDX;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.expression.function.string.serializejson;


public class cfcMethodData implements javaMethodDataInterface {
	static final long serialVersionUID = 1;
	
	private String _name = null;
	
	private List<? extends cfData> _incomingArgumentValues = null;
	private List<cfData> _evaluatedArguments = null;
	
	private cfArgStructData _namedArguments = null;
	
	private boolean _hasNamedArguments = false;
	private boolean _onMissingMethod = false;
	
	public boolean isOnMethodMissing(){
		return _onMissingMethod;
	}
	
	public void setOnMethodMissing(){
		_onMissingMethod = true;
	}

	
	public cfcMethodData(cfSession session, String name) throws cfmRunTimeException {
		this( session, name, (List<cfData>)null );
	}
	
	public cfcMethodData( cfSession session, String name, List<? extends cfData> arguments ) throws cfmRunTimeException	{
		_name = name;
		_incomingArgumentValues = arguments;
		_evaluatedArguments = new ArrayList<cfData>();
		
		_namedArguments = new cfArgStructData();
		
		// build argument structure
		if ( arguments != null ) {
			cfData evaluated = null;
			for( int idx = 0; idx < arguments.size(); idx++ ) {
				evaluated = arguments.get(idx);
				_evaluatedArguments.add( evaluated );
				_namedArguments.setData( idx, evaluated );
			}
		}
	}
	
	public cfcMethodData(cfSession session, String name, cfArgStructData arguments){
		_name = name;
		
		Object[] keys 					= arguments.keys();
		_evaluatedArguments 		= new ArrayList<cfData>(keys.length);
		_incomingArgumentValues = new ArrayList<cfData>(keys.length);
		_namedArguments 				= arguments;
		
		for ( int i = 0; i < keys.length; i++ ){
			_evaluatedArguments.add(_namedArguments.getData( keys[i].toString() ));
		}
		
		_hasNamedArguments = true;	
	}
	
	public String getFunctionName(){
		return _name;
	}
	
	public List<? extends cfData> getArguments(){
		return _incomingArgumentValues;
	}
	
	public List<cfData> getEvaluatedArguments( CFContext _context, boolean cfcMethod ){
		return _evaluatedArguments;
	}
	
	public boolean hasNamedArguments(){
		return _hasNamedArguments;
	}
	
	public cfArgStructData getNamedArguments(){
		return _namedArguments;
	}

	
	/*
	 * Used primarily by remote callers to format the outgoing data
	 */
	public String formatReturnData(cfSession session, cfComponentData comp, cfData rsp, String formatMethod, boolean serializeQueryByColumns, String jsonpCallback, String jsonCase ) throws cfmRunTimeException	 {
		cfFUNCTION.ReturnFormat returnFormat	= ( formatMethod == null ) ? comp.getReturnFormat(this) : cfFUNCTION.getReturnFormat(formatMethod);
		
		if ( returnFormat == null || returnFormat == cfFUNCTION.ReturnFormat.WDDX ){
	  	
			if ( rsp instanceof cfXmlData )
	  		return ( (cfXmlData)rsp ).getString();
	  	else
			  return cfWDDX.cfml2Wddx( rsp );
	  	
		}else if ( returnFormat == cfFUNCTION.ReturnFormat.JSON ){

			StringBuilder buffer = new StringBuilder( 5000 );
			String jsonDate	= ( formatMethod == null ) ? comp.getReturnJSONDate(this) : cfEngine.DefaultJSONReturnDate;
			
			if (jsonCase == null)
				jsonCase	= comp.getReturnJSONCase(this);
			
			serializejson sj = new serializejson();
			sj.encodeJSON(buffer, rsp, serializeQueryByColumns, serializejson.getCaseType(jsonCase), serializejson.getDateType(jsonDate) );
			
			cfApplicationData appData = session.getApplicationData();
			boolean secureJson = false;
			if ( appData != null && appData.containsKey("securejson") ){
				secureJson = appData.getData("securejson").getBoolean();
			}else{
				secureJson = comp.isSecureJSon(this);
			}

			if ( secureJson ){
				/* Do we need to prefix this JSON string with anything */
				if ( appData != null && appData.containsKey("securejsonprefix") ){
					buffer.insert( 0, appData.getData("securejsonprefix").getString() );
				}else{
					buffer.insert( 0, "//");
				}
			}

			return buffer.toString();

		}else if ( returnFormat == cfFUNCTION.ReturnFormat.JSONP ){

			StringBuilder buffer = new StringBuilder( 5000 );
			String jsonDate	= ( formatMethod == null ) ? comp.getReturnJSONDate(this) : cfEngine.DefaultJSONReturnDate;
			if (jsonCase == null)
				jsonCase	= comp.getReturnJSONCase(this);
			
			serializejson sj = new serializejson();
			sj.encodeJSON(buffer, rsp, serializeQueryByColumns, serializejson.getCaseType(jsonCase), serializejson.getDateType(jsonDate) );

			buffer.insert(0, jsonpCallback + "(");
			buffer.append( ")" );
			
			return buffer.toString();
			
		}else{
			if ( rsp instanceof cfXmlData )
	  		return ( (cfXmlData)rsp ).getString();
			else
				return rsp.getString();
		}
	}
}