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
 *  $Id: FileClose.java 1690 2011-09-26 09:38:42Z alan $
 */

package com.naryx.tagfusion.expression.function.file;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;
import com.naryx.tagfusion.expression.function.functionBase;

public class FileClose extends functionBase {
  private static final long serialVersionUID = 1L;

  public FileClose(){ min = max = 1; }
  
	public String[] getParamInfo(){
		return new String[]{
			"file object",
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"file", 
				"For an existing open file, this function closes off all handles", 
				ReturnType.BOOLEAN );
	}

  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
  	if ( !(parameters.get(0) instanceof cfVFSData) ){
  		throwException( _session, "please pass in a file object from FileOpen(.)" );
  	}
  	
  	cfVFSData vfsData = (cfVFSData)parameters.get(0);

  	try{
			vfsData.close();
		}catch(Exception e){}
  	
  	return cfBooleanData.TRUE;
  }
}
