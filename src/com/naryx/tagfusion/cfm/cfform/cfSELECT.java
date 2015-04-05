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

package com.naryx.tagfusion.cfm.cfform;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfFormData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfSELECT extends cfAbstractFormTag implements Serializable {

	static final long serialVersionUID = 1;

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("ONERROR", "tf_on_error");
		defaultAttribute("QUERYPOSITION", "below");
		parseTagHeader(_tag);

		if (!containsAttribute("NAME"))
			throw newBadFileException("Missing NAME", "You need to provide a NAME");

		if (containsAttribute("QUERY") && !containsAttribute("VALUE"))
			throw newBadFileException("Missing VALUE", "You need to provide a VALUE attribute.");
	}

	public String getEndMarker() {
		return "</CFSELECT>";
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfFormInputData formData;
		formData = (cfFormInputData) _Session.getDataBin(cfFORM.DATA_BIN_KEY);
		if (formData == null)
			throw newRunTimeException("There is no CFFORM tag");

		// Get the inner body
		String queryPosition = getDynamic(_Session, "QUERYPOSITION").getString().toLowerCase();
		String innerBody = renderToString(_Session, cfTag.HONOR_CF_SETTING).getOutput().trim();

		String name = getDynamic(_Session, "NAME").getString();

		String message = "Please select a value";
		if (containsAttribute("MESSAGE"))
			message = getDynamic(_Session, "MESSAGE").getString();

		String onError = null;
		if (containsAttribute("ONERROR"))
			onError = getDynamic(_Session, "ONERROR").getString();

		if (getRequired(_Session)) {
			formData.formTagRequired(name, "_SELECT", message, onError);
		}

		List<String> selected = getSelected(_Session, formData, name);

		boolean display = false;
		if (containsAttribute("DISPLAY"))
			display = true;

		_Session.write("<select name=\"" + name + "\" ");

		if (containsAttribute("PASSTHROUGH"))
			_Session.write(getDynamic(_Session, "PASSTHROUGH").getString() + " ");

		cfData multipleVal = getDynamic(_Session, "MULTIPLE");
		if (multipleVal != null && multipleVal.getBoolean())
			_Session.write(" multiple=\"multiple\"");

		StringBuilder attribs = new StringBuilder();
		this.appendAttributes(_Session, attribs, getIgnoreKeys());
		_Session.write(attribs.toString());

		_Session.write(">");

		if (!queryPosition.equals("below"))
			_Session.write(innerBody);

		// --[ This may be a CFSELECT for a query
		cfData queryDataTmp = null; // used til
		cfQueryResultData queryData = null;
		String QUERY = null;

		if (containsAttribute("QUERY")) {
			QUERY = getDynamic(_Session, "QUERY").getString();
			queryDataTmp = runTime.runExpression(_Session, QUERY);

			if (queryDataTmp != null) {

				if (queryDataTmp instanceof cfQueryResultData) {
					queryData = (cfQueryResultData) queryDataTmp;
				} else {
					throw newRunTimeException("The specifed QUERY is not a query type.");
				}

				queryData.reset();

				cfData queryField = getDynamic(_Session, "VALUE");
				cfData displayField = getDynamic(_Session, "DISPLAY");

				while (queryData.nextRow()) {
					cfData valData = queryData.getData(queryField);
					if (valData == null) {
						throw newRunTimeException(queryField + " is an invalid query column.");
					}

					String valueData = valData.getString();

					// --[ Send the data out
					_Session.write("<option");
					_Session.write(" value=\"" + valueData + "\"");

					// note this is case insensitive
					if (selected != null && selected.contains(valueData.toLowerCase()))
						_Session.write(" selected=\"selected\"");

					_Session.write(">");

					if (display) {
						cfData displayData = queryData.getData(displayField);
						if (displayData == null) {
							throw newRunTimeException(displayField + " is an invalid query column.");
						}

						_Session.write(displayData.getString());

					} else
						_Session.write(valueData);

				}
			} else {
				throw newRunTimeException("The specified QUERY " + QUERY + " does not exist.");
			}
		}

		if (queryPosition.equals("below"))
			_Session.write(innerBody);

		_Session.write("</select>");

		return cfTagReturnType.NORMAL;
	}

	private List<String> getSelected(cfSession _Session, cfFormInputData _formInputData, String _name) throws cfmRunTimeException {
		cfData selectedData = null;

		// if we're preserving data then look to see if a value is already in the
		// form scope
		if (_formInputData.isPreserveData()) {
			cfFormData formdata = (cfFormData) _Session.getQualifiedData(variableStore.FORM_SCOPE);
			selectedData = formdata.getData(_name);
		}

		// if we haven't already retrieved the value from the form scope and the
		// VALUE is specified
		if (selectedData == null && containsAttribute("SELECTED")) {
			selectedData = getDynamic(_Session, "SELECTED");
		}

		if (selectedData != null) {
			return com.nary.util.string.split(selectedData.getString().toLowerCase(), ',');
		} else {
			return null;
		}
	}

	protected Set<String> getIgnoreKeys() {
		Set<String> ignoreKeys = super.getIgnoreKeys();
		ignoreKeys.add("VALUE");
		ignoreKeys.add("DISPLAY");
		ignoreKeys.add("MULTIPLE");
		ignoreKeys.add("QUERY");
		ignoreKeys.add("SELECTED");
		ignoreKeys.add("QUERYPOSITION");
		return ignoreKeys;
	}
}
