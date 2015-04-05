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
 *  $Id: FileSetLastModified.java 1690 2011-09-26 09:38:42Z alan $
 */

package com.naryx.tagfusion.expression.function.file;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;
import com.naryx.tagfusion.expression.function.functionBase;

public class FileSetLastModified extends functionBase {
  private static final long serialVersionUID = 1L;

  public FileSetLastModified(){ min = 2; max = 2; }
  
	public String[] getParamInfo(){
		return new String[]{
			"srcFile",
			"datetoset"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"file", 
				"For the given file, set the last modification date", 
				ReturnType.BOOLEAN );
	}
  
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
  	cfVFSData	fileObj = null;
  	try{
  		fileObj = new cfVFSData( parameters.get(1).getString() );

  		if ( !fileObj.isNative() )
  			fileObj.getFileObject().getContent().setLastModifiedTime( parameters.get(0).getLong() );
  		else
  			fileObj.getFile().setLastModified( parameters.get(0).getLong() );

	  	return cfBooleanData.TRUE;
  	}catch(Exception e){
  		throwException( _session, "FileSetLastModified cause an error (" + e.getMessage() + ")" );
  		return null;
  	}finally{
			try{ fileObj.close(); }catch(Exception ignoreE){}
  	}
  }
}
