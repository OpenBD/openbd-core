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
package org.alanwilliamson.lang.java.inline;

import java.util.Date;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;

public class cfQuery extends Object {

	private cfQueryResultData cfdata;
	
	public cfQuery( cfQueryResultData data ){
		this.cfdata	= data;
	}
	
	public cfData getCFData(){
		return this.cfdata;
	}
	
	public String getName() {
		return "cfQuery";
	}

	public String[] getColumns(){
		return cfdata.getColumnList();
	}
	
	public int size(){
		return cfdata.getSize();
	}
	
	public int addRow(){
		try{	cfdata.addRow( 1 ); }catch(Exception E){}
		return cfdata.getNoRows();
	}

	public void deleteRow(int index){
		try{	cfdata.deleteRow( index+1 ); }catch(Exception E){}
	}
	
	public Object get(int iRow, int iCol) throws IndexOutOfBoundsException{
		try{
			return ContextImpl.getForJava( cfdata.getCell( iRow, iCol ) );
		}catch(Exception E){
			throw new IndexOutOfBoundsException();		
		}
	}
	
	public String getString(int iRow, int iCol) throws IndexOutOfBoundsException{
		try{
			return cfdata.getCell( iRow, iCol ).getString();
		}catch(Exception E){
			throw new IndexOutOfBoundsException();		
		}
	}
	
	public int getInt(int iRow, int iCol) throws IndexOutOfBoundsException{
		try{
			return cfdata.getCell( iRow, iCol ).getInt();
		}catch(Exception E){
			throw new IndexOutOfBoundsException();		
		}
	}
	
	public long getLong(int iRow, int iCol) throws IndexOutOfBoundsException{
		try{
			return cfdata.getCell( iRow, iCol ).getLong();
		}catch(Exception E){
			throw new IndexOutOfBoundsException();		
		}
	}
	
	public boolean getBoolean(int iRow, int iCol) throws IndexOutOfBoundsException{
		try{
			return cfdata.getCell( iRow, iCol ).getBoolean();
		}catch(Exception E){
			throw new IndexOutOfBoundsException();		
		}
	}
	
	public Date getDate(int iRow, int iCol) throws IndexOutOfBoundsException{
		try{
			return new Date( cfdata.getCell( iRow, iCol ).getLong() );
		}catch(Exception E){
			throw new IndexOutOfBoundsException();		
		}
	}
	
	public void set(int iRow,int iCol,String data) throws IndexOutOfBoundsException{
		try{
			cfdata.setCell( iRow+1, iCol+1, new cfStringData(data) );
		}catch(Exception E){
			throw new IndexOutOfBoundsException();		
		}
	}
	
	public void set(int iRow,int iCol,int data) throws IndexOutOfBoundsException{
		try{
			cfdata.setCell( iRow+1, iCol+1, new cfNumberData(data) );
		}catch(Exception E){
			throw new IndexOutOfBoundsException();		
		}
	}
	
	public void set(int iRow,int iCol,boolean data) throws IndexOutOfBoundsException{
		try{
			cfdata.setCell( iRow+1, iCol+1, cfBooleanData.getcfBooleanData(data) );
		}catch(Exception E){
			throw new IndexOutOfBoundsException();		
		}
	}
	
	public void set(int iRow,int iCol,Date date) throws IndexOutOfBoundsException{
		try{
			cfdata.setCell( iRow+1, iCol+1, new cfDateData(date) );
		}catch(Exception E){
			throw new IndexOutOfBoundsException();		
		}
	}

}