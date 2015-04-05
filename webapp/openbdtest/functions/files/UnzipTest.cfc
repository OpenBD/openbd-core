<cfcomponent extends="openbdtest.common.TestCase">
<cfprocessingdirective pageEncoding="utf-8">
<cfset zipFilesDir = Expandpath("/openbdtest/tags/file/ziptestfiles/")>
	
<cfscript>

// ---------------------------------------------------------------------------------
//Test normal unzipping of source directory
function testUnZip() {
	var testdestFile = ExpandPath("./testUnZip/");
	var testsourceFile =  "#zipFilesDir#toUnzip.zip";
	
	try{
		assertTrue( unzip (zipfile =testsourceFile, destination=testdestFile) );
		assertTrue(fileexists( "#testDestFile#toUnzip/ghi.txt"));
		
	}finally{
		directoryDelete(path=testdestFile, recurse = true);
	}
}
	
	
	
//Test charset functionality
function testCharset1() {
	var testdestFile = ExpandPath("./testCharset1/");
	var testsourceFile =  "#zipFilesDir#/foriegn.zip";
	
	try{
		assertTrue( unzip (zipfile =testsourceFile, destination=testdestFile, charset="CP437") );
		assertTrue( fileexists( "#testdestFile#foriegn/ﾄﾅﾓﾆｶﾅ/reí.txt" ) );
	}finally{
		directoryDelete(path=testdestFile, recurse = true);
	}
}



//Test flatten functionality
function testFlatten1() {
	var testdestFile = ExpandPath("./testFlatten1/");
	var testsourceFile =  "#zipFilesDir#toUnzip.zip";
	
	try{
		// Directory structure was not retain
		assertTrue( unzip (zipfile=testsourceFile, destination=testdestFile, flatten=true) );
		
		assertTrue( fileexists( "#testdestfile#ghi.txt" ) );
		assertTrue( fileexists( "#testdestfile#123.txt" ) );
		assertFalse( fileexists( "#testdestfile#abc/123.txt" ) );
	}finally{
		directoryDelete(path=testdestFile, recurse = true);
	}
}


//Test overwrite functionality: annot overwrite when file already exists
function testOverwrite() {
	var testdestFile = ExpandPath("./testOverwrite/");
	var testsourceFile =  "#zipFilesDir#toUnzip.zip";
	
	try{
		assertTrue( unzip (zipfile=testsourceFile, destination=testdestFile, overwrite=false) );
		
		assertTrue(directoryexists(testdestFile));
		assertTrue( fileexists( "#testdestfile#toUnzip/abc/123.txt" ) );
		assertTrue( unzip (zipfile=testsourceFile, destination=testdestFile, overwrite=false) );
		
	} catch(Any e) {
		assertTrue(e.message contains "File already exist");
	} finally{
		directoryDelete(path=testdestFile, recurse = true);
	}
}

// ---------------------------------------------------------------------------------
</cfscript>

</cfcomponent>
