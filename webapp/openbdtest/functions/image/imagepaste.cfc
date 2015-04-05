<cfcomponent extends="openbdtest.common.TestCase">

<cffunction name="testImagePaste">

	<cfset upc = imageNew( src="#ExpandPath('sample.png')#" )>
	<cfset assertTrue( isImage(upc) )>

	<cfset pImage = imageRead( src="#ExpandPath('marty-blur.png')#")>
	<cfset assertTrue( isImage(pImage) )>

	<cfset imagePaste( name=upc,name2="#pImage#",x="5",y="5" )>

</cffunction>


<cffunction name="testImageWrite">
	<cfscript>
		var image = imageNew( src="#ExpandPath('sample.png')#" );
		imageResize(image, 25, 25);
		imageWrite(image, "#ExpandPath('test.jpg')#", true, 0.4);
		assertTrue( isImage(imageRead( src="#ExpandPath('test.jpg')#")) );
		FileDelete( ExpandPath('test.jpg') );
		return true;
	</cfscript>
</cffunction>


</cfcomponent>