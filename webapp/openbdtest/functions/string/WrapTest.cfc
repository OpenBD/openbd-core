<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingWrap">

	<cfset assertEquals(Wrap(string="hello",width=2), Wrap("hello",2) )>
	<cfset assertEquals(10, len(Wrap("hello",2)) )>
	<cfset assertEquals(10, len(Wrap(string="hello",width=2)) )>

	</cffunction>

</cfcomponent>