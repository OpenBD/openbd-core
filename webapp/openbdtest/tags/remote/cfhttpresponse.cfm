<cfsilent>
	<cfif cgi.http_charenc NEQ ''>
		<cfset setEncoding( "url", cgi.http_charenc )>
		<cfset setEncoding( "form", cgi.http_charenc )>
	</cfif>
	<cfset return = {
				httprequest : getHttpRequestData(),
				form: form,
				url: url
			}>

</cfsilent><cfoutput>#SerializeJSON( return )#</cfoutput>