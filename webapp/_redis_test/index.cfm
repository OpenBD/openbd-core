<!DOCTYPE html>
<head>
	<title>Redis Test</title>
</head>
<body>
	<h1>Redis Test</h1>

	<cfdump var="#APPLICATION.cache#" />
	<cfloop collection="#APPLICATION.cache.regions#" item="region">
		<p><cfdump var="#cachegetmetadata(region)#" label="cache region: #region#"/></p>
	</cfloop>
</body>
</html>
