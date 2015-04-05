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

 *  $Id: FilesTestPart2.cfc 1731 2011-10-10 19:59:13Z alan $
	--->
<cfcomponent extends="openbdtest.common.TestCase">

<cfscript>
// ---------------------------------------------------------------------------------

function testGetFileFromPath(){
	var ft	= GetFileFromPath( "/tmp/dir1/dir2/dir3/file.txt" );
	assertEquals( ft, "file.txt" );
}

// ---------------------------------------------------------------------------------

function testGetDirectoryFromPath(){
	var ft	= GetDirectoryFromPath( "/tmp/dir1/dir2/dir3/file.txt" );
	ft	= Replace( ft, FileSeparator(), "/", "all" );
	assertEquals( ft, "/tmp/dir1/dir2/dir3/" );
}

// ---------------------------------------------------------------------------------

function testDirectoryList(){
	var ft	= DirectoryList( ExpandPath(".") );
	assertTrue( isArray(ft) );
	if ( ArrayLen(ft) == 0 )
		fail("directory list was empty");
}

// ---------------------------------------------------------------------------------

function testFileSetLastModified(){
	FileWriteLine( ExpandPath("./write.txt"), "This is a line of text to write" );
	var dt	= CreateDateTime( 1972, 12, 9, 6, 0, 0 );

	FileSetLastModified( ExpandPath("./write.txt"), dt );
	var sf	= GetFileInfo( ExpandPath("./write.txt") );

	FileDelete( ExpandPath("./write.txt") );

	assertEquals( sf.lastmodified, dt );
}

// ---------------------------------------------------------------------------------

function testFileReadBinary(){
	var binData	= FileReadBinary( ExpandPath("./FilesTest.cfc") );
	var sf	= GetFileInfo( ExpandPath("./FilesTest.cfc") );
	assertEquals( sf.size, Len(binData) );
}

// ---------------------------------------------------------------------------------

function testFileWrite(){
	FileDelete( ExpandPath("./write.txt") );
	FileWrite( ExpandPath("./write.txt"), "This is a line of text to write" );
	var line	= FileRead( ExpandPath("./write.txt") );
	FileDelete( ExpandPath("./write.txt") );
	assertEquals( line, "This is a line of text to write" );
}

// ---------------------------------------------------------------------------------

function testFileWriteLine(){
	FileDelete( ExpandPath("./write.txt") );
	FileWriteLine( ExpandPath("./write.txt"), "This is a line of text to write" );
	var line	= FileRead( ExpandPath("./write.txt") );
	FileDelete( ExpandPath("./write.txt") );
	assertEquals( Trim(line), "This is a line of text to write" );
}

// ---------------------------------------------------------------------------------

function testFileOpenFileReadLineFileIsEOFFileWriteLine(){
	var fo = FileOpen( src=ExpandPath("./FilesTest.cfc"), mode="read" );
	var fw = FileOpen( src=ExpandPath("./copy.cfc"), mode="write" );

	var line = "";
	while ( !FileIsEOF(fo) ){
		line = FileReadLine(fo);
		FileWriteLine( fw, line );
	}

	FileClose( fo );
	FileClose( fw );

	var sf	= GetFileInfo( ExpandPath("./FilesTest.cfc") );
	var df	= GetFileInfo( ExpandPath("./copy.cfc") );

	if ( df.size <= sf.size )
		fail("file size was not the same");

	assertEquals( df.type, sf.type );

	FileDelete( ExpandPath("./copy.cfc") );
}

// ---------------------------------------------------------------------------------

function testFileOpenFileReadLineFileIsEOF(){
	var fo = FileOpen( src=ExpandPath("./FilesTest.cfc"), mode="read" );

	var line = "";
	while ( !FileIsEOF(fo) ){
		line &= FileReadLine(fo);
	}

	FileClose( fo );

	if ( line == "" )
		fail("read a blank string");
}

// ---------------------------------------------------------------------------------

function testFileOpenFileReadLine(){
	var fo = FileOpen( src=ExpandPath("./FilesTest.cfc"), mode="read" );
	var line	= FileReadLine(fo);
	FileClose( fo );

	if ( line == "" )
		fail("read a blank string");
}

// ---------------------------------------------------------------------------------

function testFileOpenFileClose(){
	try{
		FileDelete( ExpandPath("./copy.txt") );
		var fo = FileOpen( src=ExpandPath("./copy.txt"), mode="write" );
		FileClose( fo );
		assertTrue( FileExists(ExpandPath("./copy.txt")) );
		FileDelete( ExpandPath("./copy.txt") );
	}catch(Any e){
		fail( e.message );
	}
}

</cfscript>

</cfcomponent>