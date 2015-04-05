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

	<cffunction name="testNamingGestToken">

	<cfset assertEquals("tree", GetToken("one,two,tree","3",",") )>
	<cfset assertEquals("tree", GetToken(string="one,two,tree",position="3",delimiters=",") )>
	<cfset assertEquals("tree", GetToken(position="3",string="one,two,tree",delimiters=",") )>
	<cfset assertEquals("tree", GetToken(string="one,two,tree",delimiters=",", position="3") )>
	<cfset assertEquals(GetToken("one,two,tree","3",","), GetToken(string="one,two,tree",position="3",delimiters=",") )>

	<cfset assertEquals("one", GetToken("one,two,tree","1",",") )>
	<cfset assertEquals("one", GetToken(string="one,two,tree",position="1",delimiters=",") )>
	<cfset assertEquals("one", GetToken(position="1",string="one,two,tree",delimiters=",") )>
	<cfset assertEquals("one", GetToken(position="1",delimiters=",",string="one,two,tree") )>
	<cfset assertEquals(GetToken(string="one,two,tree",position="1",delimiters=","), GetToken("one,two,tree","1",",") )>

	<cfset assertEquals("two", GetToken("one,two,tree","2",",") )>
	<cfset assertEquals("two", GetToken(string="one,two,tree",position="2",delimiters=",") )>
	<cfset assertEquals("two", GetToken(position="2",string="one,two,tree",delimiters=",") )>
	<cfset assertEquals("two", GetToken(position="2",delimiters=",",string="one,two,tree") )>
	<cfset assertEquals(GetToken("one,two,tree","2",","), GetToken(position="2",delimiters=",",string="one,two,tree") )>


	<cfset isOk = "0">
	<cftry>
		<cfset r=GetToken("one,two,tree","0",",")>
	<cfcatch type="Expression">
		<cfset isOk = "1">
	</cfcatch>
	</cftry>
	<cfset assertEquals("1", isOk )>

	<cfset isOk = "0">
	<cftry>
		<cfset r=GetToken(string="one,two,tree",position="0",delimiters=",")>
	<cfcatch type="Expression">
		<cfset isOk = "1">
	</cfcatch>
	</cftry>
	<cfset assertEquals("1", isOk )>

	</cffunction>

</cfcomponent>