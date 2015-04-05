<cfcomponent extends="openbdtest.common.TestCase">
	
	<cffunction name="testArrayPrepend">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset ArrayPrepend(numbers, "inserted")>
		<cfset AssertEquals("inserted", ArrayGet(numbers, 1))>
		<cfset AssertEquals("one", ArrayGet(numbers, 2))>
	</cffunction>


	<cffunction name="testNamedArrayPrepend">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset ArrayPrepend(array=numbers, toadd="inserted")>
		<cfset AssertEquals("inserted", ArrayGet(numbers, 1))>
		<cfset AssertEquals("one", ArrayGet(numbers, 2))>
	</cffunction>

</cfcomponent>