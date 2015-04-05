<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingRemovechars">

	<cfset assertEquals("hellomorning", Removechars("hellogoodmorning",6,4))>
	<cfset assertEquals("hellomorning", Removechars(string="hellogoodmorning",start=6,count=4))>
	<cfset assertEquals("hellomorning", Removechars(start=6,string="hellogoodmorning",count=4))>
	<cfset assertEquals("hellomorning", Removechars(count=4,start=6,string="hellogoodmorning"))>
	<cfset assertEquals("hellomorning", Removechars(string="hellogoodmorning",count=4,start=6))>
	<cfset assertEquals(Removechars("hellogoodmorning",6,4), Removechars(string="hellogoodmorning",count=4,start=6))>

	</cffunction>

</cfcomponent>