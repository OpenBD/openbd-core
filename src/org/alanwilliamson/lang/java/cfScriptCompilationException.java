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

package org.alanwilliamson.lang.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.tag.cfTag;

public class cfScriptCompilationException extends cfmBadFileException {
	private static final long serialVersionUID = 1L;
	
	private CharSequenceCompilerException compilerException;
	private String javablock;
	private int srcOffset = 14;
	private cfTag tag;
	
	public cfScriptCompilationException(CharSequenceCompilerException e, String javablock, cfCatchData cfData, cfTag tag, int offset) {
		super(cfData);
		
		compilerException = e;
		this.javablock = javablock;
		this.tag	= tag;
		srcOffset	= offset;
	}

  public void handleException( cfFile svrFile, cfSession _Session ){
		_Session.clearCfSettings();
		_Session.reset();
		_Session.setSuppressWhiteSpace( false );
		
		// check for default error handler
		if ( super.runDefaultErrorHandler( _Session ) ) {
			return;
		}
		
    StringBuilder	buffer	= new StringBuilder( 1024 );
		
  	buffer.append( "\r\n<p><html><title>cfScript Compilation Error</title><body>" );
        
    String fontSize = ( _Session.isWindowsOrMacUser() ? "11px" : "13px" );
      
		buffer.append( "<style><!--\r\ntd, li {font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; font-size : " + fontSize + "; }" +
				"th{color: #AFAFAF !important;font-weight: normal;text-align:right;border-right: 2px solid #00bf30;padding-right: 3px;font-size : " + fontSize + ";}" +
				"pre{margin:0px;}" +
				".rowHi{background-color: #f9f9f9}" +
				".error {color:red;}" +
				"ul { margin-top: 0px; padding-top: 0px; }" +
				" //--></style>" );
		
		buffer.append( "<table style='margin-bottom:10px;' width='100%' border='1' cellspacing='0' cellpadding='5' bgcolor='White'>");
    buffer.append( "<tr bgcolor='#80ff80'><td colspan='2' align='LEFT'><b><font size='+1'>cfScript language='java' Compilation Error</b></font></td></tr><tr><td width='1%' nowrap>Request</TD><TD>" );
		buffer.append( com.nary.util.string.escapeHtml(_Session.getRequestURI()) ); 
		buffer.append( "</td></tr>" );
		
		if ( tag.getFile() != null ){
	    buffer.append( "<tr><td width='1%' nowrap>Template</TD><TD>" );
			buffer.append( com.nary.util.string.escapeHtml( tag.getFile().getPath() ) ); 
			buffer.append( "</td></tr>" );
		}
		buffer.append( "</table>" );
		
		buffer.append("<div class='openbdjava'>");
		DiagnosticCollector<JavaFileObject> dC = compilerException.getDiagnostics();
		Iterator<Diagnostic<? extends JavaFileObject>> it = dC.getDiagnostics().iterator();
		Diagnostic<? extends JavaFileObject> diagnostic;
		
		buffer.append( "<ul>" );
		while ( it.hasNext() ){
			diagnostic = it.next();
			String m = trimMessage( diagnostic.getMessage(null) );
			
			buffer.append( "<li><a style='text-decoration:none' href='#line" + (diagnostic.getLineNumber()-srcOffset) + "'>Line#" );
			buffer.append( diagnostic.getLineNumber()-srcOffset );
			buffer.append( "</a>: " );
			buffer.append( com.nary.util.string.escapeHtml(m) );
			buffer.append( "</li>" );
		}
		buffer.append( "</ul>" );

		
		
		// Reset
		it = dC.getDiagnostics().iterator();
		diagnostic = it.next();
		
		BufferedReader r = new BufferedReader( new StringReader(javablock) );
		String javaline;
		int lineNo = 1;
		try {
			
			buffer.append("<pre>&lt;cfscript language='java'&gt;</pre><table style='margin-left: 10px;'>");
			
			while ( (javaline=r.readLine())!=null ){
				
				buffer.append("<tr");
				if ( lineNo % 2 == 0 )
					buffer.append( " class='rowHi'");

				buffer.append( "><th width='1%'><pre>" + lineNo + "</pre></th>" );
				buffer.append( "<td><pre>" + com.nary.util.string.escapeHtml(javaline).replace( "\t", "  " ) + "</pre></td></tr>" );
				
				while ( diagnostic != null && diagnostic.getLineNumber() == lineNo+srcOffset ){
					formatDiagnostic(lineNo, buffer, diagnostic );

					if ( it.hasNext() )
						diagnostic = it.next();
					else
						diagnostic = null;
				}
				
				lineNo++;
			}
			
			buffer.append("</table><pre>&lt;/cfscript&gt;</pre>");
			
		} catch (IOException e) {
		}
		
		buffer.append("</div>");
		
		buffer.append( "<table width='100%' border='1' cellspacing='0' style='margin-top: 30px;' cellpadding='5' bgcolor='White'>");
    buffer.append( "<tr bgcolor='#80ff80'><td align='LEFT'>" );
		buffer.append( cfEngine.PRODUCT_NAME );
		buffer.append( " Time @ Server: " );
		buffer.append( com.nary.util.Date.formatNow( "kk:mm:ss.SSS   EEEE, d MMMM yyyy " ) );
		buffer.append( "</td></tr></table></body></html>\r\n\r\n" );
		
    _Session.write( buffer.toString() );
		try{_Session.pageEnd();}catch(Exception ignoreE){}
  }

  private void formatDiagnostic(int lineNo, StringBuilder buffer, Diagnostic diagnostic ){
		String m = trimMessage( diagnostic.getMessage(null) );
		
		buffer.append("<tr");
		if ( lineNo % 2 == 0 )
			buffer.append( " class='rowHi'");
		
		buffer.append("><td></td><td class='error'><a name='line"+ lineNo +"'/><pre>[java compiler] ");
		buffer.append( com.nary.util.string.escapeHtml(m) );
		buffer.append("</pre></td></tr>");
  }
  
  private String trimMessage( String m ){
		if ( m.indexOf(" ") != -1 )
			m = m.substring(m.indexOf(" "));
		if ( m.lastIndexOf("location:") != -1 )
			m = m.substring(0, m.lastIndexOf("location:") );
		
		return m;
  }
  
}
