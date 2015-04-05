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

	<cffunction name="testNamingFindNocase">

	<cfset assertEquals(1, FindNocase("t","Test") )>
	<cfset assertEquals(1, FindNocase(substring="t",string="Test") )>
	<cfset assertEquals(1, FindNocase(string="Test",substring="t") )>
	<cfset assertEquals(FindNocase("t","Test"), FindNocase(string="Test",substring="t") )>

	<cfset assertEquals(1, FindNocase("t","Test",0) )>
	<cfset assertEquals(1, FindNocase(substring="t",string="Test", start=0) )>
	<cfset assertEquals(1, FindNocase(string="Test",substring="t", start=0) )>
	<cfset assertEquals(1, FindNocase(string="Test",start=0,substring="t") )>
	<cfset assertEquals(FindNocase("t","Test"), FindNocase(string="Test",substring="t", start=0) )>

	<cfset assertEquals(1, FindNocase("t","test",0) )>
	<cfset assertEquals(1, FindNocase(substring="t",string="test", start=0) )>
	<cfset assertEquals(1, FindNocase(string="test",substring="t", start=0) )>
	<cfset assertEquals(1, FindNocase(string="test",start=0,substring="t") )>
	<cfset assertEquals(FindNocase("t","test"), FindNocase(string="test",substring="t", start=0) )>

	<cfset assertEquals(1, FindNocase("t","TesT",1) )>
	<cfset assertEquals(1, FindNocase(substring="t",string="TesT", start=1) )>
	<cfset assertEquals(1, FindNocase(string="TesT",substring="t", start=1) )>
	<cfset assertEquals(1, FindNocase(string="TesT",start=1,substring="t") )>
	<cfset assertEquals(FindNocase("t","TesT",1), FindNocase(string="TesT",substring="t", start=1) )>

	<cfset assertEquals(4, FindNocase("t","Test",2) )>
	<cfset assertEquals(4, FindNocase(substring="t",string="Test", start=2) )>
	<cfset assertEquals(4, FindNocase(string="Test",substring="t", start=2) )>
	<cfset assertEquals(4, FindNocase(string="Test",start=2,substring="t") )>
	<cfset assertEquals(FindNocase("t","Test",2), FindNocase(string="Test",substring="t", start=2) )>

	<cfset assertEquals(0, FindNocase("z","test",1) )>
	<cfset assertEquals(0, FindNocase(substring="z",string="test", start=1) )>
	<cfset assertEquals(0, FindNocase(string="test",substring="z", start=1) )>
	<cfset assertEquals(0, FindNocase(string="test",start=1,substring="z") )>
	<cfset assertEquals(FindNocase("z","test"), FindNocase(string="test",substring="z", start=1) )>


	</cffunction>

</cfcomponent>