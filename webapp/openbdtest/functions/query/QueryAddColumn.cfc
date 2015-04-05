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

	<cffunction name="testAddColumnNoType">
		<cfset qTest = queryNew("col1") />
		<cfset queryaddrow( qTest, 1 )>
		<cfset querysetcell( qTest, "col1", "a" )>
		<cfset queryaddrow( qTest, 1 )>
		<cfset querysetcell( qTest, "col1", "b" )>
		<cfset queryaddrow( qTest, 1 )>
		<cfset querysetcell( qTest, "col1", "c" )>

		<cfset valueArray = [1, 2, 3] />

		<cfset queryAddColumn( query=qTest, column="col2", valuearray=valueArray) />

		<cfset assertEquals( qTest.columnlist, "col1,col2" ) />
		<cfset assertEquals( qTest.col2[1], 1 ) />
		<cfset assertEquals( qTest.col2[2], 2 ) />
		<cfset assertEquals( qTest.col2[3], 3 ) />
	</cffunction>


	<cffunction name="testAddColumnWithType">
		<cfset qTest = queryNew("col1") />
		<cfset queryaddrow( qTest, 1 )>
		<cfset querysetcell( qTest, "col1", "a" )>
		<cfset queryaddrow( qTest, 1 )>
		<cfset querysetcell( qTest, "col1", "b" )>
		<cfset queryaddrow( qTest, 1 )>
		<cfset querysetcell( qTest, "col1", "c" )>

		<cfset valueArray = [1, 2, 3] />

		<cfset queryAddColumn(qTest, "col2", "integer", valueArray) />

		<cfset assertEquals( qTest.columnlist, "col1,col2" ) />
		<cfset assertEquals( qTest.col2[1], 1 ) />
		<cfset assertEquals( qTest.col2[2], 2 ) />
		<cfset assertEquals( qTest.col2[3], 3 ) />
	</cffunction>


</cfcomponent>