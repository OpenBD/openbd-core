<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="setUp" returntype="void" access="public" hint="put things here that you want to run before each test">
		<cfset this.token = jwtcreate({name:"Marcus"}, "awesomeSecret", "HQ")>
	</cffunction>
	
	<cffunction name="testJwtCreate">
		<cfset var localToken = jwtcreate({name:"Marcus"}, "awesomeSecret", "HQ")>
		<cfset assertEquals( localToken, "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJIUSIsIm5hbWUiOiJtYXJjdXMifQ.5Yr7Td_u4YpGO5ZeWJ1HfBuRNiIRxYJYuJv-zyIjKNA" )>
	</cffunction>

	<cffunction name="testVerifyToken">
		<cfset assertTrue( jwtverify(this.token, "awesomeSecret", "HQ") )>
	</cffunction>
	
	<cffunction name="testVerifyTokenWrongSecret">
		<cfset assertFalse( jwtverify(this.token, "someOtherSecret", "HQ") )>
	</cffunction>
	
	<cffunction name="testVerifyTokenWrongIssuer">
		<cfset assertFalse( jwtverify(this.token, "awesomeSecret", "Pentagon") )>
	</cffunction>
	
	<cffunction name="testVerifyTokenRightIssuerWrongCase">
		<cfset assertFalse( jwtverify(this.token, "awesomeSecret", "hq") )>
	</cffunction>
	
	<cffunction name="testDecodeToken">
		<cfset var decodedToken = jwtDecode(this.token)>
		<cfset assertTrue( structKeyExists(decodedToken, "algorithm") )>
		<cfset assertTrue( structKeyExists(decodedToken, "iss") )>
		<cfset assertTrue( structKeyExists(decodedToken, "name") )>
		
		<cfset assertTrue( decodedToken.algorithm EQ "HS256" )>
		<cfset assertTrue( decodedToken.iss EQ "HQ" )>
		<cfset assertTrue( decodedToken.name EQ "Marcus" )>
	</cffunction>



</cfcomponent>