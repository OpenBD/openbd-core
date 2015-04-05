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
 *  http://www.openbluedragon.org/
 */

package com.naryx.tagfusion.expression.function;

/**
 * The javacast function creates a duplicate copy of the
 * given cfData and set the javacast according to the type
 * parameter value.
 */

import java.util.List;

import com.naryx.tagfusion.cfm.engine.Javacast;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class javaCast extends functionBase {

	private static final long serialVersionUID = 1L;

	
	/**
	 * Creates new instance of javaCast
	 */
	public javaCast(){
		min = max = 2;
	}

	
	/**
	 * Returns params info for manual generation
	 * 
	 * @return
	 */
	public String[] getParamInfo(){
		return new String[] { "javaobject", "cast type - boolean, int, long, double, float, string, null" };
	}

	public java.util.Map getInfo() {
		return makeInfo("format", "Returns back a duplicate variable of the java object casting it to the specific type", ReturnType.OBJECT);
	}

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException{
		Javacast castType = Javacast.NULL;
		
		cfData duplicate = null;
		boolean isArray = false;

		String type = parameters.get(1).getString().toLowerCase();
		cfData variable = parameters.get(0);
		boolean isNull = false;
		
		// check if type contains [] 
		if ( type.endsWith( "[]" ) ){ 
			// this is an array
			isArray = true;
			
			type = type.substring( 0, type.length() - 2 ); // remove [] from end of string
		}
		
		// check valid type
		if (type.equals("boolean")) {
			castType = isArray ? Javacast.BOOLEAN_ARRAY : Javacast.BOOLEAN;
		} else if (type.equals("int")) {
			castType = isArray ? Javacast.INT_ARRAY : Javacast.INT;
		} else if (type.equals("long")) {
			castType = isArray ? Javacast.LONG_ARRAY : Javacast.LONG;
		} else if (type.equals("double")) {
			castType = isArray ? Javacast.DOUBLE_ARRAY : Javacast.DOUBLE;
		} else if (type.equals("float")) {
			castType = isArray ? Javacast.FLOAT_ARRAY : Javacast.FLOAT;
		} else if (type.equals("string")) {
			castType = isArray ? Javacast.STRING_ARRAY : Javacast.STRING;
		} else if ( type.equals( "bigdecimal" ) ){
			castType = isArray ? Javacast.BIGDECIMAL_ARRAY : Javacast.BIGDECIMAL;
		} else if ( type.equals( "byte" ) ){
			castType = isArray ? Javacast.BYTE_ARRAY : Javacast.BYTE;
		} else if ( type.equals( "char" ) ){
			castType = isArray ? Javacast.CHAR_ARRAY : Javacast.CHAR;
		} else if ( type.equals( "short" ) ){
			castType = isArray ? Javacast.SHORT_ARRAY : Javacast.SHORT;
		} else if (type.equals("null")) {
			castType = Javacast.NULL;
			isNull = true;
		} else{
			Class classType;
			type = parameters.get(1).getString();
			try {
				classType = Class.forName( type );
				castType = new Javacast( classType, isArray );
			} catch (ClassNotFoundException e) {
				throwException(_session, "Invalid javacast type. Class cannot be loaded: " + type );
			}
		}

		
		if (isNull){
			// null is a special case. We just return a cfNullData
			duplicate = cfNullData.JAVA_NULL;
		} else{
			// check valid cfData. if so duplicate it.
			if ((variable.getDataType() == cfData.CFNUMBERDATA) || 
				(variable.getDataType() == cfData.CFBOOLEANDATA) || 
				(variable.getDataType() == cfData.CFSTRINGDATA)	|| 
				(variable.getDataType() == cfData.CFARRAYDATA) ){
				duplicate = variable.duplicate();
			} else if (variable.getDataType() == cfData.CFNULLDATA){
				duplicate = new cfNullData( castType, ((cfNullData) variable).isDBNull());
			} else if ( (variable.getDataType() == cfData.CFJAVAOBJECTDATA ) ){
				duplicate = variable;
			}else{
				// throw exception - invalid variable. Must be a boolean, number, or
				// string or array
				throwException(_session, "Invalid variable type. Cannot javacast this data type: " + variable.getDataTypeName() );
			}

			duplicate.setJavaCast( castType );
		}
		
		return duplicate;
	}

}
