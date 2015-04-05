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

	<cffunction name="testOne">

		<cfscript>
			q = QueryNew("A");
			QueryAddRow(q);
			QuerySetCell(q, "A", "1");

			QueryAddRow(q);
			QuerySetCell(q, "A", "2");

			qvl	= QuotedValueList(q.A);
			vl	= ValueList(q.A);

			assertEquals( qvl, "'1','2'" );
			assertEquals( vl, "1,2" );
		</cfscript>
	</cffunction>

	<cffunction name="testTwo">

		<cfscript>
			q = QueryNew("A");
			vl	= ValueList(q.A,",","0");
			assertEquals( vl, "0" );
		</cfscript>
	</cffunction>

</cfcomponent>