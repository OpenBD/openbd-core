<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testDelete">

		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
						
		<cfset StructDelete(person, "lastname")>
		
		<cfset AssertEquals(1, StructCount(person))>
		<cfset AssertEquals("Firstname", person.firstname)>

	</cffunction>

	<cffunction name="testNamedDelete">

		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
						
		<cfset StructDelete(struct=person, key="lastname")>
		
		<cfset AssertEquals(1, StructCount(struct=person))>
		<cfset AssertEquals("Firstname", person.firstname)>

	</cffunction>
		
</cfcomponent>