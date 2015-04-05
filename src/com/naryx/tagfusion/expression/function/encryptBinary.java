/* 
 *  Copyright (C) 2000 - 2014 TagServlet Ltd
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
 *  $Id: encryptBinary.java 2461 2014-12-17 11:48:57Z alan $
 */

package com.naryx.tagfusion.expression.function;

import java.util.List;

import com.nary.security.Cryptography;
import com.nary.security.encrypter;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class encryptBinary extends encrypt{

	private static final long serialVersionUID = 1L;
	
	private int mode;
	
	public encryptBinary(){
		this( Cryptography.ENCRYPT_MODE );
	}

	protected encryptBinary( int _mode ){
    min = 2;
    max = 6;
    mode = _mode;
	}
	
	public String[] getParamInfo(){
		return new String[]{
				"binary object to encrypt",
				"encryption key",
				"encryption algorithm to be applied. If not specified, a default of BD_DEFAULT will be used. The CFMX_COMPAT algorithm option is not supported.",
				"the encoding - uu (default), hex or base64 are valid options",
				"the salt to be applied in encryption",
				"the number of iterations"
		};
	}
  
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"security", 
				"Encrypts the given binary with the optional parameters", 
				ReturnType.BINARY );
	}
	
  public cfData execute( cfSession _session, List<cfData> parameters ) throws cfmRunTimeException{
    // parameters - bytes, key[, algorithm[, IVorSalt[, iterations]]]]
  	cfData cfdata = parameters.get( parameters.size()-1 ); 
  	if ( cfdata.getDataType() != cfData.CFBINARYDATA ){
  		throwException( _session, "Invalid argument. Binary data expected." );
  	}
  	
  	byte [] data = ( (cfBinaryData) cfdata ).getByteArray();
  	String key = parameters.get( parameters.size()-2 ).getString();
 
  	String fullAlgorithm = getAlgorithm( _session, parameters );
  	byte [] ivOrSalt = getIvOrSalt( _session, parameters, 3 );
  	
  	if ( fullAlgorithm.equalsIgnoreCase( "BD_DEFAULT" ) ){
      return new cfBinaryData( mode == Cryptography.ENCRYPT_MODE ? encrypter.encrypt( data, key ) : encrypter.decrypt( data, key ) );
  	}else if ( fullAlgorithm.equalsIgnoreCase( "BDNET62_COMPAT" ) ){
        return new cfBinaryData( mode == Cryptography.ENCRYPT_MODE ? encrypter.encrypt( data, key ) : encrypter.decrypt( data, key ) );
  	}else{ 
	  	int iterations = getIterations( _session, parameters, 4 );
	  	
			byte[] encrypted;
			try {
				encrypted = Cryptography.doCipher( mode, data, fullAlgorithm, 
						key.getBytes(), ivOrSalt, iterations );
		  	return new cfBinaryData( encrypted ); 

			} catch (Exception e) {
				throwException( _session, e.getMessage() );
				return null; // keep compiler happy
			}

  	}
  } 

}
