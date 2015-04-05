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
 *  $Id: dataLoad.java 2497 2015-02-02 01:53:48Z alan $
 */

package com.naryx.tagfusion.expression.function.ext;

import java.io.BufferedInputStream;
import java.util.Map;
import java.util.Map.Entry;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;
import com.naryx.tagfusion.expression.function.functionBase;


public class dataLoad extends functionBase {
	private static final long serialVersionUID = 1L;

	public dataLoad() {
		min = 1; max = 2;
		setNamedParams( new String[]{ "fileurl", "compressed" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"fileurl - url to the serialized file, or previously opened file object (FileOpen)",
			"if the file was saved in a compressed format then it needs to be loaded with the flag set to true; defaults to false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Loads a previously saved data back into memory, returning back the CF data.  The fileurl can be any URL, local, or cloud file location", 
				ReturnType.OBJECT );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
  	
  	cfVFSData	fileObj = null;
  	try{
  		cfData fileData	= getNamedParam(argStruct, "fileurl");
  		
  		if ( fileData instanceof cfVFSData )
  			fileObj	= (cfVFSData)fileData;
  		else
  			fileObj = new cfVFSData( fileData.getString(), "readbinary", null );
  		

	  	/* Check to make sure the file is opened in read only mode */
	  	if ( !fileObj.isBinaryMode() ){
	  		throwException( _session, "this file object is not opened in read mode" );
	  	}
	  	
	  	/* Read the file */
	  	Object o = com.nary.Debug.loadClass( new BufferedInputStream( fileObj.getStreamReader(), 32000 ), getNamedBooleanParam(argStruct, "compress", false) );
	  	
	  	if ( o instanceof cfData ){
	  	 	return (cfData)o;
  		}else if ( o instanceof FastMap ){
	  		cfStructData	s = new cfStructData();
	  		
	  		FastMap	fm	= (FastMap)o;
	  		java.util.Iterator it = fm.entrySet().iterator();
	  		while ( it.hasNext() ){
	  			Map.Entry me = (Entry) it.next();
	  			s.put( me.getKey(), me.getValue() );
	  		}
	  		
	  		return s;
	  	}else
	  		throw new Exception("unknown data type");

  	}catch(Exception e){
  		throwException( _session, "DataLoad caused an error (" + e.getMessage() + ")" );
  		return null;
  	}finally{
			try{ fileObj.close(); }catch(Exception ignoreE){}
  	}
		
	}
}
