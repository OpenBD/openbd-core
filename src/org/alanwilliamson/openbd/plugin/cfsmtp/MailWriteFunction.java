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
 *  http://www.openbluedragon.org/
 *  
 *  $Id: MailWriteFunction.java 1720 2011-10-07 19:39:14Z alan $
 */
package org.alanwilliamson.openbd.plugin.cfsmtp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class MailWriteFunction extends functionBase {
	private static final long	serialVersionUID	= 1L;

	public MailWriteFunction() {
		min = 2; max = 3;
		setNamedParams( new String[]{ "mail", "filepath", "overwrite" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"Mail object",
			"The full path where the file will be written",
			"Flag to determine if the mail file is to be overwritten if already exists.  Defaults to false"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"cfsmtp-plugin", 
				"This function writes the email out to disk", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		boolean bOverwrite = getNamedBooleanParam(argStruct, "overwrite", false );
		String filepath	=  getNamedStringParam( argStruct, "filepath", null );
		if ( filepath == null )
			throwException(_session, "missing parameter 'filepath'");
		
		File outFile	= new File(filepath);
		if ( outFile.isFile() && !bOverwrite )
			throwException(_session, "file already exists:" + filepath );
		else if ( outFile.isFile() )
			outFile.delete();
		
		cfData	obj	= getNamedParam(argStruct, "mail", null );
		if ( obj == null )
			throwException(_session, "missing parameter 'mail'");
		
		if ( !(obj instanceof BlueDragonMailWrapper) )
			throwException(_session, "not the required type");
			
		BlueDragonMailWrapper	mailWrapper	= (BlueDragonMailWrapper)obj;
		
		FileOutputStream	outWriter = null;
		BufferedInputStream in = null;
		try{
			
			outWriter	= new FileOutputStream( outFile );
			mailWrapper.getMessage().writeTo( outWriter );
  		outWriter.flush();
  		outWriter.close();
			
  		return cfBooleanData.TRUE;
		}catch(Exception e){
			throwException(_session, e.getMessage() );
		}finally{
			
			try {
				if (in != null)
					in.close();
			} catch (IOException ignoreE) {
			}

			try {
				if (outWriter != null)
					outWriter.close();
			} catch (IOException ignoreE) {
			}
			
		}
		
		return null;
	}
	
}
