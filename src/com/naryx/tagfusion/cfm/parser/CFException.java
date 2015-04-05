/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: CFException.java 2326 2013-02-09 19:30:40Z alan $
 */
package com.naryx.tagfusion.cfm.parser;

import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class CFException extends cfmRunTimeException implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public int line;
	public int col;

	public CFException(String mess, CFContext context) {
		super();

		line = context.getLine();
		col = context.getCol();

		catchData.setMessage( mess );
		catchData.setDetail( "Error at line " + line + ", column " + col );
		catchData.setData(cfCatchData.LINE_KEY, new cfNumberData(line));
		catchData.setData(cfCatchData.COLUMN_KEY, new cfNumberData(col));
		catchData.setSession( context.getSession() );
	}

	public String toString() {
		return "[line " + String.valueOf( line ) + ", column " + String.valueOf( col ) + "] " + super.toString();
	}
}