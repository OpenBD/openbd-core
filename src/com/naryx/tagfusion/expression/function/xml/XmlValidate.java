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

import java.io.IOException;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.cfm.xml.parse.ValidatorSource;

public class XmlValidate extends XmlValidateCommon {
	private static final long serialVersionUID = 1L;

	public XmlValidate() {
		super();
	}

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		cfData xml = null;
		String validator = null;

		if (parameters.size() == 2) {
			xml = parameters.get(1);
			validator = parameters.get(0).getString().trim();
		} else {
			xml = parameters.get(0);
		}

		// Validate
		cfStructData rtn = newValidationStruct();

		try {
			ValidatorSource vs = new ValidatorSource(cfXmlData.interpretString(_session, validator));
			if (vs.isNull()) {
				// This will deserialize a cfXmlData correctly or get the string
				// value out of a cfStringData.
				Object val = cfXmlData.interpretString(_session, xml.toString());
				// Parse and validate against an internally defined DTD
				// or internal xml schema location hints.
				cfXmlData.parseXml(val, true, "", rtn);
			} else if (!vs.isSchema()) {
				// This will deserialize a cfXmlData correctly or get the string
				// value out of a cfStringData.
				Object val = cfXmlData.interpretString(_session, xml.toString());
				// Parse and validate against the specified DTD.
				cfXmlData.parseXml(val, true, vs, rtn);
			} else {
				cfXmlData xmlObj = null;
				// If we already have a cfXmlData
				if (xml instanceof cfXmlData)
					xmlObj = (cfXmlData) xml;

				// Parse into a cfXmlData if necessary
				if (xmlObj == null)
					xmlObj = cfXmlData.parseXml(cfXmlData.interpretString(_session, xml.toString()), true, null, rtn);

				// Now perform xml schema validation
				if (xmlObj != null)
					cfXmlData.validateXml(xmlObj.getXMLNode(), vs, rtn);
			}
		} catch (IOException ex) {
			throw new cfmRunTimeException(catchDataFactory.javaMethodException("errorCode.javaException", ex.getClass().getName(), ex.getMessage(), ex));
		}
		return rtn;
	}

}
