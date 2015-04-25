/* 
 *  Copyright (C) 2012-2015 TagServlet Ltd
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
 */
package com.naryx.tagfusion.cfm.engine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.aw20.io.StreamUtil;

import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlURI;
import com.naryx.tagfusion.cfm.file.sourceReader;
import com.naryx.tagfusion.cfm.parser.ANTLRNoCaseReaderStream;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.CFLiteral;
import com.naryx.tagfusion.cfm.parser.CFMLLexer;
import com.naryx.tagfusion.cfm.parser.CFMLParser;
import com.naryx.tagfusion.cfm.parser.CFMLTree;
import com.naryx.tagfusion.cfm.parser.poundSignFilterStream;
import com.naryx.tagfusion.cfm.parser.script.CFComponentDeclStatement;
import com.naryx.tagfusion.cfm.parser.script.CFScriptStatement;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.tag.cfCOMPONENT;
import com.naryx.tagfusion.cfm.tag.cfSCRIPT;
import com.naryx.tagfusion.cfm.tag.cfTag;

public class ComponentScriptFactory {

	/**
	 * This function is called when component file was found, but it didn't actually
	 * contain any <cfcomponent> tag.  We will therefore attempt to load it via the 
	 * cfscript parser
	 * 
	 * @param componentFile
	 * @return
	 */
	public static cfFile load(cfFile componentFile) throws cfmRunTimeException  {

		Reader read = null;
		poundSignFilterStream psf = null;
		ANTLRNoCaseReaderStream input = null;
		CFMLLexer lexer = null;
		CommonTokenStream tokens = null;

		cfmlURI	cfmluri	= componentFile.getCfmlURI();
		
		try{
			read	= new BufferedReader( new FileReader( cfmluri.getFile() ) );

			psf 		= new poundSignFilterStream(read);
			input 	= new ANTLRNoCaseReaderStream(psf);
			lexer 	= new CFMLLexer(input);
			tokens 	= new CommonTokenStream(lexer);
			
			CFMLParser parser = new CFMLParser(tokens);
			ParserRuleReturnScope r = parser.scriptBlock();

			CommonTree tree = (CommonTree)r.getTree();

			CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
			nodes.setTokenStream(tokens);
			CFMLTree p2 = new CFMLTree(nodes);
			CFScriptStatement statement = p2.scriptBlock();

			if ( !(statement instanceof CFComponentDeclStatement) )
				return null;

			// find special cases of "#varName#"="value";
			StreamUtil.closeStream(read);
			read	= new BufferedReader( new FileReader( cfmluri.getFile() ) );
			sourceReader sr = new sourceReader( new BufferedReader( read ) );
			statement.checkIndirectAssignments( sr.getLines() );
			
			
			// Now to create the COMPONENT -> CFSCRIPT combination
			cfCOMPONENT	cfcomponent	= new cfCOMPONENT();
			setComponentAttributes( cfcomponent, (CFComponentDeclStatement)statement );

			cfSCRIPT	cfscript	= new cfSCRIPT();
			cfscript.setParentTag(cfcomponent);
			cfscript.setStatement(statement);
			statement.setHostTag(cfscript);

			cfTag	componentTagHolder	= new cfTag();
			componentTagHolder.setChildTag(cfcomponent);
			componentTagHolder.setParentFile(componentFile);
			
			cfcomponent.setChildTag(cfscript);
			cfcomponent.setParentTag(componentTagHolder);
			cfcomponent.tagLoadingComplete();
			
			componentFile.setFileBody(componentTagHolder);
			
			// Set the udf's
			List<userDefinedFunction>	functions = ((CFComponentDeclStatement)statement).getUDFs();
			Iterator<userDefinedFunction>	it	= functions.iterator();
			while ( it.hasNext() ){
				userDefinedFunction udf	= it.next();
				componentFile.addUDF(cfscript, udf);
				cfcomponent.addFunctionMetaData( udf.getMetaData() );
			}
			
			cfcomponent.tagLoadingComplete();
			
			return componentFile;
			
		} catch (com.naryx.tagfusion.cfm.parser.CFParseException pe) {

			cfCatchData	cd	= new cfCatchData();
			cd.setLine( pe.line );
			cd.setColumn( pe.charPositionInLine );
			cd.setDetail( pe.getMessage() );
			cd.setMessage("cfcomponent{}");
			cd.setFileURI(cfmluri);
			throw new cfmRunTimeException( cd );
			
		} catch (Exception e) {
			throw new cfmRunTimeException( catchDataFactory.extendedException("1", "cfcomponent{}", e.getMessage()) );
		} finally {
			StreamUtil.closeStream(read);
		}
	}

	
	
	private static void setComponentAttributes(cfCOMPONENT cfcomponent, CFComponentDeclStatement componentscript ) throws cfmBadFileException, dataNotSupportedException{
		if ( componentscript.getAttributes() != null ){
			Iterator<String>	it	= componentscript.getAttributes().keySet().iterator();
			while ( it.hasNext() ){
				String key	= it.next();
	
				CFExpression o = componentscript.getAttributes().get(key);
				if ( o instanceof CFLiteral ){
					cfcomponent.defaultAttribute(key, ((CFLiteral)o).getVal().getString() );
				}
			}
		}
		cfcomponent.validateParameters( -1 );
	}
	
}