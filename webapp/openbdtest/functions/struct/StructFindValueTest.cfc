<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testFindValue">

		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
						
		<cfset firstfind = StructFindValue(person, "Firstname")>
		<cfset lastfind = StructFindValue(person, "Lastname")>
		
		<cfset AssertEquals("firstname", firstfind[1].key)>
		<cfset AssertEquals("lastname", lastfind[1].key)>

	</cffunction>
	
	<cffunction name="testLayeredFindValue">
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
		<cfset person.sister.firstname = "Firstname">
		<cfset person.sister.lastname = "Lastname">
		<cfset person.sister.dupname = "Lastname">

		<cfset singlefind = StructFindValue(person, "Lastname", "ONE")>
		<cfset AssertEquals (1, ArrayLen(singlefind))>
						
		<cfset firstfind = StructFindValue(person, "Firstname", "ALL")>
		<cfset lastfind = StructFindValue(person, "Lastname", "ALL")>
		
		<cfset AssertEquals (2, ArrayLen(firstfind))>
		<cfset AssertEquals(".firstname", firstfind[1].path)>
		<cfset AssertEquals(".sister.firstname", firstfind[2].path)>

	</cffunction>	

	<cffunction name="testNamedFindValue">

		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
						
		<cfset firstfind = StructFindValue(struct=person, value="Firstname")>
		<cfset lastfind = StructFindValue(struct=person, value="Lastname")>
		
		<cfset AssertEquals("firstname", firstfind[1].key)>
		<cfset AssertEquals("lastname", lastfind[1].key)>

	</cffunction>
				
</cfcomponent>