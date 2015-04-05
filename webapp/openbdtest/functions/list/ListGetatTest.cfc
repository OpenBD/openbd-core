<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListFirst">

	<cfset assertEquals("V1" , ListGetat("V1,v1V2,V3,V4",1))>
	<cfset assertEquals(ListGetat("V1,v1V2,V3,V4",1) , ListGetat(list="V1,v1V2,V3,V4",position=1))>
	<cfset assertEquals("V1" , ListGetat(list="V1,v1V2,V3,V4",position=1))>
	<cfset assertEquals("V1" , ListGetat(position=1,list="V1,v1V2,V3,V4"))>

	<cfset assertEquals("V1" , ListGetat("V1;v1V2;V3;V4",1,";"))>
	<cfset assertEquals(ListGetat("V1;v1V2;V3;V4",1,";") , ListGetat(list="V1;v1V2;V3;V4",position=1,delimiter=";"))>
	<cfset assertEquals("V1" , ListGetat(list="V1;v1V2;V3;V4",position=1,delimiter=";"))>
	<cfset assertEquals("V1" , ListGetat(position=1,list="V1;v1V2;V3;V4",delimiter=";"))>
	<cfset assertEquals("V1" , ListGetat(list="V1;v1V2;V3;V4",delimiter=";",position=1))>
	<cfset assertEquals("V1" , ListGetat(delimiter=";",position=1,list="V1;v1V2;V3;V4"))>

	<cfset assertEquals("V1;v1V2;V3;V4" , ListGetat("V1;v1V2;V3;V4",1,"}"))>
	<cfset assertEquals("V1;v1V2;V3;V4" , ListGetat(list="V1;v1V2;V3;V4",position=1,delimiter="}"))>
	<cfset assertEquals(ListGetat("V1;v1V2;V3;V4",1,"}") , ListGetat(list="V1;v1V2;V3;V4",position=1,delimiter="}"))>
	<cfset assertEquals("V1;v1V2;V3;V4" , ListGetat(position=1,list="V1;v1V2;V3;V4",delimiter="}"))>
	<cfset assertEquals("V1;v1V2;V3;V4" , ListGetat(position=1,delimiter="}",list="V1;v1V2;V3;V4"))>
	<cfset assertEquals("V1;v1V2;V3;V4" , ListGetat(delimiter="}",position=1,list="V1;v1V2;V3;V4"))>

	<cfset assertEquals("V4" , ListGetat("V1;v1V2;V3;V4",4,";"))>
	<cfset assertEquals(ListGetat("V1;v1V2;V3;V4",4,";") , ListGetat(list="V1;v1V2;V3;V4",position=4,delimiter=";"))>
	<cfset assertEquals("V4" , ListGetat(list="V1;v1V2;V3;V4",position=4,delimiter=";"))>
	<cfset assertEquals("V4" , ListGetat(position=4,list="V1;v1V2;V3;V4",delimiter=";"))>
	<cfset assertEquals("V4" , ListGetat(delimiter=";",position=4,list="V1;v1V2;V3;V4"))>


	<cfset assertEquals("V4" , ListGetat("V1,v1V2,V3,V4",4))>
	<cfset assertEquals(ListGetat("V1,v1V2,V3,V4",4) , ListGetat(list="V1,v1V2,V3,V4",position=4))>
	<cfset assertEquals("V4" , ListGetat(position=4,list="V1,v1V2,V3,V4"))>

	<cfset assertEquals("v1V2" , ListGetat("V1,v1V2,V3,V4",2))>
	<cfset assertEquals(ListGetat("V1,v1V2,V3,V4",2) , ListGetat(list="V1,v1V2,V3,V4",position=2))>
	<cfset assertEquals("v1V2" , ListGetat(position=2,list="V1,v1V2,V3,V4"))>

	</cffunction>

</cfcomponent>