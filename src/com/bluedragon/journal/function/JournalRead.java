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
 *  $Id: JournalRead.java 2507 2015-02-09 01:20:34Z alan $
 */
package com.bluedragon.journal.function;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.aw20.io.StreamUtil;
import org.aw20.util.StringUtil;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;
import com.naryx.tagfusion.expression.function.string.DeserializeJSONJackson;


public class JournalRead extends functionBase {
	private static final long serialVersionUID = 1L;

	private static Map<String, cfStringData>	codeMap = null;

	public static final int col_t_offset = 1;
	public static final int col_code = 2;
	public static final int col_file_id = 3;
	public static final int col_session = 4;
	public static final int col_file_depth = 5;
	public static final int col_tag_depth = 6;
	public static final int col_tag = 7;
	public static final int col_line = 8;
	public static final int col_col = 9;
	public static final int col_fn = 10;
	public static final int col_scriptline = 11;
	public static final int col_journalid = 12;
	public static final int col_id = 13;


	public JournalRead() {
		min = 1; max = 2;
		setNamedParams( new String[]{ "file", "full" } );

		if ( codeMap == null ){
			codeMap	= new HashMap<String,cfStringData>();
			codeMap.put("FS", new cfStringData("FF") );
			codeMap.put("FE", new cfStringData("FE") );

			codeMap.put("EF", new cfStringData("EF") );
			codeMap.put("ER", new cfStringData("ER") );

			codeMap.put("MS", new cfStringData("MS") );
			codeMap.put("ME", new cfStringData("ME") );

			codeMap.put("TS", new cfStringData("TF") );
			codeMap.put("TE", new cfStringData("TE") );
			codeMap.put("TT", new cfStringData("TT") );

			codeMap.put("SS", new cfStringData("SS") );
			codeMap.put("SF", new cfStringData("SF") );
			codeMap.put("SE", new cfStringData("SE") );
			codeMap.put("BW", new cfStringData("BW") );
			codeMap.put("HE", new cfStringData("HE") );
		}

	}

	@Override
	public String[] getParamInfo(){
		return new String[]{
				"full path of the journal file to read",
				"flag if you wish all the journal to be read and returned back as a query object inside the main structure; defaults to false"
		};
	}


	@Override
	public java.util.Map getInfo(){
		return makeInfo(
				"system",
				"Reads a journal file, returning back a structure with the details of the request.  If full=true then the 'trace' key will have the query of all the execution",
				ReturnType.STRUCTURE );
	}

	@Override
	public cfData execute( cfSession _session, cfArgStructData argStruct )throws cfmRunTimeException{

		String file = getNamedStringParam(argStruct, "file", null);
		boolean bFull = getNamedBooleanParam(argStruct, "full", false );

		if ( file == null )
			throwException(_session, "please specify at least the 'file' attribute");

		File	journalFile	= new File( file );
		if ( !journalFile.isFile() )
			throwException(_session, "the 'file' does not exist.  file=" + file);

		BufferedReader	in = null;
		cfStructData	r;

		try{
			in	= new BufferedReader( new FileReader(journalFile) );

			// Read the first line
			String tmp = in.readLine();

			DeserializeJSONJackson	jsonFunc	= new DeserializeJSONJackson();
			argStruct.clear();
			argStruct.setData("jsonstring", tmp);
			r	= (cfStructData)jsonFunc.execute(_session, argStruct);

			if ( bFull ){

	  		cfQueryResultData qD = new cfQueryResultData( new String[]{"t_offset", "code", "file_id", "session",
	  				"file_depth", "tag_depth", "tag", "line", "col", "fn", "scriptline","journalid"}, null );
	  		qD.setQuerySource( "Journal" );
	  		r.setData("trace", qD);


				while ( (tmp=in.readLine()) != null ){
					String[] array = tmp.split(",");

    			qD.addRow(1);
    			qD.setCurrentRow( qD.getSize() );

    			qD.setCell( col_journalid, new cfNumberData( qD.getSize() ) );
    			qD.setCell( col_t_offset, new cfNumberData( StringUtil.toInteger(array[0], 0) ) );
    			qD.setCell( col_code, codeMap.get(array[1]) );

    			if ( array[1].charAt(0) == 'E' ){
      			qD.setCell( col_session, new cfStringData( array[2] ) );
    			}else{

	    			qD.setCell( col_file_id,	 	new cfNumberData( StringUtil.toInteger(array[2], 0) ) );
	    			qD.setCell( col_session, 		new cfNumberData( StringUtil.toInteger(array[3], 0) ) );
	    			qD.setCell( col_file_depth, new cfNumberData( StringUtil.toInteger(array[4], 0) ) );
	    			qD.setCell( col_tag_depth, 	new cfNumberData( StringUtil.toInteger(array[5], 0) ) );

	    			if ( array[1].charAt(0) == 'M' ){
	    				qD.setCell( col_tag, new cfStringData( array[6] ) );
	      			qD.setCell( col_line, new cfNumberData( StringUtil.toInteger(array[7], 0) ) );
	      			qD.setCell( col_col, new cfNumberData( StringUtil.toInteger(array[8], 0) ) );
	      			qD.setCell( col_fn, new cfStringData( array[9] ) );
	    			} else	if ( array[1].charAt(0) == 'T' ){
	    				qD.setCell( col_tag, new cfStringData( array[6] ) );
	      			qD.setCell( col_line, new cfNumberData( StringUtil.toInteger(array[7], 0) ) );
	      			qD.setCell( col_col, new cfNumberData( StringUtil.toInteger(array[8], 0) ) );
	    			} else	if ( array[1].equals("SS") ){
	    				qD.setCell( col_line, new cfNumberData( StringUtil.toInteger(array[7], 0) ) );
	      			qD.setCell( col_col, new cfNumberData( StringUtil.toInteger(array[8], 0) ) );
	      			qD.setCell( col_scriptline, new cfNumberData( StringUtil.toInteger(array[9], 0) ) );
	    			} else	if ( array[1].equals("SF") || array[1].equals("SE") ){
	    				qD.setCell( col_fn, new cfStringData( array[6] ) );
	    			} else	if ( array[1].equals("HE") ){
	    				qD.setCell( col_line, new cfNumberData( StringUtil.toInteger(array[7], 0) ) );
	    				qD.setCell( col_col, new cfNumberData( StringUtil.toInteger(array[8], 0) ) );
	      			qD.setCell( col_fn, new cfStringData( array[9] ) );
	    			}
    			}
				}

			}

			return r;

		} catch (IOException e) {
			throwException(_session, e.getMessage() );
		}finally{
			StreamUtil.closeStream(in);
		}

		return cfBooleanData.TRUE;
	}

}
