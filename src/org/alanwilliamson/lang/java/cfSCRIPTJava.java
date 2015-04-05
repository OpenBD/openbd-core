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
package org.alanwilliamson.lang.java;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;

import org.alanwilliamson.lang.java.inline.ContextImpl;
import org.alanwilliamson.lang.java.inline.JavaBase;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.script.JavaBlock;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.tag.ContentTypeStaticInterface;
import com.naryx.tagfusion.cfm.tag.cfSCRIPT;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfSCRIPTJava extends cfSCRIPT implements ContentTypeStaticInterface, Serializable {
	private static final long serialVersionUID = 1L;

	public static JavaClassFactory	javaClassFactory = null;
	
	private JavaBlock	javaBlock = null;
	
	protected void tagLoadingComplete() throws cfmBadFileException {
		if ( javaClassFactory == null )
			return;
		
		String jarlist		= getConstant("JARLIST");
		String importlist	= getConstant("IMPORT");
		String javabody		= new String(this.getStaticBody());
		
		// We need to determine which one it is we are looking
		try {
			javaBlock	= javaClassFactory.getCompiledClass( javaClassFactory.javaClassTemplate, javabody, jarlist, importlist );
			
		} catch (CharSequenceCompilerException e) {
			
			if ( javaClassFactory.wasExpectingMethods(e) ){
				
				try {
					javaBlock	= javaClassFactory.getCompiledClass( javaClassFactory.javaBlockTemplate, javabody, jarlist, importlist );
				} catch (CharSequenceCompilerException ee) {
					throw new cfScriptCompilationException( ee, javabody, catchDataFactory.tagRuntimeException( this, "&lt;cfscript language='java'&gt;", "" ), this, javaClassFactory.SRC_OFFSET_INLINE );
				} catch (Exception ie) {
					throw newBadFileException("SCRIPT", e.getMessage() );
				}
				
			}else{
				throw new cfScriptCompilationException( e, javabody, catchDataFactory.tagRuntimeException( this, "&lt;cfscript language='java'&gt;", "" ), this, javaClassFactory.SRC_OFFSET_INLINE );
			}
		} catch (Exception e) {
			
			throw newBadFileException("SCRIPT", e.getMessage() );
			
		} finally {
			this.tagBody	= emptyTagBody; // We don't need to keep this content hanging around in memory
		}
		

		// Add this to the UDF
		if ( (javaBlock instanceof JavaBase) && this.isSubordinate( "CFCOMPONENT" ) ) {
			attachToComponent(javaBlock);
		}
	}
	
	
	private void attachToComponent(JavaBlock jb) throws cfmBadFileException {
		Method[] methods 	= jb.getClass().getMethods();
		cfFile parentFile = this.getFile();
		
		for ( Method method : methods ){
			
			// We only want the public methods defined in the JavaBlock and nothing else
			if ( Modifier.isPublic( method.getModifiers() ) &&
					method.getDeclaringClass().equals(jb.getClass()) ){
								
				userDefinedFunction udf = new userDefinedFunction( method, jb );
				parentFile.addUDF( this, udf );
				
			}
		}
	}
	
	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		
		if ( javaBlock == null )
			return cfTagReturnType.NORMAL;

		try{
			ContextImpl.putSession(_Session);
			
			/**
			 * We have to check if this particular java block is a full class with methods or if its just a snippet.
			 * 
			 * If its a full Java class with methods, then we want to create a new instance of it as there could be
			 * threading collisions with the 'cf' method as multiple clients access it.  The reason is that we declare
			 * 'cf' as a class level member, so all methods in the class has access to it.
			 * 
			 * On the other hand if it is a just a Java snippet, then it is simple function call, and therefore not
			 * prone to the thread problems that a whole class would be. 
			 */
			if ( javaBlock instanceof JavaBase ){
				
				JavaBase newInst = (JavaBase)javaBlock.getClass().newInstance();
				newInst.r();
				

				/**
				 * This JavaBlock represents a function of the CFC that it belongs to.  However, we have to reset
				 * the Java references because it was originally created with the ones at load time.  We run around
				 * the cfComponentData, looking for the UDF functions that are acting as our java wrapper and reset
				 * the internal JavaBlock references
				 */
				if ( this.isSubordinate( "CFCOMPONENT" ) ) {
					
					cfComponentData cfcompData	= _Session.getActiveComponentData();
					Iterator	it = cfcompData.values().iterator();
					while ( it.hasNext() ){
						Object o = it.next();
						if ( o instanceof userDefinedFunction ){
							userDefinedFunction	udf	= (userDefinedFunction)o;
							
							if ( udf.isJavaBlock() && udf.getJavaBlock() == javaBlock ){
								udf.setJavaBlock( (JavaBlock)newInst );
							}
						}
						
					}

				}
				
			}else{
				javaBlock.r();	
			}

		}catch(NullPointerException npe){
			throw newRunTimeException( "CFSCRIPT language='java': NullPointerException Thrown" );
		}catch(Exception e){
			throw newRunTimeException( e.getMessage() );
		}

		return cfTagReturnType.NORMAL;
	}
}
