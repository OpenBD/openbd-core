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

package com.naryx.tagfusion.cfm;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.naryx.tagfusion.servlet.BlueDragonServletInterface;

/**
 * This class employ the HttpServlet for service client's request, it take in the client's request and 
 * proces it, then send response to the client.
 */
 
public class cfServlet extends HttpServlet implements BlueDragonServletInterface {
	private static final long serialVersionUID = 1L;

	/**
   * Initailise the servlet by calling cfEngine init() method
   *
   * @param config - the object that pass configuration information to cfServlet when it is first loaded
   */
  public synchronized void init(ServletConfig config) throws ServletException{
		super.init( config );
    com.naryx.tagfusion.cfm.engine.cfEngine.init( config );
  }


  /**
   * Called when the servlet is being taken out of action
   *
   * @param config - the object that pass configuration information to cfServlet when it is first loaded
   */
	public void destroy(){
		com.naryx.tagfusion.cfm.engine.cfEngine.destroy();
	}
  
  /**
   * Employ cfEngine service(...) method to service client's request
   */
  public void service( HttpServletRequest req, HttpServletResponse res) throws ServletException {
    com.naryx.tagfusion.cfm.engine.cfEngine.service( req, res );
  }

	
	/**
	 * This for the debugger section
	 */
	public com.naryx.tagfusion.cfm.engine.cfSession createSession( HttpServletRequest req, HttpServletResponse res) {
		return new com.naryx.tagfusion.cfm.engine.cfSession( req, res, getServletContext() );	
	}
	
	public void debugService( com.naryx.tagfusion.cfm.engine.cfSession session ) throws ServletException {
    com.naryx.tagfusion.cfm.engine.cfEngine.service( session.REQ, session.RES );
  }
}
