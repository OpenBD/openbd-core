<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testArraySort">
		
		<cfset testarray = ["one", "two", "three", "four"]>
		
		<!--- test ascending sort --->
		<cfset ArraySort(testarray, "text", "asc")>
		<cfset AssertEquals("four", ArrayGet(testarray, 1))>
		<cfset AssertEquals("one", ArrayGet(testarray, 2))>
		
		<!--- test descending sort --->
		<cfset ArraySort(testarray, "text", "desc")>
		<cfset AssertEquals("two", ArrayGet(testarray, 1))>
		<cfset AssertEquals("three", ArrayGet(testarray, 2))>

	</cffunction>


	<cffunction name="testNamedArraySort">
		<cfset testarray = ["one", "two", "three", "four"]>
		
		<!--- test ascending sort --->
		<cfset ArraySort(array=testarray, type="text", order="asc")>
		<cfset AssertEquals("four", ArrayGet(testarray, 1))>
		<cfset AssertEquals("one", ArrayGet(testarray, 2))>
		
		<!--- test descending sort --->
		<cfset ArraySort(array=testarray, type="text", order="desc")>
		<cfset AssertEquals("two", ArrayGet(testarray, 1))>
		<cfset AssertEquals("three", ArrayGet(testarray, 2))>

	</cffunction>

</cfcomponent>