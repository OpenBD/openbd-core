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

	<cffunction name="testNamingHtmlSourceFormat">

		<cfset res = HtmlSourceformat("<table><tr><td>HtmlSourceformat</td></tr></table>")>
		<cfset assertEquals(67, len(res))>
		<cfset match =  Rematchnocase(regular="[^<*(/)+>]+",string=res,unique=false) >
		<cfset assertEquals(12, len(match))>
		<cfset assertEquals("table",match[1])>
		<cfset assertEquals(4,len(match[2]))>
		<cfset assertEquals(0,len(trim(match[2])) )>
		<cfset assertEquals("tr",match[3])>
		<cfset assertEquals(6,len(match[4]))>
		<cfset assertEquals(0,len(trim(match[4])) )>
		<cfset assertEquals("td",match[5])>
		<cfset assertEquals("HtmlSourceformat",match[6])>
		<cfset assertEquals("td",match[7])>
		<cfset assertEquals(4,len(match[8]))>
		<cfset assertEquals(0,len(trim(match[8])) )>
		<cfset assertEquals("tr",match[9])>
		<cfset assertEquals(2,len(match[10]))>
		<cfset assertEquals(0,len(trim(match[10])) )>
		<cfset assertEquals("table",match[11])>
		<cfset assertEquals(2,len(match[12]))>
		<cfset assertEquals(0,len(trim(match[12])))>

		<cfset res = HtmlSourceformat(html="<table><tr><td>HtmlSourceformat</td></tr></table>")>
		<cfset assertEquals(67, len(res))>
		<cfset match =  Rematchnocase(regular="[^<*(/)+>]+",string=res,unique=false) >
		<cfset assertEquals(12, len(match))>
		<cfset assertEquals("table",match[1])>
		<cfset assertEquals(4,len(match[2]))>
		<cfset assertEquals(0,len(trim(match[2])) )>
		<cfset assertEquals("tr",match[3])>
		<cfset assertEquals(6,len(match[4]))>
		<cfset assertEquals(0,len(trim(match[4])) )>
		<cfset assertEquals("td",match[5])>
		<cfset assertEquals("HtmlSourceformat",match[6])>
		<cfset assertEquals("td",match[7])>
		<cfset assertEquals(4,len(match[8]))>
		<cfset assertEquals(0,len(trim(match[8])) )>
		<cfset assertEquals("tr",match[9])>
		<cfset assertEquals(2,len(match[10]))>
		<cfset assertEquals(0,len(trim(match[10])) )>
		<cfset assertEquals("table",match[11])>
		<cfset assertEquals(2,len(match[12]))>
		<cfset assertEquals(0,len(trim(match[12])))>

		<cfset res = HtmlSourceformat("HtmlSourceformat")>
		<cfset match =  Rematchnocase(regular="[^>]+",string=res,unique=false) >
		<cfset assertEquals(18, len(res))>
		<cfset assertEquals(1, len(match))>
		<cfset assertEquals("HtmlSourceformat",trim(match[1]))>

		<cfset res = HtmlSourceformat(html="HtmlSourceformat")>
		<cfset match =  Rematchnocase(regular="[^>]+",string=res,unique=false) >
		<cfset assertEquals(18, len(res))>
		<cfset assertEquals(1, len(match))>
		<cfset assertEquals("HtmlSourceformat",trim(match[1]))>

		<cfset assertEquals(0,len(HtmlSourceformat("")) )>
		<cfset assertEquals(0,len(HtmlSourceformat(html="")) )>

	</cffunction>

</cfcomponent>