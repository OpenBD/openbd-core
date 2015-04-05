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

import com.nary.util.string;
import com.naryx.tagfusion.cfm.tag.cfDUMP;

/**
 * This class represents the underlying data structure for all dynamic data
 * types in the engine.
 * 
 */

public abstract class cfData extends Object implements java.io.Serializable {

	static final long serialVersionUID = 1;

	// added constants below for javacast implementation
	public static enum JavaCast { INT, LONG, BOOLEAN, DOUBLE, FLOAT, STRING, BIGDECIMAL, 
		BYTE, CHAR, SHORT, INT_ARRAY, LONG_ARRAY, BOOLEAN_ARRAY, DOUBLE_ARRAY, FLOAT_ARRAY, 
		STRING_ARRAY, BIGDECIMAL_ARRAY, BYTE_ARRAY, CHAR_ARRAY, SHORT_ARRAY	};
	
	public final static byte UNKNOWN = 0, CFNUMBERDATA = 1, CFBOOLEANDATA = 2, CFSTRINGDATA = 3, CFDATEDATA = 4, CFNULLDATA = 5, CFSTRUCTDATA = 10, CFARRAYDATA = 11, CFQUERYRESULTDATA = 15, CFCOMPONENTOBJECTDATA = 20, CFWSOBJECTDATA = 21, CFJAVAOBJECTDATA = 25, CFBINARYDATA = 28, CFLDATA = 30, CFUDFDATA = 40, OTHER = 127;

	/***
	 * ATTENTION! Subclasses of cfData that create static instances MUST override
	 * the setQueryTableData() method and throw an exception if that method is
	 * invoked. It's probably a good idea to override the setJavaCast(),
	 * setReference(), setImplicit(), and setExpression() methods and also throw
	 * exceptions if these are invoked.
	 * 
	 * Basically, none of the private cfData attributes should be allowed to be
	 * changed from their defaults for static subclass instances.
	 */

	private Javacast javacast;

	private List<List<cfData>> queryTableData; // for regular queries

	private int queryColumn;

	private boolean expression; // if true, the instance may contain an expression

	// that needs to be evaluated

	private boolean isImplicit;

	private boolean invalidLoopIndex = false; // valid unless explicitly
																						// invalidated (default to false for

	
	// compatible deserialization of 6.2.1 client data)

	/**
	 * invalidLoopIndex is used to enhance performance of CFLOOP. The idea is that
	 * a cfData subclass being used as a CFLOOP index can be modified directly
	 * rather than creating a new cfData instance. For example, when looping over
	 * a numeric range, rather than creating a new cfNumberData every time the
	 * index is incremented, just update the same cfNumberData instance.
	 * 
	 * This doesn't work is when the loop index is being stored in a shared scope
	 * that uses "native" J2EE format ((request, session, or application). For
	 * example:
	 * 
	 * <cfloop from="1" to="10" index="request.loopIndex"> ... </cfloop>
	 * 
	 * In this case, modifying the cfData instance doesn't modify the shared scope
	 * variable. Therefore, the loop index needs to be "invalidated" to let the
	 * CFLOOP code know that it can't simply modify the cfData instance.
	 * 
	 * Also, if the loop index variable is assigned a new value within the loop
	 * body, then the loopIndex needs to be invalidated; for example:
	 * 
	 * <cfloop from="1" to="10" index="i"> <cfset i = i + 1> </cfloop>
	 * 
	 * See the CFLOOP code that references the isValidLoopIndex() method.
	 */

	public void invalidateLoopIndex() {
		invalidLoopIndex = true;
	}

	public boolean isValidLoopIndex() {
		return !invalidLoopIndex;
	}

	public boolean isExpression() {
		return expression;
	}
	
	public boolean isLoopIndex(){
		return false;
	}
	
	public void setExpression(boolean _exp) {
		expression = _exp;
	}

	// --[ returns the type of cfData. Use instead of instanceof when you already
	// know you have a cfData object.
	public byte getDataType() {
		return UNKNOWN;
	}

	public String getDataTypeName() {
		return "unknown";
	}

	// allows subclasses of cfStructData to identify themselves as such when they
	// need to use a different data type (such as CFCOMPONENTOBJECTDATA)
	public boolean isStruct() {
		return false;
	}

	public int getQueryColumn() {
		return queryColumn;
	}

	public List<List<cfData>> getQueryTableData() {
		return queryTableData;
	}

	public void setQueryTableData(List<List<cfData>> _queryTableData, int _queryColumn) {
		queryTableData = _queryTableData;
		queryColumn = _queryColumn;
	}

	public cfData getData(String _key) {
		return null;
	}

	public cfData getData(cfData arrayIndex) throws cfmRunTimeException {
		return (queryTableData != null) ? cfQueryResultData.getCellData(queryTableData, arrayIndex.getInt(), queryColumn) : null;
	}

	public void setData(String _key, cfData _data) {
	}

	public void setData(cfData arrayIndex, cfData _data) throws cfmRunTimeException {
		if (queryTableData != null)
			cfQueryResultData.setCellData(queryTableData, arrayIndex.getInt(), queryColumn, _data);
	}

	public void deleteData(String _key) throws cfmRunTimeException {
	}

	/**
	 * This method and it's use was added to fix bug #2083. Any cfdata that's
	 * created which we wish to ensure is not seen by or accessible to a person's
	 * cfml code should set the isImplicit flag to true.
	 * 
	 * @return true if this object is considered to be for BlueDragon use only and
	 *         should not be accessible to a person's cfml code, else false.
	 */
	public boolean isImplicit() {
		return isImplicit;
	}

	protected void setImplicit(boolean implicit) {
		isImplicit = implicit;
	}

	public cfData duplicate() {
		return null;
	}

	public cfData coerce(byte toDataType) throws dataNotSupportedException {
		if (this.getDataType() == toDataType) // no need to convert
			return this;

		switch (toDataType) {
		case CFNUMBERDATA:
			return this.getNumber();

		case CFBOOLEANDATA:
			return cfBooleanData.getcfBooleanData(this.getBoolean(), this.getString());

		case CFSTRINGDATA:
			return new cfStringData(this.getString());

		case CFDATEDATA:
			return this.getDateData();

		default:
			throw new dataNotSupportedException();
		}
	}

	/*
	 * returns true if the cfData passed in is a String, Number, Boolean or Date;
	 * false otherwise. Note that cfLData's will not be evaluated by this function
	 * hence will return false in this case.
	 */
	public static boolean isSimpleValue(cfData _d) {
		return isSimpleValue(_d.getDataType());
	}

	private static boolean isSimpleValue(byte dataType) {
		// UNKNOWN is used by CFUndefinedValue, and can be converted to a string
		return ((dataType >= UNKNOWN) && (dataType <= CFNULLDATA));
	}

	// check for number without throwing exception
	public boolean isNumberConvertible() {
		return false;
	}

	public double getDouble() throws dataNotSupportedException {
		throw new dataNotSupportedException();
	}

	public String getString() throws dataNotSupportedException {
		throw new dataNotSupportedException();
	}

	// check for boolean without throwing exception
	public boolean isBooleanConvertible() {
		return false;
	}

	public boolean getBoolean() throws dataNotSupportedException {
		throw new dataNotSupportedException();
	}

	// check for date without throwing exception
	public boolean isDateConvertible() {
		return false;
	}

	public cfDateData getDateData() throws dataNotSupportedException {
		throw new dataNotSupportedException();
	}

	public long getLong() throws dataNotSupportedException {
		throw new dataNotSupportedException();
	}

	public long getDateLong() throws dataNotSupportedException {
		throw new dataNotSupportedException();
	}

	public int getInt() throws dataNotSupportedException {
		throw new dataNotSupportedException();
	}

	public cfNumberData getNumber() throws dataNotSupportedException {
		throw new dataNotSupportedException();
	}

	public String toString() {
		return "";
	}

	public String toString(String _label) {
		return toString();
	}

	public String getName() {
		return "";
	}

	public Javacast getJavaCast() {
		return javacast;
	}

	public void setJavaCast( Javacast _cast ) {
		javacast = _cast;
	}
		

	public boolean equals(cfData _data) throws cfmRunTimeException {
		throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.expressionError", "cfdata.Equals"));
	}

	// the default behavior of CFDUMP is to do a "short" dump; if your tag only
	// supports one dump style, then only override this method (not dumpLong,
	// below)
	public void dump(java.io.PrintWriter out) {
		dump(out, "", cfDUMP.TOP_DEFAULT);
	}

	// if a cfData supports the LABEL attribute it must override this method
	public void dump(java.io.PrintWriter out, String _label, int _top) {
		out.print(this.toString(_label));
	}

	// by default, a "long" dump is the same as a short dump; if your tag supports
	// both long and short dumps then override this method and the one above
	public void dumpLong(java.io.PrintWriter out) {
		dump(out);
	}

	public void dumpLong(java.io.PrintWriter out, String _label, int _top) {
		dump(out, _label, _top);
	}

	public void dumpWDDX(int version, java.io.PrintWriter out) {
	}

	/**
	 * returns a cfNumberData from the given String '_s'. If however the String
	 * represents a long or float and _strict is false then it returns a
	 * cfStringData WARNING - do not use unless you know that _s is definitely a
	 * numeric
	 */
	public static cfData createNumber(String _str, boolean _strict) {
		String numStr = _str;
		if (numStr.charAt(0) == '+') {
			numStr = numStr.substring(1);
		}

		// if an int/long
		if (numStr.indexOf(".") == -1) {
			if (string.isInt(numStr)) {
				return new cfNumberData(Integer.parseInt(numStr), _str);
			} else if (_strict) {
				try {
					return new cfNumberData(Double.parseDouble(numStr), _str);
				} catch (java.lang.ArithmeticException ae) {
					return new cfNumberData(Float.parseFloat(numStr), _str);
				}
			} else {
				return new cfStringData(numStr);
			}
			// else if a double/float
		} else {
			double dblval;
			try {
				dblval = Double.parseDouble(numStr);
			} catch (java.lang.ArithmeticException ae) {

				dblval = Float.parseFloat(numStr);
				if (!_strict) {
					return new cfStringData(numStr);
				} else {
					return new cfNumberData(dblval, _str);
				}
			}

			if (!_strict && (dblval > Double.MAX_VALUE || dblval < Double.MIN_VALUE)) {
				return new cfStringData(numStr);
			} else {
				return new cfNumberData(dblval);
			}
		}
	}

	/**
	 * This behaves as createNumber() but will however perform a check first to
	 * ensure the String represents a valid numeric.
	 * 
	 * @throws dataNotSupportedException
	 *           if _s is not a valid numeric
	 */
	public static cfData createNumber_Validate(String _s, boolean _strict) throws dataNotSupportedException {
		String str = _s.trim();
		if (string.isNumber(str)) {
			return createNumber(str, _strict);
		} else {
			throw new dataNotSupportedException("value [" + _s + "] is not a number");
		}
	}

	/**
	 * compares 2 cfData's returns 1 if _cfd1 > _cfd2 returns 0 if _cfd1 == _cfd2
	 * returns -1 if _cfd1 < _cfd2
	 * 
	 * NOTE : this is used in QOQ for ORDER BY and MAX/MIN. Changes in this method
	 * for other use should be tested against QOQ
	 */

	public static int compare(cfData _cfd1, cfData _cfd2) {
		int dType1 = _cfd1.getDataType();
		int dType2 = _cfd2.getDataType();
		int compResult = 0;

		// if both numbers then do a numeric comparison
		if (dType1 == cfData.CFNUMBERDATA && dType2 == cfData.CFNUMBERDATA) {
			double double1 = ((cfNumberData) _cfd1).getDouble();
			double double2 = ((cfNumberData) _cfd2).getDouble();
			if (double1 > double2) {
				compResult = 1;
			} else if (double1 < double2) {
				compResult = -1;
			} else {
				compResult = 0;
			}
			// else if one of them numbers, try first to do a number comparsion
		} else if (dType1 == cfData.CFNUMBERDATA || dType2 == cfData.CFNUMBERDATA) {

			if (dType1 == cfData.CFNULLDATA) {
				return -1;
			} else if (dType2 == cfData.CFNULLDATA) {
				return 1;
			}

			try {
				double double1 = _cfd1.getDouble();
				double double2 = _cfd2.getDouble();
				if (double1 > double2) {
					compResult = 1;
				} else if (double1 < double2) {
					compResult = -1;
				} else {
					compResult = 0;
				}
			} catch (dataNotSupportedException dse) {
				try {
					compResult = _cfd1.getString().compareTo(_cfd2.getString());
				} catch (dataNotSupportedException dse2) {
					return 0;
				}
			}
		} else if (dType1 == cfData.CFDATEDATA || dType2 == cfData.CFDATEDATA) {
			try {
				long lDate1 = _cfd1.getDateData().getLong();
				long lDate2 = _cfd2.getDateData().getLong();
				if (lDate1 > lDate2) {
					compResult = 1;
				} else if (lDate1 < lDate2) {
					compResult = -1;
				} else {
					compResult = 0;
				}
			} catch (dataNotSupportedException dse2) {
				try {
					compResult = _cfd1.getString().compareTo(_cfd2.getString());
				} catch (dataNotSupportedException e) {
					return 0;
				}
			}

			// else if we can convert both to numbers, try to do a number comparsion
		} else if (_cfd1.isNumberConvertible() && _cfd1.isNumberConvertible()) {

			try {
				double double1 = _cfd1.getDouble();
				double double2 = _cfd2.getDouble();
				if (double1 > double2) {
					compResult = 1;
				} else if (double1 < double2) {
					compResult = -1;
				} else {
					compResult = 0;
				}
			} catch (dataNotSupportedException dse) {
				try {
					compResult = _cfd1.getString().compareTo(_cfd2.getString());
				} catch (dataNotSupportedException dse2) {
					return 0;
				}
			}

		} else {
			try {
				compResult = _cfd1.getString().compareTo(_cfd2.getString());
			} catch (dataNotSupportedException dse2) {
				return 0;
			}
		}

		return compResult;
	}// compare()

	/**
	 * This method designed for use by CFSWITCH/CFCASE. It "normalizes" a cfData
	 * instance to a string as follows:
	 * 
	 * - If the cfData instance is a cfBooleanData, then return "1" or "0". - If
	 * the cfData instance is a cfNumberData, then return the value of
	 * this.getString() - If the cfData instance is a string that can be converted
	 * to a number, then create a cfNumberData instance and return the
	 * cfNumberData.getString() value. - If the cfData instance is a string with
	 * the value of "true/yes" then return "1", as it would for a boolean. - If
	 * the cfData instance is a string with the value of "false/no" then return
	 * "0", as it would for a boolean. - In all other cases, return the value of
	 * this.getString().
	 * 
	 * Most importantly, it does all this without throwing any exceptions.
	 */
	public String toNormalString() throws dataNotSupportedException {
		if (this.getDataType() == CFBOOLEANDATA) {
			return (this.getBoolean() ? "1" : "0");
		}
		String dataString = this.getString().toLowerCase();
		if (this.getDataType() != CFNUMBERDATA) {
			dataString = toNormalString(dataString);
		}
		return dataString;
	}

	/**
	 * dataString is expected to be converted to lowercase before invoking this
	 * method
	 */
	public static String toNormalString(String dataString) throws dataNotSupportedException {
		if (string.isNumber(dataString)) {
			dataString = cfData.createNumber(dataString, true).getString();
		} else if (dataString.equals("true") || dataString.equals("yes")) {
			dataString = "1";
		} else if (dataString.equals("false") || dataString.equals("no")) {
			dataString = "0";
		}
		return dataString;
	}
}
