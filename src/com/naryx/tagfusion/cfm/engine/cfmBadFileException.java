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
 *  $Id: cfmBadFileException.java 1776 2011-11-06 13:12:27Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.nary.io.StreamUtils;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlURI;

public class cfmBadFileException extends cfmRunTimeException {
	private static final long serialVersionUID = 1L;

	private boolean fileMissing = false;
	private boolean pageEncodingException = false;
	private static String	templateCode = null; 

	public cfmBadFileException(cfCatchData _cfData) {
		this(_cfData, null);
	}

	public cfmBadFileException(cfCatchData _cfData, cfStringData type) {
		super(_cfData);
		if (type != null)
			catchData.setType(type);
		else
			catchData.setType(cfCatchData.TYPE_TEMPLATE);
	}

	// the _file parameter must be the absolute path to the bad file
	public cfmBadFileException(String _file, cfmBadFileException BF) {
		catchData = BF.catchData;
		catchData.setFileURI(new cfmlURI(_file, true));
	}

	// use this constructor for "file not found" errors
	public cfmBadFileException(String _file) {
		fileMissing = true;
		catchData = catchDataFactory.missingRequestFileException(_file);
	}

	
	public boolean fileNotFound() {
		return fileMissing;
	}

	public boolean isPageEncodingException() {
		return pageEncodingException;
	}

	public void setPageEncodingException(boolean _b) {
		pageEncodingException = _b;
	}

	public void handleException(cfFile svrFile, cfSession _Session) {
		_Session.clearCfSettings();
		_Session.reset();
		_Session.setSuppressWhiteSpace(false);

		// if fileMissing, chekc if a default handler specified
		if (fileMissing) {
			try {
				_Session.setStatus(404, "Not Found");
			} catch (cfmRunTimeException ignored) {
			}

			try {
				// retrieve the missing template handler from the config
				String handlerTempl = cfEngine.getConfig().getString("server.system.missingtemplatehandler", "");
				if (!handlerTempl.equals("")) { // using it if it exists
					cfFile handler = _Session.getFile(new cfmlURI(handlerTempl));
					try {
						_Session.pushActiveFile(handler);
						_Session.write(handler.renderToString(_Session).getOutput());
					} catch (cfmAbortException ignore) {
					} catch (cfmRunTimeException e) {
						e.handleException(_Session);
					} finally {
						_Session.popActiveFile();
						_Session.pageEnd();
					}
					return;
				}
			} catch (cfmRunTimeException rte) {
				// note: file might not exist file might not execute successfully in both cases use default handling
			}
		}

		// check for default error handler
		if (super.runDefaultErrorHandler(_Session)) {
			return;
		}

		String htmlOutput;
		if ( !fileMissing ){
			StringBuilder buffer	= new StringBuilder(32000);
			 
			buffer.append("<html><title></title><body>");
			String fontSize = (_Session.isWindowsOrMacUser() ? "11px" : "13px");
			buffer.append("<style>TD {font-family :  Verdana, Geneva, Arial, Helvetica, sans-serif; font-size : " + fontSize + "; }");
			buffer.append( ".redheader { background-color: #CC0033;background-image: -webkit-gradient(linear, left top, left bottom, from(#CC0033), to(#ff0000)); background-image: -webkit-linear-gradient(top,#CC0033,#ff0000); background-image:-moz-linear-gradient(top, #CC0033, #ff0000); background-image: -ms-linear-gradient(top, #CC0033, #ff0000); background-image: -o-linear-gradient(top, #CC0033, #ff0000); background-image: linear-gradient(top, #CC0033, #ff0000);" );
			buffer.append("filter: progid:DXImageTransform.Microsoft.gradient(startColorStr='#CC0033', EndColorStr='#ff0000'); }");
			buffer.append(".header_table { width: 100%; background: #fff; border-collapse: collapse; border-spacing: 0; border: 1px solid #444; -webkit-box-shadow: 0px 1px 3px #888; -moz-box-shadow: 0px 1px 3px #888; box-shadow: 0px 1px 3px #888; }");
			buffer.append(".header_table tr { border-top: 1px solid #444; }");
			buffer.append("</style>");
			
			buffer.append("<table class='header_table' width='100%' border='1' cellspacing='0' cellpadding='5' bgcolor='white' BORDERCOLOR='Black'>");
			buffer.append("<tr bgcolor='#ff9900'><td colspan='2' align='left'><a href='http://openbd.org/manual/'><img style='float: right; margin: .20em .1em .1em .3em;' src='");
			
			if ( cfEngine.OpenBDLogoDataUri != null )
				buffer.append( cfEngine.OpenBDLogoDataUri );
			else
				buffer.append( "" );
			
			buffer.append( "'/></a><font size='+1' color='black'><b>OpenBD Bad File Exception</b></font><div><a style='text-decoration:none;color:grey;margin-left:20' href='http://openbd.org/manual/'>Need some help?  Visit our manual http://openbd.org/manual/</a></div</td></tr>");
			buffer.append("<tr><td colspan=2><b>The page you were executing caused an internal server error</b></td></tr></table>");
			
			List<cfCatchData> listError	= catchData.getErrorList();
			if ( listError == null ){
				listError	= new ArrayList<cfCatchData>();
				listError.add( catchData );
			}
			
			buffer.append( "<table width='100%' border='0' cellspacing='1' cellpadding='5' bgcolor='F4F4F4' style='font-size:0.7em; margin-top: 20px;'>");
			printSubErrors( _Session, buffer, listError.iterator() );
			buffer.append( "</table>" );
			
			htmlOutput	= buffer.toString();
						
		}else{
			// We have to render our own output handler
			if ( templateCode == null ){
				try {
					templateCode 	= StreamUtils.readToString( this.getClass().getResourceAsStream("badFileTemplate.txt") );
				} catch (IOException e) {
					templateCode 	= "<html><body><h1>%REQUEST_URI%</h1></body></html>";
				}
			}

			htmlOutput	= com.nary.util.string.replaceString(templateCode, "%REQUEST_URI%", com.nary.util.string.escapeHtml(_Session.getRequestURI()) );
			htmlOutput	= com.nary.util.string.replaceString(htmlOutput, "%TIME%", com.nary.util.Date.formatNow("kk:mm:ss.SSS  d MMM yyyy ") );
			htmlOutput	= com.nary.util.string.replaceString(htmlOutput, "%DETAIL%", "" );
		}

		_Session.write(htmlOutput);
		
		try {
			_Session.pageEnd();
		} catch (Exception ignoreE) {
		}
	}
}
