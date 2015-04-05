/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.bluedragon.plugin;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfcMethodData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class ObjectCFCImpl extends Object implements ObjectCFC {

	private cfComponentData component;
	private cfArgStructData args = new cfArgStructData();
	
	protected ObjectCFCImpl(cfComponentData component){
		this.component = component;
	}
	
	public cfComponentData	getComponentCFC(){
		return this.component;
	}
	
	public cfData runMethod( cfSession Session, String methodName ) throws Exception {
		cfcMethodData invocationData = new cfcMethodData( Session, methodName, args );
		return component.invokeComponentFunction( Session, invocationData );
	}
	
	public String runMethodReturnString( cfSession Session, String methodName ) throws Exception {
		cfcMethodData invocationData = new cfcMethodData( Session, methodName, args );
		return component.invokeComponentFunction( Session, invocationData ).getString();
	}
	
	public boolean runMethodReturnBoolean( cfSession Session, String methodName ) throws Exception {
		cfcMethodData invocationData = new cfcMethodData( Session, methodName, args );
		return component.invokeComponentFunction( Session, invocationData ).getBoolean();
	}
	
	public void clearArguments(){
		args.clear();
	}
	
	public void addArgument(String name, String argVaue) {
		args.setData(name, new cfStringData(argVaue));
	}

	public void addArgument(String name, String argVaue[]) {
		cfArrayData cfArray = cfArrayData.createArray(1);
		for (int x = 0; x < argVaue.length; x++) {
			try {
				cfArray.addElement(new cfStringData(argVaue[x]));
			} catch (cfmRunTimeException ignoreException) {}
		}
		args.setData(name, cfArray);
	}

	public void addArgument(String name, boolean argVaue) {
		args.setData(name, cfBooleanData.getcfBooleanData(argVaue));
	}

	public void addArgument(String name, int argVaue) {
		args.setData(name, new cfNumberData(argVaue));
	}

	public void addArgument(String name, cfData argVaue) {
		args.setData(name, argVaue);
	}
}
