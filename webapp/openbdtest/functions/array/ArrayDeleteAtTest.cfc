<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testDeleteAt">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset ArrayDeleteAt(numbers, 1)>
		<cfset AssertEquals(3, ArrayLen(numbers))>
		<cfset AssertEquals("two", numbers[1])>
	</cffunction>


	<cffunction name="testNamedDeleteAt">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset ArrayDeleteAt(array=numbers, index=1)>
		<cfset AssertEquals(3, ArrayLen(numbers))>
		<cfset AssertEquals("two", numbers[1])>
	</cffunction>
		
</cfcomponent>