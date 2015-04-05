<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testEncodeForHtmlAttribute">
		<cfscript>
		var s	= encodeForHtmlAttribute( "this > a 'test < ""to see"" what happens" );	
		assertEquals( s, "this &gt; a &##39;test &lt; &quot;to see&quot; what happens" );
		</cfscript>
	</cffunction>


	<cffunction name="testEncodeForJavascript">
		<cfscript>
		var s	= encodeForJavascript( "this > a 'test < ""to see"" what happens" );	
		assertEquals( s, "this > a \'test < \""to see\"" what happens" );
		</cfscript>
	</cffunction>


	<cffunction name="testEncodeForFilename">
		<cfscript>
		var s	= encodeForFilename( "this 1234567890 '/\\t+-%$£&*()[]@##l~@:;<>""_-.doc" );
		assertEquals( s, "this1234567890t-l_-.doc" );

		var s	= encodeForFilename( "%$£&*()[]@##~@:;<>", "alan.doc" );
		assertEquals( s, "alan.doc" );
		</cfscript>
	</cffunction>


	<cffunction name="testEncodeFromStruct">
		<cfscript>
		var s	= {
			name : "alan",
			age : 21,
			address : "Dumfries, Scotland"
		};
		
		var uri	= urlFromStruct( s );
		assertEquals( uri, "name=alan&age=21&address=Dumfries%2C%20Scotland" );
		
		s.wife = {
			name : "ceri",
			age : 21
		};
		uri	= urlFromStruct( s );
		assertEquals( uri, "name=alan&age=21&address=Dumfries%2C%20Scotland&wife=%7B%22name%22%3A%22ceri%22%2C%22age%22%3A21%7D" );
		</cfscript>
	</cffunction>



</cfcomponent>