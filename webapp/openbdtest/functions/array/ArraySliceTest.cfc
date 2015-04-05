<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testArraySlice">
		<cfset var testarray = ["one", "two", "three", "four"]>
		<cfset var slicearray = ArraySlice(testarray, 1, 2)>
		<cfset AssertTrue(2, ArrayLen(testarray))>
	</cffunction>


	<cffunction name="testNamedArraySlice">
		<cfset var testarray = ["one", "two", "three", "four"]>
		<cfset var slicearray = ArraySlice(array=testarray, index=1, count=2)>
		<cfset AssertTrue(2, ArrayLen(testarray))>
	</cffunction>



	<cffunction name="testNamedArraySlice2">
		<cfset var testarray = [ "one", "two", "three", "four", "five" ]>
		<cfset var slicearray = ArraySlice(testarray,1,3)>
		<cfset AssertEquals( slicearray[1], "one" )>
		<cfset AssertEquals( slicearray[2], "two" )>
		<cfset AssertEquals( slicearray[3], "three" )>
	</cffunction>


</cfcomponent>