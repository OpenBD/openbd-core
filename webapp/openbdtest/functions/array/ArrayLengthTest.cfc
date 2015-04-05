<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testArrayLength">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertEquals(4, ArrayLen(numbers))>
	</cffunction>


	<cffunction name="testNamedArrayLength">
		<cfset numbers = ["one", "two", "three", "four"]>		
		<cfset AssertEquals(4, ArrayLen(array=numbers))>
	</cffunction>
	
</cfcomponent>