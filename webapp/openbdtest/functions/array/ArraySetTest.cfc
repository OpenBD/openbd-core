<cfcomponent extends="openbdtest.common.TestCase">
	
	<cffunction name="testArraySet">
		<cfset testarray = ArrayNew(1)>
		<cfset ArraySet(testarray, 1, 4, "test")>
		<cfset AssertTrue(ArrayLen(testarray))>
	</cffunction>


	<cffunction name="testNamedArraySet">
		<cfset testarray = ArrayNew(1)>
		<cfset ArraySet(array=testarray, start=1, end=4, value="test")>
		<cfset AssertTrue(ArrayLen(testarray))>
	</cffunction>

</cfcomponent>