<!---
 *
 *  Copyright (C) 2013 TagServlet Ltd
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
 *	$Id: DeserializejsonTest.cfc 2321 2013-02-09 16:12:38Z alan $
 *
	--->
<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testString">
		<cfset assertEquals( "abc", 	deserializejson( '"abc"'))>
		<cfset assertEquals( "yes", 	deserializejson( 'true' ))>
		<cfset assertEquals( "1.0", 	deserializejson( '1.0' ))>
		<cfset assertEquals( "1", 		deserializejson( '1' ))>
	</cffunction>


	<cffunction name="testNamingDeserializejson">
		<cfset myarr = [ "v1","v2","v3"]>
		<cfset arg ='["v1","v2","v3"]'>
		<cfset assertEquals( myarr, deserializejson( arg ) )>
	</cffunction>


	<cfscript>
	function testQuerySimple(){
	
		var qry = QueryNew('col1,col2,col3');
		QueryAddRow( qry, 1 );
		QuerySetCell( qry, 'col1', "value1", 1 );
		QuerySetCell( qry, 'col2', "value2", 1 );
		QuerySetCell( qry, 'col3', "value3", 1 );
	
		// To JSON
		var str = SerializeJSON( qry );
		assertEquals( str, '{"COLUMNS":["col1","col2","col3"],"DATA":[["value1","value2","value3"]]}' );
	
		// From JSON
		var qry2 = DeserializeJSON( str );
		assertTrue( IsQuery(qry2) );
	}
	

	function testQueryInner(){
		var qry = QueryNew('col1,col2,col3');
		QueryAddRow( qry, 1 );
		QuerySetCell( qry, 'col1', "value1", 1 );
		QuerySetCell( qry, 'col2', "value2", 1 );
		QuerySetCell( qry, 'col3', "value3", 1 );
	
		var struct = {
			_success : true,
			query : qry
		};
	
		// To JSON
		var str = SerializeJSON( struct );
	
		// From JSON
		var struct2 = DeserializeJSON( str );
		assertTrue( IsQuery(struct2.query) );
	}
	
	
	</cfscript>


</cfcomponent>
