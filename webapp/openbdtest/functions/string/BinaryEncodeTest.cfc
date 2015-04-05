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

	<cffunction name="testNamingBinaryEncode">

	<cfset bin64 =ToBase64("3456"  )>
	<cfset bin =ToBinary(bin64 )>

	<cfset assertEquals("MzQ1Ng==", BinaryEncode(bin,"base64") )>
	<cfset assertEquals("MzQ1Ng==", BinaryEncode(data=bin,encoding="base64") )>
	<cfset assertEquals("MzQ1Ng==", BinaryEncode(encoding="base64",data=bin) )>
	<cfset assertEquals(BinaryEncode(bin,"base64"), BinaryEncode(encoding="base64",data=bin) )>
	<cfset assertEquals(BinaryEncode(bin,"base64"), BinaryEncode(data=bin,encoding="base64") )>

	</cffunction>

</cfcomponent>