<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testFirst">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertEquals("one", ArrayFirst(numbers))>
	</cffunction>


	<cffunction name="testNamedFirst">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertEquals("one", ArrayFirst(array=numbers))>
	</cffunction>
		
</cfcomponent>