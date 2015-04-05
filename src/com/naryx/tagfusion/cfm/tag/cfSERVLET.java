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

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequestWrapper;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.servlet.jsp.cfIncludeHttpServletResponseWrapper;

public class cfSERVLET extends cfTag implements Serializable
{

  static final long serialVersionUID = 1;

	public String getEndMarker(){  return "</CFSERVLET>"; 	}

	protected void defaultParameters( String _tag ) throws cfmBadFileException{
		defaultAttribute( "WRITEOUTPUT", "YES" );
		parseTagHeader( _tag );
		
		if ( !containsAttribute("CODE") )
			throw newBadFileException( "Missing Attribute", "You must have the CODE path to the servlet you wish to invoke" );
	}
	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		
		// the CODE attribute contains the name of the servlet class to be invoked
		String servletCode = "/servlet/" + getDynamic( _Session,"CODE" ).getString();
		
		//--[ Attempt to get the RequestDispatcher
		RequestDispatcher	requestD 	= cfEngine.thisServletContext.getRequestDispatcher( servletCode );
		
		if ( requestD == null ){
			throw newRunTimeException( "The code, " + servletCode + ", could not be loaded" );	
		}

		//--[ Create the structure that will be used to report this servlet's execution		
		cfStructData	servletData	= new cfStructData();
		servletData.setData( "value", 	new cfStructData() ); 
		servletData.setData( "variable", new cfStructData() ); 
		_Session.setData( "cfservlet", servletData );
				
		//---[ Render any CFSERVLETPARAM tags that may exist
		renderToString( _Session );
		
		//---[ Now that the servlet has been found, trigger its execution
		innerHttpServletRequestWrapper servletInput = new innerHttpServletRequestWrapper( _Session );
		cfIncludeHttpServletResponseWrapper servletOutput = new cfIncludeHttpServletResponseWrapper( _Session.RES );
		
		try{
			requestD.include( servletInput, servletOutput );
		} catch (Exception sE ){
			throw newRunTimeException( "The code, " + servletCode + ", caused an error: " + sE.getMessage() );
		}
		
		//--[ Sort out the Output from the servlet
		// though if it's to be written out then surely is has to be string convertible??
		String encoding = servletOutput.getCharacterEncoding();
     
		try{
			String servletOutputStr = new String( servletOutput.getByteArray(), encoding );
     
			if ( getDynamic(_Session,"WRITEOUTPUT").getBoolean() ){
				_Session.write( servletOutputStr );
			}else{
				servletData.setData( "output", new cfStringData( servletOutputStr ) );
			}
    
		}catch( UnsupportedEncodingException ignored ){} // won't happen since the encoding came from the servlet 
    
		servletData.setData( "servletResponseHeaderName", servletOutput.getResponseHeaders() );	
		servletData.deleteData( "value" );
		servletData.deleteData( "variable" );
		
		return cfTagReturnType.NORMAL;
	}
	
		
	//-------------------------------------------------------
	//--[ Inner class to wrap up the forward response
	//-------------------------------------------------------
	
	class innerHttpServletRequestWrapper extends HttpServletRequestWrapper {
		
		cfStructData	parameterData;		
		public innerHttpServletRequestWrapper( cfSession session ){
			super( session.REQ );
			try{
				parameterData	= (cfStructData)session.getData( "cfservlet" ).getData( "value" );
			}catch(Exception E){
				parameterData = null;
			}
		}
		
		public Map getParameterMap(){
			if ( parameterData != null ){
				FastMap	HT	= new FastMap();
				Object[] keys = parameterData.keys();
				for ( int i = 0; i < keys.length; i++ ) {
					String 	K = (String)keys[ i ];
					try{
						cfData	D	= parameterData.getData( K );
						HT.put( K, D.getString() );
					}catch(Exception ignoreExecption){}
				}
				
				return HT;
			}
			return null;
		}
		
		public Enumeration getParameterNames(){
			if ( parameterData != null )
			{
				Vector v = new Vector();
				Object[] keys = parameterData.keys();
				for ( int i = 0; i < keys.length; i++ )
					v.addElement( keys[ i ] );
				
				return v.elements();
			}
			else
				return null;
		}
		
		public String[] getParameterValues(String name){
			String value	= getParameter( name );
			if ( value == null )	
				return null;
			else{
				String[] a = new String[1];
				a[0] = value;
				return a;
			}
		}
		
		public String getParameter( String name ){
			if ( parameterData == null )	return null;
			
			try{
				cfData	data 	= parameterData.getData( name );
				if ( data == null )	return null;
				return data.getString();
			}catch(Exception E){
				return null;
			}
		}
	}
}	
