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
 *  $Id: DeserializeJSONJackson.java 2496 2015-02-01 02:19:29Z alan $
 */

package com.naryx.tagfusion.expression.function.string;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.core.JsonToken;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;
import com.naryx.tagfusion.expression.function.ParseDateTime;
import com.naryx.tagfusion.expression.function.functionBase;

/*
 * Takes in a JSON String and converts it to a cfData
 * 
 */
public class DeserializeJSONJackson extends functionBase {

	private static final long serialVersionUID = 1L;

	public DeserializeJSONJackson() {
		min = 1;
		max = 3;
		setNamedParams( new String[]{ "jsonstring", "strictmapping", "file"  } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"JSON string. Cannot be used at the same time as file",
			"Flag to determine if CFML Query objects should be recognized and converted to a Query object; defaults to true",
			"path to the file to read instead of using the parameter 'jsonstring'; can be any handle to a virtual file system"
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"conversion", 
				"Decodes the given JSON string into a CFML object", 
				ReturnType.OBJECT );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		JsonFactory f = new JsonFactory();
		f.configure(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
		f.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		f.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true );
		
		cfVFSData fileObj = null;
		JsonParser jp;
		try {

			String file	= getNamedStringParam( argStruct, "file", null );
			if ( file != null ){
				fileObj	= new cfVFSData( file, "read", null );
				jp = f.createParser(fileObj.getReader());
			}else{
				String jsonString = getNamedStringParam( argStruct, "jsonstring","");
				if ( jsonString.isEmpty() )
					return cfStringData.EMPTY_STRING;
				
				jp = f.createParser(jsonString);
			}

			boolean bStrictMapping = getNamedBooleanParam( argStruct, "strictmapping", true ) ;
			cfData obj = cfBooleanData.TRUE;
			JsonToken	token	= jp.nextToken();
			
			if ( token == JsonToken.START_OBJECT ){
				obj	= parseObject( jp, new cfStructData(), bStrictMapping );
			}else if ( token == JsonToken.START_ARRAY ){
				obj	= parseArray( jp, cfArrayData.createArray(1), bStrictMapping );
			}else if ( token == JsonToken.VALUE_NUMBER_INT ){
				if ( jp.getNumberType() == NumberType.INT )
					obj	=  new cfNumberData(jp.getIntValue());
				else if ( jp.getNumberType() == NumberType.LONG || jp.getNumberType() == NumberType.BIG_INTEGER )
					obj	=  new cfNumberData(jp.getLongValue());
			}else if ( token == JsonToken.VALUE_NUMBER_FLOAT ){
				if ( jp.getNumberType() == NumberType.FLOAT )
					obj	= new cfNumberData(jp.getFloatValue());
				else if ( jp.getNumberType() == NumberType.DOUBLE )
					obj	= new cfNumberData(jp.getDoubleValue());
			} else if ( token == JsonToken.VALUE_FALSE ){
				obj	=  cfBooleanData.FALSE;
			} else if ( token == JsonToken.VALUE_TRUE ){
				obj	= cfBooleanData.TRUE;
			} else if ( token == JsonToken.VALUE_NULL ){
				obj	= cfNullData.NULL;
			}else{
				obj = getString(jp.getText());
			}

			return obj;
		} catch (JsonParseException e) {
			throwException(_session, "[Invalid JSON] Line=" + e.getLocation().getLineNr() + "; Column=" + e.getLocation().getColumnNr() + "; Offset=" + e.getLocation().getCharOffset() );
		} catch (Exception e) {
			throwException(_session, e.getMessage() );
		} finally {
			if ( fileObj != null )
				try {
					fileObj.close();
				} catch (Exception e) {}
		}

		return null;
	}
	
	
	/**
	 * Determines if this is a query object, if so, converts it to a query one
	 * 
	 * Always has a data/columns with an optional rowcount to flag a different setup
	 * 
	 * @param obj
	 * @return
	 * @throws cfmRunTimeException 
	 */
	private cfData convertToQuery(cfStructData obj) throws cfmRunTimeException {
		if ( obj.containsKey("columns") && obj.containsKey("data" ) ){
			cfData	columns	= obj.getData("columns");
			if ( columns.getDataType() != cfData.CFARRAYDATA )	// columns is always an array
				return obj;
			
			cfData	data		= obj.getData("data");
			
			if ( obj.containsKey("rowcount") ){
				if ( data.getDataType() != cfData.CFSTRUCTDATA )
					return obj;
				else
					return new cfQueryDataFromJSon( (cfArrayData)columns, (cfStructData)data, obj.getData("rowcount").getInt() );
			}else{
				if ( data.getDataType() != cfData.CFARRAYDATA )
					return obj;
				else
					return new cfQueryDataFromJSon( (cfArrayData)columns, (cfArrayData)data );
			}
			
		}else
			return obj;
	}

	

	private cfData	parseObject(JsonParser jp, cfStructData struct, boolean bStrictMapping ) throws JsonParseException, IOException, cfmRunTimeException{
		
		JsonToken	token	= jp.nextToken();
		while ( token != JsonToken.END_OBJECT ){
			String namefield = jp.getCurrentName();
			
			if ( token == JsonToken.START_ARRAY ){
				struct.setData( namefield, parseArray(jp, cfArrayData.createArray(1), bStrictMapping) );
			}else if ( token == JsonToken.START_OBJECT ){
				struct.setData( namefield, parseObject(jp,new cfStructData(), bStrictMapping) );
			}else if ( token == JsonToken.VALUE_NUMBER_INT ){

				if ( jp.getNumberType() == NumberType.INT )
					struct.setData( namefield, new cfNumberData(jp.getIntValue()) );
				else if ( jp.getNumberType() == NumberType.LONG || jp.getNumberType() == NumberType.BIG_INTEGER )
					struct.setData( namefield, new cfNumberData(jp.getLongValue()) );

			}else if ( token == JsonToken.VALUE_NUMBER_FLOAT ){

				if ( jp.getNumberType() == NumberType.FLOAT )
					struct.setData( namefield, new cfNumberData(jp.getFloatValue()) );
				else if ( jp.getNumberType() == NumberType.DOUBLE )
					struct.setData( namefield, new cfNumberData(jp.getDoubleValue()) );

			} else if ( token == JsonToken.VALUE_FALSE ){
				struct.setData( namefield, cfBooleanData.FALSE );
			} else if ( token == JsonToken.VALUE_TRUE ){
				struct.setData( namefield, cfBooleanData.TRUE );
			} else if ( token == JsonToken.VALUE_NULL ){
				struct.setData( namefield, cfNullData.NULL );
			}else{
				struct.setData( namefield, getString(jp.getText()) );
			}

			token	= jp.nextToken();
		}

		if ( bStrictMapping )
			return convertToQuery( struct );
		else
			return struct;
	}
	
	
	private cfData	parseArray(JsonParser jp, cfArrayData array, boolean bStrictMapping ) throws JsonParseException, IOException, cfmRunTimeException{
		
		JsonToken	token	= jp.nextToken();
		while ( token != JsonToken.END_ARRAY ){
			
			if ( token == JsonToken.START_ARRAY ){
				array.addElement( parseArray(jp,cfArrayData.createArray(1),bStrictMapping ) );
			}else if ( token == JsonToken.START_OBJECT ){
				array.addElement( parseObject(jp,new cfStructData(),bStrictMapping) );
			}else if ( token == JsonToken.VALUE_NUMBER_INT ){
				
				if ( jp.getNumberType() == NumberType.INT )
					array.addElement( new cfNumberData(jp.getIntValue()) );
				else if ( jp.getNumberType() == NumberType.LONG || jp.getNumberType() == NumberType.BIG_INTEGER )
					array.addElement( new cfNumberData(jp.getLongValue()) );

			}else if ( token == JsonToken.VALUE_NUMBER_FLOAT ){
		
				if ( jp.getNumberType() == NumberType.FLOAT )
					array.addElement( new cfNumberData(jp.getFloatValue()) );
				else if ( jp.getNumberType() == NumberType.DOUBLE )
					array.addElement( new cfNumberData(jp.getDoubleValue()) );

			} else if ( token == JsonToken.VALUE_FALSE ){
				array.addElement( cfBooleanData.FALSE );
			} else if ( token == JsonToken.VALUE_TRUE ){
				array.addElement( cfBooleanData.TRUE );
			} else if ( token == JsonToken.VALUE_NULL ){
				array.addElement( cfNullData.NULL );
			}else
				array.addElement(  getString(jp.getText()) );
		
			token	= jp.nextToken();
		}
		
		return array;
	}
	
	
	
	/**
	 * Attempts to convert the date to a proper date string
	 * @param jsonText
	 * @return
	 */
	private cfData	getString( String jsonText ){
		if ( jsonText.startsWith("{ts") && jsonText.endsWith("}") ){
			try{
				return new cfDateData( ParseDateTime.parseDateString( jsonText ) );
			}catch(Exception e){
				return new cfStringData(jsonText);
			}
		}else{
			return new cfStringData(jsonText);
		}
	}
}