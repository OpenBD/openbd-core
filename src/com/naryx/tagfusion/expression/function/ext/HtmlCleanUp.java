/* 
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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

package com.naryx.tagfusion.expression.function.ext;

import java.io.StringReader;
import java.io.StringWriter;

import org.w3c.tidy.Tidy;
import org.w3c.tidy.TidyMessage;
import org.w3c.tidy.TidyMessageListener;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


/*
 * Provides a wrapper to the JTidy release
 */
public class HtmlCleanUp extends functionBase {
  
  private static final long serialVersionUID = 1L;
	
  public HtmlCleanUp(){ 
  	min = 1; max = 1;
  	setNamedParams( new String[]{ "string" } );
  	}
  
	public String[] getParamInfo(){
		return new String[]{
			"html block",
		};
	}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"string", 
				"Runs the W3 Tidy utility against the passed in HTML, performing a series of cleanup and HTML repair routines", 
				ReturnType.STRING );
	}
 

	public cfData execute(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
  	try{
  		Tidy tidy	= new Tidy();

  		final StringBuilder	errors	= new StringBuilder(32);
  		
  		tidy.setMessageListener( new TidyMessageListener(){
				public void messageReceived(TidyMessage mess) {
					errors.append( "Line: " + mess.getLine() + "." + mess.getColumn() + "; " + mess.getMessage() + "\r\n" );
				}
  		});

    	tidy.setSmartIndent( false );
    	tidy.setSpaces( 2 );
    	tidy.setTabsize( 2 );
    	tidy.setWraplen( 0 );

    	tidy.setLogicalEmphasis( true );
    	tidy.setMakeClean( true );
    	tidy.setQuiet( true );
    	tidy.setDropEmptyParas( true );
    	tidy.setXHTML( true );
    	tidy.setXmlSpace( true );
    	tidy.setTrimEmptyElements( true );
    	tidy.setBreakBeforeBR( false );
    	tidy.setUpperCaseTags( false );
    	tidy.setUpperCaseAttrs( false );
    	tidy.setWord2000( true );

    	tidy.setFixUri(false);
    	tidy.setFixBackslash( false );
    	tidy.setIndentAttributes( false );
    	tidy.setShowWarnings( false );
    	tidy.setShowErrors( 1 );
    	tidy.setOnlyErrors( false );

    	tidy.setPrintBodyOnly( false );
    	tidy.setJoinClasses( true );
    	tidy.setJoinStyles( true );
    	
    	String inHtml = getNamedStringParam(argStruct,"string","");
    	
    	StringReader reader = new StringReader( inHtml );
    	StringWriter writer = new StringWriter();
    	tidy.parse( reader, writer );
    	
    	if ( errors.length() != 0 ){
    		throwException( _session, errors.toString() );
    		return null;
    	}else{
    	
	    	String outHtml	= writer.toString();
	    	int c1	= outHtml.indexOf("<body>");
	    	if ( c1 >= 0 ){
	    		outHtml	= outHtml.substring( c1 + 6 );
	    		c1 = outHtml.lastIndexOf("</body>");
	    		if ( c1 >= 0 ){
	    			outHtml	= outHtml.substring( 0, c1 );
	    		}
	    	}

	    	return new cfStringData( outHtml );
    	}
    	
  	}catch( Exception e ){
  		throwException( _session, e.getMessage() );
  		return null;
  	}
  }
}
