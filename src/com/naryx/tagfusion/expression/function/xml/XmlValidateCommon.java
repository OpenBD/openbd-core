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

package com.naryx.tagfusion.expression.function.xml;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class XmlValidateCommon extends functionBase {
	private static final long serialVersionUID = 1L;

	public XmlValidateCommon() {
		min = 1;
		max = 2;
	}

  public String[] getParamInfo(){
		return new String[]{
			"xml object",
			"validator - can be a string, URL or path to a local file"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"xml", 
				"Runs the validator against the XML object to check for errors, returning a struct of keys Errors, FatalErrors, Warning, Warnings", 
				ReturnType.STRUCTURE );
	}

	
	/**
	 * Returns a cfStructData instance that will be used as the repository for
	 * collecting parse/validation error messages.
	 * 
	 * @return cfStructData instance to hold parse/validation error messages
	 * @throws cfmRunTimeException
	 */
	protected cfStructData newValidationStruct() throws cfmRunTimeException {
		cfStructData rtn = new cfStructData();
		rtn.setData("Errors", cfArrayData.createArray(1));
		rtn.setData("FatalErrors", cfArrayData.createArray(1));
		rtn.setData("Warning", cfArrayData.createArray(1));
		rtn.setData("Warnings", rtn.getData("Warning")); // Just to help those who
																											// realized the naming in
																											// inconsistent
		rtn.setData("Status", cfBooleanData.TRUE);

		return rtn;
	}

}
