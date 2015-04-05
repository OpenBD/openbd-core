/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  
 *  $Id: cfComponentDataScriptableObject.java 1747 2011-10-25 15:46:01Z alan $
 */
package org.alanwilliamson.lang.javascript;

import java.util.ArrayList;
import java.util.List;

import org.m0zilla.javascript.NativeArray;
import org.m0zilla.javascript.Scriptable;

import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfcMethodData;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class cfComponentDataScriptableObject extends Object {

	private cfComponentData cfdata;
	
	public cfComponentDataScriptableObject( cfComponentData data ){
		super();
		this.cfdata	= data;
	}
	
	public Object __noSuchMethod__( String function, NativeArray nativeArray ) throws Exception {
		if ( cfdata.containsKey(function) ){
			cfSession	session	= (cfSession)org.m0zilla.javascript.Context.getCurrentContext().getThreadLocal("cfsession");
			
			List<cfData> parameters	= new ArrayList<cfData>();
			for ( int x=0; x < nativeArray.getLength(); x++ ){
				parameters.add( tagUtils.convertToCfData( nativeArray.get(x, null) ) );
			}
			
			cfcMethodData invocationData = new cfcMethodData( session, function, parameters );
			cfData resultData = cfdata.invokeComponentFunction(session, invocationData );
			
			return convert.cfDataConvert( resultData );
			
		}else
			throw new Exception( "function: " + function + "; does not exist" );
	}
	
	
	public String getClassName() {
		return "cfComponentData";
	}
	
	public java.lang.Object get(String name, Scriptable start){
		try {
			return convert.cfDataConvert( cfdata.getData(name) );
		} catch (Exception e) {
			return null;
		}
	}
	
	public void put(String name, Scriptable start, Object value) {
		cfdata.setData( name, tagUtils.convertToCfData( value ) );
	}
}