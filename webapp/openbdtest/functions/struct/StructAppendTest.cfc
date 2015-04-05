<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testAppend">

		<cfset person = StructNew()>
		<cfset person.frstname = "Firstname">
		<cfset person.lastname = "Lastname">
		
		<cfset address = StructNew()>
		<cfset address.street = "address1">
		<cfset address.county = "address2">
		
		<cfset StructAppend(person, address)>
		<cfset AssertEquals("address2", person.county)>

	</cffunction>
	
	
	<cffunction name="testNamedAppend">

		<cfset person = StructNew()>
		<cfset person.frstname = "NamedFirstname">
		<cfset person.lastname = "NamedLastname">
		
		<cfset address = StructNew()>
		<cfset address.street = "named-address1">
		<cfset address.county = "named-address2">
		
		<cfset StructAppend(struct1=person, struct2=address)>
		<cfset AssertEquals("named-address2", person.county)>

	</cffunction>
	
</cfcomponent>