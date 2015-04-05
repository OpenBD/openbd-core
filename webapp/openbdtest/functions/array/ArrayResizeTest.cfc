<cfcomponent extends="openbdtest.common.TestCase">
	
	<cffunction name="testArrayResize">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertEquals(4, ArrayLen(numbers))>
		<cfset ArrayResize(numbers, 10)>
		<cfset AssertEquals(10, ArrayLen(numbers))>
	</cffunction>


	<cffunction name="testNamedArrayResize">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertEquals(4, ArrayLen(numbers))>
		<cfset ArrayResize(array=numbers, size=10)>
		<cfset AssertEquals(10, ArrayLen(numbers))>
	</cffunction>

</cfcomponent>