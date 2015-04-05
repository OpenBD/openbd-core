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

package com.naryx.tagfusion.cfm.engine;

import java.util.List;

/**
 * This class is used to represent data that is not really there! :-)
 */

public class cfNullData extends cfJavaObjectData implements java.io.Serializable {

	static final long serialVersionUID = 1;

	public static cfNullData NULL = new cfNullDataStatic();

	public static cfNullData JAVA_NULL = new cfNullDataStatic().setJavaNull(true);

	// This flag is used to indicate if the null value came from a database field
	// or not.
	// If it did then in some cases we need to treat it as an empty string in
	// order to
	// stay compatible with CF6.1 and CF7. Refer to bugs 1745 and 1691.
	private boolean dbNull;

	// This flag indicates whether the null was the return value from a Java
	// method call.
	private boolean javaNull;

	public cfNullData() {
	}

	public boolean isDBNull() {
		return dbNull;
	}

	public cfNullData setDBNull(boolean _dbNull) {
		dbNull = _dbNull;
		return this;
	}

	public boolean isJavaNull() {
		return javaNull;
	}

	// only static subclass can set javaNull flag
	protected cfNullData setJavaNull(boolean _javaNull) {
		javaNull = _javaNull;
		return this;
	}

	/**
	 * Make sure we have a DB null and not the NULL static instance
	 */
	public cfNullData getDbNull() {
		if (this.dbNull) {
			return this;
		}
		return new cfNullData().setDBNull(true);
	}

	/*
	 * This constructor is only used by the JavaCast() function to get a new
	 * cfNullData object with javaCast set to the passed in value. It doesn't call
	 * copy() or duplicate() because then it would be setting the javaCast value
	 * for the static cfNullData object.
	 */
	public cfNullData(Javacast castType, boolean _dbNull) {
		setJavaCast(castType);
		dbNull = _dbNull;
	}

	public byte getDataType() {
		return cfData.CFNULLDATA;
	}

	public String getDataTypeName() {
		return "null";
	}

	public String getString() {
		return "";
	}

	public double getDouble() {
		return 0;
	}

	public boolean getBoolean() {
		return false;
	}

	public cfDateData getDateData() throws dataNotSupportedException {
		throw new dataNotSupportedException();
	}

	public long getLong() {
		return 0;
	}

	public int getInt() {
		return 0;
	}

	public cfNumberData getNumber() {
		return new cfNumberData(0);
	}

	public cfData duplicate() {
		return new cfNullData().setDBNull(dbNull);
	}

	public void dumpWDDX(int version, java.io.PrintWriter out) {
		if (version > 10)
			out.write(dbNull ? "<s></s>" : "<n/>");
		else
			out.write(dbNull ? "<string></string>" : "<null/>");
	}

	public String toString() {
		return "[null]";
	}

	public String toString(String _lbl) {
		return toString();
	}

	public boolean equals(Object _o) {
		return _o instanceof cfNullData;
	}

	/***
	 * private subclass for creating static instances
	 */
	private static class cfNullDataStatic extends cfNullData {

		private static final long serialVersionUID = 1L;

		public cfNullDataStatic() {
		}

		public cfData duplicate() {
			return this; // cfNullDataStatic is immutable
		}

		/**
		 * The following methods are not allowed to be invoked for static instances.
		 */
		public cfNullData setDBNull(boolean _dbNull) {
			throw new UnsupportedOperationException("static instance");
		}

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
}
