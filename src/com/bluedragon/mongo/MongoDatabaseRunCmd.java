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
 *  http://openbd.org/
 *  
 *  $Id: MongoDatabaseRunCmd.java 1770 2011-11-05 11:50:08Z alan $
 */
package com.bluedragon.mongo;

import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.MongoException;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class MongoDatabaseRunCmd extends MongoDatabaseList {
	private static final long serialVersionUID = 1L;

	public MongoDatabaseRunCmd(){  min = 2; max = 2; setNamedParams( new String[]{ "datasource", "cmd" } ); }
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"the command to send to the server.  Can be either a string or a structure"
		};
	}
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"mongo", 
				"Runs the given command against the database referenced by this datasource", 
				ReturnType.STRUCTURE );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		DB	db	= getDataSource( _session, argStruct );
		cfData	cmdData	= getNamedParam(argStruct, "cmd", null );
		if ( cmdData == null )
			throwException(_session, "please specify the cmd parameter");
		
		try{
			CommandResult cmr;
			
			if ( cmdData.getDataType() == cfData.CFSTRUCTDATA )
				cmr	= db.command( convertToDBObject( (cfStructData) cmdData ) );
			else
				cmr	= db.command( cmdData.getString() );
			
			return tagUtils.convertToCfData(cmr.toMap());
		} catch (MongoException me){
			throwException(_session, me.getMessage());
			return null;
		}
	}
}