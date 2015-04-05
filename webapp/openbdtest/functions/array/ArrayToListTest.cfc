<cfcomponent extends="openbdtest.common.TestCase">
	
	<cffunction name="testArrayToLst">		
		<cfset testarray = [1, 2, 3, 4]>
		<cfset resultlist = ArrayToList(testarray, ",")>
		<cfset AssertEquals("1,2,3,4", resultlist)>
	</cffunction>


	<cffunction name="testNamedArrayToList">
		<cfset testarray = [1, 2, 3, 4]>
		<cfset resultlist = ArrayToList(array=testarray, delimeter=",")>
		<cfset AssertEquals("1,2,3,4", resultlist)>
	</cffunction>

</cfcomponent>