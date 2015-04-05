<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testContians">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertFalse(ArrayContains(numbers, "ONE"))>		
		<cfset AssertTrue(ArrayContains(numbers, "one"))>		
	</cffunction>


	<cffunction name="testNamedContains">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertFalse(ArrayContains(array=numbers, element="ONE"))>		
		<cfset AssertTrue(ArrayContains(array=numbers, element="one"))>		
	</cffunction>
		
</cfcomponent>