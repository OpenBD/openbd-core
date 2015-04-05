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
import java.util.Set;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfFormData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.cfm.tag.tagReader;


public class cfTEXTAREA extends cfAbstractFormTag implements Serializable
{
	static final long serialVersionUID = 1;

	protected void defaultParameters( String _tag ) throws cfmBadFileException {
    defaultAttribute( "REQUIRED","NO" );
    defaultAttribute( "MESSAGE", "Please provide a valid value" );
		defaultAttribute( "ONERROR", "tf_on_error" );
    defaultAttribute( "RANGE",   "" );
    
		this.parseTagHeader( _tag );
		
    if ( !containsAttribute( "NAME" ) )
    	throw newBadFileException( "Missing NAME", "You need to provide a NAME" );
	}

  public String getEndMarker() {
    return "</CFTEXTAREA>";
  }
  
  public void setEndTag() {}
  public void lookAheadForEndTag(tagReader inFile) {} 

  
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		
		cfFormInputData formData = (cfFormInputData)_Session.getDataBin( cfFORM.DATA_BIN_KEY );
		if ( formData == null ){
			throw newRunTimeException( "There is no CFFORM tag" );
		}

		String name  =  getDynamic(_Session, "NAME").getString();

    StringBuilder buffer = new StringBuilder();
    buffer.append( "<textarea name=\"" ); 
    buffer.append( name );
    buffer.append( "\"" );
    
		appendAttributes( _Session, buffer, getIgnoreKeys() );
		
		StringBuilder onBlurValue = new StringBuilder();
		
		// Add in any javascript checking
   	applyValidation( _Session, formData, name, "TEXTAREA", onBlurValue );
		
    if ( onBlurValue.length() > 0 ){
    	buffer.append( " onblur=\"" );
    	buffer.append( onBlurValue.toString() );
    	buffer.append( "\"" );
		}
    
    buffer.append( ">" );

    // set the text
    String value = renderToString( _Session ).getOutput();
    if ( containsAttribute( "VALUE" ) ){
    	if ( value.length() == 0 ){
    		value = getDynamic( _Session, "VALUE" ).getString();
    	}else{
    		throw newRunTimeException( "The VALUE cannot be defined twice. Remove the VALUE attribute or the body of the CFTEXTAREA." );
    	}
    }

	// if we're preserving data then look to see if a value is already in the form scope
    cfData originalValue = null;
	if ( formData.isPreserveData() ){
		cfFormData formdata = (cfFormData)_Session.getQualifiedData( variableStore.FORM_SCOPE ); 
		originalValue = formdata.getData( name );
		
		if ( originalValue != null) {
			value = originalValue.toString();
		}
	}
    
    buffer.append( com.nary.util.string.escapeHtml( value ) );
    buffer.append( "</textarea>" );

    
    _Session.write( buffer.toString() );

		return cfTagReturnType.NORMAL;
	}
	
  protected Set getIgnoreKeys(){
  	Set ignoreKeys = super.getIgnoreKeys();
  	ignoreKeys.add( "VALUE" );
  	return ignoreKeys;
  }
}
