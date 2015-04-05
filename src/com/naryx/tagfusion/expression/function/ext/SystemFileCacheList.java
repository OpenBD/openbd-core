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

package com.naryx.tagfusion.expression.function.ext;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cachedFile;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;
import com.naryx.tagfusion.expression.function.functionBase;


public class SystemFileCacheList extends functionBase {
	private static final long serialVersionUID = 1L;

	public SystemFileCacheList() {
		min = max = 0;
	}

	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Returns an array of structs {uri,hits,lastused,realpath} representing all the files in the system file cache", 
				ReturnType.ARRAY );
	}
	
	public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{ 
		cachedFile[] allFiles = cfmlFileCache.getCachedFiles();
		
		cfArrayData	a = cfArrayData.createArray(1);
		
		for ( int x=0; x < allFiles.length; x++ ){
			cfStructData	s	= new cfStructData();
			
			s.setData( "uri", 			new cfStringData( allFiles[x].uri ) );
			s.setData( "hits", 			new cfNumberData( allFiles[x].hits ) );
			s.setData( "lastused", 	new cfDateData( allFiles[x].lastAccessed ) );
			s.setData( "realpath", 	new cfStringData( allFiles[x].realPath ) );
			
			a.addElement(s);
		}
		
		return a;
	}
}
