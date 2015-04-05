<cfcomponent>

	<cffunction name="someMethod">
  	<cfset var param1 = event />
	
		<cfreturn param1>
	</cffunction>

	<cffunction name="someMethod2">
		<cfargument name="event" required="true">
  	<cfset var param1 = event />
	
		<cfreturn param1>
	</cffunction>

</cfcomponent>