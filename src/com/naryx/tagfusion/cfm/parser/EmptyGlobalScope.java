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

package com.naryx.tagfusion.cfm.parser;

import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class EmptyGlobalScope implements CFGlobalScopeInterface {

	public EmptyGlobalScope() {}

	public boolean containsVar( String name ) {
		return false;
	}

	public cfLData get( String name, CFContext context ) {
		return new cfLData( this, name, false, false );
	}

	public cfLData get( String name, boolean create, CFContext context ) {
		return new cfLData( this, name, false, false );
	}

	public cfLData get( String name, CFContext context, boolean _doQuerysearch ) {
		return new cfLData( this, name, false, false );
	}

	public cfLData get( String name, boolean create, CFContext context,
	    boolean _doQuerysearch ) {
		return new cfLData( this, name, false, false );
	}

	public cfData getVal( String name, CFContext context ) {
		return CFUndefinedValue.UNDEFINED;
	}

	public cfData getVal( String name, CFContext context, boolean querySearch ) {
		return CFUndefinedValue.UNDEFINED;
	}

	public void put( String name, cfData val, CFContext context )
	    throws cfmRunTimeException {
	}

	public void putGlobal( String name, cfData val ) {
	}

	public void remove( String name, CFContext context )
	    throws cfmRunTimeException {
	}

  public Map<String, cfData> getScopeMap() {
	  throw new UnsupportedOperationException();
  }

}
