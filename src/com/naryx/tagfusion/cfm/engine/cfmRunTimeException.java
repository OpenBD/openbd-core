/*
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: cfmRunTimeException.java 2326 2013-02-09 19:30:40Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;
import com.naryx.tagfusion.cfm.file.cfmlURI;
import com.naryx.tagfusion.cfm.file.sourceReader;
import com.naryx.tagfusion.cfm.tag.cfDUMP;
import com.naryx.tagfusion.cfm.tag.cfERROR;
import com.naryx.tagfusion.cfm.tag.cfOUTPUT;
import com.naryx.tagfusion.cfm.tag.cfSCRIPT;
import com.naryx.tagfusion.cfm.tag.cfTRY;
import com.naryx.tagfusion.expression.compile.expressionEngine;

public class cfmRunTimeException extends Exception {
	private static final long serialVersionUID = 1L;

	protected cfCatchData catchData;
	private Throwable incomingException;
	private String	output; //this var and its use is part of the fix for NA bug #3282
	private boolean rethrow = false;
	private java.io.File logFile = null;

	public void setOutput( String o ) { output = o; }
	public String getOutput() { return ( output == null ? "" : output ); }

	public boolean isRethrow() {
		return rethrow;
	}

	public cfmRunTimeException() {
		super();
		catchData = new cfCatchData();
	}

	public cfmRunTimeException(cfSession session, Throwable E) {
		super();

		catchData = new cfCatchData(session);
		catchData.setInternal(true);

		incomingException = E;
	}

	public cfmRunTimeException(cfCatchData _catchData) {
		this(_catchData, false);
	}

	public cfmRunTimeException(cfCatchData _catchData, boolean _rethrow) {
		super(_catchData.getString("message"));
		catchData = _catchData;
		rethrow = _rethrow;
	}

	public cfmRunTimeException(cfCatchData _catchData, cfmBadFileException badFile) {
		catchData = _catchData;
		this.incomingException = badFile;
	}

	/**
	 * Overrides java.lang.Throwable. We don't care about Java stack traces for
	 * CFML runtime exceptions, so save the effort of filling it in.
	 */
	public Throwable fillInStackTrace() {
		return this;
	}

	public java.io.File getLogFile() {
		return logFile;
	}

	public String getMessage() {
		cfCatchData data;

		// If there are multiple errors then retrieve the message for the first error
		List<cfCatchData> errors = catchData.getErrorList();
		if (errors == null)
			data = catchData;
		else
			data = errors.get(0);

		String detail = data.getString("detail");
		if ((detail != null) && (detail.length() > 0))
			return detail;

		return data.getString("message");
	}
	
	
	public String getMessageThenDetail() {
		cfCatchData data;

		// If there are multiple errors then retrieve the message for the first error
		List<cfCatchData> errors = catchData.getErrorList();
		if (errors == null)
			data = catchData;
		else
			data = errors.get(0);

		String message = data.getString("message");
		if ((message != null) && (message.length() > 0))
			return message;

		return data.getString("detail");
	}

	public cfCatchData getCatchData() {
		return catchData;
	}


	public String toString() {
		return catchData.toString();
	}


	public void handleException(cfSession _Session) {
		try {
			_Session.setData(cfTRY.CFCATCHVAR, catchData);
		} catch (cfmRunTimeException e) { // shouldn't occur
			cfEngine.log("Unexpected exception in cfmRunTimeException.handleException: ");
		}

		catchData.setSession(_Session);
		_Session.recordException(this, _Session.activeFile(), _Session.activeTag());
		_Session.clearCfSettings();
		_Session.setSuppressWhiteSpace(false);

		// Determine a logfile if they want to have all RunTimeExceptions saved
		if (cfEngine.thisPlatform.getFileIO().isRunTimeLoggingEnabled()) {
			logFile = cfEngine.thisPlatform.getFileIO().getRunTimeLoggingFile();
			if (logFile != null) { // make sure log file was created successfully
				catchData.setData("errorlogfile", new cfStringData(logFile.getAbsolutePath()));
			}
		}

		// Check to see if the user has specified a CFERROR tag for this error
		boolean errorHandled = false;
		cfErrorData errorData = (cfErrorData) _Session.getDataBin(cfERROR.DATA_BIN_KEY);
		if (errorData != null) {
			if (errorData.handleExceptionError(_Session, catchData) || errorData.handleRequestError(_Session, catchData)) {
				errorHandled = true;
				if (logFile == null)
					return; // not logging the error
			}
		}

		if (!errorHandled) {
			errorHandled = runDefaultErrorHandler(_Session);
		}

		if (!errorHandled) {
			try {
				// set the response status code to 500 before flushing the page
				_Session.setStatus(javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (cfmRunTimeException ignore) {}

			// flush the output so you can see how far the output got prior to the
			// exception
			_Session.pageFlush();
		}

		StringBuilder buffer = new StringBuilder(32000);
		boolean internalError = catchData.isInternal();

		buffer.append("\r\n<p></p><html><title>OpenBD:");
		buffer.append( (internalError ? "Internal Server Error" : "CFML RunTime Error") + "</title><body>");

		String fontSize = (_Session.isWindowsOrMacUser() ? "11px" : "13px");

		buffer.append("<style>TD {font-family :  Verdana, Geneva, Arial, Helvetica, sans-serif; font-size : " + fontSize + "; }");
		buffer.append(".redheader { background-color: #CC0033;background-image: -webkit-gradient(linear, left top, left bottom, from(#CC0033), to(#ff0000)); background-image: -webkit-linear-gradient(top,#CC0033,#ff0000); background-image:-moz-linear-gradient(top, #CC0033, #ff0000); background-image: -ms-linear-gradient(top, #CC0033, #ff0000); background-image: -o-linear-gradient(top, #CC0033, #ff0000); background-image: linear-gradient(top, #CC0033, #ff0000);filter: progid:DXImageTransform.Microsoft.gradient(startColorStr='#CC0033', EndColorStr='#ff0000'); }.header_table { width: 100%; background: #fff; border-collapse: collapse; border-spacing: 0; border: 1px solid #444; -webkit-box-shadow: 0px 1px 3px #888; -moz-box-shadow: 0px 1px 3px #888; box-shadow: 0px 1px 3px #888; }.header_table tr { border-top: 1px solid #444; }</style>");
		buffer.append("<table class='header_table' width='100%' border='1' cellspacing='0' cellpadding='5' bgcolor='white'" + (internalError ? " BORDERCOLOR='Black'" : "") + ">");

		if (internalError) {
			buffer.append("<tr bgcolor='#ffff00'><td colspan='2' align='left'><a href='http://openbd.org/manual/'><img style='float: right; margin: .20em .1em .1em .3em;' src='");
			
			if ( cfEngine.OpenBDLogoDataUri != null )
				buffer.append( cfEngine.OpenBDLogoDataUri );
			else
				buffer.append( "" );
			
			buffer.append( "'/></a><font size='+1' color='black'><b>OpenBD Internal Server Error</b></font><div><a style='text-decoration:none;color:grey;margin-left:20' href='http://openbd.org/manual/'>Need some help?  Visit our manual http://openbd.org/manual/</a></div</td></tr><tr><td colspan=2><b>The page you were executing caused an internal server error</b></td></tr>");
		} else {
			buffer.append("<tr class='redheader' bgcolor='#CC0033'><td colspan='2' align='left'><a href='http://openbd.org/manual/'><img style='float: right; margin: .20em .1em .1em .3em;' src='");
			
			if ( cfEngine.OpenBDLogoDataUri != null )
				buffer.append( cfEngine.OpenBDLogoDataUri );
			else
				buffer.append( "" );
			
			buffer.append( "'/></a><font size='+1' color='yellow'><b>CFML Runtime Error</b></font><div><a style='text-decoration:none;color:white;margin-left:20' href='http://openbd.org/manual/'>Need some help?  Visit our manual http://openbd.org/manual/</a></div</td></tr>");

			if (catchData.containsKey("message")) {
				buffer.append("<tr><td colspan=2><b>");
				
				String s = com.nary.util.string.escapeHtml(catchData.getString("message"));
				if ( s.startsWith("Function: ") ){
					int c1 = s.indexOf(":")+2;
					int c2 = s.indexOf("(", c1);
					
					if ( c1 != -1 && c2 != 1 ){
						String fn = s.substring(c1,c2);
						s	= s.substring( 0, c1 ) + "<a style='text-decoration:none;color:blue;' target='_blank' title='click here for documentation' href='http://openbd.org/manual/?/function/" + fn + "'>" + fn + "</a>" + s.substring(c2);
					}
				}
				
				buffer.append(s);
				buffer.append("</b></td></tr>");
			}
		}

		StringBuilder extendedBuffer = new StringBuilder( 64000 ); // for debug and logging info

		if (cfEngine.isDebugOutputEnabled() || (logFile != null)) {
			// Print the Request that made this
			extendedBuffer.append("<tr><td width='1%' nowrap>Request<td>");
			extendedBuffer.append(com.nary.util.string.escapeHtml(_Session.getRequestURI()));
			extendedBuffer.append("</td></tr><tr><td width='1%' valign='top' nowrap>File Trace<td>");
			extendedBuffer.append(fileStack(_Session));
			extendedBuffer.append("</td></tr></table><p><table width='100%' border='0' cellspacing='1' cellpadding='5' bgcolor='f4f4f4'>");

			if (internalError) {
				extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' nowrap>Type</td><td>Internal</td></tr>");
			} else if (catchData.containsKey("type")) {
				extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' nowrap>Type</td><td>");
				extendedBuffer.append(catchData.getString("type"));
				extendedBuffer.append("</td></tr>");
			}

			if ( catchData.containsKey("errnumber") ){
				String err = catchData.getString("errnumber");
				if ( err.indexOf("function") != -1 ){
					String func = expressionEngine.getFunctionName( err );
					if ( func != null ){
						extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' nowrap>Function</td><td><a style='text-decoration:none;color:blue;' target='_blank' title='click here for documentation' href='http://openbd.org/manual/?/function/");
						extendedBuffer.append( func );
						extendedBuffer.append( "'>" );
						extendedBuffer.append( func );
						extendedBuffer.append("</a></td></tr>");
					}
				}
			}
			
			String[] functionList = catchData.getFunctionList();
			if (functionList.length > 0) {
				extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' valign='top' nowrap>Function(s)</td><td>");
				extendedBuffer.append(functionList(functionList));
				extendedBuffer.append("</td></tr>");
			}

			if (catchData.containsKey("queryerror")) {
				extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' nowrap>Query Error</td><td>");
				extendedBuffer.append(com.nary.util.string.escapeHtml(catchData.getString("queryerror")));
				extendedBuffer.append("</td></tr>");

				if (catchData.containsKey("datasource")) {
					extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' nowrap>Datasource</td><td>");
					extendedBuffer.append(catchData.getString("datasource"));
					extendedBuffer.append("</td></tr>");
				}

				if (catchData.containsKey("nativeerrorcode")) {
					extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' nowrap>Native Error Code</td><td>");
					extendedBuffer.append(catchData.getString("nativeerrorcode"));
					extendedBuffer.append("</td></tr>");
				}

				if (catchData.containsKey("sqlstate")) {
					extendedBuffer.append("<TR BGCOLOR='E0E0E0'><TD WIDTH='1%' NOWRAP>SQL State</td><td>");
					extendedBuffer.append(catchData.getString("sqlstate"));
					extendedBuffer.append("</td></tr>");
				}

				if (catchData.containsKey("sql")) {
					extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' valign='top' nowrap>SQL</td><td>");
					extendedBuffer.append(com.nary.util.string.escapeHtml(catchData.getString("sql")));
					extendedBuffer.append("</td></tr>");
				}
			} else {
				if (catchData.containsKey("detail")) {
					String detail = catchData.getString("detail");
					if (detail.length() > 0) {
						extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' valign='top' nowrap>Detail</td><td>");
						extendedBuffer.append(com.nary.util.string.escapeHtml(detail));
						extendedBuffer.append("</td></tr>");
					}
				}

				if (catchData.containsKey("extendedinfo")) {
					String extendedInfo = catchData.getString("extendedinfo");
					if (extendedInfo.length() > 0) {
						extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' valign='top' nowrap>Extended Info</td><td>");
						extendedBuffer.append(com.nary.util.string.escapeHtml(extendedInfo));
						extendedBuffer.append("</td></tr>");
					}
				}
			}

			if (catchData.containsKey("missingfilename")) {
				extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' nowrap>Missing File</td><td>");
				extendedBuffer.append(catchData.getString("missingfilename"));
				extendedBuffer.append("</td></tr>");
			}

			if (catchData.containsKey("lockname")) {
				extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' nowrap>Lock Name</td><td>");
				extendedBuffer.append(catchData.getString("lockname"));
				extendedBuffer.append("</td></tr>");
			}

			if (catchData.containsKey("lockoperation")) {
				extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%'>Lock Operation</td><td>");
				extendedBuffer.append(catchData.getString("lockoperation"));
				extendedBuffer.append("</td></tr>");
			}

			if (catchData.containsKey("tagcontext")) {
				extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' valign='top' nowrap>Tag Context</td><td>");
				extendedBuffer.append(tagStack((cfArrayData) catchData.getData("tagcontext")));
				extendedBuffer.append("</td></tr>");
			}

			// Get source, but not for precompiled templates
			// activeFile may be null if Exception occurs after the last popActive()
			cfFile activeFile = _Session.activeFile();
			if (activeFile != null) {
				printSourceCode( _Session, extendedBuffer, catchData, activeFile.getCfmlURI() );
			}

			// If there is any suberrors
			List<cfCatchData> listError = catchData.getErrorList();
			if (listError != null) {
				extendedBuffer.append("<tr><td bgcolor='white'>&nbsp;</td><td>");
				printSubErrors(_Session, extendedBuffer, listError.iterator());
				extendedBuffer.append("</td></tr>");
			}

			// Display the Stack Trace
			if (incomingException != null) {
				if (incomingException instanceof cfmBadFileException) {
					cfmBadFileException bfe = (cfmBadFileException) incomingException;
					if (!bfe.fileNotFound()) {
						cfCatchData bfeCatchData = bfe.getCatchData();
						listError = bfeCatchData.getErrorList();
						if (listError == null) {
							listError = new ArrayList<cfCatchData>();
							listError.add(bfeCatchData);
						}
						printSubErrors(_Session, extendedBuffer, listError.iterator());
					}
				} else {
					extendedBuffer.append("<tr bgcolor='e0e0e0'><td width='1%' valign='top' nowrap>Stack Trace</td><td><pre>");
					extendedBuffer.append(com.nary.Debug.getStackTraceAsString(incomingException));
					extendedBuffer.append("</pre></td></tr>");
				}
			}

			extendedBuffer.append("</table><br/>\r\n\r\n");

			// Get all the DUMP information
			try {
				extendedBuffer.append(cfDUMP.dumpSession(_Session)); // LONG dump of all variables
			} catch (cfmRunTimeException ignore) {
			} catch (Throwable t) {
				cfEngine.log("-] ERROR: caught exception when dumping session data - " + t.toString());
			}

			if (logFile != null) {
				cfEngine.thisPlatform.getFileIO().writeLogFile(logFile, buffer.toString() + extendedBuffer.toString() + "\r\n\r\n<p></body></html>\r\n");
			}

			if (cfEngine.isDebugOutputEnabled())
				buffer.append(extendedBuffer.toString());

			if (logFile != null) {
				buffer.append("<p><table><tr><td>This error has been logged to " + logFile.getName() + ".</td><td> Please contact the Administrator</td></tr></table>");
			}
		}

		buffer.append("\r\n\r\n<p></body></html>\r\n");

		if (!errorHandled) {
			// Write it out to the page
			_Session.write(buffer.toString());
			_Session.pageEnd();
		}
	}

	protected boolean runDefaultErrorHandler(cfSession _Session) {
		try {
			String defErrHandler = cfEngine.getConfig().getString("server.system.errorhandler", "");
			if (!defErrHandler.equals("")) {
				cfErrorData.setSession(_Session, catchData);
				cfFile handler = _Session.getFile(new cfmlURI(defErrHandler));
				_Session.reset();
				_Session.setContentType("text/html");
				_Session.pushActiveFile(handler);
				_Session.write(handler.renderToString(_Session).getOutput());
				_Session.pageEnd();
				return true;
			}
		} catch (cfmRunTimeException e) {
			cfEngine.log("Error rendering default error handler");
		}
		return false;
	}

	protected String tagStack(cfArrayData tagArray) {
		try {
			StringBuilder buffer = new StringBuilder(32);
			int tab = 0;
			int arraySize = tagArray.size();

			for (int x = 0; x < arraySize; x++) {
				if (tab > 0) {
					buffer.append(space(tab - 4) + "|<br/>");
					buffer.append(space(tab - 4) + "+-- ");
				}

				cfStructData SD = (cfStructData) tagArray.getElement(x + 1);
				buffer.append( "<a style='text-decoration:none;color:blue;' target='_blank' title='click here for documentation' href='http://openbd.org/manual/?/tag/" + SD.getData("id").getString() + "'>" );
				buffer.append( SD.getData("id").getString() );
				buffer.append( "</a> (" + SD.getData( cfCatchData.TEMPLATE_KEY )
								+ ", Line=" + SD.getData( cfCatchData.LINE_KEY ).getInt()
								+ ", Column=" + SD.getData( cfCatchData.COLUMN_KEY ).getInt() + ")" );
				buffer.append("<br/>");
				tab += 4;
			}

			String buf = buffer.toString();
			if (buf.length() > 4)
				return buf.substring(0, buf.length() - 5);
			else
				return "";
		} catch (Exception ignoreException) {
			return "";
		}
	}

	private String functionList(String[] functionList) {
		StringBuilder sb = new StringBuilder( 128 );
		int tab = 0;

		for (int i = 0; i < functionList.length; i++) {
			if (tab > 0) {
				sb.append(space(tab - 4) + "|<br/>");
				sb.append(space(tab - 4) + "+--");
			}
			sb.append(functionList[i]);
			sb.append("<br/>");
			tab += 4;
		}

		return sb.toString();
	}

	private String fileStack(cfSession _Session) {
		try {
			Enumeration<cfFile> E = _Session.fileStackEnumeration();
			StringBuilder buffer = new StringBuilder(32);
			int tab = 0;

			while (E.hasMoreElements()) {
				if (tab > 0) {
					buffer.append(space(tab - 4) + "|<br/>");
					buffer.append(space(tab - 4) + "+-- ");
				}

				buffer.append(E.nextElement().getName());
				buffer.append("<br/>");
				tab += 4;
			}

			String buf = buffer.toString();
			return buf.substring(0, buf.length() - 5);
		} catch (Exception ignoreException) {
			return "";
		}
	}

	private static String space(int x) {
		StringBuilder tmp = new StringBuilder( 16 );
		for (int q = 0; q < x; q++)
			tmp.append( "&nbsp;" );

		return tmp.toString();
	}

	private static String padNumber(int x, int pad) {
		String tmp = x + "";
		int t = pad - tmp.length();
		for (int v = 0; v < t; v++)
			tmp += " ";

		return tmp;
	}


	// ----------------------------------------------------------------------------------
	// ----------------------------------------------------------------------------------

	protected void printSubErrors(cfSession _Session, StringBuilder buffer, Iterator<cfCatchData> i) {

		while (i.hasNext()) {
			cfCatchData cd = i.next();

			if (cd.containsKey("type")) {
				buffer.append("<TR BGCOLOR='E0E0E0'><TD WIDTH='1%' NOWRAP>Type</TD><TD>");
				buffer.append(cd.getString("type"));
				buffer.append("</TD></TR>");
			}

			buffer.append("<TR BGCOLOR='E0E0E0'><TD WIDTH='1%' NOWRAP>Message</TD><TD>");
			buffer.append(cd.getString("message"));
			buffer.append("</TD></TR>");

			if (cd.getString("tagname").length() > 0) {
				buffer.append("<TR BGCOLOR='E0E0E0'><TD WIDTH='1%' NOWRAP>Tag</TD><TD>");
				buffer.append(com.nary.util.string.escapeHtml(cd.getString("tagname")));
				buffer.append("</TD></TR>");
			}

			if (cd.getString("line").length() > 0) {
				buffer.append("<TR BGCOLOR='E0E0E0' WIDTH='1%' NOWRAP><TD>Position</TD><TD>Line=");
				buffer.append(cd.getString("line"));
				buffer.append("; Column=");
				buffer.append(cd.getString("column"));
				buffer.append("</TD></TR>");
			}

			if (cd.getString("detail").length() > 0) {
				buffer.append("<TR BGCOLOR='E0E0E0' WIDTH='1%' NOWRAP><TD>Detail</TD><TD>");
				buffer.append(com.nary.util.string.escapeHtml(cd.getString("detail")));
				buffer.append("</TD></TR>");
			}

			if (cd.getString("extendedinfo").length() > 0) {
				buffer.append("<TR BGCOLOR='E0E0E0' WIDTH='1%' NOWRAP><TD>Extended&nbsp;Info</TD><TD>");
				buffer.append(com.nary.util.string.escapeHtml(cd.getString("extendedinfo")));
				buffer.append("</TD></TR>");
			}

			printSourceCode(_Session, buffer, cd, null);
		}
	}

	// ----------------------------------------------------------------------------------

	private void printSourceCode(cfSession _Session, StringBuilder buffer, cfCatchData cd, cfmlURI uri) {
		cfmlURI thisUrl = (uri == null) ? cd.getFileURI() : uri;
		if (thisUrl == null)
			return;

		// If a line wasn't specified then we can't display the source code
		cfData lineErrorData = cd.getData("line");
		if (lineErrorData == null)
			return;

		try {
			BufferedReader in = cfmlFileCache.getReader(_Session.REQ, _Session.CTX, thisUrl);

			int lineError 		= lineErrorData.getInt();
			cfData scriptLine = cd.getData("scriptline");
			
			if ( scriptLine != null 
					&& ( (_Session.getTagStack().empty() && this instanceof cfmBadFileException) 
					|| (_Session.getTagStack().peek() instanceof cfSCRIPT || _Session.getTagStack().peek() instanceof cfOUTPUT)
					)) {
				lineError += scriptLine.getInt();
			}
			
			sourceReader sR = new sourceReader(in, lineError - 4, 5);
			String[] lines 	= sR.getLines();
			int padSize 		= String.valueOf(lineError+5).length();

			buffer.append("<tr bgcolor='E0E0E0'><td width='1%' valign='top' nowrap>Source</td><td><pre>");

			for (int x = 0; x < lines.length; x++) {
				if (lines[x] == null)
					break;
				if (lineError == ((x + sR.getLineStart() + 1)))
					buffer.append("<font color='red'>");

				buffer.append(padNumber((x + sR.getLineStart() + 1), padSize));
				buffer.append(": ");
				buffer.append(com.nary.util.string.escapeHtml(com.nary.util.string.replaceString(lines[x], "\t", "  ")));

				if (lineError == ((x + sR.getLineStart() + 1)))
					buffer.append("</font>");
				
				if (x < lines.length - 1)
					buffer.append("\r\n");
			}

			buffer.append("</pre>^ Snippet from underlying CFML source</td></tr>");

			in.close();
		} catch (Exception E) {
			cfEngine.log("Error: " + E.toString() + ", displaying source code snippet for: " + thisUrl.getRealPath(_Session.REQ));
		}
	}
}
