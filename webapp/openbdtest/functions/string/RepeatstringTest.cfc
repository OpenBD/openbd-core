<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingRepeatstring">

	<cfset assertEquals("hellohellohello", Repeatstring("hello",3) )>
	<cfset assertEquals("hellohellohello", Repeatstring(string="hello",repeatcount=3) )>
	<cfset assertEquals("hellohellohello", Repeatstring(repeatcount=3,string="hello") )>
	<cfset assertEquals(Repeatstring("hello",3) , Repeatstring(string="hello",repeatcount=3) )>

	<cfset assertEquals("", Repeatstring("hello",0) )>
	<cfset assertEquals("", Repeatstring(string="hello",repeatcount=0) )>
	<cfset assertEquals("", Repeatstring(repeatcount=0,string="hello") )>
	<cfset assertEquals(Repeatstring("hello",0), Repeatstring(string="hello",repeatcount=0) )>

	</cffunction>

</cfcomponent>