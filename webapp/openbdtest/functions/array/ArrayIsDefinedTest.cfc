<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testArrayIsDefined">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset AssertTrue(ArrayIsDefined(numbers, 1))>
		<cfset AssertFalse(ArrayIsDefined(numbers, 10))>
	</cffunction>


	<cffunction name="testNamedArrayIsDefined">
		<cfset numbers = ["one", "two", "three", "four"]>		
		<cfset AssertTrue(ArrayIsDefined(array=numbers, index=1))>
		<cfset AssertFalse(ArrayIsDefined(array=numbers, index=10))>
	</cffunction>
	
</cfcomponent>
