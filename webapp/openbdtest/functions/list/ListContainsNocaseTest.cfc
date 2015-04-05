<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListContainsnocasenocase">


	<cfset assertEquals(0, ListContainsnocase(",V1,v1V2,V3,V2","",","))>
	<cfset assertEquals(0, ListContainsnocase(list=",V1,v1V2,V3,V2",substring="",delimiter=","))>
	<cfset assertEquals(0, ListContainsnocase(substring="",list=",V1,v1V2,V3,V2",delimiter=","))>
	<cfset assertEquals(0, ListContainsnocase(substring="",delimiter=",",list=",V1,v1V2,V3,V2"))>
	<cfset assertEquals(0, ListContainsnocase(delimiter=",",substring="",list=",V1,v1V2,V3,V2"))>
	<cfset assertEquals(ListContainsnocase(",V1,v1V2,V3,V2","",","), ListContainsnocase(list=",V1,v1V2,V3,V2",substring="",delimiter=","))>

	<cfset assertEquals(2, ListContainsnocase(",V1,v1V2,V3,V2","v2",","))>
	<cfset assertEquals(2, ListContainsnocase(list=",V1,v1V2,V3,V2",substring="v2",delimiter=","))>
	<cfset assertEquals(2, ListContainsnocase(substring="v2",list=",V1,v1V2,V3,V2",delimiter=","))>
	<cfset assertEquals(2, ListContainsnocase(delimiter=",",substring="v2",list=",V1,v1V2,V3,V2"))>
	<cfset assertEquals(ListContainsnocase(",V1,v1V2,V3,V2","v2",","), ListContainsnocase(list=",V1,v1V2,V3,V2",substring="v2",delimiter=","))>

	<cfset assertEquals(4, ListContainsnocase(",V1,v1V2,V3,V2","v2",",",true))>
	<cfset assertEquals(4, ListContainsnocase(list=",V1,v1V2,V3,v2",substring="v2",delimiter=",",exact=true))>
	<cfset assertEquals(4, ListContainsnocase(substring="v2",list=",V1,v1V2,V3,V2",delimiter=",",exact=true))>
	<cfset assertEquals(4, ListContainsnocase(substring="v2",delimiter=",",list=",V1,v1V2,V3,V2",exact=true))>
	<cfset assertEquals(4, ListContainsnocase(substring="v2",delimiter=",",exact=true,list=",V1,v1V2,V3,V2"))>

	<cfset assertEquals(0, ListContainsnocase(",V1,v1V2,V3,V2","345",","))>
	<cfset assertEquals(0, ListContainsnocase(list=",V1,v1V2,V3,V2",substring="345",delimiter=","))>
	<cfset assertEquals(0, ListContainsnocase(list=",V1,v1V2,V3,V2",delimiter=",",substring="345"))>

	</cffunction>

</cfcomponent>