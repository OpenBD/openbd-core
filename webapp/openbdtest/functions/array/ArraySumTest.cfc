<cfcomponent extends="openbdtest.common.TestCase">
	
	<cffunction name="testArraySum">		
		<cfset testarray = [1, 2, 3, 4]>
		<cfset AssertEquals(10, ArraySum(testarray))>
	</cffunction>


	<cffunction name="testNamedArraySum">
		<cfset testarray = [1, 2, 3, 4]>
		<cfset AssertEquals(10, ArraySum(array=testarray))>
	</cffunction>

</cfcomponent>