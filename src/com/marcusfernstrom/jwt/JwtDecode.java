package com.marcusfernstrom.jwt;

import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;
import com.naryx.tagfusion.expression.function.string.DeserializeJSONJackson;

public class JwtDecode extends functionBase{
	private static final long serialVersionUID = 1L;
	
	public JwtDecode() {
		min = 1;
		max = 2;
		setNamedParams( new String[]{ "token", "deserialize" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"Token to decode",
			"Boolean. Defaults to false. Set to true to automatically deserialize complex data in claims."
		};
	}
	
	@Override
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String token = getNamedStringParam(argStruct, "token", "" );
		Boolean autoDeserialize = getNamedBooleanParam(argStruct, "deserialize", false );
		
		cfStructData returnStruct = new cfStructData();
		DecodedJWT jwt = null;
		
		try {
		    jwt = JWT.decode(token);
		} catch (JWTDecodeException exception){
			throwException(_session, "Invalid JWT token");
		}
		
		Map<String, Claim> theClaims;
		
		theClaims = JWT.decode(token).getClaims();
		
		((cfStructData) returnStruct).setData("algorithm", jwt.getAlgorithm());
		
		for (Map.Entry<String, Claim> entry : theClaims.entrySet()) {
			String tokenAsString = entry.getValue().asString();
			
			// Make sure we want to auto deserialize, that it's a string (not null), and that it's likely a json string.
			if( autoDeserialize && tokenAsString != null && checkJson(tokenAsString) ) {
				// Deserialize and add it to the struct to return
				cfData r;

				cfArgStructData argStructForDeserializing = new cfArgStructData();
				argStructForDeserializing.setData("jsonstring", tokenAsString);
				r = new DeserializeJSONJackson().execute( _session, argStructForDeserializing );
				((cfStructData) returnStruct).setData(entry.getKey(), r);
				
			} else {
				// It's not a JSON string or automatic deserializing is false
				
				// Feels like there should be a better way of doing this but I'm not sure how
				Integer tokenAsInt = entry.getValue().asInt();
				Boolean tokenAsBool = entry.getValue().asBoolean();
				
				if( tokenAsString != null ) {
					((cfStructData) returnStruct).setData(entry.getKey(), tokenAsString);
				}
				
				if( tokenAsInt != null ) {
					((cfStructData) returnStruct).setData(entry.getKey(), tokenAsInt);
				}
				
				if( tokenAsBool != null ) {
					if( tokenAsBool ) {
						((cfStructData) returnStruct).setData(entry.getKey(), "YES");
					} else {
						((cfStructData) returnStruct).setData(entry.getKey(), "NO");
					}
				}
			}
		}

		return returnStruct;
	}
	
	private Boolean checkJson(String dataToCheck)throws cfmRunTimeException{
	  	String jsonString = dataToCheck;
	  	
	  	if ( jsonString.startsWith("{") && jsonString.endsWith("}") ){
				return true;
	  	}else if ( jsonString.startsWith("[") && jsonString.endsWith("]") ){
				return true;
	  	}else if ( jsonString.startsWith("\"") && jsonString.endsWith("\"") ){
	  		return true;
	  	}else
		   	return false;
	}

}
