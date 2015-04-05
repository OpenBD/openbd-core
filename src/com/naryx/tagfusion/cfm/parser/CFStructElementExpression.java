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

import java.util.ArrayList;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class CFStructElementExpression implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	
	private ArrayList<String> key;
	private CFExpression ekey;
	private CFExpression value;
	
	public CFStructElementExpression( ArrayList<String> _key, CFExpression _value ) {
		key = _key;
		value = _value;
	}

	public CFStructElementExpression( CFExpression _key, CFExpression _value ) {
		ekey = _key;
		value = _value;
	}

	public void evaluate( CFContext _context, cfStructData _struct ) throws cfmRunTimeException {
		if ( key == null ){ // must be a CFExpression key
			String nextKey = ekey.EvalFully( _context ).getString();
			cfData nextValue = value.Eval( _context );
			if ( nextValue instanceof cfLData ){
				nextValue = ( (cfLData) nextValue ).Get( _context );
			}
			
			if ( nextValue.isLoopIndex() ){
				nextValue = nextValue.duplicate();
			}
			
			_struct.setData( nextKey, nextValue );
			
		// in the majority of cases, the key should only be a single key
		}else if ( key.size() == 1 ){
			String nextKey = key.get( 0 );
			cfData nextValue = value.Eval( _context );
			if ( nextValue instanceof cfLData ){
				nextValue = ( (cfLData) nextValue ).Get( _context );
			}
			
			if ( nextValue.isLoopIndex() ){
				nextValue = nextValue.duplicate();
			}
			_struct.setData( nextKey, nextValue );		
		}else{
			// in the case where there's multiple parts to the key, create the appropriate new structs
			// required
			cfStructData struct = _struct;
			for ( int i = 0; i < key.size()-1; i++ ){
				struct = getNextStruct( _context, struct, key.get( i ) );
			}
			cfData nextValue = value.Eval( _context );
			if ( nextValue instanceof cfLData ){
				nextValue = ( (cfLData) nextValue ).Get( _context );
			}
			if ( nextValue.isLoopIndex() ){
				nextValue = nextValue.duplicate();
			}
			struct.setData( key.get( key.size() - 1 ), nextValue );
		}
	}
	
	private cfStructData getNextStruct( CFContext _context, cfStructData _parent, String _key ) throws CFException{
		if ( _parent.containsKey( _key ) ){
			cfData nextData = _parent.getData( _key );
			if ( nextData.getDataType() != cfData.CFSTRUCTDATA ){
				throw new CFException( "Struct key '" + _key + "' already exists and is not a struct", _context );
			}
			return (cfStructData) nextData;
			
		}else{
			cfStructData struct = new cfStructData();
			_parent.setData( _key, struct );
			return struct;
		}
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append( key.get( 0 ) );
		for ( int i = 1; i < key.size(); i++ ){
			sb.append( '.' );
			sb.append( key.get( i ) );
		}
		sb.append( ':' );
		sb.append( value.Decompile( 0 ) );
		return sb.toString();
	}
	
}
