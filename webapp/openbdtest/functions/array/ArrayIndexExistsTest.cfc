<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testArrayIndexExists">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertTrue(ArrayIndexExists(numbers, 1))>
		<cfset AssertFalse(ArrayIndexExists(numbers, 8))>
	</cffunction>


	<cffunction name="testNamedArrayIndexExists">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertTrue(ArrayIndexExists(array=numbers, index=1))>
		<cfset AssertFalse(ArrayIndexExists(array=numbers, index=8))>
	</cffunction>
		
</cfcomponent>