<!---
 *
 *  Copyright (C) 2011 TagServlet Ltd
 *
 *  This file is part of Open BlueDragon (OpenBD) CFML Server Engine.
 *
 *  OpenBD is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *
 *  OpenBD is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenBD.  If not, see http://www.gnu.org/licenses/
 *
 *  Additional permission under GNU GPL version 3 section 7
 *
 *  If you modify this Program, or any covered work, by linking or combining
 *  it with any of the JARS listed in the README.txt (or a modified version of
 *  (that library), containing parts covered by the terms of that JAR, the
 *  licensors of this Program grant you additional permission to convey the
 *  resulting work.
 *  README.txt @ http://www.openbluedragon.org/license/README.txt
 *
 *  http://openbd.org/
 *  $Id: cfhttp.cfc 2245 2012-08-12 21:46:53Z andy $
 *
--->
<cfcomponent extends="openbdtest.common.TestCase">
	
	
<cffunction name="testHeaders">
	<cfset var cfhttp = "">
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpresponse.cfm" method="post">
		<cfhttpparam type="header" name="foo1" value="bar1">
		<cfhttpparam type="header" name="foo2" value="bar2">
		<cfhttpparam type="header" name="foo3" value="bar3">
		<cfhttpparam type="header" name="Content-Type" value="text/xml;">
	</cfhttp>
	
	<cftry>
		<cfset var result = deserializejson( cfhttp.filecontent )>
	<cfcatch>
		<cfset fail( "Failed to deserialize result: " & cfhttp.filecontent )>
	</cfcatch>
	</cftry>	
	<cfset assertTrue( isDefined( "result.httprequest.headers.foo1" ) )>
	<cfset assertEquals( "bar1", result.httprequest.headers.foo1 )>
	<cfset assertTrue( isDefined( "result.httprequest.headers.foo2" ) )>
	<cfset assertEquals( "bar2", result.httprequest.headers.foo2 )>
	<cfset assertTrue( isDefined( "result.httprequest.headers.foo3" ) )>
	<cfset assertEquals( "bar3", result.httprequest.headers.foo3 )>
	<cfset assertTrue( structkeyexists( result.httprequest.headers, 'content-type' ) )>
	<cfset assertEquals( "text/xml;", result.httprequest.headers['content-type'] )>

</cffunction>


<cffunction name="testQueryString">
	<cfset var cfhttp = "">
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpresponse.cfm?foo1=bar1&foo2=bar%202">
	
	<cftry>
		<cfset var result = deserializejson( cfhttp.filecontent )>
	<cfcatch>
		<cfset fail( "Failed to deserialize result: " & cfhttp.filecontent )>
	</cfcatch>
	</cftry>	
	<cfset assertTrue( isDefined( "result.url.foo1" ) )>
	<cfset assertEquals( "bar1", result.url.foo1 )>
	<cfset assertTrue( isDefined( "result.url.foo2" ) )>
	<cfset assertEquals( "bar 2", result.url.foo2 )>

</cffunction>


<cffunction name="testUrlData">
	<cfset var cfhttp = "">
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpresponse.cfm">
		<cfhttpparam type="url" name="foo1" value="bar1">
		<cfhttpparam type="url" name="foo2" value="bar 2">
		<cfhttpparam type="url" name="foo3" value="bar 3&$!()@##" encoded="true">
		<cfhttpparam type="url" name="foo4" value="bar 4&$!()@##" encoded="false">
	</cfhttp>
	
	<cftry>
		<cfset var result = deserializejson( cfhttp.filecontent )>
	<cfcatch>
		<cfset fail( "Failed to deserialize result: " & cfhttp.filecontent )>
	</cfcatch>
	</cftry>	
	<cfset assertTrue( isDefined( "result.url.foo1" ) )>
	<cfset assertEquals( "bar1", result.url.foo1 )>
	<cfset assertTrue( isDefined( "result.url.foo2" ) )>
	<cfset assertEquals( "bar 2", result.url.foo2 )>
	<cfset assertTrue( isDefined( "result.url.foo3" ) )>
	<cfset assertEquals( "bar 3&$!()@##", result.url.foo3 )>
	<cfset assertTrue( isDefined( "result.url.foo4" ) )>
	<cfset assertEquals( "bar 4&$!()@##", result.url.foo4 )>

</cffunction>


<cffunction name="testFormData">
	<cfset var cfhttp = "">
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpresponse.cfm" method="post">
		<cfhttpparam type="formfield" name="foo1" value="bar1">
		<cfhttpparam type="formfield" name="foo2" value="bar 2">
		<cfhttpparam type="formfield" name="foo3" value="bar 3&$!()@##" encoded="true">
		<cfhttpparam type="formfield" name="foo4" value="bar 4" encoded="false">
	</cfhttp>
	
	<cftry>
		<cfset var result = deserializejson( cfhttp.filecontent )>
	<cfcatch>
		<cfset fail( "Failed to deserialize result: " & cfhttp.filecontent )>
	</cfcatch>
	</cftry>	
	
	<cfset assertTrue( isDefined( "result.form.foo1" ) )>
	<cfset assertEquals( "bar1", result.form.foo1 )>
	<cfset assertTrue( isDefined( "result.form.foo2" ) )>
	<cfset assertEquals( "bar 2", result.form.foo2 )>
	<cfset assertTrue( isDefined( "result.form.foo3" ) )>
	<cfset assertEquals( "bar 3&$!()@##", result.form.foo3 )>
	<cfset assertTrue( isDefined( "result.form.foo4" ) )>
	<cfset assertEquals( "bar 4", result.form.foo4 )>

	<cfset assertEquals( "foo1=bar1&foo2=bar%202&foo3=bar%203%26%24%21%28%29%40%23&foo4=bar 4", result.httprequest.content )>
</cffunction>


<cffunction name="testFormDataCharset">
	<cfset var cfhttp = "">
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpresponse.cfm" method="post" charset="windows-1252">
		<cfhttpparam type="formfield" name="test" value="abc´´def"/>
		<cfhttpparam type="header" name="charenc" value="windows-1252"/>
	</cfhttp>
	
	<cftry>
		<cfset var result = deserializejson( cfhttp.filecontent )>
	<cfcatch>
		<cfset fail( "Failed to deserialize result: " & cfhttp.filecontent )>
	</cfcatch>l
	</cftry>	

	<cfset assertEquals( "test=abc%b4%b4def", result.httprequest.content )>
	
</cffunction>

<cffunction name="testFormDataCharset2">
	<cfset var cfhttp = "">
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpresponse.cfm" method="post" charset="utf-8">
		<cfhttpparam type="formfield" name="test" value="abc´´def"/>
		<cfhttpparam type="header" name="charenc" value="utf-8"/>
	</cfhttp>
	
	<cftry>
		<cfset var result = deserializejson( cfhttp.filecontent )>
	<cfcatch>
		<cfset fail( "Failed to deserialize result: " & cfhttp.filecontent )>
	</cfcatch>l
	</cftry>	

	<cfset assertEquals( "test=abc%c2%b4%c2%b4def", result.httprequest.content )>
	
</cffunction>


<cffunction name="testBody">
	<cfset var cfhttp = "">
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpresponse.cfm" method="post" charset="utf-8">
		<cfhttpparam type="body" value="this is the body"/>
		<cfhttpparam type="header" name="Content-Type" value="text/plain"/>
	</cfhttp>
	
	<cftry>
		<cfset var result = deserializejson( cfhttp.filecontent )>
	<cfcatch>
		<cfset fail( "Failed to deserialize result: " & cfhttp.filecontent )>
	</cfcatch>l
	</cftry>	

	<cfset assertEquals( "text/plain", result.httprequest.headers['content-type'] )>
	<cfset assertEquals( "this is the body", result.httprequest.content )>
	
</cffunction>


<cffunction name="testXml">
	<cfset var cfhttp = "">
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpresponse.cfm" method="post" charset="utf-8">
		<cfhttpparam type="xml" value="this is the body"/>
	</cfhttp>
	
	<cftry>
		<cfset var result = deserializejson( cfhttp.filecontent )>
	<cfcatch>
		<cfset fail( "Failed to deserialize result: " & cfhttp.filecontent )>
	</cfcatch>
	</cftry>	
	<cfset assertEquals( "text/xml", result.httprequest.headers['content-type'] )>
	<cfset assertEquals( "this is the body", result.httprequest.content )>
	
</cffunction>


<cffunction name="testCGI">
	<cfset var cfhttp = "">
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpresponse.cfm">
		<cfhttpparam type="cgi" name="foo1" value="bar1">
		<cfhttpparam type="cgi" name="foo2" value="bar2">
		<cfhttpparam type="cgi" name="foo3" value="bar3">
	</cfhttp>
	
	<cftry>
		<cfset var result = deserializejson( cfhttp.filecontent )>
	<cfcatch>
		<cfset fail( "Failed to deserialize result: " & cfhttp.filecontent )>
	</cfcatch>
	</cftry>	
	<cfset assertTrue( isDefined( "result.httprequest.headers.foo1" ) )>
	<cfset assertEquals( "bar1", result.httprequest.headers.foo1 )>
	<cfset assertTrue( isDefined( "result.httprequest.headers.foo2" ) )>
	<cfset assertEquals( "bar2", result.httprequest.headers.foo2 )>
	<cfset assertTrue( isDefined( "result.httprequest.headers.foo3" ) )>
	<cfset assertEquals( "bar3", result.httprequest.headers.foo3 )>

</cffunction>

<cffunction name="testDownloadToFile">
	<cfscript>
	var path	= ExpandPath(".");
	var file	= "logo.png";
	var fullfilepath = Expandpath( file );
	try{
		FileDelete( fullfilepath );
	}catch( Any ignored ){}
	</cfscript>

	<cfhttp url="http://www.openbd.org/manual/sd_openBD_32.png" file="#file#" path="#path#"></cfhttp>

	<cfset assertTrue( FileExists( fullfilepath ) )>
	<cfset assertTrue( !isBinary(cfhttp.filecontent) )>
	<cfset assertEquals( 1904, cfhttp.responseheader["Content-Length"] )>
	<cfset assertEquals( 200, cfhttp.responseheader["STATUS_CODE"] )>
</cffunction>


<cffunction name="testCookies">
	<cfset var cfhttp = "">
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpresponse.cfm">
		<cfhttpparam type="cookie" name="foo1" value="this is cookie value for foo1!?">
		<cfhttpparam type="cookie" name="foo2" value="this is cookie value for foo2!?">
		<cfhttpparam type="cookie" name="foo3" value="this is cookie value for foo3!?">
	</cfhttp>
	
	
	<cftry>
		<cfset var result = deserializejson( cfhttp.filecontent )>
	<cfcatch>
		<cfset fail( "Failed to deserialize result: " & cfhttp.filecontent )>
	</cfcatch>
	</cftry>	
	
	<cfset assertEquals( "foo1=this%20is%20cookie%20value%20for%20foo1%21%3F; foo2=this%20is%20cookie%20value%20for%20foo2%21%3F; foo3=this%20is%20cookie%20value%20for%20foo3%21%3F", result.httprequest.headers.cookie )>

</cffunction>

<cffunction name="testDownloadToMemory">
	<cfscript>
	var path	= ExpandPath(".");
	var file	= "logo.png";
	var fullfilepath = Expandpath( file );
	
	try{
		FileDelete( fullfilepath );
	}catch( Any ignored ){}
	</cfscript>

	<cfhttp url="http://www.openbd.org/manual/sd_openBD_32.png" GETASBINARY="yes"></cfhttp>

	<cfset assertTrue( !FileExists(	fullfilepath ) )>
	<cfset assertTrue( isBinary(cfhttp.filecontent) )>
	<cfset assertEquals( 1904, cfhttp.responseheader["Content-Length"] )>
	<cfset assertEquals( 200, cfhttp.responseheader["STATUS_CODE"] )>
</cffunction>

<cffunction name="testBlankCharset">
	<cfset var cfhttp = "">
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpresponse.cfm?test=1" method="get" charset="">
	
	<cftry>
		<cfset var result = deserializejson( cfhttp.filecontent )>
	<cfcatch>
		<cfset fail( "Failed to deserialize result: " & cfhttp.filecontent )>
	</cfcatch>
	</cftry>	

	<cfset assertEquals( "1", result.url.test )>
	
</cffunction>


<cffunction name="testFormfield">
	<cfset var cfhttp = "">
	<cfset var uploadToken = "foobar">
	<cfset var destFolderPath = "dfpValue">
	<cfset var uploadfile = expandpath( "cfhttpresponse.cfm" )>
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpresponse.cfm?test=1" method="post" throwonerror="yes" timeout="900">
		<cfhttpparam name="uploadToken" value="#uploadtoken#" type="formfield">
		<cfhttpparam name="destFolderPath" value="#destFolderPath#" type="formfield">
		<cfhttpparam name="uploadFile" file="#uploadfile#" type="file">
	</cfhttp>

	<cftry>
		<cfset var result = deserializejson( cfhttp.filecontent )>
	<cfcatch>
		<cfset fail( "Failed to deserialize result: " & cfhttp.filecontent )>
	</cfcatch>
	</cftry>	

	<cfset assertEquals( "foobar", result.form.uploadToken)>
	<cfset assertEquals( "dfpValue", result.form.destFolderPath)>
	<cfset assertTrue( result.form.uploadFile NEQ '' )>
	
</cffunction>

<cffunction name="testRedirectFalse">
	<cfset var cfhttp = "">
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpredirect.cfm?count=1" redirect=false>

	<cfset assertEquals( "http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpredirect.cfm?count=2", cfhttp.responseheader.location )>
</cffunction>


<cffunction name="testRedirectTrue">
	<cfset var cfhttp = "">
	<cfhttp url="http://#cgi.server_name#:#cgi.server_port##cgi.context_path#/openbdtest/tags/remote/cfhttpredirect.cfm?count=1" redirect=true>

	<cfset assertEquals( "3", trim( cfhttp.filecontent ) )>
</cffunction>

</cfcomponent>