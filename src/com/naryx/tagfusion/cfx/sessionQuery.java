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

package com.naryx.tagfusion.cfx;

import com.allaire.cfx.Query;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;

public class sessionQuery extends Object implements Query {

	private String 						queryName;
	private cfQueryResultData	queryData;

	public sessionQuery( String _queryName, cfQueryResultData _queryData ){
		queryData	= _queryData;
		queryName	= _queryName;
	}

	public int 		getRowCount(){
		return queryData.getNoRows();
	}
	
	public int 		getColumnIndex(String name){
		return queryData.getColumnIndexCF(name);
	}
	
	public String[] getColumns(){
		return queryData.getColumnList();
	}
	
	public int addRow(){
		try{	queryData.addRow( 1 ); }catch(Exception E){}
		return queryData.getNoRows();
	}
	
	public String getData(int iRow, int iCol) throws IndexOutOfBoundsException{
		try{
			return queryData.getCell( iRow, iCol ).getString();
		}catch(Exception E){
			throw new IndexOutOfBoundsException();		
		}
	}
	
	public void setData(int iRow,int iCol,String data) throws IndexOutOfBoundsException{
		try{
			queryData.setCell( iRow, iCol, new cfStringData(data) );
		}catch(Exception E){
			throw new IndexOutOfBoundsException();		
		}
	}

	public String getName(){
		return queryName;
	}
}
