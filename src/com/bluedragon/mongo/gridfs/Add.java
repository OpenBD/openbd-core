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
 *  $Id: Add.java 2396 2013-07-16 18:13:03Z alan $
 */
package com.bluedragon.mongo.gridfs;

import java.io.File;
import java.io.IOException;

import com.bluedragon.mongo.MongoCollectionInsert;
import com.mongodb.DB;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class Add extends MongoCollectionInsert {
	private static final long serialVersionUID = 1L;

	public Add(){  
		min = 4; 
		max = 7; 
		setNamedParams( new String[]{ "datasource", "bucket", "file", "filename", "contenttype", "_id", "metadata" } ); 
	}
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"GridFS bucket name",
			"the file to store in the bucket.  Either a full path to the file or the binary data of a data",
			"the name of the file",
			"the mime type of the file",
			"the ID of the file if you wish to replace or override this data",
			"any meta-data to associate with this file"
		};
	}
	
	
	public java.util.Map getInfo(){
		return makeInfo(
				"mongo", 
				"Saves a file into a Mongo GridFS bucket returning back the _id of the resulting object", 
				ReturnType.STRING );
	}
	
	protected GridFS getGridFS(cfSession _session, cfArgStructData argStruct, DB db ) throws cfmRunTimeException {
		String bucket	= getNamedStringParam(argStruct, "bucket", null);
		if ( bucket == null )
			throwException(_session, "please specify a bucket");
	
		return new GridFS(db, bucket);
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		// Get the necessary Mongo references
		DB			db	= getDataSource(_session, argStruct);
		GridFS	gridfs	= getGridFS(_session, argStruct, db);
		GridFSInputFile fsInputFile = null;
		
		// Get the file information
		String filename	= getNamedStringParam(argStruct, "filename", null);
		if ( filename == null )
			throwException(_session, "please specify a filename");
		
		try{
			
			cfData	ftmp	= getNamedParam(argStruct, "file", null);
			if ( ftmp.getDataType() == cfData.CFBINARYDATA ){
				fsInputFile	= gridfs.createFile( ((cfBinaryData)ftmp).getByteArray() );
			}else{
				// The 'file' parameter is a string, which means it is a path to a file
				
				File	inputFile	= new File( ftmp.getString() );
				if ( !inputFile.exists() )
					throwException(_session, "File:" + inputFile + " does not exist" );
				
				if ( !inputFile.isFile() )
					throwException(_session, "File:" + inputFile + " is not a valid file" );
				
				fsInputFile	= gridfs.createFile(inputFile);
			}
			
		} catch (IOException e) {
			throwException(_session, e.getMessage() );
		}
		
		fsInputFile.setFilename(filename);
		
		String contenttype	= getNamedStringParam(argStruct, "contenttype", null);		
		if ( contenttype != null )
			fsInputFile.setContentType(contenttype);

		
		String _id	= getNamedStringParam(argStruct, "_id", null);
		if ( _id != null )
			fsInputFile.setId( _id );

		
		// Get and set the metadata
		cfData mTmp	= getNamedParam(argStruct, "metadata", null);
		if ( mTmp != null )
			fsInputFile.setMetaData(convertToDBObject(mTmp));
		
		
		// Save the Object
		try{
			fsInputFile.save();
			return new cfStringData( fsInputFile.getId().toString() );
		} catch (MongoException me){
			throwException(_session, me.getMessage());
			return null;
		}
	}
}