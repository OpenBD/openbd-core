<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingRematchnocase">

	<cfset res =  Rematchnocase("[^h]+","hellHohu",true)>
	<cfset assertEquals("ell", res[1] )>
	<cfset assertEquals("o", res[2] )>
	<cfset assertEquals("u", res[3] )>

	<cfset res =  Rematchnocase(regular="[^h]+",string="hellHohu",unique=true) >
	<cfset assertEquals("ell", res[1] )>
	<cfset assertEquals("o", res[2] )>
	<cfset assertEquals("u", res[3] )>

	<cfset res =  Rematchnocase(string="hellHohu",unique=true,regular="[^h]+") >
	<cfset assertEquals("ell", res[1] )>
	<cfset assertEquals("o", res[2] )>
	<cfset assertEquals("u", res[3] )>

	<cfset res =  Rematchnocase("[^h]+","hellHohu",false) >
	<cfset assertEquals("ell", res[1] )>
	<cfset assertEquals("o", res[2] )>
	<cfset assertEquals("u", res[3] )>

	<cfset res =  Rematchnocase(regular="[^h]+",string="hellHohu",unique=false) >
	<cfset assertEquals("ell", res[1] )>
	<cfset assertEquals("o", res[2] )>
	<cfset assertEquals("u", res[3] )>

	<cfset res =  Rematchnocase(string="hellHohu",unique=false,regular="[^h]+") >
	<cfset assertEquals("ell", res[1] )>
	<cfset assertEquals("o", res[2] )>
	<cfset assertEquals("u", res[3] )>

	<cfset res = Rematchnocase("[^h]+","no",false)>
	<cfset assertEquals("no", res[1] )>

	<cfset res = Rematchnocase(regular="[^h]+",string="no",unique=false) >
	<cfset assertEquals("no", res[1] )>

	<cfset res = Rematchnocase("[^h]+","no",true) >
	<cfset assertEquals("no", res[1] )>

	<cfset res = Rematchnocase(regular="[^h]+",string="no",unique=true) >
	<cfset assertEquals("no", res[1] )>


	</cffunction>

</cfcomponent>