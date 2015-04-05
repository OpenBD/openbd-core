<!---
 *
 *  Copyright (C) 2011 TagServlet Ltd
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

	<cffunction name="testBug313">
		<cfset var locale = CreateObject("java", "java.util.Locale").init("en", "US") />
		<cfset var mf = CreateObject("java", "java.text.MessageFormat").init("I am a simple message.", locale) />

		<cfset assertEquals( "I am a simple message.", mf.format( JavaCast( "string[]", ArrayNew(1) ) ) )> 


		<cfset mf = CreateObject("java", "java.text.MessageFormat").init("Dear {0}, Your order number is {1}.", locale) />
		<cfset var args = ['Peter', 87344] />

		<cfset assertEquals( "Dear Peter, Your order number is 87344.", mf.format( JavaCast("string[]", args ) ) )>
	</cffunction>

</cfcomponent>