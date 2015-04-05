<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testConcat">
		<cfset array1 = ["one"]>
		<cfset array2 = ["two", "three", "four"]>
		<cfset ArrayConcat(array1, array2)>
		<cfset AssertEquals(4, ArrayLen(array1))>		
	</cffunction>


	<cffunction name="testNamedConcat">
		<cfset array1 = ["one"]>
		<cfset array2 = ["two", "three", "four"]>
		<cfset ArrayConcat(array1=array1, array2=array2)>
		<cfset AssertEquals(4, ArrayLen(array1))>
	</cffunction>
		
</cfcomponent>