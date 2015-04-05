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

package org.alanwilliamson.amazon.sqs;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aw20.amazon.SimpleSQS;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class ReceiveMessage extends functionBase {
	private static final long serialVersionUID = 1L;
	
	public ReceiveMessage(){  
		min = 2; max = 4; 
		setNamedParams( new String[]{ "datasource", "queueurl", "maxmessages", "visibility" } );
	}

	public String[] getParamInfo(){
		return new String[]{
			"Amazon SQS session",
			"queueurl",
			"MaxNumberOfMessages",
			"VisibilityTimeout"
		};
	}
  
	public java.util.Map getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon SQS: Returns the messages available for processing sitting in teh queue", 
				ReturnType.ARRAY );
	}
	
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
 		String dsName			= getNamedStringParam(argStruct, "datasource", "");
 		String queueurl		= getNamedStringParam(argStruct, "queueurl", null );
 		if ( queueurl == null )
 			throwException(_session, "You must specify an Amazon QUEUE name to create" );
 		
 		int visibilityTimeOut 	= getNamedIntParam(argStruct, "visibility", -1);
 		int MaxNumberOfMessages = getNamedIntParam(argStruct, "maxmessages", 1 );

  	SimpleSQS	sqs = SQSFactory.getDS(dsName);
  	if ( sqs == null )
  		throwException(_session, "Invalid named Amazon SQS");

  	try {
  		List<Map>	mapList	= sqs.receiveMessage(queueurl, MaxNumberOfMessages, visibilityTimeOut);
  		cfArrayData	array	= cfArrayData.createArray(1);
  		
  		Iterator<Map> it	= mapList.iterator();
  		while ( it.hasNext() ){
  			Map map	= it.next();
  			
  			cfStructData	sMap	= new cfStructData();
  			sMap.putAll( map );
  			array.addElement( sMap );
  		}
  		
  		return array;
 		} catch (Exception e) {
			throwException(_session, "AmazonSQS: " + e.getMessage() );
			return cfBooleanData.FALSE;
		}
  }

}