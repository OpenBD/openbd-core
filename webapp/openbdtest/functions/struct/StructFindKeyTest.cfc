<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testFindKey">

		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
						
		<cfset firstfind = StructFindKey(person, "firstname")>
		<cfset lastfind = StructFindKey(person, "lastname")>
		
		<cfset AssertEquals("Firstname", firstfind[1].value)>
		<cfset AssertEquals("Lastname", lastfind[1].value)>

	</cffunction>
	
	<cffunction name="testLayeredFindKey">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
		<cfset person.sister.firstname = "SisterFirst">
		<cfset person.sister.lastname = "SisterLast">

		<cfset singlefind = StructFindKey(person, "firstname", "ONE")>
		<cfset AssertEquals (1, ArrayLen(firstfind))>
						
		<cfset firstfind = StructFindKey(person, "firstname", "ALL")>
		<cfset lastfind = StructFindKey(person, "lastname", "ALL")>
		
		<cfset AssertEquals (2, ArrayLen(firstfind))>
		<cfset AssertEquals("Firstname", firstfind[1].value)>
		<cfset AssertEquals("Lastname", lastfind[1].value)>
		<cfset AssertEquals("SisterFirst", firstfind[2].value)>
		<cfset AssertEquals("SisterLast", lastfind[2].value)>

	</cffunction>	

	<cffunction name="testNamedFindKey">

		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
						
		<cfset firstfind = StructFindKey(struct=person, key="firstname")>
		<cfset lastfind = StructFindKey(struct=person, key="lastname")>
		
		<cfset AssertEquals("Firstname", firstfind[1].value)>
		<cfset AssertEquals("Lastname", lastfind[1].value)>

	</cffunction>
		
</cfcomponent>