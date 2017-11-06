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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class Get extends Add {
	private static final long serialVersionUID = 1L;

	public Get(){  
		min = 3; 
		max = 7; 
		setNamedParams( new String[]{ "datasource", "bucket", "filepath", "filename", "_id", "query", "overwrite" } ); 
	}
  
	public String[] getParamInfo(){
		return new String[]{
			"datasource name.  Name previously created using MongoRegister",
			"GridFS bucket name",
			"the full file name and path where you want the file to be written; if not used, then file is returned with the function as either a string or binary object depending on the mimetype",
			"the name of the files to find [defaults]",
			"the _id of the file to find",
			"the query to find",
			"flag to control whether or not the local file is overwritten, defaults to true.  If false and file exists, an exception will be thrown"
			
		};
	}
	
	
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"mongo", 
				"Downloads the given file from the Mongo GridFS bucket.  If successful and 'filepath' will return TRUE, otherwise FALSE if the file was not found.  If not using 'filepath', then the function will return the object.  You can get the file specifying either file name, _id or a query.  It multiple match, it will return the first one.", 
				ReturnType.OBJECT );
	}
	
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		// Get the necessary Mongo references
		DB			db			= getDB(_session, argStruct);
		GridFS	gridfs	= getGridFS(_session, argStruct, db);
		String filepath	= getNamedStringParam(argStruct, "filepath", null);
		
		// Get the file information
		String filename	= getNamedStringParam(argStruct, "filename", null);
		GridFSDBFile file = null;
		
		if ( filename != null ){
			file = gridfs.findOne(filename);
		} else {
			
			String _id	= getNamedStringParam(argStruct, "_id", null);
			if ( _id != null ){
				file = gridfs.findOne( new ObjectId(_id) );
			} else {
				
				cfData mTmp	= getNamedParam(argStruct, "query", null);
				if ( mTmp != null )
					file = gridfs.findOne(getDBObject(mTmp));
			}
		}

		if ( file == null )
			return cfBooleanData.FALSE;
		
		
		/*
		 * if the return object is either a file or a variable
		 */
		if ( filepath != null ){
			boolean overwrite	= getNamedBooleanParam(argStruct, "overwrite", true );
			
			File jFile	= new File(filepath);
			if ( jFile.exists() && !overwrite )
				throwException(_session, "File:" + filepath + " already exists");
			else
				jFile.delete();
			
			
			BufferedOutputStream	bos = null;
			try {
				bos	= new BufferedOutputStream( new FileOutputStream(jFile) );
				
				file.writeTo(bos);
				bos.flush();	
			} catch (IOException e) {
				throwException(_session, "File:" + filepath + " caused: " + e.getMessage() );
			} finally {
				try {
					if ( bos != null )
						bos.close();
				} catch (IOException e) {}
			}
		
		} else {
			
			try {
				ByteArrayOutputStream	bos	= new ByteArrayOutputStream( (int)file.getLength() );
				file.writeTo(bos);
				bos.flush();
				
				String mimetype = file.getContentType();
				
				if ( mimetype != null && mimetype.indexOf("text") > -1 )
					return new cfStringData( bos.toByteArray() );
				else
					return new cfBinaryData( bos.toByteArray() );

			}catch(IOException e){
				throwException(_session, e.getMessage() );
			}
		}

		return cfBooleanData.TRUE;
	}	
}