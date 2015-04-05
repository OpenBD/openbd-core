<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testKeyExists">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
				
		<cfset AssertTrue(StructKeyExists(person, "firstname"))>
		<cfset AssertFalse(StructKeyExists(person, "invalidkey"))>
	</cffunction>
	
	<cffunction name="testNamedKeyExists">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
				
		<cfset AssertTrue(StructKeyExists(struct=person, key="firstname"))>
		<cfset AssertFalse(StructKeyExists(struct=person, key="invalidkey"))>
	</cffunction>
	
</cfcomponent>