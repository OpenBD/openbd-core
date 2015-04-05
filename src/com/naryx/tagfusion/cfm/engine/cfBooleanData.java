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
 */

package com.naryx.tagfusion.cfm.engine;

import java.util.List;

public class cfBooleanData extends cfJavaObjectData implements java.io.Serializable {
	static final long serialVersionUID = 1;

	/** The boolean representing the value of this cfData. */
	private boolean data;

	private String dataStr;

	private boolean isJavaBoolean;

	public final static cfBooleanData TRUE = new cfBooleanDataStatic(true);

	public final static cfBooleanData FALSE = new cfBooleanDataStatic(false);

	/**
	 * Private constructor to ensure that there are only ever 2 cfBooleanData
	 * instances.
	 */

	protected cfBooleanData(boolean _data) {
		// note cfobject method calls will operate on "true"/"false"
		super(_data ? "true" : "false");
		data = _data;
		dataStr = _data ? "YES" : "NO";
	}

	protected cfBooleanData(Boolean _data) {
		// note cfobject method calls will operate on "true"/"false"
		super(_data);
		data = _data.booleanValue();
		dataStr = data ? "YES" : "NO";
		isJavaBoolean = true;
	}

	private cfBooleanData(boolean _data, String _dataStr) {
		super(_dataStr);
		data = _data;
		dataStr = _dataStr;
	}

	/**
	 * The following static methods are factory methods for obtaining
	 * cfBooleanData's.
	 */

	public static cfBooleanData getcfBooleanData(boolean _b) {
		return _b ? TRUE : FALSE;
	}

	/**
	 * returns cfBooleanData.TRUE when the _data String is true, yes or 1
	 * otherwise it returns cfBooleanData.FALSE
	 */
	public static cfBooleanData getcfBooleanData(String _data) {
		return new cfBooleanData(com.nary.util.string.convertToBoolean(_data, false), _data);
	}

	public static cfBooleanData getcfBooleanData(boolean _b, String _image) {
		return new cfBooleanData(_b, _image);
	}

	/**
	 * This is used soley when a Boolean is returned from a java method so that it
	 * may subsequently be treated as a java.lang.Boolean when passed around as a
	 * java object as opposed to a java.lang.String
	 */
	public static cfBooleanData getcfBooleanData(Boolean _b) {
		return new cfBooleanData(_b);
	}

	public byte getDataType() {
		return cfData.CFBOOLEANDATA;
	}

	public String getDataTypeName() {
		return "boolean";
	}

	public boolean isBooleanConvertible() {
		return true;
	}

	public boolean getBoolean() {
		return data;
	}

	public cfDateData getDateData() throws dataNotSupportedException {
		throw new dataNotSupportedException("cannot convert boolean to date type : " + getString());
	}

	public String getString() {
		return dataStr;
	}

	public boolean isJavaBoolean() {
		return isJavaBoolean;
	}

	// this versio of equals is for use by generic Collections classes
	public boolean equals(Object o) {
		if (o instanceof cfBooleanData)
			return (data == ((cfBooleanData) o).data);

		return false;
	}

	public boolean equals(cfData o) {
		if (o.getDataType() == cfData.CFBOOLEANDATA)
			return (data == ((cfBooleanData) o).data);

		return false;
	}

	public int hashCode() {
		return (data ? TRUE.objHashCode() : FALSE.objHashCode());
	}

	public int objHashCode() {
		return super.hashCode();
	}

	public cfData duplicate() {
		return new cfBooleanData(data, dataStr);
	}

	public void dump(java.io.PrintWriter out, String _label, int _top) {
		out.print(this.getString());
	}

	public void dumpWDDX(int version, java.io.PrintWriter out) {
		if (version > 10) {
			out.write("<b v='");
		} else {
			out.write("<boolean value='");
		}
		out.write(toString());
		out.write("'/>");
	}

	public String toString() {
		return String.valueOf(data);
	}

	public cfNumberData getNumber() {
		if (data) {
			return new cfNumberData(1);
		} else {
			return new cfNumberData(0);
		}
	}

	public boolean isNumberConvertible() {
		return true;
	}

	public double getDouble() {
		return (data ? 1.0 : 0.0);
	}

	public int getInt() {
		return (data ? 1 : 0);
	}

	/***
	 * private subclass for static instances
	 */
	private static class cfBooleanDataStatic extends cfBooleanData {

		private static final long serialVersionUID = 1L;

		public cfBooleanDataStatic(boolean b) {
			super(b);
		}

		/**
		 * The following methods are not allowed to be invoked for static instances.
		 */
		public void setQueryTableData(List<List<cfData>> queryTableData, int queryColumn) {
			throw new UnsupportedOperationException("static instance");
		}

		public void setExpression(boolean exp) {
			throw new UnsupportedOperationException("static instance");
		}

		protected void setImplicit(boolean implicit) {
			throw new UnsupportedOperationException("static instance");
		}
	}

	protected synchronized void createInstance() throws cfmRunTimeException {
		if (instance == null) {
			if (isJavaBoolean) {
				instance = new Boolean(data);
			} else {
				instance = new String(data ? "true" : "false");
			}
		}
	}

	public Class<?> getInstanceClass() {
		if (isJavaBoolean)
			return Boolean.class;
		else
			return String.class;
	}

}