<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testClear">

		<cfset person = StructNew()>
		<cfset person.frstname = "Firstname">
		<cfset person.lastname = "Lastname">
		
		<!--- Check the structure is not empty --->
		<cfset AssertFalse(StructIsEmpty(person))>
				
		<cfset StructClear(person)>
		<!--- Check the structure is now empty --->
		<cfset AssertTrue(StructIsEmpty(person))>

	</cffunction>
	
	
	<cffunction name="testNamedClear">

		<cfset person = StructNew()>
		<cfset person.frstname = "Firstname">
		<cfset person.lastname = "Lastname">
		
		<!--- Check the structure is not empty --->
		<cfset AssertFalse(StructIsEmpty(struct=person))>
				
		<cfset StructClear(struct=person)>
		<!--- Check the structure is now empty --->
		<cfset AssertTrue(StructIsEmpty(struct=person))>

	</cffunction>
	
</cfcomponent>