<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testValueCountNoCase">		
		<cfset testarray = ["one" , "two", "ONE", "one"]>
		<cfset count = ArrayValueCountNoCase(testarray, "one")>
		<cfset AssertEquals(3, count)>
	</cffunction>
	

	<cffunction name="testNamedValueCountNoCase">
		<cfset testarray = ["one" , "two", "ONE", "one"]>
		<cfset count = ArrayValueCountNoCase(array=testarray, element="one")>
		<cfset AssertEquals(3, count)>
	</cffunction>

</cfcomponent>