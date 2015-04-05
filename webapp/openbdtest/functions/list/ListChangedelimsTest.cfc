<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListChangedelims">


	<cfset assertEquals("V1}V2}V3", ListChangedelims("V1,V2,V3","}",","))>
	<cfset assertEquals("V1}V2}V3", ListChangedelims(list="V1,V2,V3",newDel="}",oldDel=","))>
	<cfset assertEquals("V1}V2}V3", ListChangedelims(newDel="}",list="V1,V2,V3",oldDel=","))>
	<cfset assertEquals("V1}V2}V3", ListChangedelims(newDel="}",oldDel=",",list="V1,V2,V3"))>
	<cfset assertEquals("V1}V2}V3", ListChangedelims(oldDel=",",newDel="}",list="V1,V2,V3"))>
	<cfset assertEquals(ListChangedelims(list="V1,V2,V3",newDel="}",oldDel=","), ListChangedelims("V1,V2,V3","}",","))>

	<cfset assertEquals("V1*V2*V3;V4", ListChangedelims(list="V1}V2}V3;V4",newDel="*",oldDel="}"))>
	<cfset assertEquals("V1*V2*V3;V4", ListChangedelims("V1}V2}V3;V4","*","}"))>
	<cfset assertEquals("V1*V2*V3;V4", ListChangedelims(oldDel="}",newDel="*",list="V1}V2}V3;V4"))>
	<cfset assertEquals("V1*V2*V3;V4", ListChangedelims(newDel="*",list="V1}V2}V3;V4",oldDel="}"))>
	<cfset assertEquals("V1*V2*V3;V4", ListChangedelims(newDel="*",oldDel="}",list="V1}V2}V3;V4"))>
	<cfset assertEquals(ListChangedelims("V1}V2}V3;V4","*","}"), ListChangedelims(list="V1}V2}V3;V4",newDel="*",oldDel="}"))>

	<cfset assertEquals(0, Len(ListChangedelims("","!",",")) )>
	<cfset assertEquals(0, Len(ListChangedelims(list="",newDel="!",oldDel=",")) )>
	<cfset assertEquals(0, Len(ListChangedelims(newDel="!",list="",oldDel=",")) )>
	<cfset assertEquals(0, Len(ListChangedelims(newDel="!",oldDel=",",list="")) )>
	<cfset assertEquals(0, Len(ListChangedelims(oldDel=",",newDel="!",list="")) )>
	<cfset assertEquals(Len(ListChangedelims("","!",",")), Len(ListChangedelims(list="",newDel="!",oldDel=",")) )>

	</cffunction>

</cfcomponent>