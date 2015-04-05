<cfcomponent extends="openbdtest.common.TestCase">

	<cfset zipFilesDir = Expandpath("/openbdtest/tags/file/ziptestfiles/")>
	<cfscript>
	
// ---------------------------------------------------------------------------------


// Test normal zipping of source directory
function simpleZip(){
	
	var testdestFile = ExpandPath("./test1.zip");
	var testsourceFile1 = "#zipFilesDir#toZip";
	var unzipAt = ExpandPath("./testzip/");
	
	try{
		assertTrue( zip( zipfile = testdestFile, zipparams = [{source : testsourceFile1} ]) );
		
		// Test file after overwrite
		assertFalse( directoryexists( unzipAt ) );
		unzip( zipfile = testdestFile, destination = unzipAt );
		assertTrue( directoryexists( unzipAt ) );
			
		assertTrue( fileexists( "#unzipAt#/abc/123.txt") );
		
	}finally{
		try{ fileDelete(testdestFile); }catch( Any ignored ){}
		try{ directoryDelete(path=unzipAt, recurse = true); }catch( Any ignored ){}
	}
}



// Test without zipparams
function zipWithEmtyZipFile(){
	try {
		zip(zipfile = "");
	
		fail( "zip attempt should have failed" );
		
	} catch (Any e){
		assertEquals(e.message,"Missing the ZIPFILE argument or is an empty string." );
	}
}



// Test  zipparams without src
function zipWithoutSrcinZipParams(){
	try {
		var testdestFile = ExpandPath("/openbdtest/functions/files/ZipWithoutzipparams.zip");
	
		zip(zipfile = testdestFile ,zipparams = [{recurse : true}] );
	
		fail( "zip attempt should have failed" );
		
	} catch (Any e){
		assertEquals(e.message,"The SOURCE value is missing for zipparams" );
		fileDelete(testdestFile);
	}
}



//Test compression level when negative
function testCompressionNegValue2(compressionlevel) {
	var testdestFile = ExpandPath("./compressneg.zip");
  
	var testsourceFile =  "#zipFilesDir#toZip";
  
	var compressionlevel = -10;
  
	try{
		zip (zipfile=testdestFile,compressionLevel: compressionlevel, zipparams = [{source : testsourceFile}] );
       
		fail( "Zip attempt should have failed due to compression level out of range" );
	} catch ( Any e ) {
		assertEquals(e.message,"Invalid COMPRESSIONLEVEL specified. Please specify a number from 0-9 (inclusive)." );
		fileDelete(testdestFile);
	}
}



//Test newpath functionality
function testNewPath1() {

	var testdestFile = ExpandPath( "./changepath.zip" );
	var testsourceFile =  "#zipFilesDir#toZip/ghi.txt";
	
	try{
		assertTrue( zip( zipfile=testdestFile, zipparams = [{source : testsourceFile, newpath: "newpath/ghi.txt"}] ) );
		assertTrue( fileexists( testdestFile ) );
		
		var unzipAt = ExpandPath("./testnewpath/");
		unzip( zipfile = testdestFile, destination = unzipAt);
		assertTrue( directoryexists( unzipAt ) );
		
		var file1 = "#unzipAt#newpath/ghi.txt";
		assertTrue( fileexists(file1) );

	}finally{		
		//clean up	
		fileDelete( testdestFile );
		directoryDelete(path=unzipAt, recurse = true);
	}
}



// Test overwrite functionality
function testOverWriteTrue() {

	var testdestFile = ExpandPath("./overwrite2.zip");
	var testsourceFile =  "#zipFilesDir#toZip";
	
	var unzipFolder = ExpandPath("./testoverwrite/");
	// overwrites the content of existing zip file
	assertTrue( zip (zipfile=testdestFile, source=testsourceFile, overwrite=true));
	
	// Test file after overwrite
	unzip( zipfile = testdestFile, destination = unzipFolder);
	
	var file = "#unzipFolder#/def/hello.txt";

	assertTrue( fileexists(file) );
	// cleanup
	fileDelete(testdestFile);
	directoryDelete(path=unzipFolder, recurse = true);
}
	
	
	
// Test overwrite functionality
function testOverWriteFalse() {
	var testdestFile = ExpandPath("./overwriteFalse.zip");

	try {
	
		var testsourceFile =  "#zipFilesDir#toZip";
	
		// overwrites the content of existing zip file
		assertTrue( zip (zipfile=testdestFile, source=testsourceFile, overwrite=false));
		
		var unzipAt = ExpandPath("./testoverwrite/");
		unzip( zipfile = testdestFile, destination = unzipAt);

		assertTrue( directoryexists( "#unzipAt#" ));
	
		// file in root of zip should exist	
		assertTrue( fileexists( "#unzipAt#ghi.txt" ));
		assertTrue( fileexists( "#unzipAt#jkl.txt" ));
	
		try{
			zip (zipfile=testdestFile, source=testsourceFile, overwrite=false);
			fail( "Should have thrown an exception as file exists and overwrite is false" );
		}catch( Any e ){
			assertTrue( e.message contains "Cannot overwrite the existing file" );
		}
		
	} finally{
		fileDelete(testdestFile);
 	}
}



//Test recurse functionality
function testRecurse1() {

	var testdestFile = ExpandPath("./recurse.zip");
	var testsourceFile =  "#zipFilesDir#toZip";
	
	assertTrue( zip (zipfile=testdestFile,zipparams = [{source : testsourceFile, recurse: false}]) );
	
	var unzipAt = ExpandPath("./testrecurse/");
	unzip( zipfile = testdestFile, destination = unzipAt);

	assertTrue( directoryexists( "#unzipAt#" ));
	
	// file in root of zip should exist	
	assertTrue( fileexists( "#unzipAt#ghi.txt" ));
	assertTrue( fileexists( "#unzipAt#jkl.txt" ));
	// directory from the zip should not exist in extracted dir
	assertFalse( directoryExists("#unzipAt#/abc" ) );

	// clean up	
	fileDelete(testdestFile);
	directoryDelete(path=unzipAt, recurse = true);
	
}



// Test prefix functionality
function testPrefix() {

	var testdestFile = ExpandPath("./prefix.zip");
	
	var testsourceFile =  "#zipFilesDir#toZip";
	
	assertTrue( zip (zipfile=testdestFile, source=testsourceFile, prefix="c/") );
	
	var unzipAt = ExpandPath("testprefix");
	unzip( zipfile = testdestFile, destination = unzipAt);

	// this file should exist as the prefix was specified
	assertTrue( fileexists( ExpandPath( "./testprefix/c/ghi.txt" ) ) );
	assertTrue( fileexists( ExpandPath( "./testprefix/c/abc/123.txt" ) ) );
	
	// this file shouldn't exist as the prefix was specified
	assertFalse( fileExists( ExpandPath( "./testprefix/abc/123.txt" ) ) );
	
	fileDelete( testdestFile );
	directoryDelete(path=unzipAt, recurse = true);
}


//Test filter functionality
function testFilter1() {
	try {
		var testdestFile = ExpandPath("./filter1.zip");
		var testsourceFile =  "#zipFilesDir#toZip";

		assertFalse( fileExists( testdestFile ) );		
		assertTrue( zip( zipfile=testdestFile, zipparams = [{source : testsourceFile, filter: "*.txt"}] ) );
		
		var unzipAt = ExpandPath("testfilter");
		unzip( zipfile = testdestFile, destination = unzipAt );

		// confirm the zip file exists	
		assertTrue( fileexists(testdestFile) );

		// this file should exist as the prefix was specified
		assertTrue( fileexists( ExpandPath( "./testfilter/abc/123.txt" ) ) );
		assertTrue( fileexists( ExpandPath( "./testfilter/def/789.txt" ) ) );
		assertTrue( fileexists( ExpandPath( "./testfilter/ghi.txt" ) ) );
		
		var listing = ziplist( zipfile = testdestFile );
		assertEquals( 5, listing.recordcount );
		
	} catch (Any e) {
		fail( e.message );
	}finally{
		fileDelete(testdestFile);
		directoryDelete(path=unzipAt, recurse = true);
	}
}
// ---------------------------------------------------------------------------------
</cfscript>
 
</cfcomponent>
