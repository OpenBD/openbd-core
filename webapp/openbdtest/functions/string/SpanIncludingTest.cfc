<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingSpanIncluding">

	<cfset assertEquals("lollolololo", SpanIncluding ( "lollolololo","lo") )>
	<cfset assertEquals("lollolololo", SpanIncluding ( string="lollolololo",substring="lo") )>
	<cfset assertEquals("lollolololo", SpanIncluding ( substring="lo",string="lollolololo") )>
	<cfset assertEquals(SpanIncluding ( "lollolololo","lo"), SpanIncluding ( substring="lo",string="lollolololo") )>
	</cffunction>

</cfcomponent>