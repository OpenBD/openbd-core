<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testKeyList">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">

		<cfset keystring = StructKeyList(person, ",")>
		<cfset AssertEquals("firstname,lastname", keystring)>
	</cffunction>
	
	<cffunction name="testNamedKeyList">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">

		<cfset keystring = StructKeyList(struct=person, delimeter=":")>
		<cfset AssertEquals("firstname:lastname", keystring)>
	</cffunction>
	
</cfcomponent>