<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testUpdate">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">

		<cfset AssertEquals("firstname", person.firstname)>
		<cfset StructUpdate(person, "firstname", "update")>
		<cfset AssertEquals("update", person.firstname)>
	</cffunction>
	
	<cffunction name="testNamedUpdate">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">

		<cfset AssertEquals("firstname", person.firstname)>
		<cfset StructUpdate(struct=person, key="firstname", value="update")>
		<cfset AssertEquals("update", person.firstname)>		
	</cffunction>
	
</cfcomponent>