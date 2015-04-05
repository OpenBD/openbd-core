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
 *  $Id: SalesForceCreate.java 2131 2012-06-27 19:02:26Z alan $
 */
package org.alanwilliamson.openbd.plugin.salesforce;

import java.util.Iterator;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

public class SalesForceCreate extends SalesForceBaseFunction {
	private static final long serialVersionUID = 1L;

	public SalesForceCreate(){
		min = 4; max = 5;
		setNamedParams( new String[]{ "email", "passwordtoken", "type", "fields", "timeout" } );
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"SalesFoce Email address",
			"SalesForce password and token concatenated together",
			"The name of the SalesForce object.  Case matters.  Custom objects should end with __c",
			"Structure of all the fields that will be set in this object",
			"the time in milliseconds, that the connection will wait for a response"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"salesforce-plugin", 
				"Inserts a new Object into SalesForce, returning the new ID that was created", 
				ReturnType.STRING );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String	type	= getNamedStringParam(argStruct, "type", null );
		if ( type == null )
			throwException( _session, "type was not properly defined" );
		
		cfData	tmpData	= getNamedParam(argStruct, "fields", null );
		if ( tmpData == null || tmpData.getDataType() != cfData.CFSTRUCTDATA )
			throwException( _session, "fields was not properly defined; it must be a structure" );

		cfStructData	fieldsData	= (cfStructData)tmpData;
		
    PartnerConnection connection = null;
    try {
    	connection = getConnection( _session, argStruct );

      SObject so = new SObject();
      so.setType( type );

      Iterator<String>	it	= fieldsData.keySet().iterator();
      while ( it.hasNext() ){
      	String fieldName	= it.next();
      	so.setField( fieldName, tagUtils.getNatural( fieldsData.getData(fieldName)) );
      }

      // Make the call out to salesforce
      SaveResult[] saveresult = connection.create(new SObject[]{so});
      return new cfStringData( saveresult[0].getId() );

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
