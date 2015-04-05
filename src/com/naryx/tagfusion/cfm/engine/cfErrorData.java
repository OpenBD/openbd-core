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

package com.naryx.tagfusion.cfm.engine;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;
import com.naryx.tagfusion.cfm.file.cfmlURI;

public class cfErrorData extends Object {

  /**
   * A map containing lists of error handlers keyed on type. The list ordering is significant
   * as it reflects the ordering that the  
   */
	private Hashtable<String, List<cfErrorHandler>> errorHandlers;
		
	public cfErrorData(){
		errorHandlers = new Hashtable<String, List<cfErrorHandler>>();
	}
	
	public void setHandler( String _type, cfFile Template, String MailTo, String _exception ){
    addHandler( new cfErrorHandler( _type.toUpperCase(), Template, MailTo, _exception ) );
	}

  public void setHandler( String _type, cfmlURI Template, String MailTo, String _exception ){
    addHandler( new cfErrorHandler( _type.toUpperCase(), Template, MailTo, _exception ) );
  }

  private void addHandler( cfErrorHandler _handler ){
    String type = _handler.Type;
    List<cfErrorHandler> handlers = errorHandlers.get( type );
    if ( handlers == null ){
      handlers = new ArrayList<cfErrorHandler>();
      errorHandlers.put( type, handlers );
    }
    
    if ( _handler.Exception.equalsIgnoreCase( "Any" ) ){
      handlers.add( _handler ); // placed at end of list so will be matched last
    }else{
      handlers.add( 0, _handler );
      
    }
  }
  
	//------------------------------------------------------

	public boolean handleExceptionError( cfSession Session, cfCatchData catchData ){
		cfErrorHandler eHandler = getHandler( "EXCEPTION", catchData );
		if ( eHandler == null ) return false;
		
		return eHandler.displayErrorPage(Session, catchData);
	}


	public boolean handleRequestError( cfSession Session, cfCatchData catchData ){
		cfErrorHandler eHandler = getHandler( "REQUEST", catchData );
		if ( eHandler == null ) return false;
		
		return eHandler.displayRequestPage( Session, catchData );
	}

	
	public boolean handleValidationError( cfSession Session, String errorText ){

		cfErrorHandler eHandler = getHandler( "VALIDATION", null );
		if ( eHandler == null ) return false;
		
		Map<String, String> info = new FastMap<String, String>();
		info.put( "validationheader",   "Form Entries Incomplete or Invalid");
		info.put( "invalidfields", 		  errorText );
		info.put( "validationfooter", 	"Use the Back button on your web browser to return to the previous page and correct the listed problems." );
		
		return eHandler.displayValidationPage( Session, info );
	}

	public boolean handleMonitorError( cfSession Session, cfCatchData catchData ){
		cfErrorHandler eHandler = getHandler( "MONITOR", catchData );
		if ( eHandler == null ) return false;
		
		eHandler.displayErrorPage(Session, catchData);
		return true;
	}
	
	//------------------------------------------------------
	
	private cfErrorHandler getHandler( String Type, cfCatchData catchData ){
    List<cfErrorHandler> handlers = errorHandlers.get( Type.toUpperCase() );
    if ( handlers != null ){
      for ( int i = 0; i < handlers.size(); i++ ){
        cfErrorHandler eHandler = handlers.get(i);
        if ( eHandler.useErrorPage( Type, catchData ) )
          return eHandler;
      }
		}
		return null;
	}
	
	public static void setSession( cfSession Session, cfCatchData catchData ) throws cfmRunTimeException {
		setSession( Session, catchData, "" );
	}
	
	public static void setSession( cfSession Session, cfCatchData catchData, String MailTo ) throws cfmRunTimeException {
		cfStructData info = new cfStructData();

		info.setData( "mailto", 				new cfStringData( MailTo ) );
		info.setData( "datetime", 			new cfDateData( System.currentTimeMillis() ) );
		info.setData( "browser", 				new cfStringData(Session.REQ.getHeader("User-Agent") + "") );
		info.setData( "remoteAddress", 	new cfStringData(Session.REQ.getRemoteAddr() + "") );
		info.setData( "template",			 	new cfStringData(Session.REQ.getRequestURI() + "") );

		if ( Session.REQ.getHeader("Referer") != null )
			info.setData( "httpreferer", 		new cfStringData(Session.REQ.getHeader("Referer") + "") );
		else
			info.setData( "httpreferer", 		new cfStringData("") );

		info.setData( "generatedcontent", new cfStringData(Session.getOutputAsString()) );
	
		if ( Session.REQ.getQueryString() != null )
			info.setData( "querystring",	 	new cfStringData( Session.REQ.getQueryString()+ "") );
		else
			info.setData( "querystring",	 	new cfStringData("") );
	
		cfData d = catchData.getData( "tagcontext" );
		if ( d != null )
			info.setData( "tagcontext", 	d );
		else
			info.setData( "tagcontext", 	cfArrayData.createArray(1) );
		
		info.setData( "diagnostics", 	new cfStringData( createDiagnosticsString( catchData ) ) );
		info.setData( "message", 			new cfStringData(catchData.getString("message")) );
		info.setData( "type", 				catchData.getData( "type" ) );
		info.setData( "errorlogfile", catchData.getData( "errorlogfile" ) );
		
		Session.setData( "error", info );
		Session.setData( "cferror", info );
	}
	
	private static String createDiagnosticsString( cfCatchData catchData ) {
		StringBuilder diagnostic = new StringBuilder();
		if ( catchData.containsKey( "queryerror" ) ) {
            diagnostic.append( "Query Error: " );
            diagnostic.append( catchData.getString( "queryerror" ) );
            diagnostic.append( "\n\n" );
            
            if ( catchData.containsKey( "datasource" ) ) {
            	diagnostic.append( "Datasource: " );
            	diagnostic.append( catchData.getString( "datasource" ) );
            	diagnostic.append( "\n\n" );
            }

            if ( catchData.containsKey( "nativeerrorcode" ) ) {
                diagnostic.append( "Native Error Code: " );
                diagnostic.append( catchData.getString( "nativeerrorcode" ) );
                diagnostic.append( "\n\n" );
            }
        
            if ( catchData.containsKey( "sqlstate" ) ) {
				String sqlState = catchData.getString( "sqlstate" );
				if ( sqlState.length() > 0 ) {
					diagnostic.append( "SQL State: " );
					diagnostic.append( sqlState );
					diagnostic.append( "\n\n" );
				}
            }

            if ( catchData.containsKey( "sql" ) ) {
				diagnostic.append( "Executing SQL: " );
				diagnostic.append( catchData.getString( "sql" ) );
				diagnostic.append( "\n\n" );
            }
        } else {
			if ( catchData.containsKey( "detail" ) ) {
                String detail = catchData.getString( "detail" );
                if ( detail.length() > 0 ) {
    				diagnostic.append( "Detail: " );
    				diagnostic.append( detail );
    				diagnostic.append( "\n\n" );
                }
			}
	
			if ( catchData.containsKey( "extendedinfo" ) ) {
                String extendedInfo = catchData.getString( "extendedinfo" );
                if ( extendedInfo.length() > 0 ) {
    				diagnostic.append( "Extended Info: " );
    				diagnostic.append( extendedInfo );
    				diagnostic.append( "\n\n" );
                }
			}
        }
		
		// add tag and line/column info
		if ( catchData.containsKey( "tagcontext" ) ) {
			cfArrayData tagArray = (cfArrayData)catchData.getData( "tagcontext" );
			if ( ( tagArray != null ) && ( tagArray.size() > 0 ) ) {
				cfStructData tagData = (cfStructData)tagArray.getElement( tagArray.size() );
				if ( tagData != null ) {
					diagnostic.append( "Error occurred while processing element (" );
					try {
						diagnostic.append( tagData.getData( "id" ).getString() );
						diagnostic.append( ") on line " );
						diagnostic.append( tagData.getData( "line" ).getInt() );
						diagnostic.append( " column " );
						diagnostic.append( tagData.getData( "column" ).getInt() );
						diagnostic.append( "\n in template file \"" );
						diagnostic.append( tagData.getData( "template" ).getString() );
						diagnostic.append( "\"" );
					} catch ( dataNotSupportedException e ) {
						diagnostic.append( "ERROR CREATING DIAGNOSTIC MESSAGE: " + e );
					}
				}
			}
		}
		
		return diagnostic.toString();
	}
	
	
	//---------------------------------------
	
	class cfErrorHandler extends Object {
		private String Type, MailTo, Exception;
		private cfFile Template;
		private cfmlURI templatePath;
    
		public cfErrorHandler(String Type, cfFile Template, String MailTo, String Exception){
			this.Type 			= Type;
			this.Template 	= Template;
			this.MailTo			= MailTo;
			if ( this.MailTo == null ) this.MailTo = "";
		
			this.Exception	= Exception; 
		}
		
    public cfErrorHandler(String Type, cfmlURI _tempPath, String MailTo, String Exception){
      this.Type       = Type;
      this.templatePath = _tempPath;
      this.MailTo     = MailTo;
      if ( this.MailTo == null ) this.MailTo = "";
    
      this.Exception  = Exception; 
    }
		public boolean useErrorPage(String inType, cfCatchData catchData){
			if ( Type.equalsIgnoreCase( "request" ) && inType.equalsIgnoreCase("request") )
				return true;
			else if ( Type.equalsIgnoreCase( "validation" ) && inType.equalsIgnoreCase("validation") )
				return true;
			else if ( Type.equalsIgnoreCase( "exception" ) && inType.equalsIgnoreCase("exception") ){
				if ( Exception.equalsIgnoreCase("Any") ) 
					return true;
				else
					return Exception.equalsIgnoreCase( catchData.getString( "type" ) ); 
			}else if ( Type.equalsIgnoreCase( "monitor" ) && inType.equalsIgnoreCase("monitor") ){
				if ( Exception.equalsIgnoreCase("Any") ) 
					return true;
				else
					return Exception.equalsIgnoreCase( catchData.getString( "type" ) ); 
			}else
				return false;
		}
		
		public boolean displayErrorPage( cfSession _Session, cfCatchData catchData ){
			
			try{
				//--[ Setup the dump variable
				cfErrorData.setSession( _Session, catchData, MailTo );
		
				//--[ Reset the Session output so we can discard the presently rendered	stuff
				if ( !Type.equalsIgnoreCase("MONITOR") ){
					_Session.suspendFilter();
					_Session.clearCfSettings();
					try {
						_Session.reset();
						_Session.setContentType( "text/html" );
					} catch ( cfmRunTimeException ignore ) {}
				}
		
				//--[ Render the output file
				Template.render( _Session );
			
				//--[ Flush any output left behind in the buffer out
				if ( Type.equalsIgnoreCase("MONITOR") )				
					_Session.pageFlush();
				else{
					_Session.pageEnd();
				}
			} catch ( cfmAbortException aE ) {
				_Session.pageEnd();
			}catch(Exception E){ // caused an exception so return that it was unsuccessful
				return false;
			}
			return true;
		}
    
    private Map<String, String> getBasicErrorVars( cfSession _Session, cfCatchData _catchData ){
      Map<String, String> errorVars = new FastMap<String, String>();

      cfData msg = _catchData.getData( "message" );
      try{
        errorVars.put( "diagnostics", (msg != null) ? msg.getString() : "" );
      }catch( dataNotSupportedException e ){
        errorVars.put( "diagnostics", "" );
      }
      errorVars.put( "mailto", MailTo == null ? "" : MailTo );
      errorVars.put( "datetime", com.nary.util.Date.formatNow( "EEE MMM dd HH:mm:ss zzz yyyy" ) );
      errorVars.put( "browser", _Session.REQ.getHeader("User-Agent") );
      errorVars.put( "remoteaddress", _Session.REQ.getRemoteAddr() );
      
      String referer = _Session.REQ.getHeader("Referer");
      errorVars.put( "httpreferer", (referer == null) ? "" : referer );
      errorVars.put( "template", _Session.REQ.getRequestURI() );
      errorVars.put( "generatedcontent", _Session.getOutputAsString() );
      String qStr = _Session.REQ.getQueryString();
      errorVars.put( "querystring", (qStr == null) ? "" : qStr);
      
      return errorVars;
    }
    
    private boolean displayValidationPage( cfSession _Session, Map<String, String> _vars ){
      return displayNonCFMLPage( _Session, _vars );
    }
    
    private boolean displayRequestPage( cfSession _Session, cfCatchData catchData ){
      return displayNonCFMLPage( _Session, getBasicErrorVars( _Session, catchData ) );
    }
    
    private boolean displayNonCFMLPage( cfSession _Session, Map<String, String> _vars ){
      BufferedReader reader = null;
      
      try{
        reader = cfmlFileCache.getReader( _Session.REQ, _Session.CTX, templatePath );
        char[] buffer = new char[2048];
        StringBuilder fileContent = new StringBuilder();
        int read;
        while( (read = reader.read( buffer )) != -1 ){
          fileContent.append( buffer, 0, read );
        }
        
        // look for valid #error.xxx# tokens ignoring them if not supported
        int i = 0;
        String errorPrefix = "error.";
        int prefixLen = errorPrefix.length();
        while ( i < fileContent.length() ){
          if ( fileContent.charAt(i) == '#' ){
            // read in # expression
            int expStart = ++i;
            while ( i < fileContent.length() && fileContent.charAt(i) != '#' ){
              i++;
            }
            
            if (  i < fileContent.length() ){ // found an ending '#'
              String exp = fileContent.substring( expStart, i ).toLowerCase();
              if ( exp.length() > prefixLen && exp.startsWith( errorPrefix ) ){
                String subexp = exp.substring( prefixLen );
                String replStr = _vars.get( subexp );
                if ( replStr != null ){
                  fileContent = fileContent.replace(  expStart-1, i+1, replStr );
                  i = expStart + replStr.length(); // set i to correct start
                }
              }
            }
          }else{
            i++;
          }
        }
        
        _Session.suspendFilter();
        _Session.clearCfSettings();
        
        _Session.reset();
        _Session.setContentType( "text/html" );
        _Session.write( fileContent.toString() );
        _Session.pageEnd();
        
        return true;
      }catch( Exception e){
        return false;
      }finally{
	    if ( reader != null ) try{reader.close();}catch(java.io.IOException ioe){}
	  }
    }

	}
  
    
}
