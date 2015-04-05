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

	<cffunction name="testNamingFind">

	<cfset assertEquals(1, Find("t","test") )>
	<cfset assertEquals(1, Find(substring="t",string="test") )>
	<cfset assertEquals(Find("t","test"), Find(substring="t",string="test") )>
	<cfset assertEquals(1, Find(string="test",substring="t") )>
	<cfset assertEquals(Find("t","test"), Find(string="test",substring="t") )>

	<cfset assertEquals(1, Find("t","test",1) )>
	<cfset assertEquals(1, Find(substring="t",string="test",start=1) )>
	<cfset assertEquals(Find("t","test",1), Find(substring="t",string="test",start=1) )>
	<cfset assertEquals(1, Find(string="test",substring="t",start=1) )>
	<cfset assertEquals(Find("t","test",1), Find(string="test",substring="t",start=1) )>
	<cfset assertEquals(1, Find(start=1,string="test",substring="t") )>
	<cfset assertEquals(Find("t","test",1), Find(start=1,string="test",substring="t") )>
	<cfset assertEquals(1, Find(substring="t",start=1,string="test") )>
	<cfset assertEquals(Find("t","test",1), Find(substring="t",start=1,string="test") )>

	<cfset assertEquals(4, Find("t","test",2) )>
	<cfset assertEquals(4, Find(substring="t",string="test",start=2) )>
	<cfset assertEquals(Find("t","test",2), Find(substring="t",string="test",start=2) )>
	<cfset assertEquals(4, Find(string="test",substring="t",start=2) )>
	<cfset assertEquals(Find("t","test",2), Find(string="test",substring="t",start=2) )>
	<cfset assertEquals(4, Find(start=2,string="test",substring="t") )>
	<cfset assertEquals(Find("t","test",2), Find(start=2,string="test",substring="t") )>
	<cfset assertEquals(4, Find(substring="t",start=2,string="test") )>
	<cfset assertEquals(Find("t","test",2), Find(substring="t",start=2,string="test") )>

	<cfset assertEquals(0, Find("z","test",2) )>
	<cfset assertEquals(0, Find(substring="z",string="test",start=2) )>
	<cfset assertEquals(Find("z","test",2), Find(substring="z",string="test",start=2) )>
	<cfset assertEquals(0, Find(string="test",substring="z",start=2) )>
	<cfset assertEquals(Find("z","test",2), Find(string="test",substring="z",start=2) )>
	<cfset assertEquals(0, Find(start=2,string="test",substring="z") )>
	<cfset assertEquals(Find("z","test",2), Find(start=12,string="test",substring="z") )>
	<cfset assertEquals(0, Find(substring="z",start=2,string="test") )>
	<cfset assertEquals(Find("z","test",2), Find(substring="z",start=2,string="test") )>

	<cfset assertEquals(0, Find("z","test") )>
	<cfset assertEquals(0, Find(substring="z",string="test") )>
	<cfset assertEquals(Find("z","test"), Find(substring="z",string="test") )>
	<cfset assertEquals(0, Find(string="test",substring="z") )>
	<cfset assertEquals(Find("z","test"), Find(string="test",substring="z") )>

	</cffunction>

</cfcomponent>