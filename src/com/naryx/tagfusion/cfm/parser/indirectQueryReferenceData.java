/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.parser;

/**
 * A wrapper class for queryResultData entries allowing for the 
 * getting and setting of it. Used in the case of expressions
 * in the format "query.queryColumn[ index ]"
 *
 * Will exist for the period of 2 binaryExpressions at most.
 *
 */

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class indirectQueryReferenceData extends cfLData {

	private static final long serialVersionUID = 1L;

	private int row, col;
	private List<List<cfData>> queryData;
	private String image;

	public indirectQueryReferenceData(String _image, List<List<cfData>> _data, int _row, int _col) {
		super( null );
		exists = true;
		queryData = _data;
		row = _row;
		col = _col;
		image = _image;

		if ( row > cfQueryResultData.getNoRows( queryData ) ) {
			exists = false;
		}

	}

	public cfData Get( CFContext _context ) {
		if ( !exists ) {
			return new cfStringData( "" );
		} else {
			try {
				return cfQueryResultData.getCellData( queryData, row, col );
			} catch ( Exception e ) {
				return new cfStringData( "" );
			}
		}
	}

	public void Set( cfData val, CFContext _context ) throws cfmRunTimeException {
		if ( !cfQueryResultData.setCellData( queryData, row, col, val ) ) {
			throw new CFException( "Invalid expression. Cannot set " + image
			    + ". Row index doesn't exist.", _context );
		}
	}

}

