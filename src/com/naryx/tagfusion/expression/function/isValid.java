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

package com.naryx.tagfusion.expression.function;

import java.util.List;

import org.apache.oro.text.regex.MalformedPatternException;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.tag.cfPARAM;
import com.naryx.tagfusion.expression.function.xml.IsXml;
 
public class isValid extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public isValid(){  min = 2; max=4; }
 
	public String[] getParamInfo(){
		return new String[]{
			"type - Values can be: range, regex, any, string, array, binary, boolean, component, date, email, java, time, xml, numeric, float, integer, url, variablename, creditcard, usdate, eurodate, ssn, social_security_number, telephone, zipcode, struct, uuid, guid",
			"value",
			"min/pattern - if type='range' then this is the min value; if type='regex' this is the regex pattern to use",
			"max - if type='range' then this is the max value to test against"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"decision", 
				"Determines if the value is of the right type as specified by the rule set passed in", 
				ReturnType.BOOLEAN );
	}
   
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
  	
  	String type = parameters.get( parameters.size()-1 ).getString().toLowerCase();
  	boolean result = false;
  	
  	if ( type.equals( "range" ) ){
  		if ( parameters.size() != 4 ){
  			throwException( _session, "Invalid number of parameters for \"range\" TYPE. Expected 4 parameters." );
  		}
  		
  		cfData value = parameters.get( 2 );
  		try{
  			cfNumberData min = parameters.get( 1 ).getNumber();
  			cfNumberData max = parameters.get( 0 ).getNumber();
  			result = (cfNumberData.compare( value, min ) >= 0 ) && (cfNumberData.compare( value, max ) <= 0 );
  		}catch( dataNotSupportedException e ){
  			result = false;
  		}
  		
  	}else if ( type.equals( "regex" ) || type.equals( "regular_expression" ) ){
  		
  		if ( parameters.size() != 3 ){
  			throwException( _session, "Invalid number of parameters for \"" + type + "\" TYPE. Expected 3 parameters." );
  		}
  		try {
				result = cfPARAM.validateRE(  parameters.get( 0 ).getString(), parameters.get( 1 ) );
			} catch (MalformedPatternException e) {
  			throwException( _session, "Invalid regular expression: " + parameters.get( 0 ).getString() );
			}
			
  	}else{ 
  		if ( parameters.size() != 2 ){
  			throwException( _session, "Invalid number of parameters for \"" + type + "\" TYPE. Expected 2 parameters." );
  		}
  		
  		if ( type.equals( "array" ) ){
  			parameters.remove(1);
  			return new isArray().execute(_session, parameters);
  		}else if ( type.equals( "xml" ) ){
  			parameters.remove(1);
  			return new IsXml().execute(_session, parameters);
  		}else if ( type.equals( "struct" ) ){
  			parameters.remove(1);
  			return new isStruct().execute(_session, parameters);
  		}else if ( type.equals( "query" ) ){
   			parameters.remove(1);
   			return new isQuery().execute(_session, parameters);
  		}else if ( type.equals( "component" ) ){
   			parameters.remove(1);
   			return cfBooleanData.getcfBooleanData( parameters.get(0).getDataType() == cfData.CFCOMPONENTOBJECTDATA );
  		}else if ( type.equals( "java" ) ){
   			parameters.remove(1);
   			return cfBooleanData.getcfBooleanData( parameters.get(0).getDataType() == cfData.CFJAVAOBJECTDATA );
  		}else{
  			try{
  				result = cfPARAM.checkDataType( type, parameters.get( 0 ) );
  			}catch( IllegalArgumentException e ){
  				throwException( _session, e.getMessage() );
  			}
  		}
  	}
  	
  	return cfBooleanData.getcfBooleanData( result );
  }

}