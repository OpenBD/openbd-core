<h1>Start/Stop the Socket</h1>

<cfscript>
if ( !StructKeyExists(url,"stop"))
	SocketServerStart( port=2001, cfc="openbdtest.functions.socket.clientcfc" );
else
	SocketServerStop( port=2001 );


WriteDump( SocketServerGetClients(port=2001) );

</cfscript>