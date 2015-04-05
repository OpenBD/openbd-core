<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListLen">

		<cfset assertEquals(4 , ListLen("V1,v1V2,V3,V4"))>
		<cfset assertEquals(4 , ListLen(list="V1,v1V2,V3,V4"))>

		<cfset assertEquals(1 , ListLen("V1,v1V2,V3,V4",";"))>
		<cfset assertEquals(1 , ListLen(list="V1,v1V2,V3,V4",delimiter=";"))>
		<cfset assertEquals(1 , ListLen(delimiter=";",list="V1,v1V2,V3,V4"))>

		<cfset assertEquals(4 , ListLen("V1;v1V2;V3;V4",";"))>
		<cfset assertEquals(4 , ListLen(list="V1;v1V2;V3;V4",delimiter=";"))>
		<cfset assertEquals(4 , ListLen(delimiter=";",list="V1;v1V2;V3;V4"))>

		<cfset assertEquals(3 , ListLen("V1,v1V2,V3,"))>
		<cfset assertEquals(3 , ListLen(list="V1,v1V2,V3,"))>

		<cfset assertEquals(3 , ListLen("V1;v1V2;V3;",";"))>
		<cfset assertEquals(3 , ListLen(list="V1;v1V2;V3;",delimiter=";"))>
		<cfset assertEquals(3 , ListLen(delimiter=";",list="V1;v1V2;V3;"))>

		<cfset assertEquals(0 , ListLen(""))>
		<cfset assertEquals(0 , ListLen(list=""))>

	</cffunction>

</cfcomponent>