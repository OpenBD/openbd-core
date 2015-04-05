<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListSetat">

		<cfset assertEquals("V1,vs,V3,V4" , ListSetat("V1,v1V2,V3,V4",2,"vs"))>
		<cfset assertEquals("V1,vs,V3,V4" , ListSetat(list="V1,v1V2,V3,V4",position=2,element="vs"))>
		<cfset assertEquals("V1,vs,V3,V4" , ListSetat(list="V1,v1V2,V3,V4",element="vs",position=2))>
		<cfset assertEquals("V1,vs,V3,V4" , ListSetat(position=2,list="V1,v1V2,V3,V4",element="vs"))>
		<cfset assertEquals("V1,vs,V3,V4" , ListSetat(position=2,element="vs",list="V1,v1V2,V3,V4"))>
		<cfset assertEquals("V1,vs,V3,V4" , ListSetat(element="vs",position=2,list="V1,v1V2,V3,V4"))>
		<cfset assertEquals("V1,vs,V3,V4" , ListSetat(element="vs",list="V1,v1V2,V3,V4",position=2))>

		<cfset assertEquals("vz" , ListSetat("V1,v1V2,V3,V4",1,"vz",";"))>
		<cfset assertEquals("vz" , ListSetat(list="V1,v1V2,V3,V4",position=1,element="vz",delimiter=";"))>
		<cfset assertEquals("vz" , ListSetat(list="V1,v1V2,V3,V4",element="vz",position=1,delimiter=";"))>
		<cfset assertEquals("vz" , ListSetat(list="V1,v1V2,V3,V4",element="vz",delimiter=";",position=1))>
		<cfset assertEquals("vz" , ListSetat(position=1,list="V1,v1V2,V3,V4",element="vz",delimiter=";"))>

		<cfset assertEquals("vz;v1V2;V3;V4" , ListSetat("V1;v1V2;V3;V4",1,"vz",";"))>
		<cfset assertEquals("vz;v1V2;V3;V4" , ListSetat(list="V1;v1V2;V3;V4",position=1,element="vz",delimiter=";"))>
		<cfset assertEquals("vz;v1V2;V3;V4" , ListSetat(position=1,list="V1;v1V2;V3;V4",element="vz",delimiter=";"))>
		<cfset assertEquals("vz;v1V2;V3;V4" , ListSetat(list="V1;v1V2;V3;V4",element="vz",position=1,delimiter=";"))>
		<cfset assertEquals("vz;v1V2;V3;V4" , ListSetat(list="V1;v1V2;V3;V4",element="vz",delimiter=";",position=1))>

		<cfset assertEquals("V1;v1V2;V3;vy" , ListSetat("V1;v1V2;V3;V4",4,"vy",";"))>
		<cfset assertEquals("V1;v1V2;V3;vy" , ListSetat(list="V1;v1V2;V3;V4",position=4,element="vy",delimiter=";"))>
		<cfset assertEquals("V1;v1V2;V3;vy" , ListSetat(position=4,list="V1;v1V2;V3;V4",element="vy",delimiter=";"))>
		<cfset assertEquals("V1;v1V2;V3;vy" , ListSetat(list="V1;v1V2;V3;V4",position=4,element="vy",delimiter=";"))>
		<cfset assertEquals("V1;v1V2;V3;vy" , ListSetat(list="V1;v1V2;V3;V4",element="vy",position=4,delimiter=";"))>
		<cfset assertEquals("V1;v1V2;V3;vy" , ListSetat(list="V1;v1V2;V3;V4",element="vy",delimiter=";",position=4))>

	</cffunction>

</cfcomponent>

