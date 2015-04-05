<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testFindNoCase">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertEquals(3, ArrayFindNoCase(numbers, "three"))>
		<cfset AssertEquals(1, ArrayFindNoCase(numbers, "ONE"))>
	</cffunction>


	<cffunction name="testNamedFindNoCase">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertEquals(3, ArrayFindNoCase(array=numbers, element="three"))>
		<cfset AssertEquals(1, ArrayFindNoCase(array=numbers, element"ONE"))>
	</cffunction>
		
</cfcomponent>