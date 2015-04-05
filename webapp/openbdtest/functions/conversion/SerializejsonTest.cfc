<!---
 *
 *  Copyright (C) 2010 TagServlet Ltd
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
 *  http://www.openbluedragon.org/
 *
--->
<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testNamingSerializejson">

	<cfset myarr = [ "v1","v2","v3"]>
	<cfset res ='["v1","v2","v3"]'>

	<cfset assertEquals( res, serializejson( myarr ) )>
	<cfset assertEquals( res, serializejson( object=myarr ) )>
	<cfset assertEquals( serializejson( myarr ) , serializejson( object=myarr ) )>
	<cfset assertEquals( serializejson( object=myarr ), serializejson( myarr ) )>

	<cfset assertEquals(res, serializejson( myarr,"true" ) )>
	<cfset assertEquals(res, serializejson( object=myarr, sercols="true" ) )>
	<cfset assertEquals(serializejson( myarr,"true" ), serializejson( object=myarr, sercols="true" ) )>
	<cfset assertEquals(serializejson( object=myarr, sercols="true" ), serializejson( myarr,"true" ) )>

	<cfset assertEquals(res, serializejson( myarr,"false" ) )>
	<cfset assertEquals(res, serializejson( object=myarr, sercols="false" ) )>
	<cfset assertEquals(serializejson( myarr,"false" ) , serializejson( object=myarr, sercols="false" ) )>
	<cfset assertEquals(serializejson( object=myarr, sercols="false" ), serializejson( myarr,"false" ) )>


	<cfset assertEquals(res, serializejson( myarr,"false","lower" ) )>
	<cfset assertEquals(res, serializejson( object=myarr,sercols="false",key="lower" ) )>
	<cfset assertEquals(serializejson( myarr,"false","lower" ), serializejson( object=myarr,sercols="false",key="lower" ) )>
	<cfset assertEquals(serializejson( object=myarr,sercols="false",key="lower" ),serializejson( myarr,"false","lower" ) )>


	<cfset assertEquals(res, serializejson( myarr,"false","upper" ) )>
	<cfset assertEquals(res, serializejson( object=myarr,sercols="false",key="upper" ) )>
	<cfset assertEquals(serializejson( myarr,"false","upper" ), serializejson( object=myarr,sercols="false",key="upper" ) )>
	<cfset assertEquals(serializejson( object=myarr,sercols="false",key="upper" ), serializejson( myarr,"false","upper" ) )>

	<cfset assertEquals(res, serializejson( myarr,"false","maintain" )  )>
	<cfset assertEquals(res, serializejson( object=myarr,sercols="false",key="maintain" )  )>
	<cfset assertEquals(serializejson( myarr,"false","maintain" ), serializejson( object=myarr,sercols="false",key="maintain" )  )>
	<cfset assertEquals(serializejson( object=myarr,sercols="false",key="maintain" ) ,serializejson( myarr,"false","maintain" ) )>


	<cfset assertEquals( serializejson( myarr,"false"), serializejson( myarr,"false","notexists" ) )>
	<cfset assertEquals( serializejson( myarr,"true") , serializejson( myarr,"true","notexists" ) )>
	<cfset assertEquals( serializejson( myarr,"false"), serializejson( object=myarr,sercols="false",key="notexists" ) )>
	<cfset assertEquals( serializejson( myarr,"true") , serializejson( object=myarr,sercols="true",key="notexists" ) )>

	<cfset assertEquals(res, serializejson( myarr,"true","lower" ) )>
	<cfset assertEquals(res, serializejson( object=myarr,sercols="true",key="lower" ) )>
	<cfset assertEquals(serializejson( myarr,"true","lower" ), serializejson( object=myarr,sercols="true",key="lower" ) )>
	<cfset assertEquals(serializejson( object=myarr,sercols="true",key="lower" ),serializejson( myarr,"true","lower" ) )>


	<cfset assertEquals(res, serializejson( myarr,"true","upper" ) )>
	<cfset assertEquals(res, serializejson( object=myarr,sercols="true",key="upper" ) )>
	<cfset assertEquals(serializejson( myarr,"true","upper" ), serializejson( object=myarr,sercols="true",key="upper" ) )>
	<cfset assertEquals(serializejson( object=myarr,sercols="true",key="upper" ), serializejson( myarr,"true","upper" ) )>

	<cfset assertEquals(res, serializejson( myarr,"true","maintain" )  )>
	<cfset assertEquals(res, serializejson( object=myarr,sercols="true",key="maintain" )  )>
	<cfset assertEquals(serializejson( myarr,"true","maintain" ), serializejson( object=myarr,sercols="true",key="maintain" )  )>
	<cfset assertEquals(serializejson( object=myarr,sercols="true",key="maintain" ) ,serializejson( myarr,"true","maintain" ) )>

	</cffunction>

</cfcomponent>
