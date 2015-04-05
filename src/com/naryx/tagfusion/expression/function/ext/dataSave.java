/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: dataSave.java 2497 2015-02-02 01:53:48Z alan $
 */
package com.naryx.tagfusion.expression.function.ext;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;
import com.naryx.tagfusion.expression.function.functionBase;

public class dataSave extends functionBase {
	private static final long serialVersionUID = 1L;

	public dataSave() {
		min = 2; max = 3;
		setNamedParams( new String[]{ "data", "fileurl", "compress" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"the data to save to disk",
			"full path to store the data at",
			"flag to determine if you wish to compress this on the disk; defaults to false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Saves the data/object to the given file location. Supports CFC serialization.", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{

  	cfVFSData	fileObj = null;
  	try{
  		cfData fileData	= getNamedParam(argStruct, "fileurl");
  		
  		if ( fileData instanceof cfVFSData )
  			fileObj	= (cfVFSData)fileData;
  		else
  			fileObj = new cfVFSData( fileData.getString(), "write", null );
  		

	  	/* Check to make sure the file is opened in read only mode */
	  	if ( !fileObj.isWriteable() ){
	  		throwException( _session, "this file object is not opened in write mode" );
	  	}
	  	
			cfData data	= getNamedParam(argStruct, "data" );
	  	com.nary.Debug.saveClass( fileObj.getStreamWriter(), data, getNamedBooleanParam(argStruct, "compress", false) );
	  	fileObj.flushWrite();

  	}catch(Exception e){
  		throwException( _session, "DataSave caused an error (" + e.getMessage() + ")" );
  		return null;
  	}finally{
			try{ fileObj.close(); }catch(Exception ignoreE){}
  	}
		
		return cfBooleanData.TRUE;
	}
}