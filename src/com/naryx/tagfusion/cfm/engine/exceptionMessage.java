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

import java.util.ListResourceBundle;
 
public class exceptionMessage extends ListResourceBundle {

  public Object[][] getContents() { return contents; }
 
  static final Object[][] contents = {
    
		//--[---------------------------------------------------------------------
    //--[ General Messages
    {"cfEngine.welcomeMessage",   cfEngine.PRODUCT_NAME + " server is starting up..."},
    {"cfEngine.serverStarted",    cfEngine.PRODUCT_NAME + " has initialised. Ready for requests."},
    
		
		//--[---------------------------------------------------------------------
    //--[ Error Codes
		
    {"errorCode.missingStartTag",    	"Missing start tag" },
		{"errorCode.missingEndTag",    		"Missing end tag" },
		{"errorCode.notSupported",    		"Unsupported tag" },
    {"errorCode.notRecognized",       "Unrecognized tag" },
    {"errorCode.badFormat",           "Badly formatted tag"},
		{"errorCode.classError",    			"Class Error" },

		{"errorCode.missingAttribute",    "Missing tag Attribute" },
    {"errorCode.invalidAttribute",    "Invalid Attribute" },
    {"errorCode.invalidExpression",   "Invalid Expression" },
		{"errorCode.invalidTag",    			"Invalid Tag" },
		{"errorCode.runtimeError",     		"General Runtime Error" },
		{"errorCode.badRequest",     			"Invalid Request" },
		{"errorCode.sqlError",     				"General SQL Error" },
		{"errorCode.expressionError", 		"Expression Error" },
		
		{"errorCode.javaException", 		  "java.lang.NoSuchFieldException" },
		
		
		//--[---------------------------------------------------------------------
    //--[ The following are specific MESSAGE DETAIL messages  
    //--[ Tag Specific messages

		{"runtime.general",								"%1" },
		
		{"cffile.missingfile",						"A request was made to a resource that could not be located" },
		{"cffile.applicationrequest",			"The request to %1 was invalid.  This file may not be directly accessed" },
		{"cffile.writeFile",							"Problem writing new file: %1. Check the path to the file is correct" },
		{"cffile.missingcustomtag",				"The file for the custom tag %1 could not be located" },
		{"cffile.duplicatefunctioname",		"The function \"%1\" was declared twice in different templates" },
		
		{"queryofqueries.avgFunction",		"The AVG function cannot be used on data of this type" },
		{"queryofqueries.between",				"Cannot perform operation on these types" },
		{"queryofqueries.badColumn",			"Ambiguous column. %1 occurs in more than one table so should be fully qualified with the table name to use it within the statement" },
		{"queryofqueries.badTable",				"Invalid table. %1 does not exist" },
		{"queryofqueries.invalidIndex",		"Invalid column index to order by [%1]" },
		{"queryofqueries.invalidSelect",	"Invalid column in select statement. %1 does not exist" },
		{"queryofqueries.invalidUnion",		"The number of columns in the select statements of a UNION must be equal" },
		{"queryofqueries.invalidSum",			"The SUM function cannot be used on data of this type" },
		{"queryofqueries.badStatement",		"The following error was produced: %1" },
		{"queryofqueries.badParse",				"The following error was produced: %1" },
		{"queryofqueries.badMetadata",		"Unexpected error retrieving query metadata: %1" },
		
		{"cftag.badAttributes",						"The tag produced the following error: %1" },
		
		{"cfhttp.columnHeaders",					"Error reading query column headers" },
		{"cfhttp.invalidQuery",						"Invalid query; ill formatted file [%1]" },
		{"cfhttp.invalidQueryColumn",			"Invalid query column [%1]; Column names must be valid variable names beginning with a letter and containing letters, numbers and underscores only." },
	
		{"function.invalidFunction",			"Function %1 does not exist" },
		{"function.invalidOperator",			"Operator %1 does not exist" },

		{"duplicate.invalidType",					"Could not duplicate this variable. Check the variable does not contain references to complex types such as Java objects" },
		
		{"listsort.invalidOrder",					"Invalid ORDER parameter value. Valid values are ASC, DESC" },
		{"listsort.invalidType",					"Invalid TYPE parameter value. Valid values are TEXT, TEXTNOCASE, AND NUMERIC" },


		{"expression.Parse",							"Problem occurred while parsing: %1" },
		{"expression.syntaxError",				"Problem occurred while parsing: %1" },

	
		{"sql.invalidColumn",							"The column %1 could not be found" },
		{"sql.invalidColumnType",					"The column type %1 is not supported" },
		{"sql.duplicateColumn",						"The column %1 already exists" },
		{"sql.invalidDatasource",					"The datasource %1 could not be found or was invalid" },
		{"sql.invalidJDBCDriver",					"The driver %1 could not be found or was invalid" },
		{"sql.execution",									"Database reported: %1" },
		{"sql.connecting",								"When connecting to the Database this error was reported: %1" },
		{"sql.disabled",									"SQL operation disabled via admin console" },
		{"sql.storedProcedure",						"The datasource %1 you are using is not permitted to use Stored Procedures" },
		{"sql.storedProcedureSetup",			"Error occurred attempting to prepare the Call Procedure, %1" },
		{"sql.storedProcedureOUT",				"Error occurred attempting to retrieve the OUT variables" },
		{"sql.storedProcedureParams",			"Error occurred attempting to prepare Query" },
		{"sql.storedProcedureExecute",		"Error occurred attempting to execute Query" },
		{"sql.storedProcedureResult",			"Error occurred attempting to retrieve the result sets" },
	

		{"search.failedToList",						"Error occurred attempting to list collections" },
		{"search.failedToCreate",					"Error occurred attempting to create collection" },
		{"search.failedToDelete",					"Error occurred attempting to delete collection" },
		{"search.failedToUpdate",					"Error occurred attempting to update collection" },
		{"search.failedToDeleteDocs",			"Error occurred attempting to delete documents in collection" },
		{"search.failedToPurgeDocs",			"Error occurred attempting to purge documents in collection" },
		{"search.failedToSearch",	    		"Error occurred attempting to search collection" },
		{"search.failedToListCategories",	"Error occurred attempting to list categories in collection" },

		
		{"cftree.treeData",								"Problem serializing the TreeModel" },
		
		{"cfdata.dataNotSupported",				"Data not supported" },
		{"cfdata.dataNotSupportedCustom",	"Data not supported: %1" },
		{"cfdata.Equals",									"Cannot perform equals operation on these types" },
		{"cfdata.javaInvalidClass",				"Class attribute does not exist" },
		{"cfdata.javaInvalidAttribute",		"Class attribute %1 does not exist" },
		{"cfdata.javaIllegalAttribute",		"Class attribute %1 can not be accessed" },
		{"cfdata.javaobjectfail",					"Failed to instantiate object" },
		{"cfdata.javaInvalidConstructor",	"A matching constructor could not be found. Check that you've provided the correct number of arguments" },
		{"cfdata.javaInvalidConstructor2","A matching constructor could not be reliably determined. If possible, use 'javacast()' with the provided arguments to resolve this ambiguity" },		
		{"cfdata.javaInvalidMethod",			"Method %1 could not be found. Check that the method is publicly accessible, the correct number of arguments are specified and that the argument types match" },		
		{"cfdata.javaIllegalMethod",			"Method %1 could not be legally accessed" },		
		{"cfdata.javaMethodExecute",			"Method %1 caused a problem" },		
		{"cfdata.javaGetMethod",					"Method %1 could not be found. Check you have correct method name, the method name casing matches that of the Java class and you've provided the correct number of arguments" },		
		{"cfdata.javaGetMethod2",					"Method %1 is ambiguous as there is more than one method that could correspond to the provided argument types. If possible, use 'javacast()' to resolve this ambiguity." },		
		{"cfdata.javaBadMethod",					"The method cannot be matched to a method with the provided argument types. Check you have provided the correct arguments/argument types" },
		{"cfdata.javaExceptionMessage",		"%1" },		

		
		{"parseTag.missingStartTag",			"The tag %1 had no corresponding opening tag" },
		{"parseTag.missingEndTag",				"The tag %1 had no corresponding ending tag" },
		{"parseTag.notSupported",					"The tag %1 is not supported" },
        {"parseTag.notRecognized",              "The tag %1 is not recognized. Please check for correct spelling." },
		{"parseTag.classError",						"The tag %1 could not be loaded due to an internal error" },
		{"parseTag.badFormat",            "The tag is badly formatted. Check the use of single/double quotes and #'s." },
    
    {"cfquery.missingDatasource",  		"You need to provide a DATASOURCE or DBTYPE attribute for the CFQUERY tag" },
		{"cfquery.missingName",  					"You need to provide a NAME for the query, when you specify DBTYPE"},
		{"cfquery.invalidAction", 				"Only FLUSH, FLUSHALL, or FLUSHCACHE are valid values for the ACTION attribute"},
		{"cfquery.unsupportedSQL", 				"The SQL attribute is not supported.  Please place your SQL inside the body of the CFQUERY tag"},
		{"cfquery.startCachedUntilChangeFailed", 		"Failed to start CACHEDUNTILCHANGE due to following error: %1"},

		{"cfobjectcache.invalidAction", 	"Only CLEAR is valid for ACTION attribute" },


		{"cfprocparam.missingCfsqltype", "The CFSQLTYPE attribute is required" },

		{"cfupdate.missingTablename", 		"You need to provide a TABLENAME attribute"},
    {"cfupdate.missingDatasource",  	"You need to provide a DATASOURCE attribute" },

		{"cfcfx.missingClass", 						"If you are using the CFX_J notation to load your Java CFX tags then you need to have the CLASS attribute."},
		{"cfcfx.invalidClass", 						"The class %1 has not been registered"},

		{"cfmodule.tooManyAttributes", 		"You need to provide just one, either TEMPLATE or NAME"},
		{"cfinclude.missingTemplate", 		"You need to provide a TEMPLATE"},
		{"cfinclude.missingTemplatePage", "You need to provide a TEMPLATE or a PAGE attribute"},
		{"cfinclude.missingFile", 				"The file %1 could not be located"},
		{"cfforward.missingPage", 				"You need to provide the attribute PAGE to which this request will be forwarded to"},

		{"cfinclude.pageExecution",				"The page %1 caused an error" },
		
		{"cfmodule.missingMapping", 			"The directory mapping for the custom tag %1 could not be found"},		
		
		{"cferror.missingType", 					"You must provide the TYPE of error you wish to trap for"},
		{"cferror.invalidType", 					"You specified an illegal value for TYPE. Permitted values: EXCEPTION, REQUEST, VALIDATION or MONITOR"},		
		{"cferror.missingTemplate", 			"You must provide the TEMPLATE that will be used to render the error"},
		
		{"cfexit.invalidMethod", 					"Illegal METHOD attribute value. Permitted values: EXITTAG, EXITTEMPLATE, or LOOP"},

		{"cfdebugger.missingLogfile", 		"You must provide a LOGFILE attribute where the output of this trace will go"},
		
		{"cfapplication.clientBadDataSource", "Failed to locate the datasource that will be used to store the client variables" },
		
		{"cfapplication.cfchartBadDataSource", "Failed to locate the datasource that will be used to store the charts generated by CFCHART" },
	};
}
