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

package com.naryx.tagfusion.cfm.tag.awt;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfCHARTDATA extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	@SuppressWarnings("unchecked")
	public java.util.Map getInfo() {
		return createInfo("output", "The child tag of CFCHARTSERIES");
	}

	@SuppressWarnings("unchecked")
	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] { 
				createAttInfo("ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
   			createAttInfo("ITEM", "Data Point Name", "", true), 
				createAttInfo("VALUE", "Data Point Value", "", true) };
	}

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseTagHeader(_tag);
		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;
 
		if (!containsAttribute("ITEM"))
			throw newBadFileException("Missing ITEM", "You need to provide a ITEM");

		if (!containsAttribute("VALUE"))
			throw newBadFileException("Missing VALUE", "You need to provide a VALUE");
	}

  
	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if (!containsAttribute(attributes,"ITEM"))
			throw newBadFileException("Missing ITEM", "You need to provide a ITEM");

		if (!containsAttribute(attributes,"VALUE"))
			throw newBadFileException("Missing VALUE", "You need to provide a VALUE");

		return	attributes;
	}

	
	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		// Get the internal chart data in which this relates to
		cfCHARTInternalData chartData = (cfCHARTInternalData) _Session.getDataBin(cfCHART.DATA_BIN_KEY);
		if (chartData == null)
			throw newRunTimeException("CFCHARTDATA must be used inside a CFCHART tag");

		// Get the series data in which this relates to
		cfCHARTSERIESData seriesData = (cfCHARTSERIESData) _Session.getDataBin(cfCHARTSERIES.DATA_BIN_KEY);
		if (seriesData == null)
			throw newRunTimeException("CFCHARTDATA must be used inside a CFCHARTSERIES tag");

		cfStructData attributes = setAttributeCollection(_Session);
		
		if (chartData.isXyChart()) {
			// It's a scale(xy) chart so add both values as doubles
			seriesData.add(getDynamic(attributes,_Session, "ITEM").getDouble(), getDynamic(attributes,_Session, "VALUE").getDouble());
		} else {
			// It's a category chart so add the item as a String and the value as a
			// double
			seriesData.add(getDynamic(attributes,_Session, "ITEM").toString(), getDynamic(attributes,_Session, "VALUE").getDouble());
		}

		return cfTagReturnType.NORMAL;
	}
}
