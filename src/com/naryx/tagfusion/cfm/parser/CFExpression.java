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
 *  http://www.openbluedragon.org/
 *  $Id: CFExpression.java 2197 2012-07-21 16:55:40Z alan $
 */

package com.naryx.tagfusion.cfm.parser;

import java.io.IOException;
import java.io.StringReader;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.script.CFStatementResult;

public abstract class CFExpression extends CFParsedStatement implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	public static byte 	FUNCTION = 0,
											ASSIGNMENT = 1, 
											BINARY = 2, 
											LITERAL = 3, 
											IDENTIFIER = 4, 
											VARIABLE = 5, 
											UNARY = 6;
	
	public static CFExpression getCFExpression( String _infix ) throws cfmBadFileException {

		try{
			ANTLRNoCaseReaderStream input = new ANTLRNoCaseReaderStream( new poundSignFilterStream( new StringReader( _infix ) ) );
			
			CFMLLexer lexer = new CFMLLexer( input );
			CommonTokenStream tokens = new CommonTokenStream( lexer );
			CFMLParser parser = new CFMLParser( tokens );
			parser.scriptMode = false;
			CFMLParser.expression_return r = parser.expression();
			CommonTree tree = (CommonTree) r.getTree();
		
			CommonTreeNodeStream nodes = new CommonTreeNodeStream( tree );
			nodes.setTokenStream( tokens );
			CFMLTree p2 = new CFMLTree( nodes );
			p2.scriptMode = false;
			CFExpression exp = p2.expression();
			
			if ( exp instanceof CFAssignmentExpression ) {
				((CFAssignmentExpression)exp).checkIndirect( _infix );
			}

			return exp;
		}catch( RecognitionException e ){
			throw new cfmBadFileException( catchDataFactory.extendedException( "errorCode.expressionError",
					"expression.Parse", new String[]{_infix}, e.getMessage() ) );
		}catch( ParseException e ){
			throw new cfmBadFileException( catchDataFactory.extendedException( "errorCode.expressionError",
					"expression.Parse", new String[]{_infix}, e.getMessage() ) );
		} catch ( IOException e ) {
			throw new cfmBadFileException( catchDataFactory.extendedException( "errorCode.expressionError",
					"expression.syntaxError", new String[]{_infix}, e.getMessage() ) );
		} catch( poundSignFilterStreamException e ) {
			throw new cfmBadFileException( catchDataFactory.extendedException( "errorCode.expressionError",
					"expression.syntaxError", new String[]{_infix}, e.getMessage() ) );
		}
	}

	
	public byte getType(){
		return -1;
	}

	public boolean isEscapeSingleQuotes(){
		return false;
	}

  public CFExpression( org.antlr.runtime.Token t) {
  	super( t );
  }

  public CFStatementResult Exec( CFContext context) throws cfmRunTimeException {
    Eval(context);
    return null;
  }

  public abstract cfData Eval( CFContext context) throws cfmRunTimeException;

  public cfData EvalFully( CFContext context) throws cfmRunTimeException{
		cfData returned = Eval(context);
		if ( returned.getDataType() == cfData.CFLDATA ) 
			return ((cfLData) returned).Get(context);
		else
			return returned;
  }
  
}