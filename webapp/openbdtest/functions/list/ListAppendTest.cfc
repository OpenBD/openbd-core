<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingListAppend">

	<cfset myArr=["V1","V2","V3"]>
	<cfset myList=ArrayTolist( myArr, "," ) >
	<cfset myArr=["V1","V2","V3","V4"]>
	<cfset myRes=ArrayTolist( myArr, "," ) >

	<cfset assertEquals(myRes, ListAppend(myList,"V4",","))>
	<cfset assertEquals(myRes, ListAppend(myList,"V4"))>
	<cfset assertEquals(myRes, ListAppend(list=myList,value="V4",delimiter=","))>
	<cfset assertEquals(myRes, ListAppend(value="V4",list=myList,delimiter=","))>
	<cfset assertEquals(myRes, ListAppend(value="V4",delimiter=",",list=myList))>
	<cfset assertEquals(myRes, ListAppend(delimiter=",",value="V4",list=myList))>
	<cfset assertEquals(ListAppend(myList,"V4",","), ListAppend(list=myList,value="V4",delimiter=","))>

	<cfset assertEquals("V1,V2,V3;V4", ListAppend(myList,"V4",";"))>
	<cfset assertEquals("V1,V2,V3;V4", ListAppend(list=myList,value="V4",delimiter=";"))>
	<cfset assertEquals("V1,V2,V3;V4", ListAppend(value="V4",list=myList,delimiter=";"))>
	<cfset assertEquals("V1,V2,V3;V4", ListAppend(value="V4",delimiter=";",list=myList))>
	<cfset assertEquals("V1,V2,V3;V4", ListAppend(delimiter=";",value="V4",list=myList))>
	<cfset assertEquals(ListAppend(delimiter=";",value="V4",list=myList), ListAppend(myList,"V4",";"))>

	<cfset assertEquals("V1,V2,V3V5", ListAppend(myList,"V5",""))>
	<cfset assertEquals("V1,V2,V3V5", ListAppend(list=myList,value="V5",delimiter=""))>
	<cfset assertEquals("V1,V2,V3V5", ListAppend(value="V5",list=myList,delimiter=""))>
	<cfset assertEquals("V1,V2,V3V5", ListAppend(value="V5",delimiter="",list=myList))>
	<cfset assertEquals("V1,V2,V3V5", ListAppend(delimiter="",value="V5",list=myList))>
	<cfset assertEquals(ListAppend(myList,"V5",""), ListAppend(list=myList,value="V5",delimiter=""))>

	<cfset myArr=ArrayNew()>
	<cfset myList=ArrayTolist( myArr, "," ) >
	<cfset assertEquals("D", ListAppend(myList,"D",",") )>
	<cfset assertEquals("D", ListAppend(myList,"D") )>
	<cfset assertEquals("D", ListAppend(list=myList,value="D",delimiter=",") )>
	<cfset assertEquals("D", ListAppend(value="D",list=myList,delimiter=",") )>
	<cfset assertEquals("D", ListAppend(value="D",delimiter=",",list=myList) )>
	<cfset assertEquals("D", ListAppend(delimiter=",",value="D",list=myList) )>
	<cfset assertEquals(ListAppend(myList,"D",","), ListAppend(list=myList,value="D",delimiter=",") )>
	<cfset assertEquals("D", ListAppend(myList,"D",",") )>

	</cffunction>

</cfcomponent>
