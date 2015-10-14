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

	<cffunction name="testDateCorrect">
		<cfset var dt = "2015-10-06T16:09:13Z" />
		<cfset var parsed = ParseDateTime( dt )>
		<cfset checkDate( parsed, 2015, 10, 6, 16, 9, 13 )>
	</cffunction>

	<cffunction name="testDateIncorrect1" >
		<cfset var dt = "9999-99-99T99:99:99Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 9999-99-99T99:99:99Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrect2">
		<cfset var dt = "0000-00-00T00:00:00Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 0000-00-00T00:00:00Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectYear1" >
		<cfset var dt = "15-10-06T16:09:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 15-10-06T16:09:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectYear2">
		<cfset var dt = "12345-10-06T16:09:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 12345-10-06T16:09:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectYear3">
		<cfset var dt = "XXXX-10-06T16:09:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: XXXX-10-06T16:09:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectYear4">
		<cfset var dt = "0000-10-06T16:09:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 0000-10-06T16:09:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectMonth1">
		<cfset var dt = "2015-13-06T16:09:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-13-06T16:09:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectMonth2">
		<cfset var dt = "2015-XX-06T16:09:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail()>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-XX-06T16:09:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectDay1">
		<cfset var dt = "2015-10-6T16:09:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-6T16:09:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectDay2">
		<cfset var dt = "2015-10-35T16:09:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-35T16:09:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectDay3">
		<cfset var dt = "2015-10-XXT16:09:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-XXT16:09:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateHour1">
		<cfset var dt = "2015-10-06T00:09:13Z" />
		<cfset var parsed = ParseDateTime( dt )>


		<cfset checkDate( parsed, 2015, 10, 6, 0, 9, 13 )>
	</cffunction>

	<cffunction name="testDateHour2">
		<cfset var dt = "2015-10-06T04:09:13Z" />
		<cfset var parsed = ParseDateTime( dt )>
		<cfset checkDate( parsed, 2015, 10, 6, 4, 9, 13 )>
	</cffunction>

	<cffunction name="testDateIncorrectHour1">
		<cfset var dt = "2015-10-06T29:09:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06T29:09:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectHour2">
		<cfset var dt = "2015-10-06TXX:09:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06TXX:09:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectMinute1">
		<cfset var dt = "2015-10-06T16:65:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06T16:65:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectMinute2">
		<cfset var dt = "2015-10-06T16:XX:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06T16:XX:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectSecond1">
		<cfset var dt = "2015-10-06T16:09:61Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail()>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06T16:09:61Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectSecond2">
		<cfset var dt = "2015-10-06T16:09:XXZ" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06T16:09:XXZ", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectLetter1">
		<cfset var dt = "2015-10-06P16:09:13Z" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06P16:09:13Z", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateIncorrectLetter2">
		<cfset var dt = "2015-10-06T16:09:13X" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06T16:09:13X", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateNoEndLetter">
		<cfset var dt = "2015-10-06T16:09:13" />
		<cfset var parsed = ParseDateTime( dt )>
		<cfset checkDate( parsed, 2015, 10, 6, 16, 9, 13 )>
	</cffunction>

	<cffunction name="testDateTimeStamp1">
		<cfset var dt = "2015-10-06T13:06:13+05:30" />
		<cfset var parsed = ParseDateTime( dt )>
		<cfset checkDate( parsed, 2015, 10, 6, 07, 36, 13 )>
	</cffunction>

	<cffunction name="testDateTimeStamp2">
		<cfset var dt = "2015-10-06T16:09:13+00:00" />
		<cfset var parsed = ParseDateTime( dt )>
		<cfset checkDate( parsed, 2015, 10, 6, 16, 9, 13 )>
	</cffunction>

	<cffunction name="testDateTimeStamp3">
		<cfset var dt = "2015-10-06T16:09:13Z+00:00" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06T16:09:13Z+00:00", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateTimeStamp4">
		<cfset var dt = "2015-10-06T16:09:13+01:00" />
		<cfset var parsed = ParseDateTime( dt )>
		<cfset checkDate( parsed, 2015, 10, 6, 15, 9, 13 )>
	</cffunction>

	<cffunction name="testDateTimeStamp5">
		<cfset var dt = "2015-10-06T16:09:13-02:00" />
		<cfset var parsed = ParseDateTime( dt )>
		<cfset checkDate( parsed, 2015, 10, 6, 18, 9, 13 )>
	</cffunction>

	<cffunction name="testDateTimeStamp6">
		<cfset var dt = "2015-10-06T16:09:13+01.00" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06T16:09:13+01.00", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateTimeStamp7">
		<cfset var dt = "2015-10-06T16:09:13+01" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06T16:09:13+01", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateTimeStamp8">
		<cfset var dt = "2015-10-06T16:09:13+TT:TT" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06T16:09:13+TT:TT", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateTimeStamp9">
		<cfset var dt = "2015-10-06T01:09:13+05:00" />
		<cfset var parsed = ParseDateTime( dt )>
		<cfset checkDate( parsed, 2015, 10, 5, 20, 9, 13 )>
	</cffunction>

	<cffunction name="testDateTimeStamp10">
		<cfset var dt = "2015-10-06T23:09:13-05:00" />
		<cfset var parsed = ParseDateTime( dt )>
		<cfset checkDate( parsed, 2015, 10, 7, 4, 9, 13 )>
	</cffunction>

	<cffunction name="testDateTimeStamp11">
		<cfset var dt = "2015-10-06T23:09:13-23:59" />
		<cfset var parsed = ParseDateTime( dt )>
		<cfset checkDate( parsed, 2015, 10, 7, 23, 8, 13 )>
	</cffunction>

	<cffunction name="testDateTimeStamp12">
		<cfset var dt = "2015-10-06T23:09:13+23:59" />
		<cfset var parsed = ParseDateTime( dt )>
		<cfset checkDate( parsed, 2015, 10, 5, 23, 10, 13 )>
	</cffunction>

	<cffunction name="testDateTimeStamp13">
		<cfset var dt = "2015-10-06T16:09:13+25:00" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail( "Should have failed to parse this date" )>
			<cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06T16:09:13+25:00", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="testDateTimeStamp14">
		<cfset var dt = "2015-10-06T16:09:13-25:00" />
		<cftry>
			<cfset var parsed = ParseDateTime( dt )>
			<cfset fail("Should have failed to parse this date" )>
			 <cfcatch type="any">
				<cfset assertEquals("invalid date/time string: 2015-10-06T16:09:13-25:00", cfcatch.message)>
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="checkDate">
		<cfargument name="parsed">
		<cfargument name="year">
		<cfargument name="month">
		<cfargument name="day">
		<cfargument name="hour">
		<cfargument name="minute">
		<cfargument name="second">

		<cfset console(arguments.parsed)>

		<cfset assertEquals( year( arguments.parsed ), arguments.year )>
		<cfset assertEquals( month( arguments.parsed ), arguments.month )>
		<cfset assertEquals( day( arguments.parsed ), arguments.day )>
		<cfset assertEquals( hour( arguments.parsed ), arguments.hour )>
		<cfset assertEquals( minute( arguments.parsed ), arguments.minute )>
		<cfset assertEquals( second( arguments.parsed ), 13 )>

	</cffunction>

</cfcomponent>