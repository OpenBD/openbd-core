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
 *  $Id: Delete.java 2000 2012-03-25 14:13:44Z alan $
 */
package com.bluedragon.mongo.gridfs;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class Delete extends Add {
	private static final long serialVersionUID = 1L;

	public Delete(){  
		min = 3; 
		max = 5; 
		setNamedParams( new String[]{ "datasource", "bucket", "filename", "_id", "query" } ); 
	}
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"GridFS bucket name",
			"the name of the files to delete [defaults]",
			"the _id of the file to delete",
			"the query to delete"
		};
	}
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"mongo", 
				"Deletes a file(s) from the Mongo GridFS bucket.  You can delete specifying either file, _id or a query.", 
				ReturnType.BOOLEAN );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		// Get the necessary Mongo references
		DB			db	= getDataSource(_session, argStruct);
		GridFS	gridfs	= getGridFS(_session, argStruct, db);


		// Get the file information
		String filename	= getNamedStringParam(argStruct, "filename", null);
		if ( filename != null ){
			gridfs.remove(filename);
			return cfBooleanData.TRUE;
		}


		// Get the _id
		String _id	= getNamedStringParam(argStruct, "_id", null);
		if ( _id != null ){
			gridfs.remove( new ObjectId(_id) );
			return cfBooleanData.TRUE;
		}


		// Get the Query
		cfData mTmp	= getNamedParam(argStruct, "query", null);
		if ( mTmp != null ){
			gridfs.remove(convertToDBObject(mTmp));
			return cfBooleanData.TRUE;
		}
		
		throwException(_session, "Please specify file, _id or a query");
		return null;
	}
}