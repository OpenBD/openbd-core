<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingTrim">

	<cfset assertEquals("hello", Trim ("  hello  ") )>
	<cfset assertEquals("hello", Trim (string="  hello  ") )>
	<cfset assertEquals(Trim ("  hello  "), Trim (string="  hello  ") )>

	</cffunction>

</cfcomponent>