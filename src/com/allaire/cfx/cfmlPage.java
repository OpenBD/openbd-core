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

package com.allaire.cfx;

/**
 * This is the interface that will facilitate the communication between a java object
 * and a CFML page.
 */

public interface cfmlPage {

	/*
	 * This method is called as soon as the constructor of the object has completed.
	 * It is advised the implementing class, takes a reference of this cfmlPageContext
	 * to allow them to set CFML variables.
	 */
	public void cfmlPageStarted(cfmlPageContext thisCfmlPageContext);
	
	
	
	/*
	 * This method is called as soon as the page is about to go offline.  All the data
	 * has been sent to the client.  All the session variables will still exist, but
	 * will be cleared as soon as this method returns.  Therefore, there is an opportunity
	 * to retrieve variables from the Session.
	 */
	public void cfmlPageFinished(cfmlPageContext thisCfmlPageContext);
}
