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
 * Created on Jan 3, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.naryx.tagfusion.cfm.xml.ws.dynws;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.xml.ws.dynws.convert.TypeConverter;

public class ParameterConverter {
	private ClassLoader localCl = null;

	public ParameterConverter() {}

	/**
	 * Converts the specified val to a web service object. Returns the web service
	 * friendly instance.
	 * 
	 * @param val
	 *          Object to convert.
	 * @param typeHint
	 *          Type to which to convert to the specified object.
	 * @return Converted object.
	 */
	public Object toWebServiceType(Object val, Class typeHint) throws cfmRunTimeException {
		return TypeConverter.toWebServiceType(val, typeHint, this.localCl);
	}

	/**
	 * Converts the specified val into a BD object. Returns the converted object
	 * instance.
	 * 
	 * @param val
	 *          Object to convert.
	 * @param cfsession
	 *          cfSession used to create objects.
	 * @return Converted object.
	 */
	public Object toBDType(Object val, cfSession cfsession) throws cfmRunTimeException {
		return TypeConverter.toBDType(val, cfsession);
	}

	/**
	 * Registers the specified ClassLoader as the preferred class ClassLoader.
	 * This ClassLoader will be used to find and create classes that match known
	 * complex types.
	 * 
	 * @param cl
	 *          ClassLoader containing the preferred classes.
	 */
	public void registerLocalClassLoader(ClassLoader cl) {
		this.localCl = cl;
	}

}
