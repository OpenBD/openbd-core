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
 *  $Id: FileCopy.java 1690 2011-09-26 09:38:42Z alan $
 */
package com.naryx.tagfusion.expression.function.file;

import java.io.BufferedInputStream;
import java.util.List;

import org.apache.commons.vfs.Selectors;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;
import com.naryx.tagfusion.expression.function.functionBase;

public class FileCopy extends functionBase {
  private static final long serialVersionUID = 1L;

  public FileCopy(){ min = max = 2; }
  
	public String[] getParamInfo(){
		return new String[]{
			"srcFile","destFile"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"file", 
				"Makes a copy of the srcFile landing it in the destFile position.  Must be a file path for the destination", 
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


  	/* Attempt the copy */
  	try{
  		
  		if ( !vfsSrc.isNative() && !vfsDest.isNative() )
  			vfsDest.getFileObject().copyFrom( vfsSrc.getFileObject(), Selectors.SELECT_SELF );
  		else
  			copy( vfsSrc, vfsDest );
  		
  	}catch(Exception e){
  		throwException( _session, "Copying file caused an error (" + e.getMessage() + ")" );
  		return cfBooleanData.FALSE;
  	}finally{
  		try {vfsSrc.close();} catch (Exception e) {}
  		try {vfsDest.close();} catch (Exception e) {}
  	}
  	
  	return cfBooleanData.TRUE;
  }

  
  /**
   * Does a byte-4-byte copy
   *  
   * @param vfsSrc
   * @param vfsDest
   * @throws Exception 
   */
  protected	void copy(cfVFSData vfsSrc, cfVFSData vfsDest) throws Exception{
  	vfsSrc.openInputStream();
  	vfsDest.openOutputStream();
  	
  	BufferedInputStream	ios	= vfsSrc.getStreamReader();
  	byte b[]	= new byte[16000];
  	int c;
  	while ( (c=ios.read(b)) != -1 ){
  		vfsDest.getStreamWriter().write(b, 0, c);
  	}
  	
  	vfsSrc.close();
  	vfsDest.close();
  }
  
}