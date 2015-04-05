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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import com.naryx.tagfusion.cfm.tag.tagUtils;

public class cfArrayListData extends cfArrayData implements List<Object> {

	private static final long serialVersionUID = 1;

	protected cfArrayListData( int _dimensions ){
		super( _dimensions );
	}
	
	protected cfArrayListData( int _dimensions, Vector<? extends cfData> _data ) {
		super( _dimensions, _data );
	}
  
  /*******************************************************************************
   *  The following methods implement the java.util.List interface. They're
   *  implemented to support variable sharing between CFML and servlets/JSP
   *  pages. These methods are mostly just wrappers
   *  around the internal "data" attribute that automatically convert between
   *  internal BlueDragon data types and "natural" Java data types.
   *
   *  TODO: there's a bunch of unsupported methods here that could be supported
   *  quite easily.
   *******************************************************************************/

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
    return data.contains( tagUtils.convertToCfData( o ) );  
  }

  /**
   * Returns an iterator over the elements in this list in proper sequence.
   *
   * @return an iterator over the elements in this list in proper sequence.
   */
  public Iterator<Object> iterator() {
    return new cfArrayDataIterator( data.iterator() );
  }
  
  /**
   * Returns a list iterator of the elements in this list (in proper sequence).
   */
  public ListIterator<Object> listIterator() {
    return new cfArrayDataListIterator( data.listIterator() );
  }
  
  /**
   * Returns a list iterator of the elements in this list (in proper sequence),
   * starting at the specified position in this list.
   */  
  public ListIterator<Object> listIterator( int index ) {
    return new cfArrayDataListIterator( data.listIterator( index ) );
  }

  /**
   * Returns an array containing all of the elements in this list in proper
   * sequence.  Obeys the general contract of the
   * <tt>Collection.toArray</tt> method.
   *
   * @return an array containing all of the elements in this list in proper
   *         sequence.
   * @see Arrays#asList(Object[])
   */
  public Object[] toArray()
  {
    Object[] objArray = new Object[ this.size() ];
    
    for ( int i = 0; i < this.size(); i++ ) {
      objArray[ i ] = this.get( i );
    }
    
    return objArray;
  }

  /**
   * Returns an array containing all of the elements in this list in proper
   * sequence; the runtime type of the returned array is that of the
   * specified array.  Obeys the general contract of the
   * <tt>Collection.toArray(Object[])</tt> method.
   *
   * @param a the array into which the elements of this list are to
   *    be stored, if it is big enough; otherwise, a new array of the
   *    same runtime type is allocated for this purpose.
   * @return  an array containing the elements of this list.
   * 
   * @throws ArrayStoreException if the runtime type of the specified array
   *      is not a supertype of the runtime type of every element in
   *      this list.
   */
  @SuppressWarnings("unchecked")
  public Object[] toArray( Object a[] ) {
    throw new UnsupportedOperationException();
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
   *      supported by this list.
   * @throws ClassCastException if the class of the specified element
   *      prevents it from being added to this list.
   * @throws IllegalArgumentException if some aspect of this element
   *            prevents it from being added to this collection.
   */
  public boolean add( Object o ) {
    return data.add( tagUtils.convertToCfData( o ) );
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
   *      not supported by this list.
   */
  public boolean remove( Object o ) {
  	for ( int i = 0; i < data.size(); i++ ){
  		if ( tagUtils.getNatural( data.get(i) ).equals( o ) ){
  			data.remove(i);
  			return true;
  		}
  	}
  	
  	return false;
  }


  // Bulk Modification Operations

  /**
   * 
   * Returns <tt>true</tt> if this list contains all of the elements of the
   * specified collection.
   *
   * @param c collection to be checked for containment in this list.
   * @return <tt>true</tt> if this list contains all of the elements of the
   *         specified collection.
   * 
   * @see #contains(Object)
   */
  public boolean containsAll( Collection<?> c ) {
    throw new UnsupportedOperationException();
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
   *         collection prevents it from being added to this list.
   * 
   * @throws IllegalArgumentException if some aspect of an element in the
   *         specified collection prevents it from being added to this
   *         list.
   * 
   * @see #add(Object)
   */
  public boolean addAll( Collection<?> c ) {
  	synchronized(c){
  		Iterator<?> it = c.iterator();
  		while( it.hasNext() ){
  			add( it.next() );
  		}
  	}
  	// list will have changed if collection has at least 1 item
  	return c.size() > 0; 
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
   *              collection.
   * @param c elements to be inserted into this list.
   * @return <tt>true</tt> if this list changed as a result of the call.
   * 
   * @throws UnsupportedOperationException if the <tt>addAll</tt> method is
   *      not supported by this list.
   * @throws ClassCastException if the class of one of elements of the
   *      specified collection prevents it from being added to this
   *      list.
   * @throws IllegalArgumentException if some aspect of one of elements of
   *      the specified collection prevents it from being added to
   *      this list.
   * @throws IndexOutOfBoundsException if the index is out of range (index
   *      &lt; 0 || index &gt; size()).
   */
  public boolean addAll( int index, Collection<?> c ) {
  	synchronized(c){
  		int offset = index;
  		Iterator<?> it = c.iterator();
  		while( it.hasNext() ){
  			add( offset++, it.next() );
  		}
  	}
  	// list will have changed if collection has at least 1 item
  	return c.size() > 0;
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
   *      is not supported by this list.
   * 
   * @see #remove(Object)
   * @see #contains(Object)
   */
  public boolean removeAll( Collection<?> c ) {
  	boolean changed = false;
  	int index = 0;
  	while ( index < data.size() ){
  		Object natural = tagUtils.getNatural( data.get(index) );
  		
  		Iterator<?> cIterator = c.iterator();
  		while ( cIterator.hasNext() ){
  			if ( natural.equals( cIterator.next() ) ){
  				data.remove( index );
  				changed = true;
  				index--; // offset the increment
  				break;
  			}
  		}
  		index++;
  	}
  	
  	return changed;
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
   *      is not supported by this list.
   * 
   * @see #remove(Object)
   * @see #contains(Object)
   */
  public boolean retainAll( Collection<?> c ) {
    throw new UnsupportedOperationException();
  }

  /**
   * Removes all of the elements from this list (optional operation).  This
   * list will be empty after this call returns (unless it throws an
   * exception).
   *
   * @throws UnsupportedOperationException if the <tt>clear</tt> method is
   *      not supported by this list.
   */
  public void clear() {
    data.clear();
  }

  // Positional Access Operations

  /**
   * Returns the element at the specified position in this list.
   *
   * @param index index of element to return.
   * @return the element at the specified position in this list.
   * 
   * @throws IndexOutOfBoundsException if the index is out of range (index
   *      &lt; 0 || index &gt;= size()).
   */
  public Object get( int index ) {
    return tagUtils.getNatural( data.get( index ) );
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
   *      supported by this list.
   * @throws    ClassCastException if the class of the specified element
   *      prevents it from being added to this list.
   * @throws    IllegalArgumentException if some aspect of the specified
   *      element prevents it from being added to this list.
   * @throws    IndexOutOfBoundsException if the index is out of range
   *      (index &lt; 0 || index &gt;= size()).  */
  public Object set( int index, Object element ) {
    return tagUtils.getNatural( data.set( index, tagUtils.convertToCfData( element ) ) );
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
   *      supported by this list.
   * @throws    ClassCastException if the class of the specified element
   *      prevents it from being added to this list.
   * @throws    IllegalArgumentException if some aspect of the specified
   *      element prevents it from being added to this list.
   * @throws    IndexOutOfBoundsException if the index is out of range
   *      (index &lt; 0 || index &gt; size()).
   */
  public void add( int index, Object element ) {
    data.add( index, tagUtils.convertToCfData( element ) );
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
   *      not supported by this list.
   * 
   * @throws IndexOutOfBoundsException if the index is out of range (index
   *            &lt; 0 || index &gt;= size()).
   */
  public Object remove( int index ) {
    return tagUtils.getNatural( data.remove( index ) );
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
   *         element, or -1 if this list does not contain this element.
   */
  public int indexOf( Object o ) {
    throw new UnsupportedOperationException();
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
   *         element, or -1 if this list does not contain this element.
   */
  public int lastIndexOf( Object o ) {
    throw new UnsupportedOperationException();
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
   *      list.subList(from, to).clear();
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
    throw new UnsupportedOperationException();
  }
  
  public class cfArrayDataIterator implements Iterator<Object> {
    
    protected Iterator<cfData> iter;
    
    public cfArrayDataIterator( Iterator<cfData> _iter ) {
      iter = _iter;
    }
    
    /**
     * Returns <tt>true</tt> if this list iterator has more elements when
     * traversing the list in the forward direction. (In other words, returns
     * <tt>true</tt> if <tt>next</tt> would return an element rather than
     * throwing an exception.)
     *
     * @return <tt>true</tt> if the list iterator has more elements when
     *    traversing the list in the forward direction.
     */
    public boolean hasNext() {
      return iter.hasNext();
    }

    /**
     * Returns the next element in the list.  This method may be called
     * repeatedly to iterate through the list, or intermixed with calls to
     * <tt>previous</tt> to go back and forth.  (Note that alternating calls
     * to <tt>next</tt> and <tt>previous</tt> will return the same element
     * repeatedly.)
     *
     * @return the next element in the list.
     * @exception NoSuchElementException if the iteration has no next element.
     */
    public Object next() {
      return tagUtils.getNatural( iter.next() );
    }
    
    /**
     * Removes from the list the last element that was returned by
     * <tt>next</tt> or <tt>previous</tt> (optional operation).  This call can
     * only be made once per call to <tt>next</tt> or <tt>previous</tt>.  It
     * can be made only if <tt>ListIterator.add</tt> has not been called after
     * the last call to <tt>next</tt> or <tt>previous</tt>.
     *
     * @exception UnsupportedOperationException if the <tt>remove</tt>
     *      operation is not supported by this list iterator.
     * @exception IllegalStateException neither <tt>next</tt> nor
     *      <tt>previous</tt> have been called, or <tt>remove</tt> or
     *      <tt>add</tt> have been called after the last call to *
     *      <tt>next</tt> or <tt>previous</tt>.
     */
    public void remove() {
      iter.remove();
    }
  }
  
  public class cfArrayDataListIterator extends cfArrayDataIterator implements ListIterator<Object> {
    
    public cfArrayDataListIterator( ListIterator<cfData> _iter ) {
      super( _iter );
    }
    
    /**
     * Returns <tt>true</tt> if this list iterator has more elements when
     * traversing the list in the reverse direction.  (In other words, returns
     * <tt>true</tt> if <tt>previous</tt> would return an element rather than
     * throwing an exception.)
     *
     * @return <tt>true</tt> if the list iterator has more elements when
     *         traversing the list in the reverse direction.
     */
    public boolean hasPrevious() {
      return ((ListIterator<cfData>)iter).hasPrevious();
    }

    /**
     * Returns the previous element in the list.  This method may be called
     * repeatedly to iterate through the list backwards, or intermixed with
     * calls to <tt>next</tt> to go back and forth.  (Note that alternating
     * calls to <tt>next</tt> and <tt>previous</tt> will return the same
     * element repeatedly.)
     *
     * @return the previous element in the list.
     * 
     * @exception NoSuchElementException if the iteration has no previous
     *            element.
     */
    public Object previous() {
      return ((ListIterator<cfData>)iter).previous();
    }

    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to <tt>next</tt>. (Returns list size if the list iterator is at the
     * end of the list.)
     *
     * @return the index of the element that would be returned by a subsequent
     *         call to <tt>next</tt>, or list size if list iterator is at end
     *         of list. 
     */
    public int nextIndex() {
      return ((ListIterator<cfData>)iter).nextIndex();
    }

    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to <tt>previous</tt>. (Returns -1 if the list iterator is at the
     * beginning of the list.)
     *
     * @return the index of the element that would be returned by a subsequent
     *         call to <tt>previous</tt>, or -1 if list iterator is at
     *         beginning of list.
     */ 
    public int previousIndex() {
      return ((ListIterator<cfData>)iter).previousIndex();
    }


    // Modification Operations

    /**
     * Replaces the last element returned by <tt>next</tt> or
     * <tt>previous</tt> with the specified element (optional operation).
     * This call can be made only if neither <tt>ListIterator.remove</tt> nor
     * <tt>ListIterator.add</tt> have been called after the last call to
     * <tt>next</tt> or <tt>previous</tt>.
     *
     * @param o the element with which to replace the last element returned by
     *          <tt>next</tt> or <tt>previous</tt>.
     * @exception UnsupportedOperationException if the <tt>set</tt> operation
     *      is not supported by this list iterator.
     * @exception ClassCastException if the class of the specified element
     *      prevents it from being added to this list.
     * @exception IllegalArgumentException if some aspect of the specified
     *      element prevents it from being added to this list.
     * @exception IllegalStateException if neither <tt>next</tt> nor
     *            <tt>previous</tt> have been called, or <tt>remove</tt> or
     *      <tt>add</tt> have been called after the last call to
     *      <tt>next</tt> or <tt>previous</tt>.
     */
    public void set( Object o ) {
      ((ListIterator<cfData>)iter).set( tagUtils.convertToCfData( o ) );
    }

    /**
     * Inserts the specified element into the list (optional operation).  The
     * element is inserted immediately before the next element that would be
     * returned by <tt>next</tt>, if any, and after the next element that
     * would be returned by <tt>previous</tt>, if any.  (If the list contains
     * no elements, the new element becomes the sole element on the list.)
     * The new element is inserted before the implicit cursor: a subsequent
     * call to <tt>next</tt> would be unaffected, and a subsequent call to
     * <tt>previous</tt> would return the new element.  (This call increases
     * by one the value that would be returned by a call to <tt>nextIndex</tt>
     * or <tt>previousIndex</tt>.)
     *
     * @param o the element to insert.
     * @exception UnsupportedOperationException if the <tt>add</tt> method is
     *      not supported by this list iterator.
     * 
     * @exception ClassCastException if the class of the specified element
     *      prevents it from being added to this Set.
     * 
     * @exception IllegalArgumentException if some aspect of this element
     *            prevents it from being added to this Collection.
     */
    public void add( Object o ) {
      ((ListIterator<cfData>)iter).add( tagUtils.convertToCfData( o ) );
    }
  }


}
