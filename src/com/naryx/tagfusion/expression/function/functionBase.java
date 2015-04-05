/* 
 *  Copyright (C) 2000 - 2015 aw2.0Ltd
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
 *  README.txt @ http://openbd.org/license/README.txt
 *  
 *  http://openbd.org/
 *  $Id: AmazonBase.java 2466 2015-01-11 15:53:09Z alan $
 */

package com.naryx.tagfusion.expression.function;

/**
 * A simple base class that allows for the checking of the
 * function for errors
 */
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFUndefinedValue;
import com.naryx.tagfusion.expression.operator.expressionBase;

public class functionBase extends expressionBase  {
  private static final long serialVersionUID = 1L;
  private static List<String>	emptyFormals = new ArrayList<String>(0);
  
  public enum ReturnType {
  	ARRAY,
  	BINARY,
  	BOOLEAN,
  	CFC,
  	DATE,
  	NULL,
  	NUMERIC,
  	IMAGE,
  	OBJECT,
  	QUERY,
  	STRING,
  	STRUCTURE,
  	SPREADSHEET,
  	THREAD,
  	XML,
  	UNKNOWN};
  
  protected int min = 0, max = 0;
  protected List<String> namedParams = emptyFormals;
  protected Map<String,Integer> namedParamsIndex = null; 
  
  public functionBase(){}
  
  public int 	getMin(){ return min; }
  public int 	getMax(){ return max; }
  
  public boolean supportNamedParams(){ return (namedParams != emptyFormals); }
  
  public List<String> getFormals() {
  	return namedParams;
	}
  
  protected void setNamedParams( String[] params ){
  	namedParams	= new ArrayList<String>( params.length );
  	namedParamsIndex = new HashMap<String,Integer>();
  	for ( int x=0; x < params.length; x++ ){
  		namedParams.add( params[x] );
  		namedParamsIndex.put( params[x], x );
  	}
  }
  
  protected cfData getNamedParam( cfArgStructData argStruct, String namedParam ){
  	cfData v;
  	
  	if ( argStruct.isNamedBased() ){
  		v	= argStruct.getData(namedParam);
  	}else{
  		v = argStruct.getData( namedParamsIndex.get(namedParam) );
  	}
  	return v;
  }
  
  protected cfData getNamedParam( cfArgStructData argStruct, String namedParam, cfData def ){
  	cfData v;
  	
  	if ( argStruct.isNamedBased() ){
  		v	= argStruct.getData(namedParam);
  	}else{
  		v = argStruct.getData( namedParamsIndex.get(namedParam) );
  	}
  	
  	if ( v == null || v instanceof CFUndefinedValue )
  		return def;
  	else  	
  		return v;
  }
  
  protected String getNamedStringParam( cfArgStructData argStruct, String namedParam, String def ) throws cfmRunTimeException {
  	cfData v;
  	
  	if ( argStruct.isNamedBased() ){
  		v	= argStruct.getData(namedParam);
  	}else{
  		v = argStruct.getData( namedParamsIndex.get(namedParam) );
  	}
  	
  	if ( v == null || v instanceof CFUndefinedValue )
  		return def;
  	else  	
  		return v.getString();
  }
  
  protected int getNamedIntParam( cfArgStructData argStruct, String namedParam, int def ) throws cfmRunTimeException {
  	cfData v;
  	
  	if ( argStruct.isNamedBased() ){
  		v	= argStruct.getData(namedParam);
  	}else{
  		v = argStruct.getData( namedParamsIndex.get(namedParam) );
  	}
  	
  	if ( v == null || v instanceof CFUndefinedValue )
  		return def;
  	else  	
  		return v.getInt();
  }
  
  protected long getNamedLongParam( cfArgStructData argStruct, String namedParam, long def ) throws cfmRunTimeException {
  	cfData v;
  	
  	if ( argStruct.isNamedBased() ){
  		v	= argStruct.getData(namedParam);
  	}else{
  		v = argStruct.getData( namedParamsIndex.get(namedParam) );
  	}
  	
  	if ( v == null || v instanceof CFUndefinedValue )
  		return def;
  	else  	
  		return v.getLong();
  }
  
  protected double getNamedDoubleParam( cfArgStructData argStruct, String namedParam, long def ) throws cfmRunTimeException {
  	cfData v;
  	
  	if ( argStruct.isNamedBased() ){
  		v	= argStruct.getData(namedParam);
  	}else{
  		v = argStruct.getData( namedParamsIndex.get(namedParam) );
  	}
  	
  	if ( v == null || v instanceof CFUndefinedValue )
  		return def;
  	else  	
  		return v.getDouble();
  }
  
  protected cfStructData getNamedStructParam( cfSession session, cfArgStructData argStruct, String namedParam, cfStructData def ) throws cfmRunTimeException {
  	cfData v;
  	
  	if ( argStruct.isNamedBased() ){
  		v	= argStruct.getData(namedParam);
  	}else{
  		v = argStruct.getData( namedParamsIndex.get(namedParam) );
  	}
  	
  	if ( v == null || v instanceof CFUndefinedValue ){
  		return def;
  	}else if ( v instanceof cfStructData ){ 	
  		return (cfStructData)v;
  	}else{
  		throwException(session, namedParam + " is not of structure type");
  		return null;
  	}
  }
  
  protected cfArrayData getNamedArrayParam( cfSession session, cfArgStructData argStruct, String namedParam, cfArrayData def ) throws cfmRunTimeException {
  	cfData v;
  	
  	if ( argStruct.isNamedBased() ){
  		v	= argStruct.getData(namedParam);
  	}else{
  		v = argStruct.getData( namedParamsIndex.get(namedParam) );
  	}
  	
  	if ( v == null || v instanceof CFUndefinedValue ){
  		return def;
  	}else if ( v instanceof cfArrayData ){ 	
  		return (cfArrayData)v;
  	}else{
  		throwException(session, namedParam + " is not of array type");
  		return null;
  	}
  }
  
  protected boolean getNamedBooleanParam( cfArgStructData argStruct, String namedParam, boolean def ) throws cfmRunTimeException {
  	cfData v;
  	
  	if ( argStruct.isNamedBased() ){
  		v	= argStruct.getData(namedParam);
  	}else{
  		v = argStruct.getData( namedParamsIndex.get(namedParam) );
  	}
  	
  	if ( v == null || v instanceof CFUndefinedValue ){
  		return def;
  	}else{  	
  		return v.getBoolean();
  	}
  }

  
  
  public List<cfStructData> getFormalArguments() { return null; }
  
  protected int	getIntParam( List<cfData> params, int index, int def ) throws cfmRunTimeException {
  	if ( index < params.size() )
  		return params.get(index).getInt();
  	else
  		return def;
  }
  
  protected String getStringParam( List<cfData> params, int index, String def ) throws cfmRunTimeException {
  	if ( index < params.size() )
  		return params.get(index).getString();
  	else
  		return def;
  }
  
  protected boolean	getBooleanParam( List<cfData> params, int index, boolean def ) throws cfmRunTimeException {
  	if ( index < params.size() )
  		return params.get(index).getBoolean();
  	else
  		return def;
  }
  
  
  /*
   * Meta Information:
   * 
   * Returns back String array, of short descriptions for each of the 
   * parameters
   */
	public String[] getParamInfo(){
		return new String[ max ];
	}

	
	/*
	 * Meta Information:
	 * 
	 * Returns a MAP containing meta data for this function
	 */
	public java.util.Map<String,String> getInfo(){
		return makeInfo( "unknown", "not available", ReturnType.UNKNOWN );
	}
	
	
	/*
	 * Helper method for quickly creating the meta information
	 */
	protected	java.util.Map<String,String> makeInfo( String category, String summary, ReturnType returntype ){
		java.util.HashMap<String,String> map = new java.util.HashMap<String,String>();
		
		map.put("category", category.toLowerCase() );
		map.put("summary", 	summary );
		map.put("return", 	returntype.toString() );
		
		return map;
	}
	
	
	public boolean isEscapeSingleQuotes() {
		return true;
	}	
}
