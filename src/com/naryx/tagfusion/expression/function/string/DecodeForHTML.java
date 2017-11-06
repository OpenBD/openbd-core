/*
 *  Copyright (C) 2000 - 2015 Aw2.0 Ltd
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
 *
 */

package com.naryx.tagfusion.expression.function.string;

import org.apache.commons.lang.StringEscapeUtils;
import org.aw20.util.StringUtil;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;



public class DecodeForHTML extends functionBase {



	private static final long	serialVersionUID	= 1L;



	public DecodeForHTML() {
		min = max = 1;
		setNamedParams(new String[] { "string" });
	}



	public String[] getParamInfo() {
		return new String[] { "the string to unescape" };
	}




	public java.util.Map getInfo() {
		return makeInfo("string", "Unescapes a string containing entity escapes to a string containing the actual Unicode characters corresponding to the escapes", ReturnType.STRING);
	}



	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String htmlAtt = StringEscapeUtils.unescapeHtml( getNamedStringParam(argStruct, "string", "") );
		return new cfStringData( htmlAtt );
	}



}
