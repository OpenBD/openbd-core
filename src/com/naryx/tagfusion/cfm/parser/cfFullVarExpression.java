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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Vector;

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryColumnData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfWSObjectData;
import com.naryx.tagfusion.cfm.engine.cfcMethodData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;
import com.naryx.tagfusion.cfm.xml.cfXmlData;
import com.naryx.tagfusion.cfm.xml.cfXmlDataArray;

public class cfFullVarExpression extends CFVarExpression implements Serializable {
	
	private static final long serialVersionUID = 1;

	private StringBuilder image;
	private Token token;
	private List<CFExpression> expressions;

	// if true, this variable references the client or cookie scope. These scopes
	// cannot contain structs but can contain variables with names containing periods.
	private boolean isCookieClientVariable;

	/**
	 * The operator list is represented as a BitSet since there are only 2
	 * operators when it comes to the variables - the '.' and the '[' '.' is
	 * represented as 1 and '[' is represented as 0 in the BitSet
	 */
	private BitSet operators;

	private static final boolean DOT = true, LBRACKET = false;
	private int exprSize;

	public cfFullVarExpression( Token _t, CFExpression _main, String _image ) {
		super(_t);
		token = _t;
		image = new StringBuilder(_image);
		expressions = new ArrayList<CFExpression>();
		expressions.add(_main);
		operators = new BitSet(8);
		exprSize = 0;

		isCookieClientVariable = false;
		if ( _main instanceof CFIdentifier ) {
			String name = ((CFIdentifier) _main).getName().toLowerCase();
			if ( name.equals("cookie") || name.equals("client") ) {
				isCookieClientVariable = true;
			}
		}
	}

	public byte getType() {
		return expressions.get(expressions.size() - 1).getType();
	}

	public boolean isEscapeSingleQuotes() {
		return expressions.get(expressions.size() - 1).isEscapeSingleQuotes();
	}

	public void addBracketOperation( CFExpression _right ) {
		// if this is a cookie/client var we want to combine the struct key
		expressions.add(_right);
		exprSize++;
		image.append('[');
		image.append(_right.Decompile(0));
		image.append(']');
	}

	public void addDotOperation( CFExpression _right ) {
		if ( isCookieClientVariable ) { // a client/cookie expression with only '.'s
																		// should be at most 2 subexpressions -
																		// 'cookie' and variable name in the cookie
																		// scope
			if ( expressions.size() == 2 && operators.get(1) == DOT ) {
				CFExpression exp = expressions.get(1);
				expressions.remove(1);
				String subImage = exp.Decompile(0) + "." + _right.Decompile(0);
				expressions.add(new CFIdentifier(token, subImage.toString()));
			} else {
				expressions.add(_right);
				exprSize++;
				operators.set(exprSize);
				image.append('.');
				image.append(_right.Decompile(0));
			}

		} else {
			expressions.add(_right);
			exprSize++;
			operators.set(exprSize);
			image.append('.');
			image.append(_right.Decompile(0));
		}
	}

	public cfData Eval( CFContext _context ) throws cfmRunTimeException {
		setLineCol(_context);

		// INVARIANT:- operators.size() == expressions.size() - 1;
		cfData leftData = null;

		// -- first attempt

		// evaluate the lhs so we have a cfData to begin the loop with
		leftData = evalNatural(_context, false);

		// -- loop thru the combinations of possibles
		// at this point we know the whole expression doesn't evaluate
		if ( leftData == null ) {
			leftData = evalBruteForce(_context);
			if ( leftData.getDataType() == cfData.CFLDATA ) {
				if ( !((cfLData) leftData).exists() ) {
					leftData = null;
				} else {
					try {
						extIndirectReferenceData tmp2 = (extIndirectReferenceData) evalNatural(
						    _context, true);
						leftData = new combinedCFLData(tmp2, (cfLData) leftData, image.toString());
					} catch (cfmRunTimeException e) { // doesn't exist
						leftData = null;
					}
				}
			}
		}

		// -- if we still don't have a value then create try
		// evaluation with the variable being created if required.
		if ( leftData == null ) {
			leftData = evalNatural(_context, true);
		}

		_context._lastExpr = leftData;
		if ( indirect && (_context._lastExpr.getDataType() == cfData.CFLDATA) ) {
			_context._lastExpr = ((cfLData) _context._lastExpr).Get(_context);
		}

		return _context._lastExpr;
	}

	private CFExpression combine( int _start, int _end ) {
		StringBuilder newImage = new StringBuilder();
		newImage.append( expressions.get(_start).Decompile(0) );
		boolean nextOp;

		for (int i = _start + 1; i <= _end; i++) {
			nextOp = operators.get(i);
			if ( nextOp ) {
				newImage.append('.');
				newImage.append( expressions.get(i).Decompile(0) );
			} else {
				newImage.append('[');
				newImage.append( expressions.get(i).Decompile(0) );
				newImage.append(']');
			}
		}

		return new CFIdentifier(token, newImage.toString());
	}

	private cfData evalBruteForce( CFContext _context ) throws cfmRunTimeException {
		int expressionCount = 1; // number of expressions that have been combined
														 // from the LHS
		cfData nextLHS;
		cfData result = null;
		CFExpression nextExpression = expressions.get(0);
		int noExprs = expressions.size();

		// loop thru expressions on the lhs combining those
		do {
			try {
				nextLHS = evalLHS(_context, nextExpression, true);
				if ( nextLHS != null )
					result = recurseLeft(_context, expressionCount - 1, nextLHS);
			} catch (cfmRunTimeException ignore) {
			}
			nextExpression = combine(0, expressionCount);
			expressionCount++;

		} while (result == null && expressionCount < noExprs);

		if ( result == null ) { // last expression ["a.b.c...."]
			result = evalLHS(_context, nextExpression, true);
		}

		return result;
	}

	private cfData recurseLeft( CFContext _context, int _leftPtr, cfData _leftData )
	    throws cfmRunTimeException {
		int rightPtr = _leftPtr + 1;
		int leftPtr = _leftPtr;

		cfData leftData = _leftData;

		// exit clause
		if ( rightPtr == expressions.size() ) {
			return _leftData;
		} else if ( leftData.getDataType() == cfData.CFLDATA ) {
			if ( ((cfLData) leftData).exists() ) {
				leftData = ((cfLData) leftData).Get(_context);
			} else {// doesn't exist
				return null;
			}
		}

		CFExpression nextExpression = expressions.get(rightPtr);
		int joinCount = 1;
		boolean op;

		// loop thru the expressions on the RHS of the expression ptr
		while (rightPtr + joinCount <= expressions.size()) {
			op = operators.get(leftPtr + 1);
			cfData subresult = Eval(_context, leftData, 0, nextExpression, op, true,
			    false);

			if ( subresult != null ) {
				subresult = recurseLeft(_context, leftPtr + joinCount, subresult);
				if ( subresult != null ) {
					return subresult;
				}
			}

			// if we have more combinations to try, create a new nextExpression
			// for the next iteration
			if ( rightPtr + joinCount < expressions.size() ) {
				nextExpression = combine(rightPtr, rightPtr + joinCount);

			}
			joinCount++;
		}

		return null;
	}

	private cfData evalNatural( CFContext _context, boolean _create ) throws cfmRunTimeException {
		CFExpression rightExp;
		CFExpression leftExp = expressions.get(0);
		boolean op;
		cfData leftData = evalLHS(_context, leftExp, true);
		int noExprs = expressions.size();

		for (int i = 1; i < noExprs; i++) {
			// apply the RH expression to the cfData
			rightExp = expressions.get(i);
			op = operators.get(i);
			leftData = Eval(_context, leftData, i, rightExp, op, true, _create);
			if ( leftData == null ) {
				break;
			}
		}

		if ( leftData == null ) {
			leftData = evalLHS(_context, leftExp, false);

			for (int i = 1; i < noExprs; i++) {
				// apply the RH expression to the cfData
				rightExp = expressions.get(i);
				op = operators.get(i);
				leftData = Eval(_context, leftData, i, rightExp, op, false, _create);
				if ( leftData == null ) {
					break;
				}
			}
		}

		return leftData;
	}

	private static cfData evalLHS( CFContext _context, CFExpression _leftExp,
	    boolean _doQuerySearch ) throws cfmRunTimeException {
		cfData leftVal = null;

		if ( _doQuerySearch ) {
			leftVal = _leftExp.Eval(_context);
		} else {
			if ( _leftExp instanceof CFIdentifier ) {
				leftVal = ((CFIdentifier) _leftExp).Eval(_context, false);
			} else {
				leftVal = _leftExp.Eval(_context);
			}
		}

		return leftVal;
	}

	private cfData Eval( CFContext _context, cfData _leftData, int _exprIndex,
	    CFExpression _right, boolean _op, boolean _doquerysearch, boolean _create )
	    throws cfmRunTimeException {

		cfData leftVal = _leftData;
		cfData val = null; // the result of this operation
		cfQueryResultData query = null;

		// on most occasions we want to evaluate the LHS with a query scope search
		// included

		boolean isIndirQuery = (leftVal instanceof indirectQueryReferenceData);
		boolean isJavaMethodCall = (_right instanceof CFJavaMethodExpression);

		if ( !_create ) {

			if ( leftVal.getDataType() == cfData.CFLDATA ) {
				cfLData lData = (cfLData) leftVal;
				if ( lData.exists() ) {
					query = lData.getQueryResult();
					leftVal = lData.Get(_context);
				} else {
					return null;
				}
			}

		} else {
			if ( !isJavaMethodCall ) {
				// if it's a cfLData and hasn't already been created as a
				// extIndirectReferenceData
				if ( leftVal.getDataType() == cfData.CFLDATA
				    && !(leftVal instanceof extIndirectReferenceData) ) {
					if ( ((cfLData) leftVal).exists() ) {
						query = ((cfLData) leftVal).getQueryResult();
						leftVal = ((cfLData) leftVal).Get(_context);
					} else {
						// create new extendedIndirectReference with no index
						// (the index will be added later on when it comes to evaluation)
						leftVal = new extIndirectReferenceData((cfLData) leftVal,
						    getImage(_exprIndex));
					}
				}
			} else if ( leftVal.getDataType() == cfData.CFLDATA ) {
				query = ((cfLData) leftVal).getQueryResult();
				leftVal = ((cfLData) leftVal).Get(_context);
			}
		}

		// Java method call
		if ( isJavaMethodCall && leftVal instanceof cfJavaObjectData ) {
			if ( leftVal instanceof cfComponentData ) {
				cfArgStructData namedArgs = (cfArgStructData) _right.Eval(_context); // evaluate
																																						 // the
																																						 // arguments
				if ( namedArgs != null ) {
					cfcMethodData invocationData = new cfcMethodData(_context
					    .getSession(), ((CFJavaMethodExpression) _right)
					    .getFunctionName(), namedArgs);
					val = ((cfComponentData) leftVal).invokeComponentFunction(_context
					    .getSession(), invocationData);
				} else {
					val = ((cfComponentData) leftVal).invokeComponentFunction(_context
					    .getSession(), (CFJavaMethodExpression) _right);
				}
			} else if ( leftVal instanceof cfWSObjectData ) {
				_right.Eval(_context); // evaluate the arguments
        val = ((cfWSObjectData) leftVal).getWSData((CFJavaMethodExpression) _right, _context ); 
			} else {
				// see if it's a CFC method call within the super scope (or some other
				// scope)
				cfData udf = leftVal.getData(((CFJavaMethodExpression) _right).getFunctionName());
				if ( udf instanceof userDefinedFunction ) {
					cfArgStructData evaluatedArgs = (cfArgStructData) ((CFJavaMethodExpression) _right).Eval( _context );
					if ( evaluatedArgs != null ){
						val = ((userDefinedFunction) udf).execute(_context.getSession(),
													    evaluatedArgs, true );
					}else{
						val = ((userDefinedFunction) udf).execute(_context.getSession(),
								((CFJavaMethodExpression) _right).getEvaluatedArguments(_context,
										true));
					}
						
				} else {
					// treat it as a method call on a Java object
					_right.Eval(_context); // evaluate the arguments
					val = ((cfJavaObjectData) leftVal).getJavaData( (CFJavaMethodExpression) _right, _context);
				}
			}
			// Java field access
		} else if ( leftVal.getDataType() == cfData.CFJAVAOBJECTDATA
		    && _right instanceof CFIdentifier ) {
			val = ((cfJavaObjectData) leftVal).getJavaData(_op == DOT ? 
					new cfStringData(((CFIdentifier) _right).getName()) : evaluateRHS(_context, _right));
		} else if ( leftVal.getDataType() == cfData.CFJAVAOBJECTDATA
		    && _op == LBRACKET ) {
			val = ((cfJavaObjectData) leftVal).getJavaData(evaluateRHS(_context,
			    _right));
		}
		// component property field access
		else if ( leftVal.getDataType() == cfData.CFCOMPONENTOBJECTDATA
		    && _right instanceof CFIdentifier ) {
			if ( _op == DOT ) {
				val = new indirectReferenceData(_context.getSession(), Decompile(0),
				    leftVal, new cfStringData(((CFIdentifier) _right).getName()));
			} else { // LBRACKET - must be an expression that indicates the index
				val = new indirectReferenceData(_context.getSession(), Decompile(0),
				    leftVal, evaluateRHS(_context, _right));
			}
		} else if ( leftVal.getDataType() == cfData.CFCOMPONENTOBJECTDATA
		    && _op == LBRACKET ) {
			val = new indirectReferenceData(_context.getSession(), Decompile(0),
			    leftVal, evaluateRHS(_context, _right));
		} else if ( _create && leftVal instanceof extIndirectReferenceData ) {
			// add the index to the extIndirectReferenceData
			if ( _op == DOT ) {
				((extIndirectReferenceData) leftVal).addIndex_Dot(new cfStringData(
				    ((CFIdentifier) _right).getName()));
			} else {
				((extIndirectReferenceData) leftVal).addIndex_Bracket(evaluateRHS(
				    _context, _right));
			}
			val = leftVal;

			// Query field but using [] notation to get at rows
			// Note:- this skips over if the LHS was evaluated as an
			// indirectQueryReferenceData
			// to avoid the case where we have query.arrayfield[1][1]
		} else if ( _op == LBRACKET
		    && (query != null || (leftVal.getQueryTableData() != null && leftVal
		        .getDataType() != cfData.CFQUERYRESULTDATA)) && !isIndirQuery ) {
			int rowIndex;
			try {
				cfData rowIndexData = evaluateRHS(_context, _right);
				if ( rowIndexData == null ) {
					throw new CFException("Invalid index to query column: "
					    + _right.Decompile(0), _context);
				}
				rowIndex = rowIndexData.getNumber().getInt();
			} catch (cfmRunTimeException e) {
				throw new CFException(
				    "Invalid index to query column. " + _right.Decompile(0)
				        + " cannot be evaluated to an integer value.", _context);
			}

			int colIndex = 0;
			List queryData;
			if ( query != null && _leftData instanceof indirectReferenceData ) {
				queryData = query.getQueryTableData();
				colIndex = query.getColumnIndexCF(((indirectReferenceData) _leftData).getIndex().getString());
			} else {
				queryData = leftVal.getQueryTableData();
				colIndex = leftVal.getQueryColumn();
			}
			val = new indirectQueryReferenceData(Decompile(0), queryData, rowIndex,
			    colIndex);

			// XML field access, if using [some_number], convert to a cfXmlDataArray
		} else if ( leftVal instanceof cfXmlData && _op == LBRACKET
		    && (val = evaluateXmlData(_context, _right, leftVal)) != null ) {
			// do nothing, since val has already been assigned

			// STRUCT / QUERY field access using . or []
		} else if ( leftVal.getDataType() == cfData.CFSTRUCTDATA
		    || leftVal.getDataType() == cfData.CFQUERYRESULTDATA ) {
			if ( _op == DOT ) {
				val = new indirectReferenceData(Decompile(0), leftVal,
				    new cfStringData(((CFIdentifier) _right).getName()));
			} else {
				// LBRACKET - must be an expression that indicates the index
				if ( leftVal.getDataType() == cfData.CFQUERYRESULTDATA ) {
					return new cfQueryColumnData((cfQueryResultData) leftVal,
					    evaluateRHS(_context, _right));
				} else {
					val = new indirectReferenceData(Decompile(0), leftVal, evaluateRHS(
					    _context, _right));
				}
			}

			// ARRAY
		} else if ( leftVal.getDataType() == cfData.CFARRAYDATA ) {
			// create indirect reference
			cfData index = evaluateRHS(_context, _right);
			if ( index != null ) {
				val = new indirectReferenceData(Decompile(0), leftVal, index);
			} else if ( leftVal instanceof cfXmlDataArray ) {
				// If this is a cfXmlDataArray and there's no [x] index, assume [1].
				// So we must sneak in an xmlArray[1] eval to use with the actual
				// rhs expression
				val = new indirectReferenceData(Decompile(0), leftVal,
				    new cfNumberData(1));
				_context._lastExpr = val;
				val = Eval(_context, ((indirectReferenceData) val).Get(_context),
				    _exprIndex, _right, _op, _doquerysearch, _create);
			}else{ 
				throw new CFException( "Invalid index to array. " + _right.Decompile( 0 ) + " does not exist.", _context );
			}
		} else if ( _doquerysearch ) {
			// eval() again but without the query scope checking
			// val = Eval( _context, _leftData, _right, _op, false, _create );
			// do nothing - null will be returned and subsequently a this
			// method can be called again with _doquerysearch = false
			// otherwise it's not valid
		} else {
			throw new CFException("Invalid expression. [" + image + "]", _context);
		}
		return val;
	}

	private cfData evaluateXmlData( CFContext _context, CFExpression _right,
	    cfData leftVal ) throws cfmRunTimeException {
		cfData index = evaluateRHS(_context, _right);
		if ( (index != null) && (index.getDataType() == cfData.CFNUMBERDATA) ) {
			Vector<cfData> list = new Vector<cfData>(1);
			list.add(leftVal);
			// We need to support Document node types and orphened (no parent)
			// node types as well
			cfXmlData parent = ((cfXmlData) leftVal).getParent(); // May be null
			cfData newLeftVal = new cfXmlDataArray(parent, list);
			return new indirectReferenceData(Decompile(0), newLeftVal, index);
		} else {
			return null;
		}
	}

	private static cfData evaluateRHS( CFContext _context, CFExpression _right )
	    throws cfmRunTimeException {
		cfData rightVal = _right.Eval(_context);
		if ( rightVal.getDataType() == cfData.CFLDATA ) {
			if ( ((cfLData) rightVal).exists() ) {
				rightVal = ((cfLData) rightVal).Get(_context);
			} else {
				rightVal = null;
			}
		}
		return rightVal;
	}

	private String getImage( int _exprIndex ) {
		StringBuilder sb = new StringBuilder();
		sb.append( expressions.get(0).Decompile(0) );

		for (int i = 1; i < _exprIndex; i++) {
			if ( operators.get(i) ) {
				sb.append('.');
				sb.append( expressions.get(i).Decompile(0) );
			} else {
				sb.append('[');
				sb.append( expressions.get(i).Decompile(0) );
				sb.append(']');
			}
		}
		return sb.toString();
	}

	public String Decompile( int indent ) {
		return image.toString();
	}

}
