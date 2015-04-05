<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListDeleteat">


	<cfset assertEquals("v0,V1,v1V2,V3,V2", ListDeleteat ("v0,V1,v1V2,V3,V2",0))>
	<cfset assertEquals("v0,V1,v1V2,V3,V2", ListDeleteat (list="v0,V1,v1V2,V3,V2",position=0))>
	<cfset assertEquals("v0,V1,v1V2,V3,V2", ListDeleteat (position=0,list="v0,V1,v1V2,V3,V2"))>
	<cfset assertEquals(ListDeleteat ("v0,V1,v1V2,V3,V2",0), ListDeleteat (position=0,list="v0,V1,v1V2,V3,V2"))>

	<cfset assertEquals("v0,V1,v1V2,V2", ListDeleteat ("v0,V1,v1V2,V3,V2",4))>
	<cfset assertEquals("v0,V1,v1V2,V2", ListDeleteat (list="v0,V1,v1V2,V3,V2",position=4))>
	<cfset assertEquals("v0,V1,v1V2,V2", ListDeleteat (position=4,list="v0,V1,v1V2,V3,V2"))>
	<cfset assertEquals(ListDeleteat ("v0,V1,v1V2,V3,V2",4), ListDeleteat (list="v0,V1,v1V2,V3,V2",position=4))>

	<cfset assertEquals("v0,V1,V3,V2", ListDeleteat ("v0,V1,v1V2,V3,V2",3))>
	<cfset assertEquals("v0,V1,V3,V2", ListDeleteat (list="v0,V1,v1V2,V3,V2",position=3))>
	<cfset assertEquals("v0,V1,V3,V2", ListDeleteat (position=3,list="v0,V1,v1V2,V3,V2"))>
	<cfset assertEquals(ListDeleteat (list="v0,V1,v1V2,V3,V2",position=3), ListDeleteat ("v0,V1,v1V2,V3,V2",3))>

	<cfset assertEquals("v0,V1,v1V2,V3,V2", ListDeleteat ("v0,V1,v1V2,V3,V2",4,"{"))>
	<cfset assertEquals("v0,V1,v1V2,V3,V2", ListDeleteat (list="v0,V1,v1V2,V3,V2",position=4,delimiter="{"))>
	<cfset assertEquals("v0,V1,v1V2,V3,V2", ListDeleteat (position=4,list="v0,V1,v1V2,V3,V2",delimiter="{"))>
	<cfset assertEquals("v0,V1,v1V2,V3,V2", ListDeleteat (position=4,delimiter="{",list="v0,V1,v1V2,V3,V2"))>

	<cfset assertEquals("v0,V1,v1V2,V3,V2", ListDeleteat ("v0,V1,v1V2,V3,V2",5,"{"))>
	<cfset assertEquals("v0,V1,v1V2,V3,V2", ListDeleteat (list="v0,V1,v1V2,V3,V2",position=5,delimiter="{"))>
	<cfset assertEquals("v0,V1,v1V2,V3,V2", ListDeleteat (position=5,list="v0,V1,v1V2,V3,V2",delimiter="{"))>
	<cfset assertEquals("v0,V1,v1V2,V3,V2", ListDeleteat (list="v0,V1,v1V2,V3,V2",delimiter="{",position=5))>
	<cfset assertEquals(ListDeleteat (list="v0,V1,v1V2,V3,V2",position=5,delimiter="{"), ListDeleteat ("v0,V1,v1V2,V3,V2",5,"{"))>

	<cfset assertEquals("v0,V1,v1V2,V3", ListDeleteat ("v0,V1,v1V2,V3,V2",5))>
	<cfset assertEquals("v0,V1,v1V2,V3", ListDeleteat (list="v0,V1,v1V2,V3,V2",position=5))>
	<cfset assertEquals("v0,V1,v1V2,V3", ListDeleteat (position=5,list="v0,V1,v1V2,V3,V2"))>
	<cfset assertEquals(ListDeleteat ("v0,V1,v1V2,V3,V2",5), ListDeleteat (position=5,list="v0,V1,v1V2,V3,V2"))>

	</cffunction>

</cfcomponent>