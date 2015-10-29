<!---
 *
 *  Copyright (C) 2015 aw2.0 Ltd
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
	--->
<cfcomponent extends="openbdtest.common.TestCase">

<cfscript>



function beforeTests() {
	var cacheProps = {
	  "type": "mongo",
	  "server": "127.0.0.1:27017",
	  "mongoclienturi": "mongodb://127.0.0.1:27017",
	  "db": "devcache"
	};

	cacheRegionNew( "mongotest", cacheProps );
}


function afterTests() {

	CacheRemoveAll( region="mongotest" );

}



function testCacheSimple(){
	assertTrue( CacheRegionExists("mongotest") );

	var cacheValue = now();

	CachePut( region="mongotest", id="id1", value=cacheValue );
	assertEquals( cacheValue, CacheGet("id1", "mongotest") );

	CacheRemove( "id1", "mongotest" );
	assertTrue( isNull( CacheGet("id1", "mongotest") ) );
}



function testCacheClear(){
	assertTrue( CacheRegionExists("mongotest") );

	var cacheValue = now();

	CachePut( region="mongotest", id="id1", value=cacheValue );
	CachePut( region="mongotest", id="id2", value=cacheValue );

	CacheRemoveAll( region="mongotest" );

	assertTrue( isNull( CacheGet("id1", "mongotest") ) );
	assertTrue( isNull( CacheGet("id2", "mongotest") ) );
}



function testParitalCacheClear(){
	assertTrue( CacheRegionExists("mongotest") );

	var cacheValue = now();

	CachePut( region="mongotest", id="matthew-id", value=cacheValue );
	CachePut( region="mongotest", id="alice-id", value=cacheValue );

	CacheRemove( id="alice", region="mongotest", exact=false );

	assertTrue( !isNull( CacheGet("matthew-id", "mongotest") ) );
	assertTrue( isNull( CacheGet("alice-id", "mongotest") ) );
}




function testCacheTimespan(){
	assertTrue( CacheRegionExists("mongotest") );

	var cacheValue = now();

	CachePut( region="mongotest", id="id1", value=cacheValue, timespan=CreateTimeSpan(0,0,0,5) );

	assertTrue( !isNull( CacheGet( "id1", "mongotest" ) ) );
	assertEquals( cacheValue, CacheGet("id1", "mongotest") );

	sleep( 7000 );
	assertTrue( isNull( CacheGet( "id1", "mongotest" ) ) );
}



</cfscript>

</cfcomponent>
