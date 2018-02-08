package com.marcusfernstrom.jwt;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;
import com.naryx.tagfusion.expression.function.string.serializejson;
import com.naryx.tagfusion.expression.function.string.serializejson.CaseType;
import com.naryx.tagfusion.expression.function.string.serializejson.DateType;

/**
 * I Marcus Fernstrom hereby assign copyright of this code to the OpenBD project.
 */

public class JwtCreate extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public JwtCreate() {
		min = 2;
		max = 7;
		setNamedParams( new String[]{ "private", "secret", "issuer", "algorithm", "subject", "audience", "expiration" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"A struct representing the private JWT claims",
			"The secret to use when signing and verifying the JWT token",
			"The issuer information (iss)",
			"Which algorithm to use, available ones are HMAC256, HMAC384, HMAC512. Defaults to HMAC256",
			"Subject (sub)",
			"Audience (aud)",
			"Expiration (exp), seconds since epoch."
		};
	}
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"security", 
				"Create JWT tokens. See <a href=\"https://jwt.io/\" target=\"_blank\">jwt.io/</a> for information about JWT's. ", 
				ReturnType.BOOLEAN );
	}
	
	
	@Override
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		// Prep variables
		Builder tokenBuilder = JWT.create();
		String token = "";
		Algorithm algo;

		// Grab the parameters
		String secret			= getNamedStringParam(argStruct, "secret", "" );
		String issuer			= getNamedStringParam(argStruct, "issuer", "" );
		String subject			= getNamedStringParam(argStruct, "subject", "" );
		String audience			= getNamedStringParam(argStruct, "audience", "" );
		Integer expiration		= getNamedIntParam(argStruct, "expiration", -1 );
		String algorithm 		= getNamedStringParam(argStruct, "algorithm", "HMAC256" );
		cfData privateClaims 	= getNamedParam(argStruct, "private");
		
		if (!privateClaims.isStruct())
			throwException(_session, "Parameter isn't of type STRUCTURE");

		try {
			// Set the algorithm, default to HMAC256 if no match
			switch(algorithm) {
			case "HMAC384":
				algo = Algorithm.HMAC384(secret);
				break;
				
			case "HMAC512":
				algo = Algorithm.HMAC512(secret);
				break;
				
			default:
				algo = Algorithm.HMAC256(secret);
				break;
			}
			
		    // Set the public claims
			tokenBuilder.withIssuer(issuer);
		    
		    if (subject.length() > 0) {
				tokenBuilder.withSubject(subject);
			}
		    
		    if (audience.length() > 0) {
				tokenBuilder.withAudience(audience);
			}
		    
		    if (expiration > -1) {
		    		tokenBuilder.withExpiresAt(new Date(expiration));
			}
		    
		    // Set the private claims
		    cfStructData struct = (cfStructData) privateClaims;

		    Object[] thekeys = struct.keys();
		    for ( int i = 0; i < thekeys.length; i++ ) {
		    		String key2 = (String)thekeys[ i ];
		    		cfData val = struct.getData( key2 );
				
		    		if( val.getDataTypeName() == "boolean" ) {
		    			tokenBuilder.withClaim(key2, val.getBoolean());
		    			
		    		} else {
		    			if( cfData.isSimpleValue(val) ){
		    				tokenBuilder.withClaim(key2, val.getString());
		    			} else {
		    				// Let's turn our complex data into json
		    				StringBuilder buffer 			= new StringBuilder(5000);
		    				
		    				// Use the existing openbd json serializer
		    				serializejson jsonserializer 	= new serializejson();
		    				DateType datetype 				= DateType.LONG;
		    				CaseType caseConversion 			= CaseType.MAINTAIN;
		    				
		    				jsonserializer.encodeJSON(buffer, val, false, caseConversion, datetype);
		    				tokenBuilder.withClaim(key2, buffer.toString());
		    			}
		    		}
		    	}
			
			// Sign and stringify final token
			token = tokenBuilder.sign(algo);
		    
		} catch (Exception e) {
			throwException(_session, e.getMessage());
		}	

		return new cfStringData(token);
	}

}
