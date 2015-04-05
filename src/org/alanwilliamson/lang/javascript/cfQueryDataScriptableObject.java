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
 *  $Id: cfQueryDataScriptableObject.java 1747 2011-10-25 15:46:01Z alan $
 */
package org.alanwilliamson.lang.javascript;

import org.m0zilla.javascript.Scriptable;
import org.m0zilla.javascript.ScriptableObject;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;

public class cfQueryDataScriptableObject extends ScriptableObject {
	private static final long serialVersionUID = 1L;

	private cfQueryResultData cfdata;
	
	public cfQueryDataScriptableObject( cfQueryResultData data ){
		super();
		this.cfdata	= data;
	}
	
	public cfData getCFData(){
		return this.cfdata;
	}
	
	@Override
	public String getClassName() {
		return "cfQueryData";
	}

	public java.lang.Object get(String index, Scriptable start){
		try {
			cfData d = cfdata.getData(index);
			
			if ( d.getQueryTableData() == null )
				return convert.cfDataConvert( d );
			else
				return new cfQueryCellDataScriptableObject( d );
									
		} catch (Exception e) {
			return null;
		}
	}
	
	public java.lang.Object get(int index, Scriptable start){
		System.out.println( "get[" + index + "]" );
		return null;
		
		
	}

}