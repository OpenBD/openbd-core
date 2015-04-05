<html>
<head>
	<title>Open BlueDragon CFML Demo Page</title>
	<style>
	body {
		font: Verdana;
	}
	
	#header {
		border-bottom: 1px solid silver;
		margin-bottom: 10px;
	}
	
	#header h1 {
		font-size: 16pt;
	}
	
	pre.fileSrc {
		font-size: 90%;
		margin-top: 20px;
		border: 1px solid blue;
		padding: 10px;
		background-color: #EDF7FF;
	}
	</style>
</head>

<body bgcolor=#FFFFFF>

<div id="header">
	<h1>OpenBlueDragon CFML Runtime: <a href="index.cfm">Demo Page</a></H1>

<cfsilent>
	<cfset fileSrc = FileRead( ExpandPath( cgi.script_name.substring( cgi.script_name.lastIndexOf("/")+1) ) )>

	<cfset fileSrc = fileSrc.substring( fileSrc.indexOf('header.inc">') + 12 )>
	<cfset fileSrc = fileSrc.substring( 0, fileSrc.indexOf('<cfinclude template="footer.inc">') )>
	<cfset fileSrc = Trim( fileSrc )>
	
</cfsilent>

<pre class="fileSrc"><cfoutput><strong>CFML Source for this page</strong>

#HtmlEditFormat(fileSrc)#</cfoutput></pre>

</div>