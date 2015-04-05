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

package com.naryx.tagfusion.cfm.file;

import com.nary.io.FileUtils;

public class cachedFile extends java.lang.Object {
  public long     lastAccessed;
  private long    lastModified;
  public cfFile   file;
  public String   realPath;
  public String   uri;
  public int      hits;
  public boolean  bNeverExpire;
	public String  	Url;
  
	public cachedFile(){
	  file          = null;
    realPath      = null;
    uri           = null;
    lastModified  = 0;
    hits          = 0;
    lastAccessed  = System.currentTimeMillis();
    bNeverExpire  = false;
	}
	
  public cachedFile( cfFile _file, String _realPath, String _uri ){
    file          = _file;
    realPath      = _realPath;
    uri           = _uri;
    lastModified  = FileUtils.getLastModified( realPath );
    hits          = 1;
    lastAccessed  = System.currentTimeMillis();
    bNeverExpire  = false;
  }

  public void setNeverExpire(){  bNeverExpire  = true;  }
	public boolean neverExpires(){ return bNeverExpire; }

	public boolean wasModified(){
		if ( lastModified != 0 && realPath != null )
			return ( lastModified != FileUtils.getLastModified( realPath ) );

		return false;
	}
	
  public cfFile get(){
    hits++;
    lastAccessed = System.currentTimeMillis();
    return file;
  }
	
	public void setRealPath( String _realPath ) {
		realPath = _realPath;
		lastModified = FileUtils.getLastModified( realPath );
	}
	
	public String getRealPath(){ return realPath; }
	
	public void setURL( String _url ){	Url	= _url;  }
	public String getURL(){	return Url; }
}
