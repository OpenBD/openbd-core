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
 *  $Id: SalesForceDescribe.java 2131 2012-06-27 19:02:26Z alan $
 */
package org.alanwilliamson.openbd.plugin.salesforce;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

public class SalesForceDescribe extends SalesForceBaseFunction {
	private static final long serialVersionUID = 1L;

	public SalesForceDescribe(){
		min = 3; max = 4;
		setNamedParams( new String[]{ "email", "passwordtoken", "type", "timeout" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"SalesFoce Email address",
			"SalesForce password and token concatenated together",
			"The Type of the object to get information on",
			"the time in milliseconds, that the connection will wait for a response"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"salesforce-plugin", 
				"Retrieves the information about the object", 
				ReturnType.STRUCTURE );
	}

	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String	type	= getNamedStringParam(argStruct, "type", null );
		if ( type == null )
			throwException( _session, "type was not properly defined" );
		
    PartnerConnection connection = null;
    try {
    	connection = getConnection( _session, argStruct );

    	cfStructData	s = new cfStructData();
    	
      // Make the call out to salesforce
    	DescribeSObjectResult desc = connection.describeSObject(type);
    	
    	com.sforce.soap.partner.Field[] fields = desc.getFields();
    	for ( com.sforce.soap.partner.Field f : fields ){
    		
    		cfStructData	fm	= new cfStructData();

    		fm.setData("isautonumber", 							cfBooleanData.getcfBooleanData(f.isAutoNumber()) );
    		fm.setData("iscalculated", 							cfBooleanData.getcfBooleanData(f.isCalculated()) );
    		fm.setData("iscasesensitive", 					cfBooleanData.getcfBooleanData(f.isCaseSensitive()) );
    		fm.setData("iscreateable", 							cfBooleanData.getcfBooleanData(f.isCreateable()) );
    		fm.setData("iscustom", 									cfBooleanData.getcfBooleanData(f.isCustom()) );
    		fm.setData("isdefaultedoncreate", 			cfBooleanData.getcfBooleanData(f.isDefaultedOnCreate()) );
    		fm.setData("isdependentpicklist", 			cfBooleanData.getcfBooleanData(f.isDependentPicklist()) );
    		fm.setData("isdeprecatedandhidden", 		cfBooleanData.getcfBooleanData(f.isDeprecatedAndHidden()) );
    		fm.setData("isexternalid", 							cfBooleanData.getcfBooleanData(f.isExternalId()) );
    		fm.setData("isfilterable", 							cfBooleanData.getcfBooleanData(f.isFilterable()) );
    		fm.setData("isgroupable", 							cfBooleanData.getcfBooleanData(f.isGroupable()) );
    		fm.setData("ishtmlformatted", 					cfBooleanData.getcfBooleanData(f.isHtmlFormatted()) );
    		fm.setData("isidlookup", 								cfBooleanData.getcfBooleanData(f.isIdLookup()) );
    		fm.setData("isnamefield", 							cfBooleanData.getcfBooleanData(f.isNameField()) );
    		fm.setData("isnillable", 								cfBooleanData.getcfBooleanData(f.isNillable()) );
    		fm.setData("isrestrictedpicklist", 			cfBooleanData.getcfBooleanData(f.isRestrictedPicklist()) );
    		fm.setData("issortable", 								cfBooleanData.getcfBooleanData(f.isSortable()) );
    		fm.setData("isunique", 									cfBooleanData.getcfBooleanData(f.isUnique()) );
    		fm.setData("isupdateable", 							cfBooleanData.getcfBooleanData(f.isUpdateable()) );
    		fm.setData("iswriterequiresmasterread", cfBooleanData.getcfBooleanData(f.isWriteRequiresMasterRead()) );
    		
    		fm.setData("type", 											new cfStringData(f.getType().name()) );

    		s.setData( f.getName(), fm );
    	}

    	return s;

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
