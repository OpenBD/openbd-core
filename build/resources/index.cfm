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
	<h1>OpenBD CFML Runtime</H1>
</div>

<cfdump version="long">

<div style="border-top: 1px solid silver; font-size:80%;padding: 4px; margin-top: 20px">
	<a href="http://openbd.org/">OpenBD</a> &mdash; 
	<a href="https://github.com/OpenBD">OpenBD @ GitHub</a> &mdash; 
	Build Version: <strong> <cfoutput>#server.bluedragon.builddate#</cfoutput><strong>
</div>

</body>
</html>