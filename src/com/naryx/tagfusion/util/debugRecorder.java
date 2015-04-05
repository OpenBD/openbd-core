/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: debugRecorder.java 2343 2013-03-12 01:41:47Z alan $
 */

package com.naryx.tagfusion.util;

/**
 * This interface is for declaring the methods needed to record events
 * required when debugging is enabled. 
 *
 */
 
import java.util.List;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.sql.preparedData;
import com.naryx.tagfusion.cfm.sql.resultSetHolder;
import com.naryx.tagfusion.cfm.tag.cfTag;


public interface debugRecorder{

  public void startRequest();
  public void endRequest();
	
  public void startFile( cfFile thisFile );
  public void endFile( cfFile thisFile );

  public void setShow( boolean _show );
  public boolean getShow();
  public boolean getShowDBActivity();
    
  public void queryRan( String template, String qname, cfSQLQueryData query, List<preparedData> _qp );
  public void updateRan( String template, String datasrc, String sql );
  public void insertRan( String template, String datasrc, String sql );
	
  public void exceptionThrown( cfmRunTimeException exception, cfFile f, cfTag t );
		 
  public void storedProcRan( String _template, String _datasrc, String _procName, long _execTime,
                              List<preparedData> _params, List<resultSetHolder> _results );
  
  public void recordTracepoint( String tracePoint );

  public void recordTimer( String timing );

  public debugRecorder copy();
  public void dump( cfSession session );
  
  public void execOnStart( cfData sqlData );
  public void execOnEnd( cfData sqlData );
	public void execStoredProc(String datasourceName, String callString, String procName, long execTime);
	public void execMongo( DBCollection col, String action, DBObject qry, long execTime );
}
