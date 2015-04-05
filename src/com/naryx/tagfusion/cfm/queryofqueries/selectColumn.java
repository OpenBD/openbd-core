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

package com.naryx.tagfusion.cfm.queryofqueries;

/**
 * This class represents a column in SQL select statement.
 */
 
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class selectColumn{

	String columnName = "";
	String table = "";
	expression expr = null;
	String alias = null;
	cfData [] rowResult;
	String [] colNames;
	int [] colTypes;
	String [] expressionStrings = null;
	
	// the rowResult and colNames need a rowContext to be initialised so this
	// boolean ensures that it can be done once on the first execute.
	boolean init = false;
	
	private int computedResultNo = 1;
	final static byte ASTERISK = 0, COLUMN = 1, TABLEANDCOLUMN = 2, TABLEANDASTERISK = 3, EXPRESSION = 4, AGGREGATEFUNCTION = 5;
	byte colType = 0;
	
	selectColumn(){
		colType = ASTERISK; 
	}// selectColumn()
	
	
	selectColumn( String _col ){
		columnName = _col;
		colType = COLUMN;
	}// selectColumn()
	
	
	selectColumn( String _table, String _col ){
		table = _table;
		columnName = _col;
		if ( columnName.equals( "*" ) ){
			colType = TABLEANDASTERISK;
		}else{
			colType = TABLEANDCOLUMN;
		}
	}// selectColumn()
	
	
	selectColumn( expression _e ){
		expr = _e;
		if ( _e instanceof aggregateFunction ){
			colType = AGGREGATEFUNCTION;
		}else{
			colType = EXPRESSION;
		}
	}// selectColumn()
	
	
	void setAlias( String _alias ){
		alias = _alias;
	}// setAlias()
	
	String getAlias(){
		return alias;
	}// getAlias()
	
	
	// returns the name(s) for this select column.
	// this is used to name the column(s) in the result table
	String [] getColumnNames(){
		return colNames;
	}// getColumnNames()
	
	int [] getColumnTypes(){
		return colTypes;
	}
	
	
	boolean isExpression(){
		return colType == EXPRESSION;
	}// isExpression() 
	
	boolean isAggregateFunction(){
		return ( colType == AGGREGATEFUNCTION || ( colType == EXPRESSION && expr.isAggregateFunction() ) );
	}// isAggregateFunction()
	
	expression getExpression(){
		return expr;
	}// getExpression()
	
	
	byte getColumnType(){
		return colType;
	}// getColumnType()
	
  public String getTable(){
    return table;
  }
  
	
  public String getShortName(){
   return columnName; 
  }
  
  public String [] getExpressionStrings(){
  	return expressionStrings;
  }

  public String getFullName( rowContext _context ){
    if ( colType == TABLEANDCOLUMN && table.length() != 0 ){
      return table + "." + columnName;
    }else if ( colType == COLUMN ){
      return _context.getTableName( columnName ) + "." + columnName;
    }else{
      return "";
    }
   }

  
	// return something!
	cfData[] execute( rowContext _rowContext, List<cfData> _pData ) throws cfmRunTimeException{
	  /*if ( !init ){
			initResultData( _rowContext );
		}*/
		
		// cfData [] will be same size every time so just reuse it
		switch ( colType ){
			case ASTERISK:
				_rowContext.getAll( rowResult );
				break;
				
			case COLUMN:
				rowResult[0] = _rowContext.get( columnName );
				break;
			
			case TABLEANDCOLUMN:
				rowResult[0] = _rowContext.get( table, columnName );
				break;
			
			case TABLEANDASTERISK:
				_rowContext.getAllFromTable( table, rowResult );
				break;
			
			case AGGREGATEFUNCTION:
			case EXPRESSION:
				//expr.reset();
				rowResult[0] = expr.evaluate( _rowContext, _pData );
				break;

			default:
				break;
		
		}// switch
		
		return rowResult;
				
	}// execute()
	
	
	/**
	 * call once to setup the variables used in returning the
	 * results of execute(). The rowContext does not require data
	 * in it.
	 */
	 
	void initResultData( rowContext _rowContext ){
		switch ( colType ){
			
			case ASTERISK:
				colNames = _rowContext.getAllColumnNames();
				colTypes = _rowContext.getAllColumnTypes();
				rowResult = new cfData[ colNames.length ];
				break;
				
			case COLUMN:
				rowResult = new cfData [1];
				colNames = new String [1];
				colNames[0] = ( alias == null ? columnName : alias );
				break;

			case AGGREGATEFUNCTION:		
				expressionStrings = new String[1];
				expressionStrings[0] = expr.toString();

		  case EXPRESSION:
				rowResult = new cfData [1];
				colNames = new String [1];
				colNames[0] = alias;//( alias == null ? getComputedResultName() : alias );
				expr.reset();
				break;
				
			case TABLEANDCOLUMN:
				rowResult = new cfData [1];
				colNames = new String [1];
				colNames[0] = ( alias == null ? columnName : alias );
				break;
				
			case TABLEANDASTERISK:
				// get the column names of the table from the rowContext
				colNames = _rowContext.getTableColumnNames( table );
				colTypes = _rowContext.getTableColumnTypes( table );
				rowResult = new cfData[ colNames.length ];
				break;

			default:
				break;
		
		}// switch()				
		
	}// initResultData()
	

	/**
	 * this returns a copy to itself. 
	 */
	selectColumn shallowCopy(){
		selectColumn selColCopy = new selectColumn();
		selColCopy.columnName = this.columnName;
		selColCopy.table = this.table;
		selColCopy.alias = this.alias;
		selColCopy.rowResult = this.rowResult; 
		selColCopy.colNames = this.colNames;
		selColCopy.colTypes = this.colTypes;
		selColCopy.init = this.init;
		selColCopy.computedResultNo = this.computedResultNo;
		selColCopy.colType = this.colType;
		selColCopy.expr = this.expr != null ? this.expr.copy() : null;
		return selColCopy;
	}// shallowCopy()	
	
	
	public String toString(){
		if ( expr == null ){
			return "Table name : " + table + "Column name: " + columnName + ( alias == null ? "" : alias );
		}else{
			return expr.toString();
		}
		
	}// toString()
	
	
}// selectColumn
