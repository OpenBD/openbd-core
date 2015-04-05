<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingRematch">

	<cfset res =  Rematch("[^h]+","hellhohu",true)>
	<cfset assertEquals("ell", res[1] )>
	<cfset assertEquals("o", res[2] )>
	<cfset assertEquals("u", res[3] )>

	<cfset res =  Rematch(regular="[^h]+",string="hellhohu",unique=true)>
	<cfset assertEquals("ell", res[1] )>
	<cfset assertEquals("o", res[2] )>
	<cfset assertEquals("u", res[3] )>

	<cfset res =  Rematch(string="hellhohu",unique=true,regular="[^h]+")>
	<cfset assertEquals("ell", res[1] )>
	<cfset assertEquals("o", res[2] )>
	<cfset assertEquals("u", res[3] )>

	<cfset res =  Rematch("[^h]+","hellhohu",false)>
	<cfset assertEquals("ell", res[1] )>
	<cfset assertEquals("o", res[2] )>
	<cfset assertEquals("u", res[3] )>

	<cfset res =  Rematch(regular="[^h]+",string="hellhohu",unique=false)>
	<cfset assertEquals("ell", res[1] )>
	<cfset assertEquals("o", res[2] )>
	<cfset assertEquals("u", res[3] )>

	<cfset res =  Rematch(string="hellhohu",unique=false,regular="[^h]+")>
	<cfset assertEquals("ell", res[1] )>
	<cfset assertEquals("o", res[2] )>
	<cfset assertEquals("u", res[3] )>

	<cfset res = Rematch("[^h]+","no",false)>
	<cfset assertEquals("no", res[1] )>

	<cfset res = Rematch(regular="[^h]+",string="no",unique=false)>
	<cfset assertEquals("no", res[1] )>

	<cfset res = Rematch("[^h]+","no",true) >
	<cfset assertEquals("no", res[1] )>

	<cfset res = Rematch(regular="[^h]+",string="no",unique=true)>
	<cfset assertEquals("no", res[1] )>

	</cffunction>

</cfcomponent>