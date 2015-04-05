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


package com.naryx.tagfusion.cfm.engine.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MBeanServerConnection;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class JmxGetDomainList extends functionBase {
	
  private static final long serialVersionUID = 1L;

  public JmxGetDomainList(){ min = max = 0; }
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Returns back an array of all the available JMX domains within this Java virtual machine", 
				ReturnType.ARRAY );
	}
   
  
  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
 
  	MBeanServerConnection mbs = ManagementFactory.getPlatformMBeanServer();
		cfArrayData arr = cfArrayData.createArray(1);
  	
  	try {
			String[] domains = mbs.getDomains();
			
			for ( int x=0; x < domains.length; x++ )
				arr.addElement( new cfStringData( domains[x] ) );
			
		} catch (IOException e) {
			throwException( _session, "Failed to retrieve the domains: " + e.getMessage() );
		}
  	
  	return arr;
  } 
}