<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListPrepend">

		<cfset assertEquals("v0,V1,v1V2,V3,V4" , ListPrepend("V1,v1V2,V3,V4","v0"))>
		<cfset assertEquals("v0,V1,v1V2,V3,V4" , ListPrepend(list="V1,v1V2,V3,V4",value="v0"))>
		<cfset assertEquals("v0,V1,v1V2,V3,V4" , ListPrepend(value="v0",list="V1,v1V2,V3,V4"))>

		<cfset assertEquals("v0;V1,v1V2,V3,V4" , ListPrepend("V1,v1V2,V3,V4","v0",";"))>
		<cfset assertEquals("v0;V1,v1V2,V3,V4" , ListPrepend(list="V1,v1V2,V3,V4",value="v0",delimiter=";"))>
		<cfset assertEquals("v0;V1,v1V2,V3,V4" , ListPrepend(list="V1,v1V2,V3,V4",delimiter=";",value="v0"))>
		<cfset assertEquals("v0;V1,v1V2,V3,V4" , ListPrepend(value="v0",list="V1,v1V2,V3,V4",delimiter=";"))>
		<cfset assertEquals("v0;V1,v1V2,V3,V4" , ListPrepend(value="v0",delimiter=";",list="V1,v1V2,V3,V4"))>
		<cfset assertEquals("v0;V1,v1V2,V3,V4" , ListPrepend(delimiter=";",list="V1,v1V2,V3,V4",value="v0"))>
		<cfset assertEquals("v0;V1,v1V2,V3,V4" , ListPrepend(delimiter=";",value="v0",list="V1,v1V2,V3,V4"))>

		<cfset assertEquals("v0;V1;v1V2;V3;V4" , ListPrepend("V1;v1V2;V3;V4","v0",";"))>
		<cfset assertEquals("v0;V1;v1V2;V3;V4" , ListPrepend(list="V1;v1V2;V3;V4",value="v0",delimiter=";"))>
		<cfset assertEquals("v0;V1;v1V2;V3;V4" , ListPrepend(list="V1;v1V2;V3;V4",delimiter=";",value="v0"))>
		<cfset assertEquals("v0;V1;v1V2;V3;V4" , ListPrepend(value="v0",list="V1;v1V2;V3;V4",delimiter=";"))>
		<cfset assertEquals("v0;V1;v1V2;V3;V4" , ListPrepend(value="v0",delimiter=";",list="V1;v1V2;V3;V4"))>
		<cfset assertEquals("v0;V1;v1V2;V3;V4" , ListPrepend(delimiter=";",list="V1;v1V2;V3;V4",value="v0"))>
		<cfset assertEquals("v0;V1;v1V2;V3;V4" , ListPrepend(delimiter=";",value="v0",list="V1;v1V2;V3;V4"))>

		<cfset assertEquals("v0" , ListPrepend("","v0"))>
		<cfset assertEquals("v0" , ListPrepend(list="",value="v0"))>
		<cfset assertEquals("v0" , ListPrepend(value="v0",list=""))>

		<cfset assertEquals("v0" , ListPrepend("","v0",";"))>
		<cfset assertEquals("v0" , ListPrepend(list="",value="v0",delimiter=";"))>
		<cfset assertEquals("v0" , ListPrepend(list="",delimiter=";",value="v0"))>
		<cfset assertEquals("v0" , ListPrepend(value="v0",list="",delimiter=";"))>
		<cfset assertEquals("v0" , ListPrepend(value="v0",delimiter=";",list=""))>
		<cfset assertEquals("v0" , ListPrepend(delimiter=";",value="v0",list=""))>
		<cfset assertEquals("v0" , ListPrepend(delimiter=";",list="",value="v0"))>

	</cffunction>

</cfcomponent>

