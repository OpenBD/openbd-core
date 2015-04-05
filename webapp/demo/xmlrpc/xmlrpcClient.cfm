<!--
xmlrpc-1.2-b1.jar was replaced with the latest XMLRPC classes/JARs.
Currently this is version 3.1.2:
http://ws.apache.org/xmlrpc/download.html
This was done to fix OpenBD bug #196
-->

<cfset contextPath = "/xmlrpcServer">
<cfset aliasToXmlRpcService = "/xmlrpc">
<cfset serverName="http://#CGI.SERVER_NAME#:#CGI.SERVER_PORT##contextPath##aliasToXmlRpcService#">
<cfset methodName="Calculator.add">
<cfscript>
  myArray = arrayNew(1);
  myArray[1] = 10;
  myArray[2] = 2;
</cfscript>

<!--- cfxmlrpc server="" method="" params="">
 * Returns: a structure named 'xmlrpc' which has
 * success = false | true
 * result  = cfml structure with results
 * error 	= error message
 --->
<CFXMLRPC SERVER="#serverName#" METHOD="#methodName#" PARAMS="#myArray#">
<p>result should be 12</p>
<cfdump var="#xmlrpc#">

<hr>
<cfset methodName="Calculator.subtract">
<CFXMLRPC SERVER="#serverName#" METHOD="#methodName#" PARAMS="#myArray#">
<p>result should be 8</p>
<cfdump var="#xmlrpc#">


<hr>
<cfscript>
  myArray[1] = "daffy";
  myArray[2] = " duck";
</cfscript>

<cfset methodName="Calculator.concat">
<CFXMLRPC SERVER="#serverName#" METHOD="#methodName#" PARAMS="#myArray#">
<p>result should be "daffy duck"</p>
<cfdump var="#xmlrpc#">
