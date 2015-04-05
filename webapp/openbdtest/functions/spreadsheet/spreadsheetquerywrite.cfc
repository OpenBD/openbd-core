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
 *  $Id: spreadsheetquerywrite.cfc 1891 2011-12-27 20:41:26Z alan $
 *
--->
<cfcomponent extends="openbdtest.common.TestCase">

<cffunction name="testWriteQueryAll">
	<cfscript>
	FileDelete(ExpandPath("./newsheet.xls"));

	var sp 	= SpreadsheetRead( ExpandPath("./samplespreadsheet.xls") );
	var qry	= SpreadSheetQueryRead( spreadsheet=sp, sheet=0 );

	// Write the Sheet
	var spNew	= SpreadSheetNew();
	SpreadsheetCreatesheet( spNew, "sheetone" );
	SpreadsheetCreatesheet( spNew, "sheettwo" );
	SpreadSheetQueryWrite( spNew, qry, "sheetone" );
	SpreadSheetQueryWrite( spNew, qry, "sheettwo" );
	SpreadSheetWrite( spNew, ExpandPath("./newsheet.xls"), true );

	// Read back in to make sure all is well
	sp = SpreadsheetRead( ExpandPath("./newsheet.xls") );
	SpreadSheetSetActiveSheet( sp, "sheetone" );
	qry	= SpreadSheetQueryRead( spreadsheet=sp, sheet=1, headerrow=1 );
	assertTrue( isQuery(qry) );
	assertEquals( qry.recordcount, 20 );

	FileDelete(ExpandPath("./newsheet.xls"));
	</cfscript>
</cffunction>


</cfcomponent>