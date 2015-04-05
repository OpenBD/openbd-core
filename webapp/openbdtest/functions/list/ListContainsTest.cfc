<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListContains">


	<cfset assertEquals(0, ListContains(",V1,v1V2,V3,V2","",","))>
	<cfset assertEquals(0, ListContains(list=",V1,v1V2,V3,V2",substring="",delimiter=","))>
	<cfset assertEquals(0, ListContains(substring="",list=",V1,v1V2,V3,V2",delimiter=","))>
	<cfset assertEquals(0, ListContains(substring="",delimiter=",",list=",V1,v1V2,V3,V2"))>
	<cfset assertEquals(0, ListContains(delimiter=",",substring="",list=",V1,v1V2,V3,V2"))>
	<cfset assertEquals(ListContains(",V1,v1V2,V3,V2","",","), ListContains(list=",V1,v1V2,V3,V2",substring="",delimiter=","))>

	<cfset assertEquals(2, ListContains(",V1,v1V2,V3,V2","V2",","))>
	<cfset assertEquals(2, ListContains(list=",V1,v1V2,V3,V2",substring="V2",delimiter=","))>
	<cfset assertEquals(2, ListContains(substring="V2",list=",V1,v1V2,V3,V2",delimiter=","))>
	<cfset assertEquals(2, ListContains(substring="V2",delimiter=",",list=",V1,v1V2,V3,V2"))>
	<cfset assertEquals(2, ListContains(delimiter=",",substring="V2",list=",V1,v1V2,V3,V2"))>
	<cfset assertEquals(ListContains(",V1,v1V2,V3,V2","V2",","), ListContains(list=",V1,v1V2,V3,V2",substring="V2",delimiter=","))>

	<cfset assertEquals(4, ListContains(",V1,v1V2,V3,V2","V2",",",true))>
	<cfset assertEquals(4, ListContains(list=",V1,v1V2,V3,V2",substring="V2",delimiter=",",exact=true))>
	<cfset assertEquals(4, ListContains(substring="V2",list=",V1,v1V2,V3,V2",delimiter=",",exact=true))>
	<cfset assertEquals(4, ListContains(substring="V2",delimiter=",",list=",V1,v1V2,V3,V2",exact=true))>
	<cfset assertEquals(4, ListContains(substring="V2",delimiter=",",exact=true,list=",V1,v1V2,V3,V2"))>

	<cfset assertEquals(0, ListContains(",V1,v1V2,V3,V2","345",","))>
	<cfset assertEquals(0, ListContains(list=",V1,v1V2,V3,V2",substring="345",delimiter=","))>
	<cfset assertEquals(0, ListContains(list=",V1,v1V2,V3,V2",delimiter=",",substring="345"))>

	</cffunction>

</cfcomponent>