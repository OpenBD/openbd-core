<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListValuecountnocase">

		<cfset assertEquals(3 , ListValuecountnocase("V1,V2,V1,v1","V1"))>
		<cfset assertEquals(3 , ListValuecountnocase(list="V1,V2,V1,v1",element="V1"))>
		<cfset assertEquals(3 , ListValuecountnocase(element="V1",list="V1,V2,V1,v1"))>

		<cfset assertEquals(0 , ListValuecountnocase("V1,V2,V1,v1","V4"))>
		<cfset assertEquals(0 , ListValuecountnocase(list="V1,V2,V1,v1",element="V4"))>

		<cfset assertEquals(1 , ListValuecountnocase("V1;V2;V1;v1","V2",";"))>
		<cfset assertEquals(1 , ListValuecountnocase(list="V1;V2;V1;v1",element="V2",delimiter=";"))>
		<cfset assertEquals(1 , ListValuecountnocase(list="V1;V2;V1;v1",delimiter=";",element="V2"))>
		<cfset assertEquals(1 , ListValuecountnocase(delimiter=";",list="V1;V2;V1;v1",element="V2"))>
		<cfset assertEquals(1 , ListValuecountnocase(delimiter=";",element="V2",list="V1;V2;V1;v1"))>
		<cfset assertEquals(1 , ListValuecountnocase(element="V2",delimiter=";",list="V1;V2;V1;v1"))>
		<cfset assertEquals(1 , ListValuecountnocase(element="V2",list="V1;V2;V1;v1",delimiter=";"))>

	</cffunction>

</cfcomponent>
