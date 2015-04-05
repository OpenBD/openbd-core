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

/**
 * A scope is a collection of name->value maps. 
 */

package com.naryx.tagfusion.cfm.parser;

import java.util.Map;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public interface CFScope {

	// Return an lvalue for the given identifier (null if it doesn't exist)
	// Throws CFException if the identifier does not exist
	public cfLData get( String name, CFContext context )
	    throws cfmRunTimeException;

	// Return an lvalue for the given identifier. If create is true then create
	// it regardless of current existence, otherwise throws CFException
	// if the identifier does not exist
	public cfLData get( String name, boolean create, CFContext context )
	    throws cfmRunTimeException;

	// Resolve the name, returning the value of the identifier (not the lvalue).
	// Throws CFException if the identifier does not exist
	public cfData getVal( String name, CFContext context )
	    throws cfmRunTimeException;

	// an additional method to assist in non-query scope searching
	public cfData getVal( String name, CFContext context, boolean _doQuerySearch )
	    throws cfmRunTimeException;

	// returns true if the variable with the name 'name' is contained within this
	// scope
	public boolean containsVar( String name );

	// Set the value of the given identifier. A new one is created if necessary.
	public void put( String name, cfData val, CFContext context )
	    throws cfmRunTimeException;

	// Remove the identifier from the dictionary
	// Throws CFException if the identifier does not exist
	public void remove( String name, CFContext context )
	    throws cfmRunTimeException;

  //for use by CFDUMP
  public Map<String, cfData> getScopeMap();

}
