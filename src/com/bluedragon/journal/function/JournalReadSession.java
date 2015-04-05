/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: JournalReadSession.java 2497 2015-02-02 01:53:48Z alan $
 */
package com.bluedragon.journal.function;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import org.aw20.io.FileUtil;
import org.aw20.io.StreamUtil;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class JournalReadSession extends functionBase {
	private static final long serialVersionUID = 1L;

	public JournalReadSession() {
		min = max = 2;
		setNamedParams( new String[]{ "file", "session" } );
	}

	public String[] getParamInfo(){
		return new String[]{
				"full path of the journal file to read",
				"the number of the session to read, starts at 1. This is pointer to the session as per the JournalRead()"
		};
	}

	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Reads a journal file looking for the specific session", 
				ReturnType.STRUCTURE );
	}
	
	@SuppressWarnings("resource")
	public cfData execute( cfSession _session, cfArgStructData argStruct )throws cfmRunTimeException{ 
		String file = getNamedStringParam(argStruct, "file", null);
		if ( file == null )
			throwException(_session, "please specify at least the 'file' attribute");

		if ( !file.endsWith(".session") )
			file = file + ".session";

		File	journalFile	= new File( file );
		if ( !journalFile.isFile() )
			throwException(_session, "the 'file' does not exist.  file=" + file);

		int sessionPage	= getNamedIntParam(argStruct, "session", 1 );
		
		
		BufferedInputStream	in = null;
		
		try{
			in	= new BufferedInputStream( new FileInputStream(journalFile), 64000 );

			int currentPage = 1, pageSize = 0;
			byte[] sizeBytes = new byte[4];

			// Read the first one
			if ( in.read(sizeBytes, 0, 4) == -1 )
				throw new IOException( "invalid size" );
				
			pageSize	= ByteBuffer.wrap(sizeBytes).getInt();
			
			while ( currentPage != sessionPage ){
				for ( int x=0; x < pageSize; x++ )
					in.read();
				
				if ( in.read(sizeBytes, 0, 4) == -1 )
					throw new IOException( "invalid page" );
				
				pageSize	= ByteBuffer.wrap(sizeBytes).getInt();
				currentPage++;
			}
			
			// now we can read the data in
			byte[] pageData	= new byte[pageSize];
			in.read( pageData, 0, pageSize );
			
			// Uncompress it and get it to the 
			return (cfStructData)FileUtil.loadClass(pageData, true);

		} catch (IOException e) {
			throwException(_session, e.getMessage() );
		} catch (ClassNotFoundException e) {
			throwException(_session, e.getMessage() );
		} catch (DataFormatException e) {
			throwException(_session, e.getMessage() );
		}finally{
			StreamUtil.closeStream(in);
		}
		
		return cfBooleanData.TRUE;
	}

}
