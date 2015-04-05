<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListinsertat">

		<cfset assertEquals("V0,V1,v1V2,V3,V4" , Listinsertat("V1,v1V2,V3,V4",1,"V0"))>
		<cfset assertEquals("V0,V1,v1V2,V3,V4" , Listinsertat(list="V1,v1V2,V3,V4",position=1,value="V0"))>
		<cfset assertEquals("V0,V1,v1V2,V3,V4" , Listinsertat(value="V0",list="V1,v1V2,V3,V4",position=1))>
		<cfset assertEquals("V0,V1,v1V2,V3,V4" , Listinsertat(value="V0",position=1,list="V1,v1V2,V3,V4"))>
		<cfset assertEquals("V0,V1,v1V2,V3,V4" , Listinsertat(position=1,value="V0",list="V1,v1V2,V3,V4"))>
		<cfset assertEquals("V0,V1,v1V2,V3,V4" , Listinsertat(position=1,list="V1,v1V2,V3,V4",value="V0"))>

		<cfset assertEquals("V0,V1,v1V2,V3,V4" , Listinsertat(list="V1,v1V2,V3,V4",position=1,value="V0",delimiter=","))>
		<cfset assertEquals(Listinsertat("V1,v1V2,V3,V4",1,"V0",",") , Listinsertat(list="V1,v1V2,V3,V4",position=1,value="V0",delimiter=","))>
		<cfset assertEquals(Listinsertat(list="V1,v1V2,V3,V4",position=1,value="V0") , Listinsertat(list="V1,v1V2,V3,V4",position=1,value="V0",delimiter=","))>

		<cfset assertEquals("V0aV1,v1V2,V3,V4" , Listinsertat(list="V1,v1V2,V3,V4",position=1,value="V0",delimiter="a"))>
		<cfset assertEquals(Listinsertat("V1,v1V2,V3,V4",1,"V0","a") , Listinsertat(list="V1,v1V2,V3,V4",position=1,value="V0",delimiter="a"))>
		<cfset assertEquals("V0aV1,v1V2,V3,V4" , Listinsertat(list="V1,v1V2,V3,V4",delimiter="a",position=1,value="V0"))>
		<cfset assertEquals("V0aV1,v1V2,V3,V4" , Listinsertat(list="V1,v1V2,V3,V4",delimiter="a",value="V0",position=1))>
		<cfset assertEquals("V0aV1,v1V2,V3,V4" , Listinsertat(position=1,list="V1,v1V2,V3,V4",delimiter="a",value="V0"))>

		<cfset assertEquals("V1,v1V2,V3,V5,V4" , Listinsertat("V1,v1V2,V3,V4",4,"V5"))>
		<cfset assertEquals(Listinsertat("V1,v1V2,V3,V4",4,"V5") , Listinsertat(list="V1,v1V2,V3,V4",position=4,value="V5"))>
		<cfset assertEquals(Listinsertat("V1,v1V2,V3,V4",4,"V5") , Listinsertat(list="V1,v1V2,V3,V4",value="V5",position=4))>
		<cfset assertEquals(Listinsertat("V1,v1V2,V3,V4",4,"V5") , Listinsertat(position=4,list="V1,v1V2,V3,V4",value="V5"))>
		<cfset assertEquals(Listinsertat("V1,v1V2,V3,V4",4,"V5") , Listinsertat(position=4,value="V5",list="V1,v1V2,V3,V4"))>
		<cfset assertEquals(Listinsertat("V1,v1V2,V3,V4",4,"V5") , Listinsertat(value="V5",position=4,list="V1,v1V2,V3,V4"))>
		<cfset assertEquals(Listinsertat("V1,v1V2,V3,V4",4,"V5") , Listinsertat(value="V5",list="V1,v1V2,V3,V4",position=4))>

	  <cfset assertEquals("V1,v1V2,v1V2V3,V3,V4" , Listinsertat("V1,v1V2,V3,V4",3,"v1V2V3"))>
	  <cfset assertEquals(Listinsertat("V1,v1V2,V3,V4",3,"v1V2V3") , Listinsertat(list="V1,v1V2,V3,V4",position=3,value="v1V2V3"))>

	</cffunction>

</cfcomponent>