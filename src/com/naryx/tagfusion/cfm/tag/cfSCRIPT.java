/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: cfSCRIPT.java 2197 2012-07-21 16:55:40Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.sourceReader;
import com.naryx.tagfusion.cfm.parser.ANTLRNoCaseReaderStream;
import com.naryx.tagfusion.cfm.parser.CFMLLexer;
import com.naryx.tagfusion.cfm.parser.CFMLParser;
import com.naryx.tagfusion.cfm.parser.CFMLTree;
import com.naryx.tagfusion.cfm.parser.poundSignFilterStream;
import com.naryx.tagfusion.cfm.parser.poundSignFilterStreamException;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.parser.script.CFCompoundStatement;
import com.naryx.tagfusion.cfm.parser.script.CFEmptyStatement;
import com.naryx.tagfusion.cfm.parser.script.CFFuncDeclStatement;
import com.naryx.tagfusion.cfm.parser.script.CFScriptStatement;
import com.naryx.tagfusion.cfm.parser.script.CFStatementResult;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class cfSCRIPT extends cfTag implements ContentTypeStaticInterface, Serializable { 
	static final long serialVersionUID = 1;
	
	private transient CFScriptStatement scriptStatement;
  private static HashMap<String,String>	registeredLanguages = null;
	
  public static void init( xmlCFML configFile ) {
  	cfEngine.thisPlatform.registerScriptExtensions();
  }
  
  
  
	/**
	 * Allows external engines to be able to register for lang="" support
	 * 
	 * @param lang
	 * @param impl
	 */
	public static void registerLanguage( String lang, String impl ){
		if ( registeredLanguages == null )
			registeredLanguages = new HashMap<String,String>();
			
		registeredLanguages.put(lang.toLowerCase(), impl);
		
		cfEngine.log("cfSCRIPT Registered: language=" + lang.toLowerCase() + "; class=" + impl );
	}

	
  public static cfSCRIPT	getLanguage( String lang ) {
  	String cp = registeredLanguages.get(lang.toLowerCase());
  	if ( cp == null )
  		return null;
  	
  	try {
			return (cfSCRIPT)Class.forName(cp).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
  }
  
  
  protected void defaultParameters( String _tag ) throws cfmBadFileException {
    parseTagHeader( _tag );
  }

	
	@SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
		return createInfo("system", "Contains block of CFSCRIPT statements to be evaluated.");
	}
	
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo("LANGUAGE", "Specifies the type of language contained within the cfscript block.  Additional languages are added through the plugin interface. Built-in support for 'java' and 'javascript'", "", false ),
				createAttInfo("JARLIST", "For language='java' only.  Specifies the comma-separated list of JAR files that are in the /WEB-INF/lib/ folder that should be added to the dynamic compilation", "", false ),
				createAttInfo("IMPORT", "For language='java' only.  Specifies the comma-separated list of packages that will be used to resolve inner class references. By default these are included: java.io.*, java.net.*, java.math.*, java.util.*", "", false )
		};
	}

	
	public String getEndMarker(){	return "</CFSCRIPT>"; }

	protected void tagLoadingComplete() throws cfmBadFileException {
		initScriptStatement();
		if ( scriptStatement instanceof CFCompoundStatement ) {
			cfFile parentFile = this.getFile();
			Iterator<CFScriptStatement> stmtIter = ((CFCompoundStatement)scriptStatement).getStatements().iterator();
			while ( stmtIter.hasNext() ) {
				CFScriptStatement stmt = stmtIter.next();
				if ( stmt instanceof CFFuncDeclStatement ) {
					userDefinedFunction udf = ((CFFuncDeclStatement)stmt).getUDF();
					parentFile.addUDF( this, udf );
					if ( this.isSubordinate( "CFCOMPONENT" ) ) {
						((cfCOMPONENT)parentTag).addFunctionMetaData( udf.getMetaData() );
					}
				}
			}
		}
		

		// Attach a reference to this tag to each of the statements this defines
		scriptStatement.setHostTag( this );
		
		// Clean up
		wrapUnsedVars();
	}
	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		CFStatementResult result = runTime.run( _Session, scriptStatement );
		
	// allow return values to propagate up for CFFUNCTION
		if ( ( result != null ) && result.isReturn() )   
			return cfTagReturnType.newReturn( result.getReturnValue() );
		else
			return cfTagReturnType.NORMAL;
	}
  
	public void setStatement( CFScriptStatement _statement ){
		scriptStatement = _statement;
	}
  
	private void initScriptStatement() throws cfmBadFileException {
		if ( scriptStatement != null ) return;
			
		// Need to parse and compile the script
    char [] scriptWithEndTag = getStaticBody();
    if ( scriptWithEndTag.length == 0 ){
    	scriptStatement = new CFEmptyStatement();
			return;
    }

		try{
			ANTLRNoCaseReaderStream input = new ANTLRNoCaseReaderStream( new poundSignFilterStream( new CharArrayReader( scriptWithEndTag ) ) );
			
			CFMLLexer lexer = new CFMLLexer( input );
			CommonTokenStream tokens = new CommonTokenStream( lexer );
			CFMLParser parser = new CFMLParser( tokens );
			ParserRuleReturnScope r = parser.scriptBlock();
			CommonTree tree = (CommonTree) r.getTree();

			CommonTreeNodeStream nodes = new CommonTreeNodeStream( tree );
			nodes.setTokenStream( tokens );
			CFMLTree p2 = new CFMLTree( nodes );
			scriptStatement = p2.scriptBlock();
			
			// find special cases of "#varName#"="value";
			sourceReader sr = new sourceReader( new BufferedReader( new CharArrayReader( scriptWithEndTag ) ) );
			scriptStatement.checkIndirectAssignments( sr.getLines() );

		}catch( RecognitionException e ){
			int startLine = e.line - 1;
	
			throw new cfmBadFileException( catchDataFactory.extendedScriptException(
								"errorCode.expressionError", "expression.Parse",
								new String[] { e.getMessage() }, startLine, 1 ) );
			
		} catch ( IOException e ) {
			
			throw new cfmBadFileException(catchDataFactory.extendedException(
					"errorCode.expressionError", "expression.Parse",
					new String[] { new String( scriptWithEndTag ) }, e.getMessage() ) );
			
		} catch ( poundSignFilterStreamException e ) {
			
			throw new cfmBadFileException(catchDataFactory.extendedException(
					"errorCode.expressionError", "expression.Parse",
					new String[] { new String( scriptWithEndTag ) }, e.getMessage() ) );

		}
	}
	
}