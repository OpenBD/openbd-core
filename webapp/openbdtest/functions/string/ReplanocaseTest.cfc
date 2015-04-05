<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingReplaceNocase">

	<cfset assertEquals("herro", ReplaceNocase("heLlo","l","r","ALL") )>
	<cfset assertEquals("herro", ReplaceNocase(string="heLlo",substring="l",new="r",flag="ALL") )>

	</cffunction>

</cfcomponent>