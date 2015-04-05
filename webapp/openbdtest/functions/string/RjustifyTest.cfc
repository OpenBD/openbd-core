<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingRjustify">

	<cfset assertEquals(20, len(Rjustify ( "hello", 20 )) )>
	<cfset assertEquals(20, len(Rjustify ( string="hello", length=20 )) )>
	<cfset assertEquals(20, len(Rjustify (length=20,string="hello" )) )>

	<cfset assertEquals("hello", trim(Rjustify ( "hello", 20 )))>
	<cfset assertEquals("hello", trim(Rjustify ( string="hello", length=20 )) )>
	<cfset assertEquals("hello", trim(Rjustify (length=2,string="hello" ) ))>

	</cffunction>

</cfcomponent>