<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListFindnocasenocase">

	<cfset assertEquals(0, ListFindnocase (",V1,v1V2,V3,V2",""))>
	<cfset assertEquals(ListFindnocase (",V1,v1V2,V3,V2",""), ListFindnocase (list=",V1,v1V2,V3,V2",element=""))>
	<cfset assertEquals(0, ListFindnocase (list=",V1,v1V2,V3,V2",element=""))>
	<cfset assertEquals(0, ListFindnocase (element="",list=",V1,v1V2,V3,V2"))>

	<cfset assertEquals(0, ListFindnocase (",V1,v1V2,V3,V2","v34"))>
	<cfset assertEquals(ListFindnocase (",V1,v1V2,V3,V2","v34"), ListFindnocase (list=",V1,v1V2,V3,V2",element="v34"))>
	<cfset assertEquals(0, ListFindnocase (element="v34",list=",V1,v1V2,V3,V2"))>
	<cfset assertEquals(0, ListFindnocase (list=",V1,v1V2,V3,V2",element="v34"))>

	<cfset assertEquals(1, ListFindnocase (",V1,v1V2,V3,V2","v1"))>
	<cfset assertEquals(ListFindnocase (",V1,v1V2,V3,V2","v1"), ListFindnocase (list=",V1,v1V2,V3,V2",element="v1"))>
	<cfset assertEquals(1, ListFindnocase (list=",V1,v1V2,V3,V2",element="v1"))>
	<cfset assertEquals(1, ListFindnocase (element="v1",list=",V1,v1V2,V3,V2"))>

	<cfset assertEquals(4, ListFindnocase (",V1,v1V2,V3,V2","V2"))>
	<cfset assertEquals(ListFindnocase (",V1,v1V2,V3,V2","V2"), ListFindnocase (list=",V1,v1V2,V3,V2",element="V2"))>
	<cfset assertEquals(4, ListFindnocase (list=",V1,v1V2,V3,V2",element="V2"))>
	<cfset assertEquals(4, ListFindnocase (element="V2",list=",V1,v1V2,V3,V2"))>

	<cfset assertEquals(0, ListFindnocase (",V1,v1V2,V3,V2","V2","}"))>
	<cfset assertEquals(ListFindnocase (",V1,v1V2,V3,V2","V2","}"), ListFindnocase (list=",V1,v1V2,V3,V2",element="V2",delimiter="}"))>
	<cfset assertEquals(0, ListFindnocase (list=",V1,v1V2,V3,V2",element="V2",delimiter="}"))>
	<cfset assertEquals(0, ListFindnocase (element="V2",list=",V1,v1V2,V3,V2",delimiter="}"))>
	<cfset assertEquals(0, ListFindnocase (element="V2",delimiter="}",list=",V1,v1V2,V3,V2"))>

	</cffunction>

</cfcomponent>