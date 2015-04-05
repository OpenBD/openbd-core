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

package com.naryx.tagfusion.expression.function;

import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfLOOP;
import com.naryx.tagfusion.cfm.tag.cfMODULE;
import com.naryx.tagfusion.cfm.tag.cfPROCESSINGDIRECTIVE;
import com.naryx.tagfusion.cfm.tag.cfTag;

public class getBaseTagList extends functionBase {
	private static final long serialVersionUID = 1L;

	// note that this list should be used as read-only since it is shared across
	// instances
	private static final List<String> excludedTags;

	static {
		excludedTags = new ArrayList<String>();
		excludedTags.add("CFIF");
		excludedTags.add("CFELSEIF");
		excludedTags.add("CFELSE");
		excludedTags.add("CFSWITCH");
		excludedTags.add("CFCASE");
		excludedTags.add("CFDEFAULTCASE");
		excludedTags.add("CFTRY");
		excludedTags.add("CFCATCH");

		// not documented
		excludedTags.add("CFFUNCTION");
		excludedTags.add("CFSET");
		excludedTags.add("CFSCRIPT");
		excludedTags.add("CFRETURN");
	}

	public java.util.Map getInfo() {
		return makeInfo("system", "Returns the list of all the parent tags upto the current point", ReturnType.STRING);
	}	

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {

		StringBuilder buf = new StringBuilder(32);

		cfTag nextTag;

		for (int x = _session.getTagStackSize(); x > 0; x--) {
			nextTag = _session.getTagElement(x - 1);
			String tagName = nextTag.getTagName();

			if (!(excludedTags.contains(tagName) || (tagName.equals("CFLOOP") && !((cfLOOP) nextTag).containsAttribute("QUERY")) || (tagName.equals("CFPROCESSINGDIRECTIVE") && !((cfPROCESSINGDIRECTIVE) nextTag).containsAttribute("SUPPRESSWHITESPACE")))) {
				if (nextTag instanceof cfMODULE) {
					buf.append(((cfMODULE) nextTag).getCustomTagName());
				} else {
					buf.append(tagName);
				}
				buf.append(",");
			}
		}

		// remove any trailing ','
		if (buf.length() > 0 && buf.charAt(buf.length() - 1) == ',') {
			buf = buf.deleteCharAt(buf.length() - 1);
		}
		return new cfStringData(buf.toString());
	}

}
