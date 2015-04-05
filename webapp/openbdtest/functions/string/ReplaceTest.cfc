<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingReplace">

	<cfset assertEquals("herro", Replace("hello","l","r","ALL") )>
	<cfset assertEquals("herro", Replace(string="hello",substring="l",new="r",flag="ALL") )>
	<cfset assertEquals("herro", Replace(substring="l",string="hello",new="r",flag="ALL") )>

	<cfset assertEquals("herlo", Replace("hello","l","r","ONE") )>
	<cfset assertEquals("herlo", Replace(string="hello",substring="l",new="r",flag="ONE") )>
	<cfset assertEquals("herlo", Replace(substring="l",string="hello",new="r",flag="ONE") )>

	</cffunction>

</cfcomponent>