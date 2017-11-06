/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 */
package com.bluedragon.mongo.gridfs;

import java.util.Iterator;
import java.util.List;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class Find extends FindOne {
	private static final long serialVersionUID = 1L;

	public Find(){  
		min = 3; 
		max = 4; 
		setNamedParams( new String[]{ "datasource", "bucket", "filename", "query" } ); 
	}
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"GridFS bucket name",
			"the name of the files to find [defaults]",
			"the query to find"
		};
	}
	
	
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"mongo", 
				"Finds a given file(s) from the Mongo GridFS bucket.  You can get the file specifying either file name, _id or a query.   You can download the actual file using MongoGridFSGet()", 
				ReturnType.ARRAY );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		// Get the necessary Mongo references
		DB			db	= getDB(_session, argStruct);
		GridFS	gridfs	= getGridFS(_session, argStruct, db);

		
		// Get the file information
		String filename	= getNamedStringParam(argStruct, "filename", null);
		if ( filename != null ){
			return toArray( gridfs.find(filename) );
		} else {
			
			cfData mTmp	= getNamedParam(argStruct, "query", null);
			if ( mTmp != null )
				return toArray( gridfs.find(getDBObject(mTmp)) );
		}

		throwException(_session, "Please specify file or a query");
		return null;
	}
	
	
	private cfArrayData toArray( List<GridFSDBFile> list ) throws cfmRunTimeException{
		cfArrayData arr	= cfArrayData.createArray(1);
		if ( list == null || list.size() == 0 )
			return arr;
		
		Iterator<GridFSDBFile> it	= list.iterator();
		while ( it.hasNext() ){
			GridFSDBFile f	= it.next();
			arr.addElement( toStruct(f) );
		}
		
		return arr;
	}
}