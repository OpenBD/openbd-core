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

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.alanwilliamson.lang.java.cfSCRIPTJava;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayListData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.tag.tagUtils;
import com.naryx.tagfusion.expression.compile.expressionEngine;
import com.naryx.tagfusion.expression.function.functionBase;

public class ContextImpl implements Context {
	static ThreadLocal<cfSession> threadLocal = new ThreadLocal<cfSession>();

	private static cfSession getSession(){
		return threadLocal.get();
	}

	public static void putSession(cfSession session){
		threadLocal.set( session );
	}
	
	public Object call( String function, Object... objects ) throws Exception {
		try {
			functionBase fb = expressionEngine.getFunction(function);

			// Collect up the parameters
			cfArgStructData	args 	= new cfArgStructData();
			List<cfData> params		= new ArrayList<cfData>();
			
			for ( int x=0; x < objects.length; x++ ){
				if ( fb.supportNamedParams() ){
					args.setData(x, tagUtils.convertToCfData( objects[x] ) );
				}else{
					params.add( 0, tagUtils.convertToCfData( objects[x] ) );
				}
			}
			
			if ( fb.supportNamedParams() ){
				return getForJava( fb.execute(getSession(), args) );
			}else{
				return getForJava( fb.execute(getSession(), params) );
			}
			
		} catch (cfmRunTimeException e) {
			throw new Exception( e.getMessage() );
		}
	}
	
	
	public final cfData getRawCfml( String var ) {
		try {
			return runTime.runExpression( getSession(), var );
		} catch (cfmRunTimeException e) {
			return null;
		}
	}
	
	public final String getString( String var ) {
		try {
			return getRawCfml(var).getString();
		} catch (dataNotSupportedException e) {
			return null;
		}
	}

	public final int getInt( String var ) {
		try {
			return getRawCfml(var).getInt();
		} catch (dataNotSupportedException e) {
			return -1;
		}
	}

	public final long getLong( String var ) {
		try {
			return getRawCfml(var).getLong();
		} catch (dataNotSupportedException e) {
			return -1;
		}
	}
	
	public final boolean getBoolean( String var ) {
		try {
			return getRawCfml(var).getBoolean();
		} catch (dataNotSupportedException e) {
			return false;
		}
	}
	
	public final Date getDate( String var ) {
		try {
			return new Date( getRawCfml(var).getLong() );
		} catch (dataNotSupportedException e) {
			return null;
		}
	}

	public final cfArray getArray( String var ) {
		try {
			return (cfArray)getForJava( getRawCfml(var) );
		} catch (dataNotSupportedException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public final cfQuery getQuery( String var ) {
		try {
			return (cfQuery)getForJava( getRawCfml(var) );
		} catch (dataNotSupportedException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public final cfStruct getStruct( String var ) {
		try {
			return (cfStruct)getForJava( getRawCfml(var) );
		} catch (dataNotSupportedException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	public final void set( String var, Object data ){
		try {
			getSession().setData( var, tagUtils.convertToCfData( data ) );
		} catch (cfmRunTimeException e) {
		}
	}
	
	public final Object get(String var) {
		try{
			return getForJava( getRawCfml(var) );
		}catch(Exception e){
			return null;
		}
	}
	
	public final static Object getForJava(cfData data) throws Exception {
		if (data == null) {
			return null;
		} else if (data.getDataType() == cfData.CFSTRINGDATA)
			return data.getString();
		else if (data.getDataType() == cfData.CFBOOLEANDATA)
			return data.getBoolean();
		else if (data.getDataType() == cfData.CFSTRUCTDATA)
			return new cfStruct((cfStructData) data);
		else if (data.getDataType() == cfData.CFARRAYDATA)
			return new cfArray((cfArrayListData) data);
		else if (data.getDataType() == cfData.CFCOMPONENTOBJECTDATA)
			return new cfCFC((cfComponentData) data);
		else if (data.getDataType() == cfData.CFQUERYRESULTDATA)
			return new cfQuery((cfQueryResultData) data);
		else if (data.getDataType() == cfData.CFDATEDATA)
			return new Date( data.getLong() );
		else if (data.getDataType() == cfData.CFNUMBERDATA) {
			String tmp = data.getString();
			if (tmp.lastIndexOf(".") == -1)
				return Long.valueOf(tmp);
			else
				return Double.valueOf(tmp);
		}else if ( data.getDataType() == cfData.CFJAVAOBJECTDATA ){
			return ((cfJavaObjectData)data).getInstance();
		} else
			return data.getString();
	}
	
	public final void print(String s){
		getSession().write( s );
	}
	
	public final void print(StringBuilder s){
		getSession().write( s.toString() );
	}
	
	public final void print(int i){
		getSession().write( String.valueOf(i) );
	}
	
	public final void print(long i){
		getSession().write( String.valueOf(i) );
	}
	
	public final void print(double i){
		getSession().write( String.valueOf(i) );
	}
	
	public final void print(byte i){
		getSession().write( String.valueOf(i) );
	}
	
	public final void print(boolean i){
		getSession().write( String.valueOf(i) );
	}

	public final void print(Object i){
		getSession().write( i.toString() );
	}

	public Object getAttribute(String name) {
		Hashtable<String,Object>	atts = cfSCRIPTJava.javaClassFactory.getAttributes();
		return atts.get(name);
	}

	@Override
	public Iterator<String> getAttributeNames() {
		Hashtable<String,Object>	atts = cfSCRIPTJava.javaClassFactory.getAttributes();
		return atts.keySet().iterator();
	}

	@Override
	public void setAttribute(String name, Object o) {
		Hashtable<String,Object>	atts = cfSCRIPTJava.javaClassFactory.getAttributes();
		atts.put(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		Hashtable<String,Object>	atts = cfSCRIPTJava.javaClassFactory.getAttributes();
		atts.remove(name);
	}
}
