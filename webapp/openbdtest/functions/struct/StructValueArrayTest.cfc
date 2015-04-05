<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testValueArray">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">

		<cfset valuearray = StructKeyArray(person)>
		<cfset AssertEquals("Firstname", valuearray[1])>
		<cfset AssertEquals("Lastname", valuearray[2])>
	</cffunction>
	
	<cffunction name="testNamedValueArray">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">

		<cfset valuearray = StructValueArray(struct=person)>
		<cfset AssertEquals("Firstname", valuearray[1])>
		<cfset AssertEquals("Lastname", valuearray[2])>
	</cffunction>
	
</cfcomponent>