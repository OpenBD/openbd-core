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
 *  $Id: cachememory.cfc 2132 2012-06-27 19:02:56Z alan $
	--->
<cfcomponent extends="openbdtest.common.TestCase">

<cfscript>
// ---------------------------------------------------------------------------------

function testCacheSimple(){
	CacheRegionNew( "testregion", {type:"memorydisk", size:5, diskpersist:false} );
	assertTrue( CacheRegionExists("testregion") );

	var cacheValue = now();

	CachePut( region="testregion", id="id1", value=cacheValue );
	assertEquals( cacheValue, CacheGet("id1", "testregion") );

	CacheRemove( "id1", "testregion" );
	assertTrue( isNull( CacheGet("id1", "testregion") ) );
}

// ---------------------------------------------------------------------------------

function testCacheClear(){
	CacheRegionNew( "testregion", {type:"memorydisk", size:5, diskpersist:false} );
	assertTrue( CacheRegionExists("testregion") );

	var cacheValue = now();

	CachePut( region="testregion", id="id1", value=cacheValue );
	CachePut( region="testregion", id="id2", value=cacheValue );

	CacheRemoveAll( region="testregion" );
	assertTrue( isNull( CacheGet("id1", "testregion") ) );
	assertTrue( isNull( CacheGet("id2", "testregion") ) );
}

// ---------------------------------------------------------------------------------

function testCacheAge(){
	CacheRegionNew( "testregion", {type:"memorydisk", size:5, diskpersist:false} );
	assertTrue( CacheRegionExists("testregion") );

	var cacheValue = now();

	CachePut( region="testregion", id="id1", value=cacheValue, timespan=CreateTimeSpan(0,0,0,5) );

	assertEquals( cacheValue, CacheGet("id1", "testregion") );

	sleep( 7000 );
	assertTrue( isNull( CacheGet( "id1", "testregion" ) ) );
}

// ---------------------------------------------------------------------------------
</cfscript>

</cfcomponent>
