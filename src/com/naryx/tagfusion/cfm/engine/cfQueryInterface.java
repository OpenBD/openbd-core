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

/*
 This interface is designed to make integration into tagFusion a lot
 easier.  It allows a data type to be integrated into CFOUTPUT and other tags
 of a similar nature.
 
 The order in which CFOUTPUT calls methods are as follows:
 
 1. reset() 
 2. runQuery()
 3. nextRow()  - Called to get each row.  If false is returned that signals no more rows
 4. finishQuery()

 */

public interface cfQueryInterface {

	//--[ Called once, to reset the query
  public void reset();


	//--[ This is call to actually run the query
	public void runQuery( cfSession _Session ) throws cfmRunTimeException;
  
 
  //--[ Called to retrieve each row
  public boolean nextRow() throws cfmRunTimeException;
  
 	public int getCurrentRow();
  public boolean setCurrentRow( int _row );
  
 	//--[ Called to allow any clean up of the query
 	public void finishQuery();

	
	//--[ This sets the GROUP functions
	public void setGroupBy( String column, boolean caseSensitive ) throws cfmRunTimeException;
	public void removeGroupBy();
	public boolean isGrouped();
	public void startGroupOutput();
	public void endGroupOutput();
	public boolean nextRowInGroup() throws cfmRunTimeException;
}
