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
 *  $Id: queryRenameColumn.java 1651 2011-08-23 20:12:50Z alan $
 */

package com.naryx.tagfusion.expression.function.query;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class queryRenameColumn extends functionBase {

	private static final long serialVersionUID = 1L;

	public queryRenameColumn() {
		min = 3;
		max = 3;
		setNamedParams( new String[]{ "query", "oldcolumn", "newcolumn" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"query",
			"name of the old column",
			"name of the new column"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"query", 
				"Renames a column within a query object.  If the rename was successful it will return TRUE, otherwise FALSE", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		// Get the Query
		cfData thisQuery = getNamedParam(argStruct, "query");
		if (thisQuery.getDataType() != cfData.CFQUERYRESULTDATA) {
			throwException(_session, "Invalid argument type. The first argument must be a query result.");
		}

		// Get the column name
		String oldcolumnName = getNamedStringParam(argStruct, "oldcolumn", null);
		if ( oldcolumnName == null ){
			throwException(_session, "Invalid argument type. Please specify a 'oldcolumn' parameter");
		}
		
		// Get the column name
		String newcolumnName = getNamedStringParam(argStruct, "newcolumn", null);
		if ( newcolumnName == null ){
			throwException(_session, "Invalid argument type. Please specify a 'newcolumn' parameter");
		}
		
		if ( newcolumnName.trim().length() == 0 ){
			throwException(_session, "the 'newcolumn' cannot be blank");
		}
		return cfBooleanData.getcfBooleanData( ((cfQueryResultData) thisQuery).renameColumn(oldcolumnName, newcolumnName ) );
	}
}
