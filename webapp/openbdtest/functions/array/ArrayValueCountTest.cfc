<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testValueCount">		
		<cfset testarray = ["one" , "two", "ONE", "one"]>
		<cfset count = ArrayValueCount(testarray, "one")>
		<cfset AssertEquals(2, count)>
	</cffunction>
	

	<cffunction name="testNamedValueCount">
		<cfset testarray = ["one" , "two", "ONE", "one"]>
		<cfset count = ArrayValueCount(array=testarray, element="one")>
		<cfset AssertEquals(2, count)>
	</cffunction>


</cfcomponent>