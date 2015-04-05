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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.tag.cfTag;

abstract public class cfAbstractFormTag extends cfTag implements Serializable
{

  static final long serialVersionUID = 1;

  private static Set<String> supportedValidation;
  
  static{
  	supportedValidation = new HashSet<String>();
  	supportedValidation.add( "date" );
  	supportedValidation.add( "usdate" );
  	supportedValidation.add( "eurodate" );
  	supportedValidation.add( "time" );
  	supportedValidation.add( "float" );
  	supportedValidation.add( "numeric" );
  	supportedValidation.add( "integer" );
  	supportedValidation.add( "range" );
  	supportedValidation.add( "boolean" );
  	supportedValidation.add( "telephone" );
  	supportedValidation.add( "zipcode" );
  	supportedValidation.add( "creditcard" );
  	supportedValidation.add( "ssn" );
  	supportedValidation.add( "social_security_number" );
  	supportedValidation.add( "email" );
  	supportedValidation.add( "url" );
  	supportedValidation.add( "guid" );
  	supportedValidation.add( "uuid" );
  	supportedValidation.add( "maxlength" );
  	supportedValidation.add( "noblanks" );
  	supportedValidation.add( "regex" );
  	supportedValidation.add( "regular_expression" );
  	supportedValidation.add( "submitonce" );
  }

  protected void appendAttributes( cfSession _Session, StringBuilder _buffer, Set<String> _ignoreKeys ) throws cfmRunTimeException{
		Iterator<String> keys = this.properties.keySet().iterator();
		while ( keys.hasNext() ){
			String nextKey = keys.next();
			if ( !_ignoreKeys.contains( nextKey ) ){
				appendAttribute( _Session, nextKey, _buffer );
			}
		}
  }

	protected void appendAttribute( cfSession _Session, String _name, StringBuilder _buffer ) throws dataNotSupportedException, cfmRunTimeException {
    if ( containsAttribute( _name ) ){
      _buffer.append( ' ' );
      _buffer.append( _name.toLowerCase() );
      _buffer.append( "=\"" );
			_buffer.append( com.nary.util.string.escapeHtml( getDynamic(_Session, _name ).getString() ) );
			_buffer.append( "\"" );
		}
	}
	
	
	protected void applyValidation( cfSession _Session, cfFormInputData _formData, String _name, String _type, StringBuilder _onBlurValue ) throws dataNotSupportedException, cfmRunTimeException{
    String message = getDynamic( _Session, "MESSAGE" ).getString();
		String onError = getValue( _Session, "ONERROR" );
		List<String> onBlurList = null;

		//--[ where does the validation need to occur
		boolean onBlur = false;
    boolean onSubmit = false;
    boolean onServer = false;
    
  	String validateAt = "onsubmit";
  	if ( containsAttribute( "VALIDATEAT" ) ){ // note we could default it in the defaultParameters but this was added in BD 7
  		validateAt = getDynamic( _Session, "VALIDATEAT" ).getString().toLowerCase();
  	}
    // the VALIDATEAT attribute specifies where the validation is performed - on form submission, onblur or at the server
    // again multiple options can be specified as a comma separated list
    List<String> validateAtVals = com.nary.util.string.split( validateAt, ',' );
    for ( int i = 0; i < validateAtVals.size(); i++ ){
    	String nextValidateAt = validateAtVals.get( i ).toString();
    	if ( nextValidateAt.equals( "onsubmit" ) ){
    		onSubmit = true;
    	}else if ( nextValidateAt.equals( "onblur" ) ){
    		onBlur = true;
    		onBlurList = new ArrayList<String>();
    	}else if ( nextValidateAt.equals( "onserver" ) ){
    		onServer = true;
    	}else{
    		throw newRunTimeException( "Unsupported VALIDATEAT value: " + nextValidateAt + ". Supported values are onblur, onserver and onsubmit" );
    	}
    }

    boolean isRequired = getRequired(_Session ); 
    //--[ if form element is required, append check 
    if ( isRequired ){
    	if ( onSubmit ){
    		_formData.formTagRequired( _name, "_" + _type, message, onError );
    	}
    	if ( onServer ){
				addHiddenField( _formData, _name + "_CFFORMREQUIRED", message );
    	}
    }
    

		if ( containsAttribute("ONVALIDATE") ){ 
			// call custom validation function
			_formData.addValidateCheck( _name, getDynamic( _Session, "ONVALIDATE" ).getString(), null, message, onError );

		}else	if ( containsAttribute("VALIDATE") ){

			// multiple validate options can be specified as a comma separated liste
	    String validate  = getDynamic(_Session, "VALIDATE").getString().toLowerCase();
	    List<String> validateVals = com.nary.util.string.split( validate, ',' );
	
	
			String onValidate = getValue(_Session, "ONVALIDATE");
			
	    
			for ( int i = 0; i < validateVals.size(); i++ ){
				//--[ check that there exists a function for the given validate choice 
				String nextValidate = (String) validateVals.get( i );
	
				if ( nextValidate.equals( "submitonce" ) ){
					// only relevent when TYPE is SUBMIT
					if ( onSubmit ){
						_formData.setSubmitOnce();
					}
				}else if ( nextValidate.equals( "maxlength" ) ){
					int maxLength = 0;
					if ( containsAttribute( "MAXLENGTH" ) ){
						maxLength = getDynamic( _Session, "MAXLENGTH" ).getInt();
					}
					if ( onSubmit ){
						_formData.addMaxLengthCheck( _name, onValidate, nextValidate, message, onError, maxLength );
					}
					if ( onServer ){
						addHiddenField( _formData, _name + "_CFFORMMAXLENGTH", String.valueOf( maxLength ) );
					}
					if ( onBlur ){
						onBlurList.add( "!tf_validate_maxlength( this," + maxLength + " )" );
					}
					
				}else if ( nextValidate.equals("range") ){ // range checking
					// first check the value is a numeric
					if ( onSubmit ){
						_formData.addValidateCheck( _name, onValidate, "numeric", message, onError );
					}
					if ( onServer ){
						addHiddenField( _formData, _name + "_CFFORMNUMERIC", message );
					}
					
		      // get the min/max limits of the RANGE
					if ( containsAttribute( "RANGE" ) ){
						String minRange = null;
						String maxRange = null;
		        	
						String range = getDynamic(_Session, "RANGE").getString();   
						if ( range.length() > 0 ){
							int c1 = range.indexOf(",");
							if ( c1 != -1 ){
								// check if there are more than 2 in the list
								if ( range.indexOf( ',', c1+1 ) != -1 ){
									throw newRunTimeException( "Invalid RANGE attribute value. Only a minimum and maximum may be listed." );
								}
								
								if ( c1 == 0 ){ // only max specified e.g. ",10"
									maxRange = range.substring( 1 );
								}else if ( c1 == range.length()-1 ){ // only min specified e.g. "1,"
									minRange = range.substring( 0, range.length()-1 );
								}else{// both min and max specified e.g. "1,10"
									minRange  = range.substring(0,c1);
									maxRange  = range.substring(c1+1);
								}
		    	
							}else{
								minRange = range;
							}
		    	      
							// check that the range values are integers
							rangeCheck( minRange );
							rangeCheck( maxRange );
						}
		
						if ( onSubmit ){
							_formData.addRangeCheck( _name, onValidate, nextValidate, message, onError, minRange, maxRange );
						}
						
						if ( onServer ){
							addHiddenField( _formData, _name + "_CFFORMRANGE", ( minRange != null ? "MIN=" + minRange : "" ) + ( maxRange != null ? " MAX=" + maxRange : "" ) );
						}
						
						if ( onBlur ){
							onBlurList.add( "!tf_validate_range( this," + minRange + "," + maxRange + " )" );
						}

					}else{
						throw newRunTimeException( "The PATTERN attribute must be specified when VALIDATE='regular_expression'." );
					}
		        
				}else if ( nextValidate.equals( "regular_expression" ) || nextValidate.equals( "regex" ) ){ // regular expression based validation
					if ( containsAttribute( "PATTERN" ) ){
						String pattern = getDynamic( _Session, "PATTERN" ).getString();
						if ( onSubmit ){
							_formData.addRegExpCheck( _name, onValidate, "regular_expression", message, onError, pattern );
						}
						if ( onServer ){
							addHiddenField( _formData, _name + "_CFFORMREGEX", pattern );
						}
						if ( onBlur ){
							onBlurList.add( "!tf_validate_regular_expression( this, /" + pattern + "/ )" );
						}

					}else{
						throw newRunTimeException( "The PATTERN attribute must be specified when VALIDATE='regular_expression'." );
					}
		
				
				}else if ( supportedValidation.contains( nextValidate ) ){ // the rest of the validate options can be handled in the same manner
					if ( nextValidate.equals( "ssn" ) ){
						nextValidate = "social_security_number";
					}					
					if ( onSubmit ){
						_formData.addValidateCheck( _name, onValidate, nextValidate, message, onError );
					}
					
					if ( onServer ){
						addHiddenField( _formData, _name + "_CFFORM" + nextValidate.toUpperCase(), message );
					}
					if ( onBlur ){
						onBlurList.add( "!tf_validate_" + nextValidate + "( this )" );
					}

				}else{
					throw newRunTimeException( "invalid VALIDATE parameter, " + nextValidate );
				}
			}
    }
		
		//--[ if onBlur we need to append the required checks
		if ( onBlur && ( isRequired || onBlurList.size() > 0 ) ){
			_onBlurValue.append( "if ( " );
			if ( isRequired ){ // call the 'is required' function
				_onBlurValue.append( "!tf_element_has_value( this )" );
				if ( onBlurList.size() > 0 ){
					_onBlurValue.append( " && ( " );
				}
			}
			
			for ( int i = 0; i < onBlurList.size(); i++ ){
				_onBlurValue.append( onBlurList.get(i).toString() );
				if ( i < onBlurList.size()-1 ){
					_onBlurValue.append( " || " );
				}
			}
			
			if ( isRequired && onBlurList.size() > 0 ){
				_onBlurValue.append( " ) " );
			}

			// add consequenc of 'if' evaluating to true - display error message
			_onBlurValue.append( "){ alert(\'" );
			_onBlurValue.append( message );
			_onBlurValue.append( "\'); }" );

		}
	}
	
	private void rangeCheck( String _rangeVal ) throws cfmRunTimeException{
		if ( _rangeVal != null ){
			try{
				Integer.parseInt( _rangeVal );
			}catch( NumberFormatException e ){
				throw newRunTimeException( "Invalid RANGE attribute value. Failed to convert \"" + _rangeVal + "\" to an integer value" );
			}
		}
	}
   
	private void addHiddenField( cfFormInputData _formData, String _name, String _value ){
		_formData.appendToFooter( "<input type=\"hidden\" name=\"" + _name + "\" value=\"" + _value + "\"/>\r\n");
	}

  protected boolean getRequired( cfSession _Session ) throws cfmRunTimeException {
    boolean required = false;
    if ( containsAttribute("REQUIRED") ){
      cfData reqd = getDynamic(_Session, "REQUIRED");
      if ( reqd.getString().length() == 0 || reqd.getBoolean() ){
        required = true;
      }
    }
    return required;
  }

  
  protected String getValue( cfSession _Session, String _attr ) throws cfmRunTimeException{
  	if ( containsAttribute( _attr ) ){
  		return getDynamic( _Session, _attr ).getString();
  	}else{
  		return null;
  	}
  }

  protected Set<String> getIgnoreKeys(){
		Set<String> ignoreKeys = new HashSet<String>();
		ignoreKeys.add( "BIND" ); // flash only
		ignoreKeys.add( "TYPE" ); // already read
		ignoreKeys.add( "NAME" ); // already read
		ignoreKeys.add( "MASK" );
		ignoreKeys.add( "MESSAGE" );
		ignoreKeys.add( "RANGE" );
		ignoreKeys.add( "REQUIRED" );
		ignoreKeys.add( "LABEL" ); 
		ignoreKeys.add( "VALIDATE" );
		ignoreKeys.add( "VALIDATEAT" );
		ignoreKeys.add( "MESSAGE" );
		ignoreKeys.add( "PATTERN" );
		ignoreKeys.add( "ONERROR" );
		ignoreKeys.add( "ONVALIDATE" );
		ignoreKeys.add( "PASSTHROUGH" );
		return ignoreKeys;
  }

}
