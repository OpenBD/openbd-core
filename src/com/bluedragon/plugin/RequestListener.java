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

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * The RequestListener is a special class that lets you peek into the life 
 * cycle of a single request.
 * 
 * Be careful when providing an implementation for this class, as this can 
 * seriously effect the performance of the engine.
 * 
 */
public interface RequestListener {
	
	/**
	 * This is called when a request has just started.  This method may not
	 * be called if it is an invalid request.
	 * 
	 * @param session
	 */
	public void requestStart( cfSession session );
	
	/**
	 * This is called at the end of *every* request.   All output has already
	 * been sent to the client.
	 * 
	 * @param session
	 */
	public void requestEnd( cfSession session );

	
	/**
	 * This is called when the request file was not found.  Note the requestEnd()
	 * will still be called after this method.
	 * 
	 * @param bfException
	 * @param session
	 */
	public void requestBadFileException( cfmBadFileException bfException, cfSession session );
	
	
	/**
	 * This is called when an exception has been thrown.   Note the requestEnd()
	 * will still be called after this method.
	 *  
	 * @param cfException
	 * @param session
	 */
	public void requestRuntimeException( cfmRunTimeException cfException, cfSession session );
}
