<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testCount">

		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
						
		<cfset elements = StructCount(person)>
		
		<cfset AssertEquals(2, elements)>

	</cffunction>

	<cffunction name="testNamedCount">

		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
		<cfset person.age = "10">
							
		<cfset elements = StructCount(struct=person)>
		
		<cfset AssertEquals(3, elements)>

	</cffunction>
		
</cfcomponent>