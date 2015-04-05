<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testArrayLast">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertEquals("four", ArrayLast(numbers))>
	</cffunction>


	<cffunction name="testNamedArrayLast">
		<cfset numbers = ["one", "two", "three", "four"]>		
		<cfset AssertEquals("four", ArrayLast(array=numbers))>
	</cffunction>
	
</cfcomponent>