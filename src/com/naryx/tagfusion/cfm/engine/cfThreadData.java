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

package com.naryx.tagfusion.cfm.engine;

import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.tag.ext.thread.cfThreadRunner;

/**
 * This class is simply a cfStructData that contains a reference to a
 * cfThreadRunner instance.
 */
public class cfThreadData extends cfStructSelectiveReadOnlyData {
	
	static final long serialVersionUID = 1;
	
	private cfThreadRunner thread;

	private static Map READ_ONLY_KEYS;
	
	static{
		READ_ONLY_KEYS = new FastMap<String, cfData>( FastMap.CASE_INSENSITIVE );
		READ_ONLY_KEYS.put( "NAME", cfStringData.EMPTY_STRING );
		READ_ONLY_KEYS.put( "OUTPUT", cfStringData.EMPTY_STRING );
		READ_ONLY_KEYS.put( "ELAPSEDTIME", cfStringData.EMPTY_STRING );
		READ_ONLY_KEYS.put( "ERROR", cfStringData.EMPTY_STRING );
		READ_ONLY_KEYS.put( "PRIORITY", cfStringData.EMPTY_STRING );
		READ_ONLY_KEYS.put( "STARTTIME", cfStringData.EMPTY_STRING );
		READ_ONLY_KEYS.put( "STATUS", cfStringData.EMPTY_STRING );
		// since we're honouring the GENERATEDCONTENT key for backwards compatibility
		// we should make this read only too
		READ_ONLY_KEYS.put( "GENERATEDCONTENT", cfStringData.EMPTY_STRING );
	}
	
	public cfThreadData( cfThreadRunner rt ) {
		super( READ_ONLY_KEYS );
		
		thread = rt;
		
		super.setPrivateData( "name", new cfStringData( rt.getName() ) );
		
		setReturnVariable( cfNullData.NULL );
		setGeneratedContent( "" );
		setPriority( thread.getPriority() );
		setStartTime( thread.getStartTime() );
		setElapsedTime();
		setStatus();
	}
	
	public cfThreadRunner getThread() {
		return thread;
	}
	
	
	public synchronized cfData getData( String _key ) {
		// note that since the elapsedtime/status change, we grab 
		// the latest value if that is the variable that's been
		// requested
		if ( _key.equalsIgnoreCase( "elapsedtime" ) ){
			setElapsedTime();
		}else if ( _key.equalsIgnoreCase( "status" ) ){
			setStatus();
		}else if ( _key.equalsIgnoreCase( "generatedContent" ) ){ 
			// backwards compatibility with previous BD CFTHREAD implementation
			return super.getData( "output" );
		}else if ( _key.equalsIgnoreCase( "exception" ) ){ 
			// backwards compatibility with previous BD CFTHREAD implementation
			return super.getData( "error" );
		}
		return super.getData( _key );
	}

	public void setReturnVariable( cfData retval ) {
		super.setPrivateData( "returnVariable", retval );
	}
	
	public void setGeneratedContent( String content ) {
		cfStringData contentData = new cfStringData( content );
		super.setPrivateData( "output", contentData );
	}
	
	public void setException( cfCatchData catchData ) {
		super.setPrivateData( "error", catchData );
	}
	
	private void setStartTime( long _time ){
		super.setPrivateData( "starttime", new cfDateData( _time ) );
	}
	
	private void setPriority( int _p ){
		String priority = "NORMAL";
		if ( _p == Thread.MAX_PRIORITY ){
			priority = "HIGH";
		}else if ( _p == Thread.MIN_PRIORITY ){
			priority = "LOW";
		}
		super.setPrivateData( "priority", new cfStringData( priority ) );
	}
	
	private void setElapsedTime(){
		super.setPrivateData( "elapsedtime", new cfNumberData( thread.getRunningTime() ) );
	}
	
	private void setStatus(){
		super.setPrivateData( "status", new cfStringData( thread.getStatus() ) );		
	}
	
	public String getString() {
		return thread.getName();
	}
}
