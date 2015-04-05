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

package com.naryx.tagfusion.cfx;

import com.allaire.cfx.Query;
import com.allaire.cfx.cfmlInvalidContextException;
import com.allaire.cfx.cfmlInvalidDataException;
import com.allaire.cfx.cfmlPageContext;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.parser.runTime;


/**
 * this is the concrete implementation of the cfmlPageContext interface
 *
 */

public class cfmlPageContextImpl extends Object implements cfmlPageContext {

	private cfSession Session;
	
	public cfmlPageContextImpl( cfSession _Session ){
		Session	= _Session;
	}
	
	public void closeSession(){
		Session = null;
	}
	
	//--------------------------------------------
	
	public boolean 	variableExists(String name) throws cfmlInvalidContextException, cfmlInvalidDataException{
		checkContext();
		try{
			// if the variable doesn't exist, an exception will be thrown
			runTime.runExpression( Session, name );
			return true;
		}catch(cfmRunTimeException E){ return false; }
	}

	//--------------------------------------------

	public String 	getVariable(String name) throws cfmlInvalidContextException, cfmlInvalidDataException{
		checkContext();
		try{
			return runTime.runExpression( Session, name ).getString();
		}catch(cfmRunTimeException E){ throw new cfmlInvalidDataException(); }
	}

	//--------------------------------------------
	
	public int 			getIntVariable(String name) throws cfmlInvalidContextException,cfmlInvalidDataException{
		checkContext();
		try{
			return runTime.runExpression( Session, name ).getInt();
		}catch(cfmRunTimeException E){ throw new cfmlInvalidDataException(); }
	}

	//--------------------------------------------
	
	public double		getDoubleVariable(String name) throws cfmlInvalidContextException,cfmlInvalidDataException{
		checkContext();
		try{
			return runTime.runExpression( Session, name ).getDouble();
		}catch(cfmRunTimeException E){ throw new cfmlInvalidDataException(); }
	}

	//--------------------------------------------
	
	public boolean		getBooleanVariable(String name) throws cfmlInvalidContextException,cfmlInvalidDataException{
		checkContext();
		try{
			return runTime.runExpression( Session, name ).getBoolean();
		}catch(cfmRunTimeException E){ throw new cfmlInvalidDataException(); }
	}

	//--------------------------------------------

	public java.util.Date		getDateVariable(String name) throws cfmlInvalidContextException,cfmlInvalidDataException{
		checkContext();
		try{
			return new java.util.Date( runTime.runExpression( Session, name ).getDateData().getLong() );
		}catch(cfmRunTimeException E){ throw new cfmlInvalidDataException(); }
	}
	
	//--------------------------------------------
	
	public Object		getObjectVariable(String name) throws cfmlInvalidContextException,cfmlInvalidDataException{
		checkContext();
		try{
			cfData dataCF = runTime.runExpression( Session, name );
			return com.naryx.tagfusion.cfm.tag.tagUtils.convertCFtoJava( dataCF, Class.forName("java.lang.Object") );
		}catch(Exception E){ throw new cfmlInvalidDataException(); }
	}

	//--------------------------------------------
	
	public Query 		getQuery() throws cfmlInvalidContextException{
		checkContext();
		return null;	
	}
	
	//--------------------------------------------
	
	public String 	getSetting(String name) throws cfmlInvalidContextException{
		checkContext();
		return null;
	}

	//--------------------------------------------	
	//--[ Methods for setting values back into the CFML page	
	public void 	setVariable(String name, Object value) throws cfmlInvalidContextException,IllegalArgumentException{
		checkContext();
		try{
			cfData newData = com.naryx.tagfusion.cfm.tag.tagUtils.convertToCfData( value );
			
			if ( newData != null ){
				cfData var = runTime.runExpression( Session, name, false );
				if ( var.getDataType() ==  cfData.CFLDATA ){
					( (cfLData) var ).Set( newData, Session.getCFContext() );
				}else{
					Session.setData( name, newData );
				}
				
			}else{
				throw new IllegalArgumentException();
			}
				
		}catch(cfmRunTimeException E){throw new IllegalArgumentException();}
	}
	
	//--------------------------------------------
	
	public void 	setVariable(String name, String value) throws cfmlInvalidContextException,IllegalArgumentException{
		checkContext();

		try{
			cfData temp = runTime.runExpression( Session, name, false );
			if ( temp.getDataType() == cfData.CFLDATA ){
				( (cfLData) temp ).Set( new cfStringData( value ), Session.getCFContext() );
			}
		}catch(cfmRunTimeException E){throw new IllegalArgumentException();}
	}
	
	//--------------------------------------------
	
	public void 	setVariable(String name, boolean value) throws cfmlInvalidContextException,IllegalArgumentException{
		checkContext();

		try{
			runTime.runExpression( Session, name + "=" + value  );
		}catch(cfmRunTimeException E){throw new IllegalArgumentException();}
	}
	
	//--------------------------------------------
	
	public void 	setVariable(String name, int value) throws cfmlInvalidContextException,IllegalArgumentException{
		checkContext();

		try{
			runTime.runExpression( Session, name + "=" + value );
		}catch(cfmRunTimeException E){throw new IllegalArgumentException();}
	}
	
	//--------------------------------------------
	
	public void 	setVariable(String name, double value) throws cfmlInvalidContextException,IllegalArgumentException{
		checkContext();

		try{
			runTime.runExpression( Session, name + "=" + value );
		}catch(cfmRunTimeException E){throw new IllegalArgumentException();}
	}
	
	//--------------------------------------------
	
	public void 	setVariable(String name, java.util.Date value) throws cfmlInvalidContextException,IllegalArgumentException{
		setVariable( name, (Object) value );
	}
	
	//--------------------------------------------
	
	public void 	setVariable(String name, java.util.Map value) throws cfmlInvalidContextException,IllegalArgumentException{
		setVariable(name, (Object)value);
	}
	
	//--------------------------------------------
	
	public Query 	addQuery(String name,String[] columns) throws cfmlInvalidContextException,IllegalArgumentException{
		checkContext();
		return null;
	}
	
	//--------------------------------------------
	
	//--[ House Keeping methods
	public boolean bSessionValid(){
		return (Session != null);
	}
	
	private final void checkContext() throws cfmlInvalidContextException {
		//--[ Simple helper method to quickly determine if the context is still valid
		if ( Session == null ) throw new cfmlInvalidContextException();
	}
	
	public String toString(){
		return "cfmlPageContext=" + bSessionValid();
	}
}
