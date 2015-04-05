<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testArrayTrim">		
		<cfset testarray = [" 1", "2 ", "  3  ", "  4"]>
		<cfset success = ArrayTrimValue(testarray)>
		<cfset AssertTrue(success)>
		<cfset AssertEquals("1", ArrayGet(testarray, 1))>
		<cfset AssertEquals("2", ArrayGet(testarray, 2))>
	</cffunction>

</cfcomponent>