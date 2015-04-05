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

	<!---
	Testing for...in over an array
	--->
	<cffunction name="testBug290" hint="tests the use of negative numbers in switch" >

		<cfscript>
		var x = 0;
		var passed = false;
		switch(x){
			case 0:
				passed = true;
				break;
			default:
				passed = false;
		}
		
		assertTrue( passed ); 
		</cfscript>
		
		<cfscript>
		var p = -1;
		passed = false;
		switch(p){
			case "-1":
				passed = true;
				break;
			default:
				passed = false;
		}
		assertTrue( passed ); 
		</cfscript>
		
		<cfscript>
		p = -1;
		passed = false;
		switch(p){
			case -1:
				passed = true;
				break;
			default:
				passed = false;
		}
		assertTrue( passed ); 
		</cfscript>

	</cffunction>

</cfcomponent>