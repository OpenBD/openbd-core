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

package com.naryx.tagfusion.cfm.engine;

import java.util.Map;

/**
 * This class implements the CFML data structure for structures that have some read only variables.
 * Originally developed to protect Server.BlueDragon, Server.ColdFusion and Server.OS.
 */

public class cfStructSelectiveReadOnlyData extends cfStructData implements java.io.Serializable{
	
  static final long serialVersionUID = 1;
  
  private Map<String, cfData> readOnly;
  
  public cfStructSelectiveReadOnlyData( Map<String, cfData> _readOnly ){
		super();
		readOnly = _readOnly;
	}

	public void setData( cfData _key, cfData _data )  throws cfmRunTimeException  {
		String key = _key.getString();
		
		// Check if the key is read only
		if ( !readOnly.containsKey( key ) )
		{
			setData( key, _data );
		}
		else
		{
			cfCatchData catchData = new cfCatchData();
			catchData.setMessage( "Read only variable" );
			throw new cfmRunTimeException( catchData ); 
		}
	}
	
	public void deleteData( String _key ) throws cfmRunTimeException {
		
		// Check if the key is read only
		if ( !readOnly.containsKey( _key ) )
		{
			super.deleteData( _key );
		}
		else
		{
			cfCatchData catchData = new cfCatchData();
			catchData.setMessage( "Read only variable" );
			throw new cfmRunTimeException( catchData );   
		}
	}

	public void setPrivateData( String _key, cfData _data ){
		super.setData(_key,_data);
	}
}
