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
 *  $Id: DirectoryDelete.java 1758 2011-11-02 22:46:33Z alan $
 */


package com.naryx.tagfusion.expression.function.file;

import java.io.File;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;
import com.naryx.tagfusion.expression.function.functionBase;

public class DirectoryDelete extends functionBase {
  private static final long serialVersionUID = 1L;

  public DirectoryDelete(){ 
  	min = 1; max = 5;
  	setNamedParams( new String[]{ "path", "recurse" } );
  }
  
	public String[] getParamInfo(){
		return new String[]{
			"the path of the directory to delete",
			"whether it should delete all the sub-directories"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"file", 
				"For the given directory deletes the contents of the file", 
				ReturnType.BOOLEAN );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
  	String inFile	= getNamedStringParam(argStruct, "path", null );
  	if ( inFile == null )
  		throwException(_session, "invalid 'directory' attribute");
  	

  	cfVFSData vfsData = null;
  	try{
  		vfsData	= new cfVFSData( inFile );
  		
  		if ( !vfsData.isNative() ){
  			
  			vfsData.getFileObject().delete();
    		
  		}else{
  			
  			boolean recurse	= getNamedBooleanParam(argStruct, "recurse", false );
  			deleteDirectory( vfsData.getFile(), recurse );
  			
  		}

  	}catch(Exception e){
  		throwException( _session, "File [" + inFile + "] caused an error (" + e.getMessage() + ")" );
  		return cfBooleanData.FALSE;
  	}finally{
  		
  		if ( vfsData != null ){
  			try{
  				vfsData.close();
  			}catch(Exception e){}
  		}
  	}
  	
  	return cfBooleanData.TRUE;
  }
	
	
	
	public static void deleteDirectory( File directory, boolean recurse ) throws Exception{
		
		if ( !directory.exists() || !directory.isDirectory() || !directory.canWrite() )
			throw new Exception("invalid directory: " + directory );
		
		if (recurse) {
			recursiveDelete(directory);
			directory.delete();
		} else {
			File[] files = directory.listFiles();
			if ((files == null) || (files.length == 0))
				directory.delete();
			else
				throw new Exception("The directory " + directory + " cannot be deleted because it is not empty and RECURSE is set to NO");
		}
		
	}
	
	private static void recursiveDelete(File _toDelete) {
		File[] files = _toDelete.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) { // delete contents of dir first
				recursiveDelete(files[i]);
			}
			files[i].delete();
		}
	}
	
}