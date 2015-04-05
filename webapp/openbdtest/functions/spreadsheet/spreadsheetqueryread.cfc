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
 *  $Id: spreadsheetqueryread.cfc 1891 2011-12-27 20:41:26Z alan $
 *
--->
<cfcomponent extends="openbdtest.common.TestCase">

<cffunction name="testReadQueryAll">
	<cfscript>
	var sp = SpreadsheetRead( ExpandPath("./samplespreadsheet.xls") );

	var qry	= SpreadSheetQueryRead( spreadsheet=sp, sheet=0 );

	assertTrue( isQuery(qry) );
	assertEquals( qry.recordcount, 20 );
	assertEquals( qry.columnlist, "column 1,column 2,column 3,column 4,column 5,column 6,column 7" );
	</cfscript>
</cffunction>


<cffunction name="testReadQueryHeaderRow">
	<cfscript>
	var sp = SpreadsheetRead( ExpandPath("./samplespreadsheet.xls") );

	var qry	= SpreadSheetQueryRead( spreadsheet=sp, sheet=0, headerrow=1 );

	assertTrue( isQuery(qry) );
	assertEquals( qry.recordcount, 19 );
	assertEquals( qry.columnlist, "a,b,c,d,e,f,g" );
	</cfscript>
</cffunction>


<cffunction name="testReadQueryHeaderRowSixRows">
	<cfscript>
	var sp 	= SpreadsheetRead( ExpandPath("./samplespreadsheet.xls") );
	var qry	= SpreadSheetQueryRead( spreadsheet=sp, sheet=0, headerrow=1, rows="2-7" );

	assertTrue( isQuery(qry) );
	assertEquals( qry.recordcount, 6 );
	assertEquals( qry.columnlist, "a,b,c,d,e,f,g" );
	</cfscript>
</cffunction>


</cfcomponent>