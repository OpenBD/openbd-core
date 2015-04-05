/* 
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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
package org.alanwilliamson.lang.javascript;

import java.util.HashMap;

import org.m0zilla.javascript.Context;
import org.m0zilla.javascript.Script;

import com.naryx.tagfusion.cfm.tag.cfTag;

public class ScriptFactory extends Object {

	private	static HashMap<String, ScriptWrapper>	map	=	new HashMap<String, ScriptWrapper>(); 
	
	public static Script	getScript( String file, cfTag tag ){
		ScriptWrapper	sw	= map.get( file );
		if ( sw == null || sw.hashcode != tag.hashCode() ){
			sw = new ScriptWrapper();
			sw.hashcode		= tag.hashCode();
			
			Context cx = Context.enter();
			try{
				cx.setOptimizationLevel( -1 );
				sw.script	= cx.compileString( new String(tag.getStaticBody()), "javascript@" + file, 0, null );
			}finally{
				Context.exit();
			}
			map.put( file, sw );
		}
		
		return sw.script;
	}
	
}
