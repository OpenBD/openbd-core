/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
 *  $Id: cfCatchData.java 2374 2013-06-10 22:14:24Z alan $ 
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

package com.naryx.tagfusion.cfm.engine;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlURI;
import com.naryx.tagfusion.cfm.tag.cfOUTPUT;
import com.naryx.tagfusion.cfm.tag.cfSCRIPT;
import com.naryx.tagfusion.cfm.tag.cfTag;

public class cfCatchData extends cfStructData implements java.io.Serializable {
	static final long serialVersionUID = 1;

	public static final cfStringData TYPE_APPLICATION = new cfStringData("Application");

	public static final cfStringData TYPE_ANY = new cfStringData("Any");

	public static final cfStringData TYPE_DATABASE = new cfStringData("Database");

	public static final cfStringData TYPE_TEMPLATE = new cfStringData("Template");

	public static final cfStringData TYPE_TEMPLATE_EXPIRED = new cfStringData("TemplateExpired");

	public static final cfStringData TYPE_SECURITY = new cfStringData("Security");

	public static final cfStringData TYPE_OBJECT = new cfStringData("Object");

	public static final cfStringData TYPE_MISSINGINCLUDE = new cfStringData("MissingInclude");

	public static final cfStringData TYPE_EXPRESSION = new cfStringData("Expression");

	public static final cfStringData TYPE_LOCK = new cfStringData("Lock");

	public static final cfStringData TYPE_SEARCH = new cfStringData("SearchEngine");

	private static final String TYPE_KEY = "type";

	private static final String DETAIL_KEY = "detail";

	private static final String MESSAGE_KEY = "message";

	private static final String ERRORCODE_KEY = "errorcode";

	private static final String EXTENDEDINFO_KEY = "extendedinfo";

	private static final String TAGCONTEXT_KEY = "tagcontext";

	private static final String TAGNAME_KEY = "tagname";

	public static final String TEMPLATE_KEY = "template";

	public static final String LINE_KEY = "line";

	public static final String COLUMN_KEY = "column";

	private List<cfCatchData> manyErrorList = null;

	private List<String> functionList = null;

	// Address to the file to which this error occurred
	private cfmlURI fileURI; 

	// internal errors are never caught by CFCATCH
	private boolean internal = false; 
	private Throwable exceptionThrown = null;

	public boolean isInternal() {
		return internal;
	}

	public void setInternal(boolean i) {
		internal = i;
	}

	private boolean sessionSet = false;

	public cfCatchData() {
		setData(TYPE_KEY, TYPE_APPLICATION);
		setData(DETAIL_KEY, new cfStringData(""));
		setData(MESSAGE_KEY, new cfStringData(""));
		setData(ERRORCODE_KEY, new cfStringData(""));
		setData(EXTENDEDINFO_KEY, new cfStringData(""));
		setData(TAGCONTEXT_KEY, cfArrayData.createArray(1));
	}

	public cfCatchData(cfmlURI _fileURI, List<cfCatchData> lotsOfErrors) {
		setData(TYPE_KEY, TYPE_TEMPLATE);
		setData(DETAIL_KEY, new cfStringData(""));
		setData(MESSAGE_KEY, new cfStringData(""));
		setData(ERRORCODE_KEY, new cfStringData(""));
		setData(EXTENDEDINFO_KEY, new cfStringData(""));
		setData(TAGCONTEXT_KEY, cfArrayData.createArray(1));
		manyErrorList = lotsOfErrors;
		fileURI = _fileURI;

		Iterator<cfCatchData> i = manyErrorList.iterator();
		while (i.hasNext()) {
			i.next().setFileURI(fileURI);
		}
	}

	public cfCatchData(cfSession _parent) {
		setSession(_parent);
		setData(TYPE_KEY, TYPE_APPLICATION);
		setData(DETAIL_KEY, new cfStringData(""));
		setData(MESSAGE_KEY, new cfStringData(""));
		setData(ERRORCODE_KEY, new cfStringData(""));
		setData(EXTENDEDINFO_KEY, new cfStringData(""));
	}

	public Throwable getExceptionThrown(){
		return exceptionThrown;
	}
	
	public String getString() {
		return getString(MESSAGE_KEY);
	}

	public String getMessage() {
		return getString(MESSAGE_KEY);
	}

	public String getDetail() {
		return getString(DETAIL_KEY);
	}

	public String getExtendedInfo() {
		return getString(EXTENDEDINFO_KEY);
	}

	public List<cfCatchData> getErrorList() {
		return manyErrorList;
	}

	public cfmlURI getFileURI() {
		return fileURI;
	}

	public void setFileURI(cfmlURI _fileURI) {
		fileURI = _fileURI;
	}

	public void setSession(cfSession _parent) {
		if ((_parent == null) || sessionSet) {
			return;
		}
		sessionSet = true;

		cfArrayData arrayData = cfArrayData.createArray(1);
		Enumeration<cfTag> E = _parent.getTagElements();
		cfTag tg = null;
		cfStructData structData = null;
		while (E.hasMoreElements()) {
			tg = E.nextElement();
						
			structData = new cfStructData();
			structData.setData("id", 					new cfStringData(tg.getTagName()));
			structData.setData(LINE_KEY, 			new cfNumberData(tg.posLine));
			structData.setData(COLUMN_KEY, 		new cfNumberData(tg.posColumn));
			structData.setData(TEMPLATE_KEY, 	new cfStringData(tg.getFile().getName()));
			try {
				arrayData.addElementAt(structData,1);
			} catch (cfmRunTimeException ignoreAsItWillNeverHappen) {}
		}

		// See what the active file is
		cfFile activeFile	= _parent.activeFile();
		
		if (tg != null) {
			
			if ( tg.getFile() != activeFile ){

				setTemplate( activeFile.getName() );

			} else {

				setTagname(tg.getTagName());
				setTemplate(tg.getFile().getName());
				if (tg instanceof cfSCRIPT && !containsKey("scriptline")) {
					setData("scriptline", new cfNumberData(_parent.getCFContext().getLine() - 1));
				} else if (tg instanceof cfOUTPUT) {
					setData("scriptline", new cfNumberData(tg.getExpressionPosition(_parent.getActiveExpression())));
				}

				setLine(tg.posLine);
				setColumn(tg.posColumn);
			}
		}

		setData(TAGCONTEXT_KEY, arrayData);
	}

	public String getString(String _key) {
		cfData DD = getData(_key);
		if (DD == null)
			return "";
		try {
			return DD.getString();
		} catch (Exception E) {
			return "";
		}
	}

	public void setType(cfStringData _value) {
		setData(TYPE_KEY, _value);
	}

	public cfData duplicate() {
		cfCatchData duplicate = new cfCatchData();

		Object[] keys = this.keys();

		for (int i = 0; i < keys.length; i++) {
			cfData nextDataCopy = null;
			String nextKey = (String) keys[i];
			cfData nextData = getData(nextKey);
			if (nextData != null) {
				nextDataCopy = nextData.duplicate();
				if (nextDataCopy == null) { // return null if struct contains non-duplicatable type
					return null;
				}
			}
			duplicate.setData(nextKey, nextDataCopy);

		}

		return duplicate;
	}

	// --[ All the access methods for setting parameters
	public void setType(String _value) {
		setData(TYPE_KEY, new cfStringData(_value));
	}

	public void setMessage(String _value) {
		setData(MESSAGE_KEY, new cfStringData(_value));
	}

	public void setDetail(String _value) {
		setData(DETAIL_KEY, new cfStringData(_value));
	}

	public void setTagname(String _value) {
		setData(TAGNAME_KEY, new cfStringData(_value.toUpperCase()));
	}

	public void setTemplate(String _value) {
		setData(TEMPLATE_KEY, new cfStringData(_value));
	}

	public void setNativeErrorCode(String _value) {
		setData("nativeerrorcode", new cfStringData(_value));
	}

	public void setSqlState(String _value) {
		setData("sqlstate", new cfStringData(_value));
	}

	public void setSql(String _value) {
		setData("sql", new cfStringData(_value));
	}

	public void setDataSource(String _value) {
		setData("datasource", new cfStringData(_value));
	}

	public void setErrNumber(String _value) {
		setData("errnumber", new cfStringData(_value));
	}

	public void setMissingFilename(String _value) {
		setData("missingfilename", new cfStringData(_value));
	}

	public void setLockName(String _value) {
		setData("lockname", new cfStringData(_value));
	}

	public void setLockOperation(String _value) {
		setData("lockoperation", new cfStringData(_value));
	}

	public void setExtendedInfo(String _value) {
		setData(EXTENDEDINFO_KEY, new cfStringData(_value));
	}

	public void setJavaException(Throwable _value) {
		setData("javaexception", 				new cfStringData(_value.getClass().getName()));
		setData("javaexceptionobject", 	new cfJavaObjectData(_value) );
		exceptionThrown = _value;
	}

	public void setErrorCode(String _value) {
		setData(ERRORCODE_KEY, new cfStringData(_value));
	}

	public void setLine(int _line) {
		setData(LINE_KEY, new cfNumberData(_line));
	}

	public void setColumn(int _column) {
		setData(COLUMN_KEY, new cfNumberData(_column));
	}

	public String getType() {
		return super.getData(TYPE_KEY).toString();
	}

	public int getLine() {
		return ((cfNumberData) super.getData(LINE_KEY)).getInt();
	}

	public String getTagname() {
		return ((cfStringData) super.getData(TAGNAME_KEY)).getString();
	}

	public int getColumn() {
		return ((cfNumberData) super.getData(COLUMN_KEY)).getInt();
	}

	public void setSqlException(java.sql.SQLException e) {
		setNativeErrorCode(Integer.toString(e.getErrorCode()));
		setSqlState(e.getSQLState());
		setData("queryError", new cfStringData(e.getMessage()));
		exceptionThrown = e;
	}

	public void addFunction(String function) {
		if (functionList == null) {
			functionList = new ArrayList<String>();
		}
		functionList.add(0, function);
	}

	public String[] getFunctionList() {
		if (functionList == null) {
			return new String[0];
		}
		String[] functionArray = new String[functionList.size()];
		for (int i = 0; i < functionArray.length; i++) {
			functionArray[i] = functionList.get(i);
		}
		return functionArray;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(32);

		if (manyErrorList != null) {
			Iterator<cfCatchData> i = manyErrorList.iterator();
			while (i.hasNext()) {
				cfCatchData t = i.next();
				sb.append(t.toString());
			}

		} else {
			sb.append("[--Catch Data--]\r\n");
			sb.append("type: ");
			sb.append(getString(TYPE_KEY));
			sb.append("\r\n");

			sb.append("tagname: ");
			sb.append(getString(TAGNAME_KEY));
			sb.append("\r\n");

			sb.append("template: ");
			sb.append(getString(TEMPLATE_KEY));
			sb.append("\r\n");

			sb.append("Line: ");
			sb.append(getString(LINE_KEY));
			sb.append("; Column: ");
			sb.append(getString(COLUMN_KEY));
			sb.append("\r\n");

			sb.append("ErrorCode: ");
			sb.append(getString(ERRORCODE_KEY));
			sb.append("\r\n");

			sb.append("message: ");
			sb.append(getString(MESSAGE_KEY));
			sb.append("\r\n");

			sb.append("detail: ");
			sb.append(getString(DETAIL_KEY));
			sb.append("\r\n");

		}
		return sb.toString();
	}
}
