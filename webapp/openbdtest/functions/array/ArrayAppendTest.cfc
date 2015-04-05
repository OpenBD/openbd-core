<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testAppend">
		<cfset numbers = ArrayNew(1)>
		<cfset AssertEquals(0, ArrayLen(numbers))>
		
		<cfset ArrayAppend(numbers, "one")>
		<cfset AssertEquals(1, ArrayLen(numbers))>
		<cfset AssertEquals("one", numbers[1])>
	</cffunction>


	<cffunction name="testNamedAppend">
		<cfset numbers = ArrayNew(1)>
		<cfset AssertEquals(0, ArrayLen(numbers))>
		
		<cfset ArrayAppend(array=numbers, toadd="one")>
		<cfset AssertEquals(1, ArrayLen(numbers))>
		<cfset AssertEquals("one", numbers[1])>
	</cffunction>
		
</cfcomponent>