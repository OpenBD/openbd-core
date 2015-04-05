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
 *  $Id: FilesTest.cfc 2226 2012-08-04 22:56:21Z alan $
	--->
<cfcomponent extends="openbdtest.common.TestCase">

<cfscript>
// ---------------------------------------------------------------------------------

function testFileMove(){
	DirectoryCreate( ExpandPath("./tmpdirectory/") );

	FileCopy( GetCurrentTemplatePath(), ExpandPath("./copy.txt") );
	var sf	= GetFileInfo( ExpandPath("./copy.txt") );
	FileMove( ExpandPath("./copy.txt"), ExpandPath("./tmpdirectory/") );

	var df	= GetFileInfo( ExpandPath("./tmpdirectory/copy.txt") );
	assertEquals( df.type, sf.type );
	assertEquals( df.size, sf.size );

	FileDelete( ExpandPath("./tmpdirectory/copy.txt") );
	FileDelete( ExpandPath("./copy.txt") );
	DirectoryDelete( ExpandPath("./tmpdirectory/") );
}

// ---------------------------------------------------------------------------------

function testDirectoryCreate(){
	try{
		DirectoryDelete( ExpandPath("./tmpdirectory/") );
	}catch(any e){}
	
	DirectoryCreate( ExpandPath("./tmpdirectory/") );

	var df	= GetFileInfo( ExpandPath("./tmpdirectory/") );
	assertEquals( df.size, 0 );
	assertEquals( df.type, "directory" );

	FileDelete( ExpandPath("./tmpdirectory/") );
}

// ---------------------------------------------------------------------------------

function testFileCopyFromHttpToLocal(){
	FileDelete( ExpandPath("./copy.txt") );
	FileCopy( "http://www.bing.com/", ExpandPath("./copy.txt") );

	var df	= GetFileInfo( ExpandPath("./copy.txt") );
	if ( df.size == 0 )
		fail("file not downloaded");

	FileDelete( ExpandPath("./copy.txt") );
}

// ---------------------------------------------------------------------------------

function testFileCopy(){
	var parent	= GetCurrentTemplatePath();
	parent	= parent.substring( 0, parent.lastIndexOf(FileSeparator())+1 );

	FileDelete( parent & "copy.txt" );
	FileCopy(   parent & "FilesTest.cfc", parent & "copy.txt" );

	assertTrue( FileExists( parent & "copy.txt") );

	var sf	= GetFileInfo( parent & "copy.txt" );
	var df	= GetFileInfo( parent & "copy.txt" );

	assertEquals( df.type, sf.type );
	assertEquals( df.size, sf.size );

	// Cleanup after ourselves
	FileDelete( parent & "copy.txt" );
}

// ---------------------------------------------------------------------------------

function testDirectoryExists(){
	assertTrue( DirectoryExists(ExpandPath(".")) );
	assertFalse( DirectoryExists(ExpandPath("./FilesTest.cfc")) );
}

// ---------------------------------------------------------------------------------

function testGetFileInfo(){
	var f	= GetFileInfo( GetCurrentTemplatePath() );

	assertEquals( "file", f.type );
	assertEquals( "FilesTest.cfc", f.name );
	assertEquals( GetCurrentTemplatePath(), f.path );

	if ( f.size < 1000 )
		fail("file size not greater than 1KB [#f.size#]");
}

// ---------------------------------------------------------------------------------

function testGetFileInfoNotExist(){
	try{
		GetFileInfo( "/var/doesnotexist.jpg" );
		fail("file does not exist");
	}catch(any e){}
}

// ---------------------------------------------------------------------------------

function testFileSeparator(){
	if ( server.os.name.startsWith("Windows") )
		assertEquals( "\", FileSeparator() );
	else
		assertEquals( "/", FileSeparator() );
}

// ---------------------------------------------------------------------------------

function testFileDelete(){
	assertFalse( FileDelete("/tmp/invalidfile.txt") );
}

// ---------------------------------------------------------------------------------

function testFileExists(){
	assertFalse( FileExists( "/tmp/invalidfile.txt" ) );
	assertTrue( FileExists( GetTemplatePath() ) );
	assertTrue( FileExists( "http://www.bing.com/" ) );
}

// ---------------------------------------------------------------------------------

</cfscript>

</cfcomponent>