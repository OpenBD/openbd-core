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

/**
 * I Marcus Fernstrom hereby assign copyright of this code to the OpenBD project.
 */

public class JwtCreate extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public JwtCreate() {
		min = 2;
		max = 3;
		setNamedParams( new String[]{ "private", "secret", "issuer", "algorithm", "subject", "audience", "expiration" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"A struct representing the private JWT claims",
			"The secret to use when signing and verifying the JWT token",
			"The issuer information (iss)",
			"Which algorithm to use, available ones are HMAC256, HMAC384, HMAC512. Defaults to HMAC 256",
			"Subject (sub)",
			"Audience (aud)",
			"Expiration (exp)"
		};
	}
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"security", 
				"Create and read JWT tokens.", 
				ReturnType.BOOLEAN );
	}
	
	
	@Override
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		// Prep variables
		Builder tokenBuilder = JWT.create();
		String token = "";
		Algorithm algo;
		cfData privateClaims;
		String key;

		// Grab the parameters
		String secret		= getNamedStringParam(argStruct, "secret", "" );
		String issuer		= getNamedStringParam(argStruct, "issuer", "" );
		String subject		= getNamedStringParam(argStruct, "subject", "" );
		String audience		= getNamedStringParam(argStruct, "audience", "" );
		String expiration	= getNamedStringParam(argStruct, "expiration", "" );
		String algorithm 	= getNamedStringParam(argStruct, "algorithm", "HMAC256" );
		privateClaims 		= getNamedParam(argStruct, "private");
		
		if (!privateClaims.isStruct())
			throwException(_session, "Parameter isn't of type STRUCTURE");
		
		Date expDate 		= new Date();
		

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
		    
		    if (expiration.length() > 0) {
		    		// TODO Turn cfml datestring into a Java date
				tokenBuilder.withExpiresAt(expDate);
			}
		    
		    // Set the private claims
		    cfStructData struct = (cfStructData) privateClaims;
		    
		    Object[] keys = struct.keys();
		    
			for (int i = 0; i < keys.length; i++) {
				key = (String) keys[i];
				tokenBuilder.withClaim(key, struct.getData(key).toNormalString());
			}
			
			// Sign and stringify final token
			token = tokenBuilder.sign(algo);
		    
		} catch (Exception e) {
			throwException(_session, e.getMessage());
		}	

		return new cfStringData(token);
	}

}
