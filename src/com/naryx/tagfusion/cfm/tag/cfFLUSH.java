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

import com.naryx.tagfusion.cfm.engine.cfHttpServletResponse;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfFLUSH extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
	}
	

  @SuppressWarnings("unchecked")
	public java.util.Map getInfo(){
  	return createInfo("output", "Flushes the content of the buffer to the client.  Once you do this, some tags no longer work, for example CFCOOKIE");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
   			createAttInfo( "ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
   			createAttInfo( "INTERVAL", "The size of the buffer to flush", 	"", false )
  	};
  }
	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		
		if (containsAttribute(attributes, "INTERVAL")) {
			String interval = getDynamic(attributes, _Session, "INTERVAL").getString();
			if (interval.equalsIgnoreCase("PAGE")) {
				_Session.setBufferSize(cfHttpServletResponse.UNLIMITED_SIZE);
			} else {
				_Session.setBufferSize(getDynamic(attributes, _Session, "INTERVAL").getInt());
			}
		} else {
			_Session.pageFlush(this);
		}

		return cfTagReturnType.NORMAL;
	}
}
