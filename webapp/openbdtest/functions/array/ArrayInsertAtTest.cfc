<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testArrayInsertAt">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset ArrayInsertAt(numbers, 2, "inserted")>
		<cfset AssertEquals("inserted", ArrayGet(numbers, 2))>		
		<cfset AssertEquals("two", ArrayGet(numbers, 3))>		
		<cfset AssertEquals("one", ArrayGet(numbers, 1))>		
	</cffunction>


	<cffunction name="testNamedArrayInsertAt">
		<cfset numbers = ["one", "two", "three", "four"]>
		<cfset ArrayInsertAt(array=numbers, index=2, data="inserted")>
		<cfset AssertEquals("inserted", ArrayGet(array=numbers, index=2))>		
		<cfset AssertEquals("two", ArrayGet(numbers, 3))>		
		<cfset AssertEquals("one", ArrayGet(numbers, 1))>		
	</cffunction>
	
</cfcomponent>
