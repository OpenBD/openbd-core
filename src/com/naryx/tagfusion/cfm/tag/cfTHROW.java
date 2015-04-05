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

import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfJavaObjectData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class cfTHROW extends cfTag implements Serializable {

	static final long serialVersionUID = 1;

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("TYPE", "Application");
		parseTagHeader(_tag);
	}

	public java.util.Map getInfo() {
		return createInfo("control", "Throws a new exception, or rethrows an existing one");
	}
	
	 public java.util.Map[] getAttInfo(){
	  	return new java.util.Map[] {
	   			createAttInfo("ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
		 			createAttInfo("TYPE", 				"The type of exception to throw", "Application", false ),
		 			createAttInfo("OBJECT", 			"The object to throw; it can be another exception that you can rethrow", "", false ),
		 			createAttInfo("MESSAGE", 			"The message of the new exception", "", false ),
		 			createAttInfo("DETAIL", 			"The detail of the new exception", "", false ),
		 			createAttInfo("ERRORCODE", 		"The error code of the new exception", "", false ),
		 			createAttInfo("EXTENDEDINFO", "The extended information of the new exception", "", false )
	  	};
	  }
	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
  	cfStructData attributes = setAttributeCollection(_Session);
		cfCatchData catchData = null;
		boolean rethrow = false;

		if (containsAttribute(attributes,"OBJECT")) {
			cfData obj = getDynamic(attributes,_Session, "OBJECT");

			if (obj instanceof cfCatchData) {
				catchData = (cfCatchData) obj;
				rethrow = true;

				// if object is a java.lang.Exception, use the message from it
			} else if (obj.getDataType() == cfData.CFJAVAOBJECTDATA && Exception.class.isInstance(((cfJavaObjectData) obj).getInstance())) {
				catchData = new cfCatchData(_Session);
				Exception e = (Exception) ((cfJavaObjectData) obj).getInstance();
				catchData.setMessage(e.getMessage());

			} else { // create empty catchdata
				catchData = new cfCatchData(_Session);
			}

		} else {
			catchData = new cfCatchData(_Session);

			catchData.setType(getDynamic(attributes,_Session, "TYPE").getString());

			if (containsAttribute(attributes,"MESSAGE"))
				catchData.setMessage(getDynamic(attributes,_Session, "MESSAGE").getString());

			if (containsAttribute(attributes,"DETAIL"))
				catchData.setDetail(getDynamic(attributes,_Session, "DETAIL").getString());

			if (containsAttribute(attributes,"ERRORCODE"))
				catchData.setErrorCode(getDynamic(attributes,_Session, "ERRORCODE").getString());

			if (containsAttribute(attributes,"EXTENDEDINFO"))
				catchData.setExtendedInfo(getDynamic(attributes,_Session, "EXTENDEDINFO").getString());
		}

		throw new cfmRunTimeException(catchData, rethrow);
	}
}
