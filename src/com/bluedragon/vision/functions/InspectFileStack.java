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
package com.bluedragon.vision.functions;

import java.util.Iterator;
import java.util.List;

import org.aw20.collections.FastStack;

import com.bluedragon.vision.engine.CoreServer;
import com.bluedragon.vision.engine.VisionLiveSession;
import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.expression.function.functionBase;

public class InspectFileStack extends functionBase {
	private static final long serialVersionUID = 1L;

	public InspectFileStack(){
		min = max = 1;
	}
	
	public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException { 
	  int sessionid 	= parameters.get(0).getInt();
		
	  VisionLiveSession	ds = CoreServer.thisInst.getActiveSession( sessionid );
		
	  if ( ds == null ){
	  	throwException(_session, "invalid debugger session");
	  }
	  
	  String rootPath	= FileUtils.getRealPath("/").replace('\\','/');
	  if ( !rootPath.endsWith("/") )
	  	rootPath	+= "/";
	  
	  
	  cfArrayData	arr	= cfArrayData.createArray(1);
	  
	  cfSession cfsession	= ds.getCFSession();
	  FastStack<cfFile> fileStack = cfsession.getFileStack();
	  Iterator<cfFile>	it	= fileStack.iterator();
	  
	  while ( it.hasNext() ){
	  	cfFile f	= it.next();
	  	
	  	cfStructData	sd = new cfStructData();
	  	
	  	String tmp = f.getPath();
	  	sd.setData( "f", new cfStringData(tmp) );
	  	
	  	if ( tmp.startsWith( rootPath ) ){
	  		sd.put( "pf", 	new cfStringData( tmp.substring(rootPath.length()-1) ) );
	  	}else{
	  		sd.put( "pf", 	new cfStringData( tmp ) );
	  	}

	  	arr.addElement( sd );
	  }
	  
	  return arr;
	}
	
}
