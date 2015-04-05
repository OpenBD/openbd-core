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

/*
 * BlueDragon only tag.  (Alan Williamson)
 * 
 * This tag maintains a list of TOKENs and the time between
 * successive access of those TOKENs to allow for a throttle
 * mechanism.
 *  
 * ACTION = THROTTLE (default)
 *  - TOKEN = string of the token (defaults to the client IP) 
 *  - HITTHRESHOLD = number of times the token should be hit in a time period
 *  - HITTIMEPERIOD = the time period in which the max hits can appear in
 *  - MINHITTIME = the time where if a request comes in quicker its throttled
 * 
 *  returns CFTHROTTLE structure with a flag to say whether or not it should throttle
 * 
 * 
 * ACTION = STATUS
 *  - returns a structure CFTHROTTLE of all the tokens
 * 
 * 
 * ACTION = SET
 *  - HISTORY the size of the list (defaults to 100)
 * 
 */
package com.naryx.tagfusion.cfm.tag.ext;

import java.io.Serializable;

import org.apache.commons.collections.map.LRUMap;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfTHROTTLE extends cfTag  implements Serializable  {

	static final long serialVersionUID = 1;
	
	transient static LRUMap		throttleHistory = null;
	static int	totalHit = 0, totalThrottled = 0, totalThrottledQuick = 0;
	static long startTime;
	
	private int		actionType = 0;
	static int		ACTION_THROTTLE = 0;
	static int		ACTION_FLUSH		= 1;
	static int		ACTION_STATUS		= 2;
	static int		ACTION_SET			= 3;
	

	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		
		//-- If not initialised, initialise it
		if (throttleHistory == null){
			throttleHistory	= new LRUMap( 100 );
			startTime	= System.currentTimeMillis();
		}

		defaultAttribute( "ACTION", 				"THROTTLE" );
		defaultAttribute( "HITTHRESHOLD", 	"5" );
		defaultAttribute( "HITTIMEPERIOD", 	"10000" );
		defaultAttribute( "MINHITTIME", 		"500" );
		
		parseTagHeader( _tag );
		
		if ( getConstant("ACTION").equalsIgnoreCase("FLUSH") ){
			actionType	= ACTION_FLUSH;
		}else if ( getConstant("ACTION").equalsIgnoreCase("STATUS") ){
			actionType	= ACTION_STATUS;
		}else if ( getConstant("ACTION").equalsIgnoreCase("SET") ){
			actionType	= ACTION_SET;
		}else{
			actionType	= ACTION_THROTTLE;
		}
	}
	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {

		if ( actionType == ACTION_THROTTLE ){
			
			//-- Determine the token; if not present then use the clients remote IP address
			String token;
			if ( containsAttribute("TOKEN") )
				token = getDynamic(_Session,"TOKEN").getString();
			else
				token = _Session.REQ.getRemoteAddr();
			
			int hitThresHold			= getDynamic(_Session,"HITTHRESHOLD").getInt(); 
			long hitTimePeriodMs	= getDynamic(_Session,"HITTIMEPERIOD").getLong();
			long hitMinTimeMs			= getDynamic(_Session,"MINHITTIME").getLong();
			
			throttleClient 	client;
			cfStructData		result = new cfStructData();
			
			synchronized(throttleHistory){
				client	= (throttleClient)throttleHistory.get(token);
				if ( client == null ){
					client	= new throttleClient( token );
					throttleHistory.put( token, client );
				}
			}
			
			//-- Synchronize on this client
			synchronized( client ){
				long lasthit	= client.lastHit();
				long age			= client.age();
				
				totalHit++;
				client.hit();		//-- Register this hit
				
				if ( age <= hitTimePeriodMs && client.hitCount >= hitThresHold ){ //- checks to make sure they are within their allocated amount
					result.put( "THROTTLE", cfBooleanData.TRUE );
					client.throttled();
					totalThrottled++;
				} else if ( lasthit > 10 && lasthit < hitMinTimeMs ){	//-- checks for too fast accesses between requests
					result.put( "THROTTLE", cfBooleanData.TRUE );
					client.throttled();
					totalThrottled++;
					totalThrottledQuick++;
				}else
					result.put( "THROTTLE", cfBooleanData.FALSE );

				//-- If the age of this entry has expired then reset it.
				if ( age > hitTimePeriodMs )
					client.reset();
				
				//-- Set the data into the session
				result.setData("HITCOUNT", 	new cfNumberData(client.hitCount) );
				result.setData("TOTALHITS",	new cfNumberData(client.totalHits));
				result.setData("LASTHIT", 	new cfNumberData(lasthit) );
				result.setData("AGE", 			new cfNumberData(age) );

			}
			
			_Session.setData( "CFTHROTTLE", result );
			
			
		}else if ( actionType == ACTION_SET ){

			//-- Check to see if the history length has changed or been passed in
			if ( containsAttribute("HISTORY") ){
				throttleHistory	= new LRUMap( getDynamic(_Session,"HISTORY").getInt() );
			}
			
			
		}else if ( actionType == ACTION_FLUSH ){
			
			//-- Clears out the history
			synchronized(throttleHistory){
				throttleHistory.clear();
			}
			
		}else if ( actionType == ACTION_STATUS ){
			
			cfArrayData	array	= cfArrayData.createArray(1);			
			
			synchronized(throttleHistory){
				org.apache.commons.collections.OrderedMapIterator it	= throttleHistory.orderedMapIterator();
				throttleClient	client;
				cfStructData		clientData;
				
				while ( it.hasNext() ){
					it.next();
					client = (throttleClient)it.getValue();

					clientData	= new cfStructData();
	
					clientData.setData("TOKEN", 				new cfStringData(client.token) );
					clientData.setData("HIT", 					new cfNumberData(client.hitCount));
					clientData.setData("TOTALHITS",			new cfNumberData(client.totalHits));
					clientData.setData("TOTALTHROTTLE",	new cfNumberData(client.totalThrottled));
					clientData.setData("THROTTLE",			new cfNumberData(client.throttled));
					clientData.setData("LASTHIT",				new cfDateData(client.lastUsed) );
					clientData.setData("AGE",						new cfNumberData(client.age()) );
					
					array.addElement(clientData);
				}
			}

			cfStructData	throttle	= new cfStructData();
			throttle.setData("CLIENTS", 			array );
			throttle.setData("HITS", 					new cfNumberData(totalHit) );
			throttle.setData("THROTTLE", 			new cfNumberData(totalThrottled) );
			throttle.setData("QUICKTHROTTLE", new cfNumberData(totalThrottledQuick) );
			throttle.setData("STARTTIME", 		new cfDateData(startTime) );
			
			_Session.setData("CFTHROTTLE", throttle );
		}
		
		return cfTagReturnType.NORMAL;
	}

	//-----------------------
	
	class throttleClient extends Object {
		public String token;
		public int		hitCount;
		public long		lastUsed;
		public long		created;
		public int		throttled;
		public int		totalHits, totalThrottled;
		
		public throttleClient(String _token){
			token			= _token;
			hitCount 	= 0;
			throttled = 0;
			totalHits	= 0;
			totalThrottled	= 0;
			lastUsed  = System.currentTimeMillis();
			created		= lastUsed;
		}
		
		public final void throttled(){
			throttled++;
			totalThrottled++;
		}
		
		public final void hit(){
			hitCount++;
			totalHits++;
			lastUsed	= System.currentTimeMillis();
		}
		
		public final long age(){
			return System.currentTimeMillis() - created;
		}
		
		public final long lastHit(){
			return System.currentTimeMillis() - lastUsed;
		}
		
		public void reset(){
			hitCount 	= 0;
			lastUsed  = System.currentTimeMillis();
			created		= lastUsed;	
			throttled = 0;		
		}
	}

}
