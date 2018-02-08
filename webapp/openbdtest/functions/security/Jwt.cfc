<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="setUp" returntype="void" access="public" hint="put things here that you want to run before each test">
		<cfset this.token = jwtcreate({name: "Marcus", admin: false}, "awesomeSecret", "HQ")>
	</cffunction>
	
	
	<cffunction name="jwtCreate">
		<cfset var localToken = jwtcreate({name:"Marcus"}, "awesomeSecret", "HQ")>
		<cfset assertEquals( localToken, "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJIUSIsIm5hbWUiOiJNYXJjdXMifQ.wrKCkwvAuIRYJ5ehBflcsgPWDIW8W7nMxvz6eDTOUV0" )>
	</cffunction>
	
	
	<cffunction name="verifyToken">
		<cfset assertTrue( jwtverify(this.token, "awesomeSecret", "HQ") )>
	</cffunction>
	
	
	<cffunction name="verifyTokenWrongSecret">
		<cfset assertFalse( jwtverify(this.token, "someOtherSecret", "HQ") )>
	</cffunction>
	
	
	<cffunction name="verifyTokenWrongIssuer">
		<cfset assertFalse( jwtverify(this.token, "awesomeSecret", "Pentagon") )>
	</cffunction>
	
	
	<cffunction name="verifyTokenRightIssuerWrongCase">
		<cfset assertFalse( jwtverify(this.token, "awesomeSecret", "hq") )>
	</cffunction>
	
	
	<cffunction name="verifyTokenWithFutureExpiry">
		<cfset var localToken = jwtcreate(private = {name:"Marcus"}, secret = "awesomeSecret", issuer = "HQ", expiration = DateDiff("s", "January 1 1970 00:00", now()) + 30)>
		<cfset assertTrue( jwtverify(this.token, "awesomeSecret", "HQ") )>
	</cffunction>
	
	
	<cffunction name="verifyTokenWithPastExpiry">
		<cfset var localToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJIUSIsIm5hbWUiOiJtYXJjdXMiLCJleHAiOjE1MTgxMDB9.96II13K1OqJWdFRn040a7dWg3n6Qf3NyqjI0IjnVbJw">
		<cfset assertFalse( jwtverify(localToken, "awesomeSecret", "HQ") )>
	</cffunction>
	
	
	<cffunction name="decodeToken">
		<cfset var decodedToken = jwtdecode(this.token)>
		<cfset assertTrue( structKeyExists(decodedToken, "algorithm") )>
		<cfset assertTrue( structKeyExists(decodedToken, "iss") )>
		<cfset assertTrue( structKeyExists(decodedToken, "name") )>
		
		<cfset assertTrue( decodedToken.algorithm EQ "HS256" )>
		<cfset assertTrue( decodedToken.iss EQ "HQ" )>
		<cfset assertTrue( decodedToken.name EQ "Marcus" )>
	</cffunction>
	
	
	<cffunction name="decodeTokenComplexData">
		<cfset var localToken = jwtcreate(private = {name:"Marcus", stuff: ["first", "second"]}, secret = "awesomeSecret", issuer = "HQ")>
		<cfset var decoded = jwtDecode(localToken, true)>
		
		<cfset assertTrue( decoded.algorithm EQ "HS256" )>
		<cfset assertTrue( decoded.iss EQ "HQ" )>
		<cfset assertTrue( decoded.name EQ "Marcus" )>
		<cfset assertTrue( isArray(decoded.stuff) )>
		<cfset assertTrue( arrayLen(decoded.stuff) EQ 2 )>
	</cffunction>

</cfcomponent>