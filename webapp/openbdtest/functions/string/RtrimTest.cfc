<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingRtrim">

	<cfset assertEquals("hello",  Rtrim ( "hello     "))>
	<cfset assertEquals("hello",  Rtrim ( string="hello     "))>
	<cfset assertEquals(Rtrim ( "hello     "),  Rtrim ( string="hello     "))>

	</cffunction>

</cfcomponent>