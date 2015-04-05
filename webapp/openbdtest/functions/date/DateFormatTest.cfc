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

	<cffunction name="testBug287">
		<cfset var dt = CreateDate(2010, 1, 1) />
		<cfset var formatted = DateFormat( dt, "yyyy/mm/dd")> 
		<cfset assertEquals( "2010/01/01", formatted )>
	</cffunction>

	<cffunction name="testBug234">
		<cfset var dt = CreateDate(2010, 1, 1) />
		<cfset var formatted = DateFormat( dt, "mmmm d 'yy")> 
		<cfset assertEquals( "January 1 '10", formatted )>
	</cffunction>

	<cffunction name="testShort">
		<cfset var dt = CreateDate(2010, 1, 1) />
		<cfset var formatted = DateFormat( dt, "short")> 
		<cfset assertEquals( "1/1/10", formatted )>

		<cfset dt = CreateDate(2010, 12, 22) />
		<cfset formatted = DateFormat( dt, "short")> 
		<cfset assertEquals( "12/22/10", formatted )>

		<cfset dt = CreateDate(2010, 3, 4) />
		<cfset formatted = DateFormat( dt, "short")> 
		<cfset assertEquals( "3/4/10", formatted )>

	</cffunction>
	
	<cffunction name="testMedium">
		<cfset var dt = CreateDate(2010, 1, 1) />
		<cfset var formatted = DateFormat( dt, "medium")> 
		<cfset assertEquals( "Jan 1, 2010", formatted )>

		<cfset dt = CreateDate(2010, 12, 22) />
		<cfset formatted = DateFormat( dt, "medium")> 
		<cfset assertEquals( "Dec 22, 2010", formatted )>

	</cffunction>

	<cffunction name="testFull">
		<cfset var dt = CreateDate(2010, 1, 1) />
		<cfset var formatted = DateFormat( dt, "full")> 
		<cfset assertEquals( "Friday, January 1, 2010", formatted )>
		
		<cfset dt = CreateDate(2010, 12, 22) />
		<cfset formatted = DateFormat( dt, "full")> 
		<cfset assertEquals( "Wednesday, December 22, 2010", formatted )>

	</cffunction>
	

</cfcomponent>