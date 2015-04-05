<cfcomponent extends="openbdtest.common.TestCase">

	<cfprocessingdirective pageEncoding="utf-8">


	<cfscript>
		// ---------------------------------------------------------------------------------
		
		//Test normal unzipping of source directory
		function testUnZip() {
		
			var testFile	= ExpandPath("/openbdtest/tags/file/ziptestfiles/toUnzip.zip");
		
			var list = ziplist( zipfile=testFile );
			
			assertTrue(isQuery(list) );
			assertEquals( 10, list.recordcount);
			
		}
			
			
		//Test normal unzipping of source directory with charset
		function testUnZipWithCharset() {
			var testFile	= ExpandPath("/openbdtest/tags/file/ziptestfiles/foriegn.zip");
			
			var testCharset = "CP437"	;
			
			var list = ziplist (zipfile =testFile, charset=testCharset) ;
			
			assertTrue(isQuery(list) );
			assertEquals( 4, list.recordcount);
			test = queryColumnArray(list, "name");
			
			assertTrue(arrayContains(test, 'foriegn/ﾄﾅﾓﾆｶﾅ/reí.txt'));
		}
		
		
		
		//Test unzipping of source directory without charset
		function testUnZipWithoutCharset() {
			var testFile	= ExpandPath("/openbdtest/tags/file/ziptestfiles/danish.zip");
			
			var list = ziplist (zipfile =testFile) ;
			assertTrue(isQuery(list) );
			assertEquals( 2, list.recordcount);
			test = queryColumnArray(list, "name"); 
			
			//expected õvelser2007 080.txt but received ävelser2007 080.txt
			assertFalse(arrayContains(test, 'danish/õvelser2007 080.txt'));
			assertTrue(arrayContains(test, 'danish/ävelser2007 080.txt'));
		}
		
		// ---------------------------------------------------------------------------------
	</cfscript>
 
</cfcomponent>
