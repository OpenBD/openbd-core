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
 *  README.txt @ http://openbd.org/license/README.txt
 *  
 *  http://openbd.org/
 *  $Id: cfINVOKECommon.java 1831 2011-11-27 16:28:49Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfWSParameters;
import com.naryx.tagfusion.cfm.engine.cfcMethodData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFUndefinedValue;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;

public abstract class cfINVOKECommon extends cfTag implements cfOptionalBodyTag, Serializable {
	static final long serialVersionUID = 1;

	public static final String DATA_BIN_KEY = "CFINVOKE_DATA";

	protected String endMarker = null;
	protected boolean componentCall = false;

	public cfINVOKECommon() {
		endMarker = null;
		tagName = "CFINVOKE";
	}

	public String getEndMarker() {
		return endMarker;
	}

	public void setEndTag() {
		endMarker = null;
	}

	public void lookAheadForEndTag(tagReader inFile) {
		endMarker = (new tagLocator(tagName, inFile)).findEndMarker();
	}

	
	
	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
		
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;


		// CFMX doesn't require a method attribute with CFINVOKE, but
		// CFINVOKE provides no useful purpose without it
		if (!containsAttribute("METHOD"))
			throw missingAttributeException("CFINVOKE tag must contain a METHOD attribute", null);
	}

	
	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		
		if (containsAttribute(attributes, "WEBSERVICE")) {
			this.componentCall = false;
			renderWebService(attributes, _Session);
		} else if (containsAttribute(attributes, "COMPONENT")) {
			this.componentCall = true;
			renderComponent(attributes, _Session);
		} else {
			this.componentCall = true;
			renderComponentMethod(attributes, _Session);
		}

		return cfTagReturnType.NORMAL;
	}

	protected abstract void renderWebService(cfStructData attributes, cfSession session) throws cfmRunTimeException;

	protected abstract cfData invokeJavaMethod(cfStructData attributes, cfSession _Session, Object theObj) throws cfmRunTimeException;

	
	
	private void renderComponent(cfStructData attributes, cfSession session) throws cfmRunTimeException {
		cfData rtn = null;
		cfComponentData component = null;

		if (this.containsAttribute(attributes,"COMPONENT")) {
			String methodName = getDynamic(attributes,session, "METHOD").getString();
			cfData comp = getDynamic(attributes,session, "COMPONENT");
			
			if (comp.getDataType() == cfData.CFSTRINGDATA) {
				
				// not a component instance so create one of specified type
				// in CFMX this could be a java class, but we aren't supporting that
				component = new cfComponentData(session, comp.getString());
				rtn = invokeComponentMethod(attributes, session, component, methodName);
				
			} else if (comp.getDataType() == cfData.CFCOMPONENTOBJECTDATA) {

				// must be a component instance
				component = (cfComponentData) comp;
				rtn = invokeComponentMethod(attributes, session, component, methodName);

			} else if (comp.getDataType() == cfData.CFJAVAOBJECTDATA) {

				this.componentCall = false;
				cfJavaObjectData obj = (cfJavaObjectData) comp;
				rtn = invokeJavaMethod(attributes, session, obj.getInstance());

			} else {
				throw newRunTimeException(catchDataFactory.invalidTagAttributeException(this, "COMPONENT attribute contains unsupported type"));
			}
		} else {
			rtn = this.renderComponentMethod(attributes, session);
		}

		if (containsAttribute(attributes, "RETURNVARIABLE")) {
			cfStringData returnVariableName = (cfStringData) getDynamic(attributes, session, "RETURNVARIABLE");
			if (returnVariableName.getLength() != 0) {
				session.setData(returnVariableName.toString(), rtn);
			}
		}
	}

	
	
	private cfData renderComponentMethod(cfStructData attributes, cfSession session) throws cfmRunTimeException {
		cfData rtn = null;

		String methodName = getDynamic(attributes, session, "METHOD").getString();
		cfComponentData component = session.getActiveComponentData();
		if (component != null)
			rtn = invokeComponentMethod(attributes, session, component, methodName);
		else
			rtn = invokeUDF(attributes, session, methodName);

		if (containsAttribute(attributes, "RETURNVARIABLE")) {
			cfStringData returnVariableName = (cfStringData) getDynamic(attributes, session, "RETURNVARIABLE");
			if (returnVariableName.getLength() > 0) {
				session.setData(returnVariableName.toString(), rtn);
			}
		}

		return rtn;
	}

	
	
	private cfData invokeComponentMethod(cfStructData attributes, cfSession session, cfComponentData component, String methodName) throws cfmRunTimeException {
		cfcMethodData invocationData = new cfcMethodData(session, methodName, getNamedArguments(attributes, session));
		return component.invokeComponentFunction(session, invocationData);
	}

	
	
	private cfData invokeUDF(cfStructData attributes, cfSession session, String functionName) throws cfmRunTimeException {
		cfData udf = session.getData(functionName);
		if ((udf == null) || !(udf instanceof userDefinedFunction))
			throw newRunTimeException(catchDataFactory.tagRuntimeException(this, "Could not find function: " + functionName, null));

		return ((userDefinedFunction) udf).execute(session, getNamedArguments(attributes, session), false);
	}

	
	
	private cfArgStructData getNamedArguments(cfStructData attributes, cfSession session) throws cfmRunTimeException {
		cfWSParameters args = getArguments(attributes, session, null);
		cfArgStructData namedArguments = new cfArgStructData();

		String[] argNames 	= args.getNamesArray();
		Object[] argValues 	= args.getValuesArray();

		for (int i = 0; i < argNames.length; i++)
			namedArguments.setData(argNames[i], (cfData) argValues[i]);

		return namedArguments;
	}

	
	
	// CFMX's order of argument/attribute precedence is:
	// 1. Values collected from subordinate CFINVOKEARGUMENT tags
	// 2. Values collected from additional attributes to the CFINVOKE tag
	// 3. Values collected from the the structure passed in the ARGUMENTCOLLECTION attribute
	// Note: The values specified in subordinate CFINVOKEARGUEMENT tags and
	// the values specified in the ARGUMENTCOLLECTION struct are not subject
	// to the filtering of reservedWords. All values specified there are to
	// be passed directly to the target web service itself.
	protected cfWSParameters getArguments(cfStructData attributes, cfSession _Session, String[] reservedWords) throws cfmRunTimeException {
		cfWSParameters op = new cfWSParameters(reservedWords);
		cfData key = null;
		cfData val = null;
		Object naturalVal = null;

		// Get any arguments from the ARGUMENTCOLLECTION ref
		if (containsAttribute(attributes, "ARGUMENTCOLLECTION")) {
			cfData argsCol = getDynamic(attributes, _Session, "ARGUMENTCOLLECTION");
			processArgumentCollection(_Session, op, key, argsCol, false, false);
		}

		// Get any arguments passed in as extra attributes
		String[] reserved = new String[] { "ARGUMENTCOLLECTION", "COMPONENT", "METHOD", "RETURNVARIABLE", "WEBSERVICE" };

		Iterator<String> iter = this.properties.keySet().iterator();
		while (iter.hasNext()) {
			String k = iter.next();
			boolean isReserved = false;
			for (int i = 0; i < reserved.length; i++) {
				if (reserved[i].equalsIgnoreCase(k)) {
					isReserved = true;
					break;
				}
			}
			if (!isReserved) {
				val = getDynamic(_Session, k);
				naturalVal = getNatural(_Session, val, k);
				op.add(k, naturalVal, true, false);
			}
		}

		// Get any arguments passed in on nested CFINVOKEARGUMENT tags
		Map<cfData, cfData[]> invokeArgs = new HashMap<cfData, cfData[]>();
		// save old arguments for nesting of CFINVOKE tags; see bug #115
		Object oldInvokeArgs = _Session.getDataBin(DATA_BIN_KEY);
		_Session.setDataBin(DATA_BIN_KEY, invokeArgs);
		renderToString(_Session);

		Iterator<cfData> argsIter = invokeArgs.keySet().iterator();
		while (argsIter.hasNext()) {
			key = argsIter.next();
			if (!cfStringData.class.isAssignableFrom(key.getClass())) {
				throw newRunTimeException(catchDataFactory.invalidTagAttributeException(this, "NAME"));
			}

			cfData[] vals = invokeArgs.get(key);
			val = vals[0];
			if (!cfJavaObjectData.class.isAssignableFrom(val.getClass()) && (val.getDataType() != cfData.CFNULLDATA)) {
				throw newRunTimeException(catchDataFactory.invalidTagAttributeException(this, "VALUE"));
			}

			if (key.getString().equalsIgnoreCase("ARGUMENTCOLLECTION")) {
				processArgumentCollection(_Session, op, key, val, false, ((cfBooleanData) vals[1]).getBoolean());
			} else {
				String k = key.toString();
				naturalVal = getNatural(_Session, val, k);
				op.add(k, naturalVal, false, ((cfBooleanData) vals[1]).getBoolean());
			}
		}

		if (oldInvokeArgs == null) {
			_Session.deleteDataBin(DATA_BIN_KEY);
		} else { // restore old arguments for nesting of CFINVOKE tags; see bug #115
			_Session.setDataBin(DATA_BIN_KEY, oldInvokeArgs);
		}

		// Return the arguments
		return op;
	}

	
	
	protected Object getNatural(cfSession _Session, cfData value, String attributeName) {
		Object naturalVal;
		if (!this.componentCall)
			naturalVal = tagUtils.getNatural(value, true, true);
		else
			naturalVal = value;

		if (naturalVal == null) {
			cfCatchData catchData = new cfCatchData(_Session);
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
			catchData.setErrorCode("errorCode.runtimeError");
			catchData.setDetail("Attribute " + attributeName + " has an invalid value");
			catchData.setMessage("Invalid web service parameter.");
			return new cfmRunTimeException(catchData);
		}

		return naturalVal;
	}

	
	
	protected void processArgumentCollection(cfSession _Session, cfWSParameters op, cfData key, cfData argsCol, boolean filter, boolean omit) throws cfmRunTimeException {
		if (argsCol == null || !cfStructData.class.isAssignableFrom(argsCol.getClass())) {
			throw newRunTimeException("Attribute ARGUMENTCOLLECTION must be a Struct");
		}

		cfStructData aa = (cfStructData) argsCol;
		Iterator itr = aa.keySet().iterator();
		String strKey = null;

		while (itr.hasNext()) {
			Object obj = itr.next();
			if (obj instanceof String) {
				strKey = (String) obj;
			} else if (obj instanceof cfStringData) {
				strKey = ((cfStringData) obj).toString();
			} else if (!cfStringData.class.isAssignableFrom(key.getClass())) {
				throw newRunTimeException("ARGUMENTCOLLECTION Struct contains invalid key");
			}

			cfData val = aa.getData(strKey);
			if ( val instanceof CFUndefinedValue )
				continue;
			else if (!cfJavaObjectData.class.isAssignableFrom(val.getClass())) {
				throw newRunTimeException("ARGUMENTCOLLECTION Struct contains invalid value");
			}

			Object naturalVal = getNatural(_Session, val, strKey);
			op.add(strKey, naturalVal, filter, omit);
		}
	}

}