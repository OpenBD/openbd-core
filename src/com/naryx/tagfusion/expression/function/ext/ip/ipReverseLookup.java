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

import java.io.IOException;
import java.net.UnknownHostException;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class ipReverseLookup extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public ipReverseLookup() {
		min = 1; max = 2;
		setNamedParams( new String[]{ "ip", "dns" } );
	}

	public String[] getParamInfo(){
		return new String[]{
				"ip - the IP address of the server you wish to get the domain records for",	
				"dns - IP address of the DNS server to use for the lookup"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"network", 
				"Performs a reverse IP lookup to determine the domain", 
				ReturnType.STRING );
	}
	
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		String ip = getNamedStringParam(argStruct, "ip", null);
		if ( ip == null )
			throwException(_session, "Missing IP paramter" );

		try {
			Name name = ReverseMap.fromAddress( ip );
			
			Record rec = Record.newRecord(name, Type.PTR, DClass.IN);
			Message query = Message.newQuery(rec);

			SimpleResolver resolver;
			String dns = getNamedStringParam(argStruct, "dns", null);
			if ( dns != null ){
				resolver = new SimpleResolver(dns);
			}else{
				resolver = new SimpleResolver();
			}
			
			Message response = resolver.send(query);
			Record[] answers = response.getSectionArray(Section.ANSWER);

			cfArrayData array	= cfArrayData.createArray(1);

			if ( answers != null ){
				for ( int x=0; x < answers.length; x++ ){
					array.addElement( new cfStringData( answers[x].rdataToString() ) );
				}
			}
			
			return array;
		
		} catch (TextParseException e) {
			throwException(_session, "invalid dns parameter" );
		} catch (UnknownHostException e) {
			throwException(_session, "Unknown host exception" );
		} catch (IOException e) {
			throwException(_session, "IOException" );
		}
		
		return null;
	}
}
