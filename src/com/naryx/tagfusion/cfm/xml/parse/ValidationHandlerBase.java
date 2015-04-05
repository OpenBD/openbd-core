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

package com.naryx.tagfusion.cfm.xml.parse;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * Base class for handling parse/validation errors/warnings.
 * Provides facilities to collect error/warning messages in
 * a cfStructData instance.
 * 
 * @author Matt Jacobsen
 *
 */
public class ValidationHandlerBase
{
	protected cfStructData msgStruct = null;
	
	/**
	 * Default constructor.
	 *
	 */
	public ValidationHandlerBase()
	{
	}
	
	/**
	 * Alternate constructor. Takes a cfStructData instance that
	 * will collect the warning, error, and fatal error messages.
	 *
	 * @param msgStruct cfStructData to add all parse/validation
	 * 			messages to 
	 */
	public ValidationHandlerBase(cfStructData msgStruct)
	{
		this.msgStruct = msgStruct;
	}
	
	/**
	 * Handle warning messages.
	 * 
	 * @param msg warning message to record
	 * @return true if the messages was handled/recorded, false otherwise
	 */
	public boolean recordWarning(String msg, int lineNumber, int columnNumber)
	{
		if (msgStruct != null)
		{
			try
			{
				// Format is:
				// [Warning] :line:column: messsage
				msgStruct.setData("Status", cfBooleanData.FALSE);
				cfArrayData arr = (cfArrayData)msgStruct.getData("Warning");			
				arr.addElement(new cfStringData("[Warning] :" + lineNumber + ":" + columnNumber + ": " + msg));
				return true;
			}
			catch (cfmRunTimeException ex)
			{
				// Should encounter this, but ...
				com.nary.Debug.printStackTrace(ex);
			}
		}
		return false;
	}
	
	/**
	 * Handle error messages.
	 * 
	 * @param msg error message to record
	 * @return true if the messages was handled/recorded, false otherwise
	 */
	public boolean recordError(String msg, int lineNumber, int columnNumber)
	{
		if (msgStruct != null)
		{
			try
			{
				// Format is:
				// [Error] :line:column: messsage
				msgStruct.setData("Status", cfBooleanData.FALSE);
				cfArrayData arr = (cfArrayData)msgStruct.getData("Errors");			
				arr.addElement(new cfStringData("[Error] :" + lineNumber + ":" + columnNumber + ": " + msg));
				return true;
			}
			catch (cfmRunTimeException ex)
			{
				// Should encounter this, but ...
				com.nary.Debug.printStackTrace(ex);
			}
		}
		return false;
	}

	/**
	 * Handle fatal error messages.
	 * 
	 * @param msg error message to record
	 * @return true if the messages was handled/recorded, false otherwise
	 */
	public boolean recordFatalError(String msg, int lineNumber, int columnNumber)
	{
		if (msgStruct != null)
		{
			try
			{
				// Format is:
				// [FatalError] :line:column: messsage
				msgStruct.setData("Status", cfBooleanData.FALSE);
				cfArrayData arr = (cfArrayData)msgStruct.getData("FatalErrors");			
				arr.addElement(new cfStringData("[FatalError] :" + lineNumber + ":" + columnNumber + ": " + msg));
				return true;
			}
			catch (cfmRunTimeException ex)
			{
				// Should encounter this, but ...
				com.nary.Debug.printStackTrace(ex);
			}
		}
		return false;
	}
}
