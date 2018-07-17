<!---
 *
 *  Copyright (C) 2010 TagServlet Ltd
 *
 *  This file is part of Open BlueDragon (OpenBD) CFML Server Engine.
 *
 *  OpenBD is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *
 *  OpenBD is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenBD.  If not, see http://www.gnu.org/licenses/
 *
 *  Additional permission under GNU GPL version 3 section 7
 *
 *  If you modify this Program, or any covered work, by linking or combining
 *  it with any of the JARS listed in the README.txt (or a modified version of
 *  (that library), containing parts covered by the terms of that JAR, the
 *  licensors of this Program grant you additional permission to convey the
 *  resulting work.
 *  README.txt @ http://www.openbluedragon.org/license/README.txt
 *
 *  http://www.openbluedragon.org/
 *
--->
<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingHash">
			<cfset bin = ToBinary("1258" )>

			<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE" , Hash( bin ) )>
			<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE" ,	Hash( data=bin ) )>

			<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE" ,	Hash( bin, "MD5" ) )>
			<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE" ,	Hash( data=bin, algorithm="MD5" ) )>
			<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE" ,	Hash( algorithm="MD5", data=bin ) )>

			<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE" ,	Hash( bin, "MD5","CFMX_COMPAT" ) )>
			<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE" ,	Hash( data=bin, algorithm="MD5", encoding="CFMX_COMPAT" ) )>
			<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE" ,	Hash( algorithm="MD5", data=bin, encoding="CFMX_COMPAT" ) )>
			<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE" ,	Hash( algorithm="MD5", encoding="CFMX_COMPAT",data=bin ) )>
			<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE" ,	Hash( encoding="CFMX_COMPAT", algorithm="MD5" ,data=bin ) )>

	</cffunction>
	
	
	<cffunction name="testPBKDF2">
		<!--- Test PBKDF2  with string input --->
		<cfset assertEquals ("DB99349E0FA3C4E14B41A318C72771CBD1AAABF69B9F103B3652BDF670E6A107A0368E3B2330DF8F7042148AA6FC5C0B4B449EAE7C7A0E2FC429AF0EDC0BC827" , hash( "someUserPassword", "pbkdf2", "utf-8", 100000, "someReallyLongAndGeneratedSaltThatNoDevShouldEverSee", "SHA512", 512 ))>
	</cffunction>

</cfcomponent>