/*
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  $Id: $
 */


package com.naryx.tagfusion.expression.function.ext;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.tag.cfINCLUDE;
import com.naryx.tagfusion.expression.function.functionBase;

public class Include extends functionBase {
	private static final long serialVersionUID = 1L;

	public Include(){
  	min = 1; max = 2; 
  	setNamedParams( new String[]{ "template", "runonce" } ); 
	}
	
	public String[] getParamInfo(){
		return new String[]{
			"path to the template to render",
			"flag to determine if this template should be executed only once in the request"
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"The function version of the tag CFINCLUDE that renders a given template and returns back the string", 
				ReturnType.STRING );
	} 
  
	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		
		String template	= getNamedStringParam(argStruct, "template", null);
		if ( template == null )
			throwException(_session, "missing 'template' parameter");

		boolean runOnce	= getNamedBooleanParam(argStruct, "runonce", false );
		
		cfFile svrFile	= cfINCLUDE.loadTemplate( _session, template );
		
		if ( runOnce )
			_session.setDataBin( String.valueOf(svrFile.hashCode()), true);
		else{
			Boolean runOnceChk	= (Boolean)_session.getDataBin(String.valueOf(svrFile.hashCode()));
			if ( runOnceChk != null )
				return cfStringData.EMPTY_STRING;
		}
		
		return new cfStringData( svrFile.renderToString( _session ).getOutput() );
	}
}
