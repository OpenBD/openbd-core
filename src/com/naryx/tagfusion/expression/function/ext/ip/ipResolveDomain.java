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

import java.net.UnknownHostException;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class ipResolveDomain extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public ipResolveDomain() {
		min = 1; max = 2;
		setNamedParams( new String[]{ "domain", "dns" } );
	}

	public String[] getParamInfo(){
		return new String[]{
				"domain - the name of the server you wish to resolve",	
				"dns - IP address of the DNS server to use for the lookup"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"network", 
				"Returns the IP address(s) of the supplied domain", 
				ReturnType.ARRAY );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String domain = getNamedStringParam(argStruct, "domain", null);
		if ( domain == null )
			throwException(_session, "Missing domain paramter" );
		
		Lookup lookup;
		try {
			lookup = new Lookup( domain );
			
			String dns = getNamedStringParam(argStruct, "dns", null);
			if ( dns != null )
				lookup.setResolver( new SimpleResolver(dns) );
			
			cfArrayData array	= cfArrayData.createArray(1);
			Record[] records	= lookup.run();
			if ( records != null ){
				for ( int x=0; x < records.length; x++ ){
					array.addElement( new cfStringData( records[x].rdataToString() ) );
				}
			}
			return array;
		
		} catch (TextParseException e) {
			throwException(_session, "invalid dns parameter" );
		} catch (UnknownHostException e) {
			throwException(_session, "Unknown host exception" );
		}
		
		return null;
	}
}
