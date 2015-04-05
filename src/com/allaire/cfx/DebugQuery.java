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

package com.allaire.cfx;

import java.util.ArrayList;
import java.util.List;

public class DebugQuery implements Query {

	String name;
	String columns[];
	List<List<String>>		tableRows;
	
	public DebugQuery( String name, String[] columns ) throws IllegalArgumentException{
		this.name			= name;
		this.columns	= columns;
		tableRows			= new ArrayList<List<String>>();
	}

	public DebugQuery( String name, String[] columns, String[][] data ) throws IllegalArgumentException{
		this.name			= name;
		this.columns	= columns;
		tableRows			= new ArrayList<List<String>>();
		
		for ( int r=0; r < data.length; r++ ){
			addRow();
			for ( int c=0; c < data[r].length; c++ ){
				setData( r+1, c+1, data[r][c] );
			}
		}
	}

	public int getColumnIndex(String name){
		for ( int x=0; x < columns.length; x++ ){
			if ( columns[x].equalsIgnoreCase( name ) )
				return x+1;
		}
		
		return 0;
	}

	public String[] getColumns(){	return columns; }
	public String getName(){return name;}
	public int getRowCount(){	return tableRows.size();}
	
	public int addRow(){
		List<String>	rowData	= new ArrayList<String>( columns.length );
		for ( int r = 0; r < columns.length; r++ )
			rowData.add( null );
				
		tableRows.add( rowData );
		return tableRows.size();
	}
		
	public String getData(int iRow, int iCol) throws IndexOutOfBoundsException{
		if ( iRow <= 0 || iRow > tableRows.size() )
			throw new IndexOutOfBoundsException();
			
		if ( iCol <= 0 || iCol > columns.length )
			throw new IndexOutOfBoundsException();
		
		String tmp = tableRows.get( iRow-1 ).get( iCol-1 );
		return ( tmp == null ) ? "" : tmp;
	}
	
	public void setData(int iRow,int iCol,String data) throws IndexOutOfBoundsException{
		if ( iRow <= 0 || iRow > tableRows.size() )
			throw new IndexOutOfBoundsException();
			
		if ( iCol <= 0 || iCol > columns.length )
			throw new IndexOutOfBoundsException();

		List<String> rowData	= tableRows.get( iRow - 1 );
		rowData.set( iCol - 1, data );
	}
}
