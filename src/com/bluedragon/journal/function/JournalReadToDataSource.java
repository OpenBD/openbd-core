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
 *  $Id: JournalReadToDataSource.java 2510 2015-02-09 01:23:37Z alan $
 */
package com.bluedragon.journal.function;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.aw20.io.StreamUtil;
import org.aw20.util.StringUtil;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.sql.cfDataSource;
import com.naryx.tagfusion.expression.function.functionBase;
import com.naryx.tagfusion.expression.function.string.DeserializeJSONJackson;


public class JournalReadToDataSource extends functionBase {
	private static final long serialVersionUID = 1L;

	private static String CREATE_1 = "CREATE TABLE IF NOT EXISTS ";
	private static String CREATE_2 = " (t_offset INT,code VARCHAR(2),file_id SMALLINT,session SMALLINT,file_depth SMALLINT,tag_depth SMALLINT,tag VARCHAR(64),line SMALLINT,col SMALLINT,fn VARCHAR(128),scriptline SMALLINT,journalid INT,id INT, PRIMARY KEY (id, journalid) );";
	private static String INSERT = "insert into journal values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	public JournalReadToDataSource() {
		min = 2; max = 4;
		setNamedParams( new String[]{ "file", "datasource", "id", "table" } );
	}

	@Override
	public String[] getParamInfo(){
		return new String[]{
				"full path of the journal file to read",
				"the datasource to use to store the journal to",
				"id to use for this journal.  this makes it possible to store multiple journal files in a single table; defaults to 1",
				"the name of the table to insert the data to; defaults to 'journal'"
		};
	}


	@Override
	public java.util.Map getInfo(){
		return makeInfo(
				"system",
				"Reads a journal file into the database table specified, returning back a structure with the details of the request.  The table will have the columns: T_OFFSET, CODE, FILE_ID, SESSION, FILE_DEPTH, TAG_DEPTH, TAG, LINE, COL, FN, SCRIPTLINE, JOURNALID, ID",
				ReturnType.STRUCTURE );
	}

	@Override
	public cfData execute( cfSession _session, cfArgStructData argStruct )throws cfmRunTimeException{

		String file = getNamedStringParam(argStruct, "file", null);

		if ( file == null )
			throwException(_session, "please specify at least the 'file' attribute");

		File	journalFile	= new File( file );
		if ( !journalFile.isFile() )
			throwException(_session, "the 'file' does not exist.  file=" + file);

		String ds		= getNamedStringParam(argStruct, "datasource", null);
		cfDataSource sqldataSource = new cfDataSource(ds, _session);

		int	id				= getNamedIntParam(argStruct, "id", 1);
		String table	= getNamedStringParam(argStruct, "table", "journal");

		BufferedReader	in = null;
		cfStructData	r;
		Connection c = null;

		try{
			in	= new BufferedReader( new FileReader(journalFile) );

			// Read the first line; that stores the metadata
			String tmp = in.readLine();

			DeserializeJSONJackson	jsonFunc	= new DeserializeJSONJackson();
			argStruct.clear();
			argStruct.setData("jsonstring", tmp);
			r	= (cfStructData)jsonFunc.execute(_session, argStruct);

			// Manage the database
			c	= sqldataSource.getConnection();
			
			// Create the table
			Statement statement	= c.createStatement();
			statement.execute( CREATE_1 + table + CREATE_2 );
			
			PreparedStatement pstatement	= c.prepareStatement(INSERT);
			
			// Now we must insert the data
			int journalid = 1;
			while ( (tmp=in.readLine()) != null ){
				String[] array = tmp.split(",");

				// Clear/default the values
				pstatement.clearParameters();
				pstatement.setInt( JournalRead.col_file_id, 0);
				pstatement.setInt( JournalRead.col_session, 0);
				pstatement.setInt( JournalRead.col_file_depth, 0);
				pstatement.setInt( JournalRead.col_tag_depth, 0);
				pstatement.setInt( JournalRead.col_line, 0);
				pstatement.setInt( JournalRead.col_col, 0);
				pstatement.setInt( JournalRead.col_scriptline, 0);
				pstatement.setString( JournalRead.col_tag, "");
				pstatement.setString( JournalRead.col_fn, "");

				
				// Set the values
				pstatement.setInt( JournalRead.col_journalid, journalid++ );
				pstatement.setInt( JournalRead.col_id, id );
				pstatement.setInt( JournalRead.col_t_offset, StringUtil.toInteger(array[0], 0) );
				pstatement.setString( JournalRead.col_code, array[1] );
				
  			if ( array[1].charAt(0) == 'E' ){
  				pstatement.setString( JournalRead.col_session, array[2] );
  			}else{
  				
  				pstatement.setInt( JournalRead.col_file_id, StringUtil.toInteger(array[2], 0) );
  				pstatement.setInt( JournalRead.col_session, StringUtil.toInteger(array[3], 0) );
  				pstatement.setInt( JournalRead.col_file_depth, StringUtil.toInteger(array[4], 0) );
  				pstatement.setInt( JournalRead.col_tag_depth, StringUtil.toInteger(array[5], 0) );

    			if ( array[1].charAt(0) == 'M' ){
    				pstatement.setString( JournalRead.col_tag, array[6] );
    				pstatement.setInt( JournalRead.col_line, StringUtil.toInteger(array[7], 0) );
    				pstatement.setInt( JournalRead.col_col, StringUtil.toInteger(array[8], 0) );
    				pstatement.setString( JournalRead.col_fn, (array[9].length() > 128) ? array[9].substring(0,127) : array[9] );
    			} else	if ( array[1].charAt(0) == 'T' ){
    				pstatement.setString( JournalRead.col_tag, array[6] );
    				pstatement.setInt( JournalRead.col_line, StringUtil.toInteger(array[7], 0) );
    				pstatement.setInt( JournalRead.col_col, StringUtil.toInteger(array[8], 0) );
    			} else	if ( array[1].equals("SS") ){
    				pstatement.setInt( JournalRead.col_line, StringUtil.toInteger(array[7], 0) );
    				pstatement.setInt( JournalRead.col_col, StringUtil.toInteger(array[8], 0) );
    				pstatement.setInt( JournalRead.col_scriptline, StringUtil.toInteger(array[9], 0) );
    			} else	if ( array[1].equals("SF") || array[1].equals("SE") ){
    				pstatement.setString( JournalRead.col_fn, (array[6].length() > 128) ? array[6].substring(0,127) : array[6] );
    			} else	if ( array[1].equals("HE") ){
    				pstatement.setString( JournalRead.col_fn, (array[9].length() > 128) ? array[9].substring(0,127) : array[9] );
    				pstatement.setInt( JournalRead.col_line, StringUtil.toInteger(array[7], 0) );
    				pstatement.setInt( JournalRead.col_col, StringUtil.toInteger(array[8], 0) );
    			}
  			}
  			
  			pstatement.execute();
			}

			return r;

		} catch (SQLException | IOException e) {
			throwException(_session, e.getMessage() );
		}finally{
			StreamUtil.closeStream(in);
			
			if ( c != null )
				sqldataSource.close(c);
		}

		return cfBooleanData.TRUE;
	}

}
