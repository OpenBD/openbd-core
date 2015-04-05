<!--- 
 * 
 *  Copyright (C) 2010 TagServlet Ltd
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
 *
--->
<cfcomponent extends="openbdtest.common.TestCase">

	<!---
	Testing for...in over an array
	--->
	<cffunction name="testForInArray" hint="tests the for...in loop over an array" >

		<cfscript>
		var a = ["a","b","c"];
		var i;
		var result = '';
		for ( i in a ){
			result &= i;
		}
		
		assertEquals( "abc", result );
		</cfscript>

	</cffunction>

	<cffunction name="testVarDecl" hint="tests the use of var" >
		<cfscript>
		var a = ["a","b","c"];
		var result = '';
		for ( var testVarDeclIndx = 1; testVarDeclIndx <= arraylen( a ); testVarDeclIndx++ ){
			result &= a[testVarDeclIndx];
		}
		
		assertEquals( "abc", result );
		</cfscript>

	</cffunction>

	<cffunction name="testVarDecl2" hint="tests the use of var" >
		<!--- check the locally declared variable in testVarDecl is kept local --->
		<cfset assertFalse( isDefined( "testVarDeclIndx" ) )>
		<cfset testVarDecl()>
		<cfset assertFalse( isDefined( "testVarDeclIndx" ) )>

	</cffunction>
	
	
</cfcomponent>