package com.naryx.tagfusion.expression.function;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class pbkdf2 extends functionBase {
	private static final long serialVersionUID = 1L;

	
	public pbkdf2() {
		min = 2;
		max = 5;
		setNamedParams( new String[]{ "data", "salt", "iterations", "version", "keylength" } );
	}

	
	public String[] getParamInfo() {
		return new String[] { 
			"data",
			"salt",
			"iterations",
			"version",
			"keylength"
		};
	}

	
	@SuppressWarnings("rawtypes")
	public java.util.Map getInfo() {
		return makeInfo(
				"security", 
				"Hashes a string according to the PBKDF2 algorithm using SHA. sha parameter is optional and defaults to SHA512. iteration defaults to 50000. Returns a base64 encoded string.", 
				ReturnType.STRING);
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		cfData data 		= getNamedParam(argStruct, "data");
		String salt 		= getNamedStringParam( argStruct, "salt", "" );
		Integer iterations 	= getNamedIntParam( argStruct, "iterations", 50000 );
		Integer keylength 	= getNamedIntParam( argStruct, "keylength", 256 );
		String sha 			= getNamedStringParam( argStruct, "version", "SHA512" );

		if( salt.length() == 0 ) {
			throwException( _session, "You have to specify a salt when using PBKDF2" );
		}
		
		try {
           SecretKeyFactory skf = SecretKeyFactory.getInstance( "PBKDF2WithHmac" + sha );
           char[] charArray 	= data.toNormalString().toCharArray();
           byte[] saltArray 	= salt.getBytes();
           PBEKeySpec spec 		= new PBEKeySpec( charArray, saltArray, iterations, keylength );
           SecretKey key 		= skf.generateSecret( spec );
           String out 			= Base64.getEncoder().encodeToString(key.getEncoded());

           return new cfStringData( out );
 
       } catch( Exception e ) {
    	   throwException( _session, e.getMessage() );
       }
		
		return null; // keep compiler happy
	}
}
