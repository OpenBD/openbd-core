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
import java.util.Vector;

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfcMethodData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class CFNewExpression extends CFExpression implements java.io.Serializable{

	private static final long serialVersionUID = 1L;

	private String componentPath;
	private Vector args;
	
	public CFNewExpression( Token _t, String _component, Vector _args ){
		super( _t );
		componentPath = _component;
		args = _args;
	}
	
	@Override
	public cfData Eval( CFContext context ) throws cfmRunTimeException {
		cfSession session = context.getSession(); 
		cfComponentData component = new cfComponentData( session, componentPath, session.activeFile().getImportedPaths() );
		cfData initFunction = component.getData( "init" );
		if ( initFunction != null && initFunction.getDataType() == cfData.CFUDFDATA ){
			component.invokeComponentFunction( session, getMethodData( context ) );
		}
		
		return component;
	}
	
	private cfcMethodData getMethodData( CFContext context ) throws cfmRunTimeException{
		if ( args instanceof ArgumentsVector ){
			cfArgStructData argsData = CFJavaMethodExpression.prepareArguments( context, (ArgumentsVector) args );
			return new cfcMethodData( context.getSession(), "init", argsData );
		}else{
			cfData evaluated = null;
			ArrayList<cfData> argsData = new ArrayList<cfData>(args.size());
			for( int i = 0; i < args.size(); i++ ) {
				evaluated = ( (CFExpression) args.get( i ) ).Eval( context );
				if ( evaluated instanceof indirectReferenceData) {
					evaluated = ((indirectReferenceData) evaluated).Get(context);
				} else if (evaluated.getDataType() == cfData.CFLDATA) {
					evaluated = ((cfLData) evaluated).Get(context);
				}

				argsData.add( evaluated );
			}
			return new cfcMethodData( context.getSession(), "init", argsData );
		}
	}
	
	@Override
	public String Decompile( int indent ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "new " );
		sb.append( componentPath );
		sb.append( "(" );
		for (int i = 0; i < args.size(); i++) {
			sb.append( (args.elementAt(i)).toString() );
			if ( i < args.size() - 1 ) {
				sb.append( ", " );
			}
		}
		sb.append( ")" );
		
		return sb.toString();
	}
}
