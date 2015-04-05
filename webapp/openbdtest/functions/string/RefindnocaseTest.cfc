<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingRefindnocase">

	<cfset assertEquals(0, Refindnocase("^(http[s]*|file)://.+","hello") )>
	<cfset assertEquals(0, Refindnocase(regular="^(http[s]*|file)://.+",string="hello") )>
	<cfset assertEquals(0, Refindnocase(string="hello",regular="^(http[s]*|file)://.+") )>
	<cfset assertEquals(Refindnocase("^(http[s]*|file)://.+","hello"), Refindnocase(string="hello",regular="^(http[s]*|file)://.+") )>

	<cfset assertEquals(1, Refindnocase("^(http[s]*|file)://.+","HTTP://LOCALHOST:8080/") )>
	<cfset assertEquals(1, Refindnocase(regular="^(http[s]*|file)://.+",string="HTTP://LOCALHOST:8080/") )>
	<cfset assertEquals(1, Refindnocase(string="HTTP://LOCALHOST:8080/",regular="^(http[s]*|file)://.+") )>
	<cfset assertEquals(Refindnocase("^(http[s]*|file)://.+","HTTP://LOCALHOST:8080/"), Refindnocase(string="HTTP://LOCALHOST:8080/",regular="^(http[s]*|file)://.+") )>

	<cfset assertEquals(0, Refindnocase("^(http[s]*|file)://.+","HTTP://LOCALHOST:8080/",2)  )>
	<cfset assertEquals(0, Refindnocase(regular="^(http[s]*|file)://.+",string="HTTP://LOCALHOST:8080/",start=2)  )>
	<cfset assertEquals(0, Refindnocase(string="HTTP://LOCALHOST:8080/",regular="^(http[s]*|file)://.+",start=2)  )>
	<cfset assertEquals(0, Refindnocase(start=2,string="HTTP://LOCALHOST:8080/",regular="^(http[s]*|file)://.+")  )>
	<cfset assertEquals(0, Refindnocase(string="HTTP://LOCALHOST:8080/",start=2,regular="^(http[s]*|file)://.+")  )>


	<cfset arr = Refindnocase("^(http[s]*|file)://.+","HTTP://LOCALHOST:8080/",1,true)>
	<cfset assertEquals(22,arr.len[1] )>
	<cfset assertEquals(4, arr.len[2] )>
	<cfset assertEquals(1, arr.pos[1] )>
	<cfset assertEquals(1, arr.pos[2] )>

	<cfset arr = Refindnocase(regular="^(http[s]*|file)://.+",string="HTTP://LOCALHOST:8080/",start=1,subexpression=true)>
	<cfset assertEquals(22,arr.len[1] )>
	<cfset assertEquals(4, arr.len[2] )>
	<cfset assertEquals(1, arr.pos[1] )>
	<cfset assertEquals(1, arr.pos[2] )>

	<cfset arr = Refindnocase(string="HTTP://LOCALHOST:8080/",start=1,subexpression=true,regular="^(http[s]*|file)://.+")>
	<cfset assertEquals(22,arr.len[1] )>
	<cfset assertEquals(4, arr.len[2] )>
	<cfset assertEquals(1, arr.pos[1] )>
	<cfset assertEquals(1, arr.pos[2] )>

	<cfset arr = Refindnocase("^(http[s]*|file)://.+","hello",1,true)>
	<cfset assertEquals(0,arr.len[1] )>
	<cfset assertEquals(0, arr.pos[1] )>

	<cfset arr = Refindnocase(regular="^(http[s]*|file)://.+",string="hello",start=1,subexpression=true)>
	<cfset assertEquals(0,arr.len[1] )>
	<cfset assertEquals(0, arr.pos[1] )>

	<cfset arr = Refindnocase(string="hello",start=1,subexpression=true,regular="^(http[s]*|file)://.+")>
	<cfset assertEquals(0,arr.len[1] )>
	<cfset assertEquals(0, arr.pos[1] )>
	</cffunction>

</cfcomponent>