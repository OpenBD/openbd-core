<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testFind">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertEquals(3, ArrayFind(numbers, "three"))>
		<cfset AssertEquals(0, ArrayFind(numbers, "ONE"))>
	</cffunction>


	<cffunction name="testNamedFind">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertEquals(3, ArrayFind(array=numbers, element="three"))>
		<cfset AssertEquals(0, ArrayFind(array=numbers, element="ONE"))>
	</cffunction>
		
</cfcomponent>