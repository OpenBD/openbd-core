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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.naryx.tagfusion.cfm.tag.cfDUMP;
import com.naryx.tagfusion.cfm.tag.tagUtils;

/**
 * This class extends the regular cfArrayData with the purpose of giving
 * access to java.util.List (and subclasses of) instances.
 * This will give us the ability to call methods of a subclass of List 
 * that aren't in the declared List methods.
 * 
 */
public class cfJavaArrayData extends cfArrayData implements List<Object> {
	
	static final long serialVersionUID = 1;
  
	private List<Object> theList;
	
	public cfJavaArrayData( List<Object> _list ){
		super();
		theList 		= _list;
		dimensions 	= 1;
		setInstance( _list );
	}

	protected void setSize( int indx ) {
		while ( theList.size() < indx ){
			theList.add( null );
		}
	}
  
	
  public cfData getData( cfData arrayIndex ) throws cfmRunTimeException{
  	int indx;
    try{
      indx = arrayIndex.getInt();
    }catch(Exception E){ 
    	cfCatchData	catchData	= new cfCatchData();
  		catchData.setType( cfCatchData.TYPE_APPLICATION );
    	catchData.setMessage( "Attempted to access array with invalid array index." );
    	throw new cfmRunTimeException( catchData );
    }
    
  	if ( indx > theList.size()  ){
  		setSize(indx);
  	}else if ( indx < 1 ){
    	cfCatchData	catchData	= new cfCatchData();
    	catchData.setType( cfCatchData.TYPE_APPLICATION );
    	catchData.setMessage( "Attempted to access array with invalid array index [" + indx + "]." );
    	throw new cfmRunTimeException( catchData );
  	}
  	
  	return tagUtils.convertToCfData( theList.get( indx-1 ) );
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

  public void setData( int _index, cfData _element ){
		if ( _index > theList.size() ){
			setSize( _index );
		}
		
		theList.set( ( _index - 1 ), tagUtils.getNatural( _element ) );
  }
  
  
	public void addElement( cfData _element ){
	  theList.add( tagUtils.getNatural( _element ) );
	}
	
	public void addElementAt( cfData _element, int _index ){
		theList.add( ( _index - 1 ), tagUtils.getNatural( _element ) );
	}
	
	
	public void elementSwap(int _start, int _end) {
	  Object first = theList.get( ( _start - 1 ) );
	  Object second = theList.get( ( _end - 1 ) );
	  theList.set( ( _end - 1 ), first );
	  theList.set( ( _start - 1 ), second );
	}
	
	public void removeAllElements() {
		theList.clear();
	}
	
	public void removeElementAt(int _no) {
		theList.remove( _no - 1 );
	}
	

	public void setElements(int _start, int _end, cfData _value) {
		for ( int x = _start; x <= _end ; x++ ){
			this.setData( x, _value );
		}
	}
	

	public void sortArray(String _type, String _order )
			throws dataNotSupportedException {
		String order = ( _order == null ? "asc" : _order );
		Comparator<Object> comparator = null;
		if ( _type.equalsIgnoreCase( "text" ) ){
			if ( order.equalsIgnoreCase("asc") ){
				comparator = new textComparator( textComparator.ASC, true );
			}else{
				comparator = new textComparator( textComparator.DESC, true );
			}
		}else	if ( _type.equalsIgnoreCase( "textnocase" ) ){
			if ( order.equalsIgnoreCase("asc") ){
				comparator = new textComparator( textComparator.ASC, true );
			}else{
				comparator = new textComparator( textComparator.DESC, true );
			}
			Collections.sort( theList, comparator );

		}else{
			try{
				comparator = new numericComparator( order.equalsIgnoreCase("asc") ? numericComparator.ASC : numericComparator.DESC );
				Collections.sort( theList, comparator );
			}catch( ClassCastException e ){
				throw new dataNotSupportedException( "Cannot sort an array that contains non-numeric values numerically." );
			}
		}
		
	}
	
	public void setCapacity( int _size ){
		setSize( _size );
	}

	
	public cfArrayData copy(){
		List<Object> duplicateList = new ArrayList<Object>();
		duplicateList.addAll( theList );
		return new cfJavaArrayData( duplicateList );

	}
  
	public cfData duplicate(){
		return copy();
	}
  
	public cfData duplicate( boolean _deepCopy ){
		return duplicate();
	}
	
	public double getMax()throws dataNotSupportedException{
		double max;
		double nextValue;
		
		Object nextItem = theList.get(0);
		if ( nextItem instanceof Number ){
			max = ( (Number) nextItem).doubleValue();
		}else{
			throw new dataNotSupportedException( "Array contains non-numeric." );
		}
		
		for ( int i = 1; i < theList.size(); i++ ){
			nextItem = theList.get(i);
			if ( nextItem instanceof Number ){
				nextValue = ( (Number) nextItem).doubleValue();
				if ( nextValue > max ){
					max = nextValue;
				}
			}else{
				throw new dataNotSupportedException( "Array contains non-numeric." );
			}
		}
		
		return max;
	}
	
	public double getMin()throws dataNotSupportedException{
		double min;
		double nextValue;
		
		Object nextItem = theList.get(0);
		if ( nextItem instanceof Number ){
			min = ( (Number) nextItem).doubleValue();
		}else{
			throw new dataNotSupportedException( "Array contains non-numeric." );
		}
		
		for ( int i = 1; i < theList.size(); i++ ){
			nextItem = theList.get(i);
			if ( nextItem instanceof Number ){
				nextValue = ( (Number) nextItem).doubleValue();
				if ( nextValue < min ){
					min = nextValue;
				}
			}else{
				throw new dataNotSupportedException( "Array contains non-numeric." );
			}
		}
		
		return min;
	}
	
	
	public double getAverage()throws dataNotSupportedException{
		return getSum() / size();
	}
	
	public double getSum()throws dataNotSupportedException{
		Object nextItem;
		double sum = 0.0;
		for ( int i = 0; i < theList.size(); i++ ){
			nextItem = theList.get(i);
			if ( nextItem instanceof Number ){
				sum += ( (Number) nextItem).doubleValue();
			}else{
				throw new dataNotSupportedException( "Array contains non-numeric." );
			}
		}
		return sum;
	}
	
	
	public cfData getElement( int _index ){
    return tagUtils.convertToCfData( theList.get( ( _index - 1 ) ) );
	}
	
  public String toString(){	return "{ARRAY:" + theList + "}"; }
  
  
  
	public String createList( String delimiter ) {
		StringBuilder list = new StringBuilder();
		Iterator<Object> iter = theList.iterator();
		while ( iter.hasNext() )
		{
			Object next = iter.next();
			if ( next != null ) {
				list.append( next.toString() );
			}
			list.append( delimiter );
		}
		return ( theList.size() > 0 ? list.toString().substring( 0, list.length()-1 ) : "" );
	}
	
  public void dump( java.io.PrintWriter out ){
    dump( out, "", cfDUMP.TOP_DEFAULT );
  }
  
  public void dump( java.io.PrintWriter out, String _label, int _top ){
		out.write( "<table class='cfdump_table_array'>" );
		if ( theList.size() > 0 ) {
			out.write( "<th class='cfdump_th_array' colspan='2'>");
      if ( _label.length() > 0 ) out.write( _label + " - " );
      out.write( "array</th>" );
			for ( int x=0; x < theList.size(); x++ ){
				out.write( "<tr><td class='cfdump_td_array'>" );
				out.write( (x+1) + "" );
				out.write( "</td><td class='cfdump_td_value'>" );
	
				cfData element = tagUtils.convertToCfData( theList.get( x ) );
				if ( ( element == null ) || ( element.getDataType() == cfData.CFNULLDATA ) )
					out.write( "[undefined array element]" );
				else
					element.dump(out);  
	      
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
    
		out.write( theList.size() + "" );
		out.write( "'>" );
		
		for ( int x=0; x < theList.size(); x++ ){
			if( theList.get( x ) != null)
				(tagUtils.convertToCfData(theList.get( x ))).dumpWDDX( version, out );	
		}
		
    if ( version > 10 )
      out.write( "</a>" );
    else
      out.write( "</array>" );
	}
	
	/*******************************************************************************
	 *	The following methods implement the java.util.List interface. They're
	 *	implemented to support variable sharing between CFML and servlets/JSP
	 *	pages. These methods are just wrappers around the internal "theList" attribute.
	 *
	 *******************************************************************************/
	
	/**
	 * Returns the number of elements in this list.  If this list contains
	 * more than <tt>Integer.MAX_VALUE</tt> elements, returns
	 * <tt>Integer.MAX_VALUE</tt>.
	 *
	 * @return the number of elements in this list.
	 */
	public int size() {
		return theList.size();
	}

	/**
	 * Returns <tt>true</tt> if this list contains no elements.
	 *
	 * @return <tt>true</tt> if this list contains no elements.
	 */
	public boolean isEmpty() {
		return theList.isEmpty();
	}

	/**
	 * 
	 * Returns <tt>true</tt> if this list contains the specified element.
	 * More formally, returns <tt>true</tt> if and only if this list contains
	 * at least one element <tt>e</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
	 *
	 * @param o element whose presence in this list is to be tested.
	 * @return <tt>true</tt> if this list contains the specified element.
	 */
	public boolean contains( Object o ) {
		return theList.contains( o );	
	}

	/**
	 * Returns an iterator over the elements in this list in proper sequence.
	 *
	 * @return an iterator over the elements in this list in proper sequence.
	 */
	public Iterator<Object> iterator() {
		return theList.iterator();
	}
	
	/**
	 * Returns a list iterator of the elements in this list (in proper sequence).
	 */
	public ListIterator<Object> listIterator() {
		return theList.listIterator();
	}
	
	/**
	 * Returns a list iterator of the elements in this list (in proper sequence),
	 * starting at the specified position in this list.
	 */	 
	public ListIterator<Object> listIterator( int index ) {
		return theList.listIterator( index );
	}

	/**
	 * Returns an array containing all of the elements in this list in proper
	 * sequence.  Obeys the general contract of the
	 * <tt>Collection.toArray</tt> method.
	 *
	 * @return an array containing all of the elements in this list in proper
	 *	       sequence.
	 * @see Arrays#asList(Object[])
	 */
	public Object[] toArray()
	{
		return theList.toArray();
	}

	/**
	 * Returns an array containing all of the elements in this list in proper
	 * sequence; the runtime type of the returned array is that of the
	 * specified array.  Obeys the general contract of the
	 * <tt>Collection.toArray(Object[])</tt> method.
	 *
	 * @param a the array into which the elements of this list are to
	 *		be stored, if it is big enough; otherwise, a new array of the
	 * 		same runtime type is allocated for this purpose.
	 * @return  an array containing the elements of this list.
	 * 
	 * @throws ArrayStoreException if the runtime type of the specified array
	 * 		  is not a supertype of the runtime type of every element in
	 * 		  this list.
	 */
	@SuppressWarnings("unchecked")
	public Object[] toArray( Object a[] ) {
		return theList.toArray( a );
	}


	// Modification Operations

	/**
	 * Appends the specified element to the end of this list (optional
	 * operation). <p>
	 *
	 * Lists that support this operation may place limitations on what
	 * elements may be added to this list.  In particular, some
	 * lists will refuse to add null elements, and others will impose
	 * restrictions on the type of elements that may be added.  List
	 * classes should clearly specify in their documentation any restrictions
	 * on what elements may be added.
	 *
	 * @param o element to be appended to this list.
	 * @return <tt>true</tt> (as per the general contract of the
	 *            <tt>Collection.add</tt> method).
	 * 
	 * @throws UnsupportedOperationException if the <tt>add</tt> method is not
	 * 		  supported by this list.
	 * @throws ClassCastException if the class of the specified element
	 * 		  prevents it from being added to this list.
	 * @throws IllegalArgumentException if some aspect of this element
	 *            prevents it from being added to this collection.
	 */
	public boolean add( Object o ) {
		return theList.add( o );
	}

	/**
	 * Removes the first occurrence in this list of the specified element 
	 * (optional operation).  If this list does not contain the element, it is
	 * unchanged.  More formally, removes the element with the lowest index i
	 * such that <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt> (if
	 * such an element exists).
	 *
	 * @param o element to be removed from this list, if present.
	 * @return <tt>true</tt> if this list contained the specified element.
	 * 
	 * @throws UnsupportedOperationException if the <tt>remove</tt> method is
	 *		  not supported by this list.
	 */
	public boolean remove( Object o ) {
		return theList.remove( o );
	}


	// Bulk Modification Operations

	/**
	 * 
	 * Returns <tt>true</tt> if this list contains all of the elements of the
	 * specified collection.
	 *
	 * @param c collection to be checked for containment in this list.
	 * @return <tt>true</tt> if this list contains all of the elements of the
	 * 	       specified collection.
	 * 
	 * @see #contains(Object)
	 */
	public boolean containsAll( Collection<?> c ) {
		return theList.containsAll( c );
	}

	/**
	 * Appends all of the elements in the specified collection to the end of
	 * this list, in the order that they are returned by the specified
	 * collection's iterator (optional operation).  The behavior of this
	 * operation is unspecified if the specified collection is modified while
	 * the operation is in progress.  (Note that this will occur if the
	 * specified collection is this list, and it's nonempty.)
	 *
	 * @param c collection whose elements are to be added to this list.
	 * @return <tt>true</tt> if this list changed as a result of the call.
	 * 
	 * @throws UnsupportedOperationException if the <tt>addAll</tt> method is
	 *         not supported by this list.
	 * 
	 * @throws ClassCastException if the class of an element in the specified
	 * 	       collection prevents it from being added to this list.
	 * 
	 * @throws IllegalArgumentException if some aspect of an element in the
	 *         specified collection prevents it from being added to this
	 *         list.
	 * 
	 * @see #add(Object)
	 */
	public boolean addAll( Collection<?> c ) {
		return theList.addAll( c );
	}

	/**
	 * Inserts all of the elements in the specified collection into this
	 * list at the specified position (optional operation).  Shifts the
	 * element currently at that position (if any) and any subsequent
	 * elements to the right (increases their indices).  The new elements
	 * will appear in this list in the order that they are returned by the
	 * specified collection's iterator.  The behavior of this operation is
	 * unspecified if the specified collection is modified while the
	 * operation is in progress.  (Note that this will occur if the specified
	 * collection is this list, and it's nonempty.)
	 *
	 * @param index index at which to insert first element from the specified
	 *	            collection.
	 * @param c elements to be inserted into this list.
	 * @return <tt>true</tt> if this list changed as a result of the call.
	 * 
	 * @throws UnsupportedOperationException if the <tt>addAll</tt> method is
	 *		  not supported by this list.
	 * @throws ClassCastException if the class of one of elements of the
	 * 		  specified collection prevents it from being added to this
	 * 		  list.
	 * @throws IllegalArgumentException if some aspect of one of elements of
	 *		  the specified collection prevents it from being added to
	 *		  this list.
	 * @throws IndexOutOfBoundsException if the index is out of range (index
	 *		  &lt; 0 || index &gt; size()).
	 */
	public boolean addAll( int index, Collection<?> c ) {
		return theList.addAll( index, c );
	}

	/**
	 * Removes from this list all the elements that are contained in the
	 * specified collection (optional operation).
	 *
	 * @param c collection that defines which elements will be removed from
	 *          this list.
	 * @return <tt>true</tt> if this list changed as a result of the call.
	 * 
	 * @throws UnsupportedOperationException if the <tt>removeAll</tt> method
	 * 		  is not supported by this list.
	 * 
	 * @see #remove(Object)
	 * @see #contains(Object)
	 */
	public boolean removeAll( Collection<?> c ) {
		return theList.removeAll( c );
	}

	/**
	 * Retains only the elements in this list that are contained in the
	 * specified collection (optional operation).  In other words, removes
	 * from this list all the elements that are not contained in the specified
	 * collection.
	 *
	 * @param c collection that defines which elements this set will retain.
	 * 
	 * @return <tt>true</tt> if this list changed as a result of the call.
	 * 
	 * @throws UnsupportedOperationException if the <tt>retainAll</tt> method
	 * 		  is not supported by this list.
	 * 
	 * @see #remove(Object)
	 * @see #contains(Object)
	 */
	public boolean retainAll( Collection<?> c ) {
		return theList.retainAll( c );
	}

	/**
	 * Removes all of the elements from this list (optional operation).  This
	 * list will be empty after this call returns (unless it throws an
	 * exception).
	 *
	 * @throws UnsupportedOperationException if the <tt>clear</tt> method is
	 * 		  not supported by this list.
	 */
	public void clear() {
		theList.clear();
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
	public boolean equals( Object o )
	{
		return theList.equals( o );
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
		return theList.hashCode();
	}


	// Positional Access Operations

	/**
	 * Returns the element at the specified position in this list.
	 *
	 * @param index index of element to return.
	 * @return the element at the specified position in this list.
	 * 
	 * @throws IndexOutOfBoundsException if the index is out of range (index
	 * 		  &lt; 0 || index &gt;= size()).
	 */
	public Object get( int index ) {
		return theList.get( index );
	}

	/**
	 * Replaces the element at the specified position in this list with the
	 * specified element (optional operation).
	 *
	 * @param index index of element to replace.
	 * @param element element to be stored at the specified position.
	 * @return the element previously at the specified position.
	 * 
	 * @throws UnsupportedOperationException if the <tt>set</tt> method is not
	 *		  supported by this list.
	 * @throws    ClassCastException if the class of the specified element
	 * 		  prevents it from being added to this list.
	 * @throws    IllegalArgumentException if some aspect of the specified
	 *		  element prevents it from being added to this list.
	 * @throws    IndexOutOfBoundsException if the index is out of range
	 *		  (index &lt; 0 || index &gt;= size()).  */
	public Object set( int index, Object element ) {
		return theList.set( index, element );
	}

	/**
	 * Inserts the specified element at the specified position in this list
	 * (optional operation).  Shifts the element currently at that position
	 * (if any) and any subsequent elements to the right (adds one to their
	 * indices).
	 *
	 * @param index index at which the specified element is to be inserted.
	 * @param element element to be inserted.
	 * 
	 * @throws UnsupportedOperationException if the <tt>add</tt> method is not
	 *		  supported by this list.
	 * @throws    ClassCastException if the class of the specified element
	 * 		  prevents it from being added to this list.
	 * @throws    IllegalArgumentException if some aspect of the specified
	 *		  element prevents it from being added to this list.
	 * @throws    IndexOutOfBoundsException if the index is out of range
	 *		  (index &lt; 0 || index &gt; size()).
	 */
	public void add( int index, Object element ) {
		theList.add( index, element );
	}

	/**
	 * Removes the element at the specified position in this list (optional
	 * operation).  Shifts any subsequent elements to the left (subtracts one
	 * from their indices).  Returns the element that was removed from the
	 * list.
	 *
	 * @param index the index of the element to removed.
	 * @return the element previously at the specified position.
	 * 
	 * @throws UnsupportedOperationException if the <tt>remove</tt> method is
	 *		  not supported by this list.
	 * 
	 * @throws IndexOutOfBoundsException if the index is out of range (index
	 *            &lt; 0 || index &gt;= size()).
	 */
	public Object remove( int index ) {
		return theList.remove( index );
	}


	// Search Operations

	/**
	 * Returns the index in this list of the first occurrence of the specified
	 * element, or -1 if this list does not contain this element.
	 * More formally, returns the lowest index <tt>i</tt> such that
	 * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>,
	 * or -1 if there is no such index.
	 *
	 * @param o element to search for.
	 * @return the index in this list of the first occurrence of the specified
	 * 	       element, or -1 if this list does not contain this element.
	 */
	public int indexOf( Object o ) {
		return theList.indexOf( o );
	}

	/**
	 * Returns the index in this list of the last occurrence of the specified
	 * element, or -1 if this list does not contain this element.
	 * More formally, returns the highest index <tt>i</tt> such that
	 * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>,
	 * or -1 if there is no such index.
	 *
	 * @param o element to search for.
	 * @return the index in this list of the last occurrence of the specified
	 * 	       element, or -1 if this list does not contain this element.
	 */
	public int lastIndexOf( Object o ) {
		return theList.lastIndexOf( o );
	}

	// View

	/**
	 * Returns a view of the portion of this list between the specified
	 * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.  (If
	 * <tt>fromIndex</tt> and <tt>toIndex</tt> are equal, the returned list is
	 * empty.)  The returned list is backed by this list, so changes in the
	 * returned list are reflected in this list, and vice-versa.  The returned
	 * list supports all of the optional list operations supported by this
	 * list.<p>
	 *
	 * This method eliminates the need for explicit range operations (of
	 * the sort that commonly exist for arrays).   Any operation that expects
	 * a list can be used as a range operation by passing a subList view
	 * instead of a whole list.  For example, the following idiom
	 * removes a range of elements from a list:
	 * <pre>
	 *	    list.subList(from, to).clear();
	 * </pre>
	 * Similar idioms may be constructed for <tt>indexOf</tt> and
	 * <tt>lastIndexOf</tt>, and all of the algorithms in the
	 * <tt>Collections</tt> class can be applied to a subList.<p>
	 *
	 * The semantics of this list returned by this method become undefined if
	 * the backing list (i.e., this list) is <i>structurally modified</i> in
	 * any way other than via the returned list.  (Structural modifications are
	 * those that change the size of this list, or otherwise perturb it in such
	 * a fashion that iterations in progress may yield incorrect results.)
	 *
	 * @param fromIndex low endpoint (inclusive) of the subList.
	 * @param toIndex high endpoint (exclusive) of the subList.
	 * @return a view of the specified range within this list.
	 * 
	 * @throws IndexOutOfBoundsException for an illegal endpoint index value
	 *     (fromIndex &lt; 0 || toIndex &gt; size || fromIndex &gt; toIndex).
	 */
	public List<Object> subList( int fromIndex, int toIndex ) {
		return theList.subList( fromIndex, toIndex );
	}
	
	/**
	 * Comparator implementations for sortArray()
	 */
  class textComparator implements Comparator<Object> {
  	
  	public static final byte ASC=0, DESC=1;
  	
  	private byte type;
  	private boolean casesensitive;
  	
  	public textComparator( byte _type, boolean _case ){
  		type = _type;
  		casesensitive = _case;
  	}
  	
  	public int compare( Object _o1, Object _o2 ){
  		int result;
  		if ( casesensitive ){
  			result = _o1.toString().compareTo( _o2.toString() );
  		}else{
  			result = _o1.toString().compareToIgnoreCase( _o2.toString() );
  		}
  		
  		if ( type == ASC ){
  			return result; 
  		}else{
  			return -1 * result;
  		}
  	}
  }

  class numericComparator implements Comparator<Object> {
  	
  	public static final byte ASC=0, DESC=1;
  	
  	private byte type;
  	
  	public numericComparator( byte _type ){
  		type = _type;
  	}
  	
  	public int compare( Object _o1, Object _o2 ){
  		int result;
  		if ( !(_o1 instanceof Number && _o2 instanceof Number ))
  			throw new ClassCastException();
  		
  		if ( _o1.equals( _o2 ) ){
  			result = 0;
  		}else if ( ((Number) _o1).floatValue() < ((Number) _o2).floatValue() ){
  			result = -1;
  		}else{
  			result = 1;
  		}

  		if ( type == ASC ){
  			return result; 
  		}else{
  			return -1 * result;
  		}
  	}
  }

}
