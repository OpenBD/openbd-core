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

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.runTime;

public class cfSET extends cfTag implements Serializable
{
	static final long serialVersionUID = 1;

	protected CFExpression tagExpression = null;
	
	public java.util.Map getInfo(){
		return createInfo("control", "Used to declare, initialise or reassign a variable to a value.");
	}
  
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo("yourVariableName", "The name for a new variable that will be assigned the specified value, or the name of an existing variable that will be reassigned the specified value.", "", true ),
				createAttInfo("VAR", "This attribute does not take a value but by specifying VAR before the variable name will indicate this is to be a local variable, otherwise the variable is of global scope.", "", false )
		};

	}
	
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		// strip the 'cfset' from the tag <cfset ...> leaving only the body i.e. the expression
		setTagExpression( _tag, "cfset" );
	}

	protected void setTagExpression( String _tag, String tagname ) throws cfmBadFileException {
		int c1 = _tag.toLowerCase().indexOf( tagname );
		if ( c1 != -1 ) {
			String expString = _tag.substring( c1+tagname.length(), _tag.length()-1 ).trim();
			if ( expString.length() > 0 ) {
				tagExpression = CFExpression.getCFExpression( expString );
			}
		}
	}


	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		// evaluate the expression	
		runTime.runExpression( _Session, tagExpression );
		return cfTagReturnType.NORMAL;
	}
}
