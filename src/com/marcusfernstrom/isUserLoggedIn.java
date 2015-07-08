package com.marcusfernstrom;

import java.util.List;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;
import com.naryx.tagfusion.expression.function.getAuthUser;

/**
 * I Marcus Fernstrom hereby assign copyright of this code to the OpenBD project, to be licensed under the same terms as the rest of the code.
 */

/**
 * In order for this function to work properly, it must execute with the same
 * application settings that were used on the page where the user was logged in
 * via <cfloginuser> Specifically, the appName and loginStorage values must
 * be the same.
 */

public class isUserLoggedIn extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public isUserLoggedIn() {
		min = max = 0; // set the number of arguments allowed
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"security", 
				"Determines if the user specified is logged in (CFLOGINUSER)", 
				ReturnType.BOOLEAN );
	}
	
	@Override
	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		boolean isUserLoggedIn = false;
		String loginTokenValueEncoded = getAuthUser.getLoginTokenValue(_session);

		if (loginTokenValueEncoded != null) {
			Map<String, String> rolesForCurrentAuthUser = _session.getDataFromSecurityStore(loginTokenValueEncoded);
			if (rolesForCurrentAuthUser != null) {
				isUserLoggedIn = true;
			}
		}

		return cfBooleanData.getcfBooleanData(isUserLoggedIn);
	}

}
