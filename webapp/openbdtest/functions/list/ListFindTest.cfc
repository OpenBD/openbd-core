<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListFind">

	<cfset assertEquals(0, ListFind (",V1,v1V2,V3,V2",""))>
	<cfset assertEquals(ListFind (",V1,v1V2,V3,V2",""), ListFind (list=",V1,v1V2,V3,V2",element=""))>
	<cfset assertEquals(0, ListFind (list=",V1,v1V2,V3,V2",element=""))>
	<cfset assertEquals(0, ListFind (element="",list=",V1,v1V2,V3,V2"))>

	<cfset assertEquals(0, ListFind (",V1,v1V2,V3,V2","v34"))>
	<cfset assertEquals(ListFind (",V1,v1V2,V3,V2","v34"), ListFind (list=",V1,v1V2,V3,V2",element="v34"))>
	<cfset assertEquals(0, ListFind (element="v34",list=",V1,v1V2,V3,V2"))>
	<cfset assertEquals(0, ListFind (list=",V1,v1V2,V3,V2",element="v34"))>

	<cfset assertEquals(0, ListFind (",V1,v1V2,V3,V2","v1"))>
	<cfset assertEquals(ListFind (",V1,v1V2,V3,V2","v1"), ListFind (list=",V1,v1V2,V3,V2",element="v1"))>
	<cfset assertEquals(0, ListFind (list=",V1,v1V2,V3,V2",element="v1"))>
	<cfset assertEquals(0, ListFind (element="v1",list=",V1,v1V2,V3,V2"))>

	<cfset assertEquals(4, ListFind (",V1,v1V2,V3,V2","V2"))>
	<cfset assertEquals(ListFind (",V1,v1V2,V3,V2","V2"), ListFind (list=",V1,v1V2,V3,V2",element="V2"))>
	<cfset assertEquals(4, ListFind (list=",V1,v1V2,V3,V2",element="V2"))>
	<cfset assertEquals(4, ListFind (element="V2",list=",V1,v1V2,V3,V2"))>

	<cfset assertEquals(0, ListFind (",V1,v1V2,V3,V2","V2","}"))>
	<cfset assertEquals(ListFind (",V1,v1V2,V3,V2","V2","}"), ListFind (list=",V1,v1V2,V3,V2",element="V2",delimiter="}"))>
	<cfset assertEquals(0, ListFind (list=",V1,v1V2,V3,V2",element="V2",delimiter="}"))>
	<cfset assertEquals(0, ListFind (element="V2",list=",V1,v1V2,V3,V2",delimiter="}"))>
	<cfset assertEquals(0, ListFind (element="V2",delimiter="}",list=",V1,v1V2,V3,V2"))>

	</cffunction>

</cfcomponent>