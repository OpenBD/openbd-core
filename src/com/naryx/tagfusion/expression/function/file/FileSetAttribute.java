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


package com.naryx.tagfusion.expression.function.file;

import java.io.IOException;
import java.util.List;

import com.nary.util.FastMap;
import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


/*
 * For this particular function we step outside of the VFS subsystem
 * and simply utilise the functionality already offered by CFFILE
 */
public class FileSetAttribute extends functionBase {
  private static final long serialVersionUID = 1L;

  public FileSetAttribute(){ min = max = 2; }
  
	public String[] getParamInfo(){
		return new String[]{
			"srcFile",
			"filemode - the attributes for a file; archive, hidden, normal, readonly, system, temporary"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"file", 
				"For the given path, sets the file attributes {for Windows only}", 
				ReturnType.BOOLEAN );
	}
  
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
  	
  	if ( !cfEngine.WINDOWS )
  		throwException( _session, "FileSetAttribute is only for use on MS Windows" );
  	
  	String file 					= parameters.get(1).getString();
  	String attributesStr	= parameters.get(0).getString();
  	
  	try{
      FastMap attrMap = new FastMap();
      attrMap.put( "archive", 	"+A" );
      attrMap.put( "hidden", 		"+H" );
      attrMap.put( "normal", 		"-H -S -R -A" );
      attrMap.put( "readonly", 	"+R" );
      attrMap.put( "system", 		"+S" );
      attrMap.put( "temporary", "-H -S -R -A" ); // same as normal

      String [] attributes = string.convertToList( attributesStr.toLowerCase(), ',' );

      StringBuilder attribArgs = new StringBuilder();
      for ( int i = 0; i < attributes.length; i++ ){
        if ( attrMap.containsKey( attributes[i] ) ){
          attribArgs.append( " " + attrMap.get( attributes[i] ) );
        }else{
        	throwException( _session, "Invalid ATTRIBUTE value [" + attributes[i] + "]. Valid values include archive, hidden, normal, readonly, and system.");
        }
      }

      try{
        Runtime.getRuntime().exec( "attrib" + attribArgs.toString() + " " + file ).waitFor();
      } catch (SecurityException sec) {
      	throwException( _session, "file attributes cannot be modified when the SecurityPermission UnmanagedCode flag is not set. " + sec.getMessage());
      }catch( IOException ioe ){
      	throwException( _session, "Failed to modify file attributes. " + ioe.getMessage() );
      } catch (InterruptedException ignored) {}
      
  		return cfBooleanData.TRUE;
  	}catch(Exception e){
  		throwException( _session, "FileSetAttribute cause an error (" + e.getMessage() + ")" );
  		return null;
  	}
  }
}
