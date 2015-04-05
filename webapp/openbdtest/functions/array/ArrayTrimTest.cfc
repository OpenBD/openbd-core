<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testArrayTrim">		
		<cfset testarray = [1, 2, 3, 4]>
		<cfset success = ArrayTrim(testarray, 2)>
		<cfset AssertTrue(success)>
		<cfset AssertFalse(ArrayIndexExists(testarray, 3))>
		<cfset AssertEquals(1, ArrayGet(testarray, 1))>
		<cfset AssertEquals(2, ArrayGet(testarray, 2))>
	</cffunction>
	

	<cffunction name="testNamedArrayTrim">
		<cfset testarray = [1, 2, 3, 4]>
		<cfset success = ArrayTrim(array=testarray, size=2)>
		<cfset AssertTrue(success)>		
		<cfset AssertFalse(ArrayIndexExists(testarray, 3))>
		<cfset AssertEquals(1, ArrayGet(testarray, 1))>
		<cfset AssertEquals(2, ArrayGet(testarray, 2))>
	</cffunction>

</cfcomponent>