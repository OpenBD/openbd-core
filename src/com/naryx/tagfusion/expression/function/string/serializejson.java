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
 *  $Id: serializejson.java 2387 2013-06-17 02:07:50Z alan $
 */

package com.naryx.tagfusion.expression.function.string;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.mongodb.QueryBuilder;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.expression.function.functionBase;

/*
 * Takes in a cfData and converts it to a JSON string
 * 
 * It adds a new parameter to the end, that allows you to force all column
 * names to lowercase, instead of the default uppercase.	
 * 
 */
public class serializejson extends functionBase {

	public enum CaseType {
		LOWER,
		UPPER,
		MAINTAIN
	};
	
	public enum DateType {
		LONG,
		HTTP,
		JSON,
		CFML,
		MONGO
	}
	
	private static final long serialVersionUID = 1L;
	private static SimpleDateFormat dateLongFormat = new SimpleDateFormat("MMMM, d yyyy HH:mm:ss");
	private static SimpleDateFormat dateHTTPFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
	private static SimpleDateFormat dateJSONFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private static SimpleDateFormat dateMongoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	static {
		dateMongoFormat.setTimeZone( TimeZone.getTimeZone("GMT") );
	}

	public serializejson() {
		min = 1;
		max = 4;
		setNamedParams( new String[]{ "object", "sercols", "conv", "date" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"object",
			"serialize column names - default false",
			"key case - how to encode the keys. Values are: lower, upper or maintain. Default=" + cfEngine.DefaultJSONReturnCase + ". You can change the default encoding using the bluedragon.xml flag: server.system.jsoncase",
			"date formatting - how dates are encoded.  Values are: LONG, HTTP, JSON, CFML, MONGO. Default=" + cfEngine.DefaultJSONReturnDate + ". You can change the default encoding using the bluedragon.xml flag: server.system.jsondate"
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"conversion", 
				"Encodes the given object to a JSON string.", 
				ReturnType.STRING );
	}
	
	public static CaseType	getCaseType(String tmp ){
		if ( tmp == null )
			return getCaseType( cfEngine.DefaultJSONReturnCase );
		
		tmp = tmp.toLowerCase();
		if ( tmp.equals("true") || tmp.equals("lower") )
			return CaseType.LOWER;
		else if ( tmp.equals("false") || tmp.equals("upper") )
			return CaseType.UPPER;
		else if ( tmp.equals("maintain") )
			return CaseType.MAINTAIN;
		else
			return getCaseType( cfEngine.DefaultJSONReturnCase );
	}
	
	/**
	 * For the DateType string get the datetype
	 * @param tmp
	 * @return
	 */
	public static DateType	getDateType(String tmp){
		if ( tmp == null )
			return getDateType( cfEngine.DefaultJSONReturnDate );
			
		tmp = tmp.toLowerCase();
		if ( tmp.equals("long") )
			return DateType.LONG;
		else if ( tmp.equals("http") )
			return DateType.HTTP;
		else if ( tmp.equals("json") )
			return DateType.JSON; 
		else if ( tmp.equals("cfml") )
			return DateType.CFML;
		else if ( tmp.equals("mongo") )
			return DateType.MONGO;
		else
			return getDateType( cfEngine.DefaultJSONReturnDate );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
			
		cfData var = getNamedParam(argStruct, "object");
		boolean serializeQueryByColumns = getNamedBooleanParam(argStruct, "sercols", false);

		CaseType caseConversion = getCaseType( getNamedStringParam(argStruct, "conv", ""  ) );
		DateType datetype				= getDateType( getNamedStringParam(argStruct, "date", ""  ) );
		
		/* Serialise the data */
		StringBuilder buffer = new StringBuilder(5000);
		encodeJSON(buffer, var, serializeQueryByColumns, caseConversion, datetype);
		return new cfStringData(buffer.toString());
	}



	
	
	public void encodeJSON(StringBuilder out, cfData var, boolean serializeQueryByColumns, CaseType caseConversion, DateType datetype) throws dataNotSupportedException {

		if ( var == null || var.getDataType() == cfData.CFNULLDATA) {
			out.append( "null" );

		}else if ( var.getDataType() == cfData.CFSTRINGDATA ){

			/*
			 * Encode Strings
			 */
			out.append("\"" + encodeString(var.getString()) + "\"");

		} else if (var.getDataType() == cfData.CFBOOLEANDATA) {

			/*
			 * Boolean Data
			 */
			out.append(var.getBoolean() ? "true" : "false");

		} else if (var.getDataType() == cfData.CFDATEDATA) {

			/*
			 * Encode Date
			 */
			Date	localeDate	= (Date)((cfDateData)var).getUnderlyingInstance();

			if ( datetype == DateType.LONG )
				out.append("\"" + dateLongFormat.format(localeDate) + "\"");
			else if ( datetype == DateType.HTTP )
				out.append("\"" + dateHTTPFormat.format(localeDate) + "\"");
			else if ( datetype == DateType.JSON )
				out.append("\"" + dateJSONFormat.format(localeDate) + "\"");
			else if ( datetype == DateType.MONGO )
				out.append("{$date:\"" + dateMongoFormat.format(localeDate) + "\"}");
			else
				out.append("\"" + var.getString() + "\"");
			
		} else if (cfData.isSimpleValue(var)) {

			/*
			 * Encode numbers and booleans
			 */
			out.append(var.getString());

		} else if (var.getDataType() == cfData.CFARRAYDATA) {

			/*
			 * Encode Arrays
			 */
			out.append("[");

			cfArrayData cfarraydata = (cfArrayData) var;
			for (int x = 0; x < cfarraydata.size(); x++) {
				encodeJSON(out, cfarraydata.getElement(x + 1), serializeQueryByColumns, caseConversion, datetype);

				if (x < cfarraydata.size() - 1)
					out.append(",");
			}
			out.append("]");

		} else if (var.getDataType() == cfData.CFSTRUCTDATA) {

			/*
			 * Encode Structs
			 */
			out.append("{");

			cfStructData cfstructdata = (cfStructData) var;
			Object[] keys = cfstructdata.keys();
			String key, value = null;
			for (int x = 0; x < keys.length; x++) {
				key = (String) keys[x];
				
				if ( caseConversion == CaseType.LOWER )
					value = key.toLowerCase();
				else if ( caseConversion == CaseType.UPPER )
					value = key.toUpperCase();
				else if ( caseConversion == CaseType.MAINTAIN )
					value = key;

				out.append("\"" + encodeString(value) + "\"");
				out.append(":");
				encodeJSON(out, cfstructdata.getData(key), serializeQueryByColumns, caseConversion, datetype);

				if (x < keys.length - 1)
					out.append(",");
			}

			out.append("}");

		} else if (var.getDataType() == cfData.CFQUERYRESULTDATA) {

			/*
			 * Encode Query
			 */
			out.append("{");

			if (serializeQueryByColumns)
				encodeQueryByColumnsJSON(out, (cfQueryResultData) var, serializeQueryByColumns, caseConversion, datetype);
			else
				encodeQueryByRowsJSON(out, (cfQueryResultData) var, serializeQueryByColumns, caseConversion, datetype);

			out.append("}");

		}	else if (var.getDataType() == cfData.CFJAVAOBJECTDATA) {
			Object o	= ((cfJavaObjectData)var).getUnderlyingInstance();
			if ( o instanceof org.bson.types.ObjectId )
				out.append("{$oid:\"" + ((org.bson.types.ObjectId)o).toString() + "\"}");
			else if ( o instanceof java.util.regex.Pattern ){
				
				QueryBuilder	qb = new QueryBuilder();
				qb.put("tmp");
				qb.regex( (java.util.regex.Pattern)o );
				String json = qb.get().toString();
				json	= json.substring( json.indexOf("{",1), json.lastIndexOf("}") );
				out.append(json);
				
			} else
				out.append("\"" + encodeString( String.valueOf(o) ) + "\"");
		} else {
			out.append("\"unsupported\"");
		}
	}

	private void encodeQueryByRowsJSON(StringBuilder out, cfQueryResultData querydata, boolean serializeQueryByColumns, CaseType caseConversion, DateType datetype ) throws dataNotSupportedException {
		/* Output the columns */
		if (caseConversion == CaseType.LOWER)
			out.append("\"columns\":[");
		else
			out.append("\"COLUMNS\":[");

		String[] columns = querydata.getColumnList();
		String value = null;
		for (int x = 0; x < columns.length; x++) {
			if ( caseConversion == CaseType.LOWER )
				value = columns[x].toLowerCase();
			else if ( caseConversion == CaseType.UPPER )
				value = columns[x].toUpperCase();
			else if ( caseConversion == CaseType.MAINTAIN )
				value = columns[x];

			
			out.append("\"" + encodeString(value) + "\"");

			if (x < columns.length - 1)
				out.append(",");
		}

		out.append("],");

		/* Output the rows */
		if (caseConversion == CaseType.LOWER)
			out.append("\"data\":[");
		else
			out.append("\"DATA\":[");

		int totalRows = querydata.getSize();

		for (int x = 0; x < totalRows; x++) {
			out.append("[");

			/* Loop around each of the columns */
			List<cfData> row = querydata.getRow(x);
			for (int r = 0; r < row.size(); r++) {
				encodeJSON(out, row.get(r), serializeQueryByColumns, caseConversion, datetype);
				if (r < row.size() - 1)
					out.append(",");
			}

			out.append("]");
			if (x < totalRows - 1)
				out.append(",");
		}

		out.append("]");
	}

	private void encodeQueryByColumnsJSON(StringBuilder out, cfQueryResultData querydata, boolean serializeQueryByColumns, CaseType caseConversion, DateType datetype ) throws dataNotSupportedException {

		/*
		 * The rowcount
		 */
		int totalRows = querydata.getSize();
		if ( caseConversion == CaseType.LOWER )
			out.append("\"rowcount\":");
		else
			out.append("\"ROWCOUNT\":");

		out.append(totalRows);

		/*
		 * The columns
		 */
		if ( caseConversion == CaseType.LOWER )
			out.append(",\"columns\":[");
		else
			out.append(",\"COLUMNS\":[");

		String[] columns = querydata.getColumnList();
		String value = null;
		for (int x = 0; x < columns.length; x++) {
			if ( caseConversion == CaseType.LOWER )
				value = columns[x].toLowerCase();
			else if ( caseConversion == CaseType.UPPER )
				value = columns[x].toUpperCase();
			else if ( caseConversion == CaseType.MAINTAIN )
				value = columns[x];
			
			out.append("\"" + encodeString(value) + "\"");

			if (x < columns.length - 1)
				out.append(",");
		}

		out.append("],");

		/*
		 * The data
		 */
		if ( caseConversion == CaseType.LOWER )
			out.append("\"data\":{");
		else
			out.append("\"DATA\":{");

		for (int c = 0; c < columns.length; c++) {
			if ( caseConversion == CaseType.LOWER )
				value = columns[c].toLowerCase();
			else if ( caseConversion == CaseType.UPPER )
				value = columns[c].toUpperCase();
			else if ( caseConversion == CaseType.MAINTAIN )
				value = columns[c];
			
			out.append("\"" + encodeString(value) + "\":[");

			for (int r = 0; r < totalRows; r++) {
				List<cfData> row = querydata.getRow(r);
				encodeJSON(out, row.get(c), serializeQueryByColumns, caseConversion, datetype);
				if (r < totalRows - 1)
					out.append(",");
			}

			out.append("]");
			if (c < columns.length - 1)
				out.append(",");
		}

		out.append("}");
	}

	private String encodeString(String str) {
		str = com.nary.util.string.replaceString(str, "\\", "\\\\");
		str = com.nary.util.string.replaceString(str, "\r", "\\r");
		str = com.nary.util.string.replaceString(str, "\n", "\\n");
		str = com.nary.util.string.replaceString(str, "\t", "\\t");
		return com.nary.util.string.replaceString(str, "\"", "\\\"");
	}
}