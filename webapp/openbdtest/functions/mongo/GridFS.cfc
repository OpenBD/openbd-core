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
 *  $Id: GridFS.cfc 2000 2012-03-25 14:13:44Z alan $
	--->
<cfcomponent extends="openbdtest.common.TestCase">

<cfscript>
// ---------------------------------------------------------------------------------

function testAddRemoveFindOne(){
	MongoRegister( name="mongo", server="127.0.0.1", db="openbd" );
	var id = MongoGridfsSave( "mongo", "filebucket", ExpandPath("MongoDatabase.cfc"), "mongodatabase.cfc", "text/plain", "4f6e7d23c243305d41cd748c", {custom1:1, custom2:"age"} );

	var f	= MongoGridfsFindOne(datasource="mongo", bucket="filebucket", _id=id);
	assertEquals( f._id, 					"4f6e7d23c243305d41cd748c" );
	assertEquals( f.filename, 		"mongodatabase.cfc" );
	assertEquals( f.contentType, 	"text/plain" );
	assertTrue( f.length > 0 );
	assertEquals( f.metadata.custom1, 1 );
	assertEquals( f.metadata.custom2, "age" );

	// Delete it from the database
	MongoGridfsRemove( datasource="mongo", bucket="filebucket", _id=id );

	f	= MongoGridfsFindOne(datasource="mongo", bucket="filebucket", _id=id);
	assertTrue( StructCount(f) == 0 );
}

// ---------------------------------------------------------------------------------

function testAddFindRemove(){
	MongoRegister( name="mongo", server="127.0.0.1", db="openbd" );
	var id = MongoGridfsSave( "mongo", "filebucket", ExpandPath("MongoDatabase.cfc"), "mongodatabase.cfc", "text/plain", "4f6e7d23c243305d41cd748c", {custom1:1, custom2:"age"} );

	var arry	= MongoGridfsFind(datasource="mongo", bucket="filebucket", query={filename:"mongodatabase.cfc"} );
	assertTrue( ArrayLen(arry) == 1 );

	// Delete it from the database
	MongoGridfsRemove( datasource="mongo", bucket="filebucket", _id=id );
	f	= MongoGridfsFindOne(datasource="mongo", bucket="filebucket", _id=id);
	assertTrue( StructCount(f) == 0 );
}


// ---------------------------------------------------------------------------------

function testAddGetRemove(){
	MongoRegister( name="mongo", server="127.0.0.1", db="openbd" );
	var id = MongoGridfsSave( "mongo", "filebucket", ExpandPath("MongoDatabase.cfc"), "mongodatabase.cfc", "text/plain", "4f6e7d23c243305d41cd748c", {custom1:1, custom2:"age"} );

	var res	= MongoGridfsGet(datasource="mongo", bucket="filebucket", filepath=ExpandPath('deleteme.cfc'), _id=id );
	assertTrue( res );
	FileDelete( ExpandPath('deleteme.cfc') );

	// Delete it from the database
	MongoGridfsRemove( datasource="mongo", bucket="filebucket", _id=id );
	f	= MongoGridfsFindOne(datasource="mongo", bucket="filebucket", _id=id);
	assertTrue( StructCount(f) == 0 );
}

</cfscript>

</cfcomponent>