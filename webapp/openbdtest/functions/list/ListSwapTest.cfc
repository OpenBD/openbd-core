<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListSwap">

		<cfset assertEquals("v1V2,V1,V3,V4" , ListSwap("V1,v1V2,V3,V4",2,1))>
		<cfset assertEquals("v1V2,V1,V3,V4" , ListSwap(list="V1,v1V2,V3,V4",position1=2,position2=1))>
		<cfset assertEquals("v1V2,V1,V3,V4" , ListSwap(list="V1,v1V2,V3,V4",position2=1,position1=2))>
		<cfset assertEquals("v1V2,V1,V3,V4" , ListSwap(position2=1,list="V1,v1V2,V3,V4",position1=2))>
		<cfset assertEquals("v1V2,V1,V3,V4" , ListSwap(position2=1,position1=2,list="V1,v1V2,V3,V4"))>
		<cfset assertEquals("v1V2,V1,V3,V4" , ListSwap(position1=2,position2=1,list="V1,v1V2,V3,V4"))>
		<cfset assertEquals("v1V2,V1,V3,V4" , ListSwap(position1=2,list="V1,v1V2,V3,V4",position2=1))>

		<cfset assertEquals("v1V2;V1;V3;V4" , ListSwap("V1;v1V2;V3;V4",2,1,";"))>
		<cfset assertEquals("v1V2;V1;V3;V4" , ListSwap(list="V1;v1V2;V3;V4",position1=2,position2=1,delimiter=";"))>
		<cfset assertEquals("v1V2;V1;V3;V4" , ListSwap(list="V1;v1V2;V3;V4",position1=2,delimiter=";",position2=1))>
		<cfset assertEquals("v1V2;V1;V3;V4" , ListSwap(list="V1;v1V2;V3;V4",delimiter=";",position1=2,position2=1))>

		<cfset assertEquals("V4;v1V2;V3;V1" , ListSwap("V1;v1V2;V3;V4",1,4,";"))>
		<cfset assertEquals("V4;v1V2;V3;V1" , ListSwap(list="V1;v1V2;V3;V4",position1=1,position2=4,delimiter=";"))>

		<cfset assertEquals("V1;v1V2;V4;V3" , ListSwap("V1;v1V2;V3;V4",3,4,";"))>
		<cfset assertEquals("V1;v1V2;V4;V3" , ListSwap(list="V1;v1V2;V3;V4",position1=3,position2=4,delimiter=";"))>

		<cfset assertEquals("V1;V3;v1V2;V4" , ListSwap("V1;v1V2;V3;V4",3,2,";"))>
		<cfset assertEquals("V1;V3;v1V2;V4" , ListSwap(list="V1;v1V2;V3;V4",position1=3,position2=2,delimiter=";"))>

	</cffunction>

</cfcomponent>