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

package com.naryx.tagfusion.cfm.cfform;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfFormData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.tag.cfOptionalBodyTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.cfm.tag.tagLocator;
import com.naryx.tagfusion.cfm.tag.tagReader;

 
public class cfINPUT extends cfAbstractFormTag implements Serializable, cfOptionalBodyTag
{
  static final long serialVersionUID = 1;

  private static final String TAG_NAME = "CFINPUT";
  private String endMarker = null;

  
  protected void defaultParameters( String _tag ) throws cfmBadFileException {    
    defaultAttribute( "TYPE",    "TEXT" );
    defaultAttribute( "REQUIRED","NO" );
    defaultAttribute( "MESSAGE", "Please provide a valid value" );
		defaultAttribute( "ONERROR", "tf_on_error" );
    defaultAttribute( "VALIDATEAT", "onsubmit" );
        
    parseTagHeader( _tag );
    
    if ( !containsAttribute( "NAME" ) )
       throw newBadFileException( "Missing NAME", "You need to provide a NAME" );
  }

  public String getEndMarker() {
    return endMarker;
  }
  
  public void setEndTag() {
    endMarker = null;
  }

  public void lookAheadForEndTag(tagReader inFile) {
    endMarker = (new tagLocator(TAG_NAME, inFile)).findEndMarker();
  }
  
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  
		cfFormInputData formData = (cfFormInputData)_Session.getDataBin( cfFORM.DATA_BIN_KEY );
		if ( formData == null ){
			throw newRunTimeException( "Missing CFFORM tag. A parent CFFORM tag is required for CFINPUT." );
		}
    
		String type  = getDynamic( _Session, "TYPE" ).getString().toUpperCase();

		// check the TYPE is supported
		if ( !( type.equals( "RADIO" ) || type.equals( "CHECKBOX" ) || type.equals( "TEXT" )
				|| type.equals( "PASSWORD" ) || type.equals( "BUTTON" ) || type.equals( "FILE" )
				|| type.equals( "HIDDEN" ) || type.equals( "RESET" ) || type.equals( "SUBMIT" ) || type.equals( "IMAGE" ) ) ){
			throw newRunTimeException( "Unsupported TYPE: " + type + ". Valid values are BUTTON, CHECKBOX, FILE, HIDDEN, IMAGE, PASSWORD, RADIO, RESET, SUBMIT, and TEXT" );
		}

		if ( type.equals( "RADIO" ) && !containsAttribute( "VALUE" ) ){
			throw newRunTimeException( "VALUE attribute is required when TYPE=\"RADIO\"" );
		}

		String name = getDynamic(_Session, "NAME").getString();

		StringBuilder buffer = new StringBuilder();
    buffer.append( "<input type=\"" );
		buffer.append( type );
		buffer.append( "\" name=\"" );
		buffer.append( name );
		buffer.append( "\"" );
		
		// if ID is set then use it, otherwise default to the same value as NAME
		String id = name;
		if ( containsAttribute( "ID" ) ){
			id = getDynamic( _Session, "ID" ).getString();
		}
		buffer.append( " id=\"" );
		buffer.append( id );
		buffer.append(  "\"" );
		
		// the attributes to ignore
		Set<String> ignoreKeys = getIgnoreKeys();
		
		// ONBLUR is a special attribute that can obtain it's value from multiple sources
		StringBuilder onBlurValue = new StringBuilder();
		
		cfData value = null;
		cfData originalValue = null;
		// if we're preserving data then look to see if a value is already in the form scope
		if ( formData.isPreserveData() ){
			cfFormData formdata = (cfFormData)_Session.getQualifiedData( variableStore.FORM_SCOPE ); 
			originalValue = formdata.getData( name );
		}
		
		// if we haven't already retrieved the value from the form scope and the VALUE is specified
		if ( containsAttribute( "VALUE" ) ){
			value = getDynamic( _Session, "VALUE" );
		} else if ( originalValue != null ) {
			value = originalValue;
		}
		
		if ( value != null ){
			buffer.append( " value=\"" );
			buffer.append( com.nary.util.string.escapeHtml( value.getString() ) );
			buffer.append(  "\"" );
		}
		
		// let's handle the MASK here so we know whether to ignore the onkeyup, onfocus, onblur attributes 
		if ( type.equals( "TEXT" ) && containsAttribute( "MASK" ) ){
			applyMask( _Session, buffer, onBlurValue );
			
			ignoreKeys.add( "ONKEYUP" );
			ignoreKeys.add( "ONFOCUS" );
			ignoreKeys.add( "ONBLUR" );
		}

		if ( containsAttribute( "CHECKED" ) && getDynamic( _Session, "CHECKED" ).getBoolean() ) {
			buffer.append( " checked=\"checked\"" );
		} else if ( ( type.equals( "CHECKBOX" ) || type.equals( "RADIO" ) ) && formData.isPreserveData() && originalValue != null ) {
			if ( type.equals( "CHECKBOX" ) && value.equals( originalValue ) ) {
				buffer.append( " checked=\"checked\"" );
			} else if ( type.equals( "RADIO" ) ) {
				List<String> valist = string.split( value.toString(), "," );
				
				if ( valist.contains( originalValue.toString() ) ) {
					buffer.append( " checked=\"checked\"" );
				}				
								
			}
		}

		// append all the attributes from the CFINPUT tag to the INPUT tag except the ones in the ignoreKeys set
		appendAttributes( _Session, buffer, ignoreKeys );

		// note: this is deprecated in CFMX
		if ( containsAttribute("PASSTHROUGH") ){
			buffer.append( " " );
			buffer.append( getDynamic(_Session, "PASSTHROUGH").getString() );
			buffer.append( " " );
		}
		
		//-- get validation options - where the validation should occur and which validation checks
		applyValidation( _Session, formData, name, type, onBlurValue );

		if ( containsAttribute( "ONBLUR" ) ){
			onBlurValue.append( getDynamic( _Session, "ONBLUR" ).getString() );
		}

		// if any value has been given to ONBLUR then add it to the tag
		if ( onBlurValue.length() > 0 ){
			buffer.append( " onblur=\"" );
			buffer.append( onBlurValue.toString() );
			buffer.append( "\"" );
		}

		buffer.append( "/>" ); // need

    _Session.write( buffer.toString() );

    // render any tag body content
    return super.render( _Session );
	}
	
	protected void applyMask( cfSession _Session, StringBuilder _buffer, StringBuilder _onBlurValue ) throws cfmRunTimeException{
		// add calls to the required functions at onkeyup, onfocus and onblur
		// if any of these attributes have values already, then append the value
		_buffer.append( " onkeyup=\"_maskCheck(this);" );
		if ( containsAttribute( "ONKEYUP" ) ){
			_buffer.append( getDynamic( _Session, "ONKEYUP" ).getString() );
		}
		_buffer.append( "\"" );
		
		_buffer.append( " onfocus=\"_maskFocus(this,\'" ); 
		_buffer.append(  getDynamic( _Session, "MASK" ).getString() );
		_buffer.append( "\');" );
		if ( containsAttribute( "ONFOCUS" ) ){
			_buffer.append( getDynamic( _Session, "ONFOCUS" ).getString() );
		}
		_buffer.append( "\"" );

		_onBlurValue.append( "_maskCheck(this);" );
		
	}

  protected Set<String> getIgnoreKeys(){
  	Set<String> ignoreKeys = super.getIgnoreKeys();
  	ignoreKeys.add( "CHECKED" );
		ignoreKeys.add( "VALUE" );
  	return ignoreKeys;
  }

}
