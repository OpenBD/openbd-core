<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListValuecount">

		<cfset assertEquals(2 , ListValuecount("V1,V2,V1,v1","V1"))>
		<cfset assertEquals(2 , ListValuecount(list="V1,V2,V1,v1",element="V1"))>
		<cfset assertEquals(2 , ListValuecount(element="V1",list="V1,V2,V1,v1"))>

		<cfset assertEquals(0 , ListValuecount("V1,V2,V1,v1","V4"))>
		<cfset assertEquals(0 , ListValuecount(list="V1,V2,V1,v1",element="V4"))>

		<cfset assertEquals(1 , ListValuecount("V1;V2;V1;v1","V2",";"))>
		<cfset assertEquals(1 , ListValuecount(list="V1;V2;V1;v1",element="V2",delimiter=";"))>
		<cfset assertEquals(1 , ListValuecount(list="V1;V2;V1;v1",delimiter=";",element="V2"))>
		<cfset assertEquals(1 , ListValuecount(element="V2",list="V1;V2;V1;v1",delimiter=";"))>
		<cfset assertEquals(1 , ListValuecount(element="V2",delimiter=";",list="V1;V2;V1;v1"))>
		<cfset assertEquals(1 , ListValuecount(delimiter=";",element="V2",list="V1;V2;V1;v1"))>
		<cfset assertEquals(1 , ListValuecount(delimiter=";",list="V1;V2;V1;v1",element="V2"))>

	</cffunction>

</cfcomponent>
