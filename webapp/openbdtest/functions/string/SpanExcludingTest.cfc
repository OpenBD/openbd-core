<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingSpanExcluding">

	<cfset assertEquals(0, len(SpanExcluding ( "lollolololo","lo")) )>
	<cfset assertEquals(0, len(SpanExcluding ( string="lollolololo",substring="lo")) )>
	<cfset assertEquals(0, len(SpanExcluding ( substring="lo",string="lollolololo")) )>
	<cfset assertEquals(len(SpanExcluding ( "lollolololo","lo")), len(SpanExcluding ( substring="lo",string="lollolololo")) )>
	</cffunction>

</cfcomponent>