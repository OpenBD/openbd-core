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

import java.util.Iterator;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.wddx.cfWDDX;
import com.naryx.tagfusion.cfm.wddx.wddxDataTypes;

public class ToScript extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public ToScript(){ 
  	min = 2; max = 4;
  	setNamedParams( new String[]{ "object","jsname","format","shortcuts" } );
  }
  
	public String[] getParamInfo(){
		return new String[]{
			"object - struct, array, query or simple value",
			"javascript var name",
			"format - flag to set WDDX output; default false",
			"shortcuts - flag to use shortcuts for structs/arrays; default to false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Used to convert the given input to Actionscript/Javascript variable", 
				ReturnType.STRING );
	}
   
  
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
  	cfData cfvar = getNamedParam(  argStruct, "object");
  	String jsvar = getNamedStringParam(  argStruct, "jsname","");
  	boolean asFormat = getNamedBooleanParam(argStruct,"format",false );
  	boolean outputFormat = getNamedBooleanParam(argStruct,"shortcuts",false );
  	
  	String script;
  	
  	if ( outputFormat || ( cfData.isSimpleValue( cfvar ) ) ){
  		script = cfWDDX.cfml2JS( _session, cfvar, jsvar );
  	}else{
  		// actionscript
  		script = toActionScript( _session, cfvar, jsvar, asFormat );
  	}
  	return new cfStringData( script );
  }
  

  private String toActionScript( cfSession _session, cfData _data, String _var, boolean _asFormat ) throws cfmRunTimeException{
  	if ( cfData.isSimpleValue( _data ) ){
  		return cfWDDX.cfml2JS( _session, _data, _var );
  	}else if ( _data.getDataType() == cfData.CFQUERYRESULTDATA ){
  		return toActionScript( (cfQueryResultData) _data, _var, _asFormat );
  	}else if ( _data.getDataType() == cfData.CFARRAYDATA ){
  		return toActionScript( (cfArrayData) _data, _var, _asFormat );
  	}else if ( _data.getDataType() == cfData.CFSTRUCTDATA ){
  		return toActionScript( (cfStructData) _data, _var, _asFormat );
  	}else{
  		return _var + " =";
  	}
  }
  
  private String toActionScript( cfQueryResultData _queryData, String _name, boolean _asFormat ){
  	StringBuilder asString = new StringBuilder(256);
		cfData value;
		String columns[] = _queryData.getColumnList();
    
    asString.append( _name + ( _asFormat ? " = [];\n" : " = new Array();\n"  ) );
    
	 	for ( int rows=0; rows < _queryData.getSize(); rows++ ){	
			asString.append( _name  + "[" + rows + "] = " + ( _asFormat ? "{}" : "new Object()" ) + ";\n");
		 
			for( int cols=0; cols < _queryData.getNoColumns(); cols++ ){
		 		value = _queryData.getCell(rows+1, cols+1, false);

	  		asString.append( _name );
		  	asString.append( '[' );
		  	asString.append( rows );
		  	asString.append( "]['" );
		  	asString.append( columns[cols].toLowerCase() );
		  	asString.append( "'] = " );
	  		asString.append( getSimpleValueString( value ) );
			  asString.append( ";\n" );

		 	}
		}
		
		return asString.toString();	
  }
  
  private String toActionScript( cfArrayData _arrayData, String _name, boolean _asFormat ){
  	StringBuilder asString = new StringBuilder(256);
		cfData value;
    
    asString.append( _name + ( _asFormat ? " = [];\n" : " = new Array();\n"  ) );
    
		for( int i = 0; i < _arrayData.size(); i++){
			value  = _arrayData.getElement(i+1);
		
	  	if ( cfData.isSimpleValue( value ) ){
		  	asString.append( _name );
		  	asString.append( '[' );
		  	asString.append( i );
		  	asString.append( "] = " );
		  	asString.append( getSimpleValueString( value ) );
			  asString.append( ";\n" );
		  
	  	}else if ( value.getDataType() == cfData.CFQUERYRESULTDATA ){
	  		asString.append( toActionScript( (cfQueryResultData) value, _name + '[' + i + ']', _asFormat ) );
	  	}else if ( value.getDataType() == cfData.CFARRAYDATA ){
	  		asString.append( toActionScript( (cfArrayData) value, _name + "[\"" + i + "\"]", _asFormat ) );
	  	}else if ( value.getDataType() == cfData.CFSTRUCTDATA ){
	  		asString.append( toActionScript( (cfStructData) value, _name + "[\"" + i + "\"]", _asFormat ) );
	  	}
		}
		
		return asString.toString();	
  }

  
  private String toActionScript( cfStructData _arrayData, String _name, boolean _asFormat ) {
  	StringBuilder asString = new StringBuilder( 256 );
		asString.append( _name + ( _asFormat ? " = {};\n" : " = new Object();\n" ) );

		Iterator<String> keys = _arrayData.keySet().iterator();
		while ( keys.hasNext() ) {
			String key = keys.next();
			cfData value = _arrayData.getData( key );

			if ( cfData.isSimpleValue( value ) ) {
				asString.append( _name );
				asString.append( "[\"" );
				asString.append( key );
				asString.append( "\"] = " );
				asString.append( getSimpleValueString( value ) );
				asString.append( ";\n" );

			} else if ( value.getDataType() == cfData.CFQUERYRESULTDATA ) {
				asString.append( toActionScript( (cfQueryResultData)value, _name + "[\"" + key + "\"]", _asFormat ) );
			} else if ( value.getDataType() == cfData.CFARRAYDATA ) {
				asString.append( toActionScript( (cfArrayData)value, _name + "[\"" + key + "\"]", _asFormat ) );
			} else if ( value.getDataType() == cfData.CFSTRUCTDATA ) {
				asString.append( toActionScript( (cfStructData)value, _name + "[\"" + key + "\"]", _asFormat ) );
			}
		}
		return asString.toString();

	}

  private String getSimpleValueString( cfData _data ){
  	if ( _data.getDataType() == cfData.CFSTRINGDATA ){
	  	return wddxDataTypes.getRHSData( (cfStringData) _data );
	  }else if ( _data.getDataType() == cfData.CFNUMBERDATA ){
	  	return wddxDataTypes.getRHSData( (cfNumberData) _data );
	  }else if ( _data.getDataType() == cfData.CFBOOLEANDATA ){
	  	return wddxDataTypes.getRHSData( (cfBooleanData) _data );
		}else if( _data.getDataType() == cfData.CFDATEDATA ){
			return wddxDataTypes.getRHSData( (cfDateData) _data );
		}else if ( _data.getDataType() == cfData.CFNULLDATA ){
			return "null";
    }
  	return "";
  }

}
