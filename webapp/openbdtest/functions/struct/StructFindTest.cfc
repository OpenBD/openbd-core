<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testFind">

		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
						
		<cfset AssertEquals("Firstname", StructFind(person, "firstname"))>

	</cffunction>

	<cffunction name="testNamedFind">

		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
						
		<cfset AssertEquals("Firstname", StructFind(struct=person, key="firstname"))>

	</cffunction>
		
</cfcomponent>