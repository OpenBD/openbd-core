<cfcomponent extends="openbdtest.common.TestCase">

<cffunction name="testMX">

	<cfscript>
	var records	= IpGetMxRecords("google.com", "8.8.8.8");
	Console( records );
	</cfscript>


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