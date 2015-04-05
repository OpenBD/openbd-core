<!--- 
 * 
 *  Copyright (C) 2011 TagServlet Ltd
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


	<cffunction name="testBug245a">
		<cfset var test = createObject( "component", "bug245" )>
		<cfset var result1 = test.someMethod( event="foo" )>

		<cfset assertEquals( "foo", result1 )>

	</cffunction>


	<cffunction name="testBug245b">
		<cfset var test = createObject( "component", "bug245" )>
		<cfset var result2 = test.someMethod2( event="foo" )>
		
		<cfset assertEquals( "foo", result2 )>
	</cffunction>


	<cffunction name="testBug390">

		<cfset var a = ArrayNew(1) />
		<cfset a[1] = CreateObject("component", "bug52") />
		<cfset a[2] = CreateObject("component", "bug52") />

		<cfset var temp = CreateObject("component", "bug52") />

		<cfset temp.go(a) />

	</cffunction>
	
	<cffunction name="testBug403a">
		<cfscript>
		var myObj = new bug403();
		var result = myObj.foo();
		
		assertEquals( 1, result.a );
		assertEquals( 2, result.b );
		assertEquals( 3, result.c );
		</cfscript>
	</cffunction>

	<cffunction name="testBug403b">
		<cfscript>
		var myObj = new bug403();
		var result = myObj.foo(a=4);
		
		assertEquals( 4, result.a );
		assertEquals( 2, result.b );
		assertEquals( 3, result.c );
		</cfscript>
	</cffunction>

	<cffunction name="testBug403c">
		<cfscript>
		var myObj = new bug403();
		var result = myObj.foo(b=4);
		
		assertEquals( 1, result.a );
		assertEquals( 4, result.b );
		assertEquals( 3, result.c );
		</cfscript>
	</cffunction>

	<cffunction name="testBug403d">
		<cfscript>
		var myObj = new bug403();
		var result = myObj.foo(c=4);
		
		assertEquals( 1, result.a );
		assertEquals( 2, result.b );
		assertEquals( 4, result.c );
		</cfscript>
	</cffunction>
	
		<cffunction name="testBug403e">
		<cfscript>
		var myObj = new bug403();
		var result = myObj.foo(b=4,c=5);
		
		assertEquals( 1, result.a );
		assertEquals( 4, result.b );
		assertEquals( 5, result.c );
		</cfscript>
	</cffunction>
	
	<cffunction name="testBug403f">
		<cfscript>
		var myObj = new bug403();
		var result = myObj.foo(c=5,b=4);
		
		assertEquals( 1, result.a );
		assertEquals( 4, result.b );
		assertEquals( 5, result.c );
		</cfscript>
	</cffunction>
	
	
</cfcomponent>


