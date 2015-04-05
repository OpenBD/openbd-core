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
 *  $Id: MailReadFunction.java 1720 2011-10-07 19:39:14Z alan $
 */
package org.alanwilliamson.openbd.plugin.cfsmtp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class MailReadFunction extends functionBase {
	private static final long	serialVersionUID	= 1L;

	public MailReadFunction() {
		min = max = 1;
		setNamedParams( new String[]{ "filepath" } );
	}

  public String[] getParamInfo(){
		return new String[]{
			"reads the mail object that was written to disk"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"cfsmtp-plugin", 
				"Used in conjunction with MailWrite().  This function reads the previously serialized email out to disk", 
				ReturnType.OBJECT );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String filepath	=  getNamedStringParam( argStruct, "filepath", null );
		if ( filepath == null )
			throwException(_session, "missing parameter 'filepath'");
		
		File infile	= new File(filepath);
		if ( !infile.exists() || !infile.isFile() )
			throwException(_session, "invalid file specified:" + filepath );
		
		
		FileInputStream	fileIn = null;
		BufferedInputStream in = null;
		try{
			
			fileIn 	= new FileInputStream(infile);
			in 			= new BufferedInputStream(fileIn);
			MimeMessage msg = new MimeMessage( Session.getInstance(new Properties()), in);
			return new BlueDragonMailWrapper( msg );
			
		}catch(Exception e){
			throwException(_session, e.getMessage() );
		}finally{
			
			try {
				if (in != null)
					in.close();
			} catch (IOException ignoreE) {
			}

			try {
				if (fileIn != null)
					fileIn.close();
			} catch (IOException ignoreE) {
			}
			
		}
		
		return null;
	}
	
}
