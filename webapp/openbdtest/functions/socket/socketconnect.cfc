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
 *  $Id: socketconnect.cfc 1893 2011-12-28 19:19:37Z alan $
	--->
<cfcomponent extends="openbdtest.common.TestCase">

<cffunction name="testHttpHEAD">

	<cfscript>
	var socket	= SocketConnect("www.bing.com", 80);

	assertTrue( socket.connected );

	socket.sendLine( "HEAD / HTTP/1.1" );
 	socket.sendLine( "host: www.bing.com" );
	socket.sendLine( "" );

	var line	= socket.readLine( 30000 );
	assertEquals( "HTTP/1.1 200 OK", line );

	while ( line != null ){
		line	= socket.readLine( 30000 );
	}

	socket.disconnect();
	assertTrue( !socket.connected );
	</cfscript>
</cffunction>



<cffunction name="testHttpGetImage">

	<cfscript>
	var socket	= SocketConnect("openbd.org", 80);
	assertTrue( socket.connected );

	// Write out the HTTP header
	socket.sendLine( "GET /manual/sd_openBD_32.png HTTP/1.1" );
 	socket.sendLine( "host: openbd.org" );
	socket.sendLine( "" );

	// Read the header
	var line	= socket.readLine( 30000 );
	assertEquals( "HTTP/1.1 200 OK", line );

	while ( line != "" ){
		line	= socket.readLine( 30000 );
		Console( line );
	}

	// Read the bytes
	FileDelete(ExpandPath("./logo.png"));

	var bufferSize	= 4096;
	var bindata	= socket.readBytes( bufferSize, 500 );

	while ( Len(bindata) > 0 ){
		FileWrite( ExpandPath("./logo.png"), bindata );
		if ( Len(bindata) < bufferSize )
			break;

		bindata	= socket.readBytes( bufferSize, 500 );
	}

	socket.disconnect();
	assertTrue( !socket.connected );
	</cfscript>
</cffunction>

</cfcomponent>