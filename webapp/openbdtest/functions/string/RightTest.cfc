<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingRight">

	<cfset assertEquals("lo", Right( "hello", 2 ))>
	<cfset assertEquals("lo", Right(string="hello", length=2 ))>
	<cfset assertEquals("lo", Right(length=2,string="hello" ))>
	<cfset assertEquals( Right( "hello", 2 ), Right(length=2,string="hello" ))>

	</cffunction>

</cfcomponent>