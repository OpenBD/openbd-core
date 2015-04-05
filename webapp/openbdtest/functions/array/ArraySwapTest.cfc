<cfcomponent extends="openbdtest.common.TestCase">
	
	<cffunction name="testArraySwap">		
		<cfset testarray = ["one", "two", "three", "four"]>
		<cfset ArraySwap(testarray, 2, 3)>
		<cfset AssertEquals("two", ArrayGet(testarray, 3))>
		<cfset AssertEquals("three", ArrayGet(testarray, 2))>
	</cffunction>


	<cffunction name="testNamedArraySwap">
		<cfset testarray = ["one", "two", "three", "four"]>
		<cfset ArraySwap(array=testarray, index1=2, index2=3)>
		<cfset AssertEquals("two", ArrayGet(testarray, 3))>
		<cfset AssertEquals("three", ArrayGet(testarray, 2))>
	</cffunction>

</cfcomponent>