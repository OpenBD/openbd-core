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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.allaire.cfx.cfmlPage;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.naryx.tagfusion.cfx.cfmlPageContextImpl;
import com.naryx.tagfusion.expression.function.xml.xmlFormat;
import com.naryx.tagfusion.util.InstanceTrackingPrintWriter;

/**
 * This class acts as a wrapper to a Java class accessed via CFObject.
 * The instance of the Java class isn't actually created until a method or
 * field is accessed thru' getData().
 *
 * Note that when a field is requested, what is returned is a cfJavaObjectFieldData
 * instance which is basically an indirect reference to the object field allowing
 * for the getting and setting of the value.
 *
 * There is no requirement for the setData methods to be implemented.
 *
 * The internal data objects have been marked tranisent for the purposes of caching
 * queries to disk.
 */


public class cfJavaObjectData extends cfData implements java.io.Serializable {

	static final long serialVersionUID = 1;

	private static final javolution.util.FastSet<Class<?>> numericClassSet = new javolution.util.FastSet<Class<?>>( 11 );

	static {
		numericClassSet.add( Integer.class );
		numericClassSet.add( int.class );
		numericClassSet.add( Double.class );
		numericClassSet.add( double.class );
		numericClassSet.add( Float.class );
		numericClassSet.add( float.class );
		numericClassSet.add( Short.class );
		numericClassSet.add( short.class );
		numericClassSet.add( Long.class );
		numericClassSet.add( long.class );
		numericClassSet.add( Number.class );
	}

	transient protected Object instance;

	/**
	 * For performance reasons we're not going to look up the Class until we
	 * actually need it. Therefore, never read the instanceClass attribute
	 * directly, but always indirectly via the getInstanceClass() method.
	 */
	transient private Class<?> instanceClass;
	transient private cfSession parentSession;		//--[ This is for the cfmlPageContext
	transient private cfmlPageContextImpl thisPageContext;		//--[ This is for the cfmlPageContext

	public cfJavaObjectData(Object _obj){
		instance = _obj;
	}// cfJavaObjectData()

	public cfJavaObjectData( cfSession _session, Class<?> _class ){
		instanceClass = _class;
		parentSession = _session;
	}// cfJavaObjectData()

	// subclasses using this constructor MUST also invoke setInstance()
	protected cfJavaObjectData(){
		super();
	}


	public String getString() throws dataNotSupportedException {
		if ( getDataType() == cfData.CFJAVAOBJECTDATA ){
			try{
				return getInstance().toString();
			}catch( cfmRunTimeException e ){
				throw new dataNotSupportedException( "Failed to instantiate object: " + e.getMessage() );
			}
		}
		return super.getString();
	}

	public byte getDataType(){ return cfData.CFJAVAOBJECTDATA; }
	public String getDataTypeName() { return "java object"; }

	public Object getInstance() throws cfmRunTimeException {
		if ( instance == null ) {
			createInstance();
		}
		return instance;
	}// getInstance()

	public Object getUnderlyingInstance()
	{
		// Yes, even if it's null
		return instance;
	}

	protected void setInstance(Object _inst){
		instance = _inst;
		instanceClass = null;
	}// setInstance()

	/**
	 * For performance reasons we're not going to look up the Class until we
	 * actually need it. Therefore, never read the instanceClass attribute
	 * directly, but always indirectly via the getInstanceClass() method.
	 */
	public Class<?> getInstanceClass()
	{
		if ((instanceClass == null) && (instance != null))
		{
			instanceClass = instance.getClass();
		}
		return instanceClass;
	}

	public String getInstanceClassName()
	{
		Class<?> cls = getInstanceClass();
		if (cls != null)
			return cls.getName();
		else if (instance != null)
			return instance.getClass().getName();
		else
			return null;
	}

	public cfData getJavaData( cfData _field ) throws cfmRunTimeException{
		if ( ( _field != null ) && ( _field.getDataType() == cfData.CFSTRINGDATA ) ) {
			return getJavaData( ( (cfStringData) _field ).getString() );
		}else{
			throw new cfmRunTimeException( catchDataFactory.generalException( cfCatchData.TYPE_OBJECT,
																																					"errorCode.runtimeError",
																																				 	"cfdata.javaInvalidClass",
																																				 	null ));
		}

	}// getData()


	/**
	 * Returns the field named '_field' performing a case-insensitive match
	 */
	private Field getField(Class<?> cls, String _field ) {
		// we could attempt to do case sensitive match first with Class.getField( String )
		// but the likely overhead of a created NoSuchFieldException is not worth it
		Field [] allFields = cls.getFields();

		for ( int i = 0; i < allFields.length; i++ ){
			if ( allFields[i].getName().equalsIgnoreCase( _field ) ){
				return allFields[i];
			}
		}
		return null;
	}



	public cfData getJavaData( String _Field ) throws cfmRunTimeException{
		Class<?> cls = getInstanceClass();

		Exception exception = null;
		try {
			Field theField = getField(cls, _Field );
			if ( theField != null ){

				cfData converted = convertToCfData( theField );
				return converted;
			}
		}catch( SecurityException e3 ){
			exception = e3;
		}


		// either an exception was thrown or the field wasn't found
		cfData fieldAsMethod = getFieldMethod(cls, _Field );
		if ( fieldAsMethod != null ){
			return fieldAsMethod;
		}

		if ( exception == null ){
			exception = new NoSuchFieldException( _Field );
		}

		throw new cfmRunTimeException( catchDataFactory.javaMethodException( "errorCode.javaException",
				exception.getClass().getName(), exception.getMessage(), exception));


	}// getData()

  private cfData getFieldMethod(Class<?> cls, String _field ) {
    Method getMethod = null;
    Method setMethod = null;

    Method [] allMethods = cls.getMethods();
    String getMethName1 = "get" + _field;
    String getMethName3 = "is" + _field;
    List<Method> getMethods = new ArrayList<Method>();

    String setMethName1 = "set" + _field;
    List<Method> setMethods = new ArrayList<Method>();

    for ( int i = 0; i < allMethods.length; i++ ){
      String methName = allMethods[i].getName();
			if ( ( getMethName1.equalsIgnoreCase( methName ) || 
					getMethName3.equalsIgnoreCase( methName ) ) && 
					allMethods[i].getParameterTypes().length == 0 )
			{
				getMethods.add( 0, allMethods[i] );
			}
			else if ( setMethName1.equalsIgnoreCase( methName ) && 
					allMethods[i].getParameterTypes().length == 1 )
			{
				setMethods.add( 0, allMethods[i] );
			}
		}
		
		if ( getMethods.size() != 0 ){
			getMethod = getMethods.get(0);
		}
		
		if ( setMethods.size() != 0 ){
			setMethod = setMethods.get(0);
		}

		if ( getMethod == null && setMethod == null ){
			return null;
		}

		return new com.naryx.tagfusion.cfm.parser.cfJavaObjectFieldData( this, _field, getMethod, setMethod );
	}

	
	public cfData getJavaData( javaMethodDataInterface _method, CFContext _context ) throws cfmRunTimeException{
		String methodName = _method.getFunctionName();
		List<cfData> args = _method.getEvaluatedArguments( _context, false );

		if ( methodName.toLowerCase().equals( "init" ) ){
			// bug #2359: if the instance already exists, see if there's an "init" method
			if ( instance != null ){
				try{
					return invokeMethod( methodName, args, _context.getSession() );
				}catch ( cfmRunTimeException ignore ) {} // failed to invoke "init" method, try constructor
			}
			
			// handle init
			if ( args.size() == 0 ){
				createInstance();
			}else{
				createInstance( args, _context.getSession() );
			}
			return tagUtils.convertToCfData( instance );
		}
		return invokeMethod( methodName, args, _context.getSession() );
	}// getData()


	/**
	 * If the instance doesn't exist then creates it
	 **/
	synchronized protected void createInstance() throws cfmRunTimeException{
		// another thread may have executed this method whilst this thread waited queued on this method
		if ( instance != null ){
			return;
		}
		
		Class<?> cls = getInstanceClass();

		// create the instance if it doesn't exist
		try{
			instance = cls.newInstance();
			setCfmlPageContext( this );
		}catch( ExceptionInInitializerError iiee ){
			// if the constructor threw an exception, get the
			throw new cfmRunTimeException( catchDataFactory.extendedException( 
				cfCatchData.TYPE_OBJECT,
				"errorCode.runtimeError",
				"cfdata.javaobjectfail",
				null,
				iiee.getException().getClass().getName() + " ( Message : " + iiee.getException().getMessage() + ")",
				iiee.getException() ));
		}catch ( Throwable t ){
			// throw if cannot instantiate object due to IllegalArgumentException, InstantiationException or IllegalAccessException
			// also, a NoSuchMethodError can be thrown if a no-arguments constructor isn't defined
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_OBJECT,
				"errorCode.runtimeError",
				"cfdata.javaobjectfail",
				null,
				t.getMessage()));
		}
	}// createInstance()


	private void createInstance( List<cfData> _args, cfSession _Session ) throws cfmRunTimeException {

		Class<?>[] argTypes = new Class[ _args.size() ];
		Object[] argValues = new Object[ _args.size() ];

   	Class<?> cls = getInstanceClass();

		// get Constructor (throws runTimeException if can't find a matching one)
		Constructor<?> constructor = getConstructor(cls, _args, argTypes, argValues );

		try
		{
			instance = constructor.newInstance( argValues );
			setCfmlPageContext( this );
		}
		catch( InvocationTargetException ite )
		{
			Throwable e = ite.getTargetException();
			// if the constructor threw an exception, return the exception name and it's message
			throw new cfmRunTimeException( catchDataFactory.javaMethodException( "errorCode.javaException",
				e.getClass().getName(),
				e.getMessage(),
				e ));
		}
		catch( Exception ie )
		{
			// throw if cannot instantiate object due to IllegalArgumentException, InstantiationException or IllegalAccessException
			throw new cfmRunTimeException( catchDataFactory.extendedException( cfCatchData.TYPE_OBJECT,
				"errorCode.runtimeError",
				"cfdata.javaobjectfail",
				null,
				ie.getMessage() ));
		}
	}// createInstance()


	private Constructor<?> getConstructor(Class<?> cls, List<cfData> _args, Class<?>[] _newArgTypes,
			Object [] _newArgValues ) throws cfmRunTimeException
	{

		// get Vector of potential matches i.e. same name + same number of args
		List<Constructor<?>> possibles = getMatchingConstructors(cls, _args.size() );
		boolean ambiguous = false;

		if ( possibles.size() > 1 ){
			ambiguous = true;
			// do best fit
			possibles = bestFitConstructor( possibles, _args );
		}

		// if Vector.size() != 1 (i.e. could be 0 or more than 1)
		if ( possibles.size() != 1 ){
			if ( possibles.size() == 0 && !ambiguous )
			{
				throw new cfmRunTimeException( 
					catchDataFactory.generalException( 
						cfCatchData.TYPE_OBJECT,
						"errorCode.runtimeError",
						"cfdata.javaInvalidConstructor",
						null	
					)
				);
			}
			else
			{
				throw new cfmRunTimeException( 
					catchDataFactory.generalException( 
						cfCatchData.TYPE_OBJECT,
						"errorCode.runtimeError",
						"cfdata.javaInvalidConstructor2",
						null
					)
				);
			}
		}

		// INVARIANT - possibles.size() == 1;
		Constructor<?> theConstructor = possibles.get(0);
		Class<?>[] constParamTypes = theConstructor.getParameterTypes();
		System.arraycopy( constParamTypes, 0, _newArgTypes, 0, constParamTypes.length );

		// convert real args to _newArgs (throwing an exception if can't convert)
		convertData( _args, _newArgTypes, _newArgValues );

		return theConstructor;

	}// getConstructor()


	private List<Constructor<?>> getMatchingConstructors(Class<?> cls, int _noArgs ){
		List<Constructor<?>> matching = new ArrayList<Constructor<?>>();

		Constructor<?> [] constrs = cls.getConstructors();
		if ( constrs != null ){
			for ( int i = 0; i < constrs.length; i++ ){
				if ( constrs[i].getParameterTypes().length == _noArgs ){
					matching.add( constrs[i] );
				}
			}
		}

		return matching;

	}// getMatchingConstructors()


	/**
	 * returns all the Method's in '_possibles' that should be possible to
	 * invoke given the arguments and their types
	 */
	private static List<Constructor<?>> bestFitConstructor( List<Constructor<?>> _possibles, List<cfData> _actualArgs ){
		List<Constructor<?>> newPossibles = new ArrayList<Constructor<?>>();

		double minRank = 999;
		Constructor<?> nextConstr = null;
		Class<?>[] constrParamTypes = null;
		int noArgs = _actualArgs.size();

		// loop thru each method
		for ( int i = 0; i < _possibles.size(); i++ ){
			nextConstr = _possibles.get(i);
			constrParamTypes = nextConstr.getParameterTypes();
			
			double constrRank = 0;
			boolean fullMatch = true;
			// loop thru the arguments getting a rank for each argument to Method parameter
			// match up
			for ( int j = 0; j < noArgs; j++ ){
				cfData arg = _actualArgs.get( j );
				double argRank = getMatchRank( arg, constrParamTypes[j] );
				if ( argRank == 0 ){
					fullMatch = false;
					break;
				}else{
					constrRank += argRank;
				}
			}

			// if it's a match...
			if ( fullMatch ){
				// ... and a better match than the current best match
				if ( constrRank < minRank ){
					newPossibles.clear();
					newPossibles.add( nextConstr );
					minRank = constrRank;
					// ... or is an equally good match
			    }else if ( constrRank == minRank ){
			      newPossibles.add( nextConstr );
			    }
			}
		}

		return newPossibles;
	}


	// returns a cfJavaObjectFieldData representing this field
	private cfData convertToCfData( Field _field ){
		return new com.naryx.tagfusion.cfm.parser.cfJavaObjectFieldData( this, _field );
	}// convertToCfData()

	/***
	private Class[] getClasses( Vector objs ){
		int noArgs = objs.size();

		Class [] argClasses = null;
		if ( noArgs > 0 ){
			argClasses = new Class[ noArgs ];
			for ( int i = 0; i < noArgs; i++ ){
				argClasses[ i ] = objs.elementAt( i ).getClass();
			}
		}

		return argClasses;
	}// getClasses()
	***/


	/**
	 * invokes the given named method choosing the one
	 * that fits best out of the possible. An instance of the class
	 * will be created if one doesn't already exist AND the method
	 * being invoked is non-static.
	 * throws an exception if no matching method found
	 */

	public cfData invokeMethod( String _name, List<cfData> _args, cfSession _Session ) throws cfmRunTimeException {
		Class<?> [] argTypes;
		Object [] argValues;
		
		if ( _name.equals("each") ){ 
			if ( instance instanceof cfArrayData 
					|| instance instanceof cfStructData
					|| instance instanceof cfQueryResultData )
				_args.add(0, new cfDataSession(_Session) );
		}

		
		argTypes 	= new Class[ _args.size() ];
		argValues = new Object[ _args.size() ];
		

		Object returnValue = null;
    Class<?> cls = getInstanceClass();

		//get the Method trying a case-sensitive search first
		Method method = getMethod(cls, _name, _args, argTypes, argValues, true, false );
		if ( method == null ){
			method = getMethod(cls, _name, _args, argTypes, argValues, false, true );
		}

		if ( method == null )	{			
			throw new cfmRunTimeException( catchDataFactory.generalException( cfCatchData.TYPE_OBJECT,
				"errorCode.runtimeError",
				"cfdata.javaInvalidMethod",
				new String[]{_name}	));
		}

		// if instance hasn't been created and method is non-static, create the instance
		if ( instance == null ){
			if ( !Modifier.isStatic( method.getModifiers() ) ){
				createInstance();
			}
		}

		try	{
			returnValue = method.invoke( instance, argValues );
		}	catch( IllegalArgumentException e )	{ // report that a matching method could not be found
			// note that this is unlikely but there are situations where this occurs
			throw new cfmRunTimeException( catchDataFactory.generalException( cfCatchData.TYPE_OBJECT,
				"errorCode.runtimeError",
				"cfdata.javaInvalidMethod",
				new String[]{_name}	));

		}catch ( IllegalAccessException iae1 ){
			// it may be that the method is a public method from a private implementation of a
			// public interface...
			boolean throwError = false;
			Class currCls = cls;
			while(currCls != null){
				try
				{
					Method innerMeth = getInnerClassMethod(currCls, method.getName(), argTypes );
					if ( innerMeth != null )
					{
						returnValue = innerMeth.invoke( instance, argValues );
						throwError = false;
						break;
					}
				}catch( IllegalAccessException e ){
					throwError = true;

				}catch (InvocationTargetException e2 ){
					throwError = true;
				}
				
				//part of the fix for OpenBD#180
				Class<?> superClass = cls.getSuperclass();
				if(currCls == superClass) //then we can't go any higher up (we're already at java.lang.Object)
				{
					throwError = true;
					break; //to avoid an infinite loop (seen on OpenBD-GAE)
				}
				else
					currCls = superClass;
			}

			if ( throwError ){ // handle original error
				throw new cfmRunTimeException( catchDataFactory.generalException( cfCatchData.TYPE_OBJECT,
					"errorCode.runtimeError",
					"cfdata.javaIllegalMethod",
					new String[]{_name}	));
			}
			
		}catch ( InvocationTargetException ite ){
			Throwable targetExc = ite.getTargetException();
			if ( targetExc instanceof cfmAbortException )
				throw (cfmAbortException)targetExc;

			if ( targetExc != null ){
				throw new cfmRunTimeException( catchDataFactory.javaMethodException( "errorCode.javaException",
					targetExc.getClass().getName(),
					targetExc.getMessage(),
					targetExc ));
			}	else{
				String msg = "The method \"" + _name + "\" threw an exception and BlueDragon was unable to retrieve the exception information.";
				if ( ite.getMessage() != null )
					msg += " (InvocationTargetException message = " + ite.getMessage() + ")";
				throw new cfmRunTimeException( catchDataFactory.javaMethodException( "errorCode.javaException",
					ite.getClass().getName(),
					msg,
					ite ));
			}
		}

		return tagUtils.convertToCfData( returnValue );

	}// invokeMethod


	private Method getInnerClassMethod(Class<?> cls, String _method, Class<?>[] _paramTypes ) {
		// We're looking for the instance of Method that has the same attributes as the one we first
		// found but now we want the version that belongs to the super class or implemented interface
		Method parentMethod = null;

		// bug #2360: first see if the method is from an implemented interface
		Class<?>[] implemented = cls.getInterfaces();
		for ( int i = 0; i < implemented.length; i++ ) {
			try {
				parentMethod = implemented[i].getMethod( _method, _paramTypes );
				break;
			} catch ( NoSuchMethodException e ) {
			} // ignore and try next
		}

		// now look for a superclass method
		if ( parentMethod == null ) {
			try {
				Class<?> superclass = cls.getSuperclass();
				if(superclass != null) //part of the fix for OpenBD#180
					parentMethod = superclass.getMethod( _method, _paramTypes );
			} catch ( NoSuchMethodException e ) {
			}
		}

		return parentMethod;
	}

	/**
	 * returns a Method that has the name '_name' and best matches the Vector of cfData arguments.
	 * The 2 Class[]'s will reflect the chosen method. '_newArgTypes' will contain the Classes of
	 * the chosen method argmuents, and '_newArgValues' will contain the argument to pass to the
	 * Method (converted from the cfData's in '_actualArgs' to Java Objects).
	 */

	private static Method getMethod( Class<?> _class, String _name, List<cfData> _actualArgs,
														Class<?>[] _newArgTypes, Object[] _newArgValues,
														boolean _caseSensitive, boolean _throwOnError ) throws cfmRunTimeException{

		// get Vector of potential matches i.e. same name + same number of args [non-case-sensitive match]
		List<Method> possibles = getMatchingMethods( _class, _name, _actualArgs.size(), _caseSensitive, true  );
		boolean ambiguous = false;

		// if Vector.size() > 1
		if ( possibles.size() > 1 ){
			//    do best fit
		    ambiguous = true;
			possibles = bestFitMethod( possibles, _actualArgs );
		}

		// if Vector.size() != 1 (i.e. could be 0 or more than 1)
		if ( possibles.size() != 1 ){
			if ( _throwOnError ){
            // if there's no matching methods and it wasn't best matched
				if ( possibles.size() == 0 && !ambiguous ){
					throw new cfmRunTimeException( catchDataFactory.generalException( cfCatchData.TYPE_OBJECT,
																																						"errorCode.runtimeError",
																																					 	 "cfdata.javaGetMethod",
																																					 	 new String[]{_name}	));
				}else{
					throw new cfmRunTimeException( catchDataFactory.generalException( cfCatchData.TYPE_OBJECT,
																																						"errorCode.runtimeError",
																																					 	 "cfdata.javaGetMethod2",
																																					 	 new String[]{_name}	));
				}
			}else{
				return null;
			}
		}

		// INVARIANT - possibles.size() == 1;
		Method theMethod = possibles.get(0);
		Class<?>[] methParamTypes = theMethod.getParameterTypes();
		System.arraycopy( methParamTypes, 0, _newArgTypes, 0, methParamTypes.length );

		// convert real args to _newArgs (throwing an exception if can't convert)
		try{
			convertData( _actualArgs, _newArgTypes, _newArgValues );
		}catch( cfmRunTimeException e ){
			if ( _throwOnError )
				throw e;
			else
				return null;
		}
		return theMethod;

	}// getMethod()


	/**
	 * returns all the Method's for this Java class that have the name '_methodName'
	 * with '_noArgs' number of parameters
	 */
  private static List<Method> getMatchingMethods( Class<?> _class, String _methodName, int _noArgs, boolean _case, boolean _includeStatic ){
		List<Method> matching = new ArrayList<Method>();
		Method nextMethod;
		String nextMethodName;
		if ( _class == null )
      return matching;

		Method [] allMethods = _class.getMethods();

		for ( int i = 0; i < allMethods.length; i++ ){
			nextMethod = allMethods[i];
			nextMethodName = nextMethod.getName();

      if ( ( _includeStatic || (!_includeStatic && !Modifier.isStatic( nextMethod.getModifiers() ) ) )
          && ( ( _case && nextMethodName.equals(_methodName ) )
          || ( !_case && nextMethodName.equalsIgnoreCase(_methodName ) ) )
          && nextMethod.getParameterTypes().length == _noArgs ){
      	addMatchingMethod( matching, nextMethod );
			}
		}

		return matching;

	}// getMatchingMethods()

	private static void addMatchingMethod( List<Method> matching, Method m )
	{
		// With JDK 1.5, it's possible to have two methods that are exactly the same except for
		// the return type.  In this case we'll ignore the one that returns a java.lang.Object
		// and only use the one that returns a specific class.  If it's possible to have more than
		// two methods that are exactly the same except for the return type then we'll need to
		// modify this logic to ignore the ones that return specific classes and only use the one
		// that returns a java.lang.Object.  This problem was seen with the regression test for
		// bug #978.
		Class<?> returnType = m.getReturnType();
		if ( returnType != null )
		{
			for ( int i = 0; i < matching.size(); i++ )
			{
				Method nextMethod = matching.get( i );
				if ( methodArgsMatch( nextMethod, m ) )
				{
					Class<?> nextMethodReturnType = nextMethod.getReturnType();
					if ( returnType.getName().equals( "java.lang.Object" ) )
					{
						// The methods match and this one returns java.lang.Object so just
						// return without adding it to the matching vector.
						return;
					}
					else if ( nextMethodReturnType.getName().equals( "java.lang.Object" ) )
					{
						// The methods match and the one in the matching vector returns java.lang.Object so
						// replace it with the passed in method.
						matching.set( i, m );
						return;
					}
					else if ( m.getDeclaringClass().getName().equals( "java.lang.Object" ) )
					{
						// The methods match except the one passed in is declared by java.lang.Object
						// so let the non-Object version take precedence
						return;

					}
					else if ( nextMethod.getDeclaringClass().getName().equals( "java.lang.Object" ) )
					{
						// The methods match except the one passed we have already is declared by
						// java.lang.Object so replace it with the non-Object version
						matching.set(i, m);
						return;
					}
					else if (nextMethod.isBridge() && !m.isBridge())
					{
						// the methods match but the non-bridge method takes precedence
						matching.set( i, m );
						return;
					}
					else if (m.isBridge())
					{
						// ignore bridge method
						return;
					}else
					{
						// This should never happen.  If it does then we'll need to change our logic as
						// described in the comment at the top of this method.
						throw new IllegalStateException( "Found 2 methods that are exactly the same except for the return types and neither return java.lang.Object.  Method name = " + m.getName() );
					}

				}
			}
		}

		matching.add( m );
	}

	private static boolean methodArgsMatch( Method m1, Method m2 )
	{
		Class<?>[] paramTypes1 = m1.getParameterTypes();
		Class<?>[] paramTypes2 = m2.getParameterTypes();

		// If they take a different number of arguments then return false
		if ( paramTypes1.length != paramTypes2.length )
			return false;

		for ( int i = 0; i < paramTypes1.length; i++ )
		{
			// If one of the parameters doesn't match then return false
			if ( !paramTypes1[ i ].equals( paramTypes2[ i ] ) )
				return false;
		}

		return true;
	}

	/**
   * returns all the Method's in '_possibles' that should be possible to
	 * invoke given the arguments and their types
   */
  private static List<Method> bestFitMethod( List<Method> _possibles, List<cfData> _actualArgs ){
    List<Method> newPossibles = new ArrayList<Method>();

    double minRank = 999;
    Method nextMethod = null;
    Class<?>[] methodParamTypes = null;
    int noArgs = _actualArgs.size();

    // loop thru each method
    for ( int i = 0; i < _possibles.size(); i++ ){
      nextMethod = _possibles.get(i);
      methodParamTypes = nextMethod.getParameterTypes();

      double methRank = 0;
      boolean fullMatch = true;
      // loop thru the arguments getting a rank for each argument to Method parameter
      // match up
      for ( int j = 0; j < noArgs; j++ ){
        cfData arg = _actualArgs.get( j );
        double argRank = getMatchRank( arg, methodParamTypes[j] );
        if ( argRank == 0 ){
          fullMatch = false;
          break;
        }else{
          methRank += argRank;
        }
      }

      // if it's a match...
      if ( fullMatch ){
        // ... and a better match than the current best match
        if ( methRank < minRank ){
          newPossibles.clear();
          newPossibles.add( nextMethod );
          minRank = methRank;
        // ... or is an equally good match
        }else if ( methRank == minRank ){
          newPossibles.add( nextMethod );
        }
      }
    }

    return newPossibles;
  }


  private static double getMatchRank( cfData _cfdata, Class<?> _class ){
		// javacast takes priority so if it's set use that to get the rank
		Javacast javaCast = _cfdata.getJavaCast();
  	//int javaCast = _cfdata.getJavaCast();
		boolean isArrayCast = javaCast != null ? javaCast.isArray() : false;
	
		if ( javaCast != null ){
			
			if ( isArrayCast ) { 
				if ( ( javaCast == Javacast.INT_ARRAY && _class.equals( int[].class ) )
					|| ( javaCast == Javacast.LONG_ARRAY && _class.equals( long[].class ) )
					|| ( javaCast == Javacast.DOUBLE_ARRAY && _class.equals( double[].class ) )
					|| ( javaCast == Javacast.FLOAT_ARRAY && _class.equals( float[].class ) )
					|| ( javaCast == Javacast.BOOLEAN_ARRAY && _class.equals( boolean[].class ) )
					|| ( javaCast == Javacast.STRING_ARRAY && _class.equals( String[].class ) )
					|| ( javaCast == Javacast.BYTE_ARRAY && _class.equals( byte[].class ) )
					|| ( javaCast == Javacast.CHAR_ARRAY && _class.equals( char[].class ) )
					|| ( javaCast == Javacast.SHORT_ARRAY && _class.equals( short[].class ) )
					){ 
						return 0.75;
				}else if ( ( javaCast == Javacast.INT_ARRAY && _class.equals( Integer[].class ) )
					|| ( javaCast == Javacast.LONG_ARRAY && _class.equals( Long[].class ) )
					|| ( javaCast == Javacast.DOUBLE_ARRAY && _class.equals( Double[].class ) )
					|| ( javaCast == Javacast.FLOAT_ARRAY && _class.equals( Float[].class ) ) 
					|| ( javaCast == Javacast.BOOLEAN_ARRAY && _class.equals( Boolean[].class ) )
					|| ( javaCast == Javacast.BYTE && _class.equals( Byte[].class ) )
					|| ( javaCast == Javacast.CHAR_ARRAY && _class.equals( Character[].class ) )
					|| ( javaCast == Javacast.SHORT_ARRAY && _class.equals( Short[].class ) )
					|| ( javaCast == Javacast.BIGDECIMAL_ARRAY && _class.equals( BigDecimal[].class ) )
					|| ( javaCast.getDatatype() == Javacast.Datatype.CUSTOM && javaCast.isArray() && _class.equals( javaCast.getCustomClass() ) ) ){
				return 0.50;
			}
				
		}else if ( javaCast.getDatatype() == Javacast.Datatype.CUSTOM && javaCast.getCustomClass().equals( _class ) ){
			return 0.5;

		}else{ 
			// non-primitive type is preferred over it's primitive equivalent
			if ( ( javaCast == Javacast.INT && _class.equals( int.class ) ) 
				|| ( javaCast == Javacast.LONG && _class.equals( long.class ) )
				|| ( javaCast == Javacast.DOUBLE &&  _class.equals( double.class ) )
				|| ( javaCast == Javacast.FLOAT && _class.equals( float.class ) )
				|| ( javaCast == Javacast.BOOLEAN && _class.equals( boolean.class ) )
				|| ( javaCast == Javacast.STRING && _class.equals( String.class ) )
				|| ( javaCast == Javacast.BYTE && _class.equals( byte.class ) )
				|| ( javaCast == Javacast.CHAR && _class.equals( char.class ) )
				|| ( javaCast == Javacast.SHORT && _class.equals( short.class ) )
				|| ( javaCast.getDatatype() == Javacast.Datatype.CUSTOM && _class.equals( javaCast.getClass() ) ) ){
				
				return 0.75;
			
			}else if ( ( javaCast == Javacast.INT && _class.equals( Integer.class ) )
				|| ( javaCast == Javacast.LONG && _class.equals( Long.class ) )
				|| ( javaCast == Javacast.BOOLEAN && _class.equals( Boolean.class ) )
				|| ( javaCast == Javacast.DOUBLE && _class.equals( Double.class ) )
				|| ( javaCast == Javacast.FLOAT && _class.equals( Float.class ) ) 
				|| ( javaCast == Javacast.BOOLEAN && _class.equals( Boolean.class ) )
				|| ( javaCast == Javacast.BYTE && _class.equals( Byte.class ) )
				|| ( javaCast == Javacast.CHAR && _class.equals( Character.class ) )
				|| ( javaCast == Javacast.SHORT && _class.equals( Short.class ) ) 
				|| ( javaCast == Javacast.BIGDECIMAL && _class.equals( BigDecimal.class ) ) ){
				
				return 0.50;
			}else if ( javaCast == Javacast.STRING && ( _class.equals( Object.class ) || 
				  _class.equals( Serializable.class ) || _class.equals( Comparable.class ) ) ){
				
				return 0.85;
			}
		}
	}

    int dType = _cfdata.getDataType();
    boolean isStringCompatible = true;
    
    switch ( dType ){
    	case cfData.CFSTRINGDATA:
    	case cfData.CFNUMBERDATA:
    	case cfData.CFBOOLEANDATA:

	      	if ( dType == cfData.CFNUMBERDATA ){ // skip this if it's a string
	      		// indicates whether the cfNumberData has been created from a returned java method value
	      		isStringCompatible = !( (cfNumberData) _cfdata ).isJavaNumeric();
	      	}else if ( dType == cfData.CFBOOLEANDATA ){
	      		isStringCompatible = !( (cfBooleanData) _cfdata ).isJavaBoolean();
	      	}
	
	      	if ( isStringCompatible && _class == String.class ){
	      		return 1;
	      	}else if ( _class.equals( char.class ) ){
	      		try {
	      			// a string with a single character matches "char"
	      			if ( _cfdata.getString().length() == 1 ){
	      				return 1.1;
	      			}
	      		}catch ( dataNotSupportedException ignore ) {} // should never happen
	      	}

    	case cfData.CFDATEDATA:

    		if ( ( _class == String.class ) && isStringCompatible ) { // all these cfdata types can be converted to a string
        		return 1;
        }else if ( _class == Serializable.class ){
    			return 2;
    		}else if ( _class == Comparable.class ){
    			return 2;
    		}else if ( _class== Object.class ){
    			return 3;
    		}else if ( ( _class == boolean.class ) || ( _class == Boolean.class ) ){
    			return ( _cfdata.isBooleanConvertible() ? 1.1 : 0 );
    		}else if ( numericClassSet.contains( _class ) ) {
    			return ( _cfdata.isNumberConvertible() ? 1.1 : 0 );
    		}else if ( _class == java.util.Date.class ) {
    			return ( _cfdata.isDateConvertible() ? 1.1 : 0 );
    		}

    		return 0;

      case cfData.CFARRAYDATA:
    	  
    	  if ( _class.isArray() ){
    		  int dim = 1;
    		  Class<?> componentType = _class.getComponentType();
    		  
    		  if ( _cfdata instanceof cfFixedArrayData ){
    			  if ( ( (cfFixedArrayData) _cfdata).getArray().getClass().getComponentType() == componentType ){
    				  return 1;
    			  }
    		  }

	          while ( componentType.isArray() ){
	        	  dim++;
	        	  componentType = componentType.getComponentType();
	          }

          if ( dim == ( (cfArrayData)_cfdata ).getDimension() ){
            return 1.5;
          }else{
            return 2;
          }
        }else{
          return matchClass( ( (cfJavaObjectData) _cfdata ).getInstanceClass(), _class );
        }

      case cfData.CFSTRUCTDATA:
      case cfData.CFJAVAOBJECTDATA:
        return matchClass( ( (cfJavaObjectData) _cfdata ).getInstanceClass(), _class );

      case cfData.CFQUERYRESULTDATA:
        return matchClass(  _cfdata.getClass(), _class );

      case cfData.CFBINARYDATA:
        if ( _class.isArray() && (_class.getComponentType().equals( Byte.class ) ||  _class.getComponentType().equals( byte.class ) ) ){
          return 1;
        }else if ( _class.equals( Object.class ) ){
          return 3;
        }
        return 0;
      case cfData.CFNULLDATA:

        if ( ((cfNullData)_cfdata).isDBNull() ){
          if ( _class.equals( String.class ) )
            return 1;

        if ( _class.equals( Serializable.class ) )
          return 1.5;

        if ( _class.equals( Comparable.class ) )
          return 1.5;

        if ( _class.equals( Object.class ) )
            return 2;

          if ( _class.equals( Boolean.class ) || _class.equals( Number.class ) ||
              _class.equals( Integer.class ) || _class.equals( Long.class )    ||
              _class.equals( Double.class )  || _class.equals( Short.class )   ||
              _class.equals( Float.class )   || (_class.equals( float.class )  ||
              _class.equals( double.class )  || _class.equals( short.class )   ||
              _class.equals( int.class )     || _class.equals( long.class )    ||
              _class.equals( boolean.class ) ) )

            return 2.5;

          return 3;

        }else{

          if ( _class.equals( Object.class ) )
            return 2;

          if ( !_class.isPrimitive() )
            return 1;

        }
      case cfData.CFCOMPONENTOBJECTDATA:
      	if ( _class == cfComponentData.class ){
      		return 1;
      	}else if ( _class == Serializable.class ) {
      		return 2;
      	}else if ( _class.equals( Object.class ) )
          return 3;

	  default:
		break;
    }

    return 0;
  }



	/**
	 * converts all the cfData's in _actualArgs to the corresponding Class types specified
	 * in _newArgTypes leaving the resultant conversion in _newArgValues.
	 * throw an exception if can't convert? or can we definitely expect it?
	 */
	private static void convertData( List<cfData> _actualArgs, Class<?>[] _newArgTypes, Object[] _newArgValues ) throws cfmRunTimeException{
		for ( int i = 0; i < _newArgTypes.length; i++ ){
			
			cfData nextData = _actualArgs.get(i);
      // fix for bug 1691 allowing null to be passed to java methods
      // don't want this to work for primitive type args, otherwise a NullPointerException will be thrown on invoke()
      // if the null value came from a database field then let it go to the else clause so it will be treated as an
      // empty string instead of null in order to stay compatible with CF6.1 and CF7.  Refer to bugs 1745 and 1691.
      if ( nextData.getDataType() == cfData.CFNULLDATA &&
		  !((cfNullData)nextData).isDBNull() &&
		  Object.class.isAssignableFrom( _newArgTypes[i] ) ){
        _newArgValues[i] = null;
      }else{
        _newArgValues[i] = tagUtils.convertCFtoJava( nextData, _newArgTypes[i] );
			  if ( _newArgValues[i] == null ){
				  throw new cfmRunTimeException( catchDataFactory.generalException( cfCatchData.TYPE_OBJECT,
																																					"errorCode.runtimeError",
																																				 	 "cfdata.javaBadMethod",
																																				 	 null	));
        }
			}

		}

	}//convertData()



	/**
	 * returns whether or not a cfData can be strictly converted to the given Class type
	 */
	/***
	private static boolean typeMatch( int _jCastType, Class _class ){
		String className = _class.getName();

		if ( _jCastType == cfData.JAVA_INT ){
			return className.equals( "int" ) || className.equals( "java.lang.Integer" );
		}else if ( _jCastType == cfData.JAVA_LONG ){
			return className.equals( "long" ) || className.equals( "java.lang.Long" );
		}else if ( _jCastType == cfData.JAVA_BOOLEAN ){
			return className.equals( "boolean" ) || className.equals( "java.lang.Boolean" );
		}else if ( _jCastType == cfData.JAVA_DOUBLE ){
			return className.equals( "double" ) || className.equals( "java.lang.Double" );
        }else if ( _jCastType == cfData.JAVA_FLOAT ){
            return className.equals( "float" ) || className.equals( "java.lang.Float" );
		}else if ( _jCastType == cfData.JAVA_STRING ){
			return className.equals( "java.lang.String" );
		}else{
			return false;
		}

	}// typeMatch()
	***/

	/***
	private static boolean exactMatch( cfData _cfdata, Class _class ){
		String className = _class.getName();

		if ( className.equals( "java.lang.Object" ) ){
			return true;
		}

		int dType = _cfdata.getDataType();
		switch ( dType ){
			/** KEEP NUMBER & BOOLEAN OUT OF IT FOR NOW
				set( int ); vs set( long )
				and
				set( boolean ); vs set( Boolean )
			** /
			case cfData.CFNUMBERDATA:
				return className.equals( "java.lang.Integer" ) || className.equals( "int" )
					|| className.equals( "java.lang.Double" ) || className.equals( "double" )
					|| className.equals( "java.lang.Float" ) || className.equals( "float" )
					|| className.equals( "java.lang.Short" ) || className.equals( "short" )
					|| className.equals( "java.lang.Long" ) || className.equals( "long" )
					|| className.equals( "java.lang.Number" ) || className.equals( "java.lang.Comparable" )
					|| className.equals( "java.lang.Serializable" );
			case cfData.CFBOOLEANDATA:
				return className.equals( "java.lang.Boolean" ) || className.equals( "boolean" ) || className.equals( "java.lang.Serializable" );
			case cfData.CFSTRINGDATA:
				return className.equals( "java.lang.String" ) || className.equals( "java.lang.Comparable" ) || className.equals( "java.lang.Serializable" );
			case cfData.CFDATEDATA:
				try{
					return Class.forName( "java.util.Date" ).isAssignableFrom( _class ); //className.equals( "java.util.Date" ) ||
				}catch( ClassNotFoundException unlikely ){ return false; }
			case cfData.CFSTRUCTDATA:
				try{
					return Class.forName( "java.util.Hashtable" ).isAssignableFrom( _class );//Class.forName( "java.util.Map" ).isAssignableFrom( _class );
				}catch( ClassNotFoundException e ){	return false;	}
			case cfData.CFARRAYDATA:
				return  className.equals( "java.util.Vector" ) || _class.isArray() && ( ( (cfArrayData) _cfdata ).getDimension() == 1 );
			case cfData.CFJAVAOBJECTDATA:
				try{
					Object theInst = ( (cfJavaObjectData) _cfdata ).getInstance();
					return theInst.getClass().isAssignableFrom( _class );
				}catch( Exception e ){
					return false;
				}
			default:
				return false;
		}

	}// exactMatch()
	***/


  /**
   * returns a rank indicating how well matched a Class _c1 is to Class _c2.
   * returns 0 if _c1 is not within the class hierarchy of _c2
   * returns 1 if _c1 matches _c2 exactly (they are the same class)
   * returns 2 if _c2 is an interface that _c1 implements
   * returns 3+ if _c2 is a superclass of _c1
   */

  private static int matchClass(Class<?> _c1, Class<?> _c2)
	{
		if (_c1 != null && _c2 != null && _c1.getName().equals(_c2.getName()))
		{
			return 1;
		}
		else if (_c1 != null && _c2 != null && _c2.isAssignableFrom(_c1))
		{

			int superclassIndex = 0;
			Class<?> nextClass = _c1;
			// loop thru the hierarchy of superclasses
			while (nextClass != null)
			{

				// try interfaces first
				if (isInterface(nextClass, _c2))
				{
					return 2 + superclassIndex;
				}

				superclassIndex += 3;
				// no match so far, walk our way up the Object hierarchy
				Class<?> superclass = nextClass.getSuperclass();
				if (superclass.getName().equals(_c2.getName()))
				{
					return superclassIndex;
				}
				nextClass = superclass;
			}
			return 0; // shouldn't happen since we know _c2.isAssignableFrom( _c1 )

		}
		else
		{ // classes are related
			return 0;
		}

	}


  private static boolean isInterface( Class<?> _c1, Class<?> _c2 ){

    Class<?>[] interfaces = _c1.getInterfaces();
    for ( int j = 0; j < interfaces.length; j++ ){
      if ( _c2.getName().equals( interfaces[j].getName() ) ){
        return true;
      }

      if ( isInterface( interfaces[j], _c2 ) ){
        return true;
      }

    }
    return false;
  }

	//-----------------------------------------------
	//--[ Methods for handling if the underlying class is a implementation of com.allaire.cfx.cfmlPage
	//-----------------------------------------------

	public static void setCfmlPageContext( cfJavaObjectData obj ){
		if ( obj.instance instanceof com.allaire.cfx.cfmlPage ){
			cfmlPage thisPageObject	= (cfmlPage)obj.instance;

			//--[ Create the page context
			obj.thisPageContext = new cfmlPageContextImpl( obj.parentSession );

			try{
				//--[ Call the trigger method to say its alive
				thisPageObject.cfmlPageStarted( obj.thisPageContext );

				//--[ Take a note of this object for this session
				//--[ We are using the hashcode of this object since we need a unique reference for
				//--[ the data bin to accept it.
				obj.parentSession.setDataBin( obj.hashCode() + "", obj );

			}catch(Throwable seriousError){
				obj.thisPageContext = null;
			}
		}

		//--[ Session no longer required, make it null so no hanging references
		obj.parentSession = null;
	}


	public void removeCfmlPageContext(){
		//--[ This is called when the page has finished processing.
		if ( thisPageContext != null ){
			cfmlPage thisPageObject	= (cfmlPage)instance;

			try{
				thisPageObject.cfmlPageFinished( thisPageContext );		//--[ Call the trigger method
			}catch(Throwable seriousError){}

			thisPageContext.closeSession();												//--[ invalidate the session
			thisPageContext = null;
		}
	}

	public void dumpWDDX(int version, java.io.PrintWriter out)
	{
		if (!(out instanceof InstanceTrackingPrintWriter))
			out = new InstanceTrackingPrintWriter(out);

		// open object's structure
		if (version > 10)
			out.write("<s>");
		else
			out.write("<struct>");

		// set object name
		if (version > 10)
		{
			out.write("<v n='object'>");
			out.write("<s>");
			if (getInstanceClassName() != null)
				out.write(getInstanceClassName());
			else
				out.write("[undefined class]");
			out.write("</s>");
			out.write("</v>");
		}
		else
		{
			out.write("<var name='object'>");
			out.write("<string>");
			if (getInstanceClassName() != null)
				out.write(getInstanceClassName());
			else
				out.write("[undefined class]");
			out.write("</string>");
			out.write("</var>");
		}

		Class<?> cls = getInstanceClass();
		if (cls != null)
		{
			// Methods
			Method[] allMethods = cls.getMethods();
			cfArrayData arr = cfArrayData.createArray(1);
			for (int i = 0; i < allMethods.length; i++)
			{
				try
				{
					arr.addElement(new cfStringData(allMethods[i].getName() +
						" (returns " + allMethods[i].getReturnType().getName() + ")"));
				}
				catch (cfmRunTimeException ex)
				{
					com.nary.Debug.printStackTrace(ex);
				}
			}

			if (version > 10)
			{
				out.write("<v n='Methods'>");
				arr.dumpWDDX(version, out);
				out.write("</v>");
			}
			else
			{
				out.write("<var name='Methods'>");
				arr.dumpWDDX(version, out);
				out.write("</var>");
			}

			// Fields
			Field[] allFields = cls.getFields();
			for (int j = 0; j < allFields.length; j++)
			{
				if (version > 10)
				{
					out.write("<v name='");
				}
				else
				{
					out.write("<var name='");
				}
				out.write(xmlFormat.replaceChars(allFields[j].getName()));
				out.write("'>");

				try
				{
					Object fieldValue = allFields[j].get(instance);
					if (fieldValue != null)
					{
						if (out instanceof InstanceTrackingPrintWriter)
						{
							if (((InstanceTrackingPrintWriter)out).isInstancePrinted(fieldValue))
							{
								new cfStringData("[circular reference ...]").dumpWDDX(version, out);
							}
							else
							{
								((InstanceTrackingPrintWriter)out).instancePrinted(fieldValue);
								tagUtils.convertToCfData(fieldValue).dumpWDDX(version, out);
							}
						}
						else
						{
							tagUtils.convertToCfData(fieldValue).dumpWDDX(version, out);
						}
					}
					else
					{
						cfNullData.NULL.dumpWDDX(version, out);
					}
				}
				catch (Exception ex)
				{
					new cfStringData("[undeterminable value]").dumpWDDX(version, out);
				}
				if (version > 10)
					out.write("</v>");
				else
					out.write("</var>");
			}
		}

		// close object's structure
		if (version > 10)
			out.write("</s>");
		else
			out.write("</struct>");
	}

  public String toString(){
    return toString("");
  }

	public String toString(String _lbl)
	{
		StringBuilder out = new StringBuilder();

		out.append("<table class='cfdump_table_object'>");

		//--[ Print out the header
		out.append("<th class='cfdump_th_object' colspan='2'>");
		if (_lbl.length() > 0)
		{
			out.append(_lbl);
			out.append(" - ");
		}
		out.append("object of ");
		if (getInstanceClassName() != null)
			out.append(getInstanceClassName());
		else
			out.append("[undefined class]");
		out.append("</th>");

		Class<?> cls = getInstanceClass();
		if (cls != null)
		{
			// Methods
			Method[] allMethods = cls.getMethods();
			out.append("<tr><td class='cfdump_td_object'>Methods</td>");
			out.append("<td class='cfdump_td_value'>");
			for (int i = 0; i < allMethods.length; i++)
			{
				out.append(allMethods[i].getName());
				out.append(" (returns ");
				out.append(allMethods[i].getReturnType().getName());
				out.append(") <br>");
			}
			out.append("</td></tr>");

			// Fields
			Field[] allFields = cls.getFields();
			for (int j = 0; j < allFields.length; j++)
			{
				out.append("<tr><td class='cfdump_td_object'>");
				out.append(allFields[j].getName());
				out.append("</td>");
				out.append("<td class='cfdump_td_value'>");

				try
				{
					Object fieldValue = allFields[j].get(instance);
					if (fieldValue != null)
						out.append(fieldValue.toString());
					else
						out.append("[undefined value]");
				}
				catch (Exception ex)
				{
					out.append("[undeterminable value]");
				}
				out.append("</td></tr>");
			}
		}

		out.append("</table>");
		return out.toString();
	}

	// this version of equals() is for use by the CFML expression engine
	public boolean equals( cfData _data ) throws cfmRunTimeException {
		return super.equals( _data );	// throws unsupported exception
	}

	// this version of equals() is for use by generic Collections classes
	public boolean equals( Object o )
	{
		if ( o instanceof cfJavaObjectData )
			return instance.equals( ((cfJavaObjectData)o).instance );

		return false;
	}

	public int hashCode() {
		if ( instance != null )
			return instance.hashCode();
		else
			return 0;
	}

	public cfData duplicate() {
		return null; // can't duplicate Java objects
	}

	//--[ Override the Serialising methods as we don't wish this object to be serialised
	//--[ Causes problems when we would doing things with the client scope for example.
	private void writeObject( java.io.ObjectOutputStream out ) {}
	private void readObject( java.io.ObjectInputStream in ) {}
}
