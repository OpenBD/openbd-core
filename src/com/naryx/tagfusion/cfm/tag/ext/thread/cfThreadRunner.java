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

package com.naryx.tagfusion.cfm.tag.ext.thread;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfThreadData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public abstract class cfThreadRunner extends Thread {

	public static final ThreadGroup cfThreadGroup = new ThreadGroup( "BlueDragon CFTHREAD" );
	
	protected cfSession threadSession;
	protected cfThreadData threadData;
	
	protected long startTime;
	protected long runningTime;
	protected byte executionStatus;
	protected static final byte NOT_STARTED = 0, RUNNING = 1, TERMINATED = 2, COMPLETED = 3, WAITING = 4;
	protected static final String [] STATUS_STRING = new String[]{ "NOT_STARTED", "RUNNING", "TERMINATED", "COMPLETED", "WAITING" };
	  
	protected cfThreadRunner( String threadName, cfSession tmpSession, int _priority )
	{
		super( cfThreadGroup, (Runnable)null );
		
		this.threadSession = tmpSession;
		this.setPriority( _priority );
		this.startTime = System.currentTimeMillis();
		
		if ( threadName != null ) { // named threads have associated data
			super.setName( threadName );
			this.threadData = new cfThreadData( this );
		} else {
			super.setName( "CFTHREAD-" + super.getId() );
		}
		executionStatus = NOT_STARTED;
		
		try {
			tmpSession.setData( "thread", this.threadData );
		} catch (cfmRunTimeException ignored) {} // won't happen
	}
	
	public cfThreadData getThreadData() {
		return this.threadData;
	}
	
	public void stopThread() {
		threadSession.stopThread();
		this.interrupt();
		this.executionStatus = TERMINATED;
	}
	
	public static void stopAllThreads() {
		cfThreadRunner[] activeThreads = new cfThreadRunner[ cfThreadGroup.activeCount() ];
		cfThreadGroup.enumerate( activeThreads );

		for ( int i = 0; i < activeThreads.length; i++ ) {
			activeThreads[ i ].stopThread();
	    }
	}
	
	public void joinThread() throws InterruptedException {
		if ( executionStatus == RUNNING )
			executionStatus = WAITING;
		this.join();
	}
		
	public void joinThread( long timeout ) throws InterruptedException {
		if ( executionStatus == RUNNING )
			executionStatus = WAITING;
		this.join( timeout );
	}
		
  
	public long getStartTime(){
		return this.startTime;
	}
	
	public String getStatus(){
		return STATUS_STRING[ executionStatus ];
	}

	abstract public void run();

	public long getRunningTime() {
		if ( runningTime > 0 ) {
			return runningTime;
		}
		return System.currentTimeMillis() - startTime;
	}
  
}
