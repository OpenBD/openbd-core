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
 *  http://openbd.org/
 *  $Id: Directory.cfc 1817 2011-11-22 05:45:34Z alan $
	--->
<cfcomponent extends="openbdtest.common.TestCase">

<cfscript>
// ---------------------------------------------------------------------------------

function testGetDirectoryFromPath1(){
	var path = GetDirectoryFromPath( ExpandPath("../../functions/files/") );
	assertTrue( path.endsWith("files#FileSeparator()#") );

	path = GetDirectoryFromPath( ExpandPath("../../functions/files/Directory.cfc") );
	assertTrue( path.endsWith("files#FileSeparator()#") );
}

// ---------------------------------------------------------------------------------

function testGetDirectoryFromPath2(){
	var path = GetDirectoryFromPath( "/" );
	assertEquals( path, "/" );

	path = GetDirectoryFromPath( "" );
	assertEquals( path, "" );
}

// ---------------------------------------------------------------------------------

function testGetDirectoryFromPath3(){
	var path = GetDirectoryFromPath( "/a/b/c/d/e/f/" );
	assertEquals( path, "/a/b/c/d/e/f/" );

	path = GetDirectoryFromPath( "/a/b/c/d/e/f" );
	assertEquals( path, "/a/b/c/d/e/" );

	path = GetDirectoryFromPath( "/a/../d/e/f" );
	assertEquals( path, "/d/e/" );

	path = GetDirectoryFromPath( "/a/../d/e/f/" );
	assertEquals( path, "/d/e/f/" );

	path = GetDirectoryFromPath( "/a/b/c/././d/e/f/" );
	assertEquals( path, "/a/b/c/d/e/f/" );
}

// ---------------------------------------------------------------------------------
</cfscript>

</cfcomponent>
