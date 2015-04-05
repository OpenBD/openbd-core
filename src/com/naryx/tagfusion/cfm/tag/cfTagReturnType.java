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

import com.naryx.tagfusion.cfm.engine.cfData;

/***
 * This class represents a return type from the cfTag.render() method.
 */
public class cfTagReturnType {
	
	private static final int TYPE_NORMAL = 0;
	private static final int TYPE_BREAK = 1;
	private static final int TYPE_CONTINUE = 2;
	private static final int TYPE_EXIT = 3;
	private static final int TYPE_RETURN = 4;
	
	public static final cfTagReturnType NORMAL = new cfTagReturnType( TYPE_NORMAL );
	public static final cfTagReturnType BREAK = new cfTagReturnType( TYPE_BREAK );
	public static final cfTagReturnType CONTINUE = new cfTagReturnType( TYPE_CONTINUE );
	
	private int returnType;
	
	// the following attributes are for TYPE_EXIT
	private String method;
	private String output;
	
	public String getMethod(){ return method; }
	public String getOutput(){ return output; }
	
	public void setOutput( String _output ) {
		output = _output;
	}
	
	// factory method for creating TYPE_EXIT
	public static cfTagReturnType newExit( String _method, String _output ) {
		cfTagReturnType rt = new cfTagReturnType( TYPE_EXIT );
	    if ( _method != null ) {
	        rt.method = _method.toUpperCase();
	    }
	    rt.output = _output;
	    return rt;
	}
	
	// factory method for creating new TYPE_NORMAL with output
	public static cfTagReturnType newNormal( String _output ) {
		cfTagReturnType rt = new cfTagReturnType( TYPE_NORMAL );
		rt.output = _output;
		return rt;
	}
	
	// the following attribute is for TYPE_RETURN
	private cfData returnVal;
	
	public cfData getReturnValue() { return returnVal; }
	
	// factory method for TYPE_RETURN
	public static cfTagReturnType newReturn( cfData _return ) {
		cfTagReturnType rt = new cfTagReturnType( TYPE_RETURN );
	    rt.returnVal = _return;
	    return rt;
	}
	
	public boolean isNormal() {
		return ( returnType == TYPE_NORMAL );
	}
	
	public boolean isExit() {
		return ( returnType == TYPE_EXIT );
	}
	
	public boolean isBreak() {
		return ( returnType == TYPE_BREAK );
	}
	
	public boolean isContinue() {
		return ( returnType == TYPE_CONTINUE );
	}
	
	public boolean isReturn() {
		return ( returnType == TYPE_RETURN );
	}
	
	public boolean isExitOrReturn() {
		return ( ( returnType == TYPE_EXIT ) || ( returnType == TYPE_RETURN ) );
	}

	private cfTagReturnType( int type ) {
		this.returnType = type;
	}

}
