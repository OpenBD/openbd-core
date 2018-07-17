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

	<cffunction name="testNamingHashBinary">

		<cfset bin = ToBinary("1258" )>

		<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE"  , Hashbinary( bin, "MD5" ) )>
		<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE"  , Hashbinary( data=bin, algorithm="MD5" ) )>
		<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE"  , Hashbinary( algorithm="MD5", data=bin ) )>
		<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE"  , Hashbinary( data=bin) )>
		<cfset assertEquals ("5B296E6FD483DE7C5A3E55385E59C5FE"  , Hashbinary( bin )  )>

	</cffunction>
	
	<cffunction name="testPBKDF2Hash">
		<!--- Test PBKDF2  with binary input --->
		<cfset assertEquals ("847EE5FC46B0CBF031CAB25ECAEB7877BD6541809AA8026117B1897527BCD160984C9076406D5754E23A3B386A564E8CC3D2BFD2E6A150647B59889545FCDD09" , hash( toBinary( "someUserPassword" ), "pbkdf2", "utf-8", 100000, "someReallyLongAndGeneratedSaltThatNoDevShouldEverSee", "SHA512", 512 ))>
	</cffunction>

</cfcomponent>