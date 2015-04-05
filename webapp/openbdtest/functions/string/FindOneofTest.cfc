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

	<cffunction name="testNamingFindOneof">

	<cfset assertEquals("1", FindOneof("t","test") )>
	<cfset assertEquals("1", FindOneof(character="t",string="test") )>
	<cfset assertEquals("1", FindOneof(string="test",character="t") )>
	<cfset assertEquals(FindOneof("t","test"), FindOneof(string="test",character="t") )>

	<cfset assertEquals("1", FindOneof("t","test",1) )>
	<cfset assertEquals("1", FindOneof(character="t",string="test", start=1) )>
	<cfset assertEquals("1", FindOneof(string="test",character="t", start=1) )>
	<cfset assertEquals("1", FindOneof(string="test",start=1,character="t") )>
	<cfset assertEquals(FindOneof("t","test",1), FindOneof(string="test",character="t", start=1) )>

	<cfset assertEquals("4", FindOneof("t","test",2) )>
	<cfset assertEquals("4", FindOneof(character="t",string="test", start=2) )>
	<cfset assertEquals("4", FindOneof(string="test",character="t", start=2) )>
	<cfset assertEquals("4", FindOneof(string="test",start=2,character="t") )>
	<cfset assertEquals(FindOneof("t","test",2), FindOneof(string="test",character="t", start=2) )>

	<cfset assertEquals("0", FindOneof("z","test",2) )>
	<cfset assertEquals("0", FindOneof(character="z",string="test", start=2) )>
	<cfset assertEquals("0", FindOneof(string="test",character="z", start=2) )>
	<cfset assertEquals("0", FindOneof(string="test",start=2,character="z") )>
	<cfset assertEquals(FindOneof("z","test",2), FindOneof(string="test",character="z", start=2) )>

	</cffunction>

</cfcomponent>