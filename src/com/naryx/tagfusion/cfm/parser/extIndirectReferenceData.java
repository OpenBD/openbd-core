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

package com.naryx.tagfusion.cfm.parser;

/**
 * An extension of indirectReferenceData, this handles
 * struct references where the struct itself doesn't exist or
 * the field(s) don't exist.
 * 	e.g str.strFieldOne.strFieldtwo
 * where str doesn't exist or .strFieldOne.strFieldtwo doesn't exist
 * 
 */

import java.util.Enumeration;
import java.util.Vector;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class extIndirectReferenceData extends indirectReferenceData {

	private static final long serialVersionUID = 1;

	private Vector indices;

	public extIndirectReferenceData(cfLData _struct, String _image)
	    throws cfmRunTimeException {
		super( _image, _struct, null );
		indices = new Vector();
		exists = false;
	}

	public void addIndex_Dot( cfData _index ) {
		cfData index = _index;
		// if it's a number convert it too a string so that a struct is created
		// instead of an array
		try {
			if ( index instanceof cfNumberData ) {
				index = new cfStringData( index.getString() );
			}

			indices.insertElementAt( _index, 0 );

			image += "." + _index.getString();
		} catch ( Exception ignored ) {
		}
	}

	public void addIndex_Bracket( cfData _index ) {
		if ( _index != null ) {
			indices.insertElementAt( _index, 0 );
			try {
				image += "[" + _index.getString() + "]";
			} catch ( dataNotSupportedException ignored ) {
			}
		}
	}

	public cfData Get( CFContext _context ) throws cfmRunTimeException {
		throw new CFException( image + " doesn't exist.", _context );
	}

	public void Set( cfData val, CFContext _context ) throws cfmRunTimeException {
		cfData nextStruct;
		cfData lastElement;
		cfData nextElement;
		Enumeration enumer = indices.elements();

		// INVARIANT - at this point indices.size() > 0
		lastElement = val;
		boolean isFirst = true;
		while ( enumer.hasMoreElements() ) {
			nextElement = (cfData) enumer.nextElement();

			if ( nextElement.getDataType() == cfData.CFNUMBERDATA ) {
				nextStruct = cfArrayData.createArray( 1 );
				nextStruct.setData( nextElement, lastElement );
				if ( !isFirst ) {
					int elemCount = nextElement.getInt();
					for ( int i = 1; i < elemCount; i++ ) {
						cfArrayData otherElem = cfArrayData.createArray( 1 );
						nextStruct.setData( new cfNumberData( i ), otherElem );
					}
				}

			} else {
				nextStruct = new cfStructData();
				nextStruct.setData( nextElement.getString(), lastElement );
			}

			lastElement = nextStruct;
			isFirst = false;
		}

		( (cfLData) data ).Set( lastElement, _context );

	}

}
