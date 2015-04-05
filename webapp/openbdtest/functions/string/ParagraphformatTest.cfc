<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingParagraphformat">

	<cfset assertEquals("\n\nTest<p>", Paragraphformat("\n\nTest"))>
	<cfset assertEquals("\n\nTest<p>", Paragraphformat(string="\n\nTest"))>
	<cfset assertEquals(Paragraphformat("\n\nTest"), Paragraphformat(string="\n\nTest"))>

	<cfset assertEquals("Test\r\n\r\n<p>", Paragraphformat("Test\r\n\r\n") )>
	<cfset assertEquals("Test\r\n\r\n<p>", Paragraphformat(string="Test\r\n\r\n") )>
	<cfset assertEquals(Paragraphformat("Test\r\n\r\n"), Paragraphformat(string="Test\r\n\r\n") )>

	<cfset assertEquals("Testof\r\<p>", Paragraphformat("Testof\r\") )>
	<cfset assertEquals("Testof\r\<p>", Paragraphformat(string="Testof\r\") )>
	<cfset assertEquals(Paragraphformat("Testof\r\"), Paragraphformat(string="Testof\r\") )>

	<cfset assertEquals("Test\n<p>", Paragraphformat("Test\n") )>
	<cfset assertEquals("Test\n<p>", Paragraphformat(string="Test\n") )>
	<cfset assertEquals(Paragraphformat("Test\n"), Paragraphformat(string="Test\n") )>
	</cffunction>

</cfcomponent>