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

package com.naryx.tagfusion.cfm.xml.parse;

import java.io.File;
import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * EntityResolver that returns the specified DTD source when our internal 
 * DTD SYSTEM identifier is resolved or when the existing DTD SYSTEM identifier is resolved.
 * 
 * @author Matt Jacobsen
 *
 */
public class ValidationResolver implements EntityResolver, DTDFilterReader.DTDListener
{
	private ValidatorSource source = null;
	File sId = null;
	File pId = null;
	
	/**
	 * Default constructor. Accepts the ValidatorSource which represents
	 * the DTD that we need to use when parsing.
	 * 
	 * @param dtdSource DTD source with which to parse
	 */
	public ValidationResolver(ValidatorSource dtdSource)
	{			
		source = dtdSource;
		sId = null;
		pId = null;
	}

	/**
	 * Resolves the requested entity by returning an InputSource for the content
	 * of the request entity. If the requested entity is our internal DTD entity,
	 * this returns the contents of the specified DTD source. Otherwise, null is 
	 * returned so that the default EntityResolver can handle the request.
	 * 
	 * @param publicId public id of the entity to resolve
	 * @param systemId system id of the entity to resolve
	 * @return InputSource representing the resolved entity content
	 * @throws SAXException
	 * @throws IOException
	 */
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
	{
		if (DTDFilterReader.CUSTOM_DTD.equals(publicId) || DTDFilterReader.CUSTOM_DTD.equals(systemId))
		{
			// Return our validation dtd InputSource
			return source.getAsInputSource();
		}
				
		File f = null;
		if (publicId != null)
			f = new File(publicId);
		else if (systemId != null)
			f = new File(systemId);

		if (f != null)
		{
			if ((this.pId != null && f.getName().equals(this.pId.getName())) ||
					(this.sId != null && f.getName().equals(this.sId.getName())))
			{
				// Return our validation dtd InputSource
				return source.getAsInputSource();
			}
		}
		
		// Use the default behaviour
		return null;
	}

	/**
	 * Called when the DTD has been read (or when it is determined that no DTD exists).
	 */
	public void setDTD(String publicId, String systemId)
	{
		if (systemId != null)
			this.sId = new File(systemId);
		else if (publicId != null)
			this.pId = new File(publicId);
	}
	
}
