/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 */


package com.naryx.tagfusion.util;

/**
 * This class does nothing with the events triggered for 
 * debugging purposes. Used when debugging is not enabled. 
 *
 */
 
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.sql.cfSQLQueryData;
import com.naryx.tagfusion.cfm.tag.cfTag;

public class nullRecorder implements debugRecorder{

  public final static nullRecorder staticInstance = new nullRecorder();
	
  public void startRequest(){}
  public void endRequest(){}
	
  public void startFile( cfFile thisFile ){}
  public void endFile( cfFile thisFile ){}
  
  public void setShow( boolean _show ){}
  public boolean getShow(){ return false; }
  public boolean getShowDBActivity(){ return false; }
  
  @SuppressWarnings( "rawtypes" )
	public void queryRan( String template, String qname, cfSQLQueryData query, List _qp ){}
  public void updateRan( String template, String datasrc, String sql ){}
  public void insertRan( String template, String datasrc, String sql ){}

	
  public void exceptionThrown( cfmRunTimeException exception, cfFile f, cfTag t ){}
		 
  @SuppressWarnings( "rawtypes" )
	public void storedProcRan( String _template, String _datasrc, String _procName, long _execTime, List _params, List _results ){}
	public void execStoredProc(String datasourceName, String callString, String procName, long execTime) {}
	
  
  public void recordTracepoint( String tracePoint ) {}

  public void recordTimer( String timer ) {}
  
  public debugRecorder copy(){ return staticInstance; }
  public void dump( cfSession session ){}
	public void execOnStart(cfData sqlData) {}
	public void execOnEnd(cfData sqlData) {}
	public void execMongo( MongoCollection<Document> col, String action, Document qry, long execTime ){}
}
