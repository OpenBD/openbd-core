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

package com.naryx.tagfusion.cfm.wddx;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;


/**
 * Serializes and de-serializes CFML data structures to the XML-based WDDX format.
 */

public class cfWDDX extends cfTag implements Serializable{

  static final long serialVersionUID = 1;
  
	public java.util.Map getInfo(){
  	return createInfo("database", "This function can convert complex CFML data structures to and from a WDDX(XML based) format.");
  }
  
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   		createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
 			createAttInfo("ACTION", "The type of conversion to be performed. Possible options are: \"cfml2wddx\", \"wddx2cfml\", \"cfml2js\", \"wddx2js\".", "", true ),
 			createAttInfo("INPUT", "The data to be converted.", "", true ),
 			createAttInfo("OUTPUT", "The output of a conversion. This is a required attribute when ACTION = \"wddx2cfml\".", "", false ),
 			createAttInfo("VERSION", "This attribute allows the selection of the WDDX version to be used. This attribute only applies when ACTION = \"cfml2wddx\".", "", false ),
 			createAttInfo("TOPLEVELVARIABLE", "Specifies a name for the top level javascript variable during a conversion to javascript. This is a required attribute when ACTION = \"cfml2js\" or \"wddx2js\".", "", false )
  	};

  }

  protected void defaultParameters( String _tag ) throws cfmBadFileException {
    defaultAttribute( "VALIDATE", 				"NO" );
	  defaultAttribute( "USETIMEZONEINFO", 	"YES" );
    parseTagHeader( _tag );
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;

		if ( !containsAttribute( "ACTION" ) )
			throw newBadFileException( "Missing Attribute", "Must contain a ACTION attribute");
		
    if ( !containsAttribute( "INPUT" ) )
			throw newBadFileException( "Missing Attribute", "Must contain an INPUT attribute");
  }
  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if ( !containsAttribute( attributes, "ACTION" ) )
			throw newBadFileException( "Missing Attribute", "Must contain a ACTION attribute");
		
    if ( !containsAttribute( attributes, "INPUT" ) )
			throw newBadFileException( "Missing Attribute", "Must contain an INPUT attribute");

    return	attributes;
	}
  
  
	//------------------------------------------------------------
	
  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  	cfStructData attributes = setAttributeCollection(_Session);

  	boolean processingCfOutput = _Session.setProcessingCfOutput( true );
    String action 			= getDynamic( attributes, _Session, "ACTION" ).getString().toUpperCase();
    cfData inputData		= getDynamic( attributes, _Session, "INPUT" );
		
		String output = null;
			
		if ( action.equals("WDDX2CFML") ){
			
			if ( !containsAttribute( attributes, "OUTPUT" ) ){
				throw newBadFileException( "Missing Attribute", "WDDX2CFML Must contain an OUTPUT attribute");
			}
			
			if( inputData instanceof cfStringData ){
				output = getDynamic( _Session, "OUTPUT" ).getString();	
				cfData cfml = wddx2Cfml( inputData.getString(), _Session );
                _Session.setData( output, cfml );
			} else{
				throw newRunTimeException( "Input variable must be a String" );
			}
			
		}else if ( action.equals("CFML2WDDX") ){
			
      int version = 10;
      if ( containsAttribute( attributes,"VERSION") )
        version = (int)(getDynamic( attributes,_Session, "VERSION").getDouble()) * 10;
      
			String wddxString = cfml2Wddx( inputData, version );
      
			if ( containsAttribute( attributes, "OUTPUT" ) ){
				output = getDynamic( attributes, _Session, "OUTPUT" ).getString();	 
				_Session.setData( output,  new cfStringData( wddxString ) );
	  	}else
				_Session.write( wddxString );
			
			
		}else if ( action.equals("WDDX2JS") ){

			if ( !containsAttribute( attributes, "TOPLEVELVARIABLE" ) )
				throw newRunTimeException( "Please supply a top level variable for ACTION=WDDX2JS" );
			  
			String topLevelVariable = getDynamic(  attributes,_Session, "TOPLEVELVARIABLE" ).getString();
		
     	if ( containsAttribute( attributes, "OUTPUT" ) )
				output = getDynamic( attributes, _Session, "OUTPUT" ).getString();	    
	  
		 	wddx2JS( inputData.getString(), _Session, output, topLevelVariable );
			
		} else if ( action.equals("CFML2JS") ){
			
			if ( !containsAttribute( attributes, "TOPLEVELVARIABLE" ) )
				throw newRunTimeException( "Please supply a top level variable for ACTION=CFML2JS" );

			String topLevelVariable = getDynamic( attributes, _Session, "TOPLEVELVARIABLE" ).getString();
		
     	if ( containsAttribute( attributes, "OUTPUT" ) )
				output = getDynamic( attributes, _Session, "OUTPUT" ).getString();	    
	  
			cfml2JS( inputData, _Session, output, topLevelVariable );
		}
    _Session.setProcessingCfOutput( processingCfOutput );
    
    return cfTagReturnType.NORMAL;
  }
	
	//--------------------------------------------------------------
	//-- This method has been marked as 'public static' as it is required by the JMS tags

  public static String cfml2Wddx( cfData _input ) {
    return cfml2Wddx( _input, 10 );
  }
  
  public static String cfml2Wddx( cfData _input, int version ) {
  	
		CharArrayWriter	outChar	= new CharArrayWriter( 1024 );
		PrintWriter					out	= new PrintWriter( outChar );

    if ( version > 10 ){
      out.write( "<wddxPacket version='1.1'><h></h><d>" );
      _input.dumpWDDX( version, out );
      out.write( "</d></wddxPacket>" );
    }else{
      out.write( "<wddxPacket version='1.0'><header></header><data>" );
      _input.dumpWDDX( version, out );
      out.write( "</data></wddxPacket>" );
    }
    
		out.flush();
		return outChar.toString();
	}

	

	//--------------------------------------------------------------
	 
	public static cfData wddx2Cfml( String _wddxString, cfSession _Session ) throws cfmRunTimeException{
		wddxHandler handler = new wddxHandler( _Session );//TODO: remove, _output );			
		SAXParserFactory factory = SAXParserFactory.newInstance(); 
	          
		try{
			SAXParser xmlParser = factory.newSAXParser();
			InputSource ip = new InputSource( new StringReader( _wddxString ));
			xmlParser.parse( ip, handler );
      return handler.getResult();
		}catch(Exception e){
			cfCatchData	catchData	= new cfCatchData( _Session );
	    catchData.setType( "WDDX" );
	    catchData.setDetail( "CFWDDX" );
	  	catchData.setMessage( "Error deserializing WDDX string: " + (e instanceof NullPointerException ? "Badly Formatted xml" : e.getMessage() ) );
			throw new cfmRunTimeException( catchData );  
		}
  }

	//--------------------------------------------------------------
	
	private void wddx2JS( String _wddxString, cfSession _Session, String _output, String _topLevelVariable ) throws cfmRunTimeException{
		
		//-- First convert the WDDX to CFML to make sure all is well there
		cfData cfml = wddx2Cfml( _wddxString, _Session );
		//TODO: check if cfml == null
		//-- Then take the CFML, and convert it to JavaScript
		cfml2JS( cfml, _Session, _output, _topLevelVariable );
		
  }
	
	//--------------------------------------------------------------

	public static String cfml2JS( cfSession _Session, cfData _input, String _topLevelVariable ) throws cfmRunTimeException{
		String resultingJavascript = "";

		if ( _input.getDataType() == cfData.CFQUERYRESULTDATA ){
			resultingJavascript = new wddxQueryResult( _topLevelVariable, (cfQueryResultData)_input ).getJSData();
		} else if ( _input.getDataType() == cfData.CFSTRUCTDATA ){
			resultingJavascript = new wddxStruct( _topLevelVariable, (cfStructData)_input ).getJSData();
		} else if ( _input.getDataType() == cfData.CFARRAYDATA ){
			resultingJavascript = new wddxArray( _topLevelVariable, (cfArrayData)_input ).getJSData();
		} else if ( _input.getDataType() == cfData.CFSTRINGDATA ){
			resultingJavascript	= _topLevelVariable + " = " + wddxDataTypes.getRHSData((cfStringData)_input) + ";\n";
		} else if ( _input.getDataType() == cfData.CFBOOLEANDATA ){
			resultingJavascript	= _topLevelVariable + " = " + wddxDataTypes.getRHSData((cfBooleanData)_input) + ";\n";
		} else if ( _input.getDataType() == cfData.CFNUMBERDATA ){
			resultingJavascript	= _topLevelVariable + " = " + wddxDataTypes.getRHSData((cfNumberData)_input) + ";\n";
		} else if ( _input.getDataType() == cfData.CFDATEDATA ){
			resultingJavascript	= _topLevelVariable + " = " + wddxDataTypes.getRHSData((cfDateData)_input) + ";\n";
		} else {
			cfCatchData	catchData	= new cfCatchData( _Session );
			catchData.setType( "WDDX" );
			catchData.setDetail( "CFWDDX" );
			catchData.setMessage( "Cannot convert cfml data type to Javascript." );
			throw new cfmRunTimeException( catchData );  
		}
		
		return resultingJavascript;
	}

	private static void cfml2JS( cfData _input, cfSession _Session, String _output, String _topLevelVariable ) throws cfmRunTimeException{
		String resultingJavascript = cfml2JS( _Session, _input, _topLevelVariable );

		if ( _output != null )
			_Session.setData( _output, new cfStringData( resultingJavascript ) );
		else
			_Session.write( resultingJavascript );
		
	}
}
