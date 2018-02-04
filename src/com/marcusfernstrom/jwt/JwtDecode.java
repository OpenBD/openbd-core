package com.marcusfernstrom.jwt;

import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class JwtDecode extends functionBase{
	private static final long serialVersionUID = 1L;
	
	public JwtDecode() {
		min = 1;
		max = 1;
		setNamedParams( new String[]{ "token" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"Token to decode"
		};
	}
	
	@Override
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String token = getNamedStringParam(argStruct, "token", "" );
		cfStructData theStruct = new cfStructData();
		DecodedJWT jwt = null;
		
		try {
		    jwt = JWT.decode(token);
		} catch (JWTDecodeException exception){
			throwException(_session, "Invalid JWT token");
		}
		
		Map<String, Claim> theClaims;
		
		theClaims = JWT.decode(token).getClaims();
		
		((cfStructData) theStruct).setData("algorithm", jwt.getAlgorithm());
		
		for (Map.Entry<String, Claim> entry : theClaims.entrySet()) {
			System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue().asString());
			((cfStructData) theStruct).setData(entry.getKey(), entry.getValue().asString());
		}

		return theStruct;
	}

}
