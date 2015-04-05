<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListFirst">

	<cfset assertEquals(",V1,v1V2,V3,V2" , ListFirst (",V1,v1V2,V3,V2","}"))>
	<cfset assertEquals(",V1,v1V2,V3,V2" , ListFirst (list=",V1,v1V2,V3,V2",delimiter="}"))>
	<cfset assertEquals(",V1,v1V2,V3,V2" , ListFirst (delimiter="}",list=",V1,v1V2,V3,V2"))>
	<cfset assertEquals(ListFirst (",V1,v1V2,V3,V2","}") , ListFirst (list=",V1,v1V2,V3,V2",delimiter="}"))>

	<cfset assertEquals("V1", ListFirst ("V1;v1V2;V3;V2",";"))>
	<cfset assertEquals("V1", ListFirst (list="V1;v1V2;V3;V2",delimiter=";"))>
	<cfset assertEquals("V1", ListFirst (delimiter=";",list="V1;v1V2;V3;V2"))>
	<cfset assertEquals(ListFirst ("V1;v1V2;V3;V2",";"), ListFirst (delimiter=";",list="V1;v1V2;V3;V2"))>

	<cfset assertEquals("V1", ListFirst ("V1,v1V2,V3,V2"))>
	<cfset assertEquals("V1", ListFirst (list="V1,v1V2,V3,V2"))>
	<cfset assertEquals(ListFirst ("V1,v1V2,V3,V2"), ListFirst (list="V1,v1V2,V3,V2"))>

	<cfset assertEquals("V1,v1V2,V3,V2", ListFirst ("V1,v1V2,V3,V2", "}"))>
	<cfset assertEquals("V1,v1V2,V3,V2", ListFirst (list="V1,v1V2,V3,V2", delimiter="}"))>
	<cfset assertEquals("V1,v1V2,V3,V2", ListFirst ( delimiter="}",list="V1,v1V2,V3,V2"))>
	<cfset assertEquals(ListFirst ("V1,v1V2,V3,V2", "}"), ListFirst (list="V1,v1V2,V3,V2", delimiter="}"))>

	</cffunction>

</cfcomponent>