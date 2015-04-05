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

 *  $Id: FilesUpload.cfc 1687 2011-09-25 18:24:21Z alan $
	--->
<cfcomponent extends="openbdtest.common.TestCase">

<cffunction name="testUpload">

	<cfhttp url="/openbdtest/testdata/upload.cfm" method="post" multipart="true">
		<cfhttpparam file="#ExpandPath('./FilesTest.cfc')#" type="file" name="filefield" />
	</cfhttp>

	<cfscript>
	var fa	= DeserializeJSon( cfhttp.filecontent );
	assertTrue( IsArray(fa) );
	assertEquals( 1, ArrayLen(fa) );
	assertEquals( "FilesTest.cfc", fa[1].clientfile );
	assertEquals( "cfc", 				fa[1].clientfileext );
	assertEquals( "cfc", 				fa[1].serverfileext );
	assertEquals( "FilesTest", 	fa[1].clientfilename );
	assertTrue( fa[1].fileexisted );
	assertTrue( fa[1].filewassaved );
	assertTrue( fa[1].filewasrenamed );
	FileDelete( fa[1].serverfileuri );
	</cfscript>

</cffunction>

</cfcomponent>