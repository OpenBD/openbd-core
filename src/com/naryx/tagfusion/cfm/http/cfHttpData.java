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

package com.naryx.tagfusion.cfm.http;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nary.net.http.urlEncoder;
import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.file.fileDescriptor;


/**
 * The <B>cfHttpData</B> class holds the information that the cfHTTP tag requires 
 * to carry out its job of retrieving a page. It holds the different parameters 
 * that may be added to the URL for POST procedures (these are added to it by 
 * CFHTTPPARAM tags.). It also holds the data that comes back from retrieving the page:
 *  - the content of the page itself
 *  - the response headers
 *  - the status code
 *  - the header
 *  - the mime type
 *  - the parseStatus (an extra feature that CF doesn't have - see cfHttp.
 */

public class cfHttpData extends cfStructData implements Serializable{

  static final long serialVersionUID = 1;
  
  /** The response headers retrieved from cfHTTP tag.*/

  private List<fileDescriptor> files;
  private Map<String, String> headers;
  private Map<String, List<String>> cookies;
  private Map<String, String> urlData;
  private Map<String, FormFieldWrapper> formData;
  private boolean paramsUsed = false; // default - this is set once a httpParam is processed
  private boolean usesGet; // set by cfHttp so that any cfHTTPParams can check which method is being used
	 
  private String reqbody = null;
  private String contentType = null;
  
  private String charset;
  
  public cfHttpData( String _charset ){
  	charset = _charset;
  	
    files = new ArrayList<fileDescriptor>();
    // headers is required to be a ArrayList rather than a hashtable
    // since there can be for example more than one 'Cookie' header
    headers = new FastMap<String, String>();
    cookies = new FastMap<String, List<String>>();
    urlData = new FastMap<String, String>();
    formData = new FastMap<String, FormFieldWrapper>();

  }// cfHttpData()
	

  public String getCharset(){
  	return charset;
  }
  
  public void setUsesGet(boolean _usesGet){
    usesGet = _usesGet;
  }// setUsesGet()


  public boolean usesGet(){
    return usesGet;
  }// usesGet()
	
  
  public void addFile( fileDescriptor _fd ){
    files.add( _fd );
  }

  public List<fileDescriptor> getFiles(){
    return files;
  }// getFiles()

  public void removeFiles(){
    files.clear(); // or = null;
  }// removeFiles()

  public void addHeader(String name, String _value, boolean _encode){
    String value = _value;
    if ( _encode ){
      value = urlEncoder.encode( value );
    }
    
    if ( headers.containsKey( name ) ){
      headers.put( name, headers.get( name ) + "," + value );
    }else{
      headers.put( name, value );
    }
  }// addHeader()


  public void addCookie( String _name, String _value, boolean _enc ){
  	String value = _value;
  	if ( _enc ){
  		value = urlEncoder.encode( value ); 
  	}
    if ( cookies.containsKey( _name ) ){
      List<String> otherVals = cookies.get( _name );
      otherVals.add( value ); 
    }else{
      List<String> values = new ArrayList<String>( 4 );
      values.add( value );  
      cookies.put( _name, values );
    }
  }

  
  public Map<String, String> getHeaders(){
    return headers;
  }// getHeaders()

  
  public void removeHeaders(){
    headers.clear(); // or = null;
  }// removeHeaders()

  public Map<String, List<String>> getCookies(){
  	return cookies;
  }
  
  public void removeCookies(){
  	cookies.clear();
  }
  
  public void addURLData(String name, String value){
    if ( urlData.containsKey( name ) ){
      urlData.put( name, urlData.get( name ) + "," + value ); 
    }else{
      urlData.put(name, value);    
    }    
  }// addUrlData()
  
  public Map<String, String> getURLData(){
    return urlData;
  }
  
  public void addFormData(String name, String value, boolean _encoded ){
    if ( !usesGet ){
      formData.put( name, new FormFieldWrapper( value, _encoded ) );    
    }else{
      addURLData( name, value );
    }

  }// addFormData()
  
  public Map<String, String> getFormData(){
  	Map<String, String> encFormData = new FastMap<String, String>();
  	boolean isMultipart = (files.size()>0);
  	Iterator<String> keys = formData.keySet().iterator();
  	while( keys.hasNext() ){
  		String nextKey = keys.next();
  		FormFieldWrapper ff = formData.get( nextKey );
  		String value = ff.value;
  		if ( ff.enc && !isMultipart ){ // don't encode data regardless of encoding value if this is a multipart post
  			try {
					value = urlEncoder.encode( value, charset );
				} catch (UnsupportedEncodingException e) {
					value = urlEncoder.encode( value );
				}
  		}
  		encFormData.put( nextKey, value );
  	}
  	
    return encFormData;
  }// getUrlData()



  public void removeData(){
    urlData.clear(); // or = null;
    formData.clear();
  }// removeData()

  
  public void setParamsUsed(boolean _b){
    paramsUsed = _b;
  }// setParamsUsed()


  public boolean getParamsUsed(){
    return paramsUsed;
  }// getParamsUsed()


  public boolean isBodySet(){
    return reqbody != null;
  }
  
  public void setBody( String _body, String _type ){
    reqbody = _body;
    contentType = _type;
  }
  
  public String getBody(){
    return reqbody;
  }
  
  // required if the body is set
  // returns null if not set
  public String getContentType(){
    return contentType;
  }
  
  public String toString(){
    return "{HTTPDATA}";
  }// toString()
  
  class FormFieldWrapper {
  	String value;
  	boolean enc;
  
  	FormFieldWrapper( String _val, boolean _enc ){
  		value = _val;
  		enc = _enc;
  	}
  }  
}// cfHttpData
