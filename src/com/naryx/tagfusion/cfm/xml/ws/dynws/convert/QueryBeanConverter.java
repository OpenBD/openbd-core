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

/*
 * Created on Dec 29, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.naryx.tagfusion.cfm.xml.ws.dynws.convert;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.ws.dynws.DynamicCacheClassLoader;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.IQueryBean;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.QueryBean;

/**
 * Converts query data types to/from their BD equivalents.
 */
public class QueryBeanConverter {

	public QueryBeanConverter() {}

	/**
	 * Converts the Object in the ObjectWrapper into the Class specified by
	 * typeHint (or something suitable for SOAP serialization if typeHint is not
	 * specified). Returns true if the Object was successfully converted/replaced,
	 * false otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the Object to convert/replace.
	 * @param typeHint
	 *          Class into which we need to convert the Object.
	 * @param cl
	 *          ClassLoader that may contain the specified typeHint.
	 * @return true if the Object was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	public static boolean toWebServiceType(ObjectWrapper wrapper, Class typeHint, ClassLoader cl) throws cfmRunTimeException {
		// Must be a cfQueryResultData
		if (!(wrapper.value instanceof cfQueryResultData))
			return false;

		// Default type is the ClassLoader's IQueryBean implementation or our
		// QueryBean
		// if no such implementation exists (even if System.Object is specified)
		if (typeHint == null || typeHint.equals(Object.class))
			typeHint = getIQueryBeanType(cl);

		if (IQueryBean.class.isAssignableFrom(typeHint)) {
			// Convert it into a IQueryBean
			return cfQueryResultDataToIQueryBean(wrapper, typeHint, cl);
		} else {
			// Don't know what this is.
			return false;
		}
	}

	/**
	 * Converts the cfQueryResultData in the ObjectWrapper into an IQueryBean
	 * implementation. Returns true if the cfQueryResultData was successfully
	 * converted/replaced, false otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the cfQueryResultData to convert/replace.
	 * @param typeHint
	 *          Class into which we need to convert the Object.
	 * @param cl
	 *          ClassLoader that may contain the specified typeHint.
	 * @return true if the cfQueryResultData was successfully converted/replaced,
	 *         false otherwise.
	 * @throws cfmRunTimeException
	 */
	private static boolean cfQueryResultDataToIQueryBean(ObjectWrapper wrapper, Class typeHint, ClassLoader cl) throws cfmRunTimeException {
		// Cast to a cfQueryResultData object
		cfQueryResultData query = (cfQueryResultData) wrapper.value;

		IQueryBean rtn = null;
		try {
			// Create the type
			rtn = (IQueryBean) typeHint.newInstance();

			// Populate the data field
			Object[][] dataArr = new Object[query.getNoRows()][];
			for (int i = 0; i < query.getNoRows(); i++) {
				Object[] dataSubArr = new Object[query.getNoColumns()];
				for (int j = 0; j < query.getNoColumns(); j++)
					dataSubArr[j] = TypeConverter.toWebServiceType(query.getCell(i + 1, j + 1), null, cl);
				dataArr[i] = dataSubArr;
			}

			// Populate the IQueryBean
			rtn.setColumnList(query.getColumnList());
			rtn.setData(dataArr);
			wrapper.value = rtn;
			return true;
		} catch (cfmRunTimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new cfmRunTimeException(catchDataFactory.generalException("errorCode.runtimeError", "Cannot convert type: " + TypeConverter.getTypeDesc(wrapper.value.getClass()) + ": " + Utils.getExceptionMessage(ex)));
		}
	}

	/**
	 * Returns the type for an IQueryBean. If a locally generated type exists,
	 * this will return that type. If not, this will return the default QueryBean
	 * type.
	 * 
	 * @param cl
	 *          ClassLoader that may contain the preferred IQueryBean
	 *          implementation type
	 * @return preferred IQueryBean implementation type
	 */
	private static Class getIQueryBeanType(ClassLoader cl) {
		if (cl != null && cl instanceof DynamicCacheClassLoader) {
			// Use the ClassLoader's specified impl
			DynamicCacheClassLoader dcl = (DynamicCacheClassLoader) cl;
			Class rtn = dcl.getIQueryBean();
			if (rtn != null)
				return rtn;
		}
		// Otherwise return the default
		return QueryBean.class;
	}

	/**
	 * Converts the Object in the ObjectWrapper into a cfData. Returns true if the
	 * Object was successfully converted/replaced, false otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the Object to convert/replace.
	 * @param session
	 *          cfSession class, needed for object creation
	 * @return true if the Object was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	public static boolean toBDType(ObjectWrapper wrapper, cfSession session) throws cfmRunTimeException {
		if (wrapper.value instanceof IQueryBean)
			return iQueryBeanTocfQueryResultData(wrapper, session);
		else
			return false;
	}

	/**
	 * Converts the IQueryBean in the ObjectWrapper into a cfQueryResultData.
	 * Returns true if the IQueryBean was successfully converted/replaced, false
	 * otherwise.
	 * 
	 * @param wrapper
	 *          ObjectWrapper containing the IQueryBean to convert/replace.
	 * @param session
	 *          cfSession class, needed for object creation
	 * @return true if the IQueryBean was successfully converted/replaced, false
	 *         otherwise.
	 * @throws cfmRunTimeException
	 */
	private static boolean iQueryBeanTocfQueryResultData(ObjectWrapper wrapper, cfSession session) throws cfmRunTimeException {
		IQueryBean qb = (IQueryBean) wrapper.value;
		String[] columnList = qb.getColumnList();
		Object[] data = qb.getData();

		cfQueryResultData rtn = new cfQueryResultData(columnList, "QueryNew()");
		// Populate tablerows
		// Get the row num by getting the num of rows in the data set
		int noCols = columnList.length;
		int noRows = data.length;
		rtn.addRow(noRows);
		for (int i = 0; i < noRows; i++) {
			Object[] dataSub = (Object[]) data[i];
			for (int j = 0; j < noCols; j++)
				rtn.setCell(i + 1, j + 1, TypeConverter.toBDType(dataSub[j], session));
		}

		wrapper.value = rtn;
		return true;
	}
}
