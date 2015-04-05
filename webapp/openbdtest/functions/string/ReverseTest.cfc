<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingReverse">

	<cfset assertEquals("olleh", Reverse("hello"))>
	<cfset assertEquals("olleh", Reverse(string="hello"))>
	<cfset assertEquals(Reverse("hello"), Reverse(string="hello"))>

	</cffunction>

</cfcomponent>