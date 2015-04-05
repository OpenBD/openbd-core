/*
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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
package com.naryx.tagfusion.expression.function.ext;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.expression.function.functionBase;

public class ToHTML extends functionBase {
	private static final long serialVersionUID = 1;

	public ToHTML() { min = 1; max = 1; setNamedParams( new String[] {"query"} );}

	public String[] getParamInfo(){
		return new String[]{
			"query"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"query", 
				"Transforms the query object into an HTML TABLE block", 
				ReturnType.STRING );
	} 
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		cfQueryResultData queryData;

		if ( getNamedParam(argStruct, "query").getDataType() != cfData.CFQUERYRESULTDATA )
			throwException(_session, "Parameter must be of type query");

		queryData	= (cfQueryResultData)getNamedParam(argStruct, "query");

		return new cfStringData( convertQueryToHtml(queryData) );
	}


	public static	String convertQueryToHtml( cfQueryResultData queryData ) throws dataNotSupportedException{
		StringBuilder	bluffy	= new StringBuilder( 32000 );

		bluffy.append("<table>\r\n\t<thead>\r\n\t\t<tr>");

		//Columns
		String cols[] = queryData.getColumnNames();
		for ( int c=0; c < cols.length; c++ ){
			bluffy.append( "\r\n\t\t\t<th class='column" + (c+1) + "'>" );
			bluffy.append( escapeColumn( cols[c] ) );
			bluffy.append( "</th>" );
		}
		bluffy.append( "\r\n\t\t</tr>\r\n\t</thead>\r\n\t<tbody>" );

		//Rows
		for ( int r=0; r < queryData.getSize(); r++ ){
			bluffy.append( "\r\n\t\t<tr>" );

			for ( int c=0; c < cols.length; c++ ){
				bluffy.append( "\r\n\t\t\t<td class='column" + (c+1) + "'>" );

				cfData cell	= queryData.getCell( r+1, c+1 );
				if ( cell.getDataType() == cfData.CFBOOLEANDATA )
					bluffy.append( cell.getBoolean() );
				else if ( cell.getDataType() == cfData.CFNUMBERDATA )
					bluffy.append( cell.getString() );
				else
					bluffy.append( escapeColumn( cell.getString() ) );

				bluffy.append( "</td>" );
			}
			bluffy.append( "\r\n\t\t</tr>" );
		}

		bluffy.append( "\r\n\t</tbody>\r\n</table>" );
		return bluffy.toString();
	}

	private static String escapeColumn( String s ){
		StringBuffer b = new StringBuffer( s.length() * 2 );

		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt( i );
			switch ( c )
			{
				case '<': b.append( "&lt;" );	break;
				case '>': b.append( "&gt;" );	break;
				case '&': b.append( "&amp;" );	break;
				case '"': b.append( "&quot;" );	break;
				default :
					b.append( c );
					break;
			}
		}

		return b.toString();
	}
}
