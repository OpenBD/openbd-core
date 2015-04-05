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
 *  $Id: FileOpen.java 1690 2011-09-26 09:38:42Z alan $
 */


package com.naryx.tagfusion.expression.function.file;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.vfs.cfVFSData;
import com.naryx.tagfusion.expression.function.functionBase;

public class FileOpen extends functionBase {
  private static final long serialVersionUID = 1L;

  public FileOpen(){ 
  	min = 1; max = 3;
  	setNamedParams( new String[]{ "src", "mode", "charset" } );
  }
  
	public String[] getParamInfo(){
		return new String[]{
			"srcFile",
			"mode - file open type: read, readbinary, append, write",
			"charset"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"file", 
				"Opens up the file at the given locatio for either reading or writing", 
				ReturnType.OBJECT );
	}
  
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
  	String filename	= getNamedStringParam( argStruct, "src", null );
  	String mode			= getNamedStringParam( argStruct, "mode", "read" );
  	String charset	= getNamedStringParam( argStruct, "charset", null );
		
  	if ( filename == null )
  		throwException(_session, "missing the src parameter");
  	
  	/* Create the object */
  	try {
			return new cfVFSData( filename, mode, charset );
		} catch (Exception e) {
			throwException( _session, "An error opening the file: " + e.getMessage() );
			return null;
		}
  }
}
