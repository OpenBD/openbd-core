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
 * A wrapper class for array/struct entries allowing for the 
 * getting and setting of it.
 *
 * Will exist for the period of 2 binaryExpressions at most.
 *
 */

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class indirectReferenceData extends cfLData {
	private static final long serialVersionUID = 1;

	protected String image;
	protected cfQueryResultData queryResult;
	protected cfSession currentSession;

	private cfData index;

	public indirectReferenceData(cfSession session, String _image, cfData _data, cfData _index) throws cfmRunTimeException {
		super( null );
		currentSession = session;
		exists = true;
		data = _data;
		index = _index;
		image = _image;

		byte dataType = _data.getDataType();

		if ( dataType == cfData.CFQUERYRESULTDATA ) {
			queryResult = (cfQueryResultData) data;
		}

		if ( _index == null ) {
			exists = false;
			return;
		}

		if ( dataType == cfData.CFARRAYDATA ) {
			int indx = _index.getInt();

			// if index exceeds the size of the array, or the array is
			// multidimensional and the element is empty
			cfArrayData arrayData = ( (cfArrayData) _data );
			if ( ( indx > arrayData.size() )
			    || ( ( arrayData.getDimension() > 1 ) && 
			    		( arrayData.getElement( indx ) == null ) )
			    || ( arrayData.getDimension() == 1 && arrayData.getElement( indx ) == null ) ) {
				exists = false;
			}
		} else if ( ( dataType == cfData.CFSTRUCTDATA )
		    || ( dataType == cfData.CFQUERYRESULTDATA )
		    || ( dataType == cfData.CFCOMPONENTOBJECTDATA ) ) {
			exists = ( _data.getData( _index ) != null );
		}
	}

	public indirectReferenceData(String _image, cfData _data, cfData _index)
	    throws cfmRunTimeException {
		this( null, _image, _data, _index );
	}

	public cfQueryResultData getQueryResult() {
		return queryResult;
	}

	public cfData getIndex() {
		return index;
	}

	public cfData Get( CFContext _context ) throws cfmRunTimeException {
		if ( !exists && ( index != null )
		    && ( data.getDataType() == cfData.CFARRAYDATA )
		    && ( ( (cfArrayData) data ).getDimension() > 1 ) ) {
			cfArrayData newArray = cfArrayData.createArray( ( (cfArrayData) data )
			    .getDimension() - 1 );
			data.setData( index, newArray );
			return newArray;
		} else {
			if ( !exists ) {
				throw new CFException( image + " doesn't exist.", _context );
			}
			if ( index != null ) {
				cfData indexData = data.getData( index );
				if ( ( indexData == null )
				    && ( data.getDataType() == cfData.CFARRAYDATA ) ) {
					// we know it exists but is null so must simply be an undefined array
					// element
					indexData = cfArrayData.createArray( 1 );
					data.setData( index, indexData );
				}
				return indexData;
			}
			return null;
		}
	}

	public void Set( cfData val, CFContext _context ) throws cfmRunTimeException {
		if ( index == null ) {
			throw new CFException( "Index does not exist: " + image, _context );
		}

		if ( data.getDataType() == cfData.CFARRAYDATA
		    && ( (cfArrayData) data ).getDimension() > 1 ) {
			if ( !( val.getDataType() == cfData.CFARRAYDATA ) ) {
				throw new CFException(
				    "Invalid expression. This is a multi-dimensional array. "
				        + "Cannot set the value of " + image
				        + " to a non-array object. ", _context );
			}
		}

		if ( data.getDataType() == cfData.CFCOMPONENTOBJECTDATA ) {
			( (cfComponentData) data ).setData( index, val );
		} else if ( data.getDataType() == cfData.CFQUERYRESULTDATA ) {
			( (cfQueryResultData) data ).setCell( index.getString(), val );
		} else if ( data.getDataType() == cfData.CFARRAYDATA
		    && val.getDataType() == cfData.CFARRAYDATA ) {
			data.setData( index, val );

			// need to fill in any undefined fields as Arrays
			cfArrayData array = (cfArrayData) data;
			for ( int i = 1; i < index.getInt(); i++ ) {
				if ( array.getElement( i ) == null ) {
					cfArrayData newArray = cfArrayData.createArray( 1 );
					array.setData( i, newArray );
				}
			}

		} else {
			data.setData( index, val );
		}

		exists = true;
	}

	// to support CFDUMP and debugging
	public String toString() {
		cfData indexData = null;
		try {
			indexData = this.Get( null );
			return ( indexData == null ? "[null]" : indexData.getString() );
		} catch ( cfmRunTimeException e ) {
			return ( indexData == null ? "[null]" : indexData.toString() );
		}
	}

}
