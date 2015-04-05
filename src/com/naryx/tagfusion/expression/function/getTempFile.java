/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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
 *  http://www.openbluedragon.org/
 */

package com.naryx.tagfusion.expression.function;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
 
public class getTempFile extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public getTempFile(){
     min = max = 2;
  }
  
	public String[] getParamInfo(){
		return new String[]{
			"directory",
			"prefix"
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"file", 
				"Creates a new unique file in the given directory with the prefix.  Extension of the new file is '.tmp'", 
				ReturnType.STRING );
	}
	  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{    
 	 	String dir 		= parameters.get(1).getString();
 	  String prefix = parameters.get(0).getString();
  
  	if ( prefix.length() > 3 )
  		prefix = prefix.substring( 0, 3 );
  
  	prefix	= prefix + System.currentTimeMillis() + ".tmp";
  	File FF = new File( dir, prefix );
  	try {
	    FF.createNewFile();
    } catch ( IOException e ) {
    	throwException( _session, "Failed to create temporary file " + FF.getAbsolutePath() + ". Check the directory exists and has the appropriate permissions" );
	  }
    return new cfStringData( FF.toString() );
  } 
}
