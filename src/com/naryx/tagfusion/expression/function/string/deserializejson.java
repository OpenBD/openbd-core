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
 *  $Id: deserializejson.java 2419 2013-11-25 21:27:42Z andy $
 */

package com.naryx.tagfusion.expression.function.string;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.expression.function.functionBase;

/*
 * Takes in a JSON String and converts it to a cfData
 * 
 * April 2011: Please note that this implementation is currently not being utilised in
 * favor of the DeserializeJSONJackson version that utilises the Jackson library
 * 
 * Will be removed from subsequent versions
 * 
 */
public class deserializejson extends functionBase {

	private static final long serialVersionUID = 1L;

	public deserializejson() {
		min = 1;
		max = 2;
		setNamedParams( new String[]{ "jsonstring", "strictmapping" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"JSON string",
			"strict mapping flag"
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"conversion", 
				"Decodes the given JSON string into a CFML object", 
				ReturnType.OBJECT );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		boolean strictMapping = getNamedBooleanParam( argStruct, "strictmapping", true );
		String jsonString = getNamedStringParam( argStruct, "jsonstring","");
		
		try {
			return getCfDataFromJSon( jsonString, strictMapping );
		} catch (Exception e) {
			throwException(_session, "invalid JSON string");
			return null;
		}
	}
	
	/**
	 * Utilised by cfEngine.serviceCFCMethod() when decoded JSON params coming from the CFAJAXProxy calls.
	 * We only do the params specified as we do not wish to be JSONDecoding stuff that would have been in the
	 * normal form
	 * 
	 * @param args
	 * @throws Exception 
	 * @throws cfmRunTimeException 
	 * @throws dataNotSupportedException 
	 */
	public static cfArgStructData transformStructElements(cfArgStructData args, String params[] ) throws dataNotSupportedException, cfmRunTimeException, Exception{
		cfArgStructData	decodedArgs = new cfArgStructData();
		
		Iterator<String>	it	= args.keySet().iterator();
		while ( it.hasNext() ){
			String k	= it.next();
			
			if ( params != null && params.length > 0 ){
				boolean processIt = false;
				for ( int x=0; x < params.length; x++ ){
					if ( params[x].equalsIgnoreCase(k) && !params[x].startsWith("__BD") ){
						processIt = true;
						break;
					}
				}
				
				if (!processIt)
					continue;
			}
			
			cfData arg	= args.getData(k);
			if (arg != null){
				arg = getCfDataFromJSon(arg.getString(),true);
			}
			
			decodedArgs.setData(k, arg);
			it.remove();
		}
		
		return decodedArgs;
	}
	
	
	public static cfData getCfDataFromJSon(String jsonString, boolean strictMapping) throws Exception {
		if ( jsonString.isEmpty() )
			return cfStringData.EMPTY_STRING;
		else if (jsonString.startsWith("{"))
			return convertToCf(new JSONObject(jsonString), strictMapping);
		else if (jsonString.startsWith("["))
			return convertToCf(new JSONArray(jsonString), strictMapping);
		else{
			JSONTokener tokener = new JSONTokener( jsonString );
			Object value = tokener.nextValue();
			if ( tokener.next() > 0 ){
				throw new Exception("invalid JSON string");
			}
			
			if ( value instanceof String ){
				return new cfStringData( (String) value );
			}else if ( value instanceof Boolean ){
				return cfBooleanData.getcfBooleanData( (Boolean) value, ( (Boolean) value ).booleanValue() ? "true" : "false" );
			}else if ( value instanceof Number ){
				return cfNumberData.getNumber( (Number) value );
			}else if ( value == JSONObject.NULL ){
				return cfNullData.NULL;
			}else
				return new cfStringData( jsonString );
		}
	}
	
	
	private static cfData convertToCf(Object jsonData, boolean strictMapping) throws Exception {

		if (jsonData instanceof JSONArray) {

			JSONArray jsonArray = (JSONArray) jsonData;
			cfArrayData cfarray = cfArrayData.createArray(1);

			for (int x = 0; x < jsonArray.length(); x++) {

				if (jsonArray.isNull(x)) {
					cfarray.addElement(new cfNullData());
					continue;
				}

				try {
					cfarray.addElement(convertToCf(jsonArray.getJSONArray(x), strictMapping));
					continue;
				} catch (JSONException notThisType) {}

				try {
					cfarray.addElement(convertToCf(jsonArray.getJSONObject(x), strictMapping));
					continue;
				} catch (JSONException notThisType) {}

				
				/* only convert to a number if the original value is not a string */
				String str = jsonArray.getString(x);
				if (!(jsonArray.get(x) instanceof String)) {
					try {
						cfNumberData num = new cfNumberData(jsonArray.getDouble(x));
						
						// Check to see if this has leading numbers
						if (str.length() > 1 && str.charAt(0) == '0' && str.indexOf(".") == -1) {
							// has a leading zero
						} else {
							cfarray.addElement( num );
							continue;
						}

					} catch (JSONException notThisType) {}
				}

				try {
					cfarray.addElement(cfBooleanData.getcfBooleanData(jsonArray.getBoolean(x)));
					continue;
				} catch (JSONException notThisType) {}

				cfarray.addElement( new cfStringData( str ) );
			}

			return cfarray;

		} else if (jsonData instanceof JSONObject) {

			JSONObject jsonObject = (JSONObject) jsonData;

			if (strictMapping || (!strictMapping && !isQuery(jsonObject))) {

				cfStructData cfstruct = new cfStructData();

				Iterator<String> keysIt = jsonObject.keys();
				while (keysIt.hasNext()) {
					String key = keysIt.next();

					if (jsonObject.isNull(key)) {
						cfstruct.setData(key, new cfNullData());
						continue;
					}

					try {
						cfstruct.setData(key, convertToCf(jsonObject.getJSONArray(key), strictMapping));
						continue;
					} catch (JSONException notThisType) {}

					try {
						cfstruct.setData(key, convertToCf(jsonObject.getJSONObject(key), strictMapping));
						continue;
					} catch (JSONException notThisType) {}

					
					/* only convert to a number if the original value is not a string */
					String str = jsonObject.getString(key);
					if (!(jsonObject.get(key) instanceof String)) {
						try {
							cfNumberData num = new cfNumberData(jsonObject.getDouble(key));
	
							// Check to see if this has leading numbers
							if (str.length() > 1 && str.charAt(0) == '0' && str.indexOf(".") == -1) {
								// has a leading zero
							} else {
								cfstruct.setData(key, num);
								continue;
							}
	
						} catch (JSONException notThisType) {}
					}

					try {
						cfstruct.setData(key, cfBooleanData.getcfBooleanData(jsonObject.getBoolean(key)));
						continue;
					} catch (JSONException notThisType) {}

					cfstruct.setData(key, new cfStringData(str));
				}

				return cfstruct;

			} else
				return new cfJSONQueryData(jsonObject);
		}

		return null;
	}

	private static boolean isQuery(JSONObject jsonObject) {
		/*
		 * Determines to see if this is a CFQUERY object
		 */

		/* Try the first method */
		try {
			jsonObject.getJSONArray("COLUMNS");
			jsonObject.getJSONArray("DATA");
			return true;
		} catch (Exception e) {}

		try {
			jsonObject.getJSONArray("columns");
			jsonObject.getJSONArray("data");
			return true;
		} catch (Exception e) {}

		try {
			jsonObject.getInt("ROWCOUNT");
			jsonObject.getJSONArray("COLUMNS");
			jsonObject.getJSONObject("DATA");
			return true;
		} catch (Exception e) {}

		try {
			jsonObject.getInt("rowcount");
			jsonObject.getJSONArray("columns");
			jsonObject.getJSONObject("data");
			return true;
		} catch (Exception e) {}

		return false;
	}
}