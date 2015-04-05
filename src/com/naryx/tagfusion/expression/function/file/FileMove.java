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
 *  $Id: FileMove.java 1754 2011-10-28 09:52:17Z alan $
 */


package com.naryx.tagfusion.expression.function.file;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;

public class FileMove extends FileCopy {
  private static final long serialVersionUID = 1L;

  public FileMove(){ min = max = 2; }
  
	public String[] getParamInfo(){
		return new String[]{
			"srcFile","destFile"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"file", 
				"Moves the srcFile landing it in the destFile position.  Must be a file path for the destination", 
				ReturnType.BOOLEAN );
	}
  
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
  	String srcFile	= parameters.get(1).getString();
  	String destFile	= parameters.get(0).getString();


  	/* Src File */
  	cfVFSData vfsSrc = null;
  	try{
  		vfsSrc	= new cfVFSData( srcFile );
  	}catch(Exception e){
  		throwException( _session, "File [" + srcFile + "] caused an error (" + e.getMessage() + ")" );
  		return cfBooleanData.FALSE;
  	}


  	/* Dest File */
  	cfVFSData vfsDest = null;
  	try{
  		vfsDest	= new cfVFSData( destFile );
  	}catch(Exception e){
  		throwException( _session, "File [" + destFile + "] caused an error (" + e.getMessage() + ")" );
  		return cfBooleanData.FALSE;
  	}finally{
  		try {vfsSrc.close();} catch (Exception e) {}
  	}


  	/* Attempt the move */
  	try{

  		if ( !vfsSrc.isNative() )
  			vfsSrc.getFileObject().moveTo( vfsDest.getFileObject() );
  		else
  			move( vfsSrc, vfsDest );

  	}catch(Exception e){
  		throwException( _session, "Moving file caused an error (" + e.getMessage() + ")" );
  		return cfBooleanData.FALSE;
  	}finally{
  		try {vfsSrc.close();} catch (Exception e) {}
  		try {vfsDest.close();} catch (Exception e) {}
  	}
  	
  	return cfBooleanData.TRUE;
  }

  
  
	private void move(cfVFSData vfsSrc, cfVFSData vfsDest) throws Exception {
		String dest = null;
		if ( vfsDest.isDirectory() ){
			dest = vfsDest.getFullName() + java.io.File.separator + vfsSrc.getFileName();
		}else{
			dest = vfsDest.getFullName();
		}

		vfsDest.close();
		vfsDest	= new cfVFSData( dest );
		copy(vfsSrc, vfsDest);

		if ( !vfsSrc.isNative() )
			vfsSrc.getFileObject().delete();
		else
			vfsSrc.getFile().delete();
	}

}
