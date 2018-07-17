/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: registerTagsExpressions.java 2480 2015-01-19 19:09:07Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

/**
 * This class is run once at start up and is used to register all the tags that the engine will use.
 * When adding new expressions/functions/tags add them to this file.
 */

import java.util.Map;

public class registerTagsExpressions extends java.lang.Object {

  public static void registerFunctions( Map<String, String> functions ){

  	functions.put( "newarray", "com.naryx.tagfusion.expression.function.newArray" );
    functions.put( "cos", "com.naryx.tagfusion.expression.function.cos" );
    functions.put( "tan", "com.naryx.tagfusion.expression.function.tan" );
    functions.put( "acos", "com.naryx.tagfusion.expression.function.acos" );
    functions.put( "asin", "com.naryx.tagfusion.expression.function.asin" );
    functions.put( "sin", "com.naryx.tagfusion.expression.function.sin" );
    functions.put( "pi", "com.naryx.tagfusion.expression.function.pi" );
    functions.put( "atn", "com.naryx.tagfusion.expression.function.atn" );
    functions.put( "bitand", "com.naryx.tagfusion.expression.function.bit.bitWiseAnd" );
    functions.put( "bitor", "com.naryx.tagfusion.expression.function.bit.bitWiseOr" );
    functions.put( "bitxor", "com.naryx.tagfusion.expression.function.bit.bitWiseXOr" );
    functions.put( "bitnot", "com.naryx.tagfusion.expression.function.bit.bitWiseNot" );
    functions.put( "ceiling", "com.naryx.tagfusion.expression.function.ceiling" );
    functions.put( "decrementvalue", "com.naryx.tagfusion.expression.function.decrementValue" );
    functions.put( "incrementvalue", "com.naryx.tagfusion.expression.function.incrementValue" );
    functions.put( "exp", "com.naryx.tagfusion.expression.function.exp" );
    functions.put( "int", "com.naryx.tagfusion.expression.function.Int" );
    functions.put( "log", "com.naryx.tagfusion.expression.function.log" );
    functions.put( "log10", "com.naryx.tagfusion.expression.function.log10" );
    functions.put( "max", "com.naryx.tagfusion.expression.function.max" );
    functions.put( "min", "com.naryx.tagfusion.expression.function.min" );
    functions.put( "random", "com.naryx.tagfusion.expression.function.random" );
    functions.put( "round", "com.naryx.tagfusion.expression.function.round" );
    functions.put( "sqr", "com.naryx.tagfusion.expression.function.squareRoot" );
    functions.put( "abs", "com.naryx.tagfusion.expression.function.abs" );
    functions.put( "sgn", "com.naryx.tagfusion.expression.function.sign" );
    functions.put( "rand", "com.naryx.tagfusion.expression.function.random" );
    functions.put( "randrange", "com.naryx.tagfusion.expression.function.randomRange" );
    functions.put( "randomize", "com.naryx.tagfusion.expression.function.randomize" );
    functions.put( "bitshln", "com.naryx.tagfusion.expression.function.bit.bitShiftLeft" );
    functions.put( "bitshrn", "com.naryx.tagfusion.expression.function.bit.bitShiftRight" );
    functions.put( "bitmaskclear", "com.naryx.tagfusion.expression.function.bit.bitMaskClear" );
    functions.put( "bitmaskread", "com.naryx.tagfusion.expression.function.bit.bitMaskRead" );
    functions.put( "bitmaskset", "com.naryx.tagfusion.expression.function.bit.bitMaskSet" );
    functions.put( "inputbasen", "com.naryx.tagfusion.expression.function.inputBaseN" );
    functions.put( "formatbasen", "com.naryx.tagfusion.expression.function.formatBaseN" );
    functions.put( "isdefined", "com.naryx.tagfusion.expression.function.isDefined" );

    functions.put( "arrayappend",	 		"com.naryx.tagfusion.expression.function.array.arrayAppend" );
    functions.put( "arrayprepend", 		"com.naryx.tagfusion.expression.function.array.arrayPrepend" );
    functions.put( "arraylen", 				"com.naryx.tagfusion.expression.function.array.arrayLength" );
    functions.put( "arrayclear",		 	"com.naryx.tagfusion.expression.function.array.arrayClear" );
    functions.put( "arraydeleteat", 	"com.naryx.tagfusion.expression.function.array.arrayDeleteAt" );
    functions.put( "arrayinsertat", 	"com.naryx.tagfusion.expression.function.array.arrayInsertAt" );
    functions.put( "arrayisempty", 		"com.naryx.tagfusion.expression.function.array.arrayIsEmpty" );
    functions.put( "arrayappend", 		"com.naryx.tagfusion.expression.function.array.arrayAppend" );
    functions.put( "arraynew", 				"com.naryx.tagfusion.expression.function.newArray" );
    functions.put( "arrayavg", 				"com.naryx.tagfusion.expression.function.array.arrayAverage" );
    functions.put( "arraysum", 				"com.naryx.tagfusion.expression.function.array.arraySum" );
    functions.put( "arrayresize", 		"com.naryx.tagfusion.expression.function.array.arrayResize" );
    functions.put( "arrayset", 				"com.naryx.tagfusion.expression.function.array.arraySet" );
    functions.put( "arrayswap", 			"com.naryx.tagfusion.expression.function.array.arraySwap" );
    functions.put( "arraymax", 				"com.naryx.tagfusion.expression.function.array.arrayMax" );
    functions.put( "arraymin", 				"com.naryx.tagfusion.expression.function.array.arrayMin" );
    functions.put( "arraysort",			 	"com.naryx.tagfusion.expression.function.array.arraySort" );
    functions.put( "arraytolist", 		"com.naryx.tagfusion.expression.function.array.arrayToList" );
    functions.put( "arraytrimvalue", 	"com.naryx.tagfusion.expression.function.array.ArrayTrimValue" );
    functions.put( "arrayeach", 			"com.naryx.tagfusion.expression.function.array.arrayEach" );
    functions.put( "arrayfilter",			"com.naryx.tagfusion.expression.function.array.arrayFilter" );
    functions.put( "arrayfindall",		"com.naryx.tagfusion.expression.function.array.arrayFindAll" );

    functions.put( "listtoarray", 			"com.naryx.tagfusion.expression.function.list.listToArray" );
    functions.put( "isarray", 					"com.naryx.tagfusion.expression.function.isArray" );
    functions.put( "isbinary", 					"com.naryx.tagfusion.expression.function.isBinary" );
    functions.put( "isboolean", 				"com.naryx.tagfusion.expression.function.isBoolean" );
    functions.put( "isnumeric", 				"com.naryx.tagfusion.expression.function.isNumeric" );
    functions.put( "isquery", 					"com.naryx.tagfusion.expression.function.isQuery" );
    functions.put( "issimplevalue", 		"com.naryx.tagfusion.expression.function.isSimpleValue" );
    functions.put( "isnull", 						"com.naryx.tagfusion.expression.function.isNull" );
    functions.put( "tostring", 					"com.naryx.tagfusion.expression.function.toString" );
    functions.put( "toboolean", 				"com.naryx.tagfusion.expression.function.toBoolean" );
    functions.put( "isdebugmode", 			"com.naryx.tagfusion.expression.function.isDebugMode" );
    functions.put( "parameterexists", 	"com.naryx.tagfusion.expression.function.parameterExists" );


    functions.put( "asc", 						"com.naryx.tagfusion.expression.function.string.asc" );
    functions.put( "chr", 						"com.naryx.tagfusion.expression.function.string.chr" );
    functions.put( "cjustify", 				"com.naryx.tagfusion.expression.function.string.cJustify" );
    functions.put( "compare", 				"com.naryx.tagfusion.expression.function.string.compare" );
    functions.put( "comparenocase", 	"com.naryx.tagfusion.expression.function.string.compareNoCase" );
    functions.put( "dayofweekasstring", "com.naryx.tagfusion.expression.function.dayOfWeekAsString" );
    functions.put( "monthasstring", 	"com.naryx.tagfusion.expression.function.monthAsString" );
    functions.put( "formatbasen", 		"com.naryx.tagfusion.expression.function.formatBaseN" );
    functions.put( "find", 						"com.naryx.tagfusion.expression.function.find" );
    functions.put( "findnocase", 			"com.naryx.tagfusion.expression.function.findNoCase" );
    functions.put( "findoneof", 			"com.naryx.tagfusion.expression.function.findOneOf" );
    functions.put( "gettoken", 				"com.naryx.tagfusion.expression.function.getToken" );
    functions.put( "insert", 					"com.naryx.tagfusion.expression.function.insert" );
    functions.put( "left", 						"com.naryx.tagfusion.expression.function.string.left" );
    functions.put( "len", 						"com.naryx.tagfusion.expression.function.string.len" );
    functions.put( "ljustify", 				"com.naryx.tagfusion.expression.function.string.lJustify" );
    functions.put( "lsparsenumber", 	"com.naryx.tagfusion.expression.function.lsParseNumber" );
    functions.put( "ltrim", 					"com.naryx.tagfusion.expression.function.string.lTrim" );
    functions.put( "mid", 						"com.naryx.tagfusion.expression.function.string.mid" );
    functions.put( "removechars", 		"com.naryx.tagfusion.expression.function.string.removeChars" );
    functions.put( "repeatstring", 		"com.naryx.tagfusion.expression.function.string.repeatString" );
    functions.put( "replace", 				"com.naryx.tagfusion.expression.function.string.replace" );
    functions.put( "replacenocase", 	"com.naryx.tagfusion.expression.function.string.replaceNoCase" );
    functions.put( "reverse", 				"com.naryx.tagfusion.expression.function.string.reverse" );
    functions.put( "right", 					"com.naryx.tagfusion.expression.function.string.right" );
    functions.put( "rjustify", 				"com.naryx.tagfusion.expression.function.string.rJustify" );
    functions.put( "rtrim", 					"com.naryx.tagfusion.expression.function.string.rTrim" );
    functions.put( "spanexcluding", 	"com.naryx.tagfusion.expression.function.string.spanExcluding" );
		functions.put( "spanincluding", 	"com.naryx.tagfusion.expression.function.string.spanIncluding" );
    functions.put( "ucase", 					"com.naryx.tagfusion.expression.function.string.uCase" );
    functions.put( "replacelist", 		"com.naryx.tagfusion.expression.function.string.replaceList" );
    functions.put( "val", 						"com.naryx.tagfusion.expression.function.val" );

    functions.put( "isstruct", 				"com.naryx.tagfusion.expression.function.isStruct" );
    functions.put( "structclear", 		"com.naryx.tagfusion.expression.function.struct.structClear" );
    functions.put( "structcount", 		"com.naryx.tagfusion.expression.function.struct.structCount" );
    functions.put( "structdelete", 		"com.naryx.tagfusion.expression.function.struct.structDelete" );
    functions.put( "structinsert", 		"com.naryx.tagfusion.expression.function.struct.structInsert" );
    functions.put( "structfind", 			"com.naryx.tagfusion.expression.function.struct.structFind" );
    functions.put( "structisempty", 	"com.naryx.tagfusion.expression.function.struct.structIsEmpty" );
    functions.put( "structkeyarray", 	"com.naryx.tagfusion.expression.function.struct.structKeyArray" );
    functions.put( "structkeyexists", "com.naryx.tagfusion.expression.function.struct.structKeyExists" );
    functions.put( "structkeylist", 	"com.naryx.tagfusion.expression.function.struct.structKeyList" );
    functions.put( "structnew", 			"com.naryx.tagfusion.expression.function.struct.structNew" );
    functions.put( "structupdate", 		"com.naryx.tagfusion.expression.function.struct.structUpdate" );
    functions.put( "structcopy", 			"com.naryx.tagfusion.expression.function.struct.structCopy" );
    functions.put( "structequals", 		"com.naryx.tagfusion.expression.function.struct.structEquals" );
    functions.put( "structeach", 			"com.naryx.tagfusion.expression.function.struct.structEach" );
    functions.put( "structfilter", 		"com.naryx.tagfusion.expression.function.struct.structFilter" );


    functions.put( "listappend", 					"com.naryx.tagfusion.expression.function.list.listAppend" );
    functions.put( "listchangedelims", 		"com.naryx.tagfusion.expression.function.list.listChangeDelims" );
    functions.put( "listcontains", 				"com.naryx.tagfusion.expression.function.list.listContains" );
    functions.put( "listcontainsnocase", 	"com.naryx.tagfusion.expression.function.list.listContainsNoCase" );
    functions.put( "listdeleteat", 				"com.naryx.tagfusion.expression.function.list.listDeleteAt" );
    functions.put( "listfind", 						"com.naryx.tagfusion.expression.function.list.listFind" );
    functions.put( "listfindnocase", 			"com.naryx.tagfusion.expression.function.list.listFindNoCase" );
    functions.put( "listfirst", 					"com.naryx.tagfusion.expression.function.list.listFirst" );
    functions.put( "listgetat", 					"com.naryx.tagfusion.expression.function.list.listGetAt" );
    functions.put( "listinsertat", 				"com.naryx.tagfusion.expression.function.list.listInsertAt" );
    functions.put( "listlast", 						"com.naryx.tagfusion.expression.function.list.listLast" );
    functions.put( "listlen", 						"com.naryx.tagfusion.expression.function.list.listLen" );
    functions.put( "listprepend", 				"com.naryx.tagfusion.expression.function.list.listPrepend" );
    functions.put( "listqualify", 				"com.naryx.tagfusion.expression.function.list.listQualify" );
    functions.put( "listrest", 						"com.naryx.tagfusion.expression.function.list.listRest" );
    functions.put( "listsetat", 					"com.naryx.tagfusion.expression.function.list.listSetAt" );
    functions.put( "listsort", 						"com.naryx.tagfusion.expression.function.list.listSort" );
    functions.put( "listvaluecount", 			"com.naryx.tagfusion.expression.function.list.listValueCount" );
    functions.put( "listvaluecountnocase", "com.naryx.tagfusion.expression.function.list.listValueCountNoCase" );

    functions.put( "expandpath", 						"com.naryx.tagfusion.expression.function.expandPath" );

    functions.put( "getclientvariableslist", "com.naryx.tagfusion.expression.function.getClientVariablesList" );
    functions.put( "createuuid", "com.naryx.tagfusion.expression.function.createUUID" );
    functions.put( "decimalformat", "com.naryx.tagfusion.expression.function.decimalFormat" );
    functions.put( "dollarformat", "com.naryx.tagfusion.expression.function.dollarFormat" );
    functions.put( "yesnoformat", "com.naryx.tagfusion.expression.function.yesNoFormat" );
    functions.put( "urldecode", "com.naryx.tagfusion.expression.function.urlDecode" );
    functions.put( "trim", 						"com.naryx.tagfusion.expression.function.string.trim" );
    functions.put( "setvariable", 		"com.naryx.tagfusion.expression.function.setVariable" );
    functions.put( "paragraphformat", "com.naryx.tagfusion.expression.function.string.paragraphFormat" );
    functions.put( "lcase", 					"com.naryx.tagfusion.expression.function.string.lCase" );
    functions.put( "encrypt", 				"com.naryx.tagfusion.expression.function.encrypt" );
    functions.put( "decrypt", 				"com.naryx.tagfusion.expression.function.decrypt" );
    functions.put( "gettickcount", 		"com.naryx.tagfusion.expression.function.getTickCount" );
    functions.put( "deleteclientvariable", "com.naryx.tagfusion.expression.function.deleteClientVariable" );
    functions.put( "urlencodedformat", "com.naryx.tagfusion.expression.function.urlEncodedFormat" );
    functions.put( "fixeol", 					"com.naryx.tagfusion.expression.function.string.fixEOL" );

    functions.put( "createdate", "com.naryx.tagfusion.expression.function.createDate" );
    functions.put( "createdatetime", "com.naryx.tagfusion.expression.function.createDateTime" );
    functions.put( "createodbcdate", "com.naryx.tagfusion.expression.function.createODBCDate" );
    functions.put( "createodbcdatetime", "com.naryx.tagfusion.expression.function.createODBCDateTime" );
    functions.put( "createodbctime", "com.naryx.tagfusion.expression.function.createODBCTime" );
    functions.put( "createtime", "com.naryx.tagfusion.expression.function.createTime" );
    functions.put( "createtimespan", "com.naryx.tagfusion.expression.function.createTimeSpan" );

    functions.put( "dateadd", 		"com.naryx.tagfusion.expression.function.dateAdd" );
    functions.put( "datecompare", "com.naryx.tagfusion.expression.function.dateCompare" );
    functions.put( "dateconvert", "com.naryx.tagfusion.expression.function.dateConvert" );
    functions.put( "datediff", 		"com.naryx.tagfusion.expression.function.dateDiff" );
    functions.put( "dateformat", 	"com.naryx.tagfusion.expression.function.dateFormat" );
    functions.put( "datepart", 		"com.naryx.tagfusion.expression.function.datePart" );

    functions.put( "day", "com.naryx.tagfusion.expression.function.day" );
    functions.put( "dayofweek", "com.naryx.tagfusion.expression.function.dayOfWeek" );
    functions.put( "dayofyear", "com.naryx.tagfusion.expression.function.dayOfYear" );
    functions.put( "daysinmonth", "com.naryx.tagfusion.expression.function.daysInMonth" );
    functions.put( "daysinyear", "com.naryx.tagfusion.expression.function.daysInYear" );
    functions.put( "firstdayofmonth", "com.naryx.tagfusion.expression.function.firstDayOfMonth" );
    functions.put( "hour", "com.naryx.tagfusion.expression.function.hour" );
    functions.put( "isleapyear", "com.naryx.tagfusion.expression.function.isLeapYear" );
    functions.put( "minute", "com.naryx.tagfusion.expression.function.minute" );
    functions.put( "month", "com.naryx.tagfusion.expression.function.month" );
    functions.put( "now", "com.naryx.tagfusion.expression.function.now" );
    functions.put( "quarter", "com.naryx.tagfusion.expression.function.quarter" );
    functions.put( "second", "com.naryx.tagfusion.expression.function.second" );
    functions.put( "week", "com.naryx.tagfusion.expression.function.week" );
    functions.put( "isdate", "com.naryx.tagfusion.expression.function.isDate" );
    functions.put( "lsparsedatetime", "com.naryx.tagfusion.expression.function.lsParseDateTime" );

    functions.put( "parsedatetime", 	"com.naryx.tagfusion.expression.function.ParseDateTime" );
    functions.put( "timeformat", 			"com.naryx.tagfusion.expression.function.timeFormat" );
    functions.put( "year", 						"com.naryx.tagfusion.expression.function.year" );
    functions.put( "fix", 						"com.naryx.tagfusion.expression.function.fix" );
    functions.put( "jsstringformat", 	"com.naryx.tagfusion.expression.function.string.jsStringFormat" );
    functions.put( "htmlcodeformat", 	"com.naryx.tagfusion.expression.function.string.htmlCodeFormat" );
    functions.put( "htmleditformat", 	"com.naryx.tagfusion.expression.function.string.htmlEditFormat" );
    functions.put( "numberformat", 		"com.naryx.tagfusion.expression.function.numberFormat" );
    functions.put( "gettempdirectory","com.naryx.tagfusion.expression.function.getTempDirectory" );
    functions.put( "gettempfile",			"com.naryx.tagfusion.expression.function.getTempFile" );
    functions.put( "getprofilesections",	"com.naryx.tagfusion.expression.function.getProfileSections" );
    functions.put( "getprofilestring","com.naryx.tagfusion.expression.function.getProfileString" );
    functions.put( "setprofilestring","com.naryx.tagfusion.expression.function.setProfileString" );
    functions.put( "getvariable",			"com.naryx.tagfusion.expression.function.getVariable" );

    functions.put( "evaluate",				"com.naryx.tagfusion.expression.function.evaluate" );
    functions.put( "de",							"com.naryx.tagfusion.expression.function.de" );
    functions.put( "iif",							"com.naryx.tagfusion.expression.function.iif" );
    functions.put( "preservesinglequotes",	"com.naryx.tagfusion.expression.function.preserveSingleQuotes" );
    functions.put( "writeoutput",			"com.naryx.tagfusion.expression.function.writeOutput" );

    functions.put( "queryaddcolumn",	"com.naryx.tagfusion.expression.function.query.queryAddColumn" );
    functions.put( "queryaddrow",			"com.naryx.tagfusion.expression.function.query.queryAddRow" );
    functions.put( "querynew",				"com.naryx.tagfusion.expression.function.query.queryNew" );
    functions.put( "querysetcell",		"com.naryx.tagfusion.expression.function.query.querySetCell" );

    functions.put( "valuelist",				"com.naryx.tagfusion.expression.function.query.valueList" );
    functions.put( "quotedvaluelist",	"com.naryx.tagfusion.expression.function.query.quotedValueList" );

    functions.put( "getbasetaglist",	"com.naryx.tagfusion.expression.function.getBaseTagList" );
    functions.put( "getbasetagdata",	"com.naryx.tagfusion.expression.function.getBaseTagData" );
    functions.put( "charat",	        "com.naryx.tagfusion.expression.function.string.charat" );

    functions.put( "tobase64",	           "com.naryx.tagfusion.expression.function.toBase64" );
    functions.put( "tobinary",	           "com.naryx.tagfusion.expression.function.toBinary" );
    functions.put( "getfunctionlist",	     "com.naryx.tagfusion.expression.function.getFunctionList" );
    functions.put( "gettimezoneinfo",	     "com.naryx.tagfusion.expression.function.getTimeZoneInfo" );
    functions.put( "hash",	               "com.naryx.tagfusion.expression.function.hash" );

    functions.put( "structappend",	       "com.naryx.tagfusion.expression.function.struct.structAppend" );
    functions.put( "structfindkey",	       "com.naryx.tagfusion.expression.function.struct.structFindKey" );
    functions.put( "getcurrenttemplatepath","com.naryx.tagfusion.expression.function.getCurrentTemplatePath" );
    functions.put( "gethttptimestring",     "com.naryx.tagfusion.expression.function.getHttpTimeString" );
    functions.put( "getbasetemplatepath",   "com.naryx.tagfusion.expression.function.getBaseTemplatePath" );
	  functions.put( "iscustomfunction",      "com.naryx.tagfusion.expression.function.isCustomFunction" );
    functions.put( "getbasetemplatepath",   "com.naryx.tagfusion.expression.function.getBaseTemplatePath" );

		functions.put( "refind",	              "com.naryx.tagfusion.expression.function.string.reFind" );
    functions.put( "refindnocase",	        "com.naryx.tagfusion.expression.function.string.reFindNoCase" );
    functions.put( "rereplace",	            "com.naryx.tagfusion.expression.function.string.reReplace" );
    functions.put( "rereplacenocase",	      "com.naryx.tagfusion.expression.function.string.reReplaceNoCase" );
    functions.put( "duplicate",	            "com.naryx.tagfusion.expression.function.duplicate" );
    functions.put( "gettemplatepath",	      "com.naryx.tagfusion.expression.function.getBaseTemplatePath" );

		functions.put( "urlsessionformat",			"com.naryx.tagfusion.expression.function.urlsessionformat" );
    functions.put( "setencoding",           "com.naryx.tagfusion.expression.function.setEncoding" );
    functions.put( "getencoding",           "com.naryx.tagfusion.expression.function.getEncoding" );

	  functions.put( "getpagecontext", "com.naryx.tagfusion.expression.function.GetPageContext" );
	  functions.put( "assert", "com.naryx.tagfusion.expression.function.assertFunction" );

		functions.put( "xmlparse", "com.naryx.tagfusion.expression.function.xml.XmlParse" );
		functions.put( "xmlvalidate", "com.naryx.tagfusion.expression.function.xml.XmlValidate" );

		functions.put( "xmlnew", "com.naryx.tagfusion.expression.function.xml.XmlNew" );
		functions.put( "xmlelemnew", "com.naryx.tagfusion.expression.function.xml.XmlElemNew" );
		functions.put( "xmlformat", "com.naryx.tagfusion.expression.function.xml.xmlFormat" );
		functions.put( "isxmlroot", "com.naryx.tagfusion.expression.function.xml.IsXmlRoot" );
		functions.put( "isxml", "com.naryx.tagfusion.expression.function.xml.IsXml" );
		functions.put( "isxmlattribute", "com.naryx.tagfusion.expression.function.xml.IsXmlAttribute" );
		functions.put( "xmlgetnodetype", "com.naryx.tagfusion.expression.function.xml.XmlGetNodeType" );
		functions.put( "isxmlnode", "com.naryx.tagfusion.expression.function.xml.IsXmlNode" );
		functions.put( "isxmlelem", "com.naryx.tagfusion.expression.function.xml.IsXmlElement" );
		functions.put( "isxmlelement", "com.naryx.tagfusion.expression.function.xml.IsXmlElement" );
		functions.put( "isxmldoc", "com.naryx.tagfusion.expression.function.xml.IsXmlDoc" );
		functions.put( "xmlchildpos", "com.naryx.tagfusion.expression.function.xml.XmlChildPos" );
		functions.put( "xmltransform", "com.naryx.tagfusion.expression.function.xml.XmlTransform" );

		functions.put( "addsoaprequestheader", "com.naryx.tagfusion.expression.function.addSOAPRequestHeader" );
		functions.put( "addsoapresponseheader", "com.naryx.tagfusion.expression.function.addSOAPResponseHeader" );
		functions.put( "getsoaprequestheader", "com.naryx.tagfusion.expression.function.getSOAPRequestHeader" );
		functions.put( "getsoapresponseheader", "com.naryx.tagfusion.expression.function.getSOAPResponseHeader" );
		functions.put( "issoaprequest", "com.naryx.tagfusion.expression.function.isSOAPRequest" );
		functions.put( "getsoaprequest", "com.naryx.tagfusion.expression.function.getSOAPRequest" );
		functions.put( "getsoapresponse", "com.naryx.tagfusion.expression.function.getSOAPResponse" );

		// added in CFMX 7
		functions.put( "getcontextroot", "com.naryx.tagfusion.expression.function.getContextRoot" );
		functions.put( "binaryencode", "com.naryx.tagfusion.expression.function.BinaryEncode" );
		functions.put( "binarydecode", "com.naryx.tagfusion.expression.function.BinaryDecode" );
		functions.put( "charsetencode", "com.naryx.tagfusion.expression.function.CharsetEncode" );
		functions.put( "charsetdecode", "com.naryx.tagfusion.expression.function.CharsetDecode" );
		functions.put( "getlocaledisplayname", "com.naryx.tagfusion.expression.function.ls.GetLocaleDisplayName" );
		functions.put( "generatesecretkey", "com.naryx.tagfusion.expression.function.GenerateSecretKey" );
		functions.put( "encryptbinary", "com.naryx.tagfusion.expression.function.encryptBinary" );
		functions.put( "decryptbinary", "com.naryx.tagfusion.expression.function.decryptBinary" );
		functions.put( "toscript", "com.naryx.tagfusion.expression.function.ToScript" );
		functions.put( "isvalid", "com.naryx.tagfusion.expression.function.isValid" );
		functions.put( "islocalhost", "com.naryx.tagfusion.expression.function.IsLocalHost" );
		functions.put( "getlocalhostip", "com.naryx.tagfusion.expression.function.GetLocalHostIP" );

		functions.put( "wrap", "com.naryx.tagfusion.expression.function.wrap");
		functions.put( "gethttprequestdata", "com.naryx.tagfusion.expression.function.getHttpRequestData" );

		functions.put("getmetadata", "com.naryx.tagfusion.expression.function.getMetaData");

		functions.put( "isobject", "com.naryx.tagfusion.expression.function.isObject" );
		functions.put( "iswddx", "com.naryx.tagfusion.expression.function.isWDDX" );
		functions.put( "javacast", "com.naryx.tagfusion.expression.function.javaCast" );

		functions.put( "getauthuser", "com.naryx.tagfusion.expression.function.getAuthUser" );
		functions.put( "isuserinrole", "com.naryx.tagfusion.expression.function.isUserInRole" );
		functions.put( "isuserloggedin","com.marcusfernstrom.isUserLoggedIn.isUserLoggedIn");
		
		functions.put( "jwtcreate", "com.marcusfernstrom.jwt.JwtCreate");
		functions.put( "jwtverify", "com.marcusfernstrom.jwt.JwtVerify");
		functions.put( "jwtdecode", "com.marcusfernstrom.jwt.JwtDecode");
		
		functions.put( "getnumericdate", "com.naryx.tagfusion.expression.function.getNumericDate" );

		//[- added in CF8
		functions.put( "flush", "com.naryx.tagfusion.expression.function.Flush" );
		functions.put( "sleep", "com.naryx.tagfusion.expression.function.Sleep" );

    functions.put( "lsparsedatetime",      "com.naryx.tagfusion.expression.function.ls.LSParseDateTime" );
    functions.put( "lsparseeurocurrency",   "com.naryx.tagfusion.expression.function.ls.LSParseEuroCurrency" );

    functions.put( "isnumericdate",	        "com.naryx.tagfusion.expression.function.isNumericDate" );
    functions.put( "structfindvalue",	      "com.naryx.tagfusion.expression.function.struct.structFindValue" );
    functions.put( "structsort",	          "com.naryx.tagfusion.expression.function.struct.structSort" );
    functions.put( "structget",             "com.naryx.tagfusion.expression.function.struct.structGet" );

    functions.put( "setlocale",            "com.naryx.tagfusion.expression.function.ls.setLocale" );
    functions.put( "getlocale",            "com.naryx.tagfusion.expression.function.ls.getLocale" );
    functions.put( "lscurrencyformat",     "com.naryx.tagfusion.expression.function.ls.LSCurrencyFormat" );
    functions.put( "lseurocurrencyformat", "com.naryx.tagfusion.expression.function.ls.LSEuroCurrencyFormat" );
    functions.put( "lsdateformat",         "com.naryx.tagfusion.expression.function.ls.LSDateFormat" );
    functions.put( "lsiscurrency",         "com.naryx.tagfusion.expression.function.ls.LSIsCurrency" );
    functions.put( "lsisdate",             "com.naryx.tagfusion.expression.function.ls.LSIsDate" );
    functions.put( "lstimeformat",         "com.naryx.tagfusion.expression.function.ls.LSTimeFormat" );
    functions.put( "lsparsecurrency",      "com.naryx.tagfusion.expression.function.ls.LSParseCurrency" );
    functions.put( "lsnumberformat",        "com.naryx.tagfusion.expression.function.ls.LSNumberFormat" );
   	functions.put( "lsisnumeric", 					"com.naryx.tagfusion.expression.function.ls.LSIsNumeric" );
    functions.put( "lsparsenumber",      		"com.naryx.tagfusion.expression.function.ls.LSParseNumber" );

   	//-- BlueDragon only functions
   	functions.put( "getbytessent",   				"com.naryx.tagfusion.expression.function.ext.getBytesSent" );
   	functions.put( "querysort",       			"com.naryx.tagfusion.expression.function.query.querySort" );
   	functions.put( "render",       					"com.naryx.tagfusion.expression.function.ext.render" );
   	functions.put( "querydeleterow",       	"com.naryx.tagfusion.expression.function.query.queryDeleteRow" );
		functions.put( "listremoveduplicates", 	"com.naryx.tagfusion.expression.function.list.listRemoveDuplicates" );

		functions.put( "threadisalive",	 			"com.naryx.tagfusion.expression.function.ext.ThreadIsAlive" );
		functions.put( "threadrunningtime", 	"com.naryx.tagfusion.expression.function.ext.ThreadRunningTime" );
		functions.put( "threadinterrupt", 		"com.naryx.tagfusion.expression.function.ext.ThreadInterrupt" );
		functions.put( "threadjoin", 					"com.naryx.tagfusion.expression.function.ext.ThreadJoin" );
		functions.put( "threadstop", 					"com.naryx.tagfusion.expression.function.ext.ThreadStop" );
		functions.put( "getallthreads", 			"com.naryx.tagfusion.expression.function.ext.GetAllThreads" );


		/* Cache functions */
		functions.put( "cacheget",   					"com.naryx.tagfusion.cfm.cache.functions.CacheGet" );
		functions.put( "cacheput",   					"com.naryx.tagfusion.cfm.cache.functions.CachePut" );
		functions.put( "cacheremove",					"com.naryx.tagfusion.cfm.cache.functions.CacheRemove" );
		functions.put( "cacheremoveall",			"com.naryx.tagfusion.cfm.cache.functions.CacheRemoveAll" );
		functions.put( "cacheidexists",				"com.naryx.tagfusion.cfm.cache.functions.CacheIDExists" );

		functions.put( "cacheregionnew", 			"com.naryx.tagfusion.cfm.cache.functions.CacheRegionNew" );
		functions.put( "cacheregionremove", 	"com.naryx.tagfusion.cfm.cache.functions.CacheRegionRemove" );
		functions.put( "cacheregionexists", 	"com.naryx.tagfusion.cfm.cache.functions.CacheRegionExists" );
		functions.put( "cacheregiongetall", 	"com.naryx.tagfusion.cfm.cache.functions.CacheRegionGetAll" );
		functions.put( "cachesetproperties", 	"com.naryx.tagfusion.cfm.cache.functions.CacheSetProperties" );
		functions.put( "cachegetproperties", 	"com.naryx.tagfusion.cfm.cache.functions.CacheGetProperties" );
		functions.put( "cachegetmetadata", 		"com.naryx.tagfusion.cfm.cache.functions.CacheGetMetaData" );

		/* FileXXX functions */
    functions.put("fileexists", 					"com.naryx.tagfusion.expression.function.file.FileExists" );
		functions.put("filedelete", 					"com.naryx.tagfusion.expression.function.file.FileDelete");
		functions.put("fileseparator",				"com.naryx.tagfusion.expression.function.file.FileSeparator");
		functions.put("getfileinfo", 					"com.naryx.tagfusion.expression.function.file.GetFileInfo");
    functions.put("directoryexists", 			"com.naryx.tagfusion.expression.function.file.directoryExists" );
		functions.put("filecopy", 						"com.naryx.tagfusion.expression.function.file.FileCopy");
		functions.put("directorycreate", 			"com.naryx.tagfusion.expression.function.file.DirectoryCreate");
		functions.put("directorydelete", 			"com.naryx.tagfusion.expression.function.file.DirectoryDelete");
		functions.put("filemove", 						"com.naryx.tagfusion.expression.function.file.FileMove");
		functions.put("fileopen",	 						"com.naryx.tagfusion.expression.function.file.FileOpen");
		functions.put("fileclose", 						"com.naryx.tagfusion.expression.function.file.FileClose");
		functions.put("filereadline", 				"com.naryx.tagfusion.expression.function.file.FileReadLine");
		functions.put("fileiseof", 						"com.naryx.tagfusion.expression.function.file.FileIsEOF");
		functions.put("filewriteline", 				"com.naryx.tagfusion.expression.function.file.FileWriteLine");
		functions.put("filewrite", 						"com.naryx.tagfusion.expression.function.file.FileWrite");
		functions.put("filereadbinary", 			"com.naryx.tagfusion.expression.function.file.FileReadBinary");
		functions.put("fileread", 						"com.naryx.tagfusion.expression.function.file.FileRead");
		functions.put("filesetlastmodified",	"com.naryx.tagfusion.expression.function.file.FileSetLastModified");
		functions.put("filesetattribute",			"com.naryx.tagfusion.expression.function.file.FileSetAttribute");
		functions.put("filesetaccessmode",		"com.naryx.tagfusion.expression.function.file.FileSetAccessMode");
		functions.put("directorylist", 				"com.naryx.tagfusion.expression.function.file.DirectoryList");
    functions.put("getdirectoryfrompath", "com.naryx.tagfusion.expression.function.file.getDirectoryFromPath" );
    functions.put("getfilefrompath", 			"com.naryx.tagfusion.expression.function.file.getFileFromPath" );
    functions.put("fileupload", 					"com.naryx.tagfusion.expression.function.file.FileUpload");
		functions.put("fileuploadall", 				"com.naryx.tagfusion.expression.function.file.FileUploadAll");


		functions.put("serializejson", 					"com.naryx.tagfusion.expression.function.string.serializejson");
		functions.put("deserializejson", 				"com.naryx.tagfusion.expression.function.string.DeserializeJSONJackson");
		functions.put("isjson", 								"com.naryx.tagfusion.expression.function.string.isjson");

		functions.put("systemreloadconfig",			"com.naryx.tagfusion.expression.function.ext.SystemReloadConfig");

		functions.put( "tobase62",           		"com.naryx.tagfusion.expression.function.toBase62" );
		functions.put( "frombase62",         		"com.naryx.tagfusion.expression.function.fromBase62" );

		functions.put( "htmlgetprintabletext",  "com.naryx.tagfusion.expression.function.ext.HtmlGetPrintableText" );
		functions.put( "arraytrim",  						"com.naryx.tagfusion.expression.function.array.ArrayTrim" );
		functions.put( "arrayisdefined", 				"com.naryx.tagfusion.expression.function.array.arrayIsDefined" );

		functions.put( "structvaluearray", 			"com.naryx.tagfusion.expression.function.struct.structValueArray" );

		/* HTML Routines */
		functions.put( "htmlsourceformat",						"com.naryx.tagfusion.expression.function.ext.HtmlSourceFormat");
		functions.put( "htmltidy",										"com.naryx.tagfusion.expression.function.ext.HtmlCleanUp");
		functions.put( "tohtml",											"com.naryx.tagfusion.expression.function.ext.ToHTML");
		functions.put( "location",										"com.naryx.tagfusion.expression.function.location");
		functions.put( "html",												"com.naryx.tagfusion.expression.function.string.jsoup.html");
		functions.put( "markdown",										"com.naryx.tagfusion.expression.function.string.markdown");

		functions.put( "getenginefunctioninfo",				"com.naryx.tagfusion.expression.function.ext.GetEngineFunctionInfo");
		functions.put( "getenginefunctioncategories",	"com.naryx.tagfusion.expression.function.ext.GetEngineFunctionCategories");

		functions.put( "getenginetaginfo",						"com.naryx.tagfusion.expression.function.ext.GetEngineTagInfo");
		functions.put( "getenginetagcategories",			"com.naryx.tagfusion.expression.function.ext.GetEngineTagCategories");

		functions.put( "getsupportedfunctions",				"com.naryx.tagfusion.expression.function.ext.getSupportedFunctions" );
		functions.put( "getsupportedtags",   					"com.naryx.tagfusion.expression.function.ext.getSupportedTags" );

		functions.put( "hashbinary",   								"com.naryx.tagfusion.expression.function.hashBinary" );
		functions.put( "arrayget",   									"com.naryx.tagfusion.expression.function.array.arrayGetAt" );

		functions.put( "arraycontains",								"com.naryx.tagfusion.expression.function.array.arrayContains" );
		functions.put( "arrayfind",										"com.naryx.tagfusion.expression.function.array.arrayFind" );
		functions.put( "arraycontainsnocase",					"com.naryx.tagfusion.expression.function.array.arrayContainsNoCase" );
		functions.put( "arrayfindnocase",							"com.naryx.tagfusion.expression.function.array.arrayFindNoCase" );
		functions.put( "arraylast",										"com.naryx.tagfusion.expression.function.array.arrayLast" );
		functions.put( "arrayfirst",									"com.naryx.tagfusion.expression.function.array.arrayFirst" );
		functions.put( "listitemtrim",								"com.naryx.tagfusion.expression.function.list.listTrim" );

		functions.put( "arrayindexexists",						"com.naryx.tagfusion.expression.function.array.arrayIndexExists" );
		functions.put( "listindexexists",							"com.naryx.tagfusion.expression.function.list.listIndexExists" );

		functions.put( "reescape",										"com.naryx.tagfusion.expression.function.string.reEscape" );
		functions.put( "rematch",											"com.naryx.tagfusion.expression.function.string.reMatch" );
		functions.put( "rematchnocase",								"com.naryx.tagfusion.expression.function.string.reMatchNoCase" );

		functions.put( "htmlhead",										"com.naryx.tagfusion.expression.function.string.htmlHead" );
		functions.put( "htmlbody",										"com.naryx.tagfusion.expression.function.string.htmlBody" );

		functions.put( "arrayconcat",									"com.naryx.tagfusion.expression.function.array.arrayConcat" );
		functions.put( "arrayvaluecount",							"com.naryx.tagfusion.expression.function.array.arrayValueCount" );
		functions.put( "arrayvaluecountnocase",				"com.naryx.tagfusion.expression.function.array.arrayValueCountNoCase" );
		functions.put( "listswap",										"com.naryx.tagfusion.expression.function.list.listSwap" );
		functions.put( "listcompact",									"com.naryx.tagfusion.expression.function.list.listCompact" );

		functions.put( "arrayrest",										"com.naryx.tagfusion.expression.function.array.arrayRest" );
		functions.put( "arrayreverse",								"com.naryx.tagfusion.expression.function.array.arrayReverse" );
		functions.put( "arrayslice",									"com.naryx.tagfusion.expression.function.array.arraySlice" );

		functions.put( "queryrun",										"com.naryx.tagfusion.cfm.sql.queryRun" );
		functions.put( "queryofqueryrun",							"com.naryx.tagfusion.cfm.sql.queryOfQueryRun" );

		functions.put( "datasourcecreate",						"com.naryx.tagfusion.expression.function.ext.datasource.DataSourceCreate" );
		functions.put( "datasourcedelete",						"com.naryx.tagfusion.expression.function.ext.datasource.DataSourceDelete" );
		functions.put( "datasourceisvalid",						"com.naryx.tagfusion.expression.function.ext.datasource.DataSourceIsValid" );
		functions.put( "datasourceinfo",							"com.naryx.tagfusion.expression.function.ext.datasource.DataSourceInfo" );
		functions.put( "datasourceactivepoolstats",		"com.naryx.tagfusion.expression.function.ext.datasource.DataSourceActivePoolStats" );

		functions.put( "queryisempty",								"com.naryx.tagfusion.expression.function.query.queryIsEmpty" );
		functions.put( "querycolumnarray",						"com.naryx.tagfusion.expression.function.query.queryColumnArray" );
		functions.put( "queryrowstruct",							"com.naryx.tagfusion.expression.function.query.queryRowStruct" );
		functions.put( "querycolumnlist",							"com.naryx.tagfusion.expression.function.query.queryColumnList" );
		functions.put( "querydeletecolumn",						"com.naryx.tagfusion.expression.function.query.queryDeleteColumn" );
		functions.put( "queryrenamecolumn",						"com.naryx.tagfusion.expression.function.query.queryRenameColumn" );
		functions.put( "queryeach",										"com.naryx.tagfusion.expression.function.query.queryEach" );

		functions.put( "throwobject",									"com.naryx.tagfusion.expression.function.ext.ThrowObject" );
		functions.put( "throw",												"com.naryx.tagfusion.expression.function.ext.Throw" );
		functions.put( "renderinclude",								"com.naryx.tagfusion.expression.function.ext.Include" );

		functions.put( "isinstanceof",								"com.naryx.tagfusion.expression.function.IsInstanceOf" );
		functions.put( "gethashcode",									"com.naryx.tagfusion.expression.function.GetHashCode" );

		functions.put( "gettagstack", 								"com.naryx.tagfusion.expression.function.GetTagStack" );

		functions.put( "csvread", 										"com.naryx.tagfusion.expression.function.ext.ReadCSV" );
		functions.put( "csvwrite",										"com.naryx.tagfusion.expression.function.ext.ToCSV");


		functions.put( "writelog", 										"com.naryx.tagfusion.expression.function.WriteLog" );
		functions.put( "writedump",										"com.naryx.tagfusion.expression.function.WriteDump" );
		functions.put( "setcookie", 									"com.naryx.tagfusion.cfm.cookie.SetCookie" );

		functions.put("console", 											"com.naryx.tagfusion.expression.function.ext.Console");
		functions.put("consoleoutput",								"com.naryx.tagfusion.expression.function.ext.ConsoleOutput");
		functions.put("queryrequestmetrics",					"com.naryx.tagfusion.expression.function.query.queryRequestMetrics");

		functions.put("datasave",											"com.naryx.tagfusion.expression.function.ext.dataSave");
		functions.put("dataload",											"com.naryx.tagfusion.expression.function.ext.dataLoad");

		functions.put("applicationlist",							"com.naryx.tagfusion.cfm.application.applicationListFunction");
		functions.put("applicationremove",						"com.naryx.tagfusion.cfm.application.applicationRemoveFunction");
		functions.put("applicationcount",							"com.naryx.tagfusion.cfm.application.applicationCountFunction");
		functions.put("getapplicationmetadata",				"com.naryx.tagfusion.cfm.application.getApplicationMetaData");

		functions.put("systemfilecachelist",					"com.naryx.tagfusion.expression.function.ext.SystemFileCacheList");
		functions.put("systemfilecacheflush",					"com.naryx.tagfusion.expression.function.ext.SystemFileCacheFlush");
		functions.put("systemfilecacheinfo",					"com.naryx.tagfusion.expression.function.ext.SystemFileCacheInfo");
		functions.put("systemmemory",									"com.naryx.tagfusion.expression.function.ext.SystemMemory");
		functions.put("sessioncount",									"com.naryx.tagfusion.cfm.application.SessionCountFunction");

		functions.put("logger",												"com.naryx.tagfusion.expression.function.ext.LoggerF");
		functions.put("gethttpstatuslabel",						"com.naryx.tagfusion.expression.function.GetHttpStatusLabel");
		functions.put("datetimeformat",								"com.naryx.tagfusion.expression.function.dateTimeFormat");
		functions.put("structlistnew",								"com.naryx.tagfusion.expression.function.struct.StructList");

		functions.put("zip",													"com.naryx.tagfusion.expression.function.file.Zip");
		functions.put("ziplist",											"com.naryx.tagfusion.expression.function.file.ZipList");
		functions.put("unzip",												"com.naryx.tagfusion.expression.function.file.Unzip");

		functions.put("getcomponentmetadata",					"com.naryx.tagfusion.expression.function.getComponentMetaData");

		functions.put("mappingcreatearchive",					"com.naryx.tagfusion.cfm.file.mapping.FunctionCreateArchiveMapping");
		functions.put("mappingadd",										"com.naryx.tagfusion.cfm.file.mapping.FunctionAddMapping");

		functions.put("http",													"com.naryx.tagfusion.expression.function.remote.Http");

		/* FTP functions */
		functions.put("ftpopen",											"com.naryx.tagfusion.cfm.tag.net.ftp.FtpOpen");
		functions.put("ftpclose",											"com.naryx.tagfusion.cfm.tag.net.ftp.FtpClose");
		functions.put("ftplist",											"com.naryx.tagfusion.cfm.tag.net.ftp.FtpList");
		functions.put("ftpsetcurrentdir",							"com.naryx.tagfusion.cfm.tag.net.ftp.FtpSetCurrentDir");
		functions.put("ftpgetcurrentdir",							"com.naryx.tagfusion.cfm.tag.net.ftp.FtpGetCurrentDir");
		functions.put("ftpgetcurrenturl",							"com.naryx.tagfusion.cfm.tag.net.ftp.FtpGetCurrentUrl");
		functions.put("ftpexistsfile",								"com.naryx.tagfusion.cfm.tag.net.ftp.FtpExistsFile");
		functions.put("ftpexistsdir",									"com.naryx.tagfusion.cfm.tag.net.ftp.FtpExistsDir");
		functions.put("ftpexists",										"com.naryx.tagfusion.cfm.tag.net.ftp.FtpExists");
		functions.put("ftpcreatedir",									"com.naryx.tagfusion.cfm.tag.net.ftp.FtpCreateDir");
		functions.put("ftpremovedir",									"com.naryx.tagfusion.cfm.tag.net.ftp.FtpRemoveDir");
		functions.put("ftpgetfile",										"com.naryx.tagfusion.cfm.tag.net.ftp.FtpGetFile");
		functions.put("ftpputfile",										"com.naryx.tagfusion.cfm.tag.net.ftp.FtpPutFile");
		functions.put("ftpremove",										"com.naryx.tagfusion.cfm.tag.net.ftp.FtpRemove");
		functions.put("ftprename",										"com.naryx.tagfusion.cfm.tag.net.ftp.FtpRename");
		functions.put("ftpsite",											"com.naryx.tagfusion.cfm.tag.net.ftp.FtpSite");
		functions.put("ftpcmd",												"com.naryx.tagfusion.cfm.tag.net.ftp.FtpCmd");


		functions.put("pause",												"com.naryx.tagfusion.expression.function.ext.Pause");
		functions.put("millisecond",									"com.naryx.tagfusion.expression.function.Millisecond");
		functions.put("epoch",												"com.naryx.tagfusion.expression.function.Epoch");
		functions.put("querytoarray",									"com.naryx.tagfusion.expression.function.query.queryToArray");
		functions.put("stripcr",											"com.naryx.tagfusion.expression.function.string.StripCR");
		functions.put("encodeforhtmlattribute",				"com.naryx.tagfusion.expression.function.string.encodeForHtmlAttribute");
		functions.put("encodeforjavascript",					"com.naryx.tagfusion.expression.function.string.encodeForJavascript");
		functions.put("encodeforfilename",						"com.naryx.tagfusion.expression.function.string.encodeForFilename");
		functions.put("decodeforhtml",									"com.naryx.tagfusion.expression.function.string.DecodeForHTML");
		functions.put("hmac",													"com.naryx.tagfusion.expression.function.hmac");
		functions.put("urlfromstruct",								"com.naryx.tagfusion.expression.function.urlFromStruct");

		functions.put( "createobject", 								"com.naryx.tagfusion.expression.function.system.createObject" );
		functions.put( "invoke", 											"com.naryx.tagfusion.expression.function.system.invoke" );
		functions.put( "sethttpstatus", 							"com.naryx.tagfusion.expression.function.system.setHttpStatus" );
		functions.put( "sethttpvalue", 								"com.naryx.tagfusion.expression.function.system.setHttpValue" );
		functions.put( "htmlbody", 										"com.naryx.tagfusion.expression.function.system.htmlBody" );
		functions.put( "htmlhead", 										"com.naryx.tagfusion.expression.function.system.htmlHead" );

		functions.put( "httpdump", 										"com.naryx.tagfusion.expression.function.system.httpDump" );
		functions.put( "getjournaldirectory", 				"com.naryx.tagfusion.expression.function.getJournalDirectory" );

		functions.put( "jsonfileread", 								"com.naryx.tagfusion.expression.function.string.JsonFileRead" );

		cfEngine.thisPlatform.registerFunctions(functions);
  }
}
