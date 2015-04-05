<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListRest">

		<cfset assertEquals("v1V2,V3,V4" , ListRest("V1,v1V2,V3,V4"))>
		<cfset assertEquals("v1V2,V3,V4" , ListRest(list="V1,v1V2,V3,V4"))>

		<cfset assertEquals("v1V2;V3;V4" , ListRest("V1;v1V2;V3;V4",";"))>
		<cfset assertEquals("v1V2;V3;V4" , ListRest(list="V1;v1V2;V3;V4",delimiter=";"))>
		<cfset assertEquals("v1V2;V3;V4" , ListRest(delimiter=";",list="V1;v1V2;V3;V4"))>

		<cfset assertEquals("" , ListRest("V1"))>
		<cfset assertEquals("" , ListRest(list="V1"))>

		<cfset assertEquals("" , ListRest("V1",","))>
		<cfset assertEquals("" , ListRest(list="V1",delimiter=","))>
		<cfset assertEquals("" , ListRest(delimiter=",",list="V1"))>
	</cffunction>

</cfcomponent>