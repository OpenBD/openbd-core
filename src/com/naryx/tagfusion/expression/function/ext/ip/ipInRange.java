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

package com.naryx.tagfusion.expression.function.ext.ip;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class ipInRange extends ipGetCount {
	private static final long serialVersionUID = 1L;
	
	public ipInRange() {
		min = 2; max = 3;
		setNamedParams( new String[]{ "ip", "mask", "checkip" } );
	}

	public String[] getParamInfo(){
		return new String[]{
				"ip - the IP address.  If in a simple notation (10.0.0.1) then netmask must be present, otherwise CIDR notation (10.0.0.1/24)",	
				"mask - if the IP address is not in CIDR notation, this is the netmask",
				"checkip - the IP address to check if its in the range"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"network", 
				"Determines if the checked IP address is in the IP address range", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		SubnetInfo	info	= getInfo( _session, argStruct );
		
		String checkip	= getNamedStringParam(argStruct, "checkip", null);
		if ( checkip == null )
			throwException(_session, "Missing checkip paramter" );

		
		return cfBooleanData.getcfBooleanData( info.isInRange(checkip) );
	}
	
	
	/**
	 * Gets the SubnetInfo block from the input parameters
	 * @param _session
	 * @param argStruct
	 * @return
	 * @throws cfmRunTimeException
	 */
	protected SubnetInfo	getInfo(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String ipaddress	= getNamedStringParam(argStruct, "ip", null);
		if ( ipaddress == null )
			throwException(_session, "Missing ip paramter" );
		
		if ( ipaddress.lastIndexOf("/") == -1 ){
			String mask	= getNamedStringParam(argStruct, "mask", null);
			if ( mask == null )
				throwException(_session, "Since IP address is not in CIDR notation, please provide a mask" );
			
			return new SubnetUtils(ipaddress, mask).getInfo();
		}else
			return new SubnetUtils(ipaddress).getInfo();
	}
}
