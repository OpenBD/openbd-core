<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListToarray">

		<cfset assertEquals(["V1","V2"] , ListToarray("V1,V2"))>
		<cfset assertEquals(["V1","V2"] , ListToarray(list="V1,V2"))>

		<cfset assertEquals(["V1","V2",""] , ListToarray("V1,V2,",",",true))>
		<cfset assertEquals(["V1","V2",""] , ListToarray(list="V1,V2,",delimiter=",",flag=true))>
		<cfset assertEquals(["V1","V2",""] , ListToarray(list="V1,V2,",flag=true,delimiter=","))>
		<cfset assertEquals(["V1","V2",""] , ListToarray(flag=true,list="V1,V2,",delimiter=","))>
		<cfset assertEquals(["V1","V2",""] , ListToarray(flag=true,delimiter=",",list="V1,V2,"))>
		<cfset assertEquals(["V1","V2",""] , ListToarray(delimiter=",",flag=true,list="V1,V2,"))>
		<cfset assertEquals(["V1","V2",""] , ListToarray(delimiter=",",list="V1,V2,",flag=true))>

		<cfset assertEquals(["V1","V2",""] , ListToarray("V1;V2;",";",true))>
		<cfset assertEquals(["V1","V2",""] , ListToarray(list="V1;V2;",delimiter=";",flag=true))>

		<cfset assertEquals(["V1","V2"] , ListToarray("V1;V2;",";",false))>
		<cfset assertEquals(["V1","V2"] , ListToarray(list="V1;V2;",delimiter=";",flag=false))>

		<cfset assertEquals(["V1;V2;"] , ListToarray("V1;V2;","{"))>
		<cfset assertEquals(["V1;V2;"] , ListToarray(list="V1;V2;","{"))>

	</cffunction>

</cfcomponent>
