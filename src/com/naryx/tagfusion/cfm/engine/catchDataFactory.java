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

import java.sql.SQLException;
import java.util.List;

import com.naryx.tagfusion.cfm.tag.cfTag;

public class catchDataFactory extends Object {

	public static cfCatchData generalException( cfSession session, cfStringData type, String message, String detail ) {
		cfCatchData catchData = new cfCatchData(session);
		catchData.setType( type );
		catchData.setDetail( detail );
		catchData.setMessage( message );
		return catchData;
	}
	
  public static cfCatchData generalException( String errCode, String detail ){
    return generalException( cfCatchData.TYPE_APPLICATION, errCode, detail, null );	
  }
	
  public static cfCatchData generalException( String errCode, String detail, String values[] ){
    return generalException( cfCatchData.TYPE_APPLICATION, errCode, detail, values );	
  }

  public static cfCatchData generalException( cfStringData type, String errCode, String detail, String values[] ){
    cfCatchData	catchData	= new cfCatchData();
    catchData.setType( type );
    catchData.setErrorCode( errCode );
    catchData.setMessage( cfEngine.getMessage(errCode) );
    catchData.setDetail( cfEngine.getMessage(detail,values) );
    return catchData;
  }
	
  public static cfCatchData extendedException( String errCode, String detail, String values[], String extendedInfo ){
    return extendedException( cfCatchData.TYPE_APPLICATION, errCode, detail, values, extendedInfo );
  }
	
  public static cfCatchData extendedException( cfStringData type, String errCode, String detail, String values[], String extendedInfo ){
        cfCatchData	catchData	= new cfCatchData();
    catchData.setType( type );
    catchData.setErrorCode( errCode );
        catchData.setMessage( cfEngine.getMessage(errCode) );
    catchData.setDetail( cfEngine.getMessage(detail,values) );
    catchData.setExtendedInfo( extendedInfo );
            
        return catchData;
  }

  public static cfCatchData extendedException(String errCode, String msg, String detail)
  {
	  return extendedException(cfCatchData.TYPE_APPLICATION, errCode, msg, detail);
  }
  
  public static cfCatchData extendedException(cfStringData type, String errCode, String msg, String detail)
  {
      cfCatchData catchData = new cfCatchData();
      catchData.setType(type);
      catchData.setErrorCode(errCode);
      catchData.setMessage(msg);
      catchData.setDetail(detail); 
      return catchData;
}
	
  public static cfCatchData extendedScriptException( String errCode, String detail, String values[], int _line, int _col ){
      cfCatchData catchData = new cfCatchData();
      catchData.setType( cfCatchData.TYPE_TEMPLATE );
      catchData.setErrorCode( errCode );
      catchData.setMessage( cfEngine.getMessage(errCode) );
      catchData.setDetail(  cfEngine.getMessage(detail,values) );
      catchData.setLine( _line );
      catchData.setColumn( _col );
      return catchData;
  }
  
    public static cfCatchData databaseException( String datasource, String detail, String values[], String sql, SQLException e ){
        cfCatchData catchData = new cfCatchData();
        catchData.setType( cfCatchData.TYPE_DATABASE );
        catchData.setErrorCode( "errorCode.sqlError" );
        catchData.setMessage( cfEngine.getMessage( "errorCode.sqlError" ) );
        catchData.setDetail( cfEngine.getMessage( detail, values ) );
        catchData.setSql( sql );
        catchData.setSqlException( e );
        catchData.setDataSource( datasource );
        
        return catchData;
    }

	public static cfCatchData searchException( String errCode, String detail ){
		cfCatchData catchData = new cfCatchData();
		catchData.setType( cfCatchData.TYPE_SEARCH );
		catchData.setErrorCode( errCode );
		catchData.setMessage( cfEngine.getMessage( errCode ) );
		catchData.setDetail( detail );
		return catchData;
	}

  public static cfCatchData extendedException( cfStringData type, String errCode, String detail, String values[], String extendedInfo, Throwable throwable ){
    cfCatchData catchData = extendedException( type, errCode, detail, values, extendedInfo );
    catchData.setJavaException( throwable );
    return catchData;
  }
	
  public static cfCatchData javaMethodException( String errCode, String _exceptionName, String _message, Throwable throwable ){ 
    cfCatchData	catchData	= new cfCatchData();
    catchData.setType( _exceptionName );
    catchData.setErrorCode( errCode );
    catchData.setMessage( _message );
    catchData.setJavaException( throwable );
    return catchData;
  }
	
  public static cfCatchData noStartTagException( String tag, int lineNo, int colNo ){
    cfCatchData	catchData	= new cfCatchData();
    catchData.setType( cfCatchData.TYPE_TEMPLATE );
    catchData.setLine( lineNo );
    catchData.setColumn( colNo );
    catchData.setTagname( tag );
    catchData.setErrorCode( "errorCode.missingStartTag" );
    catchData.setMessage( cfEngine.getMessage("errorCode.missingStartTag") );
    catchData.setDetail( cfEngine.getMessage("parseTag.missingStartTag",new String[]{tag}) );
    return catchData;
  }

  public static cfCatchData tagNotSupportedException( String tag, int lineNo, int colNo ){
    cfCatchData	catchData	= new cfCatchData();
    catchData.setType( cfCatchData.TYPE_TEMPLATE );
    catchData.setLine( lineNo );
    catchData.setColumn( colNo );
    catchData.setTagname( tag );
    catchData.setErrorCode( "errorCode.notSupported" );
    catchData.setMessage( cfEngine.getMessage("errorCode.notSupported") );
    catchData.setDetail( cfEngine.getMessage("parseTag.notSupported",new String[]{tag}) );
    return catchData;
  }

    public static cfCatchData tagNotRecognizedException( String tag, int lineNo, int colNo ){
        cfCatchData catchData = new cfCatchData();
        catchData.setType( cfCatchData.TYPE_TEMPLATE );
        catchData.setLine( lineNo );
        catchData.setColumn( colNo );
        catchData.setTagname( tag );
        catchData.setErrorCode( "errorCode.notRecognized" );
        catchData.setMessage( cfEngine.getMessage("errorCode.notRecognized") );
        catchData.setDetail( cfEngine.getMessage("parseTag.notRecognized",new String[]{tag.substring(1, tag.length()-1)}) );
        return catchData;
    }

    public static cfCatchData tagParseException( String tag, int lineNo, int colNo ){
      cfCatchData catchData = new cfCatchData();
      catchData.setType( cfCatchData.TYPE_TEMPLATE );
      catchData.setLine( lineNo );
      catchData.setColumn( colNo );
      catchData.setErrorCode( "errorCode.parseTag" );
      catchData.setMessage( cfEngine.getMessage("errorCode.badFormat") );
      catchData.setDetail( cfEngine.getMessage("parseTag.badFormat",new String[]{}) );
      return catchData;
  }

  public static cfCatchData classNotFoundException( String tag, int lineNo, int colNo ){
    cfCatchData	catchData	= new cfCatchData();
    catchData.setType( cfCatchData.TYPE_TEMPLATE );
    catchData.setLine( lineNo );
    catchData.setColumn( colNo );
    catchData.setTagname( tag );
    catchData.setErrorCode( "errorCode.classError" );
    catchData.setMessage( cfEngine.getMessage("errorCode.classError") );
    catchData.setDetail( cfEngine.getMessage("parseTag.classError",new String[]{tag}) );
    return catchData;
  }

  public static cfCatchData noEndTagException( cfTag t ){
    cfCatchData	catchData	= new cfCatchData();
    catchData.setLine( t.posLine  );
    catchData.setType( cfCatchData.TYPE_TEMPLATE );
    catchData.setTagname( t.getTagName() );
    catchData.setColumn( t.posColumn  );
    catchData.setErrorCode( "errorCode.missingEndTag" );
    catchData.setMessage( cfEngine.getMessage("errorCode.missingEndTag") );
    catchData.setDetail( cfEngine.getMessage("parseTag.missingEndTag",new String[]{t.getTagName()}) );
    return catchData;
  }

  public static cfCatchData missingAttributeException( cfTag t, String detail, String values[]  ){
    cfCatchData	catchData	= new cfCatchData();
    catchData.setLine( t.posLine  );
    catchData.setType( cfCatchData.TYPE_TEMPLATE );
    catchData.setTagname( t.getTagName() );
    catchData.setColumn( t.posColumn  );
    catchData.setErrorCode( "errorCode.missingAttribute" );
    catchData.setMessage( cfEngine.getMessage("errorCode.missingAttribute") );
    catchData.setDetail( cfEngine.getMessage(detail,values) );
    return catchData;
  }

  public static cfCatchData invalidAttributeException( cfTag t, String detail, String values[]  ){
    cfCatchData	catchData	= new cfCatchData();
    catchData.setLine( t.posLine  );
    catchData.setType( cfCatchData.TYPE_TEMPLATE );
    catchData.setTagname( t.getTagName() );
    catchData.setColumn( t.posColumn  );
    catchData.setErrorCode( "errorCode.invalidAttribute" );
    catchData.setMessage( cfEngine.getMessage("errorCode.invalidAttribute") );
    catchData.setDetail( cfEngine.getMessage(detail,values) );
    return catchData;
  }

  public static cfCatchData invalidExpressionException( cfTag t, String detail, int _line ){
    cfCatchData	catchData	= new cfCatchData();
    catchData.setLine( t.posLine  );
    catchData.setType( cfCatchData.TYPE_TEMPLATE );
    catchData.setTagname( t.getTagName() );
    catchData.setColumn( t.posColumn  );
    catchData.setData( "scriptline", new cfNumberData( _line ) );
    catchData.setErrorCode( "errorCode.invalidExpression" );
    catchData.setMessage( cfEngine.getMessage("errorCode.invalidExpression") );
    catchData.setDetail( detail );
    return catchData;
  }

  public static cfCatchData tagRuntimeException( cfTag t, String msg, String detail  ){
    cfCatchData	catchData	= new cfCatchData();
    catchData.setLine( t.posLine  );
    catchData.setType( cfCatchData.TYPE_TEMPLATE );
    catchData.setTagname( t.getTagName() );
    catchData.setColumn( t.posColumn  );
    catchData.setErrorCode( "errorCode.runtimeError" );
    catchData.setMessage( msg );
    catchData.setDetail( detail );
    return catchData;
  }
  
  /**
   * Creates a cfCatchData suit
   */
  public static cfCatchData summarizeBadFileException( cfTag t, String msg, cfmBadFileException bfe  ){
    cfCatchData catchData = new cfCatchData();
    catchData.setType( cfCatchData.TYPE_TEMPLATE );

	if (t != null)
	{
		catchData.setLine(t.posLine);
		catchData.setTagname(t.getTagName());
		catchData.setColumn(t.posColumn);
	}
	else
	{
		catchData.setLine(bfe.getCatchData().getLine());
		catchData.setTagname(bfe.getCatchData().getTagname());
		catchData.setColumn(bfe.getCatchData().getColumn());
	}

    catchData.setErrorCode( "errorCode.runtimeError" );
    catchData.setMessage( msg );
    StringBuilder details = new StringBuilder();
    List<cfCatchData> errorList = bfe.getCatchData().getErrorList();
    if ( errorList != null ){
      details.append( errorList.size() + " error" + (errorList.size() > 1 ? "s" : "") + " found: <br>" );
      for ( int i = 0; i < errorList.size(); i++ ){
        cfCatchData nextError = (cfCatchData) errorList.get(i);
        details.append( (i+1) + ". " + nextError.getString( "message" ) + " at line " +  nextError.getString( "line" ) 
            + ", column " + nextError.getString( "column" ) + " : " + nextError.getString( "detail" ) );
        details.append( "<br>" );
      }
      catchData.setDetail( details.toString() );
    }else{
      catchData.setDetail( bfe.getMessage() );
    }
    return catchData;
  }
	
  public static cfCatchData invalidTagAttributeException( cfTag t, String value ){
    cfCatchData	catchData	= new cfCatchData();
    catchData.setType( cfCatchData.TYPE_TEMPLATE );
    catchData.setLine( t.posLine  );
    catchData.setTagname( t.getTagName() );
    catchData.setColumn( t.posColumn  );
    catchData.setErrorCode( "errorCode.invalidTag" );
    catchData.setMessage( cfEngine.getMessage("cftag.badAttributes", new String[]{value}) );
    return catchData;
  }
	
  public static cfCatchData runtimeException( cfTag t, String value ){
    cfCatchData	catchData	= new cfCatchData();
    if ( t != null ) {
	    catchData.setLine( t.posLine  );
	    catchData.setTagname( t.getTagName() );
	    catchData.setColumn( t.posColumn  );
    }
    catchData.setErrorCode( "errorCode.runtimeError" );
    catchData.setMessage( cfEngine.getMessage("errorCode.runtimeError") );
    catchData.setDetail( cfEngine.getMessage("runtime.general", new String[]{value}) );
    return catchData;
  }
	
  public static cfCatchData missingFileException( cfTag t, String filename ){
    cfCatchData	catchData	= new cfCatchData();
    if ( t != null ) {
    	catchData.setLine( t.posLine  );
    	catchData.setTagname( t.getTagName() );
    	catchData.setColumn( t.posColumn  );
    }
    catchData.setType( cfCatchData.TYPE_MISSINGINCLUDE );
    catchData.setErrorCode( "errorCode.runtimeError" );
    catchData.setMessage( cfEngine.getMessage("errorCode.runtimeError") );
    catchData.setDetail( cfEngine.getMessage("cfinclude.missingFile", new String[]{filename}) );
    catchData.setMissingFilename( filename );
    return catchData;
  }
	
  public static cfCatchData applicationRequestFileException( String filename ){
    cfCatchData	catchData	= new cfCatchData();
    catchData.setType( cfCatchData.TYPE_TEMPLATE );
    catchData.setErrorCode( "errorCode.badRequest" );
    catchData.setMessage( cfEngine.getMessage("errorCode.badRequest") );
    catchData.setDetail( cfEngine.getMessage("cffile.applicationrequest", new String[]{filename}) );
    catchData.setMissingFilename( filename );
    return catchData;
  }
	
  public static cfCatchData missingRequestFileException( String filename ){
    cfCatchData	catchData	= new cfCatchData();
    catchData.setType( cfCatchData.TYPE_TEMPLATE );
    catchData.setErrorCode( "errorCode.badRequest" );
    catchData.setMessage( cfEngine.getMessage("errorCode.badRequest") );
    catchData.setDetail( cfEngine.getMessage("cffile.missingfile", new String[]{filename}) );
    catchData.setMissingFilename( filename );
    return catchData;
  }
  
  public static cfCatchData missingCustomTagException( String tagname ){
    cfCatchData	catchData	= new cfCatchData();
    catchData.setType( cfCatchData.TYPE_APPLICATION );
    catchData.setErrorCode( "errorCode.badRequest" );
    catchData.setMessage( cfEngine.getMessage("errorCode.badRequest") );
    catchData.setDetail( cfEngine.getMessage("cffile.missingcustomtag", new String[]{ tagname }) );
    catchData.setMissingFilename( tagname );
    return catchData;
  }
  
  public static cfCatchData badEncodingException( String _msg ){
    cfCatchData catchData = new cfCatchData();
    catchData.setType( cfCatchData.TYPE_TEMPLATE );
    catchData.setErrorCode( "errorCode.badRequest" );
    catchData.setMessage( cfEngine.getMessage("errorCode.badRequest") );
    catchData.setDetail( _msg );
    return catchData;
  }
  
  	public static cfCatchData eventHandlerException( String eventHandler, cfCatchData rootCause ) {
  		cfCatchData catchData = new cfCatchData();
  		catchData.setType( rootCause.getType() );
  		catchData.setMessage( "Event Handler Exception" );
  		catchData.setDetail( "An exception occurred in an Application.cfc event handler: " + eventHandler );
  		catchData.setData( "rootcause", rootCause );
  		catchData.setData( "name", new cfStringData( eventHandler ) );
  		return catchData;
  	}
	
}
