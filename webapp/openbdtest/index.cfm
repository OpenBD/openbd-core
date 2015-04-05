<cfinclude template="/openbdtest/assets/_public_header.cfm">

<cfset functionTests = DirectoryList( path=ExpandPath("./functions/"), listinfo="name" )>
<cfset scriptTests = DirectoryList( path=ExpandPath("./script/"), listinfo="name" )>
<cfset tagTests = DirectoryList( path=ExpandPath("./tags/"), listinfo="name" )>

<h1>Execute Tests</h1>

<cfoutput>
<table>
<tr>
	<td width="50%" valign="top">

		<ul>
		<li><a href="run.cfm?dir=#ExpandPath('.')#&componentpath=openbdtest">Execute All Tests</a></li>
		<li><a href="run.cfm?dir=#ExpandPath('./script')#&componentpath=openbdtest.script">Execute script Tests</a></li>
		<cfloop array="#tagTests#" index="name">
			<cfif name == ".svn"><cfcontinue></cfif>
			<li>Tag Group: <a href="run.cfm?dir=#ExpandPath('./tags/' & name )#&componentpath=openbdtest.tags.#name#">#name#</a>

				<ul>
				<cfset innerTests	= DirectoryList( path=ExpandPath("./tags/" & name ), listinfo="name" )>
				<cfloop array="#innerTests#" index="cfc">
					<cfif cfc.endsWith(".cfc")>
						<li><a href="/openbdtest/tags/#name#/#cfc#?method=runtestremote&output=html">#cfc#</a></li>
					</cfif>
				</cfloop>
				</ul>

			</li>
		</cfloop>
		</ul>

	</td>
	<td width="50%" valign="top">

		<ul>
			<cfloop array="#functionTests#" index="name">
			<cfif name == ".svn"><cfcontinue></cfif>
			<li>Function Group: <a href="run.cfm?dir=#ExpandPath('./functions/' & name )#&componentpath=openbdtest.functions.#name#">#name#</a>

				<ul>
				<cfset innerTests	= DirectoryList( path=ExpandPath("./functions/" & name ), listinfo="name" )>
				<cfloop array="#innerTests#" index="cfc">
					<cfif cfc.endsWith(".cfc")>
						<li><a href="/openbdtest/functions/#name#/#cfc#?method=runtestremote&output=html">#cfc#</a></li>
					</cfif>
				</cfloop>
				</ul>

			</li>
		</cfloop>
		</ul>

	</td>
</tr>
</table>

</cfoutput>

<cfinclude template="/openbdtest/assets/_public_footer.cfm">