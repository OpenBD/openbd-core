<cfsilent>

<cfif isDefined( "form.filefield")>
	<cfset fil = FileUploadAll( destination="/openbdtest/functions/files/", nameconflict="makeunique", uri=true )>
<cfelse>
	<cfset fil = []>
</cfif>

</cfsilent><cfoutput>#SerializeJSon( fil )#</cfoutput>