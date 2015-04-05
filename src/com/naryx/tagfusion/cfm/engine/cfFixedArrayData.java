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
import java.lang.reflect.Array;

import com.naryx.tagfusion.cfm.tag.tagUtils;

/**
 * This class allows for the conversion of Java arrays to 
 * unmodifiable cfArrays. You should be able to perform
 * any of the array functions on a cfFixedArrayData that
 * do not attempt to modify the array.
 * 
 * 
 */

public class cfFixedArrayData extends cfArrayData implements java.io.Serializable
{
  static final long serialVersionUID = 1;
	
  private Object array;
  
  /*
   * Must use cfArrayData.createArray() to create an instance of this object
   */
  
  protected cfFixedArrayData( Object _array ){
    array = _array;
    setInstance( array );
  }
  
  
  public Object getArray(){
    return array;
  }
  
  // does nothing.
  public void addElement( cfData _element ){}
  
  // does nothing.
  public void addElementAt( cfData _element, int _index ){}
  
  
  public cfArrayData copy() {
    return this;
  }
  
  public String createList( String delimiter ) {
    int arrayLen = Array.getLength( array );
    if ( arrayLen > 0 ){
    	StringBuilder list = new StringBuilder();
      for ( int i = 0; i < arrayLen; i++ ){
        list.append( Array.get( array, i ).toString() );
        list.append( delimiter );
      }
      list.setLength( list.length()-1 );
      return list.toString();
    }else{
      return "";
    }
  }
  
  
  public void dump( PrintWriter out, String _label, int _top ) {
    int arrayLen = Array.getLength( array );
    out.write( "<table class='cfdump_table_array'>" );
    if ( arrayLen > 0 ) {
      out.write( "<th class='cfdump_th_array' colspan='2'>" );
      if ( _label.length() > 0 ) out.write( _label + " - " );
      out.write( "array</th>" );
      
      int max = ( _top < arrayLen ? _top : arrayLen );
      
			for ( int x=0; x < max; x++ ){
        out.write( "<tr><td class='cfdump_td_array'>" );
        out.write( (x+1) + "" );
        out.write( "</td><td class='cfdump_td_value'>" );
  
        cfData element = tagUtils.convertToCfData( Array.get( array, x ) );
        if ( ( element == null ) || ( element.getDataType() == cfData.CFNULLDATA ) )
          out.write( "[undefined array element]" );
        else
          element.dump(out,"",_top);  
        
        out.write( "</td></tr>" );
      }
    } else {
      out.write( "<th class='cfdump_th_array' colspan='2'>array [empty]</th>" );
    }
    out.write( "</table>" );
  }
  
  
  public void dumpWDDX( int version, PrintWriter out ) {
    int arrayLen = Array.getLength( array );
    
    if ( version > 10 )
      out.write( "<a l='" );
    else
      out.write( "<array length='" );
    
    out.write( arrayLen  + "" );
    out.write( "'>" );
    
    for ( int x=0; x < arrayLen; x++ ){
      Object next = Array.get( array, x );
      if( next != null)
        ( tagUtils.convertToCfData( next ) ).dumpWDDX( version, out );  
    }
    
    if ( version > 10 )
      out.write( "</a>" );
    else
      out.write( "</array>" );
  }
  
  public cfData duplicate(boolean bDeepCopy) {
  	return duplicate();
  }
  
  public cfData duplicate() {
    Class<?> arrClass = array.getClass().getComponentType();
    int arrayLen = Array.getLength( array );
    Object duplicate = Array.newInstance( arrClass, arrayLen );
    
    for ( int i = 0; i < arrayLen; i++ ){
      Array.set( duplicate, i, Array.get( array, i ) );
    }
    
    return new cfFixedArrayData( duplicate );
  }
  
  // does nothing
  public void elementSwap( int _start, int _end ){}
  
  public boolean equals( Object o ) {
  	if ( o instanceof cfFixedArrayData ) {
  		return array.equals( ((cfFixedArrayData)o).array );
  	}
    return array.equals( o );
  }
  
  
  public double getAverage() throws dataNotSupportedException {
    int arrayLen = Array.getLength( array );
    if ( arrayLen > 0 ){
      return getSum() / arrayLen;
    }
    return 0.0;
  }
  
  public cfData getData( cfData arrayIndex ) throws cfmRunTimeException {
    int indx;
    try{
      indx = arrayIndex.getInt();
    }catch(Exception E){ 
      cfCatchData catchData = new cfCatchData();
      catchData.setType( cfCatchData.TYPE_APPLICATION );
      catchData.setMessage( "Attempted to access array with invalid array index." );
      throw new cfmRunTimeException( catchData );
    }
    
    int arrayLen = Array.getLength( array );
    if ( indx > arrayLen || indx < 1 ){
      cfCatchData catchData = new cfCatchData();
      catchData.setType( cfCatchData.TYPE_APPLICATION );
      catchData.setMessage( "Attempted to access array with invalid array index [" + indx + "]." );
      throw new cfmRunTimeException( catchData );
    }
    
    return tagUtils.convertToCfData( getElementAsObject( indx ) );
  }
  
  
  public int getDimension() {
    return 1;
  }
  
  private Object getElementAsObject( int _index ) {
    return Array.get( array, _index - 1 );
  }

  public cfData getElement( int _index ) {
    return tagUtils.convertToCfData( getElementAsObject( _index ) );
  }
  
  public double getMax() throws dataNotSupportedException {
    int arrayLen = Array.getLength( array );
    double max;
    try{
      max = arrayLen > 0 ? Array.getDouble( array, 0 ) : 0.0;
      for ( int i = 1; i < arrayLen; i++ ){
        double nextVal = Array.getDouble( array, i );
        if ( nextVal > max ){
          max = nextVal;
        }
      }
    }catch( IllegalArgumentException iae ){
      throw new dataNotSupportedException( "Found non-numeric value in array." );
    }
    return max; 
  }
  
  public double getMin() throws dataNotSupportedException {
    int arrayLen = Array.getLength( array );
    
    double min;
    try{
      min = arrayLen > 0 ? Array.getDouble( array, 0 ) : 0.0;
      for ( int i = 1; i < arrayLen; i++ ){
        double nextVal = Array.getDouble( array, i );
        if ( nextVal < min ){
          min = nextVal;
        }
      }
    }catch( IllegalArgumentException iae ){
      throw new dataNotSupportedException( "Found non-numeric value in array." );
    }

    return min; 
  }
  
  public double getSum() throws dataNotSupportedException {
    int arrayLen = Array.getLength( array );
    double sum = 0.0;
    
    try{
      for ( int i = 0; i < arrayLen; i++ ){
        sum += Array.getDouble( array, i );
      }
    }catch( IllegalArgumentException iae ){
      throw new dataNotSupportedException( "Found non-numeric value in array." );
    }
    return sum; 
  }
  
  public boolean isEmpty() {
    return Array.getLength( array ) == 0;
  }
  
  public void removeAllElements() throws cfmRunTimeException {
    cfCatchData catchData = new cfCatchData();
    catchData.setType( cfCatchData.TYPE_APPLICATION );
    catchData.setMessage( "Attempted to remove all the elements of an unmodifiable array." );
    throw new cfmRunTimeException( catchData );
  }
  
  // does nothing
  public void removeElementAt( int _no ){}
  
  // does nothing
  public void setCapacity( int _size ){}
  
  public void setData( cfData arrayIndex, cfData _data )
      throws cfmRunTimeException {
    
    cfCatchData catchData = new cfCatchData();
    catchData.setType( cfCatchData.TYPE_APPLICATION );
    catchData.setMessage( "Attempted to set the value of an unmodifiable array." );
    throw new cfmRunTimeException( catchData );
  }

  //does nothing.
  public void setData( int _index, cfData _element ){}
  
  // does nothing.
  public void setElements( int _start, int _end, cfData _value ){}

  
  public int size() {
    return Array.getLength( array );
  }
  
  public void sortArray( String _type, String _order )
      throws dataNotSupportedException {
    throw new dataNotSupportedException( "Attempted to modifies the values of an unmodifiable array." );
  }

  public String toString() {
    return "{ARRAY:" + array + "}";
  }

	public int hashCode() 
	{
		return array.hashCode();
	}
}
