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
 *  $Id: FileSetAccessMode.java 1686 2011-09-25 18:23:56Z alan $
 */


package com.naryx.tagfusion.expression.function.file;

import java.io.IOException;
import java.util.List;

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
public class FileSetAccessMode extends functionBase {
  private static final long serialVersionUID = 1L;

  public FileSetAccessMode(){ min = max = 2; }
  
	public String[] getParamInfo(){
		return new String[]{
			"srcFile",
			"filemode - the same attributes you use for the Linux command 'chmod'"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"file", 
				"For the given path, sets the file attributes {for *NIX systems only}", 
				ReturnType.BOOLEAN );
	}
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
  	
  	if ( cfEngine.WINDOWS )
  		throwException( _session, "FileSetAttribute is only for use on *NIX systems" );
  	
  	String file 					= parameters.get(1).getString();
  	String attributesStr	= parameters.get(0).getString();

  	try{
      if ( attributesStr.length() != 3 ){
      	throwException( _session, "Invalid MODE specified: " + attributesStr + ". Expected a three digit octal value e.g. 755." );
      }

      try{
        Runtime.getRuntime().exec( "chmod " + attributesStr + " " + file ).waitFor();
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
