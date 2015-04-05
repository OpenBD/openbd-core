<!---
 *
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: JournalSession.java 2503 2015-02-04 14:53:31Z alan $
 *
--->
<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testXmlParse">

		<cfset mydoc = XmlParse(ExpandPath("/WEB-INF/bluedragon/bluedragon.xml")) />
		
		<cfloop array="#mydoc.xmlRoot.xmlchildren#" index="i" >
			<cfoutput>
				attributes:
					<cfloop collection="#i.xmlattributes#" item="a" >
						#a#: #i.xmlAttributes[a]#<br/>
					</cfloop>
				data:
					<cfloop array="#i.xmlchildren#" index="x">
						#x.xmlName#: #x.xmlText#<br/>
					</cfloop>
		
			</cfoutput>
		</cfloop>


	
	</cffunction>

</cfcomponent>