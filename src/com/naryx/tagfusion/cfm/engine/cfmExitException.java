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

/***
 *	From the CFMX docs for CFEXIT: "If this tag is encountered outside the context of a
 *	custom tag, for example in the base page or an included page, it executes in the same
 *	way as cfabort." There are three known exceptions to this: when CFEXIT is rendered
 *	within Application.cfm, a template included via CFINCLUDE, and when rendering (executing)
 *	a CFFUNCTION.
 *
 *	Therefore, the only places that should ever have to catch cfmExitException are: (1) in 
 *	cfEngine when processing Application.cfm; (2) in cfINCLUDE; and, (3) in cfFUNCTION. Since
 *	cfmExitException is a subclass of cfmAbortException, in all other cases it will be
 *	treated just like an abort.
 *
 *	Note that cfmExitException is *not* thrown when CFEXIT is rendered within a custom tag.
 */
public class cfmExitException extends cfmAbortException {

	private static final long serialVersionUID = 1;

	public cfmExitException() {
	}
}
