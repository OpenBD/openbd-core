<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListItemtrim">

		<cfset assertEquals("V1,v1V2,V3,V4" ,  ListItemtrim("V1 , v1V2 ,V3,V4"))>
		<cfset assertEquals("V1,v1V2,V3,V4" ,  ListItemtrim(list="V1 , v1V2 ,V3,V4"))>

		<cfset assertEquals("V1 , v1V2 ,V3,V4",ListItemtrim(list="V1 , v1V2 ,V3,V4",delimiter=";"))>
		<cfset assertEquals(ListItemtrim("V1 , v1V2 ,V3,V4",";"),ListItemtrim(list="V1 , v1V2 ,V3,V4",delimiter=";"))>
		<cfset assertEquals("V1 , v1V2 ,V3,V4",ListItemtrim(delimiter=";",list="V1 , v1V2 ,V3,V4"))>

		<cfset assertEquals("V1,v1V2,V3,V4" ,  ListItemtrim(",V1 , v1V2 ,V3,V4"))>
		<cfset assertEquals("V1,v1V2,V3,V4" ,  ListItemtrim(list=",V1 , v1V2 ,V3,V4"))>

		<cfset assertEquals("V1,v1V2,V3,V4" ,  ListItemtrim(",V1 , v1V2 ,V3,,V4 "))>
		<cfset assertEquals("V1,v1V2,V3,V4" ,  ListItemtrim(list=",V1 , v1V2 ,V3,,V4 "))>

		<cfset assertEquals("V1;v1V2;V3;V4" ,  ListItemtrim(list=";V1 ; v1V2 ;V3;;V4 ", delimiter=";"))>
		<cfset assertEquals(ListItemtrim(";V1 ; v1V2 ;V3;;V4 ", ";") ,  ListItemtrim(list=";V1 ; v1V2 ;V3;;V4 ", delimiter=";"))>
		<cfset assertEquals("V1;v1V2;V3;V4" ,  ListItemtrim(delimiter=";",list=";V1 ; v1V2 ;V3;;V4 "))>

	</cffunction>

</cfcomponent>