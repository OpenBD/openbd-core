<cfcomponent extends="openbdtest.common.TestCase">
	
	<cfprocessingdirective pageEncoding="utf-8">
	<cfset zipFilesDir = Expandpath("/openbdtest/tags/file/ziptestfiles/")>
	
<!----------------------------Start of Testing Create zip------------------->
	<cffunction name="testZipTag">
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		<cfset var source	= "#zipFilesDir#toZip" >
		<cfset var zipFile	= ExpandPath("./ziptag.zip")>
		
		<cftry>
				<cfzip action="create" source="#source#" zipfile="#zipFile#">
				</cfzip>
				
				<cfset assertTrue( fileexists(zipFile) )>
				
				<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#" charset="cp437"/>
				
				<cfset var file1=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/ghi.txt")>
				<cfset var file2=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/jkl.txt")>
				
				<cfset assertTrue( fileexists(file1) )>
				<cfset assertTrue( fileexists(file2) )>
				
				<cffinally><!--- cleanup --->
					<cfset cleanup(zipFile,destination)>
				</cffinally>
			
		</cftry>
	</cffunction>


	<cffunction name="testZipparamsWithNegCompLvl">
		<cfset var source	= "#zipFilesDir#toZip" >
		<cfset var zipFile	= ExpandPath("./zipwithnegComlvl.zip")>
		// Throws an exception 	Compression level cannot be -1. 	
		<cftry>
			<cfzip action="create" source="#source#" zipfile="#zipFile#" recurse = true compressionlevel = -1>
				<cfzipparam source="#source#" recurse=true filter="*" />
			</cfzip>
			<cfcatch>
					<cfset assertTrue( cfcatch.message contains "Invalid COMPRESSIONLEVEL specified. Please specify a number from 0-9 (inclusive)." )>
			</cfcatch>
		</cftry>
	</cffunction>


	<cffunction name="testZipparamsWithemptysrc">
		<cfset var source	= "">
		<cfset var zipFile	= ExpandPath("./zipwithemptysrc.zip")>
		
		<!---  Throws an exception  --->
		<cftry>
			<cfzip action="create" source="#source#" zipfile="#zipFile#" >
			</cfzip>

			<cfset fail( "Should have thrown an exception" )>
			
			<cfcatch>
				<cfset assertTrue( cfcatch.message contains "SOURCE specified is an empty string. It is not a valid path." )>
			</cfcatch>
			
		</cftry>
	</cffunction>
	

 <cffunction name="testzipFileEmptyString">
		<cfset var zipFile	= ExpandPath("./zipwithemptysrc.zip")>
		
		<cftry>
			<cfzip action="create"  zipfile="">
			</cfzip>

			<cfset fail( "Should have thrown an exception" )>
			
			<cfcatch>
				<cfset assertTrue( cfcatch.message contains "Missing the ZIPFILE argument or is an empty string." )>
			</cfcatch>
			
		</cftry>
	</cffunction>
	
	
	<cffunction name="testcharset">
		
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		
		<cftry>
			<cfset var zipfile=ExpandPath("./charset.zip")>
			<cfset var source	= "#zipFilesDir#foriegn" >
		
			//File zipping operation
			<cfzip action="create" zipfile="#zipfile#" source="#source#" charset="cp437">
			</cfzip>
			<cfset assertTrue( fileexists(zipfile) )>
			
			//extract the file being zipped
			<cfzip action="extract"  zipfile="#zipfile#" destination="#destination#" charset="cp437"/>
			
			<cfset var file=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/ﾄﾅﾓﾆｶﾅ/reí.txt")>
			
			<cfset assertTrue( fileexists(file) )>
			
			<cffinally><!--- cleanup --->
				<cfset cleanup(zipFile,destination)>
			</cffinally>
			
		</cftry>
	</cffunction>
	
	
	<cffunction name="testcompressionlevel3">
		
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		
		<cftry>
			<cfset var zipfile=ExpandPath("./charset.zip")>
			<cfset var source	= "#zipFilesDir#toZip/abc/123.txt" >
			
			//File zipping operation 
			<cfzip action="create" zipfile="#zipfile#" source="#source#" charset="cp437" compressionlevel = 3>
			</cfzip>
			
			<cfset assertTrue( fileexists(zipfile) )>
			
			//list the file being zipped 
			<cfzip action="list"  zipfile="#zipfile#" variable = "result"/>
			
			<cfset test = queryColumnArray(result, "compressedsize") />
			<cfset assertTrue(arrayContains(test, 1188))>
			
			<cffinally><!--- cleanup --->
				<cfset cleanup( zipfile = zipfile )>
			</cffinally>
			
		</cftry>
	</cffunction>
	
	
	<cffunction name="testcompressionlevel5">
		
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		
		<cftry>
			<cfset var zipfile=ExpandPath("./charset.zip")>
			<cfset var source	= "#zipFilesDir#toZip/abc/123.txt" >
			
			//File zipping operation 
			<cfzip action="create" zipfile="#zipfile#" source="#source#" charset="cp437" compressionlevel = 5>
			</cfzip>
			
			<cfset assertTrue( fileexists(zipfile) )>
			
			<!--- list the file being zipped  --->
			<cfzip action="list"  zipfile="#zipfile#" variable = "result"/>
			
			<cfset test = queryColumnArray(result, "compressedsize") />
			<cfset assertTrue(arrayContains(test, 1153))>
			
			<cffinally><!--- cleanup --->
					<cfset cleanup( zipfile = zipfile )>
			</cffinally>
			
		</cftry>
	</cffunction>
	
	
	<cffunction name="testcompressionlevel9">
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		
		<cftry>
			<cfset var zipfile=ExpandPath("./charset.zip")>
			<cfset var source	= "#zipFilesDir#toZip/abc/123.txt" >
			
			//File zipping operation 
			<cfzip action="create" zipfile="#zipfile#" source="#source#" charset="cp437" compressionlevel = 9>
			</cfzip>
			
			<cfset assertTrue( fileexists(zipfile) )>
			
			<!--- list the file being zipped  --->
			<cfzip action="list"  zipfile="#zipfile#" variable = "result"/>
			
			
			<cfset test = queryColumnArray(result, "compressedsize") />
			<cfset assertTrue(arrayContains(test, 1153))>
			
			<cffinally><!--- cleanup --->
					<cfset cleanup( zipfile = zipfile )>
			</cffinally>
			
		</cftry>
	</cffunction>


	<cffunction name="testZipfilter1">
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		<cfset var source	= "#zipFilesDir#toZip" >
		<cfset var zipFile	= ExpandPath("./zipfilter1.zip")>

		<cftry>
			<cfzip action="create" zipfile="#zipFile#" source="#source#" filter="*.txt" >
			</cfzip>
			<cfset assertTrue( fileexists(zipFile) )>
			
			<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#"/>
				
				<cfset var file1=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/ghi.txt")>
				<cfset var file2=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/jkl.txt")>
				
				<cfset assertTrue( fileexists(file1) )>
				<cfset assertTrue( fileexists(file2) )>
				
				<cffinally><!--- cleanup --->
					<cfset cleanup(zipFile,destination)>
				</cffinally>
			
		</cftry>
	</cffunction>
	
	<cffunction name="testZipfilter2">
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		<cfset var source	= "#zipFilesDir#toZip" >
		<cfset var zipFile	= ExpandPath("./zipfilter2.zip")>

		<cftry>
			<cfzip action="create" zipfile="#zipFile#" source="#source#" filter="*.exe">
			</cfzip>
			<cfset assertTrue( fileexists(zipFile) )>
			
			<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#"/>
				
				<cfset var file1=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/abc/456.exe")>
				<cfset var file2=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/mno.exe")>
				
				<cfset assertTrue( fileexists(file1) )>
				<cfset assertTrue( fileexists(file2) )>
				
				<cffinally><!--- cleanup --->
					<cfset cleanup(zipFile,destination)>
				</cffinally>
			
		</cftry>
	</cffunction>
	
	
	<cffunction name="testZiprecurse">
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		<cfset var source	= "#zipFilesDir#toZip" >
		<cfset var zipFile	= ExpandPath("./ziprecurse.zip")>

		<cftry>
			<cfzip action="create" zipfile="#zipFile#" source="#source#" recurse = false >
			</cfzip>
			<cfset assertTrue( fileexists(zipFile) )>
			
			<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#"/>
				
				<cfset var file1=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/abc/456.exe")>
				<cfset var file2=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/mno.exe")>
				<cfset var file3=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/jkl.txt")>
				
				<cfset assertFalse( fileexists(file1) )>
				<cfset assertTrue( fileexists(file2) )>
				<cfset assertTrue( fileexists(file3) )>
				
				<cffinally><!--- cleanup --->
					<cfset cleanup(zipFile,destination)>
				</cffinally>
			
		</cftry>
	</cffunction>
	
	
	<cffunction name="testZipnewpath">
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		<cfset var source	= "#zipFilesDir#toZip/abc/123.txt" >
		<cfset var zipFile	= ExpandPath("./zipnewpath.zip")>

		<cftry>
			<cfzip action="create" zipfile="#zipFile#" source="#source#" newpath = "C/hello.txt" >
			</cfzip>
			<cfset assertTrue( fileexists(zipFile) )>
			
			<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#"/>
				
				<cfset var file1=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/C/hello.txt")>
				<cfset var file2=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/123.txt")>
					
				<cfset assertTrue( fileexists(file1) )>
				<cfset assertFalse( fileexists(file2) )>
				
				<cffinally><!--- cleanup --->
					<cfset cleanup(zipFile,destination)>
				</cffinally>
			
		</cftry>
	</cffunction>
	
	
	<cffunction name="testZipprefix">
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		<cfset var source	= "#zipFilesDir#toZip" >
		<cfset var zipFile	= ExpandPath("./zipprefix.zip")>

		<cftry>
			<cfzip action="create" zipfile="#zipFile#" source="#source#" prefix = "C">
			</cfzip>
			<cfset assertTrue( fileexists(zipFile) )>
			
			<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#"/>
				
				<cfset var file1=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/C/abc/456.exe")>
				<cfset var file2=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/C/mno.exe")>
				<cfset var file3=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/C/jkl.txt")>
				<cfset var file4=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/ghi.txt")>
					
				<cfset assertTrue( fileexists(file1) )>
				<cfset assertTrue( fileexists(file2) )>
				<cfset assertTrue( fileexists(file3) )>
				<cfset assertFalse( fileexists(file4) )>
				
				<cffinally><!--- cleanup --->
					<cfset cleanup(zipFile,destination)>
				</cffinally>
			
		</cftry>
	</cffunction>
	

<!----------------------------Testing ZipParam ------------------->
	<cffunction name="testZipparamswithTwoSource">
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		
		<cftry>
			<cfset var zipfile=ExpandPath("./zipparameters.zip")>
			<cfset var source1	= "#zipFilesDir#toZip" >
			<cfset var source2	= "#zipFilesDir#foriegn" >
			
			//File zipping operation
			<cfzip action="create" zipfile="#zipfile#" charset="cp437">
				<cfzipparam source="#source1#" />
				<cfzipparam source="#source2#" />
			</cfzip>
			
			<cfset assertTrue( fileexists(zipfile) )>
			
			//extract the file being zipped
			<cfzip action="extract"  zipfile="#zipfile#" destination="#destination#" charset="cp437"/>
			
			<cfset var file1=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/abc/123.txt")>
			<cfset var file2=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/ﾄﾅﾓﾆｶﾅ/reí.txt")>
			
			<cfset assertTrue( fileexists(file1) )>
			<cfset assertTrue( fileexists(file2) )>
			
			<cffinally><!--- cleanup --->
				<cfset cleanup(zipFile,destination)>
			</cffinally>
			
		</cftry>
	</cffunction>


<cffunction name="testZipparamsfilter1">
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		<cfset var source	= "#zipFilesDir#toZip" >
		<cfset var zipFile	= ExpandPath("./zipparamfilter1.zip")>

		<cftry>
			<cfzip action="create" zipfile="#zipFile#" >
				<cfzipparam source="#source#" filter="*.txt" />
			</cfzip>
			<cfset assertTrue( fileexists(zipFile) )>
		
			<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#"/>
				
				<cfset var file1=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/ghi.txt")>
				<cfset var file2=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/jkl.txt")>
				
				<cfset assertTrue( fileexists(file1) )>
				<cfset assertTrue( fileexists(file2) )>
				
				<cffinally><!--- cleanup --->
					<cfset cleanup(zipFile,destination)>
				</cffinally>
			
		</cftry>
	</cffunction>
	
	<cffunction name="testZipparamsfilter2">
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		<cfset var source	= "#zipFilesDir#toZip" >
		<cfset var zipFile	= ExpandPath("./zipparamfilter2.zip")>

		<cftry>
			<cfzip action="create" zipfile="#zipFile#" >
				<cfzipparam source="#source#" filter="*.exe" />
			</cfzip>
			<cfset assertTrue( fileexists(zipFile) )>
		
			<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#"/>
				
				<cfset var file1=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/abc/456.exe")>
				<cfset var file2=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/mno.exe")>
				
				<cfset assertTrue( fileexists(file1) )>
				<cfset assertTrue( fileexists(file2) )>
				
				<cffinally><!--- cleanup --->
					<cfset cleanup(zipFile,destination)>
				</cffinally>
			
		</cftry>
	</cffunction>
	
	
	<cffunction name="testZipparamsrecurse">
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		<cfset var source	= "#zipFilesDir#toZip" >
		<cfset var zipFile	= ExpandPath("./zipparamrecurse.zip")>

		<cftry>
			<cfzip action="create" zipfile="#zipFile#" >
				<cfzipparam source="#source#" recurse = false />
			</cfzip>
			<cfset assertTrue( fileexists(zipFile) )>
		
			<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#"/>
				
				<cfset var file1=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/abc/456.exe")>
				<cfset var file2=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/mno.exe")>
				<cfset var file3=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/jkl.txt")>
				
				<cfset assertFalse( fileexists(file1) )>
				<cfset assertTrue( fileexists(file2) )>
				<cfset assertTrue( fileexists(file3) )>
				
				<cffinally><!--- cleanup --->
					<cfset cleanup(zipFile,destination)>
				</cffinally>
			
		</cftry>
	</cffunction>
	
	<cffunction name="testZipparamsprefix">
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		<cfset var source	= "#zipFilesDir#toZip" >
		<cfset var zipFile	= ExpandPath("./zipparamprefix.zip")>

		<cftry>
			<cfzip action="create" zipfile="#zipFile#" >
				<cfzipparam source="#source#" prefix = "C" />
			</cfzip>
			<cfset assertTrue( fileexists(zipFile) )>
		
			<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#"/>
				
				<cfset var file1=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/C/abc/456.exe")>
				<cfset var file2=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/C/mno.exe")>
				<cfset var file3=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/C/jkl.txt")>
				<cfset var file4=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/ghi.txt")>
					
				<cfset assertTrue( fileexists(file1) )>
				<cfset assertTrue( fileexists(file2) )>
				<cfset assertTrue( fileexists(file3) )>
				<cfset assertFalse( fileexists(file4) )>
				
				<cffinally><!--- cleanup --->
					<cfset cleanup(zipFile,destination)>
				</cffinally>
			
		</cftry>
	</cffunction>
	
	
	<cffunction name="testZipparamsnewpath">
		<cfset var destination	= ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/")>
		<cfset var source	= "#zipFilesDir#toZip/abc/123.txt" >
		<cfset var zipFile	= ExpandPath("./zipparamnewpath.zip")>

		<cftry>
			<cfzip action="create" zipfile="#zipFile#" >
				<cfzipparam source="#source#" newpath = "C/hello.txt" />
			</cfzip>
			<cfset assertTrue( fileexists(zipFile) )>
		
			<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#"/>
				
				<cfset var file1=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/C/hello.txt")>
				<cfset var file2=ExpandPath("/openbdtest/tags/file/ziptestfiles/unZipTag/123.txt")>
					
				<cfset assertTrue( fileexists(file1) )>
				<cfset assertFalse( fileexists(file2) )>
				
				<cffinally><!--- cleanup --->
					<cfset cleanup(zipFile,destination)>
				</cffinally>
			
		</cftry>
	</cffunction>
	

<!----------------------------End of Testing ZipParam ------------------->

<!----------------------------End of Testing Create zip------------------->

<!----------------------------Start of Testing list zip------------------->
	<cffunction name="testtagZipList">
		<cfset var zipFile	= "#zipFilesDir#toUnzip.zip" >
		
		<cfzip action="list" zipfile="#zipFile#" variable = "result"  />
		
		<cfset assertTrue(isQuery(result) )>
		<cfset assertTrue(10,result.recordcount )>
		<cfset test = queryColumnArray(result, "name") />
		<cfset assertTrue(arrayContains(test, 'toUnzip/abc/123.txt'))>
	</cffunction>
	
	
	<cffunction name="testtagZipList2">
		<cfset var zipFile	= "#zipFilesDir#/foriegn.zip" >

		<cfzip action="list" zipfile="#zipFile#" variable = "result" charset="cp437"/>
		
		<cfset assertTrue( isQuery(result) )>
		<cfset assertTrue( 4,result.recordcount )>
		<cfset test = queryColumnArray(result, "name") />
		<cfset assertTrue(arrayContains(test, 'foriegn/ﾄﾅﾓﾆｶﾅ/reí.txt'))>
	</cffunction>

<!----------------------------End of Testing List zip------------------->

<!----------------------------Start of Testing extract zip------------------->
	
	<cffunction name="testflattentrue">
		<cfset var zipFile	= "#zipFilesDir#toUnzip.zip" >
		<cfset var destination	= ExpandPath("unZipTag/")>
		<cfset Directorycreate(destination)>
		<cftry>
			
			<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#" flatten = true/>
			
			<cfset assertTrue( fileexists( "#destination#ghi.txt" ) )>
			<cfset assertTrue( fileexists( "#destination#123.txt") )>
			<cfset assertFalse( fileexists( "#destination#abc/123.txt" ) )>
			
		<cffinally><!--- cleanup --->
			<cfset cleanup(destination = destination)>
		</cffinally>
		
		</cftry>
	</cffunction>


	<cffunction name="testoverwritefalse">
		<cfset var zipFile	= "#zipFilesDir#toUnzip.zip" >
		<cfset var destination	= ExpandPath("./testOverwrite/")>
		<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#" overwrite = false />
		
		<cftry>
			<cfset assertTrue(directoryexists(destination))>
			<cfset assertTrue( fileexists( "#destination#toUnzip/abc/123.txt" ) )>
			
			<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#" overwrite = false />
			
			<cfcatch>
				<cfset assertTrue( cfcatch.message contains "File already exist" )>
			</cfcatch>
			
			<cffinally><!--- cleanup --->
			<cfset cleanup(destination = destination)>
		</cffinally>
		
		</cftry>
	</cffunction>


	<cffunction name="testcharsetextract">
		<cfset var zipFile	= "#zipFilesDir#foriegn.zip" >
		<cfset var destination	= ExpandPath("./testCharset/")>
		
		<cftry>
			
			<cfzip action="extract"  zipfile="#zipFile#" destination="#destination#" charset = "cp437" />
			
			<cfset assertTrue( fileexists( "#destination#foriegn/ﾄﾅﾓﾆｶﾅ/reí.txt" ) )>
			
			<cffinally><!--- cleanup --->
			<cfset cleanup(destination = destination)>
		</cffinally>
		
		</cftry>
	</cffunction>


<!----------------------------End of Testing Extract zip------------------->

	
	<cffunction name="cleanup" access="private" output="no" returntype="void">
	
				<cfargument name="zipFile">
				<cfargument name="destination">
				
				<!--- cleanup file--->
				<cfif fileExists( arguments.zipFile )>
					<cfset fileDelete( arguments.zipfile )>
				</cfif>
				
				<!--- cleanup directory--->
				<cfif directoryexists( arguments.destination )>
					<cfset DirectoryDelete( path = arguments.destination, recurse = true  )>
				</cfif>
			
	</cffunction>

	
</cfcomponent>
