<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testCopy">

		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
						
		<cfset person2 = StructCopy(person)>
		
		<cfset AssertEquals("Firstname", person2.firstname)>
		<cfset AssertEquals("Lastname", person2.lastname)>

	</cffunction>
	
	
	<cffunction name="testNamedCopy">

		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
						
		<cfset person2 = StructCopy(struct=person)>
		
		<cfset AssertEquals("Firstname", person2.firstname)>
		<cfset AssertEquals("Lastname", person2.lastname)>

	</cffunction>
	
</cfcomponent>