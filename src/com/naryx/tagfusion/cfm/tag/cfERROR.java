/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfErrorData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlURI;

public class cfERROR extends cfTag implements Serializable {

  static final long serialVersionUID = 1;
  
  public static final String DATA_BIN_KEY = "CFERROR_DATA";


  @SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
  	return createInfo("control", "Sets up the default error handling to capture for errors in an orderly fashion");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),

   			createAttInfo( "TEMPLATE", "The name of the template to use in the event of an error", 	"", true ),
   			createAttInfo( "TYPE", "The type of error to trap for; REQUEST, EXCEPTION, VALIDATION and MONITOR", 	"", false ),
   			
   			createAttInfo( "MAILTO", "The email address to send the error to", 	"", false ),
   			createAttInfo( "EXCEPTION", "The exception type to trap for", 	"Any", false )
  	};
  }
  
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute("EXCEPTION",		"Any");
    parseTagHeader( _tag );
		setFlushable( false );
		
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;
    
		if ( !containsAttribute("TYPE") )
			throw missingAttributeException( "cferror.missingType", null );

		if ( !containsAttribute("TEMPLATE") )
			throw missingAttributeException( "cferror.missingTemplate", null );
  }

	 
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if ( !containsAttribute(attributes,"TYPE") )
			throw missingAttributeException( "cferror.missingType", null );

		if ( !containsAttribute(attributes,"TEMPLATE") )
			throw missingAttributeException( "cferror.missingTemplate", null );

    return	attributes;
	}

	
  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  	cfStructData attributes = setAttributeCollection(_Session);
  	
		String TYPE	=  getDynamic( attributes, _Session, "TYPE" ).getString().toUpperCase();
		if ( !TYPE.equals("EXCEPTION") && !TYPE.equals("REQUEST") && !TYPE.equals("VALIDATION") && !TYPE.equals("MONITOR") )
			throw invalidAttributeException( "cferror.invalidType", null );			
		
		cfFile svrFile;
		String templatePath = getDynamic( attributes, _Session, "TEMPLATE" ).getString();
		
    if ( TYPE.equals( "REQUEST" ) || TYPE.equals( "VALIDATION" ) ){
      
      // for REQUEST/VALIDATION type we don't need the file to contain valid cfml tags
      // since the we don't support cfml within these template types.
      // We do however support mappings and root relative paths
      
      cfmlURI filePath = getPath( _Session, templatePath );
      
      // simply check if the file exists
      try{
        _Session.getFile( filePath );
      }catch( cfmBadFileException e ){ 
        if ( e.fileNotFound() ){
          cfCatchData catchData = catchDataFactory.missingFileException( this, templatePath );
          throw new cfmRunTimeException( catchData );
        }
      }
      
      
      String emailMailTo = null;
      if ( containsAttribute(attributes, "MAILTO") )
        emailMailTo = getDynamic(attributes, _Session, "MAILTO" ).getString();
      
      String EXCEPTION = getDynamic(attributes, _Session, "EXCEPTION" ).getString();
      
      cfErrorData eData = (cfErrorData)_Session.getDataBin( DATA_BIN_KEY );
      if ( eData == null ){
        eData = new cfErrorData();
        _Session.setDataBin( DATA_BIN_KEY, eData );
      }

      eData.setHandler( TYPE, filePath, emailMailTo, EXCEPTION );

    }else{
      
      try{
        svrFile = _Session.getFile( getPath( _Session, templatePath ) );
      }catch(cfmBadFileException BFE){
        if ( !BFE.fileNotFound() ){
          cfCatchData catchData = catchDataFactory.tagRuntimeException( this, "Badly formatted template: " + templatePath, BFE.getMessage() );
          catchData.setSession( _Session );
          throw new cfmRunTimeException( catchData, BFE );
        }else{
          cfCatchData catchData = catchDataFactory.missingFileException( this, templatePath );
          throw new cfmRunTimeException( catchData );
        }
      }		
		
      String emailMailTo = null;
      if ( containsAttribute("MAILTO") )
        emailMailTo = getDynamic( attributes, _Session, "MAILTO" ).getString();
			
      String EXCEPTION = getDynamic( attributes, _Session, "EXCEPTION" ).getString();


      // If we are this far then we have enough information to stick into the
      // Session object for testing
      cfErrorData eData	= (cfErrorData)_Session.getDataBin( DATA_BIN_KEY );
      if ( eData == null ){
        eData = new cfErrorData();
        _Session.setDataBin( DATA_BIN_KEY, eData );
      }
		
      eData.setHandler( TYPE, svrFile, emailMailTo, EXCEPTION );
          
    }
    
    return cfTagReturnType.NORMAL;
  }
  
	private static cfmlURI getPath( cfSession _Session, String _path ) {
		String templatePath = _path.replace('\\', '/');
		cfmlURI filePath = null;

		if ( templatePath.charAt(0) == '/' ) {
			// look for mappings
			filePath = cfINCLUDE.getMappedCfmlURI(_Session, templatePath);
			if ( filePath == null ) { // if no mappings, look in the doc root
				filePath = _Session.getFilePath(templatePath);
			}
		} else {
			String presentFilePath = _Session.getPresentFilePath();
			if ( presentFilePath != null ) {
				// relative to the physical path of the calling template
				return new cfmlURI( presentFilePath, templatePath );
			}
			filePath = _Session.getFilePath( templatePath );
		}
		return filePath;
	}
}
