/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: CoreServer.java 2450 2014-09-20 21:52:24Z andy $
 */
package com.bluedragon.vision.engine;

import java.util.HashMap;

import com.bluedragon.plugin.RequestListener;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class CoreServer implements RequestListener {

	public static CoreServer thisInst;

	private HashMap<Integer, VisionLiveSession> activeSessions;
	public HashMap<String, BreakPoint> fileBreakPoints;
	public boolean bStopOnException = false;


	public CoreServer() {
		thisInst = this;
	}


	public void enable( boolean eb ) {
		if ( eb ) {
			activeSessions = new HashMap<Integer, VisionLiveSession>();
			fileBreakPoints = new HashMap<String, BreakPoint>();
		} else {
			if ( activeSessions != null )
				activeSessions.clear();

			if ( fileBreakPoints != null )
				fileBreakPoints.clear();

			activeSessions = null;
			fileBreakPoints = null;
		}
	}


	public void setBreakPoint( BreakPoint bp ) {
		if ( !fileBreakPoints.containsKey( bp.getKey() ) ) {
			fileBreakPoints.put( bp.getKey(), bp );
		}
	}


	public void clearBreakPoint( BreakPoint bp ) {
		fileBreakPoints.remove( bp.getKey() );
	}


	public void clearAllBreakPoint() {
		fileBreakPoints.clear();
	}


	public void requestBadFileException( cfmBadFileException bfException, cfSession session ) {}


	public void requestRuntimeException( cfmRunTimeException cfException, cfSession session ) {
		VisionLiveSession ds = getActiveSession( session.getSessionID() );
		if ( ds != null ) {
			ds.onRunTimeException( cfException );
		}
	}


	public void requestStart( cfSession session ) {
		String queryString = session.REQ.getQueryString();
		if ( queryString != null && ( queryString.indexOf( "_cfmlbug" ) != -1 || queryString.indexOf( "_openbddebugger" ) != -1 ) )
			return;

		// This is not a request to the debugger; so lets track this one
		VisionLiveSession db = new VisionLiveSession( session );

		activeSessions.put( session.getSessionID(), db );
	}


	public void requestEnd( cfSession session ) {
		activeSessions.remove( session.getSessionID() );
	}


	public HashMap<Integer, VisionLiveSession> getActiveSessions() {
		return activeSessions;
	}


	public VisionLiveSession getActiveSession( int sessionid ) {
		return activeSessions.get( sessionid );
	}


	public void clearRequests() {
		activeSessions.clear();
	}

}