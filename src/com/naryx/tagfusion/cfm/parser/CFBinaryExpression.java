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

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNullData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;


public class CFBinaryExpression extends CFExpression implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	static private final int _ERR = 0;
	static private final int _NUM = 1;
	static private final int _STR = 2;
	static private final int _BOOL = 3;
	static private final int _REF = 4; // cfStructs, cfArrays
	static private final int _DATE = 5; // date ops

	// instance vars
	private int _kind;
	private CFExpression _left;
	private CFExpression _right;
	private String operatorImage;

	public CFBinaryExpression( Token t, CFExpression left, CFExpression right ) {
		super(t);
		_kind = t.getType();
		operatorImage = t.getText();
		if ( _kind == CFMLLexer.ANDOPERATOR ) {
			_kind = CFMLLexer.AND;
		} else if ( _kind == CFMLLexer.OROPERATOR ) {
			_kind = CFMLLexer.OR;
		} else if ( _kind == CFMLLexer.MODOPERATOR ) {
			_kind = CFMLLexer.MOD;
		}
		_left = left;
		_right = right;
	}

	public byte getType() {
		return CFExpression.BINARY;
	}

	public cfData Eval( CFContext context ) throws cfmRunTimeException {
		// note that boolean's are treated as numbers
		cfData leftVal = null;
		cfData rightVal = null;
		cfData val = null;

		double leftNum = 0.0;
		double rightNum = 0.0;
		double valNum = 0.0;

		long leftDate = 0;
		long rightDate = 0;
		long valDate = 0;

		String leftStr = "";
		String rightStr = "";
		String valStr = "";

		boolean leftBool = false;
		boolean rightBool = false;
		boolean valBool = false;

		int opTypes = _ERR;
		// req'd for result type. Req'd cos for example "str EQ str" returns a
		// boolean
		int valType = _ERR;

		setLineCol(context);
		leftVal = _left.Eval(context);
		setLineCol(context);

		if ( leftVal.getDataType() == cfData.CFLDATA ) {
			leftVal = ((cfLData) leftVal).Get(context);
		}

		if ( _kind != CFMLLexer.AND && _kind != CFMLLexer.OR ) {
			rightVal = _right.Eval(context);
			if ( rightVal.getDataType() == cfData.CFLDATA ) {
				rightVal = ((cfLData) rightVal).Get(context);
			}
		}

		// Convert the operands to appropriate types
		switch (_kind) {

		// numeric operations
		case CFMLLexer.STAR:
		case CFMLLexer.SLASH:
		case CFMLLexer.BSLASH:
		case CFMLLexer.MOD:
		case CFMLLexer.POWER:
		case CFMLLexer.PLUS:
		case CFMLLexer.MINUS:

			if ( leftVal.isNumberConvertible() && rightVal.isNumberConvertible() ) {
				// Both operands must be numbers
				leftNum = leftVal.getDouble();
				rightNum = rightVal.getDouble();
				opTypes = _NUM;
				valType = _NUM;
			} else if ( leftVal.isDateConvertible() && rightVal.isDateConvertible() ) {
				leftDate = leftVal.getDateLong();
				rightDate = rightVal.getDateLong();
				opTypes = _DATE;
				valType = _DATE;
			} else {
				throw new CFException(
				    "Can't perform this operation on these data types.", context);
			}
			break;

		// comparator operators
		case CFMLLexer.GT:
		case CFMLLexer.LT:
		case CFMLLexer.GTE:
		case CFMLLexer.GE:
		case CFMLLexer.LTE:
		case CFMLLexer.LE:
		case CFMLLexer.EQ:
		case CFMLLexer.IS:
		case CFMLLexer.NEQ:
			if ( leftVal.isNumberConvertible() && rightVal.isNumberConvertible() ) {
				leftNum = leftVal.getDouble();
				rightNum = rightVal.getDouble();
				opTypes = _NUM;
			} else if ( leftVal.isDateConvertible() && rightVal.isDateConvertible() ) {
				leftDate = leftVal.getDateLong();
				rightDate = rightVal.getDateLong();
				opTypes = _DATE;
			} else if ( isStringConvertible(leftVal) && isStringConvertible(rightVal) ) {
				// All simple data types are string convertible
				leftStr = leftVal.getString();
				rightStr = rightVal.getString();
				opTypes = _STR;
			} else {
				throw new CFException(
				    "Can't perform this operation on these data types.", context);
			}
			valType = _BOOL;
			break;

		case CFMLLexer.CONTAINS:
		case CFMLLexer.DOESNOTCONTAIN:
			if ( isStringConvertible(leftVal) && isStringConvertible(rightVal) ) {
				leftStr = leftVal.getString();
				rightStr = rightVal.getString();
				valType = _BOOL;
				opTypes = _STR;
			} else {
				throw new CFException(
				    "Can't perform this operation on these data types.", context);
			}
			break;

		case CFMLLexer.CONCAT:
			if ( isStringConvertible(leftVal) && isStringConvertible(rightVal) ) {
				leftStr = leftVal.getString();
				rightStr = rightVal.getString();
				valType = _STR;
				opTypes = _STR;
			} else {
				throw new CFException(
				    "Can't perform this operation on these data types.", context);
			}
			break;

		case CFMLLexer.AND:
		case CFMLLexer.OR:

			// don't want to touch the RHS until LHS is evaluated to true or not
			if ( leftVal.isBooleanConvertible() ) {
				// Both operands must be booleans
				leftBool = leftVal.getBoolean();
				opTypes = _BOOL;
				valType = _BOOL;
			} else {
				throw new CFException(
				    "Can't perform this operation on these data types.", context);
			}
			break;

		case CFMLLexer.XOR:
		case CFMLLexer.IMP:
		case CFMLLexer.EQV:

			if ( leftVal.isBooleanConvertible() && rightVal.isBooleanConvertible() ) {
				// Both operands must be booleans
				leftBool = leftVal.getBoolean();
				rightBool = rightVal.getBoolean();
				opTypes = _BOOL;
				valType = _BOOL;
			} else {
				throw new CFException(
				    "Can't perform this operation on these data types.", context);
			}
			break;

		default:
			break;
		}

		// finally perform the operation
		switch (_kind) {
		case CFMLLexer.AND:
			if ( leftBool ) {
				rightVal = _right.Eval(context);
				if ( rightVal.getDataType() == cfData.CFLDATA ) {
					rightVal = ((cfLData) rightVal).Get(context);
				}
				if ( rightVal.isBooleanConvertible() ) {
					rightBool = rightVal.getBoolean();
					valBool = rightBool; // && leftBool -- we know leftBool is true
				} else {
					throw new CFException(
					    "Can't perform this operation on these data types.", context);
				}
			} else {
				valBool = false;
			}
			break;

		case CFMLLexer.XOR:
			valBool = (leftBool | rightBool) && !(leftBool && rightBool);
			break;

		case CFMLLexer.OR:
			// only if the LHS evaluates to false should the RHS be evaluated
			if ( !leftBool ) {
				rightVal = _right.Eval(context);
				if ( rightVal.getDataType() == cfData.CFLDATA ) {
					rightVal = ((cfLData) rightVal).Get(context);
				}
				if ( rightVal.isBooleanConvertible() ) {
					rightBool = rightVal.getBoolean();
					valBool = rightBool; // we know leftBool is false
				} else {
					throw new CFException(
					    "Can't perform this operation on these data types.", context);
				}
			} else {
				valBool = true;
			}

			break;

		case CFMLLexer.IMP:
			valBool = !(leftBool && !(rightBool));
			break;

		case CFMLLexer.EQV:
			valBool = leftBool == rightBool;
			break;

		case CFMLLexer.CONCAT:
			valStr = leftStr + rightStr;
			break;

		case CFMLLexer.MINUS:
			if ( opTypes == _NUM ) {
				valNum = leftNum - rightNum;
			} else { // optype == _DATE
				valDate = leftDate - rightDate;
			}
			break;

		case CFMLLexer.STAR:
			valNum = leftNum * rightNum;
			break;

		case CFMLLexer.SLASH:
			valNum = leftNum / rightNum;
			break;

		case CFMLLexer.BSLASH:
			valNum = (int) (leftNum / (int) rightNum);
			break;

		case CFMLLexer.MOD:
			valNum = (int) leftNum % (int) rightNum;
			break;

		case CFMLLexer.POWER:
			valNum = Math.pow(leftNum, rightNum);
			break;

		case CFMLLexer.PLUS:
			if ( opTypes == _NUM ) {
				valNum = leftNum + rightNum;
			} else { // optype == _DATE
				valDate = leftDate + rightDate;
			}
			break;

		case CFMLLexer.GT:
			switch (opTypes) {
			case _NUM:
				valBool = leftNum > rightNum;
				break;
			case _STR:
				valBool = leftStr.compareTo(rightStr) > 0;
				break;
			case _DATE:
				valBool = leftDate > rightDate;
				break;
			default:
				throw new CFException(
				    "Internal error - invalid op types in >=. This should not happen.",
				    context);
			}
			break;

		case CFMLLexer.LT:
			switch (opTypes) {
			case _NUM:
				valBool = (leftNum < rightNum);
				break;
			case _STR:
				valBool = leftStr.compareTo(rightStr) < 0;
				break;
			case _DATE:
				valBool = leftDate < rightDate;
				break;
			default:
				throw new CFException(
				    "Internal error - invalid op types in >=. This should not happen.",
				    context);
			}
			break;

		case CFMLLexer.LE:
		case CFMLLexer.LTE:
			switch (opTypes) {
			case _NUM:
				valBool = (leftNum <= rightNum);
				break;
			case _STR:
				valBool = leftStr.compareTo(rightStr) <= 0;
				break;
			case _DATE:
				valBool = leftDate <= rightDate;
				break;
			default:
				throw new CFException(
				    "Internal error - invalid op types in >=. This should not happen.",
				    context);
			}
			break;

		case CFMLLexer.GE:
		case CFMLLexer.GTE:

			switch (opTypes) {
			case _NUM:
				valBool = (leftNum >= rightNum);
				break;
			case _STR:
				valBool = leftStr.compareTo(rightStr) >= 0;
				break;
			case _DATE:
				valBool = leftDate >= rightDate;
				break;
			default:
				throw new CFException(
				    "Internal error - invalid op types in >=. This should not happen.",
				    context);
			}
			break;

		case CFMLLexer.EQ:
			switch (opTypes) {
			case _NUM:
				valBool = (leftNum == rightNum);
				break;
			case _STR:
				valBool = leftStr.equalsIgnoreCase( rightStr );
				break;
			case _DATE:
				valBool = leftDate == rightDate;
				break;
			default:
				throw new CFException(
				    "Internal error - invalid op types in ==. This should not happen.",
				    context);
			}
			break;

		case CFMLLexer.NEQ:
			switch (opTypes) {
			case _NUM:
				valBool = (leftNum != rightNum);
				break;
			case _STR:
				valBool = !leftStr.equalsIgnoreCase( rightStr );
				break;
			case _DATE:
				valBool = leftDate != rightDate;
				break;
			default:
				throw new CFException(
				    "Internal error - invalid op types in !=. This should not happen.",
				    context);
			}
			break;

		case CFMLLexer.CONTAINS:
			valBool = leftStr.toLowerCase().indexOf(rightStr.toLowerCase()) != -1;
			break;

		case CFMLLexer.DOESNOTCONTAIN:
			valBool = leftStr.toLowerCase().indexOf(rightStr.toLowerCase()) == -1;
			break;

		default:
			throw new CFException("Unknown binary operator (" + String.valueOf(_kind)
			    + ").", context);
		}

		// Construct the expression value
		switch (valType) {
		case _NUM:
			val = new cfNumberData(valNum);
			break;

		case _STR:
			val = new cfStringData(valStr);
			break;
		case _BOOL:
			val = cfBooleanData.getcfBooleanData(valBool);
			break;
		case _DATE:
			val = new cfDateData(valDate);
			break;
		case _REF:
			break;
		default:
			throw new CFException(
			    valType
			        + "  Internal error - invalid result type in binary expression. This should not happen."
			        + "when Evaluating [" + _left.Decompile(0) + "] [" + _kind
			        + "] [" + _right.Decompile(0) + "]", context);
		}

		return context._lastExpr = val;
	}

	public static cfData evaluate( CFContext _context, int _op, cfData _left,
	    cfData _right ) throws CFException, dataNotSupportedException {
		double leftNum = 0.0;
		double rightNum = 0.0;
		double valNum = 0.0;

		long leftDate = 0;
		long rightDate = 0;
		long valDate = 0;

		String leftStr = "";
		String rightStr = "";
		String valStr = "";

		boolean valBool = false;

		int opTypes = _ERR;
		// req'd for result type. Req'd cos for example "str EQ str" returns a
		// boolean
		int valType = _ERR;

		switch (_op) {

		// numeric operations
		case CFMLLexer.STAR:
		case CFMLLexer.SLASH:
		case CFMLLexer.MOD:
		case CFMLLexer.POWER:
		case CFMLLexer.PLUS:
		case CFMLLexer.MINUS:
			if ( _left.isNumberConvertible() && _right.isNumberConvertible() ) {
				// Both operands must be numbers
				leftNum = _left.getDouble();
				rightNum = _right.getDouble();
				opTypes = _NUM;
				valType = _NUM;
			} else if ( _left.isDateConvertible() && _right.isDateConvertible() ) {
				leftDate = _left.getDateLong();
				rightDate = _right.getDateLong();
				opTypes = _DATE;
				valType = _DATE;
			} else {
				throw new CFException(
				    "Can't perform this operation on these data types.", _context);
			}
			break;

		case CFMLLexer.CONCAT:
			if ( isStringConvertible(_left) && isStringConvertible(_right) ) {
				leftStr = _left.getString();
				rightStr = _right.getString();
				valType = _STR;
				opTypes = _STR;
			} else {
				throw new CFException(
				    "Can't perform this operation on these data types.", _context);
			}
			break;

		default:
			break;
		}

		// finally perform the operation
		switch (_op) {
		case CFMLLexer.CONCAT:
			valStr = leftStr + rightStr;
			break;

		case CFMLLexer.MINUS:
			if ( opTypes == _NUM ) {
				valNum = leftNum - rightNum;
			} else { // optype == _DATE
				valDate = leftDate - rightDate;
			}
			break;

		case CFMLLexer.STAR:
			valNum = leftNum * rightNum;
			break;

		case CFMLLexer.SLASH:
			valNum = leftNum / rightNum;
			break;

		case CFMLLexer.BSLASH:
			valNum = (int) (leftNum / (int) rightNum);
			break;

		case CFMLLexer.MOD:
			valNum = (int) leftNum % (int) rightNum;
			break;

		case CFMLLexer.POWER:
			valNum = Math.pow(leftNum, rightNum);
			break;

		case CFMLLexer.PLUS:
			if ( opTypes == _NUM ) {
				valNum = leftNum + rightNum;
			} else { // optype == _DATE
				valDate = leftDate + rightDate;
			}
			break;

		default:
			throw new CFException("Unknown binary operator (" + String.valueOf(_op)
			    + ").", _context);
		}

		cfData val = cfNullData.NULL;
		// Construct the expression value
		switch (valType) {
		case _NUM:
			val = new cfNumberData(valNum);
			break;

		case _STR:
			val = new cfStringData(valStr);
			break;
		case _BOOL:
			val = cfBooleanData.getcfBooleanData(valBool);
			break;
		case _DATE:
			val = new cfDateData(valDate);
			break;
		case _REF:
			break;
		}

		return val;
	}

	public String Decompile( int indent ) {
		String endChar = "";
		if ( _kind == CFMLLexer.LEFTBRACKET ) {
			endChar = "]";
		}
		return "" + _left.Decompile(indent) + operatorImage
		    + _right.Decompile(indent) + endChar;
	}

	private static boolean isStringConvertible( cfData _val ) {
		return cfData.isSimpleValue(_val)
		    || _val.getDataType() == cfData.CFJAVAOBJECTDATA;
	}

}

