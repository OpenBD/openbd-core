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

import org.apache.oro.text.regex.MalformedPatternException;

import com.nary.util.string;
import com.nary.util.date.dateTimeTokenizer;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.parser.CFUndefinedValue;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.expression.function.xml.IsXml;


public class cfPARAM extends cfTag implements Serializable
{

  static final long serialVersionUID = 1;
  
  private static final String USER_RE = "(([0-9a-zA-Z\\?:@&=$\\-_\\.+!*'(),])|(%[0-9A-Fa-f]+))+";
  private static final String HOST_RE = "([a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*)";
  private static final String PORT_RE = "(:[0-9]+)?";
  public static final String EMAIL_RE = "[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@([a-zA-Z_0-9-]+\\.)+[a-zA-Z]{2,9}$"; 
  public static final String URL_RE = "^((http|ftp|https|ftps|sftp)://)(" + USER_RE + ":([a-zA-Z0-9$-_\\.+;?&=]+)?@)?" + HOST_RE + PORT_RE + "(/[a-zA-Z0-9\\&%_\\,\\'\\$\\./+\\?=~#\\-]*)?$";
  private static final String FSEGMENT_RE = "(([0-9a-zA-Z\\?:@&=$-_\\.+])|(%[0-9A-Fa-f]+)+)";
  private static final String FILE_URL_RE = "^file://" + HOST_RE + "?/" + FSEGMENT_RE + "*(/" + FSEGMENT_RE + "*)*$";
  private static final String NEWS_URL_RE = "^news:" + HOST_RE + PORT_RE + "$";
  private static final String MAILTO_URL_RE = "^mailto:" + EMAIL_RE.substring( 1, EMAIL_RE.length()-1 ) + "$";
  public static final String VARNAME_RE = "[A-Za-z_]+([A-Za-z_0-9]+)?(\\.[A-Za-z_0-9]+)*";
  public static final String SSN_RE = "^[0-9]{3}[\\- ][0-9]{2}[\\- ][0-9]{4}$"; // SSN is a 9 digit number of the format NNN-NN-NNNN
  public static final String PHONE_RE = "^((1[ \\.-]?)?(\\(?[2-9][0-9]{2}\\)?))?[ \\.\\-]?[2-9][0-9]{2}[ \\.\\-]?[0-9]{4}( x?[0-9]{1,5})?$"; // US phone number (see http://www.nanpa.com/area_codes/index.html)
  public static final String ZIPCODE_RE = "^[0-9]{5}([\\- ][0-9]{4})?$";
  public static final String UUID_RE = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{16}";
  public static final String GUID_RE = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
  
	public java.util.Map getInfo(){
		return createInfo("control", "Used to check if a variable exists, and validates its type and value. If the specified variable doesn't exist a default value can also be supplied at this point.");
	}
  
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo("NAME", "Provide a name for the parameter to be checked.", "", true ),
				createAttInfo("DEFAULT", "A value to be applied to this parameter if it does not exist at the time of this check.", "", false ),
				createAttInfo("TYPE", "By providing a data type to validate the expected parameter (e.g. QUERY, STRUCT), an error will be thrown if the wrong type is passed in. Other type options include \"range\", \"regex\" or \"regular_expression\" to validate the value of a parameter.", "ANY", false ),
				createAttInfo("MIN", "The minimum value this parameter should have, only used when TYPE = \"range\". This is a required attribute when TYPE = \"range\".", "", false ),
				createAttInfo("MAX", "The maximum value this parameter should have, only used when TYPE = \"range\". This is a required attribute when TYPE = \"range\".", "", false ),
				createAttInfo("PATTERN", "A javascript regex pattern this parameter should conform to, only used when TYPE = \"regex\" or \"regular_expression\". This is a required attribute when TYPE = \"regex\" or \"regular_expression\".", "", false )
		};
	}
  
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
  	defaultAttribute( "TYPE", "any" );
  	
    parseTagHeader( _tag );
    
    if ( !containsAttribute("NAME") )
    	throw newBadFileException( "Missing NAME", "You need to provide a NAME" );
  }
  
  public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
  
  	//--[ Check to see that a name value has been given.
  	//--[ If not then simply return
 		String NAME = getDynamic( _Session, "NAME").getString().trim();
 		if ( NAME.length() == 0 )
 			throw newRunTimeException( "Invalid NAME attribute" );
 		
 		cfData data = runTime.runExpression( _Session, NAME, false );
		if ( ((cfLData)data).exists() ) {
			cfData val = ((cfLData)data).Get( _Session.getCFContext() );
			if ( val != CFUndefinedValue.UNDEFINED ) {
				checkDataType( _Session, val, NAME, false );
				return cfTagReturnType.NORMAL;
			}
		}

		// variable doesn't exist or is undefined
		if ( !containsAttribute( "DEFAULT" ) ) {
			throw newRunTimeException( "Required variable [" + NAME + "] is not available" );
		} else {
			cfData defaultVal = getDynamic( _Session, "DEFAULT" );
			checkDataType( _Session, defaultVal, NAME, true );
			((cfLData)data).Set( defaultVal, _Session.getCFContext() );
		}
		
		return cfTagReturnType.NORMAL;
  }
  
  /*
   * Checks that the given value is within the range specified by the min and max params.
   * If _min is null then only the upper limit will be checked. Similarly if _max is null
   * only the lower limit will be checked.
   */
  
  public void validateRange( cfSession _Session, cfData _value ) throws cfmRunTimeException{
  	
		cfNumberData min = containsAttribute( "MIN" ) ? getDynamic( _Session, "MIN" ).getNumber() : null;
		cfNumberData max = containsAttribute( "MAX" ) ? getDynamic( _Session, "MAX" ).getNumber() : null;
		if ( min == null && max == null ){
			throw newRunTimeException( "Missing attributes \"MAX\" and/or \"MIN\" are required when TYPE=\"range\"." );
		}
		
		if ( min != null && max != null && cfData.compare( min, max ) > 0 ){
			throw newRunTimeException( "Invalid range: the maximum value must be greater than the minimum value." );
		}

  	if ( min != null && cfData.compare( _value, min ) < 0 ){
  		throw newRunTimeException( "Value must be greater than or equal to " + min.getString() );
  	}
  	
  	if ( max != null && cfData.compare( _value, max ) > 0 ){
  		throw newRunTimeException( "Value must be less than or equal to " + max.getString() );
  	}
  	
  }

  public static boolean validateRE( String _regex, cfData _value ) throws dataNotSupportedException, MalformedPatternException{
		return validateRE( _regex, _value.getString() );
  }

  public static boolean validateRE( String _regex, String _value ) throws MalformedPatternException {
  	return string.regexMatches( _value.trim(), _regex );
  }

  private void checkDataType( cfSession _Session, cfData data, String _name, boolean isDefault ) throws cfmRunTimeException {
  	String type = getDynamic( _Session, "TYPE" ).getString().toLowerCase();
  	if ( type.equals( "regex" ) || type.equals( "regular_expression" ) ){ 
  		if ( !containsAttribute( "PATTERN" ) ){
  			throw newRunTimeException( "Missing attribute PATTERN is required when TYPE=\"" + type + "\"." );
  		}
  		String re = getDynamic( _Session, "PATTERN" ).getString();
  		try {
				if ( !validateRE( re, data ) ){
					throw newRunTimeException( ( isDefault ? "Default value" : "Variable" ) + " [" + _name + "] does not match the regular expression [" + re + "]" );
				}
			} catch (MalformedPatternException e) {
				throw newRunTimeException( "Invalid regular expression [" + re + "]" );
			}

  	}else if ( type.equals( "range" ) ){
  		validateRange( _Session, data);

  	}else{
  		try{
	  		if ( !checkDataType( type, data ) ){
	  			throw newRunTimeException( "Variable [" + _name + "] is not of type [" + getDynamic( _Session, "TYPE" ).getString() + "]" );
	  		}
  		}catch( IllegalArgumentException e ){
  			throw newRunTimeException( e.getMessage() );
  		}
  	}
  }
  

  public static boolean checkDataType( String type, cfData data ){ 
  	
  	try{
	  	if ( type.equals("any") )
	  		return true;
	  	else if ( type.equals("string") ){
				data.getString();
	  		return true;
	  	}else if ( type.equals("array") && data.getDataType() == cfData.CFARRAYDATA )
	  		return true;
	  	else if ( type.equals("binary") && data instanceof cfBinaryData )
	  		return true;
	  	else if ( type.equals("boolean") ){
	  	 	if ( data.getDataType() == cfData.CFBOOLEANDATA )
		  		return true;
		  		
		  	data.getBoolean(); // purposefully throws an exception if this is not a boolean
		  	return true;
	  	}else if ( type.equals("date") || type.equals( "time" ) ){
	  		data.getDateData();
				return true;
	  	}else if ( type.equals("xml") ){
				return IsXml.check( data );
	  	}else if ( type.equals("numeric") ){
	  	 	
	  		try{
	  			data.getNumber();
	  			return true;
	  		}catch( dataNotSupportedException e ){
	  			return false;
	  		}
	  		
	  	}else if ( type.equals( "integer" ) ){
	  		
	  		try{
	  			cfNumberData d = data.getNumber();
	  			if ( d.getString().indexOf(".") == -1 )
	  				return true;
	  		}catch( dataNotSupportedException e ){
	  			return false;
	  		}
	  		
	  	}else if ( type.equals( "float" ) ){
	  		
	  		try{
	  			cfNumberData d = data.getNumber();
	  			if ( d.getString().indexOf(".") != -1 )
	  				return true;
	  		}catch( dataNotSupportedException e ){
	  			return false;
	  		}
	  		
	  	}else if ( type.equals( "email" ) ){
	  		String re = EMAIL_RE;
	  		return validateRE( re, data );
	  	}else if ( type.equals( "url" ) ){
	  		return isUrl( data.getString() );
	  	}else if ( type.equals( "variablename" ) ){
	  		return validateRE( VARNAME_RE, data );
	  	}else if ( type.equals("query") && data.getDataType() == cfData.CFQUERYRESULTDATA )
	  		return true;
	  	else if ( type.equals( "creditcard" ) ){
	  		return isCreditCard( data.getString() );
	  	}else if ( type.equals( "usdate" ) ){
	  		if ( data.getDataType() == cfData.CFDATEDATA ){
	  			return false;
	  		}
	  		return dateTimeTokenizer.getUSDate( data.getString() ) != null;
	  	}else if ( type.equals( "eurodate" ) ){
	  		if ( data.getDataType() == cfData.CFDATEDATA ){
	  			return false;
	  		}

	  		return dateTimeTokenizer.getUKDate( data.getString() ) != null;
	  	}else if ( type.equals( "ssn" ) || type.equals( "social_security_number" ) ) 
	  		return validateRE( SSN_RE, data );
	  	else if ( type.equals( "telephone" ) ) 
	  		return validateRE( PHONE_RE, data );
	  	else if ( type.equals( "zipcode" ) )
	  		return validateRE( ZIPCODE_RE, data );
	  	else if ( type.equals("struct") && data.getDataType() == cfData.CFSTRUCTDATA )
	  		return true;
	  	else if ( type.equals( "uuid" ) )
	  		return validateRE( UUID_RE, data );
	  	else if ( type.equals( "guid" ) )
	  		return validateRE( GUID_RE, data );
	  	else
	  		throw new IllegalArgumentException( "Invalid type \"" + type + "\" specified" );
  	}catch( dataNotSupportedException E ){
  	} catch (MalformedPatternException e) {} // won't happen since the reg exps are set by us

  	
  	return false;
  }
  
  public static boolean isUrl( String _url ) throws MalformedPatternException{
		String re = null;
		if ( _url.startsWith( "mailto:" ) ){
			re = MAILTO_URL_RE;
		}else if ( _url.startsWith( "news:" ) ){
			re = NEWS_URL_RE;
		}else if ( _url.startsWith( "file:" ) ){
			re = FILE_URL_RE; 
		}else{
			re = URL_RE; 
		}
		return validateRE( re, _url );
  }
  
  public static boolean isCreditCard( String _cc ) throws MalformedPatternException{
		// firstly check the number only contains numerics and separator chars
		if ( !validateRE( "[0-9 \\.-]+", _cc ) ){
			return false;
		}
		
		// now create string with just the digits contained
		char[] ccChars = _cc.toCharArray();
		StringBuilder numerics = new StringBuilder();
		for ( int i = 0; i < ccChars.length; i++ ){
			if ( ccChars[i] >= '0' && ccChars[i] <= '9' ){
				numerics.append( ccChars[i] );
			}
		}
		return checkLuhn( numerics.toString() );

  }
  
  // performs the Luhn (or mod 10) algorithm used for credit card numbers
  // as a checksum
	private static boolean checkLuhn(String purportedCC) {
		int sum = 0;
		int nDigits = purportedCC.length();
		int parity = nDigits%2;
		for ( int i=0; i < nDigits; i++ ){
			int digit = purportedCC.charAt(i) - '0';
			if ( i%2 == parity ){
				digit = digit * 2;
			}
			if ( digit > 9 ){
				digit = digit - 9;
			}
			sum = sum + digit;
    }
		return (sum%10) == 0;
	}


}
