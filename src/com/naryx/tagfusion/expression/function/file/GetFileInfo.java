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
 *  $Id: GetFileInfo.java 2226 2012-08-04 22:56:21Z alan $
 */


package com.naryx.tagfusion.expression.function.file;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;
import com.naryx.tagfusion.expression.function.functionBase;

public class GetFileInfo extends functionBase {
  private static final long serialVersionUID = 1L;

  public GetFileInfo(){ min = 1; max = 1; }
  
	public String[] getParamInfo(){
		return new String[]{
			"srcFile"			
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"file", 
				"Reads the file at the given path and returns back a structure of details (name, path, parent, size, lastmodified, canread, canwrite, ishidden, type)", 
				ReturnType.STRUCTURE );
	}

  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
  	cfVFSData	fileObj = null;
  	try{
  		fileObj = new cfVFSData( parameters.get(0).getString() );
  		
  		if ( !fileObj.exists() )
  			throw new Exception("file does not exist");
  		
  		fileObj.populateInfo();
	  	return fileObj;
  	}catch(Exception e){
  		throwException( _session, "GetFileInfo caused an error (" + e.getMessage() + ")" );
  		return null;
  	}finally{
			try{ fileObj.close(); }catch(Exception ignoreE){}
  	}
  }
}
