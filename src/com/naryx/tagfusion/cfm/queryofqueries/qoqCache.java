/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  $Id: $
 */

package com.naryx.tagfusion.cfm.queryofqueries;


/**
 * SelectStatement Cache for query of queries 
 */

import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;

import org.aw20.util.SystemClockEvent;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class qoqCache implements SystemClockEvent {
  
  private static qoqCache qCache;
  private Hashtable<String, statementWrapper> cache;
  
	static{
		qCache	= new qoqCache();
	}
	
 	private qoqCache(){
    cache = new Hashtable<String, statementWrapper>();
    cfEngine.thisPlatform.timerSetListenerMinute( this, 3 );
    cfEngine.log( "-] qoqCache Loaded $Revision: 1.1 $" );
  }

	public static selectStatement getStatement( String _infix, int _maxParams ) throws cfmRunTimeException {
		return qCache.getStatementInner( _infix, _maxParams );				
	}


	public selectStatement getStatementInner( String _infix, int _maxParams ) throws cfmRunTimeException {
		statementWrapper sw = cache.get(_infix);
		if ( sw != null ){
			return sw.getStatement().copy();
		}

		try{
  		selectSQLParser sqlp = new selectSQLParser( new StringReader( _infix ) );
	    selectStatement prog = sqlp.Program( _maxParams ); 
			cache.put( _infix, new statementWrapper( prog ) );
  		return prog;
		}catch(ParseException pe){
			throw new cfmRunTimeException( catchDataFactory.extendedException("errorCode.expressionError", 
																																			"queryofqueries.badStatement", 
																																			new String[]{pe.getMessage()},
																																			_infix ) );
		}catch(Throwable e ){
			if ( e instanceof cfmRunTimeException ){
				throw (cfmRunTimeException)e;
			}
			throw new cfmRunTimeException( catchDataFactory.extendedException("errorCode.expressionError", 
																																			"queryofqueries.badParse", 
																																			new String[]{e.getMessage()},
																																			_infix));
		}
	}
  
  public void clockEvent(int type){
    Enumeration<String> keys = cache.keys();
    while( keys.hasMoreElements() ){
      String _infix = keys.nextElement();
      statementWrapper wrapper = cache.get( _infix );
      if ( ( System.currentTimeMillis() - wrapper.time ) > 300000 )
        cache.remove( _infix );
    }
  }

  class statementWrapper extends Object {
	  selectStatement statement;
	  long time;
	  
	  public statementWrapper( selectStatement _statement ){
	  	time = System.currentTimeMillis();
	  	statement	 = _statement;
	  }
	  
	  public selectStatement getStatement(){
	  	time = System.currentTimeMillis();
	  	return statement;
	  }
	}  
}
