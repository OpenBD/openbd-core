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
 *  $Id: MongoDatabase.cfc 2488 2015-01-28 01:35:58Z alan $
	--->
<cfcomponent extends="openbdtest.common.TestCase">

<cfscript>
// ---------------------------------------------------------------------------------

function testCreateDataSource(){
	if ( !MongoIsValid(name="mongo") )
		MongoRegister( name="mongo", server="127.0.0.1", db="openbd" );
	var dbs	= MongoDatabaseList( "mongo" );
	assertTrue( isArray(dbs) );
}

// ---------------------------------------------------------------------------------

function testCollectionList(){
	if ( !MongoIsValid(name="mongo") )
		MongoRegister( name="mongo", server="127.0.0.1", db="openbd" );

	var dbs	= MongoCollectionList( "mongo" );
	assertTrue( isArray(dbs) );
}


// ---------------------------------------------------------------------------------

function testDatabaseStats(){
	if ( !MongoIsValid(name="mongo") )
		MongoRegister( name="mongo", server="127.0.0.1", db="openbd" );
	var stats	= MongoDatabaseStats( "mongo" );
	assertTrue( isStruct(stats) );
	assertEquals("127.0.0.1:27017", stats.serverUsed );
	assertEquals("openbd", stats.db );
}

// ---------------------------------------------------------------------------------

function testRunCommand(){
	if ( !MongoIsValid(name="mongo") )
		MongoRegister( name="mongo", server="127.0.0.1", db="openbd" );
	var cmd	= MongoDatabaseRunCmd( "mongo", "ping" );
	assertEquals("127.0.0.1:27017", cmd.serverUsed );

	var cmd	= MongoDatabaseRunCmd( "mongo", {"ping":1} );
	assertEquals("127.0.0.1:27017", cmd.serverUsed );
}

// ---------------------------------------------------------------------------------

function testCollectionInsert(){
	if ( !MongoIsValid(name="mongo") )
		MongoRegister( name="mongo", server="127.0.0.1", db="openbd" );

	MongoCollectionDrop( "mongo", "mycoll" );

	MongoCollectionInsert( "mongo", "mycoll", {name:"alan", age: 21, dob: now()} );

	MongoCollectionIndexEnsure("mongo", "mycoll", { "id": 1 }, "myindex", false );

	var id	= MongoCollectionInsert( "mongo", "mycoll", "{name:'andy', age: 21}" );
	assertTrue( IsSimpleValue(id) );

	count	= MongoCollectionInsert( "mongo", "mycoll", [{name:"sam", age: 22, dob: now()}, {name:"tom", age: 23, dob: now()}] );
	assertEquals( 2, ArrayLen(count) );


	count	= MongoCollectionCount( "mongo", "mycoll" );
	assertEquals( 4, count );

	count	= MongoCollectionCount( "mongo", "mycoll", {age:21} );
	assertEquals( 2, count );

	count	= MongoCollectionCount( "mongo", "mycoll", "{age:23}" );
	assertEquals( 1, count );

	// Remove
	MongoCollectionRemove( "mongo", "mycoll", {age:21} );
	count	= MongoCollectionCount( "mongo", "mycoll", {age:21} );
	assertEquals( 0, count );


}

// ---------------------------------------------------------------------------------

function testCollectionSave(){
	if ( !MongoIsValid(name="mongo") )
		MongoRegister( name="mongo", server="127.0.0.1", db="openbd" );

	MongoCollectionDrop( "mongo", "mycoll" );

	var newid	= MongoCollectionInsert( "mongo", "mycoll", {name:"alan", age: 21, dob: now()} );

	var obj	= MongoCollectionFindOne("mongo","mycoll",{age:21});
	assertEquals( ToString(obj._id), newid );

	obj.age	= 22;
	MongoCollectionSave("mongo","mycoll",obj);

	obj	= MongoCollectionFindOne("mongo","mycoll",{age:22});
	assertEquals( 22, obj.age );
}

// ---------------------------------------------------------------------------------

function testId(){
	if ( !MongoIsValid(name="mongo") )
		MongoRegister( name="mongo", server="127.0.0.1", db="openbd" );

	MongoCollectionDrop( "mongo", "mycoll" );

	var newid	= MongoCollectionInsert( "mongo", "mycoll", {name:"alan", age: 21, dob: now()} );

	var obj	= MongoCollectionFindOne("mongo","mycoll",{age:21} );
	var id	= ToString(obj._id);
	assertEquals( id, newid );
	
	var x = MongoCollectionFindOne( "mongo", "mycoll", {_id:MongoObjectid(id)} );
	id	= ToString(x._id);
	assertEquals( id, newid );
}

// ---------------------------------------------------------------------------------

</cfscript>

</cfcomponent>