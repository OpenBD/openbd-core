/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  
 *  $Id: SalesForceDelete.java 2131 2012-06-27 19:02:26Z alan $
 */
package org.alanwilliamson.openbd.plugin.salesforce;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

public class SalesForceDelete extends SalesForceBaseFunction {
	private static final long serialVersionUID = 1L;

	public SalesForceDelete(){
		min = 3; max = 4;
		setNamedParams( new String[]{ "email", "passwordtoken", "id", "timeout" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"SalesFoce Email address",
			"SalesForce password and token concatenated together",
			"The Id for this object to update",
			"the time in milliseconds, that the connection will wait for a response"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"salesforce-plugin", 
				"Deletes the object within SalesForce at the given Id", 
				ReturnType.BOOLEAN );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String	id	= getNamedStringParam(argStruct, "id", null );
		if ( id == null )
			throwException( _session, "id was not properly defined" );
		
    PartnerConnection connection = null;
    try {
    	connection = getConnection( _session, argStruct );

      // Make the call out to salesforce
      DeleteResult[] delresult = connection.delete(new String[]{id});
      return cfBooleanData.getcfBooleanData( delresult[0].getSuccess() );

    }catch(ConnectionException e){
    	throwException( _session, e.getMessage() );
    }finally{
    	if ( connection != null ){
				try {
					connection.logout();
				} catch (ConnectionException e) {}
    	}
    }

    return null;
	}

}
