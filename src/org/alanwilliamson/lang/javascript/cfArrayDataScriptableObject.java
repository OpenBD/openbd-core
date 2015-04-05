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
 *  $Id: cfArrayDataScriptableObject.java 2352 2013-04-01 08:19:50Z alan $
 */
package org.alanwilliamson.lang.javascript;

import org.m0zilla.javascript.Scriptable;
import org.m0zilla.javascript.ScriptableObject;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class cfArrayDataScriptableObject extends ScriptableObject {
	private static final long serialVersionUID = 1L;

	private cfArrayData cfdata;
	
	public cfArrayDataScriptableObject( cfArrayData data ){
		super();
		this.cfdata	= data;
	}

	public int length(){
		return this.cfdata.size();
	}
	
	public cfData getCFData(){
		return this.cfdata;
	}

	@Override
	public String getClassName() {
		return "cfArrayData";
	}
	
	@Override
	public Object get(String name, Scriptable start) {
		if ( name.equals("length") )
			return this.cfdata.size();
		else
			return super.get(name, start);
	}
	
	public java.lang.Object get(int index, Scriptable start){
		try {
			return convert.cfDataConvert( cfdata.getElement( index+1 ) );
		} catch (Exception e) {
			return null;
		}
	}
	
	public void put(int index, Scriptable start, Object value) {
		try {
			cfdata.setData( index + 1, tagUtils.convertToCfData( value ) );
		} catch (cfmRunTimeException e) {
			
		}
	}
}