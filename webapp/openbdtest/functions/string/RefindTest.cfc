<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingRefind">

	<cfset assertEquals(0, Refind("^(http[s]*|file)://.+","hello") )>
	<cfset assertEquals(0, Refind(regular="^(http[s]*|file)://.+",string="hello") )>
	<cfset assertEquals(0, Refind(string="hello",regular="^(http[s]*|file)://.+") )>
	<cfset assertEquals(Refind("^(http[s]*|file)://.+","hello"), Refind(string="hello",regular="^(http[s]*|file)://.+") )>

	<cfset assertEquals(1, Refind("^(http[s]*|file)://.+","http://localhost:8080/") )>
	<cfset assertEquals(1, Refind(regular="^(http[s]*|file)://.+",string="http://localhost:8080/") )>
	<cfset assertEquals(1, Refind(string="http://localhost:8080/",regular="^(http[s]*|file)://.+") )>
	<cfset assertEquals(Refind("^(http[s]*|file)://.+","http://localhost:8080/"), Refind(string="http://localhost:8080/",regular="^(http[s]*|file)://.+") )>

	<cfset assertEquals(0, Refind("^(http[s]*|file)://.+","http://localhost:8080/",2)  )>
	<cfset assertEquals(0, Refind(regular="^(http[s]*|file)://.+",string="http://localhost:8080/",start=2)  )>
	<cfset assertEquals(0, Refind(string="http://localhost:8080/",regular="^(http[s]*|file)://.+",start=2)  )>
	<cfset assertEquals(0, Refind(start=2,string="http://localhost:8080/",regular="^(http[s]*|file)://.+")  )>
	<cfset assertEquals(0, Refind(string="http://localhost:8080/",start=2,regular="^(http[s]*|file)://.+")  )>


	<cfset arr = Refind("^(http[s]*|file)://.+","http://localhost:8080/",1,true)>
	<cfset assertEquals(22,arr.len[1] )>
	<cfset assertEquals(4, arr.len[2] )>
	<cfset assertEquals(1, arr.pos[1] )>
	<cfset assertEquals(1, arr.pos[2] )>

	<cfset arr = Refind(regular="^(http[s]*|file)://.+",string="http://localhost:8080/",start=1,subexpression=true)>
	<cfset assertEquals(22,arr.len[1] )>
	<cfset assertEquals(4, arr.len[2] )>
	<cfset assertEquals(1, arr.pos[1] )>
	<cfset assertEquals(1, arr.pos[2] )>

	<cfset arr = Refind(string="http://localhost:8080/",start=1,subexpression=true,regular="^(http[s]*|file)://.+")>
	<cfset assertEquals(22,arr.len[1] )>
	<cfset assertEquals(4, arr.len[2] )>
	<cfset assertEquals(1, arr.pos[1] )>
	<cfset assertEquals(1, arr.pos[2] )>

	<cfset arr = Refind("^(http[s]*|file)://.+","hello",1,true)>
	<cfset assertEquals(0,arr.len[1] )>
	<cfset assertEquals(0, arr.pos[1] )>

	<cfset arr = Refind(regular="^(http[s]*|file)://.+",string="hello",start=1,subexpression=true)>
	<cfset assertEquals(0,arr.len[1] )>
	<cfset assertEquals(0, arr.pos[1] )>

	<cfset arr = Refind(string="hello",start=1,subexpression=true,regular="^(http[s]*|file)://.+")>
	<cfset assertEquals(0,arr.len[1] )>
	<cfset assertEquals(0, arr.pos[1] )>

	</cffunction>

</cfcomponent>