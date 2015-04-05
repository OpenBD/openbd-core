/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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
 */

/*
 * This class is provided to convert standard CFML data types to the
 * RHS side for JavaScript.  The String is encoded using %xx for chars
 * outside of the standard ASCII set.  This is then encoded around a
 * javascript standard unescape() function.
 */
package com.naryx.tagfusion.cfm.wddx;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfStringData;

public class wddxDataTypes extends Object {

	@SuppressWarnings("deprecation")
	public static String getRHSData( cfDateData data ){
		java.util.Date date = data.getCalendar().getTime();
		
		return "new Date(" + (date.getYear()+1900) + "," + date.getMonth() + "," + date.getDate() + "," + date.getHours() + "," + date.getMinutes() + "," + date.getSeconds() + ");";
	}

	public static String getRHSData( cfNumberData data ){
		return data.getString();
	}

	public static String getRHSData( cfBooleanData data ){
		if ( data.getBoolean() )
			return "true";
		else
			return "false";
	}
	
	public static String getRHSData( cfStringData data ){
		boolean bEscaped = false;
		StringBuilder sb	= new StringBuilder( 10 + (data.getLength() * 3) );
		sb.append("\"");

		char [] chars = data.getString().toCharArray();
		
		for ( int i = 0; i < chars.length; i++ ){
			if ( chars[i] <= 0x1f && chars[i] >= 0 ){
				bEscaped	= true;
				sb.append("%");
				String hex = Integer.toHexString( chars[i] );
				if ( hex.length() == 1 )
					sb.append( "0" );
					
				sb.append( hex );
      }else if ( chars[i] == '"' ){
        sb.append( "\\\"" );
			}else
				sb.append( chars[i] );
		}
		sb.append("\"");
		
		//-- Only use the unescape() function if special characters were present
		if ( bEscaped ){
			sb.insert(0,"unescape(");
			sb.append(")");
		}
		return sb.toString();
	}
}
