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

package com.naryx.tagfusion.cfm.parser;

import java.io.IOException;
import java.io.StringReader;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfJavaArrayData;
import com.naryx.tagfusion.cfm.engine.cfQueryColumnData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class CFAssignmentExpression extends CFExpression {

	private static final long serialVersionUID = 1L;

	private CFExpression left;
	private CFExpression right;

	private int type;

	public CFAssignmentExpression( Token t, CFExpression _left,
	    CFExpression _right ) {
		super(t);
		left = _left;
		right = _right;
		type = t.getType();
	}

	public byte getType() {
		return CFExpression.ASSIGNMENT;
	}

	public void checkIndirect( String expr ) {
		if ( left instanceof CFVarExpression ) {
			String lhs = expr.substring(0, expr.indexOf('=')).trim();
			// check for special case of "#foo#"="bar" or '#foo#'='bar'
			if ( (lhs.startsWith("\"#") && lhs.endsWith("#\""))
			    || (lhs.startsWith("'#") && lhs.endsWith("#'")) ) {
				((CFVarExpression) left).setIndirect(true);
			}
		}
	}

	public void checkIndirectAssignments( String[] scriptSource ) {
		if ( left instanceof CFVarExpression ) {
			/*
			 * We know this is a valid assignment expression but is the LHS expression
			 * a # expression e.g. "#foo#" = "bar" Since the poundSignFilter removes
			 * the #'s for the parser we need to look at the original source. We can
			 * get the line from this CFExpression but the column isn't accurate
			 * enough for CFFullVarExpressions especially as more than one expression
			 * may occur on the same line.
			 * 
			 * We look backwards from the '=' skipping any whitespace and looking for
			 * #", #' or # on it's own.
			 */

			// find the original expression in the script source code
			String expr = scriptSource[left.getLine() - 1];
			int lhsEnd = expr.indexOf("=", left.getColumn());
			if ( lhsEnd == -1 ) {
				// if the expression is split over 2 lines
				// e.g. "#foo#"
				// = "bar"
				lhsEnd = expr.length();
			}

			char[] exprChars = expr.substring(0, lhsEnd).toCharArray();
			int i = exprChars.length - 1;
			// skip over whitespace
			while (i >= 0 && (Character.isWhitespace(exprChars[i]))) { 
				i--;
			}

			// does LHS expression end in #" or #'?
			if ( i >= 0 && (exprChars[i] == '\"' || exprChars[i] == '\'') ) {
				i--;
				if ( i >= 0 && exprChars[i] == '#' ) {
					((CFVarExpression) left).setIndirect(true);
				}
			}
		}
	}

	public cfData Eval( CFContext context ) throws cfmRunTimeException {
		boolean copy = false; // used to decide, in the case of an assignment
													// whether to copy the RHS

		cfData leftVal = null;
		cfData rightVal = null;
		cfLData lval = null; // Non-null only when assignment is to be done
		cfData val = null;

		setLineCol(context);

		leftVal = left.Eval(context);
		setLineCol(context);

		if ( leftVal.getDataType() == cfData.CFLDATA ) {
			lval = (cfLData) leftVal;
		} else if ( leftVal.getDataType() == cfData.CFSTRINGDATA ) {
			cfData result = reparse((cfStringData) leftVal, context).Eval(context);
			if ( result.getDataType() == cfData.CFLDATA ) {
				lval = (cfLData) result;
			} else {
				throw new CFException(
				    "Left-hand side of assignment is not a variable. "
				        + leftVal.getClass().getName(), context);
			}
		} else {
			throw new CFException(
			    "Left-hand side of assignment is not a variable. "
			        + leftVal.getClass().getName(), context);
		}
		

		if ( type != CFMLLexer.EQUALSOP && !lval.exists() ) {
			throw new CFException("Left-hand side of assignment does not exist.",
			    context);
		}

		boolean queryField = false;
		rightVal = right.Eval(context);
		if ( rightVal.getDataType() == cfData.CFLDATA ) {
			queryField = (rightVal instanceof indirectQueryReferenceData);
			rightVal = ((cfLData) rightVal).Get(context);
			copy = true;
		}

		if ( rightVal.getDataType() == cfData.CFARRAYDATA
		    && rightVal instanceof cfQueryColumnData ) {
			rightVal = ((cfQueryColumnData) rightVal).getData();
		}

		if ( copy ) {
			if ( cfData.isSimpleValue(rightVal) ) {
				rightVal = rightVal.duplicate();
			} else if ( queryField ) {
				if ( rightVal.getDataType() == cfData.CFARRAYDATA ) {
					rightVal = cfArrayData.createFrom((cfArrayData) rightVal);
				}
			} else if ( rightVal.getDataType() == cfData.CFARRAYDATA
			    && !(rightVal instanceof cfJavaArrayData)
			    && !(right instanceof CFFunctionExpression) ) {
				rightVal = ((cfArrayData) rightVal).copy();
			}
		}

		val = rightVal;

		if ( type != CFMLLexer.EQUALSOP ) { // +=, -=, etc
			int op = 0;
			switch (type) {
			case CFMLLexer.PLUSEQUALS:
				op = CFMLLexer.PLUS;
				break;
			case CFMLLexer.MINUSEQUALS:
				op = CFMLLexer.MINUS;
				break;
			case CFMLLexer.STAREQUALS:
				op = CFMLLexer.STAR;
				break;
			case CFMLLexer.SLASHEQUALS:
				op = CFMLLexer.SLASH;
				break;
			case CFMLLexer.MODEQUALS:
				op = CFMLLexer.MOD;
				break;
			case CFMLLexer.CONCATEQUALS:
				op = CFMLLexer.CONCAT;
				break;
			}
			val = CFBinaryExpression.evaluate(context, op, lval.Get(context),
			    rightVal);
		}

		lval.Set(val, context);

		return context._lastExpr = val;
	}

	private CFExpression reparse( cfStringData _string, CFContext _context )
	    throws cfmRunTimeException {
		// note, the fact that calling leftVal.getString() will not include the
		// pound signs is what's req'd
		// note addition of ';' at end of expression to make it parsable
		try {
			ANTLRNoCaseReaderStream input = new ANTLRNoCaseReaderStream(
			    new poundSignFilterStream(new StringReader(_string.getString())));

			CFMLLexer lexer = new CFMLLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			CFMLParser parser = new CFMLParser(tokens);
			parser.scriptMode = false;
			CFMLParser.expression_return r = parser.expression();
			CommonTree tree = (CommonTree) r.getTree();

			CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
			nodes.setTokenStream(tokens);
			CFMLTree p2 = new CFMLTree(nodes);
			p2.scriptMode = false;
			return p2.expression();
		} catch (IOException ioe) { // shouldn't happen
			throw new CFException("Invalid expression : " + left.Decompile(0),
			    _context);
		} catch (RecognitionException pe) {
			throw new CFException("Invalid expression : " + left.Decompile(0),
			    _context);
		} catch (poundSignFilterStreamException e) {
			throw new CFException("Invalid expression : " + left.Decompile(0),
			    _context);
		}

	}

	public String Decompile( int indent ) {
		return left.Decompile(0) + "=" + right.Decompile(0);
	}
}
