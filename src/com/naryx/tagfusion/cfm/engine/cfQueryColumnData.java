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

import java.io.PrintWriter;
import java.util.List;

public class cfQueryColumnData extends cfArrayData {

	private static final long serialVersionUID = 1L;
	
  private cfQueryResultData query;
  private int col;
  
  public cfQueryColumnData( cfQueryResultData _query, cfData _col ) throws dataNotSupportedException {
    this( _query, _query.getColumnIndexCF( _col.getString() ) );
  }
  
  private cfQueryColumnData( cfQueryResultData _query, int _col ){
    super();
    setInstance( query ); 
    query = _query;    
    col = _col;
  }
  
  public void addElement( cfData _element ) {} //do nothing

  public void addElementAt( cfData _element, int _index ) {} //do nothing

  public cfArrayData copy() {
    return new cfQueryColumnData( (cfQueryResultData) query.duplicate(), col );
  }

  public String createList( String delimiter ) throws dataNotSupportedException {
    int size = query.getSize();
    
    // If there are no items then return an empty string
    if ( size == 0 )
    	return "";
    	
    StringBuilder result = new StringBuilder();
	
	// Append the first item
	result.append( query.getCell( 1, col ).getString() );

    // If there are more items then append them with the delimiter
    for ( int i = 2; i <= size; i++ ){
      result.append( delimiter );
      result.append( query.getCell( i, col ).getString() );
    }
    
    return result.toString();
  }

  
  public void dump( PrintWriter out, String _label, int _top ) {
  	if ( query.getSize() == 0 ){
  		cfStringData.EMPTY_STRING.dump( out, _label, _top );
  	}else{
  		query.getCell( 1, col ).dump( out, _label, _top );
  	}
  }

  public void dump( PrintWriter out ) {
  	if ( query.getSize() == 0 ){
  		cfStringData.EMPTY_STRING.dump( out );
  	}else{
  		query.getCell( 1, col ).dump( out );
  	}
  }

  public void dumpWDDX( int version, PrintWriter out ) {
    int qrySize = query.getSize();
    if ( version > 10 )
      out.write( "<a l='" );
    else
      out.write( "<array length='" );
    
    out.write( qrySize + "" );
    out.write( "'>" );
  
    for ( int x=1; x <= qrySize; x++ ){
      cfData nextCell = query.getCell( x, col );
      if( nextCell != null)
        nextCell.dumpWDDX( version, out );  
    }
  
    if ( version > 10 )
      out.write( "</a>" );
    else
      out.write( "</array>" );
  }

  public cfData duplicate() {
    return copy();
  }

  public void elementSwap( int _start, int _end ) {
    cfData first = query.getCell( _start, col );
    cfData second = query.getCell( _end, col );
    query.setCell( _end, col, first );
    query.setCell( _start, col, second );
  }

  public boolean equals( Object o ) {
    if ( o instanceof cfQueryColumnData )
      return ((cfQueryColumnData)o).col == col && query.equals( ((cfArrayData)o).data );
  
    return false;
  }

  public double getAverage() throws dataNotSupportedException {
    if ( query.getSize() == 0 ) 
      return 0;
    
    double sum = 0;
    for ( int x = 1; x <= query.getSize(); x++ )
      sum += query.getCell(x,col).getDouble();

    return ( sum / query.getSize() );
  }

  
  public cfData getData( cfData arrayIndex ) throws cfmRunTimeException {
  	return query.getCell( arrayIndex.getInt(), col );
  }

  // returns the value from this column using the current row in the query as the index
  public cfData getData(){ 
  	if ( query.getSize() == 0 ){
  		return new cfStringData( "" );
  	}
	  	
  	int currentRow = query.getCurrentRow();
  	if ( currentRow == 0 ){
  		query.setCurrentRow( 1 );
  		currentRow = 1;
  	}
  	return query.getCell( currentRow, col );
  }

  public int getDimension() {
    return 1;
  }

  public cfData getElement( int _index ) {
    return query.getCell( _index, col );

  }

  public double getMax() throws dataNotSupportedException {
    int qrySize = query.getSize();
    if ( qrySize == 0 ){
      return 0.0;
    }

    double max = query.getCell(1, col ).getDouble();
    double temp;
    for ( int x = 2; x <= qrySize; x++ ){
      temp = query.getCell(x, col ).getDouble();
      if ( temp > max )
        max = temp;
    }
    
    return max;
  }

  public double getMin() throws dataNotSupportedException {
    int qrySize = query.getSize();
    if ( qrySize == 0 ){
      return 0.0;
    }

    double min = query.getCell(1, col ).getDouble();
    double temp;
    for ( int x = 2; x <= qrySize; x++ ){
      temp = query.getCell(x, col ).getDouble();
      if ( temp < min )
        min = temp;
    }
    
    return min;
  }

  public double getSum() throws dataNotSupportedException {
    int qrySize = query.getSize();
    double sum = 0.0;
    
    for ( int x = 1; x <= qrySize; x++ ){
      sum += query.getCell(x, col ).getDouble();
    }
    return sum;
  }


  public int hashCode() {
    return query.hashCode();
  }

  public boolean isEmpty() {
    return query.getSize() == 0;
  }

  public void removeAllElements() throws cfmRunTimeException {
    cfCatchData catchData = new cfCatchData();
    catchData.setType( cfCatchData.TYPE_APPLICATION );
    catchData.setMessage( "Cannot perform this operation on a query column." );
    throw new cfmRunTimeException( catchData );
  } 

  public void removeElementAt( int _no ) {}

  public void setCapacity( int _size ) {} // do nothing

  public void setData( cfData arrayIndex, cfData _data ) throws cfmRunTimeException {
    setData( arrayIndex.getInt(), _data );
  }

  public void setData( int _index, cfData _element ) {
    query.setCell( _index, col, _element );
  }
  
  public void setElements( int _start, int _end, cfData _value ) throws cfmRunTimeException{
    if ( _end > query.getSize() ){
      cfCatchData catchData = new cfCatchData();
      catchData.setType( cfCatchData.TYPE_APPLICATION );
      catchData.setMessage( "Cannot perform this operation on a query column." );
      throw new cfmRunTimeException( catchData );
    }else{
      for ( int x = _start; x <= _end ; x++ ){    
        query.setCell( x, col, _value );
      }
    }
    
  } // do nothing

  public int size() {
    return query.getSize();
  }

  
  public int getQueryColumn() {
    return col;
  }

  public List<List<cfData>> getQueryTableData() {
    return query.getTableRows();
  }

  public void sortArray( String _type, String _order ) throws dataNotSupportedException {
    if ( query.getSize() == 0 ){
      return;
    }

    SelectionSort sort = null;
    
    if ( _type.equalsIgnoreCase("numeric") )
      sort = new SelectionSort( new NumericComparator( _order.equalsIgnoreCase( "asc" ) ) );
    else if ( _type.equalsIgnoreCase("text") )
      sort = new SelectionSort( new TextComparator( _order.equalsIgnoreCase( "asc" ) ) );
    else if ( _type.equalsIgnoreCase("textnocase") )
      sort = new SelectionSort( new TextNoCaseComparator( _order.equalsIgnoreCase( "asc" ) ) );
    else
      throw new dataNotSupportedException(); 
    sort.doSort();
  }
  
  // similar to java.util.Comparator but allows for throwing of dataNotSupportedException
  interface Comparator{
    public int compare( cfData o1, cfData o2 ) throws dataNotSupportedException;
  }
  
  class NumericComparator implements Comparator{
    
    private int order;
    
    public NumericComparator( boolean _asc ){
      order = _asc ? -1 : 1;
    }
    
    public int compare( cfData o1, cfData o2 ) throws dataNotSupportedException{
      if ( o1.getDouble() < o2.getDouble() )
        return -1 * order;
      else
        return 1 * order;
    }
    
  }

  class TextComparator implements Comparator{
    
    private int order;
    
    public TextComparator( boolean _asc ){
      order = _asc ? -1 : 1;
    }
    
    public int compare( cfData o1, cfData o2 ) throws dataNotSupportedException{
      return order * o1.getString().compareTo( o2.getString() );
    }
  }

  class TextNoCaseComparator implements Comparator{
    
    private int order;
    
    public TextNoCaseComparator( boolean _asc ){
      order = _asc ? -1 : 1;
    }
    
    public int compare( cfData o1, cfData o2 ) throws dataNotSupportedException{
      return order * o1.getString().toLowerCase().compareTo( o2.getString().toLowerCase() );
    }
    
  }

  class SelectionSort{
    
    private Comparator comparator;
    
    SelectionSort( Comparator _c ){
      comparator = _c;
    }
    
    public void doSort() throws dataNotSupportedException{
      sort( 1, query.getSize() );
    }
    
    private void sort( int min, int max ) throws dataNotSupportedException {
      if (min == max)
         return;
  
      // Find the smallest.
      int index = select( min, max);
  
      // Swap the smallest with the first.
      cfData temp = query.getCell( min, col );
      query.setCell(min,col,query.getCell( index, col ));
      query.setCell(index,col,temp);
  
      // Sort the rest.
      sort(min + 1, max);
    }
  
    private int select(int min, int max) throws dataNotSupportedException  {
      int index = min;
      for (int i = min + 1; i <= max; ++i)
        if ( comparator.compare( query.getCell(i,col), query.getCell(index,col) ) > 0 )
           index = i;
      return index;
    }
  }

  public String toString() {
    return "{ARRAY:" + query + "}";
  }
  
}
