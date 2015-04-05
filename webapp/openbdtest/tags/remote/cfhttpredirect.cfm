<cfparam name="url.count" default="1">

<cfif url.count GTE 3>
	<cfoutput>#url.count#</cfoutput>
<cfelse>
	<cflocation url="cfhttpredirect.cfm?count=#count+1#">
</cfif>

