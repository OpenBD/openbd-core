<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testGetAt">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertEquals("one", ArrayGet(numbers, 1))>
	</cffunction>


	<cffunction name="testNamedGetAt">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertEquals("one", ArrayGet(array=numbers, index=1))>
	</cffunction>
		
</cfcomponent>