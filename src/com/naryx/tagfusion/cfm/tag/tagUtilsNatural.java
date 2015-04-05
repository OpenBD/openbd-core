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

package com.naryx.tagfusion.cfm.tag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfFixedArrayData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFUndefinedValue;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.cfm.xml.ws.encoding.ser.QueryBean;

public final class tagUtilsNatural extends Object {

	/**
	 * If "deep" is set to true, objects contained within cfStructData and
	 * cfArrayData are also converted to Java natural objects. If "forceDouble" 
	 * is set to true, all numeric cfData objects are forced into Double objects
	 * irrespective of their significant digits (or lack thereof). If 
	 * "preferObjectArray" is true cfArrayData types are converted to Object[] 
	 * instead of a VectorArrayList.
	 */
	public static Object getNatural( cfData _cfdata, boolean deep, boolean forceDouble, boolean preferObjectArray )
	{
		if ( ( _cfdata == null ) || ( _cfdata == CFUndefinedValue.UNDEFINED ) ) {
			return null;
		}
			
		try {
			int cfDataType = _cfdata.getDataType();
			switch ( cfDataType ) {
				case cfData.UNKNOWN:				// can't currently be shared by J2EE
					return _cfdata;
				case cfData.CFNUMBERDATA:
					cfNumberData number = (cfNumberData) _cfdata;
					if ( number.isJavaNumeric() ){
						return number.getInstance();
					}else if ( number.isInt() && !forceDouble){
						return new Integer( number.getInt() );
					}else{
						return new Double( number.getDouble() );
					}
				case cfData.CFBOOLEANDATA:
					return new Boolean( _cfdata.getBoolean() );
				case cfData.CFSTRINGDATA:
					return _cfdata.getString();
				case cfData.CFDATEDATA:
					return new java.util.Date( ( (cfDateData) _cfdata ).getLong() );
				case cfData.CFNULLDATA:
					if ( ((cfNullData)_cfdata).isDBNull() ) {
						return "";
					}
					return null;
				case cfData.CFSTRUCTDATA:      		// implements java.util.Map
					if ( _cfdata instanceof cfXmlData ) {	// XML objects can't be converted
						return _cfdata;
					}
					if ( deep ) {
						return getNaturalMap( (cfStructData)_cfdata, deep, forceDouble, preferObjectArray );		
					} else { 
						return _cfdata;
					}
				case cfData.CFARRAYDATA:			// implements java.util.List
          if ( _cfdata instanceof cfFixedArrayData ){
            return ( (cfFixedArrayData) _cfdata ).getArray();
          }
          
					if ( deep ) 
						return getNaturalList( (cfArrayData)_cfdata, deep, forceDouble, preferObjectArray );
					else{
            return _cfdata;
          }
				case cfData.CFQUERYRESULTDATA: 		// implements java.sql.ResultSet
					if ( deep )
						return getNaturalQueryBean( (cfQueryResultData)_cfdata, deep, forceDouble, preferObjectArray );
					else
						return _cfdata;
				case cfData.CFCOMPONENTOBJECTDATA:	// can't currently be shared by J2EE
					return _cfdata;
				case cfData.CFWSOBJECTDATA:			// can't currently be shared by J2EE
					return _cfdata;
				case cfData.CFJAVAOBJECTDATA:
					try {
						return ((cfJavaObjectData)_cfdata).getInstance();
					} catch ( cfmRunTimeException rte ) {
						return _cfdata;
					}
				case cfData.CFBINARYDATA:
					return ((cfBinaryData)_cfdata).getByteArray();
				case cfData.CFLDATA:				// can't currently be shared by J2EE
					return _cfdata;
				case cfData.CFUDFDATA:				// can't currently be shared by J2EE
					return _cfdata;
				case cfData.OTHER:					// can't currently be shared by J2EE
					return _cfdata;
				default:
					throw new IllegalArgumentException( "A cfData type of " + cfDataType + " is not supported" );
			}
		}
		catch( IllegalArgumentException iae ) {
			throw iae;
		}
		catch( Exception e ) {
			return null;
		}
		
	}// getNatural()
	
	public static Map<String, Object> getNaturalMap(cfStructData struct, boolean deep, 
		boolean forceDouble, boolean preferObjectArray)
	{
		Map<String, Object> rtn = new FastMap<String, Object>(struct.size());
		Iterator<String> itr = struct.keySet().iterator();
		String key  = null;
		while (itr.hasNext())
		{
			key = itr.next();
			rtn.put(key, getNatural(struct.getData(key.toString()), deep, forceDouble, preferObjectArray));
		}
		return rtn;
	}
	
	private static Object getNaturalList(cfArrayData array, boolean deep, 
		boolean forceDouble, boolean preferObjectArray)
	{
		Object next = null;

		if (preferObjectArray)
		{
			Object[] rtn = new Object[array.size()];
			for (int i=1; i <= array.size(); i++)
			{
				next = array.getElement(i);
				if (cfData.class.isAssignableFrom(next.getClass()))
					rtn[i] = getNatural((cfData)next, deep, forceDouble, preferObjectArray);
				else
					rtn[i] = next;
			}
			return rtn;
		}		
		else
		{
			List<Object> rtn = new ArrayList<Object>(array.size());
			for (int i=1; i <= array.size(); i++)
			{
				next = array.getElement(i);
				if (cfData.class.isAssignableFrom(next.getClass()))
					rtn.add(getNatural((cfData)next, deep, forceDouble, preferObjectArray));
				else
					rtn.add(next);
			}
			return rtn;
		}
	}
	
	private static QueryBean getNaturalQueryBean(cfQueryResultData query, boolean deep, 
		boolean forceDouble, boolean preferObjectArray)
	{
		int noCols = query.getNoColumns();
		int noRows = query.getNoRows();
		Object[][] data = new Object[noRows][];
		for (int i=1; i<=noRows; i++)
		{
			data[i-1] = new Object[noCols];
			for (int j=1; j<=noCols; j++)
				data[i-1][j-1] = getNatural(query.getCell(i, j), deep, forceDouble, preferObjectArray);
		}

		return new QueryBean(query.getColumnList(), data);
	}	
}

