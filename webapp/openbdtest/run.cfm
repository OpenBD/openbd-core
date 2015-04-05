<cfinclude template="/openbdtest/assets/_public_header.cfm">

<cfparam name="url.dir" default=".">

<cfscript>
results = createObject( "component", "mxunit.runner.DirectoryTestSuite" ).run( directory=url.dir, componentpath=url.componentpath, excludes="openbdtest.common.TestCase", recurse=true );
html = results.getHtmlResults();
</cfscript>

<p><a href="./">back</a></p>
<cfoutput>#html#</cfoutput>

<cfinclude template="/openbdtest/assets/_public_footer.cfm">