/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  http://openbd.org/
 *  
 *  $Id: SalesForceBaseFunction.java 2131 2012-06-27 19:02:26Z alan $
 */

package org.alanwilliamson.openbd.plugin.salesforce;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class SalesForceBaseFunction  extends functionBase {
	private static final long serialVersionUID = 1L;

	
	/**
	 * Ges the Connection to SalesForce using the WSC wrapper
	 * 
	 * @param _session
	 * @param argStruct
	 * @return
	 * @throws cfmRunTimeException
	 * @throws ConnectionException
	 */
	protected PartnerConnection getConnection(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException, ConnectionException {
		String	email	= getNamedStringParam(argStruct, "email", null );
		if ( email == null )
			throwException( _session, "email was not properly defined" );

		String	passwordtoken	= getNamedStringParam(argStruct, "passwordtoken", null );
		if ( passwordtoken == null )
			throwException( _session, "passwordtoken was not properly defined" );

		// Make the connection to SalesForce
		ConnectorConfig config = new ConnectorConfig();
    config.setUsername(email);
    config.setPassword(passwordtoken);
    
    int timeout =	getNamedIntParam(argStruct, "timeout", -1 );
    if ( timeout > 0 )
      config.setReadTimeout(timeout);

    return Connector.newConnection(config);
	}
	
}
