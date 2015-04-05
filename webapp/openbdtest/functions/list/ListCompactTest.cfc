<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListCompact">


	<cfset assertEquals("V1,V2,V3", ListCompact(",V1,V2,V3,",","))>
	<cfset assertEquals("V1,V2,V3", ListCompact(list=",V1,V2,V3,",delimiter=","))>
	<cfset assertEquals("V1,V2,V3", ListCompact(delimiter=",",list=",V1,V2,V3,"))>
	<cfset assertEquals(ListCompact(",V1,V2,V3,",","), ListCompact(list=",V1,V2,V3,",delimiter=","))>

	<cfset assertEquals(",V1,V2,V3,", ListCompact(",V1,V2,V3,","}"))>
	<cfset assertEquals(",V1,V2,V3,", ListCompact(list=",V1,V2,V3,",delimiter="}"))>
	<cfset assertEquals(",V1,V2,V3,", ListCompact(delimiter="}",list=",V1,V2,V3,"))>
	<cfset assertEquals(ListCompact(",V1,V2,V3,","}"), ListCompact(list=",V1,V2,V3,",delimiter="}"))>

	<cfset assertEquals(0, Len(ListCompact("","}")) ) >
	<cfset assertEquals(0, Len(ListCompact(list="",delimiter="}")) ) >
	<cfset assertEquals(0, Len(ListCompact(delimiter="}",list="")) ) >
	<cfset assertEquals(Len(ListCompact("","}")) , Len(ListCompact(list="",delimiter="}")) ) >

	</cffunction>

</cfcomponent>