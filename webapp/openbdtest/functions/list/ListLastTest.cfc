<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListLast">

		<cfset assertEquals("V4" , ListLast("V1,v1V2,V3,V4"))>
		<cfset assertEquals("V4" , ListLast(list="V1,v1V2,V3,V4"))>

		<cfset assertEquals("V1,v1V2,V3,V4" , ListLast(list="V1,v1V2,V3,V4",delimiter=";"))>
		<cfset assertEquals(ListLast("V1,v1V2,V3,V4",";") , ListLast(list="V1,v1V2,V3,V4",delimiter=";"))>
		<cfset assertEquals("V1,v1V2,V3,V4" , ListLast(delimiter=";",list="V1,v1V2,V3,V4"))>

		<cfset assertEquals("V3" , ListLast("V1,v1V2,V3,"))>
		<cfset assertEquals("V3" , ListLast(list="V1,v1V2,V3,"))>

		<cfset assertEquals("" , ListLast(""))>
		<cfset assertEquals("" , ListLast(list=""))>

		<cfset assertEquals("V4" , ListLast("V1;v1V2;V3;V4",";"))>
		<cfset assertEquals("V4" , ListLast(list="V1;v1V2;V3;V4",delimiter=";"))>
		<cfset assertEquals("V4" , ListLast(delimiter=";",list="V1;v1V2;V3;V4"))>

		<cfset assertEquals("V3" , ListLast("V1;v1V2;V3;",";"))>
		<cfset assertEquals("V3" , ListLast(list="V1;v1V2;V3;",delimiter=";"))>
		<cfset assertEquals("V3" , ListLast(delimiter=";",list="V1;v1V2;V3;"))>

	</cffunction>

</cfcomponent>
