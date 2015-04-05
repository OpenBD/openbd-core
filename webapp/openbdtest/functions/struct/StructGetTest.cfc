<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testGet">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
		<cfset person.sister.firstname = "SisterFirst">
		<cfset person.sister.lastname = "SisterLast">

		<cfset rtnstruct = StructGet("person.sister")>
		<cfset AssertEquals("SisterFirst", rtnstruct.firstname)>
		<cfset AssertEquals("SisterLast", rtnstruct.lastname)>
	</cffunction>

	<cffunction name="testNamedGet">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
		<cfset person.sister.firstname = "SisterFirst">
		<cfset person.sister.lastname = "SisterLast">

		<cfset rtnstruct = StructGet(keypath="person.sister")>
		<cfset AssertEquals("SisterFirst", rtnstruct.firstname)>
		<cfset AssertEquals("SisterLast", rtnstruct.lastname)>
	</cffunction>
		
</cfcomponent>