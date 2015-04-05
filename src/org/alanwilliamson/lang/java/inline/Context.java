package org.alanwilliamson.lang.java.inline;

import java.util.Date;
import java.util.Iterator;

public interface Context {

	/**
	 * Executes a CFML function returning back the Java object
	 * 
	 * @param function
	 * @param objects
	 * @return
	 * @throws Exception
	 */
	public Object call( String function, Object... objects ) throws Exception ;
	
	/**
	 * Returns the CFML variable as a Java String object
	 * 
	 * @param var - CFML path eg "form.param1"
	 * @return
	 */
	public String getString( String var );

	/**
	 * Returns the CFML variable as a Java integer
	 * 
	 * @param var - CFML path eg "form.param1"
	 * @return
	 */
	public int getInt( String var );

	/**
	 * Returns the CFML variable as a Java long
	 * 
	 * @param var - CFML path eg "form.param1"
	 * @return
	 */
	public long getLong( String var );

	/**
	 * Returns the CFML variable as a Java boolean
	 * 
	 * @param var - CFML path eg "form.param1"
	 * @return
	 */
	public boolean getBoolean( String var );

	/**
	 * Returns the CFML variable as a Java Date
	 * 
	 * @param var - CFML path eg "form.param1"
	 * @return
	 */
	public Date getDate( String var );

	/**
	 * Returns the CFML variable as a Java Object
	 * 
	 * @param var - CFML path eg "form.param1"
	 * @return
	 */
	public Object get( String var );

	/**
	 * Returns the CFML array as a Java cfArray
	 * 
	 * @param var - CFML path eg "form.param1"
	 * @return - null if not found or not an array
	 */
	public cfArray getArray( String var );

	/**
	 * Returns the CFML array as a Java cfQuery
	 * 
	 * @param var - CFML path eg "form.param1"
	 * @return - null if not found or not an query
	 */
	public cfQuery getQuery( String var );

	/**
	 * Returns the CFML array as a Java cfStruct
	 * 
	 * @param var - CFML path eg "form.param1"
	 * @return - null if not found or not an struct
	 */
	public cfStruct getStruct( String var );
	
	/**
	 * Sets the given Java object to the CFML path.
	 * It will automatically convert to Array, Struct accordingly
	 * 
	 * @param var - CFML path eg "form.param1"
	 * @param data
	 */
	public void set( String var, Object data );	
	
	/**
	 * Prints the given parameter to the request output
	 * @param s
	 */
	public void print(String s);
	public void print(int s);
	public void print(StringBuilder s);
	public void print(long s);
	public void print(double s);
	public void print(boolean s);
	public void print(byte s);
	public void print(Object i);

	/**
	 * Attributes for storing Java objects between calls
	 */
	public void setAttribute(String name, Object o);
	public Object getAttribute(String name);
	public void removeAttribute(String name);
	public Iterator<String> getAttributeNames();
}
