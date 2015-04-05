<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testClear">
		<cfset numbers = ArrayNew(1)>
		<cfset AssertEquals(0, ArrayLen(numbers))>
		
		<cfset ArrayAppend(numbers, "one")>
		<cfset AssertEquals(1, ArrayLen(numbers))>
		<cfset AssertEquals("one", numbers[1])>

		<cfset ArrayClear(numbers)>
		<cfset AssertEquals(0, ArrayLen(numbers))>
	</cffunction>


	<cffunction name="testNamedClear">
		<cfset numbers = ArrayNew(1)>
		<cfset AssertEquals(0, ArrayLen(numbers))>
		
		<cfset ArrayAppend(array=numbers, toadd="one")>
		<cfset AssertEquals(1, ArrayLen(numbers))>
		<cfset AssertEquals("one", numbers[1])>

		<cfset ArrayClear(array=numbers)>
		<cfset AssertEquals(0, ArrayLen(numbers))>

	</cffunction>
		
</cfcomponent>