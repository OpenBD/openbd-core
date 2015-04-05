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

package com.naryx.tagfusion.expression.function.list;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class listDeleteAt extends functionBase {
	private static final long serialVersionUID = 1L;

	public listDeleteAt() {
		min = 2;
		max = 3;
		setNamedParams( new String[]{ "list","position","delimiter"} );
	}

	public String[] getParamInfo(){
		return new String[]{
			"list",
			"position",
			"delimiter - default comma (,)"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"list", 
				"Removes item from the list at the given position, returning back the new list", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		String list = getNamedStringParam( argStruct, "list" ,"" );
		String delimiter = getNamedStringParam( argStruct, "delimiter" ,"," );
		
		int position = getNamedIntParam( argStruct, "position" ,0 );

		// Note that we can't use java.util.StringTokenizer nor
		// com.nary.util.stringtokenizer
		// here because we need to maintain the list delimiters used in the original
		// list
		// and there may be more than one delimiter char.

		StringBuilder newList = new StringBuilder(list);

		int currPos = 0;
		int lastDelim = -1;
		int currListPos = 1;

		while (currPos < newList.length()) {
			// if we've found a delimiter or at the end of the list
			if (delimiter.indexOf(newList.charAt(currPos)) != -1 || currPos == newList.length() - 1) {

				if ((currPos == 0 && newList.length() != 1) || (currPos - 1 == lastDelim && currPos != newList.length() - 1)) { // empty
					// list
					// element
					lastDelim = currPos;
					currPos++;

				} else if (currListPos == position) { // this is the element that should
					// be deleted
					newList = newList.delete(lastDelim + 1, currPos + 1);
					currPos = lastDelim + 1;
					// as well as not appending the item we wish to delete to the new
					// list, we need to
					// do some cleanup if there are any empty list elements directly
					// surrounding that element
					while (currPos < newList.length() && delimiter.indexOf(newList.charAt(currPos)) != -1) {
						newList = newList.deleteCharAt(currPos);
					}

					// if this was the last token in the list then we want to remove any
					// empty elements that may have preceeded it
					if (currPos >= newList.length()) {
						while (newList.length() >= 1 && delimiter.indexOf(newList.charAt(newList.length() - 1)) != -1) {
							newList = newList.deleteCharAt(newList.length() - 1);
						}
					}
					currListPos++;
					break; // don't need to edit the list any further

				} else {
					currListPos++;
					lastDelim = currPos;
					currPos++;
				}

			} else {
				currPos++;
			}
		}

		return new cfStringData(newList.toString());
	}
}
