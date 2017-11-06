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

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class FindOne extends Add {
	private static final long serialVersionUID = 1L;

	public FindOne(){  
		min = 3; 
		max = 5; 
		setNamedParams( new String[]{ "datasource", "bucket", "filename", "_id", "query" } ); 
	}
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"GridFS bucket name",
			"the name of the files to find [defaults]",
			"the _id of the file to find",
			"the query to find"
		};
	}
	
	
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"mongo", 
				"Finds a given file from the Mongo GridFS bucket.  You can get the file specifying either file name, _id or a query.  It multiple match, it will return the first one.   You can download the actual file using MongoGridFSGet()", 
				ReturnType.STRUCTURE );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		// Get the necessary Mongo references
		DB			db	= getDB(_session, argStruct);
		GridFS	gridfs	= getGridFS(_session, argStruct, db);

		
		// Get the file information
		String filename	= getNamedStringParam(argStruct, "filename", null);
		if ( filename != null ){
			return toStruct( gridfs.findOne(filename) );
		} else {
			
			String _id	= getNamedStringParam(argStruct, "_id", null);
			if ( _id != null ){
				return toStruct( gridfs.findOne( new ObjectId(_id) ) );
			} else {
				
				cfData mTmp	= getNamedParam(argStruct, "query", null);
				if ( mTmp != null )
					return toStruct( gridfs.findOne(getDBObject(mTmp)) );
			}
		}

		throwException(_session, "Please specify file, _id or a query");
		return null;
	}
	
	
	
	/**
	 * Converts a GridFSDBFile object into a struct
	 * 
	 * @param filefs
	 * @return
	 */
	protected cfStructData	toStruct( GridFSDBFile filefs ){
		cfStructData	s	= new cfStructData();
		if ( filefs == null )
			return s;
		
		s.put("_id", 				new cfStringData(filefs.getId().toString()) );
		s.put("chunkSize", 	new cfNumberData(filefs.getChunkSize()) );
		s.put("length", 		new cfNumberData(filefs.getLength()) );
		s.put("md5",			 	new cfStringData(filefs.getMD5()) );
		s.put("filename", 	new cfStringData(filefs.getFilename()) );
		s.put("contentType", new cfStringData(filefs.getContentType()) );
		s.put("uploadDate", new cfDateData(filefs.getUploadDate()) );
		
		s.put("metadata", 	tagUtils.convertToCfData( filefs.getMetaData() ) );
		
		return s;
	}
}