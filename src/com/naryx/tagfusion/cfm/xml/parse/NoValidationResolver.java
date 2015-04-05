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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.nary.util.byteArray;

/**
 * EntityResolver that returns an empty source whenever an entity is resolved. Event when
 * parsing without validation, the xml parser will try to resolve entity references. If
 * it cannot, it will throw an error. This prevents the xml parser from throwing any
 * errors by returning empty (zero length) entity sources.
 *
 * @author Matt Jacobsen
 *
 */
public class NoValidationResolver implements EntityResolver
{
	/**
	 * Default constructor.
	 */
	public NoValidationResolver()
	{
	}

	/**
	 * Resolves the requested entity by returning an empty InputSource for the content
	 * of the request entity.
	 *
	 * @param publicId public id of the entity to resolve
	 * @param systemId system id of the entity to resolve
	 * @return InputSource representing the resolved entity content
	 * @throws SAXException
	 * @throws IOException
	 */
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
	{
		if(systemId == null || systemId.toLowerCase().endsWith(".dtd"))
			return new InputSource(new ByteArrayInputStream(new byte[0]));
		else
		{
			if(systemId.toLowerCase().startsWith("file:///"))
				systemId = systemId.substring(8);

			byte[] fileBytes = byteArray.convertToByteArray(new File(systemId));
			return new InputSource(new ByteArrayInputStream(fileBytes));
		}
	}

}
