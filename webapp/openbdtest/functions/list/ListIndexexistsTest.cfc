<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListIndexexists">

		<cfset assertEquals("yes" , ListIndexexists("V1,v1V2,V3,V4",1))>
		<cfset assertEquals(ListIndexexists("V1,v1V2,V3,V4",1) , ListIndexexists(list="V1,v1V2,V3,V4",position=1))>
		<cfset assertEquals("yes" , ListIndexexists(list="V1,v1V2,V3,V4",position=1))>
		<cfset assertEquals("yes" , ListIndexexists(position=1,list="V1,v1V2,V3,V4"))>

		<cfset assertEquals("no" , ListIndexexists(list="V1,v1V2,V3,V4",position=2,delimiter=";"))>
		<cfset assertEquals(ListIndexexists("V1,v1V2,V3,V4",2,";") , ListIndexexists(list="V1,v1V2,V3,V4",position=2,delimiter=";"))>
		<cfset assertEquals("no" , ListIndexexists(position=2,list="V1,v1V2,V3,V4",delimiter=";"))>
		<cfset assertEquals("no" , ListIndexexists(list="V1,v1V2,V3,V4",delimiter=";",position=2))>
		<cfset assertEquals("no" , ListIndexexists(delimiter=";",position=2,list="V1,v1V2,V3,V4"))>

		<cfset assertEquals("no" , ListIndexexists("V1,v1V2,V3,V4",5))>
		<cfset assertEquals("no" , ListIndexexists(list="V1,v1V2,V3,V4",position=5))>
		<cfset assertEquals("no" , ListIndexexists(position=5,list="V1,v1V2,V3,V4"))>

	  <cfset assertEquals("yes" , ListIndexexists("V1,v1V2,V3,V4",3))>
		<cfset assertEquals("yes" , ListIndexexists(list="V1,v1V2,V3,V4",position=3))>
		<cfset assertEquals("yes" , ListIndexexists(position=3,list="V1,v1V2,V3,V4"))>

	</cffunction>

</cfcomponent>