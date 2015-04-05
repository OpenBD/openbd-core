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


public class cfFormInputData extends Object {

  private String        	formName; 		// the name of the form
  private StringBuilder  	formTag;			// contains the <FORM> start tag complete with attributes
  private String        	userOnSubmit; // a function to be called on submitting the form
  private	String					archive;			
  private StringBuilder  	scriptHeader;
  private StringBuilder  	scriptCheckFunction;  // the javascript snippet that calls the specified validation functions
  private StringBuilder	  formFooter; 				  // the buffer containing anything that needs to be written out at the end of the form 
  private boolean					submitOnce;						// set to true if we need to add a check
  private int 						functionBeginIndx;		// the index into the string buffer where the function begins
  private boolean					preserveData;					// set to true if we need to preserve values currently in the form scope
  
  public cfFormInputData(){
    scriptHeader        	= new StringBuilder(128);
    scriptCheckFunction 	= new StringBuilder(128);
    formTag             	= new StringBuilder(64);
    formFooter						= new StringBuilder(64);
  }
  
  public void reset(){
    scriptHeader        	= new StringBuilder(128);
    scriptCheckFunction 	= new StringBuilder(128);
    formTag             	= new StringBuilder(64);
    formFooter						= new StringBuilder( "</form>");
  }

	public void setArchive( String _archive ){
		archive	= _archive;
	}

	public String getArchive(){
		return archive;
	}
	
	public void setPreserveData( boolean _p ){
		preserveData = _p;
	}
	
	public boolean isPreserveData(){
		return preserveData;
	}

  public void setFormName( String _formName ){
    formName = _formName;
  }

  public String getFormName(){
    return formName;
  }

  public void setFormTag( String _attributes, String _onSubmit ){
    formTag.append( "<form" );

    userOnSubmit  = _onSubmit;
    
    formTag.append( _attributes );
    formTag.append( " onsubmit=\"return check_TF_" );
    formTag.append( formName );
    formTag.append( "(this);\"" );
    formTag.append( ">" );
    
    
    //--[ Setup the SCRIPT
    scriptHeader.append( "<script language=\"javascript\">\r\n<!--\r\n" );
    // init variable for submitOnce checking
    scriptCheckFunction.append( "  cfform_submit_status[\"" );
    scriptCheckFunction.append( formName );
    scriptCheckFunction.append( "\"]=null;\r\n" );
    
    scriptCheckFunction.append( "\r\n  function check_TF_" );
    scriptCheckFunction.append( formName );
    scriptCheckFunction.append( "( theForm ){ " );

    scriptCheckFunction.append( "\r\n    cfform_isvalid = true;" );
    scriptCheckFunction.append( "\r\n    cfform_error_message = \"\";" );
    scriptCheckFunction.append( "\r\n    cfform_invalid_fields = new Object();\r\n" );
    
    functionBeginIndx = scriptCheckFunction.length();
  }
  
  public String outputFORMtag(){
    return formTag.toString(); 
  }
  
  public void appendToFooter( String _str ){
  	formFooter.append( _str );
  }
  
  public void setSubmitOnce(){
  	// insert the submit once check to the start of the function
  	StringBuilder submitCheck = new StringBuilder();
  	submitCheck.append( "\r\n    if ( cfform_submit_status[\"" );
  	submitCheck.append( formName );
  	submitCheck.append( "\"]=='submitting' ) return false;" );
  	submitCheck.append( "\r\n    cfform_submit_status[\"" );
  	submitCheck.append( formName );
  	submitCheck.append( "\"]='submitting';\r\n" );
  	scriptCheckFunction.insert( functionBeginIndx, submitCheck );
  	
  	submitOnce = true;
  }
  
  public String closeFORMTag(){
    formFooter.append( "</form>");
    return formFooter.toString();
  }
  
  public void closeScript(){
    scriptCheckFunction.append( "\r\n    if ( cfform_isvalid ){" );
    if ( userOnSubmit != null ){
    	scriptCheckFunction.append( "\r\n      " );  
      scriptCheckFunction.append( userOnSubmit );
    }

    scriptCheckFunction.append( "\r\n      return true;" );
    scriptCheckFunction.append( "\r\n    }else{" );
    scriptCheckFunction.append( "\r\n      alert( cfform_error_message );" );
    if ( submitOnce ){ // reset submit status
    	scriptCheckFunction.append( "\r\n      cfform_submit_status[\"" );
    	scriptCheckFunction.append( formName );
    	scriptCheckFunction.append( "\"]=null;" );
    }
    scriptCheckFunction.append( "\r\n      return false;" );
    scriptCheckFunction.append( "\r\n    }" );
    scriptCheckFunction.append( "\r\n  }\r\n" );

    scriptHeader.append( scriptCheckFunction.toString() );
    scriptHeader.append( "\r\n//-->\r\n</script>\r\n\r\n" );
  }
  
  public String getJavaScript(){
    return scriptHeader.toString();
  }


  public void formTagRequired( String elementName, String elementType, String errorMessage, String onError ){
    //--[ Load in the required function
  	callFunction( "tf_element_has_value", "theForm['" + elementName + "'], \"" + elementType + "\"", elementName, null, onError, errorMessage );
  }

  public void addMaxLengthCheck( String elementName, String customValidation, String validation, String errorMessage, String onError, int _max ){
  	callFunction( "tf_validate_" + validation,  "theForm['" + elementName + "']," + _max, elementName, customValidation, onError, errorMessage );
  }

  public void addValidateCheck( String elementName, String customValidation, String validation, String errorMessage, String onError ){
  	callFunction( "tf_validate_" + validation, "theForm['" + elementName + "']", elementName, customValidation, onError, errorMessage );
  }

  public void addRangeCheck( String elementName, String customValidation, String validation, String errorMessage, String onError, String _min, String _max ){
    callFunction( "tf_validate_range",  "theForm['" + elementName + "']," + _min + "," + _max, elementName, customValidation, onError, errorMessage );
  }
	
  public void addRegExpCheck( String elementName, String customValidation, String validation, String errorMessage, String onError, String pattern ){
    callFunction( "tf_validate_regular_expression", "theForm['" + elementName + "'], /" + pattern + "/", elementName, customValidation, onError, errorMessage );
  }

  
  
  private void callFunction( String _functionName, String _args, String _elementName, String _customValidation, String _onError, String _errorMessage ){
    //--[ Add the check to the CHECK function
    scriptCheckFunction.append( "\r\n    if ( !" );

    if ( _customValidation != null ){
      scriptCheckFunction.append( _customValidation );
      scriptCheckFunction.append( "( theForm, theForm['" );
      scriptCheckFunction.append( _elementName );
      scriptCheckFunction.append( "'], theForm['" );
      scriptCheckFunction.append( _elementName );
      scriptCheckFunction.append( "'].value, \"" );
      scriptCheckFunction.append( _errorMessage );
      scriptCheckFunction.append( "\" ) ){" );
    }else{
      scriptCheckFunction.append(  _functionName );
      scriptCheckFunction.append( "( " );
      scriptCheckFunction.append( _args );
      scriptCheckFunction.append( " ) ){" );
    }

    String errorHandler = "tf_on_error";
    if ( _onError != null && _onError.length() > 0  ){
      errorHandler = _onError;
    }
    
    scriptCheckFunction.append( "\r\n      " );
    scriptCheckFunction.append( errorHandler );
    scriptCheckFunction.append( "( theForm, \"" );
    scriptCheckFunction.append( _elementName );
    scriptCheckFunction.append( "\", theForm['" );
    scriptCheckFunction.append( _elementName );
    scriptCheckFunction.append( "'].value, \"" );
    scriptCheckFunction.append( _errorMessage );
    scriptCheckFunction.append( "\" );" );
      
      
    scriptCheckFunction.append( "\r\n    }" );
  	
  }

}
