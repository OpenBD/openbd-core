<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testSort">
		
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
		<cfset person.Age = "age">

		<!--- Sort text case sensitive (ascending) --->
		<cfset result = StructSort(person)>
		<cfset AssertEquals("firstname", result[1])>
		<cfset AssertEquals("lastname", result[2])>
		<cfset AssertEquals("Age", result[3])>

		<!--- Sort text case insensitive (ascending) --->
		<cfset result = StructSort(person, "textnocase")>
		<cfset AssertEquals("Age", result[1])>
		<cfset AssertEquals("firstname", result[2])>
		<cfset AssertEquals("lastname", result[3])>
		
		<!--- Sort text descending --->
		<cfset result = StructSort(person, "textnocase", "desc")>
		<cfset AssertEquals("Age", result[3])>
		<cfset AssertEquals("firstname", result[2])>
		<cfset AssertEquals("lastname", result[1])>

	</cffunction>
		
	<cffunction name="testChildSort">
		<cfset foo = structNew()>
		<cfset foo.raymond = structNew()>
		<cfset foo.raymond.age =9>
		<cfset foo.raymond.lastname = "Camden">
		<cfset foo.jeremy = structNew()>
		<cfset foo.jeremy.age =10>
		<cfset foo.jeremy.lastname = "Petersen">
		<cfset foo.joe = structNew()>
		<cfset foo.joe.age =12>
		<cfset foo.joe.lastname = "Test">

		<cfset result = StructSort(foo, "numeric", "asc", "age")>
		<cfset AssertEquals("raymond", result[1])>
		<cfset AssertEquals("jeremy", result[2])>
		<cfset AssertEquals("joe", result[3])>
	</cffunction>
		
	<cffunction name="testNamedSort">
				
		<cfset person = StructNew()>
		<cfset person.firstname = "Firstname">
		<cfset person.lastname = "Lastname">
		<cfset person.Age = "age">

		<!--- Sort text case sensitive (ascending) --->
		<cfset result = StructSort(struct=person)>
		<cfset AssertEquals("firstname", result[1])>
		<cfset AssertEquals("lastname", result[2])>
		<cfset AssertEquals("Age", result[3])>

		<!--- Sort text case insensitive (ascending) --->
		<cfset result = StructSort(struct=person, type="textnocase")>
		<cfset AssertEquals("Age", result[1])>
		<cfset AssertEquals("firstname", result[2])>
		<cfset AssertEquals("lastname", result[3])>
		
		<!--- Sort text descending --->
		<cfset result = StructSort(struct=person, direction="desc")>
		<cfset AssertEquals("Age", result[1])>
		<cfset AssertEquals("lastname", result[2])>
		<cfset AssertEquals("firstname", result[3])>

	</cffunction>
	
</cfcomponent>