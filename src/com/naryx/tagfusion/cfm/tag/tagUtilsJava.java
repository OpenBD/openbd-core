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
 * Created on Oct 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.naryx.tagfusion.cfm.tag;

import org.apache.axis.types.UnsignedByte;

import com.naryx.tagfusion.cfm.engine.cfData;

/**
 * @author Matt Jacobsen
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public final class tagUtilsJava
{
	public static cfData convertToCfData( Object obj )
	{
		if ( obj.getClass().isArray() && !obj.getClass().getComponentType().isPrimitive() )
		{
			// See if this is a UnsignedByte Object[]. Should convert those
			// to real instances of byte[].
			boolean isByteArray = false;
			Object[] ar = (Object[])obj;
			byte[] ba = new byte[ar.length];
			for (int pp=0; pp<ar.length; pp++)
			{				
				if (!(ar[pp] instanceof UnsignedByte))
				{
					isByteArray = false;
					break;
				}
				else
				{
					isByteArray = true;
					ba[pp] = ((UnsignedByte)ar[pp]).byteValue();
				}
			}
			if (isByteArray)
				return tagUtils.convertToCfData(ba);
		}
		return null;
	}

}
