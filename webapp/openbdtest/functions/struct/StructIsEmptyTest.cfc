<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testIsEmpty">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
		<cfset AssertFalse(StructIsEmpty(person))>
		
		<cfset empty=StructNew()>
		<cfset AssertTrue(StructIsEmpty(empty))>

	</cffunction>

	<cffunction name="testNamedIsEmpty">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
		<cfset AssertFalse(StructIsEmpty(struct=person))>

		<cfset empty=StructNew()>
		<cfset AssertTrue(StructIsEmpty(struct=empty))>
	</cffunction>
		
</cfcomponent>