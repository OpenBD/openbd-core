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

	<cffunction name="testNamingMid">

	<cfset assertEquals(4, Len(Mid("one Test",1,4)) )>
	<cfset assertEquals(4, Len(Mid(string="one Test",start=1,number=4)) )>
	<cfset assertEquals(Len(Mid("one Test",1,4)) , Len(Mid(string="one Test",start=1,number=4)) )>
	<cfset assertEquals(4, Len(Mid(string="one Test",number=4,start=1)) )>
	<cfset assertEquals(4, Len(Mid(start=1,string="one Test",number=4)) )>
	<cfset assertEquals(4, Len(Mid(start=1,number=4,string="one Test")) )>

	<cfset assertEquals("one ", Mid("one Test",1,4) )>
	<cfset assertEquals("one ", Mid(string="one Test",start=1,number=4) )>
	<cfset assertEquals(Mid("one Test",1,4), Mid(string="one Test",start=1,number=4) )>
	<cfset assertEquals("one ", Mid(start=1,string="one Test",number=4) )>
	<cfset assertEquals("one ", Mid(start=1,number=4,string="one Test") )>


	</cffunction>

</cfcomponent>