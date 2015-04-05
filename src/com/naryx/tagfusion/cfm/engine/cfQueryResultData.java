/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  http://openbd.org/
 *  $Id: cfQueryResultData.java 2408 2013-10-22 12:12:42Z craig $
 */

package com.naryx.tagfusion.cfm.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.queryofqueries.orderByCol;
import com.naryx.tagfusion.cfm.sql.preparedData;
import com.naryx.tagfusion.cfm.tag.cfDUMP;

/** 
 * This class handles the Query data structure in ColdFusion.
 * Please note *all* row/column references are 1 based and not 0 based
 */

public class cfQueryResultData extends cfJavaObjectData implements cfQueryInterface, ResultSet, Serializable {

	private static final long serialVersionUID = 1L;
	
	private cfQueryResultSetMetaData	rsmd;
	private List<List<cfData>>			tableRows;
  
	private Vector<cfNullData> nulldatas;

	// The following item is displayed in a dump so let's serialize it out.
	private String	querySource;
	public long executeTime;
	public String queryString;

	transient protected List<preparedData> preparedDataList;
	transient protected int maxRows;

	
	transient private int	LASTROW, CURRENTROW;
  
	transient private List<cfData> rowCursor;
	
	transient private Stack<Integer> groupColumns = new Stack<Integer>();

	transient private Stack<Boolean> groupCaseSensitive = new Stack<Boolean>();
  
	transient private int	  groupCurrentRow = 0;
	
	transient private boolean groupEnabled = true;

	public cfQueryResultData( String _querySource ){
		this(new String[] {}, _querySource);
	}
	
	public cfQueryResultData( String _columnList[], String _querySource ){
		this( _columnList, null, _querySource );
	}

	public cfQueryResultData( String _columnList[], int [] _columnTypeList, String _querySource ){
		super( "" ); // fudge
		setInstance( this );
		init( _columnList, _columnTypeList, _querySource );
	}
	
	public cfQueryResultData( ResultSet rs, String querySource, int maxRows ) throws SQLException {
		super( "" ); // fudge
		setInstance( this );
		this.tableRows		= new ArrayList<List<cfData>>();
		this.nulldatas    = new Vector<cfNullData>();
		this.querySource	= querySource;
		LASTROW						= 0;
		CURRENTROW				= 0;
		rowCursor					= null;
		
		populate( rs, maxRows );
	}

	private cfQueryResultData( cfQueryResultSetMetaData rsmd, String querySource ) {
		super( "" ); // fudge
		setInstance( this );
		this.tableRows		= new ArrayList<List<cfData>>();
		this.nulldatas    = new Vector<cfNullData>();
		this.rsmd = rsmd;
		this.querySource	= querySource;
		LASTROW						= 0;
		CURRENTROW				= 0;
		rowCursor					= null;
	}
	
	protected void init( String [] _columnList, int [] _columnTypeList, String _querySource ){
		this.tableRows		= new ArrayList<List<cfData>>();
		this.nulldatas    = new Vector<cfNullData>();
		this.rsmd = new cfQueryResultSetMetaData( _columnList, _columnTypeList );
		this.querySource	= _querySource;
		LASTROW						= 0;
		CURRENTROW				= 0;
		rowCursor					= null;
	}
	

	public void addPreparedData(preparedData _Data) {
		if (preparedDataList == null)
			preparedDataList = new ArrayList<preparedData>();

		preparedDataList.add(_Data);
	}


	public long getExecuteTime() {
		return executeTime;
	}
	
	public String getQueryString() {
		return queryString;
	}

	public void setMaxRows(int _maxRows) {
		maxRows = _maxRows;
	}

	public void setQueryString(String _queryString) {
		queryString = _queryString.trim();
	}
	
	protected void populate( ResultSet rs, int maxRows ) throws SQLException {
		rsmd = new cfQueryResultSetMetaData( rs.getMetaData() );
	
		for ( int rowCount = 0; rs.next() && ( rowCount != maxRows ); rowCount++ )
			setRow( rs );
		
		CURRENTROW = 0;
		setCursors();
	}

	/*
	 * getRowCursor
	 * 
	 * rowCursor is a transient member that isn't serialized therefore we need to make
	 * sure it is initialized before it is used after this object is deserialized.
	 */
	public List<cfData> getRowCursor()
	{
		if ( (rowCursor == null) && (tableRows.size() > 0) )
		{
			rowCursor = tableRows.get(0);
		}

		return rowCursor;
	}

	/*
	 * getGroupColumns
	 * 
	 * groupColumns is a transient member that isn't serialized therefore we need to make
	 * sure it is initialized before it used after this object is deserialized.
	 */
	public Stack<Integer> getGroupColumns()
	{
		if ( groupColumns == null )
			groupColumns = new Stack<Integer>();

		return groupColumns;
	}
	
	/*
	 * getGroupCaseSensitive
	 * 
	 * groupCaseSensitive is a transient member that isn't serialized therefore we need to make
	 * sure it is initialized before it used after this object is deserialized.
	 */
	public Stack<Boolean> getGroupCaseSensitive()
	{
		if ( groupCaseSensitive == null )
			groupCaseSensitive = new Stack<Boolean>();

		return groupCaseSensitive;
	}

	public byte getDataType(){ return cfData.CFQUERYRESULTDATA; }
	public String getDataTypeName() { return "query"; }
	
  
  public List<List<cfData>> getQueryTableData(){
    return this.tableRows;
  }
    
	public void setQuerySource(String _querySource){
		querySource = _querySource; 
	}
	
	public String getQuerySource(){
		return querySource;
	}
  
	public void setQueryData( cfQueryResultData newQuery, String src ){
		/*
		 *  When we cache data we serialise the original object instance.  However upon reloading we simply
		 *  set the elements we require internally.  This makes life easier as we don't need to rewrite any
		 *  object references that maybe pointing to this object instance from inside the core engine. 
		 */
		tableRows		= newQuery.tableRows;
		nulldatas   = newQuery.nulldatas;
		rsmd 				= newQuery.rsmd;
		querySource	= src;
    setCursors();
	}

	public void setQueryData( List<List<cfData>> _queryData ){
		// assumes that queryData supplied will work with the set columnList.
		tableRows = _queryData;
		setCursors();
	}

	private void setCursors() {
		LASTROW = tableRows.size();
		if (tableRows.size() > 0) {
			CURRENTROW = 1;
			rowCursor = tableRows.get(0);
		}
	}
	
	public List<cfData> getRow( int _index ){
		return tableRows.get( _index );
	}
	
	private void setRow( ResultSet dataRow ) throws SQLException{
		int columnCount = getNoColumns();
		
		//--[ Add a new row to the underlying data set
		addRow(1);

		for ( int x = 0; x < columnCount; x++ )
		{
			int columnType = rsmd.getColumnType( x + 1 );
//			String columnName = rsmd.getColumnName( x + 1 ); // useful when debugging
            String columnTypeName = null;
			cfData newData = null;
			
			switch ( columnType )
			{	
				// NOTE:  java.sql.Types.DATE used to be treated separately with a call to getDate()
				//        instead of getTimestamp() but this caused problems with the Oracle 10g JDBC
				//        driver which maps the Oracle DATE data type to java.sql.Types.DATE when it
				//        should map it to java.sql.Types.TIMESTAMP.  To work around this we now treat
				//        it as if it was a java.sql.Types.TIMESTAMP.  Refer to bug #1433.
				case java.sql.Types.TIMESTAMP :
				case java.sql.Types.DATE :
					try {
						Timestamp ts = dataRow.getTimestamp( x + 1 );
						if ( ts != null ) {
							newData = new cfDateData( ts );
						}
					} catch ( SQLException e ) {
						// for MySQL column type YEAR(2)
						String year = dataRow.getString( x + 1 );
						if ( year != null ) {
							try {
								String fullDate;
								if ( year.length() == 2 ) {
									// MySql JDBC driver 3.1.x
									fullDate = "01/01/" + ( Integer.parseInt( year ) < 70 ? "20" : "19" ) + year;
								} else {
									// MySql JDBC driver 5.0.x
									fullDate = "01/01/" + year.substring(0,4);
								}
								long date = DateFormat.getDateInstance( DateFormat.SHORT ).parse( fullDate ).getTime();
								newData = new cfDateData( new java.sql.Date( date ) );
							} catch ( ParseException pe ) {
								newData = new cfStringData( year );
							}
						}
					}
					break;
				
				case java.sql.Types.TIME :
					Time time = dataRow.getTime( x + 1 );
					if ( time != null ) {
						newData = new cfDateData( time );
					}
					break;

				case java.sql.Types.BIT :
					boolean b = dataRow.getBoolean( x + 1 );  // ODBC requires this for bits instead of getInt()
					if ( !dataRow.wasNull() ) {
						newData = new cfNumberData( b ? 1 : 0 );  // CF5/MX treat bits as numbers, not booleans
					}
					break;
					
				case java.sql.Types.TINYINT :
					short tiny;
					if ( rsmd.isSigned( x + 1 ) ) {
						tiny = dataRow.getByte( x + 1 );
					} else {  // for MySQL, must get unsigned as short
						tiny = dataRow.getShort( x + 1 );
					}
					if ( !dataRow.wasNull() ) {
						newData = new cfNumberData( tiny );
					}
					break;

				case java.sql.Types.SMALLINT :
					int s;
					if ( rsmd.isSigned( x + 1 ) ) {
						s = dataRow.getShort( x + 1 );
					} else {  // for MySQL, must get unsigned as int
						s = dataRow.getInt( x + 1 );
					}
					if ( !dataRow.wasNull() ) {
						newData = new cfNumberData( s );
					}
					break;
				
				case java.sql.Types.INTEGER :
					long i;
					if ( rsmd.isSigned( x + 1 ) ) {
						i = dataRow.getInt( x + 1 );
					} else { // for MySQL, must get unsigned as long
						i = dataRow.getLong( x + 1 );
					}
					if ( !dataRow.wasNull() ) {
						newData = new cfNumberData( i );
					}
					break;
				
				case java.sql.Types.BIGINT :
					if ( rsmd.isSigned( x + 1 ) ) {
						long l = dataRow.getLong( x + 1 );
						if ( !dataRow.wasNull() ) {
							newData = new cfNumberData( l );
						}
						break;
					}
					// for unsigned (MySQL), fall through and get as double
					
				case java.sql.Types.DECIMAL :
				case java.sql.Types.NUMERIC :
				case java.sql.Types.DOUBLE :
				case java.sql.Types.FLOAT :
					double d = dataRow.getDouble( x + 1 );
					if ( !dataRow.wasNull() ) {
						newData = new cfNumberData( d );
					}
					break;
				
				case java.sql.Types.REAL :
					float f = dataRow.getFloat( x + 1 );
					if ( !dataRow.wasNull() ) {
						// For some reason casting a float to a double doesn't return a double
						// that exactly matches the original float so we'll use the less efficient
						// algorithm of converting the float to a string and the string to a double.
						// If for some reason this fails then we'll revert to casting the float to
						// a double.
						double d2;
						try {
							d2 = Double.valueOf( Float.toString( f ) ).doubleValue();
						} catch ( Exception e ) { 	
							d2 = f;
						}
						newData = new cfNumberData( d2 );
					}
					break;

				// this is old code that works, but the code above for DOUBLE
				// is more efficient
//				case java.sql.Types.DECIMAL :
//				case java.sql.Types.NUMERIC :
//					BigDecimal bd = dataRow.getBigDecimal( x + 1 );
//					if ( !dataRow.wasNull() ) {
//						newData = new cfNumberData( bd.doubleValue() );
//						querySizeBytes += 8;
//					}
//					break;
				
				case java.sql.Types.BINARY :
				case java.sql.Types.VARBINARY :
				case java.sql.Types.LONGVARBINARY :
				case java.sql.Types.BLOB :
					InputStream in = dataRow.getBinaryStream( x + 1 );
					if ( !dataRow.wasNull() ) {
						newData = new cfBinaryData( in );
						try{ in.close(); }catch( IOException ignored ){}
					}
					break;
                
                case java.sql.Types.LONGVARCHAR :
                case java.sql.Types.CLOB :
                  newData = getAsCfStringData( dataRow, x+1 );
                  break;
                  
				case java.sql.Types.OTHER :
					// for Microsoft SQL Server via the JDBC-ODBC Bridge, column
					// types such as UniqueIdentifier, NChar, NText, and NVarChar
					// get here, but obj is always null
					Object obj = dataRow.getObject( x + 1 );
					if ( dataRow.wasNull() ) {
						break;
					}
					if ( obj != null ) {
						String className = obj.getClass().getName();
						if ( obj.getClass().isArray() ){
							if ( className.equals( "[B" ) ) {
								newData = new cfBinaryData( (byte[]) obj );
								break;
							}
						} else if ( className.equals( "org.postgresql.util.PGobject" ) ) {
							// This code will be hit for Postgresql abstime and reltime types.  We copy what
							// happens when the JDBC-ODBC bridge is used and return a cfDateData type for
							// abstime and a cfStringData type for reltime.
							try {
								java.lang.reflect.Method m = obj.getClass().getMethod( "getType", (Class[])null );
								String type = (String)m.invoke( obj, (Object[])null );
								if ( type.equals( "abstime" ) )
								{
									m = obj.getClass().getMethod( "getValue", (Class[])null );
									String value = (String)m.invoke( obj, (Object[])null );
									newData = new cfDateData( java.sql.Timestamp.valueOf(value.substring(0,19)) );
									break;
								}
							} catch ( Exception e ) {}
							newData = new cfStringData( obj.toString() );
							break;
						} else {
							newData = new cfJavaObjectData( obj );
                        	break;
                        }
					} else {
                        columnTypeName = rsmd.getColumnTypeName( x + 1 ).toLowerCase();
                        if ( columnTypeName.equals( "ntext" ) )
                        {
                            in = dataRow.getBinaryStream( x + 1 );
                            try {
                                newData = cfStringData.getString( in );
                            } catch ( IOException e ) {
                                throw new SQLException( e.toString() );
                            }
                            break;
                        }
                        else if ( columnTypeName.equals( "uniqueidentifier" ) )
                        {
                            newData = new cfStringData( getUniqueIdentifier( dataRow.getBytes( x + 1 ) ) );
                            break;
                        }
					}
					// fall through to default case (string)
					
				case -8 : // OracleTypes.ROWID
				case java.sql.Types.CHAR :
				case java.sql.Types.VARCHAR :
					// fall through to default case (string)
    
				default :	// everything else is treated as string data
					String str = dataRow.getString( x + 1 );
					if ( str != null ) {
						newData = new cfStringData( str );
					}
					break;
			}

			//--[ Put this into the Engine now
			//--[ If it's a null field value then use cfNullData.DBNULL instead of cfNullData.NULL.
			setCell( LASTROW, x + 1, ( newData == null ? getNull( x + 1 ) : newData ) );
		}
	}
	
	public static cfStringData getAsCfStringData( ResultSet _result, int _index ) throws SQLException {
		Reader inr = _result.getCharacterStream( _index );
		if ( !_result.wasNull() ) {
			try {
				return cfStringData.getString( inr );
			} catch ( IOException e ) {
				throw new SQLException( e.toString() );
			}
		}else{
			return null;
		}
	}
    
    private static String getUniqueIdentifier( byte[] obj )
    {
        if ( obj == null )
        	return null;
        
        // this code copied from com.newatlanta.jturbo.driver.ResultSet.getObjectUsingType 
        byte[] abyte0 = obj;
        int k = abyte0.length;
        if ( k < 8 )
            return null;
    
        StringBuilder stringbuffer = new StringBuilder(40);
        for ( int i1 = 3; i1 >= 0; i1-- )
        {
            stringbuffer.append(Integer.toHexString(abyte0[i1] >> 4 & 0xf).toUpperCase());
            stringbuffer.append(Integer.toHexString(abyte0[i1] & 0xf).toUpperCase());
        }

        stringbuffer.append('-');
        for ( int j1 = 5; j1 >= 4; j1-- )
        {
            stringbuffer.append(Integer.toHexString(abyte0[j1] >> 4 & 0xf).toUpperCase());
            stringbuffer.append(Integer.toHexString(abyte0[j1] & 0xf).toUpperCase());
        }

        stringbuffer.append('-');
        for ( int k1 = 7; k1 >= 6; k1-- )
        {
            stringbuffer.append(Integer.toHexString(abyte0[k1] >> 4 & 0xf).toUpperCase());
            stringbuffer.append(Integer.toHexString(abyte0[k1] & 0xf).toUpperCase());
        }

        for ( int i2 = 8; i2 < abyte0.length; i2++)
        {
            if ( i2 == 8 || i2 == 10 )
                stringbuffer.append('-');
            stringbuffer.append(Integer.toHexString(abyte0[i2] >> 4 & 0xf).toUpperCase());
            stringbuffer.append(Integer.toHexString(abyte0[i2] & 0xf).toUpperCase());
        }

        return stringbuffer.toString();
    }

	/**
	 * Returns the row data. Added for union functionality
	 * in query of queries
	 */
	public List<List<cfData>> getTableRows(){
		return tableRows;
	}
	
	
	public cfData duplicate(){
		List<List<cfData>> copiedTableRows = new ArrayList<List<cfData>>();
		cfQueryResultData copiedQuery = new cfQueryResultData( (cfQueryResultSetMetaData)rsmd.clone(), querySource );
		
		for ( int i = 0; i < tableRows.size(); i++ ){
			List<cfData> nextRow = tableRows.get( i );
			
			int nextRowLen = nextRow.size();
			List<cfData> nextRowCopy = new ArrayList<cfData>(nextRowLen);
			
			for ( int j = 0; j < nextRowLen; j++ ){
				cfData nextItemCopy = nextRow.get( j ).duplicate();
				if ( nextItemCopy == null ){
					return null;
				}
				nextItemCopy.setQueryTableData( copiedTableRows, j+1 );
				nextRowCopy.add( nextItemCopy );
			}
			
			copiedTableRows.add( nextRowCopy );
		}
		
		
    copiedQuery.setQueryData( copiedTableRows );
		return copiedQuery;
	}
	
	//-----------------------------------------------------
	//--[ Interface Operations
	//-----------------------------------------------------
	
	public void reset(){
		CURRENTROW	= 0;
		rowCursor		= null;
	}

	//--[ This is call to actually run the query
	public void runQuery( cfSession _Session ) throws cfmRunTimeException {}
   
  //--[ Called to retrieve each row
  public boolean nextRow() throws cfmRunTimeException{
		CURRENTROW++;
		if ( CURRENTROW > tableRows.size() )
			return false;
		else if ( isGrouped() ){
			return setNextGroupRow();
		}else{
			rowCursor = tableRows.get( CURRENTROW - 1 );
			return true;
		}
	}

  public int getCurrentRow() {
    return CURRENTROW;
  }

  public boolean setCurrentRow( int _row ) {
    if ( _row > tableRows.size() || _row <= 0 ){
      return false;
    }else{
      CURRENTROW = _row;
      rowCursor   = tableRows.get( CURRENTROW - 1 );
      return true;
    }
  }

 	//--[ Called to allow any clean up of the query
 	public void finishQuery(){
		if ( tableRows.size() > 0 ){
			CURRENTROW 	= 1;
			rowCursor		= tableRows.get( CURRENTROW - 1 );
		}
	}

	//-----------------------------------------------------
	//--[ GroupBy operators
	//-----------------------------------------------------

	private boolean setNextGroupRow() throws cfmRunTimeException {
		if ( CURRENTROW == 1 ){
			//--[ The first row has to be returned
			rowCursor	= tableRows.get( CURRENTROW - 1 );
			return true;
		} else {
			int groupColumn = getGroupColumn();
			boolean caseSensitive = isGroupCaseSensitive();
			int previousRow = CURRENTROW - 1;
      
			//--[ Lower case if this group is case insensitive
			String lastColumn = getCell( previousRow, groupColumn ).getString();
			if ( !caseSensitive )	lastColumn = lastColumn.toLowerCase();
			
      //--[ check the remaining rows searching for the next row where the 
      //    all the group by columns bar the one at the top of the group by
      //    column stack match
			for ( int RowX=CURRENTROW; RowX <= tableRows.size(); RowX++ ){ 
				String thisColumn	= getCell( RowX, groupColumn ).getString();
				if ( !caseSensitive )	thisColumn = thisColumn.toLowerCase();
				
        CURRENTROW = RowX;
        rowCursor = tableRows.get( CURRENTROW - 1 );
        
				if ( lastColumn.compareTo( thisColumn ) != 0 ) {
					
					// if previous group columns match return true, otherwise false
					for ( int i = 0; i < getGroupColumns().size(); i++ ) {
						int column = getGroupColumns().elementAt( i ).intValue();
						if ( column != groupColumn ) {
							caseSensitive = getGroupCaseSensitive().elementAt( i ).booleanValue();
							if ( !isCurrentRowInGroup( previousRow, column, caseSensitive ) )
								return false;
						}
					}
					return true;
				}else if ( getGroupColumns().size() > 1 ){
          //--[ we know that the current group column matches but if there
          //    are other group columns then they should match too
          for ( int i = 0; i < getGroupColumns().size(); i++ ) {
            int column = getGroupColumns().elementAt( i ).intValue();
            caseSensitive = getGroupCaseSensitive().elementAt( i ).booleanValue();
            if ( !isCurrentRowInGroup( previousRow, column, caseSensitive ) ){
              return false;
            }
          }
        }
			}
			
			return false;
		}
	}
	
	public void startGroupOutput(){
		groupCurrentRow = CURRENTROW;
		CURRENTROW -= 1;
	}
	
	public void endGroupOutput(){
		CURRENTROW	= groupCurrentRow;
		rowCursor	= tableRows.get( CURRENTROW - 1 );
	}
	
	public boolean nextRowInGroup() throws cfmRunTimeException {
		CURRENTROW++;
		if ( CURRENTROW > tableRows.size() )
			return false;
	
		rowCursor = tableRows.get( CURRENTROW - 1 );
		
		//--[ Check the last one to see || (CURRENTROW+1) > tableRows.size()
		if ( (CURRENTROW-1) == 0 || CURRENTROW == groupCurrentRow )
			return true;
						
    for( int i=0; i < groupColumns.size(); i++ ){
  		if (!isCurrentRowInGroup( groupColumns.elementAt(i).intValue(), isGroupCaseSensitive() ))
        return false;
    }
    return true;
	}

  private boolean isCurrentRowInGroup( int groupColumn, boolean caseSensitive ) throws cfmRunTimeException {
    return isCurrentRowInGroup( CURRENTROW-1, groupColumn, caseSensitive );
  }
  
	private boolean isCurrentRowInGroup( int _row, int groupColumn, boolean caseSensitive ) throws cfmRunTimeException {
		String thisColumn = getCell( CURRENTROW, groupColumn ).getString();
		if ( !caseSensitive )	thisColumn = thisColumn.toLowerCase();

		String lastColumn = getCell( _row, groupColumn ).getString();
		if ( !caseSensitive )	lastColumn = lastColumn.toLowerCase();

		if ( thisColumn.compareTo( lastColumn ) == 0 )
			return true;
		else
			return false;
	}
	
	public void setGroupedByEnabled( boolean enabled ) {
		groupEnabled = enabled;
	}
	
	public final boolean isGrouped(){
		return ( groupEnabled && !getGroupColumns().empty() );
	}
	
	private int getGroupColumn() {
		return getGroupColumns().peek().intValue();
	}
	private boolean isGroupCaseSensitive() {
		return getGroupCaseSensitive().peek().booleanValue();
	}
	
	public void setGroupBy( String column, boolean caseSensitive ) throws cfmRunTimeException {
		int groupColumn = rsmd.getColumnIndex(column);
		if ( groupColumn == 0 ) {
			throw new cfmRunTimeException( catchDataFactory.generalException( "errorCode.sqlError", 
																			  "sql.invalidColumn", 
																			  new String[]{column} ) );
		}
		getGroupColumns().push( new Integer( groupColumn ) );
		getGroupCaseSensitive().push( new Boolean( caseSensitive ) );
		groupEnabled = true;
	}
	
	public void removeGroupBy() {
		getGroupColumns().pop();
		getGroupCaseSensitive().pop();
		relative( -1 );
	}
	
	//-----------------------------------------------------
	//--[ Column Operations
	//-----------------------------------------------------
	
	public String[] getColumnNames(){	return rsmd.getColumnNames();	} // added to fix bug #3009
	
	public String[] getColumnList(){	return rsmd.getColumnNames();	}
	
	public String getColumns(){
		String[] columnList = rsmd.getColumnNames();
		java.util.Arrays.sort( columnList, new ColumnNameComparator() );
		
		String tmp = "";
		for ( int r = 0; r < columnList.length; r++ ){
			tmp += columnList[r];
			if ( r < columnList.length-1 )
				tmp += ",";
		}

		return tmp;
	}
	
	public int[] getColumnTypes() { return rsmd.getColumnTypes(); }
	
	private class ColumnNameComparator implements Comparator<String> {

		public int compare( String o1, String o2 ) {
			return o1.compareToIgnoreCase( o2 );
		}
	}
	
	private int addColumn( String columnName ){
		return rsmd.addColumn( columnName );
	}

	private int addColumn( String columnName, int type ){
		return rsmd.addColumn( columnName, type );
	}

	public int getNoColumns(){ return rsmd.getColumnCount(); }
	public int getNoRows(){ return tableRows.size(); }
	
	public int getColumnIndexCF( String columnName ){
		return rsmd.getColumnIndex( columnName );
	}
	
	
	public void deleteColumn( String columnName ){
		int columnIndex = rsmd.getColumnIndex( columnName );
		if ( columnIndex == 0 ) return;
		
		// Delete the data
		Iterator<List<cfData>>	it	= tableRows.iterator();
		while ( it.hasNext() ){
			List<cfData>	row = it.next();
			row.remove( columnIndex-1 );
		}
		
		// Delete the meta data
		rsmd.deleteColumn(columnName);
	}
	
	
	//-----------------------------------------------------
	//--[ Table Adjustment operations
	//-----------------------------------------------------

	public void populateQuery( List<Map<String, cfData>> listOfHashMaps ){
		tableRows = new ArrayList<List<cfData>>( listOfHashMaps.size() );
		List<cfData> rowData;
		Map<String, cfData>	rowHash;
		String key;
		int columnIndx;
		cfData cellData;
	
		Iterator<Map<String, cfData>> iter = listOfHashMaps.iterator();
		while ( iter.hasNext() )
		{
			rowHash	= iter.next();
			rowData	= new ArrayList<cfData>();
			for ( int c=0; c < rsmd.getColumnCount(); c++ )	rowData.add( null );
			
			Iterator<String> iter2 = rowHash.keySet().iterator();
			while ( iter2.hasNext() )
			{
				key = iter2.next();
				columnIndx	= rsmd.getColumnIndex(key);
				if ( columnIndx == 0 )	continue;
				
				Object o = rowHash.get(key);
				if( o instanceof cfData ) {
					cellData = getUniqueCellData( (cfData)o );
					cellData.setQueryTableData( tableRows, columnIndx );
				} else if ( o != null ) {
					cellData = new cfStringData( o.toString() );
					cellData.setQueryTableData( tableRows, columnIndx );
				} else {
					cellData = getNull(columnIndx);
				}
				rowData.set( columnIndx - 1, cellData );
			}
			
			tableRows.add( rowData );
		}
		
		if ( tableRows.size() > 0 ){
			CURRENTROW	= 1;
			rowCursor	= tableRows.get( 0 );
		}
	}
	
	private static cfData getUniqueCellData( cfData cellData ) {
		// we need to copy booleans to avoid setQueryTableData() being
		// called on static cfData instances
		byte dataType = cellData.getDataType();
		if ( dataType == cfData.CFBOOLEANDATA ) {
			return cellData.duplicate();
		} else if ( dataType == cfData.CFNULLDATA ) {
			return ((cfNullData)cellData).getDbNull();
		}
		return cellData;
	}
	
	// return 1-based column number
	public int addColumnData( String columnName, cfArrayData colData, Integer _type ) throws cfmRunTimeException {
		if ( rsmd.getColumnIndex(columnName) > 0 ){
     	throw new cfmRunTimeException( catchDataFactory.generalException( "errorCode.sqlError", 
																		  "sql.duplicateColumn", 
																		  new String[]{columnName} ) );
		}
			
		if ( colData.size() > tableRows.size() ){
			int noToInsert = (colData.size()-tableRows.size());
			addRow( noToInsert );
		}
        
		int columnNo;
		if ( _type != null ){
			columnNo = addColumn( columnName, _type.intValue() );
		}else{
			columnNo = addColumn( columnName );
		}
		
		//--[ Run through the list of tableRows
		List<cfData> rowData;
		for ( int rows=0; rows < tableRows.size(); rows++ ){
			rowData		= tableRows.get( rows );

      
			if ( rows < colData.size() ){
        cfData nextColData = colData.getElement( rows + 1 );
        if ( nextColData == null || nextColData.getDataType() == cfData.CFNULLDATA ){
          nextColData = getNull( columnNo );
        }else{
          nextColData  = nextColData.duplicate();
          nextColData.setQueryTableData( tableRows, columnNo );
        }
				rowData.add( nextColData );
      }else{
				rowData.add( getNull( columnNo ) );
      }
		}
        
		return columnNo;
	}		
        
	
	public void deleteRow( int rowNo ){
		//-- This is used by the QueryDelete() CFML expression
		//-- The rowNo is 1-based;
		if ( rowNo > tableRows.size() )
			return;
		
		tableRows.remove( rowNo - 1 );
		
		//-- Reset the pointers if they are out of sync
		LASTROW	= tableRows.size();
		if ( CURRENTROW == 0 || CURRENTROW > tableRows.size() ){
			CURRENTROW	= 1;
			if ( LASTROW == 0 ){
				rowCursor = null;
			}else{
				rowCursor		= tableRows.get( 0 );
			}
		}
	}
	
	public void addRow( int noRows ){
		int numCols = rsmd.getColumnCount();
		for ( int x=0; x < noRows; x++ ){
			List<cfData> rowData = new ArrayList<cfData>( numCols );
			for ( int r = 0; r < numCols; r++ ){
				cfNullData cellData = new cfNullData();
				cellData.setDBNull( true );
				cellData.setQueryTableData( tableRows, r+1 );
				rowData.add( cellData );
			}
			tableRows.add( rowData );
		}
			
		//--[ Position some of the cursors
		LASTROW	= tableRows.size();
		if (tableRows.size() > 0)
		{
			if (CURRENTROW == 0)
			{
				CURRENTROW = 1;
				rowCursor = tableRows.get(0);
			}
		}
	}
		
	public cfData getCell( int rowNo, int colNo ) {
		return getCell( rowNo, colNo, true );
	}

  public cfData getCell( int rowNo, int colNo, boolean _convertNulls ) {
    if ( rowNo > tableRows.size() || colNo > rsmd.getColumnCount() )
      return null;
    
    cfData tmp = (cfData)tableRows.get( rowNo-1 ).get( colNo-1 );
    return ( ( tmp == null ) || ( tmp.getDataType() == cfData.CFNULLDATA && _convertNulls ) ? new cfStringData( "" ) : tmp );
  }

	public boolean setCell( int rowNo, String columnName, cfData cellData ){
		int columnIndex	= rsmd.getColumnIndex( columnName );
		if ( columnIndex == 0 )	return false;
		return setCell( rowNo, columnIndex, cellData );
	}

	public boolean setCell( int rowNo, int colNo, cfData _cellData ){
		List<cfData> rowData = tableRows.get( rowNo - 1 );
		cfData uniqueCellData = getUniqueCellData( _cellData );
		rowData.set( colNo - 1, uniqueCellData );
		uniqueCellData.setQueryTableData( this.tableRows, colNo );

		return true;
	}

	/**
	 * Set the cell value for the current row.
	 */
	public boolean setCell( String columnName, cfData cellData ) {
		int columnIndex	= rsmd.getColumnIndex( columnName );
		if ( columnIndex == 0 )	return false;
		return setCell( columnIndex, cellData );
	}

	public boolean setCell( int colNo, cfData cellData ) {
		if ( tableRows.size() == 0 ) {
			return false;
		}
		if ( ( CURRENTROW > 0 ) && ( CURRENTROW <= LASTROW ) ) {
			return setCell( CURRENTROW, colNo, cellData );
		} else {
			return setCell( 1, colNo, cellData );
		}
	}


  //-----------------------------------------------------
  //--[ Methods for dealing with ValueList() and QuotedValueList()
  //--[ These are marked static, as they placed here as convient methods
  //--[ to ensure all related functionality remains in this class.
  //-----------------------------------------------------

  public static int getNoRows( List<List<cfData>> tableData ){
    return tableData.size();
  }


  public static int getNoColumns( List<List<cfData>> tableData ){
    List<cfData> rowData = tableData.get(0);
    
    if (rowData == null)
      return -1;
    
    return rowData.size();
  }

  public static boolean setCellData(List<List<cfData>> tableData, int rowNo, int colNo, cfData _cellData ){
    if ( rowNo > tableData.size() )
      return false;
    
    //-- Gets the row data
    List<cfData> rowData = tableData.get(rowNo-1);
    if ( colNo > rowData.size() )
      return false;

    cfData uniqueCellData = getUniqueCellData( _cellData );
    rowData.set( colNo - 1, uniqueCellData );
    uniqueCellData.setQueryTableData( tableData, colNo );
    return true;
  }

  public static cfData getCellData( List<List<cfData>> tableData, int rowNo, int colNo ){
    if ( rowNo > tableData.size() )
      return null;
    
    //-- Gets the row data
    List<cfData> rowData = tableData.get(rowNo-1);
    if ( colNo > rowData.size() )
      return null;
      
    return rowData.get(colNo-1);
  }
  
  /*
   * Method returns the appropriate cfNullData for the given column ensuring that
   * there is only one instance of cfNullData created per column
   */
  public cfNullData getNull( int _colno ){
    cfNullData nulld = null;
    if ( nulldatas.size() < _colno ){
      nulldatas.setSize( _colno );
    }else{
      nulld = nulldatas.elementAt( _colno-1 );
    }

    if ( nulld == null ){
      nulld = new cfNullData().setDBNull( true );
      nulld.setQueryTableData( tableRows, _colno );
      nulldatas.setElementAt( nulld, _colno-1 );
    }
    
    return nulld;
  }
  
	//-----------------------------------------------------
	//--[ Sorting Methods
	//-----------------------------------------------------

	/**
	 * Decides what type of sort to carry out on this cfArrayData.
	 *
	 * @param column the column on which to sort
	 * @param type the kind of sort to carry out, numeric, text or textnocase.
	 * @param direction the order to do the sort, asc or desc.
	 */
	public void sort( String _column, String _type, String _direction ) throws cfmRunTimeException{
		reset();
		
		if ( _type.equalsIgnoreCase("numeric") ){
			if ( _direction.equalsIgnoreCase("asc") ){
				Collections.sort( tableRows, new NumericAscComparator( _column ) );
			}else{
				Collections.sort( tableRows, new NumericDescComparator( _column ) );
			}  
		}else if ( _type.equalsIgnoreCase("text") ){
		    if ( _direction.equalsIgnoreCase("asc") ){
				Collections.sort( tableRows, new TextAscComparator( _column ) );
	  	  	}else{
				Collections.sort( tableRows, new TextDescComparator( _column ));
			}  
		}else if ( _type.equalsIgnoreCase("textnocase") ){
			if ( _direction.equalsIgnoreCase("asc") ){
				Collections.sort( tableRows, new TextNoCaseAscComparator( _column ) );
	  	  	}else{
	  	  		Collections.sort( tableRows, new TextNoCaseDescComparator( _column ));
			}  
		}
		// reset the row cursor to the start of the newly ordered query data
		reset();
		nextRow();
	}
	
	
	/**
	 * Sorts the query according to a list of columns to sort on. 
	 * NB. This is designed to work for the ORDER BY clause in query of queries
	 *
	 * @param _columnToSortBy a list of orderByCols that determine the list of columns to
	 * order the query by.
	 */
	public void sort( List<orderByCol> _columnsToSortBy ) throws cfmRunTimeException{
		Collections.sort( tableRows, new MultiColumnComparator( _columnsToSortBy ) );
		// reset the row cursor to the start of the newly ordered query data
		reset();
		nextRow();
	}// sort
	
	
	//*************INNER CLASS COMPARATORS for sorting *************
	/** Note this is very bad OO design. It is purely for performance reasons
	*   that it has been implemented this way.
	*/
	
	class NumericDescComparator implements Comparator<List<cfData>> {
		String colName;
		
		NumericDescComparator( String _col ) {
			colName = _col;
		}
		
		public int compare( List<cfData> o1, List<cfData> o2 ) {
			try {
				if ( o1.get( rsmd.getColumnIndex( colName ) - 1 ).getDouble() >
						o2.get( rsmd.getColumnIndex( colName ) - 1 ).getDouble() )
					return -1;
				else if(o1.get( rsmd.getColumnIndex( colName ) - 1 ).getDouble() <
						o2.get( rsmd.getColumnIndex( colName ) - 1 ).getDouble() )
					return 1;
				else
					return 0;
			} catch ( dataNotSupportedException E ) {
				return 0;
			}
		}
	}// NumericDescComparator
	
	class NumericAscComparator implements Comparator<List<cfData>> {
		String colName;

		NumericAscComparator( String _col ) {
			colName = _col;
		}

		public int compare( List<cfData> o1, List<cfData> o2 ) {
			try {
				if ( o1.get( rsmd.getColumnIndex( colName ) - 1 ).getDouble() <
						o2.get( rsmd.getColumnIndex( colName ) - 1 ).getDouble() )
					return -1;
				else if( o1.get( rsmd.getColumnIndex( colName ) - 1 ).getDouble() >
						o2.get( rsmd.getColumnIndex( colName ) - 1 ).getDouble() )
					return 1;
				else
					return 0;
			} catch ( dataNotSupportedException E ) {
				return 0;
			}
		}// compare()
	}// NumericAscComparator

	class TextAscComparator implements Comparator<List<cfData>> {
		String colName;

		TextAscComparator( String _col ) {
			colName = _col;
		}// TextAscComparator()

		public int compare( List<cfData> o1, List<cfData> o2 ) {
			try {
				return o1.get( rsmd.getColumnIndex( colName ) - 1 ).getString().compareTo(
								o2.get( rsmd.getColumnIndex( colName ) - 1 ).getString() );
			} catch ( dataNotSupportedException E ) {
				return 0;
			}
		}// compare()
	}// TextAscComparator

	class TextDescComparator implements Comparator<List<cfData>> {
		String colName;

		TextDescComparator( String _col ) {
			colName = _col;
		}// TextDescComparator()

		public int compare( List<cfData> o1, List<cfData> o2 ) {
			try {
				return ( o2.get( rsmd.getColumnIndex( colName ) - 1 ).getString().compareTo(
						o1.get( rsmd.getColumnIndex( colName ) - 1 ).getString() ) );
			} catch ( dataNotSupportedException E ) {
				return 0;
			}
		}// compare()
	}// TextDescComparator

	class TextNoCaseAscComparator implements Comparator<List<cfData>> {
		String colName;

		TextNoCaseAscComparator( String _col ) {
			colName = _col;
		}// TextNoCaseAscComparator()

		public int compare( List<cfData> o1, List<cfData> o2 ) {
			try {
				return o1.get( rsmd.getColumnIndex( colName ) - 1 ).getString().compareToIgnoreCase( 
														o2.get( rsmd.getColumnIndex( colName ) - 1 ).getString() );
			} catch ( dataNotSupportedException E ) {
				return 0;
			}
		}// compare()
	}// TextNoCaseAscComparator

	class TextNoCaseDescComparator implements Comparator<List<cfData>> {
		String colName;

		TextNoCaseDescComparator( String _col ) {
			colName = _col;
		}// TextNoCaseDescComparator()

		public int compare( List<cfData> o1, List<cfData> o2 ) {
			try {
				return o2.get( rsmd.getColumnIndex( colName ) - 1 ).getString().compareToIgnoreCase(
														o1.get( rsmd.getColumnIndex( colName ) - 1 ).getString() );
			} catch ( dataNotSupportedException E ) {
				return 0;
			}
		}// compare()
	}// TextNoCaseDescComparator

	class MultiColumnComparator implements Comparator<List<cfData>> {
		orderByCol[] colList;

		MultiColumnComparator( List<orderByCol> _cols ) {
			colList = new orderByCol[ _cols.size() ];
			for ( int i = 0; i < colList.length; i++ ) {
				colList[ i ] = _cols.get( i );
			}
		}// MultiColumnComparator()

		public int compare( List<cfData> o1, List<cfData> o2 ) {
			orderByCol obCol;
			cfData dataItem1, dataItem2; // the components of the 2 rows to compare

			// loop thru the colList returning if o1 and o2 are not equal on a column
			for ( int i = 0; i < colList.length; i++ ) {
				obCol = colList[ i ];

				// get columns
				if ( obCol.isIndex() ) {
					dataItem1 = o1.get( obCol.getIndex() - 1 );
					dataItem2 = o2.get( obCol.getIndex() - 1 );
				} else {
					dataItem1 = o1.get( rsmd.getColumnIndex( obCol.getColName() ) - 1 );
					dataItem2 = o2.get( rsmd.getColumnIndex( obCol.getColName() ) - 1 );
				}

				int compResult = cfData.compare( dataItem1, dataItem2 );
				if ( compResult != 0 ) { // if items not equal
					if ( obCol.isAscending() ) {
						return compResult;
					} else {
						return -1 * compResult; // return the reverse of the comparison result
					}
				}
			}
			return 0;
		}// compare()
	}// MultiColumnComparator
	
	
	// -----------------------------------------------------
	//--[ Core JTags methods
	//-----------------------------------------------------

	private static final FastMap<String, Integer> keyMap = new FastMap<String, Integer>( FastMap.CASE_INSENSITIVE );

	static {
		keyMap.put( "recordcount", new Integer( 0 ) );
		keyMap.put( "currentrow", new Integer( 1 ) );
		keyMap.put( "columnlist", new Integer( 2 ) );
	}

	/**
	 * WARNING! The performance of this method is critical to overall system
	 * performance. Do not make any changes to this method without doing
	 * before-and-after timing measurements to make sure you have not decreased
	 * performance.
	 */
	public cfData getData( String _key ) {
		if ( getRowCursor() != null ) {
			int columnIndx = rsmd.getColumnIndex( _key );
			if ( columnIndx > 0 ) {
				cfData cellData = rowCursor.get( columnIndx-1 );
				if ( cellData == null ) {
					cellData = new cfStringData( "" );
					cellData.setQueryTableData( tableRows, columnIndx );
				}
				return cellData;
			}
		}

		Integer index = keyMap.get( _key );
		if ( index != null ) {
			switch( index.intValue() ) {
				case 0 : return new cfNumberData( tableRows.size() );
				case 1 : return new cfNumberData( CURRENTROW );
				case 2 : return new cfStringData( getColumns() );
				default: break;
			}
		}
		
		if ( ( getSize() == 0 ) && rsmd.getColumnIndex( _key ) > 0 )
			return new cfStringData( "" );

		return null;
	}
	
	public cfData getData( cfData arrayIndex ) throws cfmRunTimeException {
		return getData( arrayIndex.getString() );
	}
	
	public int getSize(){
		return tableRows.size();
	}
	

	public cfStructData getRowAsStruct() {
		cfStructData sd	= new cfStructData();
		
		sd.setData("currentrow", CURRENTROW );
		sd.setData("recordcount", tableRows.size() );
		List<cfData> rowData	= tableRows.get( CURRENTROW-1 );
		
		for ( int cols=0; cols < rsmd.getColumnCount(); cols++ )	{
			try {
				sd.setData( rsmd.getColumnName( cols + 1 ), rowData.get(cols) );
			} catch (SQLException e) {
				sd.setData( "Column"+cols, rowData.get(cols-1) );
			}
		}
		
		return sd;
	}
	
	//-----------------------------------------------------
	//--[ Output methods
	//-----------------------------------------------------
	
	public String toString(){
		StringBuilder buffer = new StringBuilder( 256 );
		buffer.append( "{RESULT: " );
		buffer.append( "Records=" + getNoRows() + "," );
		buffer.append( "Columns=" );
		for ( int cols=1; cols <= rsmd.getColumnCount(); cols++ )	{
			try {
				buffer.append( rsmd.getColumnName( cols ) );
			} catch ( SQLException e ){
				buffer.append( e.getMessage() );
			}
			if ( cols < rsmd.getColumnCount() )
				buffer.append( "," );
		}
		buffer.append( "}" );
	    
		return buffer.toString();
	}

  public void dump( java.io.PrintWriter out  ){
    dump( out, "", cfDUMP.TOP_DEFAULT );
  }
    
    
	public void dump( java.io.PrintWriter out, String _label, int _top  ) {
		
		out.write( "<table class='cfdump_table_query'>" );
		out.write( "<th class='cfdump_th_query' colspan='2'>" );
    if ( _label.length() > 0 ) out.write( _label + " - " );    
    out.write( "query [short version]</th>" );
				

		out.write( "<tr><td class='cfdump_td_query'>Query Source:</TD><TD class='cfdump_td_value' >"+ querySource +"</td></tr>" );

		out.write( getExtraInfo( false ) );

		out.write( "<TR><TD class='cfdump_td_query'>Records:</TD>" );
		out.write( "<TD class='cfdump_td_value' >"+ getNoRows() +" </TD>");

		out.write( "<tr><td class='cfdump_td_query'>Columns:</td>" );
		
		out.write("<td class='cfdump_td_value'>");
		for ( int breakCnt=0, cols=1; cols <= rsmd.getColumnCount(); cols++, breakCnt++ ) {
			try {
				out.write( rsmd.getColumnName( cols ) );
			} catch ( SQLException e ) {
				out.write( e.getMessage() );
			}
			if(breakCnt == 6 ) {
				out.write("<br>");
				breakCnt=0;
			}
			else if(cols<rsmd.getColumnCount() ) out.write( ", " );
		}
		out.write( "</td></tr>" );
		out.write( "</table>");
	}
	
	
  public void dumpLong( java.io.PrintWriter out  ){
    dumpLong( out, "", cfDUMP.TOP_DEFAULT );
  }
  
	public void dumpLong( java.io.PrintWriter out, String _label, int _top  ){
		out.write( "<table class='cfdump_table_query'>" );
		out.write( "<th class='cfdump_th_query' colspan='" + ( rsmd.getColumnCount() + 1 ) + "'>" );
    if ( _label.length() > 0 ) out.write( _label + " - " );    
    
    out.write( "query [long version]</th>" );
		
		out.write( "<tr>");
		out.write( "<td class='cfdump_td_query'>&nbsp;</td>" );
		
		for ( int cols=1; cols <= rsmd.getColumnCount(); cols++ ) {
			out.write( "<td class='cfdump_td_query'>" );
			try {
				out.write( rsmd.getColumnName( cols ) );
			} catch ( SQLException e ) {
				out.write( e.getMessage() );
			}
			out.write( "</td>" );
		}
		out.write( "</tr>");
		
		// Print out the data part of the table
		cfData cellData;
		List<cfData> rowData;
		int maxRows = ( _top < tableRows.size() ? _top : tableRows.size() );
		
		for ( int rows=0; rows < maxRows; rows++ )
		{
			out.write( "<tr>");
			out.write( "<td class='cfdump_td_query'>" + ( rows + 1 ) + "</td>" );
			rowData	= tableRows.get( rows );
			for ( int cols=0; cols < rsmd.getColumnCount(); cols++ )
			{
				out.write( "<td class='cfdump_td_value'>" );
				try { 
					cellData = (cfData)rowData.get( cols );
					if ( cellData != null )
						cellData.dump(out,"",_top);
					else
						out.write( "&nbsp;" );
				} catch(Exception ignoreIt){out.write( "&nbsp;" );}
				out.write( "</td>");
			}
			out.write( "</tr>");
		}
				
		// Print out the footer information
		out.write( "<tr><td class='cfdump_td_query'>Query Source:</TD><TD class='cfdump_td_value' COLSPAN="+ rsmd.getColumnCount() +">"+ querySource +"</td></tr>" );
		out.write( getExtraInfo( true ) );
			
    out.write( "</table>" );
	}

	public void dumpWDDX( int version, java.io.PrintWriter out ){
		
		//--[ Create the header tag
    if ( version > 10 )
      out.write( "<r c='" );
    else
      out.write( "<recordset rowCount='" );
    
		out.write( tableRows.size() + "" );
    
    if ( version > 10 )
      out.write( "' n='" );
    else
      out.write( "' fieldNames='" );
    
		int numCols = rsmd.getColumnCount();
    for ( int cols=1; cols <= numCols; cols++ ){
      try {
      	out.write( rsmd.getColumnName( cols ) );
      } catch ( SQLException e ) {
      	out.write( e.getMessage() );
      }
      if( cols < numCols )
        out.write( "," );
    }
		out.write( "'>" );
		
		//--[ Generate the body
		cfData 		cellData;
		List<cfData> rowData;
    for ( int cols=1; cols <= numCols; cols++ ){
      if ( version > 10 )
        out.write( "<f n='");
      else
        out.write( "<field name='");
        
			try {
				out.write( rsmd.getColumnName( cols ) );
			} catch ( SQLException e ) {
				out.write( e.getMessage() );
			}
			out.write( "'>" );

			for ( int rows=0; rows < tableRows.size(); rows++ ){			
				rowData		= tableRows.get( rows ); 
				cellData 	= (cfData)rowData.get( cols-1 );
				if ( cellData != null )
					cellData.dumpWDDX( version, out );
      }	
			
      if ( version > 10 )
        out.write( "</f>");
      else
        out.write( "</field>");
    }
    
    if ( version > 10 )    
      out.write( "</r>" );
    else
      out.write( "</recordset>" );
	}

	
	protected String getExtraInfo( boolean _isLong ){
		return "";
	}
	
	/**************************************************************************
	 * 
	 * 	The following methods implement the java.sql.Result interface. They're
	 * 	here to facility variable sharing with JSP pages. These methods return
	 * 	"natural" Java objects, not CFML variables.
	 * 
	 **************************************************************************/

	private boolean wasNull;
	
	private cfData getCell( int colNo )
	{
		if ( ( CURRENTROW < 1 ) || ( CURRENTROW > tableRows.size() ) ||
			( colNo < 1 ) || ( colNo > rsmd.getColumnCount() ) )
		{
			return null;
		}
		
		cfData data = (cfData)tableRows.get( CURRENTROW-1 ).get( colNo-1 );
		
		if ( ( data == null ) || ( data.getDataType() == cfData.CFNULLDATA ) ) {
			wasNull = true;
			return null;
		}
		
		wasNull = false;
		return data;
	}
	
	/**
	 * Moves the cursor down one row from its current position.
	 * A <code>ResultSet</code> cursor is initially positioned
	 * before the first row; the first call to the method
	 * <code>next</code> makes the first row the current row; the
	 * second call makes the second row the current row, and so on. 
	 *
	 * <P>If an input stream is open for the current row, a call
	 * to the method <code>next</code> will
	 * implicitly close it. A <code>ResultSet</code> object's
	 * warning chain is cleared when a new row is read.
	 *
	 * @return <code>true</code> if the new current row is valid; 
	 * <code>false</code> if there are no more rows 
	 * @exception SQLException if a database access error occurs
	 */
	public boolean next() throws SQLException {
		if ( getNoRows() == 0 )
			return false;
					
		return relative( 1 );
	}


	/**
	 * Releases this <code>ResultSet</code> object's database and
	 * JDBC resources immediately instead of waiting for
	 * this to happen when it is automatically closed.
	 *
	 * <P><B>Note:</B> A <code>ResultSet</code> object
	 * is automatically closed by the
	 * <code>Statement</code> object that generated it when
	 * that <code>Statement</code> object is closed,
	 * re-executed, or is used to retrieve the next result from a
	 * sequence of multiple results. A <code>ResultSet</code> object
	 * is also automatically closed when it is garbage collected.  
	 *
	 * @exception SQLException if a database access error occurs
	 */
	public void close() throws SQLException {
	}

	/**
	 * Reports whether
	 * the last column read had a value of SQL <code>NULL</code>.
	 * Note that you must first call one of the getter methods
	 * on a column to try to read its value and then call
	 * the method <code>wasNull</code> to see if the value read was
	 * SQL <code>NULL</code>.
	 *
	 * @return <code>true</code> if the last column value read was SQL
	 *         <code>NULL</code> and <code>false</code> otherwise
	 * @exception SQLException if a database access error occurs
	 */
	public boolean wasNull() throws SQLException {
		return wasNull;
	}
    
	//======================================================================
	// Methods for accessing results by column index
	//======================================================================

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>String</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public String getString(int columnIndex) throws SQLException {
		try {
			cfData data = getCell( columnIndex );
			if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
				return null;
			}
			return data.getString();
		} catch ( dataNotSupportedException e ) {
			throw new SQLException( "Could not convert value to string" );
		}
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>boolean</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>false</code>
	 * @exception SQLException if a database access error occurs
	 */
	public boolean getBoolean(int columnIndex) throws SQLException {
		try {
			cfData data = getCell( columnIndex );
			if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
				return false;
			}
			return data.getBoolean();
		} catch ( dataNotSupportedException e ) {
			throw new SQLException( "Could not convert value to boolean" );
		}
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public byte getByte(int columnIndex) throws SQLException {
		try {
			cfData data = getCell( columnIndex );
			if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
				return 0;
			}
			return (byte)data.getInt();
		} catch ( dataNotSupportedException e ) {
			throw new SQLException( "Could not convert value to byte" );
		}
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>short</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public short getShort(int columnIndex) throws SQLException {
		try {
			cfData data = getCell( columnIndex );
			if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
				return 0;
			}
			return (short)data.getInt();
		} catch ( dataNotSupportedException e ) {
			throw new SQLException( "Could not convert value to short" );
		}
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * an <code>int</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public int getInt(int columnIndex) throws SQLException {
		try {
			cfData data = getCell( columnIndex );
			if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
				return 0;
			}
			return data.getInt();
		} catch ( dataNotSupportedException e ) {
			throw new SQLException( "Could not convert value to int" );
		}
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>long</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public long getLong(int columnIndex) throws SQLException {
		try {
			cfData data = getCell( columnIndex );
			if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
				return 0;
			}
			return data.getLong();
		} catch ( dataNotSupportedException e ) {
			throw new SQLException( "Could not convert value to long" );
		}
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>float</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public float getFloat(int columnIndex) throws SQLException {
		try {
			cfData data = getCell( columnIndex );
			if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
				return 0;
			}
			return (float)data.getDouble();
		} catch ( dataNotSupportedException e ) {
			throw new SQLException( "Could not convert value to float" );
		}
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>double</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public double getDouble(int columnIndex) throws SQLException {
		try {
			cfData data = getCell( columnIndex );
			if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
				return 0;
			}
			return data.getDouble();
		} catch ( dataNotSupportedException e ) {
			throw new SQLException( "Could not convert value to double" );
		}
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.BigDecimal</code> in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param scale the number of digits to the right of the decimal point
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 * @deprecated
	 */
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return getBigDecimal( columnIndex ).setScale( scale, BigDecimal.ROUND_HALF_UP );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> array in the Java programming language.
	 * The bytes represent the raw values returned by the driver.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public byte[] getBytes(int columnIndex) throws SQLException {
		try {
			cfData data = getCell( columnIndex );
			if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
				return null;
			}
			if ( data.getDataType() == cfData.CFBINARYDATA ) {
				return ((cfBinaryData)data).getByteArray();
			}
			return data.getString().getBytes();
		} catch ( dataNotSupportedException e ) {
			throw new SQLException( "Could not convert value to byte[]" );
		}
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Date</code> object in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public java.sql.Date getDate(int columnIndex) throws SQLException {
		try {
			cfData data = getCell( columnIndex );
			if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
				return null;
			}
			return new java.sql.Date( data.getDateData().getLong() );
		} catch ( dataNotSupportedException e ) {
			throw new SQLException( "Could not convert value to java.sql.Date" );
		}
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Time</code> object in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public java.sql.Time getTime(int columnIndex) throws SQLException {
		try {
			cfData data = getCell( columnIndex );
			if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
				return null;
			}
			return new java.sql.Time( data.getDateData().getLong() );
		} catch ( dataNotSupportedException e ) {
			throw new SQLException( "Could not convert value to java.sql.Time" );
		}
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Timestamp</code> object in the Java programming language.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
		try {
			cfData data = getCell( columnIndex );
			if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
				return null;
			}
			return new java.sql.Timestamp( data.getDateData().getLong() );
		} catch ( dataNotSupportedException e ) {
			throw new SQLException( "Could not convert value to java.sql.Timestamp" );
		}
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a stream of ASCII characters. The value can then be read in chunks from the
	 * stream. This method is particularly
	 * suitable for retrieving large <char>LONGVARCHAR</char> values.
	 * The JDBC driver will
	 * do any necessary conversion from the database format into ASCII.
	 *
	 * <P><B>Note:</B> All the data in the returned stream must be
	 * read prior to getting the value of any other column. The next
	 * call to a getter method implicitly closes the stream.  Also, a
	 * stream may return <code>0</code> when the method
	 * <code>InputStream.available</code>
	 * is called whether there is data available or not.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return a Java input stream that delivers the database column value
	 * as a stream of one-byte ASCII characters;
	 * if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * as a stream of two-byte Unicode characters. The first byte is
	 * the high byte; the second byte is the low byte.
	 *
	 * The value can then be read in chunks from the
	 * stream. This method is particularly
	 * suitable for retrieving large <code>LONGVARCHAR</code>values.  The 
	 * JDBC driver will do any necessary conversion from the database
	 * format into Unicode.
	 *
	 * <P><B>Note:</B> All the data in the returned stream must be
	 * read prior to getting the value of any other column. The next
	 * call to a getter method implicitly closes the stream.  
	 * Also, a stream may return <code>0</code> when the method 
	 * <code>InputStream.available</code>
	 * is called, whether there is data available or not.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return a Java input stream that delivers the database column value
	 *         as a stream of two-byte Unicode characters;
	 *         if the value is SQL <code>NULL</code>, the value returned is 
	 *         <code>null</code>
	 *
	 * @exception SQLException if a database access error occurs
	 * @deprecated use <code>getCharacterStream</code> in place of 
	 *              <code>getUnicodeStream</code>
	 */
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a binary stream of
	 * uninterpreted bytes. The value can then be read in chunks from the
	 * stream. This method is particularly
	 * suitable for retrieving large <code>LONGVARBINARY</code> values.
	 *
	 * <P><B>Note:</B> All the data in the returned stream must be
	 * read prior to getting the value of any other column. The next
	 * call to a getter method implicitly closes the stream.  Also, a
	 * stream may return <code>0</code> when the method 
	 * <code>InputStream.available</code>
	 * is called whether there is data available or not.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return a Java input stream that delivers the database column value
	 *         as a stream of uninterpreted bytes;
	 *         if the value is SQL <code>NULL</code>, the value returned is 
	 *         <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return null;
	}


	//======================================================================
	// Methods for accessing results by column name
	//======================================================================

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>String</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public String getString( String columnName ) throws SQLException {
		return getString( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>boolean</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>false</code>
	 * @exception SQLException if a database access error occurs
	 */
	public boolean getBoolean( String columnName ) throws SQLException {
		return getBoolean( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public byte getByte( String columnName ) throws SQLException {
		return getByte( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>short</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public short getShort( String columnName ) throws SQLException {
		return getShort( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * an <code>int</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public int getInt( String columnName ) throws SQLException {
		return getInt( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>long</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public long getLong( String columnName ) throws SQLException {
		return getLong( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>float</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public float getFloat( String columnName ) throws SQLException {
		return getFloat( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>double</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 * @exception SQLException if a database access error occurs
	 */
	public double getDouble( String columnName ) throws SQLException {
		return getDouble( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.math.BigDecimal</code> in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @param scale the number of digits to the right of the decimal point
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 * @deprecated
	 */
	public BigDecimal getBigDecimal( String columnName, int scale ) throws SQLException {
		return getBigDecimal( findColumn( columnName ), scale );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> array in the Java programming language.
	 * The bytes represent the raw values returned by the driver.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public byte[] getBytes( String columnName ) throws SQLException {
		return getBytes( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Date</code> object in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public java.sql.Date getDate( String columnName ) throws SQLException {
		return getDate( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row  
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Time</code> object in the Java programming language.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; 
	 * if the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public java.sql.Time getTime( String columnName ) throws SQLException {
		return getTime( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Timestamp</code> object.
	 *
	 * @param columnName the SQL name of the column
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public java.sql.Timestamp getTimestamp( String columnName ) throws SQLException {
		return getTimestamp( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a stream of
	 * ASCII characters. The value can then be read in chunks from the
	 * stream. This method is particularly
	 * suitable for retrieving large <code>LONGVARCHAR</code> values.
	 * The JDBC driver will
	 * do any necessary conversion from the database format into ASCII.
	 *
	 * <P><B>Note:</B> All the data in the returned stream must be
	 * read prior to getting the value of any other column. The next
	 * call to a getter method implicitly closes the stream. Also, a
	 * stream may return <code>0</code> when the method <code>available</code>
	 * is called whether there is data available or not.
	 *
	 * @param columnName the SQL name of the column
	 * @return a Java input stream that delivers the database column value
	 * as a stream of one-byte ASCII characters.
	 * If the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code>.
	 * @exception SQLException if a database access error occurs
	 */
	public InputStream getAsciiStream( String columnName ) throws SQLException {
		return getAsciiStream( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a stream of two-byte
	 * Unicode characters. The first byte is the high byte; the second
	 * byte is the low byte.
	 *
	 * The value can then be read in chunks from the
	 * stream. This method is particularly
	 * suitable for retrieving large <code>LONGVARCHAR</code> values.
	 * The JDBC technology-enabled driver will
	 * do any necessary conversion from the database format into Unicode.
	 *
	 * <P><B>Note:</B> All the data in the returned stream must be
	 * read prior to getting the value of any other column. The next
	 * call to a getter method implicitly closes the stream.
	 * Also, a stream may return <code>0</code> when the method 
	 * <code>InputStream.available</code> is called, whether there 
	 * is data available or not.
	 *
	 * @param columnName the SQL name of the column
	 * @return a Java input stream that delivers the database column value
	 *         as a stream of two-byte Unicode characters.  
	 *         If the value is SQL <code>NULL</code>, the value returned 
	 *         is <code>null</code>.
	 * @exception SQLException if a database access error occurs
	 * @deprecated use <code>getCharacterStream</code> instead
	 */
	public InputStream getUnicodeStream( String columnName ) throws SQLException {
		return getUnicodeStream( findColumn( columnName ) );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a stream of uninterpreted
	 * <code>byte</code>s.
	 * The value can then be read in chunks from the
	 * stream. This method is particularly
	 * suitable for retrieving large <code>LONGVARBINARY</code>
	 * values. 
	 *
	 * <P><B>Note:</B> All the data in the returned stream must be
	 * read prior to getting the value of any other column. The next
	 * call to a getter method implicitly closes the stream. Also, a
	 * stream may return <code>0</code> when the method <code>available</code>
	 * is called whether there is data available or not.
	 *
	 * @param columnName the SQL name of the column
	 * @return a Java input stream that delivers the database column value
	 * as a stream of uninterpreted bytes; 
	 * if the value is SQL <code>NULL</code>, the result is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	public InputStream getBinaryStream( String columnName ) throws SQLException
	{
		return getBinaryStream( findColumn( columnName ) );
	}


	//=====================================================================
	// Advanced features:
	//=====================================================================

	/**
	 * Retrieves the first warning reported by calls on this 
	 * <code>ResultSet</code> object.
	 * Subsequent warnings on this <code>ResultSet</code> object
	 * will be chained to the <code>SQLWarning</code> object that 
	 * this method returns.
	 *
	 * <P>The warning chain is automatically cleared each time a new
	 * row is read.  This method may not be called on a <code>ResultSet</code>
	 * object that has been closed; doing so will cause an 
	 * <code>SQLException</code> to be thrown.
	 * <P>
	 * <B>Note:</B> This warning chain only covers warnings caused
	 * by <code>ResultSet</code> methods.  Any warning caused by
	 * <code>Statement</code> methods
	 * (such as reading OUT parameters) will be chained on the
	 * <code>Statement</code> object. 
	 *
	 * @return the first <code>SQLWarning</code> object reported or 
	 *         <code>null</code> if there are none
	 * @exception SQLException if a database access error occurs or this method is 
	 *            called on a closed result set
	 */
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	/**
	 * Clears all warnings reported on this <code>ResultSet</code> object.
	 * After this method is called, the method <code>getWarnings</code>
	 * returns <code>null</code> until a new warning is
	 * reported for this <code>ResultSet</code> object.  
	 *
	 * @exception SQLException if a database access error occurs
	 */
	public void clearWarnings() throws SQLException {
	}

	/**
	 * Retrieves the name of the SQL cursor used by this <code>ResultSet</code>
	 * object.
	 *
	 * <P>In SQL, a result table is retrieved through a cursor that is
	 * named. The current row of a result set can be updated or deleted
	 * using a positioned update/delete statement that references the
	 * cursor name. To insure that the cursor has the proper isolation
	 * level to support update, the cursor's <code>SELECT</code> statement 
	 * should be of the form <code>SELECT FOR UPDATE</code>. If 
	 * <code>FOR UPDATE</code> is omitted, the positioned updates may fail.
	 * 
	 * <P>The JDBC API supports this SQL feature by providing the name of the
	 * SQL cursor used by a <code>ResultSet</code> object.
	 * The current row of a <code>ResultSet</code> object
	 * is also the current row of this SQL cursor.
	 *
	 * <P><B>Note:</B> If positioned update is not supported, a
	 * <code>SQLException</code> is thrown.
	 *
	 * @return the SQL name for this <code>ResultSet</code> object's cursor
	 * @exception SQLException if a database access error occurs
	 */
	public String getCursorName() throws SQLException {
		throw new SQLException( "positioned update not supported" );
	}

	/**
	 * Retrieves the  number, types and properties of
	 * this <code>ResultSet</code> object's columns.
	 *
	 * @return the description of this <code>ResultSet</code> object's columns
	 * @exception SQLException if a database access error occurs
	 */
	public ResultSetMetaData getMetaData() throws SQLException {
		return rsmd;
	}

	/**
	 * <p>Gets the value of the designated column in the current row 
	 * of this <code>ResultSet</code> object as 
	 * an <code>Object</code> in the Java programming language.
	 *
	 * <p>This method will return the value of the given column as a
	 * Java object.  The type of the Java object will be the default
	 * Java object type corresponding to the column's SQL type,
	 * following the mapping for built-in types specified in the JDBC 
	 * specification. If the value is an SQL <code>NULL</code>, 
	 * the driver returns a Java <code>null</code>.
	 *
	 * <p>This method may also be used to read database-specific
	 * abstract data types.
	 *
	 * In the JDBC 2.0 API, the behavior of method
	 * <code>getObject</code> is extended to materialize  
	 * data of SQL user-defined types.  When a column contains
	 * a structured or distinct value, the behavior of this method is as 
	 * if it were a call to: <code>getObject(columnIndex, 
	 * this.getStatement().getConnection().getTypeMap())</code>.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return a <code>java.lang.Object</code> holding the column value  
	 * @exception SQLException if a database access error occurs
	 */
	public Object getObject(int columnIndex) throws SQLException {
		
		// Need to check for null first since some getXXX() methods like getLong() will
		// return 0 when the value is null.  In this case we need to return null instead of 0.
		cfData data = getCell( columnIndex );
		if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
			return null;
		}

		int type = rsmd.getColumnType( columnIndex );
		switch ( type )
		{		
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			return getString( columnIndex );
				
		case Types.NUMERIC:
		case Types.DECIMAL:
			return getBigDecimal( columnIndex );
				
		case Types.BIT:
			return new Boolean( getBoolean( columnIndex ) );
			
		case Types.TINYINT:
			return new Byte( getByte( columnIndex ) );
			
		case Types.SMALLINT:
			return new Short( getShort( columnIndex ) );
			
		case Types.INTEGER:
			return new Integer( getInt( columnIndex ) );

		case Types.BIGINT:
			return new Long( getLong( columnIndex ) );
			
		case Types.REAL:
			return new Float( getFloat( columnIndex ) );
			
		case Types.FLOAT:
		case Types.DOUBLE:
			return new Double( getDouble( columnIndex ) );
			
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			return getBytes( columnIndex );
				
		case Types.DATE:
			return getDate( columnIndex );
				
		case Types.TIME:
			return getTime( columnIndex );
				
		case Types.TIMESTAMP:
			return getTimestamp( columnIndex );
			
		case Types.CLOB :
			return getClob( columnIndex );
			
		case Types.BLOB :
			return getBlob( columnIndex );
			
		case Types.ARRAY :
			return getArray( columnIndex );
			
		case Types.REF :
			return getRef( columnIndex );

		case Types.OTHER:
			String columnName = rsmd.getColumnTypeName( columnIndex );
			if ( columnName != null )
			{
				if ( columnName.equals( "binary" ) )
					return getBytes( columnIndex );
				if ( columnName.equals( "varbinary" ) )
					return getBytes( columnIndex );
			}
				
		default:
			throw new SQLException( "getObject() does not support the java.sql.Types value " + type );
		}
	}

	/**
	 * <p>Gets the value of the designated column in the current row 
	 * of this <code>ResultSet</code> object as 
	 * an <code>Object</code> in the Java programming language.
	 *
	 * <p>This method will return the value of the given column as a
	 * Java object.  The type of the Java object will be the default
	 * Java object type corresponding to the column's SQL type,
	 * following the mapping for built-in types specified in the JDBC 
	 * specification. If the value is an SQL <code>NULL</code>, 
	 * the driver returns a Java <code>null</code>.
	 * <P>
	 * This method may also be used to read database-specific
	 * abstract data types.
	 * <P>
	 * In the JDBC 2.0 API, the behavior of the method
	 * <code>getObject</code> is extended to materialize  
	 * data of SQL user-defined types.  When a column contains
	 * a structured or distinct value, the behavior of this method is as 
	 * if it were a call to: <code>getObject(columnIndex, 
	 * this.getStatement().getConnection().getTypeMap())</code>.
	 *
	 * @param columnName the SQL name of the column
	 * @return a <code>java.lang.Object</code> holding the column value  
	 * @exception SQLException if a database access error occurs
	 */
	public Object getObject( String columnName ) throws SQLException {
		return getObject( findColumn( columnName ) );
	}

	//----------------------------------------------------------------

	/**
	 * Maps the given <code>ResultSet</code> column name to its
	 * <code>ResultSet</code> column index.
	 *
	 * @param columnName the name of the column
	 * @return the column index of the given column name
	 * @exception SQLException if the <code>ResultSet</code> object
	 * does not contain <code>columnName</code> or a database access error occurs
	 */
	public int findColumn( String columnName ) throws SQLException {
		return rsmd.getColumnIndex( columnName );
	}


	//--------------------------JDBC 2.0-----------------------------------

	//---------------------------------------------------------------------
	// Getters and Setters
	//---------------------------------------------------------------------

	/**
	 * Retrieves the value of the designated column in the current row 
	 * of this <code>ResultSet</code> object as a
	 * <code>java.io.Reader</code> object.
	 * @return a <code>java.io.Reader</code> object that contains the column
	 * value; if the value is SQL <code>NULL</code>, the value returned is
	 * <code>null</code> in the Java programming language.
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row 
	 * of this <code>ResultSet</code> object as a
	 * <code>java.io.Reader</code> object.
	 *
	 * @param columnName the name of the column
	 * @return a <code>java.io.Reader</code> object that contains the column
	 * value; if the value is SQL <code>NULL</code>, the value returned is
	 * <code>null</code> in the Java programming language
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public Reader getCharacterStream(String columnName) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a
	 * <code>java.math.BigDecimal</code> with full precision.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return the column value (full precision);
	 * if the value is SQL <code>NULL</code>, the value returned is
	 * <code>null</code> in the Java programming language.
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		try {
			cfData data = getCell( columnIndex );
			if ( data == null || data.getDataType() == cfData.CFNULLDATA ) {
				return null;
			}
			return new BigDecimal( data.getDouble() );
		} catch ( dataNotSupportedException e ) {
			throw new SQLException( "Could not convert value to BigDecimal" );
		}
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a
	 * <code>java.math.BigDecimal</code> with full precision.
	 *
	 * @param columnName the column name
	 * @return the column value (full precision);
	 * if the value is SQL <code>NULL</code>, the value returned is
	 * <code>null</code> in the Java programming language.
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 *
	 */
	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		return getBigDecimal( findColumn( columnName ) );
	}

	//---------------------------------------------------------------------
	// Traversal/Positioning
	//---------------------------------------------------------------------

	/**
	 * Retrieves whether the cursor is before the first row in 
	 * this <code>ResultSet</code> object.
	 *
	 * @return <code>true</code> if the cursor is before the first row;
	 * <code>false</code> if the cursor is at any other position or the
	 * result set contains no rows
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public boolean isBeforeFirst() throws SQLException {
		if ( ( getNoRows() == 0 ) || ( CURRENTROW != 0 ) )
			return false;
		
		return true;
	}
      
	/**
	 * Retrieves whether the cursor is after the last row in 
	 * this <code>ResultSet</code> object.
	 *
	 * @return <code>true</code> if the cursor is after the last row;
	 * <code>false</code> if the cursor is at any other position or the
	 * result set contains no rows
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public boolean isAfterLast() throws SQLException {
		if ( ( getNoRows() == 0 ) || ( CURRENTROW != ( LASTROW + 1 ) ) )
			return false;
		
		return true;
	}
 
	/**
	 * Retrieves whether the cursor is on the first row of
	 * this <code>ResultSet</code> object.
	 *
	 * @return <code>true</code> if the cursor is on the first row;
	 * <code>false</code> otherwise   
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public boolean isFirst() throws SQLException {
		if ( getNoRows() == 0 )
			return false;
			
		return ( CURRENTROW == 1 );
	}
 
	/**
	 * Retrieves whether the cursor is on the last row of 
	 * this <code>ResultSet</code> object.
	 * Note: Calling the method <code>isLast</code> may be expensive
	 * because the JDBC driver
	 * might need to fetch ahead one row in order to determine 
	 * whether the current row is the last row in the result set.
	 *
	 * @return <code>true</code> if the cursor is on the last row;
	 * <code>false</code> otherwise   
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public boolean isLast() throws SQLException {
		if ( getNoRows() == 0 )
			return false;
			
		return ( CURRENTROW == LASTROW );
	}

	/**
	 * Moves the cursor to the front of
	 * this <code>ResultSet</code> object, just before the
	 * first row. This method has no effect if the result set contains no rows.
	 *
	 * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
	 * @since 1.2
	 */
	public void beforeFirst() {
		reset();
	}

	/**
	 * Moves the cursor to the end of
	 * this <code>ResultSet</code> object, just after the
	 * last row. This method has no effect if the result set contains no rows.
	 * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
	 * @since 1.2
	 */
	public void afterLast() {
		if ( getNoRows() > 0 ) {
			CURRENTROW = LASTROW + 1;
			rowCursor  = null;
		}
	}

	/**
	 * Moves the cursor to the first row in
	 * this <code>ResultSet</code> object.
	 *
	 * @return <code>true</code> if the cursor is on a valid row;
	 * <code>false</code> if there are no rows in the result set
	 * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
	 * @since 1.2
	 */
	public boolean first() throws SQLException {
		if ( getNoRows() == 0 )
			return false;
			
		return absolute( 1 );
	}

	/**
	 * Moves the cursor to the last row in
	 * this <code>ResultSet</code> object.
	 *
	 * @return <code>true</code> if the cursor is on a valid row;
	 * <code>false</code> if there are no rows in the result set
	 * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
	 * @since 1.2
	 */
	public boolean last() throws SQLException {
		if ( ( getNoRows() == 0 ) )
			return false;
		
		return absolute( -1 );
	}

	/**
	 * Retrieves the current row number.  The first row is number 1, the
	 * second number 2, and so on.  
	 *
	 * @return the current row number; <code>0</code> if there is no current row
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public int getRow() throws SQLException {
		if ( CURRENTROW > LASTROW )
			return 0;
			
		return CURRENTROW;
	}

	/**
	 * Moves the cursor to the given row number in
	 * this <code>ResultSet</code> object.
	 *
	 * <p>If the row number is positive, the cursor moves to 
	 * the given row number with respect to the
	 * beginning of the result set.  The first row is row 1, the second
	 * is row 2, and so on. 
	 *
	 * <p>If the given row number is negative, the cursor moves to
	 * an absolute row position with respect to
	 * the end of the result set.  For example, calling the method
	 * <code>absolute(-1)</code> positions the 
	 * cursor on the last row; calling the method <code>absolute(-2)</code>
	 * moves the cursor to the next-to-last row, and so on.
	 *
	 * <p>An attempt to position the cursor beyond the first/last row in
	 * the result set leaves the cursor before the first row or after 
	 * the last row.
	 *
	 * <p><B>Note:</B> Calling <code>absolute(1)</code> is the same
	 * as calling <code>first()</code>. Calling <code>absolute(-1)</code> 
	 * is the same as calling <code>last()</code>.
	 *
	 * @param row the number of the row to which the cursor should move.
	 *        A positive number indicates the row number counting from the
	 *        beginning of the result set; a negative number indicates the
	 *        row number counting from the end of the result set
	 * @return <code>true</code> if the cursor is on the result set;
	 * <code>false</code> otherwise
	 * @exception SQLException if a database access error
	 * occurs, or the result set type is <code>TYPE_FORWARD_ONLY</code>
	 * @since 1.2
	 */
	public boolean absolute( int row )
	{
		if ( getNoRows() == 0 )
			return false;
					
		if ( row > 0 ) {
			if ( row <= tableRows.size() ) {
				CURRENTROW = row;
				rowCursor  = tableRows.get( CURRENTROW - 1 );
				return true;
			} else {
				afterLast();
				return false;
			}
		} else if ( row < 0 ) {
			CURRENTROW = tableRows.size() + row + 1; // row is negative, so we're subtracting it here
			if ( CURRENTROW <= tableRows.size() ) {
				rowCursor = tableRows.get( CURRENTROW - 1 );
				return true;
			}
		}
		
		// ( row == 0 ) or negative row number past first
		beforeFirst();
		return false;
	}

	/**
	 * Moves the cursor a relative number of rows, either positive or negative.
	 * Attempting to move beyond the first/last row in the
	 * result set positions the cursor before/after the
	 * the first/last row. Calling <code>relative(0)</code> is valid, but does
	 * not change the cursor position.
	 *
	 * <p>Note: Calling the method <code>relative(1)</code>
	 * is identical to calling the method <code>next()</code> and 
	 * calling the method <code>relative(-1)</code> is identical
	 * to calling the method <code>previous()</code>.
	 *
	 * @param rows an <code>int</code> specifying the number of rows to
	 *        move from the current row; a positive number moves the cursor
	 *        forward; a negative number moves the cursor backward
	 * @return <code>true</code> if the cursor is on a row;
	 *         <code>false</code> otherwise
	 * @exception SQLException if a database access error occurs, 
	 *            there is no current row, or the result set type is 
	 *            <code>TYPE_FORWARD_ONLY</code>
	 * @since 1.2
	 */
	public boolean relative( int rows ) {
		if ( getNoRows() == 0 )
			return false;
			
		return absolute( CURRENTROW + rows );
	}

	/**
	 * Moves the cursor to the previous row in this
	 * <code>ResultSet</code> object.
	 *
	 * @return <code>true</code> if the cursor is on a valid row; 
	 * <code>false</code> if it is off the result set
	 * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
	 * @since 1.2
	 */
	public boolean previous() throws SQLException {
		if ( getNoRows() == 0 )
			return false;
			
		return relative( -1 );
	}

	/**
	 * Gives a hint as to the direction in which the rows in this
	 * <code>ResultSet</code> object will be processed. 
	 * The initial value is determined by the 
	 * <code>Statement</code> object
	 * that produced this <code>ResultSet</code> object.
	 * The fetch direction may be changed at any time.
	 *
	 * @param direction an <code>int</code> specifying the suggested
	 *        fetch direction; one of <code>ResultSet.FETCH_FORWARD</code>, 
	 *        <code>ResultSet.FETCH_REVERSE</code>, or
	 *        <code>ResultSet.FETCH_UNKNOWN</code>
	 * @exception SQLException if a database access error occurs or
	 * the result set type is <code>TYPE_FORWARD_ONLY</code> and the fetch
	 * direction is not <code>FETCH_FORWARD</code>
	 * @since 1.2
	 * @see Statement#setFetchDirection
	 * @see #getFetchDirection
	 */
	public void setFetchDirection(int direction) throws SQLException {
	}

	/**
	 * Retrieves the fetch direction for this 
	 * <code>ResultSet</code> object.
	 *
	 * @return the current fetch direction for this <code>ResultSet</code> object 
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 * @see #setFetchDirection
	 */
	public int getFetchDirection() throws SQLException {
		return ResultSet.FETCH_FORWARD;
	}

	/**
	 * Gives the JDBC driver a hint as to the number of rows that should 
	 * be fetched from the database when more rows are needed for this 
	 * <code>ResultSet</code> object.
	 * If the fetch size specified is zero, the JDBC driver 
	 * ignores the value and is free to make its own best guess as to what
	 * the fetch size should be.  The default value is set by the 
	 * <code>Statement</code> object
	 * that created the result set.  The fetch size may be changed at any time.
	 *
	 * @param rows the number of rows to fetch
	 * @exception SQLException if a database access error occurs or the
	 * condition <code>0 <= rows <= Statement.getMaxRows()</code> is not satisfied
	 * @since 1.2
	 * @see #getFetchSize
	 */
	public void setFetchSize(int rows) throws SQLException {
	}

	/**
	 * Retrieves the fetch size for this 
	 * <code>ResultSet</code> object.
	 *
	 * @return the current fetch size for this <code>ResultSet</code> object
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 * @see #setFetchSize
	 */
	public int getFetchSize() throws SQLException {
		return 0;
	}

	/**
	 * Retrieves the type of this <code>ResultSet</code> object.  
	 * The type is determined by the <code>Statement</code> object
	 * that created the result set.
	 *
	 * @return <code>ResultSet.TYPE_FORWARD_ONLY</code>,
	 *         <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>,
	 *         or <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public int getType() throws SQLException {
		return ResultSet.TYPE_SCROLL_INSENSITIVE;
	}

	/**
	 * Retrieves the concurrency mode of this <code>ResultSet</code> object.
	 * The concurrency used is determined by the 
	 * <code>Statement</code> object that created the result set.
	 *
	 * @return the concurrency type, either
	 *         <code>ResultSet.CONCUR_READ_ONLY</code>
	 *         or <code>ResultSet.CONCUR_UPDATABLE</code>
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public int getConcurrency() throws SQLException {
		return ResultSet.CONCUR_READ_ONLY;
	}

	//---------------------------------------------------------------------
	// Updates
	//---------------------------------------------------------------------

	/**
	 * Retrieves whether the current row has been updated.  The value returned 
	 * depends on whether or not the result set can detect updates.
	 *
	 * @return <code>true</code> if both (1) the row has been visibly updated
	 *         by the owner or another and (2) updates are detected
	 * @exception SQLException if a database access error occurs
	 * @see DatabaseMetaData#updatesAreDetected
	 * @since 1.2
	 */
	public boolean rowUpdated() throws SQLException {
		return false;
	}

	/**
	 * Retrieves whether the current row has had an insertion.
	 * The value returned depends on whether or not this
	 * <code>ResultSet</code> object can detect visible inserts.
	 *
	 * @return <code>true</code> if a row has had an insertion
	 * and insertions are detected; <code>false</code> otherwise
	 * @exception SQLException if a database access error occurs
	 * 
	 * @see DatabaseMetaData#insertsAreDetected
	 * @since 1.2
	 */
	public boolean rowInserted() throws SQLException {
		return false;
	}
   
	/**
	 * Retrieves whether a row has been deleted.  A deleted row may leave
	 * a visible "hole" in a result set.  This method can be used to
	 * detect holes in a result set.  The value returned depends on whether 
	 * or not this <code>ResultSet</code> object can detect deletions.
	 *
	 * @return <code>true</code> if a row was deleted and deletions are detected;
	 * <code>false</code> otherwise
	 * @exception SQLException if a database access error occurs
	 * 
	 * @see DatabaseMetaData#deletesAreDetected
	 * @since 1.2
	 */
	public boolean rowDeleted() throws SQLException {
		return false;
	}

	/**
	 * Gives a nullable column a null value.
	 * 
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code>
	 * or <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateNull(int columnIndex) throws SQLException {
		throw new SQLException( "updates not supported" );
	}
	
	/**
	 * Updates the designated column with a <code>boolean</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>byte</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>short</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with an <code>int</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateInt(int columnIndex, int x) throws SQLException  {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>long</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>float</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>double</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>java.math.BigDecimal</code> 
	 * value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>String</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateString(int columnIndex, String x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>byte</code> array value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateBytes(int columnIndex, byte x[]) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>java.sql.Date</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateDate(int columnIndex, java.sql.Date x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>java.sql.Time</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateTime(int columnIndex, java.sql.Time x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>java.sql.Timestamp</code>
	 * value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateTimestamp(int columnIndex, java.sql.Timestamp x)
		throws SQLException 
	{
		throw new SQLException( "updates not supported" );
	}

	/** 
	 * Updates the designated column with an ascii stream value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @param length the length of the stream
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateAsciiStream(int columnIndex, 
			 java.io.InputStream x, 
			 int length) throws SQLException
	{
		throw new SQLException( "updates not supported" );
	}

	/** 
	 * Updates the designated column with a binary stream value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value     
	 * @param length the length of the stream
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateBinaryStream(int columnIndex, 
				java.io.InputStream x,
				int length) throws SQLException
	{
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a character stream value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @param length the length of the stream
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateCharacterStream(int columnIndex,
				 java.io.Reader x,
				 int length) throws SQLException
	{
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with an <code>Object</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @param scale for <code>java.sql.Types.DECIMA</code>
	 *  or <code>java.sql.Types.NUMERIC</code> types,
	 *  this is the number of digits after the decimal point.  For all other
	 *  types this value will be ignored.
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateObject(int columnIndex, Object x, int scale)
		throws SQLException
	{
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with an <code>Object</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateObject(int columnIndex, Object x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>null</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateNull(String columnName) throws SQLException {
		throw new SQLException( "updates not supported" );
	}  

	/**
	 * Updates the designated column with a <code>boolean</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateBoolean(String columnName, boolean x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>byte</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateByte(String columnName, byte x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>short</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateShort(String columnName, short x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with an <code>int</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateInt(String columnName, int x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>long</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateLong(String columnName, long x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>float	</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateFloat(String columnName, float x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>double</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateDouble(String columnName, double x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>java.sql.BigDecimal</code>
	 * value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>String</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateString(String columnName, String x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a byte array value.
	 *
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> 
	 * or <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateBytes(String columnName, byte x[]) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>java.sql.Date</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateDate(String columnName, java.sql.Date x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>java.sql.Time</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateTime(String columnName, java.sql.Time x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a <code>java.sql.Timestamp</code>
	 * value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateTimestamp(String columnName, java.sql.Timestamp x)
		throws SQLException
	{
		throw new SQLException( "updates not supported" );
	}

	/** 
	 * Updates the designated column with an ascii stream value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @param length the length of the stream
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateAsciiStream(String columnName, 
			 java.io.InputStream x, 
			 int length) throws SQLException
	{
		throw new SQLException( "updates not supported" );
	}

	/** 
	 * Updates the designated column with a binary stream value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @param length the length of the stream
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateBinaryStream(String columnName, 
				java.io.InputStream x,
				int length) throws SQLException
	{
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with a character stream value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param reader the <code>java.io.Reader</code> object containing
	 *        the new column value
	 * @param length the length of the stream
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateCharacterStream(String columnName,
				 java.io.Reader reader,
				 int length) throws SQLException
	{
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with an <code>Object</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @param scale for <code>java.sql.Types.DECIMAL</code>
	 *  or <code>java.sql.Types.NUMERIC</code> types,
	 *  this is the number of digits after the decimal point.  For all other
	 *  types this value will be ignored.
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateObject(String columnName, Object x, int scale) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Updates the designated column with an <code>Object</code> value.
	 * The updater methods are used to update column values in the
	 * current row or the insert row.  The updater methods do not 
	 * update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName the name of the column
	 * @param x the new column value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public void updateObject(String columnName, Object x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Inserts the contents of the insert row into this 
	 * <code>ResultSet</code> object and into the database.  
	 * The cursor must be on the insert row when this method is called.
	 *
	 * @exception SQLException if a database access error occurs,
	 * if this method is called when the cursor is not on the insert row,
	 * or if not all of non-nullable columns in
	 * the insert row have been given a value
	 * @since 1.2
	 */
	public void insertRow() throws SQLException {
		throw new SQLException( "insert not supported" );
	}

	/**
	 * Updates the underlying database with the new contents of the
	 * current row of this <code>ResultSet</code> object.
	 * This method cannot be called when the cursor is on the insert row.
	 *
	 * @exception SQLException if a database access error occurs or
	 * if this method is called when the cursor is on the insert row
	 * @since 1.2
	 */
	public void updateRow() throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Deletes the current row from this <code>ResultSet</code> object 
	 * and from the underlying database.  This method cannot be called when
	 * the cursor is on the insert row.
	 *
	 * @exception SQLException if a database access error occurs
	 * or if this method is called when the cursor is on the insert row
	 * @since 1.2
	 */
	public void deleteRow() throws SQLException {
		throw new SQLException( "delete not supported" );
	}

	/**
	 * Refreshes the current row with its most recent value in 
	 * the database.  This method cannot be called when
	 * the cursor is on the insert row.
	 *
	 * <P>The <code>refreshRow</code> method provides a way for an 
	 * application to 
	 * explicitly tell the JDBC driver to refetch a row(s) from the
	 * database.  An application may want to call <code>refreshRow</code> when 
	 * caching or prefetching is being done by the JDBC driver to
	 * fetch the latest value of a row from the database.  The JDBC driver 
	 * may actually refresh multiple rows at once if the fetch size is 
	 * greater than one.
	 * 
	 * <P> All values are refetched subject to the transaction isolation 
	 * level and cursor sensitivity.  If <code>refreshRow</code> is called after
	 * calling an updater method, but before calling
	 * the method <code>updateRow</code>, then the
	 * updates made to the row are lost.  Calling the method
	 * <code>refreshRow</code> frequently will likely slow performance.
	 *
	 * @exception SQLException if a database access error
	 * occurs or if this method is called when the cursor is on the insert row
	 * @since 1.2
	 */
	public void refreshRow() throws SQLException {
		throw new SQLException( "refresh not supported" );
	}

	/**
	 * Cancels the updates made to the current row in this
	 * <code>ResultSet</code> object.
	 * This method may be called after calling an
	 * updater method(s) and before calling
	 * the method <code>updateRow</code> to roll back 
	 * the updates made to a row.  If no updates have been made or 
	 * <code>updateRow</code> has already been called, this method has no 
	 * effect.
	 *
	 * @exception SQLException if a database access error
	 *            occurs or if this method is called when the cursor is 
	 *            on the insert row
	 * @since 1.2
	 */
	public void cancelRowUpdates() throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	/**
	 * Moves the cursor to the insert row.  The current cursor position is 
	 * remembered while the cursor is positioned on the insert row.
	 *
	 * The insert row is a special row associated with an updatable
	 * result set.  It is essentially a buffer where a new row may
	 * be constructed by calling the updater methods prior to 
	 * inserting the row into the result set.  
	 *
	 * Only the updater, getter,
	 * and <code>insertRow</code> methods may be 
	 * called when the cursor is on the insert row.  All of the columns in 
	 * a result set must be given a value each time this method is
	 * called before calling <code>insertRow</code>.  
	 * An updater method must be called before a
	 * getter method can be called on a column value.
	 *
	 * @exception SQLException if a database access error occurs
	 * or the result set is not updatable
	 * @since 1.2
	 */
	public void moveToInsertRow() throws SQLException {
		throw new SQLException( "insert not supported" );
	}

	/**
	 * Moves the cursor to the remembered cursor position, usually the
	 * current row.  This method has no effect if the cursor is not on 
	 * the insert row. 
	 *
	 * @exception SQLException if a database access error occurs
	 * or the result set is not updatable
	 * @since 1.2
	 */
	public void moveToCurrentRow() throws SQLException {
	}

	/**
	 * Retrieves the <code>Statement</code> object that produced this 
	 * <code>ResultSet</code> object.
	 * If the result set was generated some other way, such as by a
	 * <code>DatabaseMetaData</code> method, this method returns 
	 * <code>null</code>.
	 *
	 * @return the <code>Statment</code> object that produced 
	 * this <code>ResultSet</code> object or <code>null</code>
	 * if the result set was produced some other way
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public Statement getStatement() throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as an <code>Object</code>
	 * in the Java programming language.
	 * If the value is an SQL <code>NULL</code>, 
	 * the driver returns a Java <code>null</code>.
	 * This method uses the given <code>Map</code> object
	 * for the custom mapping of the
	 * SQL structured or distinct type that is being retrieved.
	 *
	 * @param i the first column is 1, the second is 2, ...
	 * @param map a <code>java.util.Map</code> object that contains the mapping 
	 * from SQL type names to classes in the Java programming language
	 * @return an <code>Object</code> in the Java programming language
	 * representing the SQL value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public Object getObject(int i, java.util.Map<String, Class<?>> map) throws SQLException {

		// NOTE: the map parameter is ignored for now.  This parameter is used to map
		//       SQL user-defined types (UDTs) to a Java class.  If any of our customers
		//       start to use UDTs then this method will need to be updated.
		//       Refer to Chapter 35 of "JDBC API Tutorial and Reference, Second Edition"

		return getObject( i );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>Ref</code> object
	 * in the Java programming language.
	 *
	 * @param i the first column is 1, the second is 2, ...
	 * @return a <code>Ref</code> object representing an SQL <code>REF</code> 
	 *         value
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public Ref getRef(int i) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>Blob</code> object
	 * in the Java programming language.
	 *
	 * @param i the first column is 1, the second is 2, ...
	 * @return a <code>Blob</code> object representing the SQL 
	 *         <code>BLOB</code> value in the specified column
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public Blob getBlob(int i) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>Clob</code> object
	 * in the Java programming language.
	 *
	 * @param i the first column is 1, the second is 2, ...
	 * @return a <code>Clob</code> object representing the SQL 
	 *         <code>CLOB</code> value in the specified column
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public Clob getClob(int i) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as an <code>Array</code> object
	 * in the Java programming language.
	 *
	 * @param i the first column is 1, the second is 2, ...
	 * @return an <code>Array</code> object representing the SQL 
	 *         <code>ARRAY</code> value in the specified column
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public Array getArray(int i) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as an <code>Object</code>
	 * in the Java programming language.
	 * If the value is an SQL <code>NULL</code>, 
	 * the driver returns a Java <code>null</code>.
	 * This method uses the specified <code>Map</code> object for
	 * custom mapping if appropriate.
	 *
	 * @param colName the name of the column from which to retrieve the value
	 * @param map a <code>java.util.Map</code> object that contains the mapping 
	 * from SQL type names to classes in the Java programming language
	 * @return an <code>Object</code> representing the SQL value in the 
	 *         specified column
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public Object getObject(String colName, java.util.Map<String, Class<?>> map) throws SQLException {
		return getObject( findColumn( colName ), map );
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>Ref</code> object
	 * in the Java programming language.
	 *
	 * @param colName the column name
	 * @return a <code>Ref</code> object representing the SQL <code>REF</code> 
	 *         value in the specified column
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public Ref getRef(String colName) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>Blob</code> object
	 * in the Java programming language.
	 *
	 * @param colName the name of the column from which to retrieve the value
	 * @return a <code>Blob</code> object representing the SQL <code>BLOB</code> 
	 *         value in the specified column
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public Blob getBlob(String colName) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>Clob</code> object
	 * in the Java programming language.
	 *
	 * @param colName the name of the column from which to retrieve the value
	 * @return a <code>Clob</code> object representing the SQL <code>CLOB</code>
	 * value in the specified column
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public Clob getClob(String colName) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as an <code>Array</code> object
	 * in the Java programming language.
	 *
	 * @param colName the name of the column from which to retrieve the value
	 * @return an <code>Array</code> object representing the SQL <code>ARRAY</code> value in
	 *         the specified column
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public Array getArray(String colName) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>java.sql.Date</code> object
	 * in the Java programming language.
	 * This method uses the given calendar to construct an appropriate millisecond
	 * value for the date if the underlying database does not store
	 * timezone information.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param cal the <code>java.util.Calendar</code> object
	 * to use in constructing the date
	 * @return the column value as a <code>java.sql.Date</code> object;
	 * if the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code> in the Java programming language
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>java.sql.Date</code> object
	 * in the Java programming language.
	 * This method uses the given calendar to construct an appropriate millisecond
	 * value for the date if the underlying database does not store
	 * timezone information.
	 *
	 * @param columnName the SQL name of the column from which to retrieve the value
	 * @param cal the <code>java.util.Calendar</code> object
	 * to use in constructing the date
	 * @return the column value as a <code>java.sql.Date</code> object;
	 * if the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code> in the Java programming language
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>java.sql.Time</code> object
	 * in the Java programming language.
	 * This method uses the given calendar to construct an appropriate millisecond
	 * value for the time if the underlying database does not store
	 * timezone information.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param cal the <code>java.util.Calendar</code> object
	 * to use in constructing the time
	 * @return the column value as a <code>java.sql.Time</code> object;
	 * if the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code> in the Java programming language
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>java.sql.Time</code> object
	 * in the Java programming language.
	 * This method uses the given calendar to construct an appropriate millisecond
	 * value for the time if the underlying database does not store
	 * timezone information.
	 *
	 * @param columnName the SQL name of the column
	 * @param cal the <code>java.util.Calendar</code> object
	 * to use in constructing the time
	 * @return the column value as a <code>java.sql.Time</code> object;
	 * if the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code> in the Java programming language
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public java.sql.Time getTime(String columnName, Calendar cal) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>java.sql.Timestamp</code> object
	 * in the Java programming language.
	 * This method uses the given calendar to construct an appropriate millisecond
	 * value for the timestamp if the underlying database does not store
	 * timezone information.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @param cal the <code>java.util.Calendar</code> object
	 * to use in constructing the timestamp
	 * @return the column value as a <code>java.sql.Timestamp</code> object;
	 * if the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code> in the Java programming language
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		return null;
	}

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a <code>java.sql.Timestamp</code> object
	 * in the Java programming language.
	 * This method uses the given calendar to construct an appropriate millisecond
	 * value for the timestamp if the underlying database does not store
	 * timezone information.
	 *
	 * @param columnName the SQL name of the column
	 * @param cal the <code>java.util.Calendar</code> object
	 * to use in constructing the date
	 * @return the column value as a <code>java.sql.Timestamp</code> object;
	 * if the value is SQL <code>NULL</code>,
	 * the value returned is <code>null</code> in the Java programming language
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	public java.sql.Timestamp getTimestamp(String columnName, Calendar cal)	throws SQLException {
		return null;
	}
	
	public java.net.URL getURL(int x) throws SQLException {
		return null;
	}
	
	public java.net.URL getURL(String x) throws SQLException {
		return null;
	}

	public void updateArray(int x, Array A)	throws SQLException {
		throw new SQLException( "updates not supported" );
	}
	
	public void updateArray(String x, Array A) throws SQLException {
		throw new SQLException( "updates not supported" );
	}
	
	public void updateBlob(String columnName, java.sql.Blob x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}
	
	public void updateBlob(int columnName, java.sql.Blob x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}
	
	public void updateClob(String columnName, java.sql.Clob x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}
	
	public void updateClob(int columnName, java.sql.Clob x)	throws SQLException {
		throw new SQLException( "updates not supported" );
	}
	
	public void updateRef(int columnName, java.sql.Ref x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}
	
	public void updateRef(String columnName, java.sql.Ref x) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	
	/*
	 * Need to override the hashCode() method defined in cfJavaObjectData.  If we don't then when
	 * it's called it will go in an infinite loop.
	 */
	public int hashCode(){
		return tableRows.hashCode();
	}

	public boolean equals( cfData _data ){
		if ( _data == this ){
			return true;
		}else if ( _data.getDataType() == cfData.CFQUERYRESULTDATA ){
			if ( getSize() == ( (cfQueryResultData)_data).getSize() 
					&& this.getColumns().equals( ( (cfQueryResultData)_data).getColumns() ) ){
				return this.tableRows.equals( ( (cfQueryResultData)_data).tableRows );
			}
		}
		return false;
	}

	@Override
  public int getHoldability() throws SQLException {
	  return 0;
  }

	@Override
  public Reader getNCharacterStream( int arg0 ) throws SQLException {
	  return null;
  }

	@Override
  public Reader getNCharacterStream( String arg0 ) throws SQLException {
	  return null;
  }

	@Override
  public NClob getNClob( int arg0 ) throws SQLException {
	  return null;
  }

	@Override
  public NClob getNClob( String arg0 ) throws SQLException {
	  return null;
  }

	@Override
  public String getNString( int arg0 ) throws SQLException {
	  return null;
  }

	@Override
  public String getNString( String arg0 ) throws SQLException {
	  return null;
  }

	@Override
  public RowId getRowId( int arg0 ) throws SQLException {
	  return null;
  }

	@Override
  public RowId getRowId( String arg0 ) throws SQLException {
	  return null;
  }

	@Override
  public SQLXML getSQLXML( int arg0 ) throws SQLException {
	  return null;
  }

	@Override
  public SQLXML getSQLXML( String arg0 ) throws SQLException {
	  return null;
  }

	@Override
  public boolean isClosed() throws SQLException {
	  return true;
  }

	@Override
  public void updateAsciiStream( int arg0, InputStream arg1 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateAsciiStream( String arg0, InputStream arg1 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateAsciiStream( int arg0, InputStream arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );	  
  }

	@Override
  public void updateAsciiStream( String arg0, InputStream arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateBinaryStream( int arg0, InputStream arg1 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateBinaryStream( String arg0, InputStream arg1 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateBinaryStream( int arg0, InputStream arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateBinaryStream( String arg0, InputStream arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateBlob( int arg0, InputStream arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateBlob( String arg0, InputStream arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateBlob( int arg0, InputStream arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateBlob( String arg0, InputStream arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateCharacterStream( int arg0, Reader arg1 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateCharacterStream( String arg0, Reader arg1 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateCharacterStream( int arg0, Reader arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateCharacterStream( String arg0, Reader arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateClob( int arg0, Reader arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateClob( String arg0, Reader arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );	  
  }

	@Override
  public void updateClob( int arg0, Reader arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );  }

	@Override
  public void updateClob( String arg0, Reader arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );  }

	@Override
  public void updateNCharacterStream( int arg0, Reader arg1 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	@Override
  public void updateNCharacterStream( String arg0, Reader arg1 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateNCharacterStream( int arg0, Reader arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );	  
  }

	@Override
  public void updateNCharacterStream( String arg0, Reader arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );	  
  }

	@Override
  public void updateNClob( int arg0, NClob arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	@Override
  public void updateNClob( String arg0, NClob arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateNClob( int arg0, Reader arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );  
	}

	@Override
  public void updateNClob( String arg0, Reader arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	@Override
  public void updateNClob( int arg0, Reader arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
	}

	@Override
  public void updateNClob( String arg0, Reader arg1, long arg2 )
      throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateNString( int arg0, String arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateNString( String arg0, String arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateRowId( int arg0, RowId arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateRowId( String arg0, RowId arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateSQLXML( int arg0, SQLXML arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public void updateSQLXML( String arg0, SQLXML arg1 ) throws SQLException {
		throw new SQLException( "updates not supported" );
  }

	@Override
  public boolean isWrapperFor( Class<?> iface ) throws SQLException {
		throw new SQLException( "method not supported" );
	}

	@Override
  public <T> T unwrap( Class<T> iface ) throws SQLException {
		throw new SQLException( "method not supported" );
	}

	public boolean renameColumn(String oldcolumnName, String newcolumnName) {
		return rsmd.renameColumn( oldcolumnName, newcolumnName );
	}
	
	
	/**
	 * Special function for looping around the data with a UserDefinedFunction
	 * 
	 * @param SessionData
	 * @param _data
	 * @throws cfmRunTimeException
	 */
	public void each( cfDataSession SessionData, cfData _data ) throws cfmRunTimeException {
		if ( _data.getDataType() != cfData.CFUDFDATA )
			throw new cfmRunTimeException( catchDataFactory.generalException("Invalid Attribute", "Must be a user defined function") );

		userDefinedFunction	udf	= (userDefinedFunction)_data;
		List<cfData>	args	= new ArrayList<cfData>(1);

		int resetRow = getCurrentRow();
		reset();
		
		while (nextRow()) {
			cfStructData	rowData	= getRowAsStruct();
		
			args.clear();
			args.add( rowData );
			udf.execute( SessionData.Session, args );
		}

		setCurrentRow( resetRow == 0 ? 1 : resetRow );
	}

	public < T > T getObject( int arg0, Class<T> arg1 ) throws SQLException {
		return null;
	}

	
	public < T > T getObject( String arg0, Class<T> arg1 ) throws SQLException {
		return null;
	}
}
