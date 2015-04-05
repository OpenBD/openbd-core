<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testInsert">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
		<cfset AssertFalse(StructKeyExists(person, "age"))>
		
		<cfset StructInsert(person, "age", "10")>
		<cfset AssertTrue(StructKeyExists(person, "age"))>
	</cffunction>
	
	<cffunction name="testOverwrite">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
		<cfset AssertEquals("Firstname", person.firstname)>
		
		<cfset StructInsert(person, "firstname", "Overwrite", "true")>
		<cfset AssertEquals("Overwrite", person.firstname)>
	</cffunction>

	<cffunction name="testNamedInsert">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
		<cfset AssertFalse(StructKeyExists(person, "age"))>
		
		<cfset StructInsert(struct=person, key="age", value="10")>
		<cfset AssertTrue(StructKeyExists(person, "age"))>
	</cffunction>
	
</cfcomponent>