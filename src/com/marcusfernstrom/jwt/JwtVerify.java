package com.marcusfernstrom.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class JwtVerify extends functionBase{
	private static final long serialVersionUID = 1L;
	
	public JwtVerify() {
		min = 2;
		max = 4;
		setNamedParams( new String[]{ "token", "secret", "issuer", "algorithm" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"Token to verify",
			"Secret to use for verification",
			"Issuer to use for verification",
			"Which algorithm to use, defaults to HMAC256"
		};
	}
	
	@Override
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String token 		= getNamedStringParam(argStruct, "token", "" );
		String secret 		= getNamedStringParam(argStruct, "secret", "" );
		String issuer 		= getNamedStringParam(argStruct, "issuer", "" );
		String algorithm 	= getNamedStringParam(argStruct, "algorithm", "HMAC256" );
		
		Algorithm algo;
		Boolean verified = false;
		
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
		
			
			try {
				// If this doesn't throw an error, it's verified
				Verification verifier = JWT.require(algo);
			    
			    	verifier.withIssuer(issuer);
			    
			    @SuppressWarnings("unused")
				DecodedJWT jwt = verifier.build().verify(token);
			    
			    verified = true;
			} catch (JWTVerificationException exception){
				verified = false;
			}
		} catch (Exception e) {
			throwException(_session, e.getMessage());
		}
		
		return (verified == true) ? cfBooleanData.TRUE : cfBooleanData.FALSE;
	}
}
