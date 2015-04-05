/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;
import java.util.List;

import com.nary.util.string;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

/**
 * This implements CFCASE tag
 */

public class cfCASE extends cfTag implements Serializable {

	static final long serialVersionUID = 1;

	@SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
		return createInfo("control", "Child tag of CFSWITCH. Compares value against evaluated expression of CFSWITCH");
	}

	@SuppressWarnings("unchecked")
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
				createAttInfo("VALUE", "The value or delimited set of values to be compared to CFSWITCH tag's expression", "", true ),
				createAttInfo("DELIMITERS", "Delimiter to split delimited values", "", false ) 
		};
	}
  
  protected void defaultParameters(String _tag) throws cfmBadFileException {
    defaultAttribute("DELIMITERS", ",");
    parseTagHeader(_tag);

    if (!containsAttribute("VALUE"))
      throw newBadFileException("Missing VALUE", "You must specify the VALUE attribute");
  }

  public String getEndMarker() {
    return "</CFCASE>";
  }

	public String[] getValues() {
		String value = getConstant( "VALUE" );
		if ( value.length() == 0 ) {
			return new String[] { value };
		}

		List<String> valist = string.split( value, getConstant( "DELIMITERS" ) );
		String[] values = new String[ valist.size() ];
		for ( int i = 0; i < valist.size(); i++ ) {
			try {
				values[ i ] = cfData.toNormalString( valist.get( i ).toLowerCase() );
			} catch ( dataNotSupportedException e ) { // should never happen
				values[ i ] = valist.get( i );
			}
		}
		return values;
	}
}
