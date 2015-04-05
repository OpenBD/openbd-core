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

package com.bluedragon.plugin;

import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;

/**
 * This represents a single CFC object, allowing you to perform operations on it.
 * 
 * It is not create directly but instead use the PluginManagerInterface.createCFC(..) methods.
 *
 */
public interface ObjectCFC {
	
	/**
	 * Returns the underlying reference to the CFC
	 * 
	 * @return cfComponentData
	 */
	public cfComponentData	getComponentCFC();
	
	/**
	 * If the method you wish to call requires parameters then you setup them up
	 * using any of the addArgument(..) calls.
	 * 
	 * @param name		cfc argument name 
	 * @param argVaue	value for this argument
	 */
	public void addArgument(String name, String argVaue);

	/**
	 * If the method you wish to call requires parameters then you setup them up
	 * using any of the addArgument(..) calls.  This method takes an array of
	 * strings and converts them into a CFML Array object
	 * 
	 * @param name		cfc argument name 
	 * @param argVaue	value for this argument
	 */
	public void addArgument(String name, String argVaue[]);
	
	/**
	 * If the method you wish to call requires parameters then you setup them up
	 * using any of the addArgument(..) calls.
	 * 
	 * @param name		cfc argument name 
	 * @param argVaue	value for this argument
	 */
	public void addArgument(String name, boolean argVaue);
	
	/**
	 * If the method you wish to call requires parameters then you setup them up
	 * using any of the addArgument(..) calls.
	 * 
	 * @param name		cfc argument name 
	 * @param argVaue	value for this argument
	 */
	public void addArgument(String name, int argVaue);
	
	/**
	 * If the method you wish to call requires parameters then you setup them up
	 * using any of the addArgument(..) calls.
	 * 
	 * @param name		cfc argument name 
	 * @param argVaue	value for this argument
	 */
	public void addArgument(String name, cfData argVaue);

	/**
	 * Clears down the current arguments
	 */
	public void clearArguments();
	

	/**
	 * Given the current arguments, will invoke the 'methodName' on this CFC based
	 * on the current Session.
	 * 
	 * @param Session
	 * @param methodName
	 * @return the raw cfData object this CFC returned
	 * @throws Exception
	 */
	public cfData runMethod( cfSession Session, String methodName ) throws Exception;

	
	/**
	 * Given the current arguments, will invoke the 'methodName' on this CFC based
	 * on the current Session.
	 * 
	 * @param Session
	 * @param methodName
	 * @return the value the CFC returned as a string
	 * @throws Exception
	 */
	public String runMethodReturnString( cfSession Session, String methodName ) throws Exception;

	
	/**
	 * Given the current arguments, will invoke the 'methodName' on this CFC based
	 * on the current Session.
	 * 
	 * @param Session
	 * @param methodName
	 * @return the value the CFC returned as a boolean
	 * @throws Exception
	 */
	public boolean runMethodReturnBoolean( cfSession Session, String methodName ) throws Exception;
	
}
