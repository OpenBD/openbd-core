/* 
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: cfFormData.java 2471 2015-01-11 23:37:49Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

/**
 * Handles the incoming HTTP data that may originate from either a URL or a FORM submission.
 * In addition this class holds the body of the incoming request for POST only.  This allows
 * the function 'GetHttpRequestData' to be able to poll it.
 * 
 * This class can also handle the combined scopes to allow 'form' and 'url' to act the same.
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletInputStream;

import org.apache.oro.text.regex.MalformedPatternException;

import com.nary.util.SequencedHashMap;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.tag.cfERROR;
import com.naryx.tagfusion.cfm.tag.cfPARAM;
import com.naryx.tagfusion.util.RequestUtil;

public class cfFormData extends cfUrlData implements java.io.Serializable{
	
  static final long serialVersionUID = 1;
  
  private boolean	combined;
  private byte[]	incomingRequest = null;
  private boolean isMultipartData = false;
  
	public cfFormData( cfSession _session ){
		this(_session,false);
	}

	public cfFormData( cfSession _session, boolean combinedScopes ){
		combined	= combinedScopes;
		
		String enc = _session.REQ.getCharacterEncoding();
		if ( enc == null ){
			enc = cfEngine.getDefaultEncoding();
		}
    try{
      init( _session, enc );
    }catch ( UnsupportedEncodingException ue ) {
    	cfEngine.log( ue.getMessage() );  // what should we do here?
    }
	}

	protected void reinit( cfSession _session, String enc ) throws UnsupportedEncodingException{
    encoding  = enc;
    
    // if combined, then decode the URI string first of all
    if ( combined )
      decodeURIString( _session ); 


    int contentLength = _session.REQ.getContentLength();
    
    if ( // _session.REQ.getMethod().equals( "POST" ) && // see bug #2696 for why this is commented-out
         ( contentLength > 0 ) &&
         ( _session.REQ.getContentType().indexOf( "application/x-www-form-urlencoded" ) == 0 ) ) {
        readParams();
    }
	}
  
	public boolean isMultipartData(){
		return isMultipartData;
	}
  
  private void readParams() throws UnsupportedEncodingException{
    SequencedHashMap formParms = new SequencedHashMap();
    int len = incomingRequest.length;
    
    // note we're making a copy of the request data to pass to RequestUtil.parseParameters()
    // because that method edits the contents of the byte array. This is not desirable since
    // we'll need the original contents if we wish to reinit() with a different encoding
    byte [] reqCopy = new byte[len];
    System.arraycopy( incomingRequest,0,reqCopy,0,len);
    RequestUtil.parseParameters( formParms, reqCopy, encoding ); 
    
    Iterator<?> iter = formParms.keySet().iterator();
    while ( iter.hasNext() ){
      String key = (String)iter.next();
      String[] valArray = (String[])formParms.get( key );
      String value = valArray[ 0 ];
          
      // create a comma-separated list for multiple values
      if ( valArray.length > 1 ) {
      	StringBuilder valBuffer = new StringBuilder();
        
        for ( int i = 0; i < valArray.length; i++ ){
          if ( valArray[ i ].length() > 0 ) {
            valBuffer.append( valArray[ i ] );
            valBuffer.append( ',' );
          }
        }
          
        if ( valBuffer.length() > 0 ) {
          // remove trailing ','
          value = valBuffer.toString().substring( 0, valBuffer.length() - 1 );
        }
      }

      //-- Check to see if this value hasn't already been added with a URI
      if ( combined ){
        cfData data;
        if ( (data=this.getData(key)) != null  ){
          try{
            value = value + "," + data.getString();
          }catch(Exception ignoreE){}
        }
      }
          
      this.setData( key, new cfStringData( value ) );
    }

    this.setData( "fieldnames", new cfStringData( this.getKeyList( "," ) ) ); 
  }
  
  
	protected void init( cfSession _session, String enc ) throws UnsupportedEncodingException {
		encoding = enc;

		// --[ if combined, then decode the URI string first of all
		if ( combined ) {
			decodeURIString( _session );
		}

		int contentLength = _session.REQ.getContentLength();
		String contentType = _session.REQ.getContentType();
		if ( contentType == null ) {
			contentType = "";
		}

		isMultipartData = (contentType.indexOf( "multipart/form-data" ) == 0 );
		
		if ( // _session.REQ.getMethod().equals( "POST" ) && // see bug #2696 for why this is commented-out
				( contentLength > 0 ) 
				&& ( !isMultipartData ) ) {
			try {
				int max = contentLength;
				int len = 0;
				incomingRequest = new byte[ contentLength ];
				ServletInputStream is = _session.REQ.getInputStream();
				while ( len < max ) {
					int next = is.read( incomingRequest, len, max - len );
					if (next < 0) {
						break;
					}
					len += next;
				}
				is.close();
			} catch ( IOException ignoreIOexcption ) {}
			
			if ( contentType.indexOf( "application/x-www-form-urlencoded" ) == 0 ) {
				readParams();
			}
		}
	}
  
  
  public void setEncoding( cfSession _session, String _encoding ) throws UnsupportedEncodingException {
  	String currEncoding = encoding;
    encoding = _encoding;
    
    cfDecodedInput di = (cfDecodedInput) _session.getDataBin( cfDecodedInput.DATA_BIN_KEY );
    if ( di != null ){ // if cfDecodedInput is present then form data is multipart
      di.reencodeParameters( _session, currEncoding, _encoding );
    }else{ // otherwise use different method to reset the encoding of the form data
      clear();
      reinit( _session, _encoding );
    }
  }
  
  public String getEncoding(){
    return encoding;
  }
  
  public byte[]	getRequestData(){
  	return incomingRequest;
  }
  
	public void validateFormFields( cfSession parentSession ) throws cfmAbortException {
		Object [] keys  = keys();
    
		String parameter;
		String type;
		int typeStartIndx;
		StringBuilder errorText = new StringBuilder();
		boolean bError = false;
		boolean subError;
		
		for ( int i = 0; i < keys.length; i++ ){
			parameter	= (String) keys[i]; 
			subError = false;
      
			typeStartIndx = parameter.toLowerCase().lastIndexOf( "_cfform" ); 
			if ( typeStartIndx == -1 ){
				typeStartIndx = parameter.lastIndexOf( '_' );
				// legacy support for _eurodate, _range, _float, _date, _time, _integer only as per cfmx
				if ( typeStartIndx != -1 ){
					type = parameter.substring( typeStartIndx+1 ).toLowerCase();;
					if ( !( type.equals( "integer" ) || type.equals( "time" ) || type.equals( "date" ) || type.equals( "float" ) || type.equals( "range" ) || type.equals( "eurodate" ) ) ){
						continue;
					}
				}else{
					continue;
				}
			}else{
				type = parameter.substring( typeStartIndx+7 ).toLowerCase();
			}
			
			//--[ Check for the required
			if ( type.equals( "required" ) && !isVariablePresent(parameter) ){
				subError = true;
			} else if ( isVariablePresent( parameter ) ){
				if ( type.equals( "integer" ) ){
					subError = !isInteger(parameter);
				}else if ( type.equals( "float" ) || type.equals( "numeric" ) ){
					subError = !isFloat(parameter); 
				}else if ( ( type.equals( "date" ) || type.equals( "usdate" ) || type.equals( "eurodate" ) || type.equals( "time" ) ) ){
					subError = !isTimeDate(parameter);
				}else if ( type.equals( "ssn") || type.equals( "social_security_number" ) ){
					subError = !isSSN( parameter ); 
				}else if ( type.equals( "uuid") ){
					subError = !isUUID( parameter ); 
				}else if ( type.equals( "guid") ){
					subError = !isGUID( parameter ); 
				}else if ( type.equals( "zipcode") ){
					subError = !isZipCode( parameter ); 
				}else if ( type.equals( "telephone") ){
					subError = !isTelephone( parameter ); 
				}else if ( type.equals( "creditcard") ){
					subError = !isCreditCard( parameter ); 
				}else if ( type.equals( "boolean") ){
					subError = !isBoolean( parameter ); 
				}else if ( type.equals( "email") ){
					subError = !isEmail( parameter ); 
				} else if ( type.equals( "maxlength" ) ){
					subError = !isLengthLessThan(parameter);
				} else if ( type.equals( "noblanks" ) ){
					subError = getParameterValue( parameter ).trim().length() == 0;
				} else if ( type.equals( "range" ) ){
					subError = !isInRange(parameter);
				} else if ( type.equals( "url" ) ){
					subError = !isUrl(parameter);
				} else if ( type.equals( "regex" ) ){
					subError = !isMatch(parameter);
				}
			}

			// if it failed then add the error
			if ( subError ){
				bError = true;
				errorText.append( "<LI>" );
				errorText.append( getError( type, parameter ) );
				errorText.append( "</LI>" );
			}

		}
		
		
		//--[ If an error then display and throw an error
		if ( bError ){
						
			//--[ Check to see if the user has specified a CFERROR tag for this error
			cfErrorData errorData = (cfErrorData)parentSession.getDataBin( cfERROR.DATA_BIN_KEY );
			if ( errorData == null || (errorData != null && !errorData.handleValidationError( parentSession, errorText.toString())) ){
				parentSession.clearCfSettings();
				parentSession.reset();
				parentSession.write( "<HTML><HEAD><TITLE>Form Entries Incomplete or Invalid</TITLE></HEAD><BODY><HR><H3>Form Entries Incomplete or Invalid</H3>One or more problems exist with the data you have entered.<UL>" );
				parentSession.write( errorText.toString() );
				parentSession.write( "</UL>Use the <I>Back</I> button on your web browser to return to the previous page and correct the listed problems.<P><HR></BODY></HTML>" );
			}
			
			parentSession.abortPageProcessing();			
		}
	}
	
	protected String getParameter( String parameter ) {
		try {
			cfData data = getData( parameter );
			return ( data == null ? null : data.getString() );
		} catch ( dataNotSupportedException e ) {
			return null; 
		}
	}
	
	protected String getParameterName( String field ) {
		return field.substring( 0, field.lastIndexOf("_") );
	}
	
	private boolean isVariablePresent( String field ){
		String tmp = getParameter( getParameterName(field) );
		return ( tmp == null || tmp.length() == 0 ) ? false : true; 
	}
	
	// returns the appropriate error message for the given field type
	private String getError( String type, String field ){
		String errorValue = getParameter( field );
		// if the type is maxlength, noblanks or regex - the VALUE doesn't contain the error message to use
		if ( type.equals( "maxlength" ) ){
			return  "Data entered in the <B>" + getParameterName( field ) + "</B> field must be, at most, " +  getParameter( field ) + " characters in length (you entered '" + getParameter( getParameterName(field) ) + "').";
		}else if ( type.equals( "regex" ) ){
			return  "Data entered in the <B>" + getParameterName( field ) + "</B> field must match the regular expression " +  getParameter( field ) + " (you entered '" + getParameter( getParameterName(field) ) + "').";
		}else if ( type.equals( "range" ) ){
			return  "Data entered in the <B>" + getParameterName( field ) + "</B> field must be in the range [" +  getParameter( field ) + "] (you entered '" + getParameter( getParameterName(field) ) + "').";
		}else if ( errorValue != null && errorValue.length() > 0 ){ // if the VALUE is present and not blank
			return errorValue;
		}else if ( type.equals( "required" ) ){
			return  "A value must be entered for the <B>" + getParameterName( field ) + "</B> field.";
		}else if ( type.equals( "noblanks" ) ){
			return  "Data entered in the <B>" + getParameterName( field ) + "</B> field must not be blank (you entered '" + getParameter( getParameterName(field) ) + "').";
		}else if ( type.equals( "integer" ) ){
			return  "Data entered in the <B>" + getParameterName( field ) + "</B> field must be an integer (you entered '" + getParameter( getParameterName(field) ) + "').";
		}else{
			return  "Data entered in the <B>" + getParameterName( field ) + "</B> field must be a " + type + " (you entered '" + getParameter( getParameterName(field) ) + "').";
		}

	}
	
	private boolean isMatch( String field ){
		try{
			return com.nary.util.string.regexMatches( getParameterValue( field ), getParameter( field ) );
		}catch( Exception e ){
			return false;
		}
	}
	
	private boolean isInteger( String field ){
		try{
			Integer.valueOf( getParameterValue( field ) );
			return true;
		}catch( NumberFormatException E){
			return false;
		}
	}
	
	private boolean isFloat( String field ){
		try{
			Double.valueOf( getParameterValue( field ) );
			return true;
		}catch( NumberFormatException E){
			return false;
		}
	}

	private boolean isBoolean( String field ){
		String bool = getParameterValue( field ).toLowerCase();
		if ( bool.equalsIgnoreCase( "yes" ) || bool.equals( "true" ) || bool.equals( "no" ) || bool.equals( "false" ) ){
			return true;
		}

		try{
			Integer.parseInt( bool );
			return true;
		}catch( NumberFormatException E){}
		
		return false;
	}

	private boolean isUrl( String field ){
		try {
			return cfPARAM.isUrl( getParameterValue( field ) );
		} catch (MalformedPatternException e) { // shouldn't happen
			return false; 
		}
	}

	private boolean isSSN( String field ){
		try{
			return cfPARAM.validateRE( cfPARAM.SSN_RE, getParameterValue( field ) );
		} catch (MalformedPatternException e) { // shouldn't happen
			return false; 
		}
	}

	private boolean isUUID( String field ){
		try{
			return cfPARAM.validateRE( cfPARAM.UUID_RE, getParameterValue( field ).toLowerCase() );
		} catch (MalformedPatternException e) { // shouldn't happen
			return false; 
		}
	}

	private boolean isZipCode( String field ){
		try{
			return cfPARAM.validateRE( cfPARAM.ZIPCODE_RE, getParameterValue( field ) );
		} catch (MalformedPatternException e) { // shouldn't happen
			return false; 
		}
	}

	private boolean isTelephone( String field ){
		try{
			return cfPARAM.validateRE( cfPARAM.PHONE_RE, getParameterValue( field ) );
		} catch (MalformedPatternException e) { // shouldn't happen
			return false; 
		}
	}

	private boolean isCreditCard( String field ){
		try{
			return cfPARAM.isCreditCard( getParameterValue( field ) );
		} catch (MalformedPatternException e) { // shouldn't happen
			return false; 
		}
	}

	private boolean isGUID( String field ){
		try{
			return cfPARAM.validateRE( cfPARAM.GUID_RE, getParameterValue( field ).toLowerCase() );
		} catch (MalformedPatternException e) { // shouldn't happen
			return false; 
		}
	}

	private boolean isEmail( String field ){
		try{
			return cfPARAM.validateRE( cfPARAM.EMAIL_RE, getParameterValue( field ) );
		} catch (MalformedPatternException e) { // shouldn't happen
			return false; 
		}
	}

	private boolean isLengthLessThan( String field ){
		String maxlenStr = getParameter( field );
		try{
			int maxLen = Integer.parseInt( maxlenStr );
			if ( maxLen < 0 ){
				return true;
			}
			return getParameterValue( field ).length() <= maxLen; 
		}catch( NumberFormatException E){
			return true;
		}
	}

	private String getParameterValue( String field ){
		String value = getParameter( getParameterName( field ) );
		value = com.nary.util.string.replaceString( value, ",", "" );
		value = com.nary.util.string.replaceString( value, "$", "" );
		return value.trim();
	}
	
	private boolean isInRange( String field ){
		String minmax = getParameter( field ).toLowerCase();
		if ( minmax.indexOf("min=") == -1 && minmax.indexOf("max=") == -1 )
			return false;
	
		try{
			double valueD = Double.valueOf( getParameterValue( field ) ).doubleValue();
			
			List<String> tokens = string.split( minmax, " " );
			for ( int i = 0; i < tokens.size(); i++ ){
				String mm = tokens.get(i).toString();
				int c1 = mm.indexOf( "=" );
				if ( c1 == -1 )
					return false;
					
				String mmType		= mm.substring( 0, c1 );
				double mmValue	= Double.valueOf( mm.substring( c1+1 ) ).doubleValue();
				
				if ( mmType.equals("min") && (valueD < mmValue) )
					return false;
				else if ( mmType.equals("max") && (valueD > mmValue) )
					return false;
			}

		}catch( NumberFormatException E){
			return false;
		}
		
		return true;
	}
	
	private boolean isTimeDate( String field ){
		String DD = getParameter( getParameterName( field ) );
		if ( DD == null || DD.trim().length() == 0 )
			return false;
		
		com.nary.util.date.dateTimeTokenizer DT = new com.nary.util.date.dateTimeTokenizer( DD );
		if ( ( !DT.validateStructure() ) || ( DT.getDate() == null ) )
			return false;
		else
			return true;
	}
}
