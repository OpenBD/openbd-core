<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingStripcr">

	<cfset assertEquals(Stripcr ( string="hello\nworld\n"), Stripcr ( "hello\nworld\n") )>
	</cffunction>

</cfcomponent>