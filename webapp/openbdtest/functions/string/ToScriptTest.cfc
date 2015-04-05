<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingToScript">

	<cfset assertEquals('myvar = "simple";', trim(ToScript ( "simple", "myvar")) )>
	<cfset assertEquals('myvar = "simple";', trim(ToScript ( object="simple", jsname="myvar")) )>
	<cfset assertEquals('myvar = "simple";', trim(ToScript ( jsname="myvar",object="simple")) )>
	<cfset assertEquals(ToScript ( "simple", "myvar"), ToScript ( jsname="myvar",object="simple") )>

	<cfset myarr=["v1","v1"]>
	<cfset assertEquals(ToScript ( myarr, "myvar", true,true), ToScript ( object=myarr, jsname="myvar",format=true,shortcuts=true) )>

	</cffunction>

</cfcomponent>