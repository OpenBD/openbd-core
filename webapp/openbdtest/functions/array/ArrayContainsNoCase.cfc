<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testContainsNoCase">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertTrue(ArrayContainsNoCase(numbers, "ONE"))>		
		<cfset AssertTrue(ArrayContainsNoCase(numbers, "one"))>		
	</cffunction>


	<cffunction name="testNamedContainsNoCase">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertTrue(ArrayContainsNoCase(array=numbers, element="ONE"))>		
		<cfset AssertTrue(ArrayContainsNoCase(array=numbers, element="one"))>		
	</cffunction>
		
</cfcomponent>