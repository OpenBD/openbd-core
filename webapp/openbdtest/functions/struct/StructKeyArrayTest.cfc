<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testKeyArray">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">

		<cfset keyarray = StructKeyArray(person)>
		<cfset AssertEquals("firstname", keyarray[1])>
		<cfset AssertEquals("lastname", keyarray[2])>
	</cffunction>
	
	<cffunction name="testNamedKeyArray">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">

		<cfset keyarray = StructKeyArray(struct=person)>
		<cfset AssertEquals("firstname", keyarray[1])>
		<cfset AssertEquals("lastname", keyarray[2])>
	</cffunction>
	
</cfcomponent>