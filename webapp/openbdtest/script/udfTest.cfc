<cfcomponent extends="mxunit.framework.TestCase">
	
	<cfset variables.inited = false>
	
	<cffunction name="setUp" access="public" returntype="void">
		<cfif !variables.inited>
			<!--- includes all the udfs for the set of tests --->
			<cfinclude template="_udfTest.cfm">	
			<cfset variables.inited = true>
		</cfif>
	</cffunction>

	<!---
	Testing function access type
	--->
	<cffunction name="testAccessTypePublic" hint="tests the function access type when set to 'public'" >
		<cfset assertTrue( isDefined( "accessTypePublic" ) )>
		<cfset var functionMetadata = getmetadata(accessTypePublic)>
		<cfset assertEquals( "public", functionMetadata.access )>
		<cfset assertEquals( "public", accessTypePublic() )>		
	</cffunction>


	<cffunction name="testAccessTypePrivate" hint="tests the function access type when set to 'private'" >
		<cfset assertTrue( isDefined( "accessTypePrivate" ) )>
		<cfset var functionMetadata = getmetadata(accessTypePrivate)>
		<cfset assertEquals( "private", functionMetadata.access )>
		<cfset assertEquals( "private", accessTypePrivate() )>		
	</cffunction>


	<!---
	Testing function return type
	--->

	<cffunction name="testReturnTypeString" hint="tests the function return type when set to 'string'" >
		<cfset assertTrue( isDefined( "returnTypeString" ) )>
		<cfset var functionMetadata = getmetadata(returnTypeString)>
		<cfset assertEquals( "public", functionMetadata.access )>
		<cfset assertEquals( "string", functionMetadata.returntype )>
		<cfset assertEquals(  "test string", returnTypeString( "test string" ) )>		
		<cfset assertEquals(  "1", returnTypeString( 1 ) )>		
		
		<cftry>
			<cfset returnTypeString( [] )>
			<cfset fail( "Should have failed due to return type validation" )>
		<cfcatch>
			<cfset assertTrue( cfcatch.message contains "not of type string" )>
		</cfcatch> 
		</cftry>
	</cffunction>


	<cffunction name="testReturnTypeBoolean" hint="tests the function return type when set to 'boolean'" >
		<cfset assertTrue( isDefined( "returnTypeBoolean" ) )>
		<cfset var functionMetadata = getmetadata(returnTypeBoolean)>
		<cfset assertEquals( "public", functionMetadata.access )>
		<cfset assertEquals( "boolean", functionMetadata.returntype )>
		<cfset assertEquals(  true, returnTypeBoolean( true ) )>		
		<cfset assertEquals(  1, returnTypeBoolean( 1 ) )>		
		<cfset assertEquals(  0, returnTypeBoolean( 0 ) )>		
		<cfset assertEquals(  "YES", returnTypeBoolean( "YES" ) )>		
		<cfset assertEquals(  "NO", returnTypeBoolean( "NO" ) )>		
		
		<cftry>
			<cfset returnTypeBoolean( "foo" )>
			<cfset fail( "Should have failed due to return type validation" )>
		<cfcatch>
			<cfset assertTrue( cfcatch.message contains "not of type boolean" )>
		</cfcatch> 
		</cftry>
	</cffunction>


	<cffunction name="testReturnTypeArray" hint="tests the function return type when set to 'array'" >
		<cfset assertTrue( isDefined( "returnTypeArray" ) )>
		<cfset var functionMetadata = getmetadata(returnTypeArray)>
		<cfset assertEquals( "public", functionMetadata.access )>
		<cfset assertEquals( "array", functionMetadata.returntype )>
		<cfset assertTrue(  isArray( returnTypeArray( [ 1, 2, 3 ] ) ) )>		
		
		<cftry>
			<cfset returnTypeArray( {} )>
			<cfset fail( "Should have failed due to return type validation" )>
		<cfcatch>
			<cfset assertTrue( cfcatch.message contains "not of type array" )>
		</cfcatch> 
		</cftry>
	</cffunction>


	<!---
	Testing function parameter support
	--->
	
	<cffunction name="testParamString" hint="tests the function param type when set to 'string'">
		<cfset assertTrue( isDefined( "paramString" ) )>
		<cfset var functionMetadata = getmetadata(paramString)>
		<cfset assertEquals( "public", functionMetadata.access )>
		<cfset assertEquals( 1, arraylen( functionMetadata.parameters ) )>
		<cfset assertEquals( "foo", functionMetadata.parameters[1].name )>
		<cfset assertFalse( functionMetadata.parameters[1].required )>
		<cfset assertEquals( "string", functionMetadata.parameters[1].type )>
		
		<cfset assertTrue( paramString( "str" ) )>		
		<cfset assertTrue( paramString( true ) )>		
		<cfset assertTrue( paramString( 1 ) )>		
		<!--- parameter is not required --->
		<cfset assertFalse( paramString() )>		
		
		<cftry>
			<!--- check param type validation works --->
			<cfset paramString( {} )>
			<cfset fail( "Should have failed due to return type validation" )>
		<cfcatch>
			<cfset assertTrue( cfcatch.message contains "not of type string" )>
		</cfcatch> 
		</cftry>
	</cffunction>
	

	<cffunction name="testParamRequired" hint="tests the function param when it's required">
		<cfset assertTrue( isDefined( "paramRequired" ) )>
		<cfset var functionMetadata = getmetadata(paramRequired)>
		<cfset assertEquals( "public", functionMetadata.access )>
		<cfset assertEquals( 1, arraylen( functionMetadata.parameters ) )>
		<cfset assertEquals( "foo", functionMetadata.parameters[1].name )>
		<cfset assertTrue( functionMetadata.parameters[1].required )>
		<cfset assertEquals( "string", functionMetadata.parameters[1].type )>
		
		<cfset assertTrue( paramRequired( "str" ) )>		
		<cfset assertTrue( paramRequired( true ) )>		
		<cfset assertTrue( paramRequired( 1 ) )>		
		
		<cftry>
			<!--- check param type validation works --->
			<cfset paramRequired()>
			<cfset fail( "Should have failed due to return type validation" )>
		<cfcatch>
			<cfset assertTrue( cfcatch.message contains "required" )>
		</cfcatch> 
		</cftry>
	</cffunction>
	

	<cffunction name="testParamDefault" hint="tests the function param default">
		<cfset assertTrue( isDefined( "paramDefault" ) )>
		<cfset var functionMetadata = getmetadata(paramDefault)>
		<cfset assertEquals( "public", functionMetadata.access )>
		<cfset assertEquals( 1, arraylen( functionMetadata.parameters ) )>
		<cfset assertEquals( "foo", functionMetadata.parameters[1].name )>
		<cfset assertFalse( functionMetadata.parameters[1].required )>
		<cfset assertEquals( "string", functionMetadata.parameters[1].type )>
		<cfset assertEquals( "bar", functionMetadata.parameters[1].default )>
		
		<cfset assertEquals( "str", paramDefault( "str" ) )>		
		<cfset assertEquals( "bar", paramDefault() )>		
		
		<cftry>
			<!--- check param type validation works --->
			<cfset paramDefault( [] )>
			<cfset fail( "Should have failed due to return type validation" )>
		<cfcatch>
			<cfset assertTrue( cfcatch.message contains "not of type string" )>
		</cfcatch> 
		</cftry>
	</cffunction>
	
	<cffunction name="testParamMulti" hint="tests a function with multiple parameters">
		<cfset assertTrue( isDefined( "paramMulti" ) )>
		<cfset var functionMetadata = getmetadata(paramMulti)>
		<cfset assertEquals( "public", functionMetadata.access )>
		<cfset assertEquals( 5, arraylen( functionMetadata.parameters ) )>
		
		<cfset assertEquals( "foo1", functionMetadata.parameters[1].name )>
		<cfset assertTrue( functionMetadata.parameters[1].required )>
		<cfset assertEquals( "string", functionMetadata.parameters[1].type )>

		<cfset assertEquals( "foo2", functionMetadata.parameters[2].name )>
		<cfset assertFalse( functionMetadata.parameters[2].required )>
		<cfset assertEquals( "any", functionMetadata.parameters[2].type )>

		<cfset assertEquals( "foo3", functionMetadata.parameters[3].name )>
		<cfset assertFalse( functionMetadata.parameters[3].required )>
		<cfset assertFalse( structkeyexists( functionMetadata.parameters[3], "type" ) )>
		<cfset assertFalse( structkeyexists( functionMetadata.parameters[3], "default" ) )>

		<cfset assertEquals( "foo4", functionMetadata.parameters[4].name )>
		<cfset assertFalse( functionMetadata.parameters[4].required )>
		<cfset assertFalse( structkeyexists( functionMetadata.parameters[4], "type" ) )>
		<cfset assertEquals( "foo4defaulted", functionMetadata.parameters[4].default )>

		<cfset assertEquals( "foo5", functionMetadata.parameters[5].name )>
		<cfset assertFalse( functionMetadata.parameters[5].required )>
		<cfset assertFalse( structkeyexists( functionMetadata.parameters[5], "type" ) )>
		<cfset assertEquals( "[runtime expression]", functionMetadata.parameters[5].default )>
		
		<cfset str = "test">
		<cfset assertEquals( "str1 - str2 - true - foo4defaulted - testtest", paramMulti( "str1", "str2", true ) )>		
		<cfset assertEquals( "str1 - str2 - str3 - str4 - str5", paramMulti( "str1", "str2", "str3", "str4", "str5" ) )>		
		
	</cffunction>
	
	<cffunction name="testFunctionAttributes" hint="tests a function with attributes">
		<cfset assertTrue( isDefined( "functionAttributes" ) )>
		<cfset var functionMetadata = getmetadata(functionAttributes)>
		
		<cfset assertEquals( "public", functionMetadata.access )>
		<cfset assertEquals( 1, arraylen( functionMetadata.parameters ) )>
		
		<cfset assertEquals( "my hint", functionMetadata.hint )>
		<cfset assertEquals( "my description", functionMetadata.description )>
		<cfset assertEquals( "my displayname", functionMetadata.displayname )>
		<cfset assertEquals( "my roles", functionMetadata.roles )>

	</cffunction>
	

	<cffunction name="testFunctionOutputAttribute" hint="tests a function with attributes">
		<cfset assertTrue( isDefined( "functionOutputAttributeTrue" ) )>
		<cfset assertTrue( isDefined( "functionOutputAttributeFalse" ) )>

		<cfset var functionMetadata = getmetadata(functionOutputAttributeTrue)>
		<cfset assertEquals( "true", functionMetadata.output )>
		<cfset var result1 = "">
		<cfsavecontent variable="result1"><cfset functionOutputAttributeTrue()></cfsavecontent>
		<cfset assertEquals( "some output", result1 )>

		<cfset functionMetadata = getmetadata(functionOutputAttributeFalse)>
		<cfset assertEquals( "false", functionMetadata.output )>
		<cfset var result2 = "">
		<cfsavecontent variable="result2"><cfset functionOutputAttributeFalse()></cfsavecontent>
		<cfset assertEquals( "", result2 )>
	</cffunction>
	
	<cffunction name="testkeywords" hint="Test to ensure function keywords aren't treated as reserved words">

		<!--- test cfscript first --->	
		<cfscript>
		public = "public1";
		private = "private1";
		remote = "remote1";
		package = "package1";
		required = "required1";
		</cfscript>
		
		<cfoutput>
		#public#<br />
		#private#<br />
		#remote#<br />
		#package#<br />
		#required#<br />
		</cfoutput>

		<!--- test expression engine --->	
		<cfset public = "public2">
		<cfset private = "private2">
		<cfset remote = "remote2">
		<cfset package = "package2">
		<cfset required = "required2">

		<cfoutput>
		#public#<br />
		#private#<br />
		#remote#<br />
		#package#<br />
		#required#<br />
		</cfoutput>
	
	</cffunction>

	<cffunction name="testBareMinimum" hint="Test to ensure simplest function declaration works">
		<cfset assertTrue( isDefined( "functionBareMinimum" ) )>
		
		<cfset assertEquals( "passed", functionBareMinimum() )>
		
		<cfset var comp = createObject( "mycomponent") >
		<cfset assertEquals( "passed", comp.functionBareMinimum() )>
	</cffunction>
	
</cfcomponent>