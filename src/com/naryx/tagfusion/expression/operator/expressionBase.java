/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 *  http://openbd.org/
 *  $Id: expressionBase.java 2488 2015-01-28 01:35:58Z alan $
 */

package com.naryx.tagfusion.expression.operator;

import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class expressionBase extends cfData  
{
	private static final long serialVersionUID = 1L;

	public String getName(){ return ""; }


  public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{ 
  	cfCatchData	catchData	= new cfCatchData( _session );
  	catchData.setType( "Expression" );
  	catchData.setErrNumber( this.getClass().getName() );
  	catchData.setMessage( "This function is not supported in this release" );
 		throw new cfmRunTimeException( catchData );
	}

  public cfData execute( cfSession _session, cfArgStructData argStruct )throws cfmRunTimeException{ 
  	cfCatchData	catchData	= new cfCatchData( _session );
  	catchData.setType( "Expression" );
  	catchData.setErrNumber( this.getClass().getName() );
  	catchData.setMessage( "This function is not supported in this release" );
 		throw new cfmRunTimeException( catchData );
	}

	protected void throwException( cfSession _session, String _ErrMessage ) throws cfmRunTimeException {
  	cfCatchData	catchData	= new cfCatchData( _session );
  	catchData.setType( "Expression" );
  	catchData.setErrNumber( this.getClass().getName() );
  	catchData.setMessage( _ErrMessage );
 		throw new cfmRunTimeException( catchData );
	}

	protected boolean isNumber( cfData data ) throws cfmRunTimeException {
		try{
			data.getDouble();
			return true;
		}catch(Exception E){}
		
		return false;
	}


	protected int	getInt( cfSession _session, cfData data ) throws cfmRunTimeException {
		try{
			return data.getInt();
		}catch(Exception E){
			throwException( _session, "parameter must be an integer " + data );
		}
		return 0;
	}
	
	protected boolean	getBoolean( cfSession _session, cfData data ) throws cfmRunTimeException {
		try{
			return data.getBoolean();
		}catch(Exception E){
			throwException( _session, "parameter must be an boolean " + data );
		}
		return false;
	}


	protected long getLong( cfSession _session, cfData data ) throws cfmRunTimeException {
		try{
			return data.getLong();
		}catch(Exception E){
			throwException( _session, "parameter must be a long " + data );
		}
		return 0;
	}

	protected double getDouble( cfSession _session, cfData data ) throws cfmRunTimeException {
		try{
			return data.getDouble();
		}catch(Exception E){
			throwException( _session, "parameter must be a double " + data );
		}
		return 0;
	}

	/*
	 * getDoubleWithChecks
	 * 
	 * This method performs some checks on the passed in cfData object before calling getDouble().
	 * Currently it only checks if the value is an empty string.
	 * 
	 * NOTE: this method is called by CFML functions that need to accept empty strings as zero.
	 */ 
	protected double getDoubleWithChecks( cfSession _session, cfData _data ) throws cfmRunTimeException{
	  // if the passed in value is an empty string then return 0
	  if ( _data.getDataType() == cfData.CFSTRINGDATA && _data.getString().equals("") )
			  return 0;
      
	  return getDouble( _session, _data );
	}

  public cfNumberData getNumber( cfSession _session, cfData data ) throws cfmRunTimeException {
    try{
			return data.getNumber();
		}catch(Exception E){
			throwException( _session, "parameter must be a number " + data );
		}
    return null;
  }
}
