<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingUcase">

	<cfset assertEquals("HELLO", Ucase ("hello") )>
	<cfset assertEquals("HELLO", Ucase (string="hello") )>
	<cfset assertEquals(Ucase ("hello"), Ucase (string="hello") )>

	</cffunction>

</cfcomponent>