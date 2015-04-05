<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListQualify">

		<cfset assertEquals("?V1,v1V2,V3,V4?" , ListQualify("V1,v1V2,V3,V4","?","!"))>
		<cfset assertEquals(ListQualify("V1,v1V2,V3,V4","?","!") , ListQualify(list="V1,v1V2,V3,V4",qualifier="?",delimiter="!"))>
		<cfset assertEquals("?V1,v1V2,V3,V4?" , ListQualify(qualifier="?",delimiter="!",list="V1,v1V2,V3,V4"))>
		<cfset assertEquals("?V1,v1V2,V3,V4?" , ListQualify(qualifier="?",list="V1,v1V2,V3,V4",delimiter="!"))>
		<cfset assertEquals("?V1,v1V2,V3,V4?" , ListQualify(qualifier="?",delimiter="!",list="V1,v1V2,V3,V4"))>
		<cfset assertEquals("?V1,v1V2,V3,V4?" , ListQualify(delimiter="!",qualifier="?",list="V1,v1V2,V3,V4"))>
		<cfset assertEquals("?V1,v1V2,V3,V4?" , ListQualify(delimiter="!",list="V1,v1V2,V3,V4",qualifier="?"))>

		<cfset assertEquals(";V1;,;v1V2;,;V3;,;V4;" , ListQualify("V1,v1V2,V3,V4",";"))>
		<cfset assertEquals(ListQualify("V1,v1V2,V3,V4",";") , ListQualify(list="V1,v1V2,V3,V4",qualifier=";"))>
		<cfset assertEquals(";V1;,;v1V2;,;V3;,;V4;" , ListQualify(qualifier=";",list="V1,v1V2,V3,V4"))>

		<cfset assertEquals("!v1,v1v2,v3,v4!" , ListQualify("V1,v1V2,V3,V4","!",";","CHAR"))>
		<cfset assertEquals(ListQualify("V1,v1V2,V3,V4","!",";","CHAR") , ListQualify(list="V1,v1V2,V3,V4",qualifier="!",delimiter=";",type="CHAR"))>
		<cfset assertEquals("!v1,v1v2,v3,v4!" , ListQualify(list="V1,v1V2,V3,V4",qualifier="!",delimiter=";",type="CHAR"))>
		<cfset assertEquals("!v1,v1v2,v3,v4!" , ListQualify(list="V1,v1V2,V3,V4",qualifier="!",type="CHAR",delimiter=";"))>
		<cfset assertEquals("!v1,v1v2,v3,v4!" , ListQualify(list="V1,v1V2,V3,V4",type="CHAR",qualifier="!",delimiter=";"))>
		<cfset assertEquals("!v1,v1v2,v3,v4!" , ListQualify(type="CHAR",list="V1,v1V2,V3,V4",qualifier="!",delimiter=";"))>

		<cfset assertEquals("!v1,v1v2,v3,v4!" , ListQualify("V1,v1V2,V3,V4","!",";","ALL"))>
		<cfset assertEquals(ListQualify("V1,v1V2,V3,V4","!",";","ALL") , ListQualify(list="V1,v1V2,V3,V4",qualifier="!",delimiter=";",type="ALL"))>
		<cfset assertEquals("!v1,v1v2,v3,v4!" , ListQualify(list="V1,v1V2,V3,V4",qualifier="!",delimiter=";",type="ALL"))>
		<cfset assertEquals("!v1,v1v2,v3,v4!" , ListQualify(list="V1,v1V2,V3,V4",qualifier="!",type="ALL",delimiter=";"))>
		<cfset assertEquals("!v1,v1v2,v3,v4!" , ListQualify(list="V1,v1V2,V3,V4",type="ALL",qualifier="!",delimiter=";"))>
		<cfset assertEquals("!v1,v1v2,v3,v4!" , ListQualify(type="ALL",list="V1,v1V2,V3,V4",qualifier="!",delimiter=";"))>

		<cfset assertEquals("!v1!;!v1v2!;!v3!;!v4!" , ListQualify("V1;v1V2;V3;V4","!",";","ALL"))>
		<cfset assertEquals(ListQualify("V1;v1V2;V3;V4","!",";","ALL") , ListQualify(list="V1;v1V2;V3;V4",qualifier="!",delimiter=";",type="ALL"))>

		<cfset assertEquals("!v1!;!v1v2!;!v3!;!v4!" , ListQualify("V1;v1V2;V3;V4","!",";","CHAR"))>
		<cfset assertEquals(ListQualify("V1;v1V2;V3;V4","!",";","CHAR") , ListQualify(list="V1;v1V2;V3;V4",qualifier="!",delimiter=";",type="CHAR"))>

	</cffunction>

</cfcomponent>