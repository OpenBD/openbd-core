<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingReplaceList">

	<cfset assertEquals("heuup", ReplaceList("hello","l,o","u,p") )>
	<cfset assertEquals("heuup", ReplaceList(string="hello",listsub="l,o",listnew="u,p") )>
	<cfset assertEquals("heuup", ReplaceList(listsub="l,o",string="hello",listnew="u,p") )>
	<cfset assertEquals(ReplaceList("hello","l,o","u,p"), ReplaceList(listsub="l,o",string="hello",listnew="u,p") )>

	</cffunction>

</cfcomponent>