/*
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 *
 *  $Id: preparedData.java 2505 2015-02-08 21:39:24Z alan $
 *
 *  2013 November: Patched by Matthew Roach for additional data types
 *  2015 February: #527: Stored Proc fails in MS SQL Server when variable named with @ (Dave Siracusa)
 */

package com.naryx.tagfusion.cfm.sql;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class preparedData implements java.io.Serializable {

	static final long						serialVersionUID		= 1;

	public static final int			CF_SQL_BIGINT				= 0;
	public static final int			CF_SQL_BIT					= 1;
	public static final int			CF_SQL_CHAR					= 2;
	public static final int			CF_SQL_BLOB					= 3;
	public static final int			CF_SQL_CLOB					= 4;
	public static final int			CF_SQL_DATE					= 5;
	public static final int			CF_SQL_DECIMAL			= 6;
	public static final int			CF_SQL_DOUBLE				= 7;
	public static final int			CF_SQL_FLOAT				= 8;
	public static final int			CF_SQL_IDSTAMP			= 9;
	public static final int			CF_SQL_INTEGER			= 10;
	public static final int			CF_SQL_LONGVARCHAR	= 11;
	public static final int			CF_SQL_MONEY				= 12;
	public static final int			CF_SQL_MONEY4				= 13;
	public static final int			CF_SQL_NUMERIC			= 14;
	public static final int			CF_SQL_REAL					= 15;
	public static final int			CF_SQL_REFCURSOR		= 16;
	public static final int			CF_SQL_SMALLINT			= 17;
	public static final int			CF_SQL_TIME					= 18;
	public static final int			CF_SQL_TIMESTAMP		= 19;
	public static final int			CF_SQL_TINYINT			= 20;
	public static final int			CF_SQL_VARCHAR			= 21;
	public static final int			CF_SQL_BINARY				= 22;		// BlueDragon-specific type for MS SQL Server
	public static final int			CF_SQL_VARBINARY		= 23;		// BlueDragon-specific type for MS SQL Server
	public static final int			CF_SQL_NCLOB				= 24;		// BlueDragon-specific type for Oracle
	public static final int			CF_SQL_NCHAR				= 25;		// BlueDragon-specific type for Oracle 8
	public static final int			CF_SQL_NVARCHAR			= 26;		// BlueDragon-specific type for Oracle 8

	// proprietary jdbc types
	private static final short	FORM_NCHAR					= 2;		// oracle.jdbc.OraclePreparedStatement.FORM_NCHAR

	public static final int			ORACLE_CURSOR				= -10;	// equivalent to OracleTypes.CURSOR
	public static final int			ORACLE_NCLOB				= 100;	// just picked a value currently not used by java.sql.Types since there isn't an OracleTypes.NCLOB
	public static final int			ORACLE_NCHAR				= 101;	// just picked a value currently not used by java.sql.Types since there isn't an OracleTypes.NCHAR
	public static final int			ORACLE_NVARCHAR			= 102;	// just picked a value currently not used by java.sql.Types since there isn't an OracleTypes.NVARCHAR

	// map cfsqltype to jdbc type
	// NOTE: this array should not be directly used. Instead the method getJdbcType() should be used.
	private static final int[]	jdbcType				= { java.sql.Types.BIGINT, // CF_SQL_BIGINT
																									java.sql.Types.BIT, // CF_SQL_BIT
																									java.sql.Types.CHAR, // CF_SQL_CHAR
																									java.sql.Types.LONGVARBINARY, // CF_SQL_BLOB
																									java.sql.Types.LONGVARCHAR, // CF_SQL_CLOB
																									java.sql.Types.DATE, // CF_SQL_DATE
																									java.sql.Types.DECIMAL, // CF_SQL_DECIMAL
																									java.sql.Types.DOUBLE, // CF_SQL_DOUBLE
																									java.sql.Types.FLOAT, // CF_SQL_FLOAT
																									java.sql.Types.VARCHAR, // CF_SQL_IDSTAMP
																									java.sql.Types.INTEGER, // CF_SQL_INTEGER
																									java.sql.Types.LONGVARCHAR, // CF_SQL_LONGVARCHAR
																									java.sql.Types.DECIMAL, // CF_SQL_MONEY
																									java.sql.Types.DECIMAL, // CF_SQL_MONEY4
																									java.sql.Types.NUMERIC, // CF_SQL_NUMERIC
																									java.sql.Types.REAL, // CF_SQL_REAL
																									ORACLE_CURSOR, // CF_SQL_REFCURSOR
																									java.sql.Types.SMALLINT, // CF_SQL_SMALLINT
																									java.sql.Types.TIME, // CF_SQL_TIME
																									java.sql.Types.TIMESTAMP, // CF_SQL_TIMESTAMP
																									java.sql.Types.TINYINT, // CF_SQL_TINYINT
																									java.sql.Types.VARCHAR, // CF_SQL_VARCHAR
																									java.sql.Types.BINARY, // CF_SQL_BINARY (BlueDragon-specific type)
																									java.sql.Types.VARBINARY, // CF_SQL_VARBINARY (BlueDragon-specific type)
																									ORACLE_NCLOB, // CF_SQL_NCLOB (BlueDragon-specific type)
																									ORACLE_NCHAR, // CF_SQL_NCHAR (BlueDragon-specific type)
																									ORACLE_NVARCHAR };					// CF_SQL_NVARCHAR (BlueDragon-specific type)

	protected List<cfData>			data								= new ArrayList<cfData>(2);	// contains cfData objects
	protected String						sqlType;

	private int									cfSqlType;
	private String							outVariable;

	private int									maxLength						= -1;
	private int									scale								= 0;
	private int									padding							= 0;
	private boolean							passAsNull					= false;
	private String							listSeparator				= ",";

	private boolean							_in									= false;
	private boolean							_out								= false;

	protected String						paramName						= null;
	private boolean							useNamedParameters;



	public preparedData() {
		cfSqlType = CF_SQL_CHAR;
		sqlType = "CF_SQL_VARCHAR";
	}



	public cfData getData(int _index) {
		return (cfData) data.get(_index);
	}



	public int getSize() {
		return data.size();
	}



	public int getPadding() {
		return padding;
	}



	public void setPadding(int p) {
		padding = p;
	}



	public void setUseNamedParameters(boolean b) {
		useNamedParameters = b;
	}



	public void setDataType(String type) throws cfmRunTimeException {
		sqlType = type.toUpperCase().trim();

		if (sqlType == null)
			throw newRunTimeException("CFSQLTYPE can't be null");
		else if (sqlType.equals("CF_SQL_CHAR") || sqlType.equals("CHAR"))
			cfSqlType = CF_SQL_CHAR;
		else if (sqlType.equals("CF_SQL_VARCHAR") || sqlType.equals("CF_SQL_STRING") || sqlType.equals("VARCHAR") || sqlType.equals("STRING"))
			cfSqlType = CF_SQL_VARCHAR;
		else if (sqlType.equals("CF_SQL_TIMESTAMP") || sqlType.equals("TIMESTAMP"))
			cfSqlType = CF_SQL_TIMESTAMP;
		else if (sqlType.equals("CF_SQL_BIGINT") || sqlType.equals("BIGINT"))
			cfSqlType = CF_SQL_BIGINT;
		else if (sqlType.equals("CF_SQL_BIT") || sqlType.equals("BIT"))
			cfSqlType = CF_SQL_BIT;
		else if (sqlType.equals("CF_SQL_BLOB") || sqlType.equals("CF_SQL_IMAGE") || sqlType.equals("BLOB") || sqlType.equals("IMAGE"))
			cfSqlType = CF_SQL_BLOB;
		else if (sqlType.equals("CF_SQL_CLOB") || sqlType.equals("CLOB"))
			cfSqlType = CF_SQL_CLOB;
		else if (sqlType.equals("CF_SQL_DATE") || sqlType.equals("DATE"))
			cfSqlType = CF_SQL_DATE;
		else if (sqlType.equals("CF_SQL_DECIMAL") || sqlType.equals("DECIMAL"))
			cfSqlType = CF_SQL_DECIMAL;
		else if (sqlType.equals("CF_SQL_DOUBLE") || sqlType.equals("DOUBLE"))
			cfSqlType = CF_SQL_DOUBLE;
		else if (sqlType.equals("CF_SQL_FLOAT") || sqlType.equals("FLOAT"))
			cfSqlType = CF_SQL_FLOAT;
		else if (sqlType.equals("CF_SQL_IDSTAMP") || sqlType.equals("IDSTAMP"))
			cfSqlType = CF_SQL_IDSTAMP;
		else if (sqlType.equals("CF_SQL_INTEGER") || sqlType.equals("INTEGER"))
			cfSqlType = CF_SQL_INTEGER;
		else if (sqlType.equals("CF_SQL_LONGVARCHAR") || sqlType.equals("LONGVARCHAR"))
			cfSqlType = CF_SQL_LONGVARCHAR;
		else if (sqlType.equals("CF_SQL_MONEY") || sqlType.equals("MONEY")) {
			cfSqlType = CF_SQL_MONEY;
			setScale(4);
		} else if (sqlType.equals("CF_SQL_MONEY4") || sqlType.equals("MONEY4")) {
			cfSqlType = CF_SQL_MONEY4;
			setScale(4);
		} else if (sqlType.equals("CF_SQL_NUMERIC") || sqlType.equals("NUMERIC"))
			cfSqlType = CF_SQL_NUMERIC;
		else if (sqlType.equals("CF_SQL_REAL") || sqlType.equals("REAL"))
			cfSqlType = CF_SQL_REAL;
		else if (sqlType.equals("CF_SQL_REFCURSOR") || sqlType.equals("REFCURSOR"))
			cfSqlType = CF_SQL_REFCURSOR;
		else if (sqlType.equals("CF_SQL_SMALLINT") || sqlType.equals("SMALLINT"))
			cfSqlType = CF_SQL_SMALLINT;
		else if (sqlType.equals("CF_SQL_TIME") || sqlType.equals("TIME"))
			cfSqlType = CF_SQL_TIME;
		else if (sqlType.equals("CF_SQL_TINYINT") || sqlType.equals("TINYINT"))
			cfSqlType = CF_SQL_TINYINT;
		else if (sqlType.equals("CF_SQL_BINARY") || sqlType.equals("BINARY"))
			cfSqlType = CF_SQL_BINARY;
		else if (sqlType.equals("CF_SQL_VARBINARY") || sqlType.equals("VARBINARY"))
			cfSqlType = CF_SQL_VARBINARY;
		else if (sqlType.equals("CF_SQL_NCLOB") || sqlType.equals("NCLOB"))
			cfSqlType = CF_SQL_NCLOB;
		else if (sqlType.equals("CF_SQL_NCHAR") || sqlType.equals("NCHAR"))
			cfSqlType = CF_SQL_NCHAR;
		else if (sqlType.equals("CF_SQL_NVARCHAR") || sqlType.equals("NVARCHAR"))
			cfSqlType = CF_SQL_NVARCHAR;
		else
			throw newRunTimeException("Invalid CFSQLTYPE: " + sqlType);
	}



	public void setData(cfData _data) {
		data.add(_data);
	}



	public String getDataAsString() throws dataNotSupportedException {
		StringBuilder sb = new StringBuilder();
		Iterator<cfData> iter = data.iterator();
		while (iter.hasNext()) {
			sb.append(iter.next().getString());
			if (iter.hasNext()) {
				sb.append(listSeparator.charAt(0));
			}
		}
		return sb.toString();
	}



	public void setParamName(String name) {
		paramName = name;
	}



	public String getSQLType() {
		return sqlType;
	}



	public int getCfSqlType() {
		return cfSqlType;
	}



	public void setIN() {
		_in = true;
	}



	public void setOUT() {
		_out = true;
	}



	public boolean isIN() {
		return _in;
	}



	public boolean isOUT() {
		return _out;
	}



	public void setOutVariable(String _data) {
		outVariable = _data;
	}



	public String getOutVariable() {
		return outVariable;
	}



	public void setPassAsNull(boolean _null) {
		passAsNull = _null;
	}



	public void setMaxLength(int _maxLength) {
		maxLength = _maxLength;
	}



	public int getMaxLength() {
		return maxLength;
	}


	public String getSeparator() {
		return listSeparator;
	}


	public void setList( String separator, cfData defaultData ) throws dataNotSupportedException {
		// convert data to list if _list is true; only convert once
		listSeparator	= separator;

		String _data = null;

		if ( data.size() == 1 ){
			_data	= data.get(0).getString();
			data.clear();
			if ( _data.length() == 0 && defaultData != null )
				_data	= defaultData.getString();
		} else if ( defaultData != null )
			_data	= defaultData.getString();


		if ( _data != null ){
			List<String> tokens = string.split(_data, listSeparator);
			for (int i = 0; i < tokens.size(); i++)
				data.add(new cfStringData(tokens.get(i).toString()));
		}
	}


	public void setScale(int _scale) {
		if ((cfSqlType == CF_SQL_MONEY) || (cfSqlType == CF_SQL_MONEY4)) {
			scale = 4;
		} else {
			scale = _scale;
		}
	}



	public int getScale() {
		return scale;
	}



	public String toString() {
		return "Type=" + sqlType + "; Data=" + data + "; Direction=" + ((_in ? "IN" : "") + (_out ? "OUT" : "")) + "; Variable=" + outVariable;
	}



	public int hashCode() {
		// --[ This hashcode is overridden to make sure we take into account the elements that could change for a given
		// --[ CFQUERYPARAM value, which this class represents. This is the purpose of CACHE validation purposes.
		return (31 * data.hashCode()) + com.nary.util.string.hashCode(sqlType);
	}



	// ----------------------------------------------------

	public String getQueryString() {
		if (data.size() == 0) // should never happen
			return "";

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.size(); i++) {
			sb.append("?,");
		}
		return sb.toString().substring(0, sb.length() - 1);
	}



	public void validateData(cfSession _Session) throws cfmRunTimeException {
		if (isOUT() || passAsNull)
			return;

		for (int i = 0; i < data.size(); i++) {
			cfData _data = data.get(i);

			switch (cfSqlType) {
				case CF_SQL_VARCHAR:
				case CF_SQL_CHAR:
				case CF_SQL_LONGVARCHAR:
				case CF_SQL_CLOB:
				case CF_SQL_NCLOB:
				case CF_SQL_NCHAR:
				case CF_SQL_NVARCHAR:
					String tmp = _data.getString();
					if ((maxLength != -1) && (tmp.length() > maxLength)) {
						throw newRunTimeException("The value provided is longer than the maxLength");
					}
					break;

				case CF_SQL_SMALLINT:
				case CF_SQL_INTEGER:
				case CF_SQL_TINYINT:
					_data.getInt();
					break;

				case CF_SQL_BLOB:
				case CF_SQL_BINARY:
				case CF_SQL_VARBINARY:
					if (_data.getDataType() != cfData.CFBINARYDATA) {
						throw newRunTimeException("The value provided is not a BINARY");
					}
					break;

				case CF_SQL_BIGINT:
					_data.getLong();
					break;

				case CF_SQL_DOUBLE:
				case CF_SQL_DECIMAL:
				case CF_SQL_FLOAT:
				case CF_SQL_REAL:
				case CF_SQL_NUMERIC:
					_data.getDouble();
					break;

				case CF_SQL_DATE:
				case CF_SQL_TIME:
				case CF_SQL_TIMESTAMP:
					data.set(data.indexOf(_data), _data.getDateData());
					break;

				case CF_SQL_IDSTAMP:
					if (!tagUtils.isGUID(_data)) {
						throw newRunTimeException("The value provided for type CF_SQL_IDSTAMP is not a valid UniqueIdentifier");
					}
					break;

				default:
					break;
			}
		}
	}



	protected cfmRunTimeException newRunTimeException(String ErrorMessage) {
		cfCatchData catchData = new cfCatchData();
		catchData.setDetail(toString());
		catchData.setMessage(ErrorMessage);
		catchData.setType("Database");
		return new cfmRunTimeException(catchData);
	}



	// ----------------------------------------------------

	/*
	 * prepareStatement
	 * Supports named parameters.
	 */
	public int prepareStatement(int ColIndex, CallableStatement CallStatmt, Connection _conn) throws dataNotSupportedException, cfmRunTimeException, SQLException {
		if (useNamedParameters) {
			paramName = paramName.replace("@", "");

			if (isOUT()) {
				int jType = getJdbcType(CallStatmt, cfSqlType);

				if ((jType == java.sql.Types.DECIMAL) || (jType == java.sql.Types.NUMERIC))
					CallStatmt.registerOutParameter(paramName, jType, scale);
				else
					CallStatmt.registerOutParameter(paramName, jType);
			}

			if (isIN())	{
				prepareStatement(paramName, CallStatmt, _conn);
			}

			return ColIndex + 1;
		}

		if (isOUT()) {
			int jType = getJdbcType(CallStatmt, cfSqlType);
			if ((jType == java.sql.Types.DECIMAL) || (jType == java.sql.Types.NUMERIC))
				CallStatmt.registerOutParameter(ColIndex, jType, scale);
			else
				CallStatmt.registerOutParameter(ColIndex, jType);
		}
		return (isIN() ? prepareStatement(ColIndex, (PreparedStatement) CallStatmt, _conn) : ColIndex + 1);
	}



	/*
	 * prepareStatement
	 * This method follows the logic of preparedDataCommon.prepareStatement() except that since it is only
	 * called for CFPROCPARAM's it doesn't need to iterate over the data VectorArrayList. Instead it only
	 * needs to extract the one value.
	 */
	private void prepareStatement(String paramName, CallableStatement CallStatmt, Connection _conn) throws dataNotSupportedException, cfmRunTimeException, SQLException {
		// Map the CFML type to a JDBC type
		int jType = getJdbcType(CallStatmt, cfSqlType);

		paramName = paramName.replace("@", "");

		if (passAsNull) {
			// JDBC drivers don't recognize ORACLE_NCLOB so we need to pass it in as a Types.CHAR
			if (jType == ORACLE_NCLOB)
				CallStatmt.setNull(paramName, Types.CHAR);
			else
				CallStatmt.setNull(paramName, jType);
			return;
		}

		// Get the value associated with this CFPROCPARAM
		cfData _data = data.get(0);

		switch (jType) {
			// for MS SQL Server via JDBC-ODBC Bridge, if you try to use setString()
			// instead of setObject(), it will pad VARCHAR columns when it shouldn't
			case Types.CHAR:
			case Types.VARCHAR:
				CallStatmt.setObject(paramName, _data.getString(), jType);
				break;

			case Types.LONGVARCHAR:
				CallStatmt.setObject(paramName, _data.getString(), jType);
				break;

			case ORACLE_NCLOB:
				CallStatmt.setObject(paramName, _data.getString(), jType);
				break;

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
				CallStatmt.setObject(paramName, ((cfBinaryData) _data).getByteArray(), jType);
				break;

			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
				if (_data.getNumber().isInt()) {
					CallStatmt.setInt(paramName, _data.getInt());
					break;
				}
				// if not an int, fall through to next case

			case Types.BIGINT:
				double d = _data.getDouble();
				if (d <= Long.MAX_VALUE) {
					if (isSetLongSupported(_conn)) {
						CallStatmt.setLong(paramName, _data.getLong());
					} else {
						CallStatmt.setDouble(paramName, d);
					}
				} else {
					CallStatmt.setDouble(paramName, d);
				}
				break;

			case Types.DECIMAL:
			case Types.NUMERIC:
				try {
					// NOTE: if a customer is complaining about losing decimal places then make sure they
					// are setting the scale properly in cfqueryparam. The default value for scale
					// is 0 which causes all decimal places to be removed.
					CallStatmt.setBigDecimal(paramName, new BigDecimal(_data.getDouble()).setScale(scale, BigDecimal.ROUND_HALF_UP));
					break;
				} catch (Exception e) {
					// fall through to next case
				}

			case Types.FLOAT:
			case Types.DOUBLE:
				CallStatmt.setDouble(paramName, _data.getDouble());
				break;

			case Types.REAL:
				CallStatmt.setFloat(paramName, new Float(_data.getDouble()).floatValue());
				break;

			case Types.DATE:
				long date = (_data.getDataType() == cfData.CFDATEDATA ? _data.getLong() : _data.getDateData().getLong());
				try {
					CallStatmt.setDate(paramName, new java.sql.Date(date));
				} catch (SQLException e) { // JDBC-ODBC Bridge doesn't support setDate() for MS SQL Server
					CallStatmt.setString(paramName, com.nary.util.Date.formatDate(date, "dd-MMM-yy"));
				}
				break;

			case Types.TIME:
				long time = (_data.getDataType() == cfData.CFDATEDATA ? _data.getLong() : _data.getDateData().getLong());
				try {
					CallStatmt.setTime(paramName, new java.sql.Time(time));
				} catch (SQLException e) { // JDBC-ODBC Bridge doesn't support setTime() for MS SQL Server
					CallStatmt.setString(paramName, com.nary.util.Date.formatDate(time, "hh:mm aa"));
				}
				break;

			case Types.TIMESTAMP:
				long ts = (_data.getDataType() == cfData.CFDATEDATA ? _data.getLong() : _data.getDateData().getLong());
				CallStatmt.setTimestamp(paramName, new java.sql.Timestamp(ts));
				break;

			case Types.BIT:
				CallStatmt.setBoolean(paramName, _data.getBoolean());
				break;

			case Types.NULL:
				CallStatmt.setNull(paramName, getJdbcType(CallStatmt, cfSqlType));
				break;

			default:
				throw newRunTimeException("Unsupported CFSQLTYPE: " + sqlType);
		}
	}



	public int prepareStatement(int ColIndex, PreparedStatement Statmt, Connection _conn) throws dataNotSupportedException, cfmRunTimeException, SQLException {
		// Map the CFML type to a JDBC type
		int jType = getJdbcType(Statmt, cfSqlType);

		if (passAsNull) {
			// JDBC drivers don't recognize ORACLE_NCLOB, ORACLE_NCHAR or ORACLE_NVARCHAR so
			// we need to pass it in as a Types.CHAR.
			if ((jType == ORACLE_NCLOB) || (jType == ORACLE_NCHAR) || (jType == ORACLE_NVARCHAR))
				setOracleNull(Statmt, ColIndex, jType);
			else
				Statmt.setNull(ColIndex, jType);
			return ColIndex + 1;
		}

		Iterator<cfData> iter = data.iterator();

		while (iter.hasNext()) {
			cfData _data = iter.next();

			switch (jType) {
				// This type should only be used with Oracle 8.
				case ORACLE_NCHAR:
					setOracleNChar(Statmt, ColIndex, _data.getString(), jType);
					break;

				// This type should only be used with Oracle 8.
				case ORACLE_NVARCHAR:
					setOracleNVarChar(Statmt, ColIndex, _data.getString(), jType);
					break;

				// for MS SQL Server via JDBC-ODBC Bridge, if you try to use setString()
				// instead of setObject(), it will pad VARCHAR columns when it shouldn't
				case Types.CHAR:
				case Types.VARCHAR:
//					Debug.println("prepareStatement, Types.VARCHAR, data: " + _data.getString());
					Statmt.setObject(ColIndex, _data.getString(), jType);
					break;

				case Types.LONGVARCHAR:
				case Types.CLOB:
					setLongVarChar(Statmt, ColIndex, _data.getString(), jType);
					break;

				case ORACLE_NCLOB:
					setOracleNClob(Statmt, ColIndex, _data.getString(), jType);
					break;

				case Types.BINARY:
				case Types.VARBINARY:
				case Types.LONGVARBINARY:
					Statmt.setObject(ColIndex, ((cfBinaryData) _data).getByteArray(), jType);
					break;

				case Types.TINYINT:
				case Types.SMALLINT:
				case Types.INTEGER:
					if (_data.getNumber().isInt()) {
						Statmt.setInt(ColIndex, _data.getInt());
						break;
					}
					// if not an int, fall through

				case Types.BIGINT:
					double d = _data.getDouble();
					if (d <= Long.MAX_VALUE) {
						if (isSetLongSupported(_conn)) {
							Statmt.setLong(ColIndex, _data.getLong());
						} else {
							Statmt.setDouble(ColIndex, d);
						}
					} else {
						Statmt.setDouble(ColIndex, d);
					}
					break;

				case Types.DECIMAL:
				case Types.NUMERIC:
					try {
						// NOTE: if a customer is complaining about losing decimal places then make sure they
						// are setting the scale properly in cfqueryparam. The default value for scale
						// is 0 which causes all decimal places to be removed.
						Statmt.setBigDecimal(ColIndex, new BigDecimal(_data.getDouble()).setScale(scale, BigDecimal.ROUND_HALF_UP));
						break;
					} catch (Exception e) {
						// fall through to next case
					}

				case Types.FLOAT:
				case Types.DOUBLE:
					Statmt.setDouble(ColIndex, _data.getDouble());
					break;

				case Types.REAL:
					Statmt.setFloat(ColIndex, new Float(_data.getDouble()).floatValue());
					break;

				case Types.DATE:
					long date = (_data.getDataType() == cfData.CFDATEDATA ? _data.getLong() : _data.getDateData().getLong());
					try {
						Statmt.setDate(ColIndex, new java.sql.Date(date));
					} catch (SQLException e) { // JDBC-ODBC Bridge doesn't support setDate() for MS SQL Server
						Statmt.setString(ColIndex, com.nary.util.Date.formatDate(date, "dd-MMM-yy"));
					}
					break;

				case Types.TIME:
					long time = (_data.getDataType() == cfData.CFDATEDATA ? _data.getLong() : _data.getDateData().getLong());
					try {
						Statmt.setTime(ColIndex, new java.sql.Time(time));
					} catch (SQLException e) { // JDBC-ODBC Bridge doesn't support setTime() for MS SQL Server
						Statmt.setString(ColIndex, com.nary.util.Date.formatDate(time, "hh:mm aa"));
					}
					break;

				case Types.TIMESTAMP:
					long ts = (_data.getDataType() == cfData.CFDATEDATA ? _data.getLong() : _data.getDateData().getLong());
					Statmt.setTimestamp(ColIndex, new java.sql.Timestamp(ts));
					break;

				case Types.BIT:
					Statmt.setBoolean(ColIndex, _data.getBoolean());
					break;

				case Types.NULL:
					Statmt.setNull(ColIndex, getJdbcType(Statmt, cfSqlType));
					break;

				default:
					throw newRunTimeException("Unsupported CFSQLTYPE: " + sqlType);
			}

			ColIndex++;
		}

		return ColIndex;
	}



	private static void setLongVarChar(PreparedStatement Statmt, int ColIndex, String s, int jdbcType) throws SQLException {
		Method m;

		Class<? extends PreparedStatement> c = Statmt.getClass();
		String className = c.getName();
		if (className.equals("oracle.jdbc.driver.T4CPreparedStatement") || className.equals("oracle.jdbc.driver.T4CCallableStatement")) {
			// When updating an Oracle CLOB column using the Oracle JDBC driver we need to use the
			// Oracle proprietary method setStringForClob() so it will work for strings longer than 32765.
			// NORMAL CODE: ((oracle.jdbc.OraclePreparedStatement)Statmt).setStringForClob( ColIndex, s );
			try {
				m = c.getMethod("setStringForClob", new Class[] { int.class, String.class });
				m.invoke(Statmt, new Object[] { new Integer(ColIndex), s });
			} catch (Exception e) {
				// If the reflection code fails for some reason then just call setObject()
				Statmt.setObject(ColIndex, s, jdbcType);
			}
		} else {
			// for MS SQL Server via JDBC-ODBC Bridge, if you try to use setString()
			// instead of setObject(), it will pad VARCHAR columns when it shouldn't
			Statmt.setObject(ColIndex, s, jdbcType);
		}
	}



	private static void setOracleNClob(PreparedStatement Statmt, int ColIndex, String s, int jdbcType) throws SQLException {
		Method m;

		Class<? extends PreparedStatement> c = Statmt.getClass();
		String className = c.getName();
		if (className.equals("oracle.jdbc.driver.T4CPreparedStatement")) {
			// When updating an Oracle NCLOB column using the Oracle JDBC driver we need to use the
			// Oracle proprietary method setFormOfUse() so it will know it's an NCLOB instead of CLOB.
			// NOTE: a string longer than 2000 characters will result in the following error:
			// "ORA-01461: can bind a LONG value only for insert into a LONG column."
			// Using setString, setCharacterStream or setObject instead of setStringForClob
			// results in the same limitation.
			// NORMAL CODE: ((oracle.jdbc.OraclePreparedStatement)Statmt).setFormOfUse( ColIndex, oracle.jdbc.OraclePreparedStatement.FORM_NCHAR );
			// NORMAL CODE: ((oracle.jdbc.OraclePreparedStatement)Statmt).setStringForClob( ColIndex, s );
			try {
				m = c.getMethod("setFormOfUse", new Class[] { int.class, short.class });
				m.invoke(Statmt, new Object[] { new Integer(ColIndex), new Short(FORM_NCHAR) });
				m = c.getMethod("setStringForClob", new Class[] { int.class, String.class });
				m.invoke(Statmt, new Object[] { new Integer(ColIndex), s });
			} catch (Exception e) {
				// If the reflection code fails for some reason then just call setObject()
				Statmt.setObject(ColIndex, s, Types.CLOB);
			}
		} else {
			// Need to pass a jdbc type of Types.CLOB instead of ORACLE_NCLOB since the JDBC drivers
			// won't recognize ORACLE_NCLOB.
			Statmt.setObject(ColIndex, s, Types.CLOB);
		}
	}



	private static void setOracleNChar(PreparedStatement Statmt, int ColIndex, String s, int jdbcType) throws SQLException {
		setFormOfUseToNCHAR(Statmt, ColIndex);
		Statmt.setObject(ColIndex, s, Types.CHAR);
	}



	private static void setOracleNVarChar(PreparedStatement Statmt, int ColIndex, String s, int jdbcType) throws SQLException {
		setFormOfUseToNCHAR(Statmt, ColIndex);
		Statmt.setObject(ColIndex, s, Types.VARCHAR);
	}



	private static void setOracleNull(PreparedStatement Statmt, int ColIndex, int jdbcType) throws SQLException {
		// JDBC drivers don't recognize ORACLE_NCLOB, ORACLE_NCHAR or ORACLE_NVARCHAR so
		// we need to pass it in as a Types.CHAR.
		setFormOfUseToNCHAR(Statmt, ColIndex);
		Statmt.setNull(ColIndex, Types.CHAR);
	}



	private static void setFormOfUseToNCHAR(PreparedStatement Statmt, int ColIndex) {
		Class<? extends PreparedStatement> c = Statmt.getClass();
		String className = c.getName();
		if (className.equals("oracle.jdbc.driver.T4CPreparedStatement")) {
			try {
				Method m = c.getMethod("setFormOfUse", new Class[] { int.class, short.class });
				m.invoke(Statmt, new Object[] { new Integer(ColIndex), new Short(FORM_NCHAR) });
			} catch (Exception e) {
			}
		}
	}



	private static boolean isSetLongSupported(java.sql.Connection _conn) throws SQLException {
		if (_conn.getMetaData().getDatabaseProductName().equalsIgnoreCase("ACCESS") && _conn.getMetaData().getDriverName().startsWith("JDBC-ODBC Bridge")) {
			return false;
		} else {
			return true;
		}
	}



	public void retrieveOutVariables(int ColIndex, cfSession _Session, CallableStatement _stmt) throws SQLException, cfmRunTimeException {
		boolean b;
		byte[] bin;
		int i;
		long l;
		double dbl;
		float flt;
		java.sql.Date dt;
		java.sql.Time t;
		Timestamp ts;
		ResultSet rs;
		String str;
		cfData outData = null;

		if (!isOUT())
			return;

		switch (cfSqlType) {
			case CF_SQL_BIT:
				// With the Oracle JDBC driver, if we set the parameters using named parameters then
				// we must retrieve them using named parameters too.
				if (useNamedParameters) {
					b = _stmt.getBoolean(paramName);
				} else {
					b = _stmt.getBoolean(ColIndex);
				}
				if (!_stmt.wasNull())
					outData = cfBooleanData.getcfBooleanData(b);
				break;

			case CF_SQL_BINARY:
			case CF_SQL_VARBINARY:
			case CF_SQL_BLOB:
				// With the Oracle JDBC driver, if we set the parameters using named parameters then
				// we must retrieve them using named parameters too.
				if (useNamedParameters) {
					bin = _stmt.getBytes(paramName);
				} else {
					bin = _stmt.getBytes(ColIndex);
				}

				if ((!_stmt.wasNull()) && (bin != null)) {
					outData = new cfBinaryData(bin);
				}
				break;

			case CF_SQL_SMALLINT:
			case CF_SQL_INTEGER:
			case CF_SQL_TINYINT:
				try {
					// With the Oracle JDBC driver, if we set the parameters using named parameters then
					// we must retrieve them using named parameters too.
					if (useNamedParameters) {
						i = _stmt.getInt(paramName);
					} else {
						i = _stmt.getInt(ColIndex);
					}

					if (!_stmt.wasNull())
						outData = new cfNumberData(i);
				} catch (NumberFormatException e) {
					// With JDK 1.3 and the JDBC-ODBC bridge, the getInt() method will
					// throw a number format exception for in/out params so just ignore it.
					// Ignoring it allows us to retrieve the out param values.
				}
				break;

			case CF_SQL_BIGINT:
				// With the Oracle JDBC driver, if we set the parameters using named parameters then
				// we must retrieve them using named parameters too.
				if (useNamedParameters) {
					l = _stmt.getLong(paramName);
				} else {
					l = _stmt.getLong(ColIndex);
				}

				if (!_stmt.wasNull())
					outData = new cfNumberData(l);
				break;

			case CF_SQL_DECIMAL:
			case CF_SQL_NUMERIC:
				dbl = getBigDecimalAsDouble(_stmt, useNamedParameters, paramName, ColIndex);
				if (!_stmt.wasNull())
					outData = new cfNumberData(dbl);
				break;

			case CF_SQL_DOUBLE:
			case CF_SQL_FLOAT:
			case CF_SQL_MONEY:
			case CF_SQL_MONEY4:
				// With the Oracle JDBC driver, if we set the parameters using named parameters then
				// we must retrieve them using named parameters too.
				if (useNamedParameters) {
					dbl = _stmt.getDouble(paramName);
				} else {
					dbl = _stmt.getDouble(ColIndex);
				}

				if (!_stmt.wasNull())
					outData = new cfNumberData(dbl);
				break;

			case CF_SQL_REAL:
				// With the Oracle JDBC driver, if we set the parameters using named parameters then
				// we must retrieve them using named parameters too.
				if (useNamedParameters) {
					flt = _stmt.getFloat(paramName);
				} else {
					flt = _stmt.getFloat(ColIndex);
				}

				// For some reason casting a float to a double doesn't return a double
				// that exactly matches the original float so we'll use the less efficient
				// algorithm of converting the float to a string and the string to a double.
				// If for some reason this fails then we'll revert to casting the float to
				// a double.
				if (!_stmt.wasNull()) {
					try {
						dbl = Double.valueOf(Float.toString(flt)).doubleValue();
					} catch (Exception e) {
						dbl = (double) flt;
					}
					outData = new cfNumberData(dbl);
				}
				break;

			case CF_SQL_DATE:
				// With the Oracle JDBC driver, if we set the parameters using named parameters then
				// we must retrieve them using named parameters too.
				if (useNamedParameters) {
					dt = _stmt.getDate(paramName);
				} else {
					dt = _stmt.getDate(ColIndex);
				}

				if ((!_stmt.wasNull()) && (dt != null)) {
					outData = new cfDateData(dt);
				}
				break;

			case CF_SQL_TIME:
				// With the Oracle JDBC driver, if we set the parameters using named parameters then
				// we must retrieve them using named parameters too.
				if (useNamedParameters) {
					t = _stmt.getTime(paramName);
				} else {
					t = _stmt.getTime(ColIndex);
				}

				if ((!_stmt.wasNull()) && (t != null)) {
					outData = new cfDateData(t);
				}
				break;

			case CF_SQL_TIMESTAMP:
				try {
					// With the Oracle JDBC driver, if we set the parameters using named parameters then
					// we must retrieve them using named parameters too.
					if (useNamedParameters) {
						ts = _stmt.getTimestamp(paramName);
					} else {
						ts = _stmt.getTimestamp(ColIndex);
					}

					if ((!_stmt.wasNull()) && (ts != null)) {
						outData = new cfDateData(ts);
					}
				} catch (NullPointerException e) {
					// With JDK 1.3 and the JDBC-ODBC bridge, the getTimestamp() method will
					// throw a null ptr exception when the underlying value is null so just ignore it.
				}
				break;

			case CF_SQL_REFCURSOR:
				// This CF SQL Type is only used with Oracle for result sets returned by a
				// stored procedure.

				// With the Oracle JDBC driver, if we set the parameters using named parameters then
				// we must retrieve them using named parameters too.
				if (useNamedParameters) {
					rs = (ResultSet) _stmt.getObject(paramName);
				} else {
					rs = (ResultSet) _stmt.getObject(ColIndex);
				}

				if ((!_stmt.wasNull()) && (rs != null)) {
					outData = new cfQueryResultData(rs, "Stored Procedure", maxLength);
				}
				break;

			default:
				// With the Oracle JDBC driver, if we set the parameters using named parameters then
				// we must retrieve them using named parameters too.
				if (useNamedParameters) {
					str = _stmt.getString(paramName);
				} else {
					str = _stmt.getString(ColIndex);
				}

				if ((!_stmt.wasNull()) && (str != null)) {
					outData = new cfStringData(str);
				}
				break;
		}

		_Session.setData(outVariable, (outData == null ? cfNullData.NULL : outData));
	}



	private static double getBigDecimalAsDouble(CallableStatement CallStatmt, boolean useNamedParameters, String paramName, int ColIndex) throws SQLException {
		// The PointBase driver will throw an exception if getDouble()
		// is called for these types so we need to call getBigDecimal() instead.
		BigDecimal bd;
		if (useNamedParameters)
			bd = CallStatmt.getBigDecimal(paramName);
		else
			bd = CallStatmt.getBigDecimal(ColIndex);

		if (bd == null)
			return 0;

		return bd.doubleValue();
	}



	private int getJdbcType(PreparedStatement Statmt, int cfSqlType) {
		if (cfSqlType == CF_SQL_CLOB) {
			// For Oracle callable statements we need to map the CF_SQL_CLOB
			// type to the JDBC type java.sql.Types.CLOB.
			Class<? extends PreparedStatement> c = Statmt.getClass();
			String className = c.getName();
			if (className.equals("oracle.jdbc.driver.T4CCallableStatement"))
				return Types.CLOB;
		}

		return jdbcType[cfSqlType];
	}



	public static boolean supportsNamedParameters(Connection _conn) {
		// If the driver supports named parameters then return true
		try {
			DatabaseMetaData dbmd = _conn.getMetaData();
			if (dbmd.supportsNamedParameters())
				return true;
		} catch (Throwable t) {
			// The supportsNamedParameters() method was added in JDBC 3.0 (JDK 1.4) so
			// ignore exceptions that might be thrown by older drivers. For example,
			// with older JTurbo drivers the above call causes a java.lang.AbstractMethodError.
		}

		// Must not support named parameters so return false
		return false;
	}


}
