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
	*  $Id: DirectoryList.cfc 1980 2012-03-14 12:36:22Z alan $
	--->
<cfcomponent extends="openbdtest.common.TestCase">

<!--- ------------------------------------------------------------------------ --->

<cffunction name="testDirectoryList_listinfo_tag">
	<cfset var rootdir	= ExpandPath("../../functions/files/")>
	<cfdirectory action="list" directory="#rootdir#" name="r" />
	<cfset assertTrue( isQuery(r) )>
	<cfset assertEquals( ListLen(r.columnlist), 7 )>
	<cfdirectory action="list" directory="#rootdir#" listinfo="name" name="r" />
	<cfset assertTrue( isQuery(r) )>
	<cfset assertEquals( ListLen(r.columnlist), 1 )>
</cffunction>

<!--- ------------------------------------------------------------------------ --->

<cffunction name="testDirectoryList_listinfo_tag_recurse">
	<cfset var r = true>
	<cfset var rootdir	= ExpandPath("../../functions/")>
	<cfdirectory action="list" directory="#rootdir#" name="r" listinfo="name" recurse="true" />

	<cfset assertTrue( isQuery(r) )>
	<cfset assertEquals( ListLen(r.columnlist), 1 )>
</cffunction>


<cfscript>
// ---------------------------------------------------------------------------------

function testDirectoryList_listinfo(){
	var rootdir	= ExpandPath("../../functions/files/");

	// check the default which is the full path
	var r = DirectoryList( path=rootdir );
	assertTrue( r[1].startsWith(rootdir) );

	r = DirectoryList( path=rootdir, listinfo="path" );
	assertTrue( r[1].startsWith(rootdir) );

	r = DirectoryList( path=rootdir, listinfo="name" );
	assertTrue( !r[1].startsWith(rootdir) );

	r = DirectoryList( path=rootdir, listinfo="query" );
	assertTrue( isQuery(r) );
}

// ---------------------------------------------------------------------------------
</cfscript>

</cfcomponent>
