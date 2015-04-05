<cfcomponent>

<cffunction name="onConnect">
	<cfargument name="socketdata">
	<cfset Console( "onConnect" )>
	<cfset arguments.socketdata.sendLine("Welcome")>
</cffunction>



<cffunction name="onDisconnect">
	<cfargument name="socketdata">
	<cfset Console( "onDisconnect" )>

</cffunction>


<cffunction name="onReadLine">
	<cfargument name="socketdata">
	<cfargument name="line">

	<cfset arguments.socketdata.sendLine("Rxd:" & arguments.line)>
</cffunction>


</cfcomponent>