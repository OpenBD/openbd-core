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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.tag.cfDUMP;

/**
 * The <B>cfArrayData</B> class represents a tagServlet array. As a tagServlet array can 
 * dynamically increase its size, a Vector is used to hold the cfDatas that make up the 
 * elements rather than an array.  Note multi-dimensional arrays are not supported.
 */
 
public abstract class cfArrayData extends cfArrayDataBase implements java.io.Serializable{ 
  
  static final long serialVersionUID = 1;
  
  /** The vector that represents the tagServlet array. */
	protected Vector<cfData> data;
	protected int	dimensions;
	
	protected cfArrayData() {
		// purely for subclasses
	}
	
	protected cfArrayData( int _dimensions ) {
		super();
		data = new Vector<cfData>();
		setInstance( this );
		dimensions = _dimensions;
		
		if ( dimensions < 0 ) dimensions = 1;
	}
	
	protected cfArrayData( int _dimensions, Vector<? extends cfData> _data ) {
		super();
		data = new Vector<cfData>( _data );
		setInstance( this );
		dimensions = _dimensions;
	}

  /**
   * Creates a new instance of cfArrayData setting the number of dimension the
   * array will have. 
   *
   * @param _dimension an int between one and three.
   */

  public static cfArrayData createArray( int _dimensions ){
    return new cfArrayListData( _dimensions );
  }


  /**
   * This is called from a Java object.  The only caveat is that if we bring back a string array
   * we will convert it to a proper array and let it modified
   * 
   * @param _arr
   * @return
   */
  public static cfArrayData createArray( Object _arr ){
  	try{
  		
  		if ( _arr instanceof String[] ){
    		String[] sarr = (String[])_arr;
    		cfArrayListData arr = new cfArrayListData(1);
    		for ( int x=0; x < sarr.length; x++ )
    			arr.addElement( new cfStringData(sarr[x]) );
    		return arr;
    	}

  	}catch(cfmRunTimeException ignoreWeCouldNotCreate){}
  	
  	return new cfFixedArrayData( _arr );
  }

  // creates a shallow copy of the given array. The parent query
  // fields, if set, are not copied to the resulting array
  public static cfArrayData createFrom( cfArrayData _arr ){
    cfArrayListData newArr = new cfArrayListData( _arr.dimensions );
    newArr.data = _arr.data;
    newArr.instance = newArr;
    return newArr;
  }
  
	public byte getDataType(){ return cfData.CFARRAYDATA; }
	public String getDataTypeName() { return "array"; }
	public int getDimension(){ return dimensions; }
	
	public int size() {
		return data.size();
	}
	
	public boolean isEmpty() {
		return data.isEmpty();
	}

  public cfData getData( cfData arrayIndex )  throws cfmRunTimeException {
		int indx;
    try{
      indx = arrayIndex.getInt();
    }catch(Exception E){ 
    	cfCatchData	catchData	= new cfCatchData();
  		catchData.setType( cfCatchData.TYPE_APPLICATION );
    	catchData.setMessage( "Attempted to access array with invalid array index." );
    	throw new cfmRunTimeException( catchData );
    }

    return getData( indx );
  }
  
  public cfData getData( int indx )  throws cfmRunTimeException {
  	if ( indx > data.size() )
  		data.setSize( indx );
  	else if ( indx < 1 ){
    	cfCatchData	catchData	= new cfCatchData();
    	catchData.setType( cfCatchData.TYPE_APPLICATION );
    	catchData.setMessage( "Attempted to access array with invalid array index [" + indx + "]." );
    	throw new cfmRunTimeException( catchData );
  	}
  	
  	return (cfData)data.get( indx-1 );
  }
  
  public void	setData( cfData arrayIndex, cfData _data ) throws cfmRunTimeException{
    int indx;
		try{
      indx = arrayIndex.getInt();
    }catch(Exception E){ 
    	cfCatchData	catchData	= new cfCatchData();
  		catchData.setType( cfCatchData.TYPE_APPLICATION );
    	catchData.setMessage( "Attempted to access array with invalid array index." );
    	throw new cfmRunTimeException( catchData );
    }

   	setData( indx, _data );
  }

  public void setData( int _index, cfData _element ) throws cfmRunTimeException { 
		if ( _index > data.size() )
  		data.setSize( _index );
 
		data.set( _index - 1, _element );
  }
  
	public void addElement( cfData _element ) throws cfmRunTimeException {
	  data.add( _element );
	}
	
	public void addElementAt( cfData _element, int _index ) throws cfmRunTimeException {
	  data.add( ( _index - 1 ), _element );
	}
	
	public void removeAllElements() throws cfmRunTimeException{
	  data.clear();
	}
	
	public void removeElementAt( int _no ) throws cfmRunTimeException {
	  data.remove( ( _no - 1 ) );
	}
	
	public void setCapacity( int _size ){ 
	  data.setSize( _size );
	}
	
	public void setElements( int _start, int _end, cfData _value ) throws cfmRunTimeException{
	  for ( int x = ( _start - 1 ); x < _end ; x++ ){	   
  		data.set( x, _value );
		}
	}
	
	public void elementSwap( int _start, int _end ) throws cfmRunTimeException {
	  cfData first = data.get( _start - 1 );
	  cfData second = data.get( _end - 1 );
	  data.set( _end - 1, first );
	  data.set( _start - 1, second );
	}
	

	public void replace(int x, cfData _value) {
		data.set( x-1, _value);
	}
	
	@SuppressWarnings("unchecked")
	public cfArrayData copy() {
		cfArrayData arr = createArray( dimensions );
		arr.data = (Vector<cfData>)data.clone(); // unchecked cast is OK here
		for ( int i = 0; i < arr.data.size(); i++ ) {
			cfData nextData = arr.data.get( i );
			if ( nextData != null && nextData.getDataType() == cfData.CFARRAYDATA ) {
				arr.data.set( i, ( (cfArrayData)nextData ).copy() );
			}
		}
		return arr;
	}
	
	public cfData duplicate(){
		return duplicate( true );
	}
		
	public cfData duplicate( boolean _deepCopy ){

		cfArrayData arrCopy = copy();
		Vector<cfData> theData = arrCopy.data;
		Vector<cfData> clonedArrData = new Vector<cfData>();
		cfData clonedData;
    Object nextElement = null;
		
		int arrLen = theData.size();
		for ( int i = 0; i < arrLen; i++ ){
      nextElement = theData.get( i );
      if ( nextElement != null ){
      	if ( _deepCopy || ( (cfData) nextElement ).getDataType() == cfData.CFARRAYDATA ){
      		clonedData = ( (cfData) nextElement ).duplicate();
      	}else{
      		clonedData = ( (cfData) nextElement );
      	}
	   		if ( clonedData == null ){
	 	   		return null;
        }
        clonedArrData.add( clonedData );
      }else{
        clonedArrData.add( null );
      }
			
		}
		
		arrCopy.data = clonedArrData;
		return arrCopy;
	}
	
	public double getMax()throws dataNotSupportedException{
	  if ( data.size() == 0 ){
      return 0.0;
    }

    double max = getCfDataElement(0).getDouble();
    double temp;
	  for ( int x = 1; x < data.size(); x++ ){
      temp = getCfDataElement(x).getDouble();
	    if ( temp > max )
	      max = temp;
	  }
	  
	  return max;
	}
	
	public double getMin()throws dataNotSupportedException{
    if ( data.size() == 0 ){
      return 0.0;
    }
    
    double min = getCfDataElement(0).getDouble();
	  double temp;
	  for ( int x = 1; x < data.size(); x++ ){
	    temp = getCfDataElement(x).getDouble();
	    if ( temp < min )
	      min = temp;
	  }
	  
	  return min;
    
	}
	
	public double getAverage()throws dataNotSupportedException{
		if ( data.size() == 0 )	
			return 0;
			
    double sum = 0;
    for ( int x = 0; x < data.size(); x++ )
    	sum += getCfDataElement(x).getDouble();

	  return ( sum / data.size() );
	}
	
	public double getSum()throws dataNotSupportedException{
  	int size = data.size();
    double sum = 0;
    for ( int x = 0; x < size; x++ )
    	sum += getCfDataElement(x).getDouble();
    
	  return sum;
	}
	
	public cfData getElement( int _index ){
    return (cfData)data.get( ( _index - 1 ) );
	}
  
  
  // returns a cfData from the array from the given index.
  // @throws dataNotSupportedException if the element at the provided index is null.
  private cfData getCfDataElement( int _index ) throws dataNotSupportedException{
    Object element = data.get(_index);
    if ( element != null )
      return (cfData) element;
    else{
      throw new dataNotSupportedException( "The value of array element [" + (_index+1) + "] is undefined." );
    }
  }
	
  public String toString(){	return "{ARRAY:" + data + "}"; }
	
	/**
	 * Decides what type of sort to carry out on this cfArrayData.
	 *
	 * @param _type the kind of sort to carry out, numeric, text or textnocase.
	 * @param _order the order to do the sort, asc or desc.
	 * @exception dataNotSupportedException when the cfData does not implement this method.
	 */
	public void sortArray( String _type, String _order ) throws dataNotSupportedException{
	  if ( _type.equalsIgnoreCase("numeric") )
	    sortNumeric( _order );
	  else if ( _type.equalsIgnoreCase("text") )
	    sortText( _order );
	  else if ( _type.equalsIgnoreCase("textnocase") )
	    sortTextNoCase( _order );
	  else
	    throw new dataNotSupportedException();  
	}
	
	private void sortNumeric( String _order ) {
	  if ( _order == null || _order.equalsIgnoreCase("asc") ){

      Collections.sort( data, new Comparator<cfData>(){ 
      public int compare( cfData o1, cfData o2 ){
        try{
          if ( o1.getDouble() < o2.getDouble() )
            return -1;
          else
            return 1;
        } catch (dataNotSupportedException E){ return 0; }
      }
      } );
    
    }else{

      Collections.sort( data, new Comparator<cfData>(){ 
      public int compare( cfData o1, cfData o2 ){
        try{
          if ( o1.getDouble() > o2.getDouble() )
            return -1;
          else
            return 1;
        } catch (dataNotSupportedException E){ return 0; }
      }
      } );
  
    }
	}
	
	private void sortText( String _order ) {
	  if ( _order == null || _order.equalsIgnoreCase("asc") ){

      Collections.sort( data, new Comparator<cfData>(){ 
      public int compare( cfData o1, cfData o2 ){
        try{
          return o1.getString().compareTo( o2.getString() );
        } catch (dataNotSupportedException E){ return 0; }
      }
      } );

	  }else{

      Collections.sort( data, new Comparator<cfData>(){ 
      public int compare( cfData o1, cfData o2 ){
        try{
          return o2.getString().compareTo( o1.getString() );
        } catch (dataNotSupportedException E){ return 0; }
      }
      } );

    }
	}
	
	private void sortTextNoCase( String _order ) {
	  if ( _order == null || _order.equalsIgnoreCase("asc") )
	  {
        Collections.sort( data, new Comparator<cfData>()
          { 
	        public int compare( cfData o1, cfData o2 )
	        {
	          try
	          {
	            return o1.getString().toLowerCase().compareTo( o2.getString().toLowerCase() );
	          }
	          catch (dataNotSupportedException E){ return 0; }
	        }
          } );

	  }
	  else //desc
	  {
		Collections.reverse(data); //to fix bug #1177
        Collections.sort( data, new Comparator<cfData>()
          { 
            public int compare( cfData o1, cfData o2 )
            {
              try
              {
                return o2.getString().toLowerCase().compareTo( o1.getString().toLowerCase() );
              } catch (dataNotSupportedException E){ return 0; }
            }
          } );

      }
	}
	
	public String createList( String delimiter ) throws dataNotSupportedException{
		return createList( delimiter, null );
	}
	
	public String createList( String delimiter, String qualifier ) throws dataNotSupportedException{
		StringBuilder list = new StringBuilder();
		Iterator<cfData> iter = data.iterator();
		while ( iter.hasNext() ){
			cfData next = iter.next();
			if ( next != null ) {
				String nextStr = next.getString(); 
				if ( qualifier != null && nextStr.contains( delimiter ) ){
					nextStr = qualifier + nextStr + qualifier;
				}
				list.append( nextStr );
			}
			list.append( delimiter );
		}
		return ( data.size() > 0 ? list.toString().substring( 0, list.length()-delimiter.length() ) : "" );
	}

  public void dump( java.io.PrintWriter out ){
      dump( out, "", cfDUMP.TOP_DEFAULT );
  }
    
	public void dump( java.io.PrintWriter out, String _label, int _top ){
		out.write( "<table class='cfdump_table_array'>" );
		if ( data.size() > 0 ) {
			out.write( "<th class='cfdump_th_array' colspan='2'>" );
      if ( _label.length() > 0 ) out.write( _label + " - " );
      out.write( "array</th>" );
      int max = ( _top < data.size() ? _top : data.size() );
      
			for ( int x=0; x < max; x++ ){
				out.write( "<tr><td class='cfdump_td_array'>" );
				out.write( (x+1) + "" );
				out.write( "</td><td class='cfdump_td_value'>" );
	
				cfData element = (cfData)data.get( x );
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

	public void dumpWDDX( int version, java.io.PrintWriter out ){
    if ( version > 10 )
      out.write( "<a l='" );
    else
      out.write( "<array length='" );
    
		out.write( data.size() + "" );
		out.write( "'>" );
		
		for ( int x=0; x < data.size(); x++ ){
			if( data.get( x ) != null)
				((cfData)data.get( x )).dumpWDDX( version, out );	
		}
		
    if ( version > 10 )
      out.write( "</a>" );
    else
      out.write( "</array>" );
	}

	// Comparison and hashing

	/**
	 * Compares the specified object with this list for equality.  Returns
	 * <tt>true</tt> if and only if the specified object is also a list, both
	 * lists have the same size, and all corresponding pairs of elements in
	 * the two lists are <i>equal</i>.  (Two elements <tt>e1</tt> and
	 * <tt>e2</tt> are <i>equal</i> if <tt>(e1==null ? e2==null :
	 * e1.equals(e2))</tt>.)  In other words, two lists are defined to be
	 * equal if they contain the same elements in the same order.  This
	 * definition ensures that the equals method works properly across
	 * different implementations of the <tt>List</tt> interface.
	 *
	 * @param o the object to be compared for equality with this list.
	 * @return <tt>true</tt> if the specified object is equal to this list.
	 */
	public boolean equals( Object o ){
		if ( o instanceof cfArrayData )
			return data.equals( ((cfArrayData)o).data );
		
		return false;
	}

	public boolean equals( cfData o ){
		if ( o.getDataType() == cfData.CFARRAYDATA )
			return data.equals( ((cfArrayData)o).data );
		
		return false;
	}

	/**
	 * Returns the hash code value for this list.  The hash code of a list
	 * is defined to be the result of the following calculation:
	 * <pre>
	 *  hashCode = 1;
	 *  Iterator i = list.iterator();
	 *  while (i.hasNext()) {
	 *      Object obj = i.next();
	 *      hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
	 *  }
	 * </pre>
	 * This ensures that <tt>list1.equals(list2)</tt> implies that
	 * <tt>list1.hashCode()==list2.hashCode()</tt> for any two lists,
	 * <tt>list1</tt> and <tt>list2</tt>, as required by the general
	 * contract of <tt>Object.hashCode</tt>.
	 *
	 * @return the hash code value for this list.
	 * @see Object#hashCode()
	 * @see Object#equals(Object)
	 * @see #equals(Object)
	 */
	public int hashCode() {
		return data.hashCode();
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

		for ( int x = 0; x < data.size(); x++ ){
			args.clear();
			args.add( data.get(x) );
			udf.execute( SessionData.Session, args );			
		}
	}
}