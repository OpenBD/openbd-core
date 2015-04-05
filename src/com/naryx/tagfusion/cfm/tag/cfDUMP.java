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
 *  $Id: cfDUMP.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.util.fullRecorder;

/**
 * This class implements the ColdFusion CFDUMP tag. It has the benefit over the
 * CF implementation of printing out *all* the variables, should the VAR="..."
 * attribute not be present.
 */
public class cfDUMP extends cfTag implements Serializable {

	static final long serialVersionUID = 1;

	public static final int TOP_DEFAULT = 9999;

	public java.util.Map getInfo() {
		return createInfo("debugging", "This tag outputs the content of a CFML variable to the browser, often used during debugging or as a quick way to view variables at runtime.");
	}

	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] { 
				createAttInfo("ATTRIBUTECOLLECTION", "A structure containing the tag attributes", 	"", false ),
				createAttInfo("VAR", "A variable or text string to be output to the browser at runtime.", "", true), 
				createAttInfo("LABEL", "A label or title to be shown for the output of the VAR attribute.", "", false), 
				createAttInfo("TOP", "How deep to iterate into the variable, e.g. the number of rows to output.", "9999", false),
				createAttInfo("ABORT", "Abort this request as soon as the tag has finished", "false", false), 
				createAttInfo("OUTPUT", "Determines where the output of this dump goes. Can be 'browser', or the full path to a file name to use", "browser", false),
				createAttInfo("VERSION", "Determines how verbose query and java objects should outputted", "short", false) 
		};
	}

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("LABEL", "");
		defaultAttribute("ABORT", "false");
		defaultAttribute("OUTPUT", "browser");
		parseTagHeader(_tag);
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);
		cfData version = getDynamic(attributes,_Session, "VERSION"); // default to LONG for
		
		// query variables and CFCs, SHORT for everything else
		cfData _variable = getDynamic(attributes,_Session, "VAR");
		if (version == null) {
			if ((_variable != null) && ((_variable.getDataType() == cfData.CFQUERYRESULTDATA) || (_variable.getDataType() == cfData.CFCOMPONENTOBJECTDATA))) {
				version = new cfStringData("LONG");
			} else {
				version = new cfStringData("SHORT");
			}
		}
		
		int top = TOP_DEFAULT;
		if (containsAttribute(attributes,"TOP")) {
			top = getDynamic(attributes,_Session, "TOP").getInt();
		}
		
		String label 	= getDynamic(attributes,_Session, "LABEL").getString();
		String output	= getDynamic(attributes,_Session, "OUTPUT").getString();
		
		String outString;
		if ( output.equalsIgnoreCase("browser") ){
			outString = cfDUMP.dumpSession(_Session, _variable, version, label, top, false, false );
			if (outString != null) {
				_Session.forceWrite(outString);
			}
		}else{
			outString = cfDUMP.dumpSession(_Session, _variable, version, label, top, false, true );
			try {
				FileUtils.writeFile( new File(output), outString);
			} catch (IOException e) {
				throw newRunTimeException( "Failed to write file [" + output + "]; " + e.getMessage() );	
			}
		}
		
		
		// Determine if this request should stop
		if ( getDynamic(attributes,_Session, "ABORT").getBoolean() )
			_Session.abortPageProcessing(true);
		
		return cfTagReturnType.NORMAL;
	}
	
	
	// LONG dump of all variables, no LABEL (invoked by
	// cfmRunTimeException.handleException)
	public static String dumpSession(cfSession _Session) throws cfmRunTimeException {
		return dumpSession(_Session, null, null, "", TOP_DEFAULT, true, false );
	}

	// LONG dump of specified variable, no LABEL (invoked by CFTRACE.render)
	public static String dumpSession(cfSession _Session, cfData _variable) throws cfmRunTimeException {
		return dumpSession(_Session, _variable, null, "", TOP_DEFAULT, false, false );
	}

	public static String dumpSession(cfSession _Session, cfData _variable, cfData _version, String _label, int _top, boolean debug, boolean forceHeader ) throws cfmRunTimeException {
		boolean longVersion = true; // default is to do long version
		if (_version != null) {
			longVersion = !_version.getString().equalsIgnoreCase("SHORT");
		}

		CharArrayWriter outChar = new CharArrayWriter(1024);
		PrintWriter out = new PrintWriter(outChar);

		writeDumpStyles(_Session, out,forceHeader);

		if (_variable == null) {

			out.write("<table width='100%' border='0' cellspacing='1' cellpadding='5' bgcolor='#F4F4F4'>");

			cfData Data = _Session.getLocalScope();
			if ((Data != null) && (!debug || fullRecorder.getConfigBoolean("variables.local", true))) {
				out.write("<tr><td class='cfdump_td_name' style='font-weight: bold'>locals</td>");
				out.write("<td class='cfdump_td_value' bgcolor='#ffffff'>");
				if (longVersion)
					Data.dumpLong(out, _label, _top);
				else
					Data.dump(out, _label, _top);
				out.write("</td></tr>");
			}

			out.write("<tr><td class='cfdump_td_name' colspan='2'>Expanded Debug Details - Click to expand the detailed variables</td></tr>");
			
			Map<String, cfStructData> hT = _Session.getDataStore();
			Enumeration<String> E = new com.nary.util.StringSortedEnumeration(Collections.enumeration(hT.keySet()));
			while (E.hasMoreElements()) {
				String Key = E.nextElement();

				// check Debug configuration for which scopes to dump
				if (debug && !fullRecorder.getConfigBoolean("variables." + Key, true)) {
					continue;
				}

				// don't include Server or CGI scope in short dump
				if (!longVersion && (Key.equalsIgnoreCase(variableStore.SERVER_SCOPE_NAME) || Key.equalsIgnoreCase(variableStore.CGI_SCOPE_NAME))) {
					continue;
				}

				Data = hT.get(Key);
				out.write("<tr><td class='cfdump_td_name' width='1%' nowrap='true' style='font-weight: bold'><a class='openbd-toggle' href='#' title='click to toggle display of variable block' onclick='return openbdToggle(\"openbd-"+ Key +"\");'>");
				out.write(Key);
				out.write("</a></td><td class='cfdump_td_value' bgcolor='#ffffff'><div style='display:none;' id='openbd-" + Key + "'>");

				if (Key.equalsIgnoreCase(variableStore.URL_SCOPE_NAME) && cfEngine.isFormUrlScopeCombined())
					out.write("<I>see [form] scope</I>");
				else if (Data == null) // this should never happen?!
					out.write("[empty scope]");
				else if (longVersion)
					Data.dumpLong(out, _label, _top);
				else
					Data.dump(out, _label, _top);

				out.write("</div></td></tr>");
			}

			out.write("<tr><td class='cfdump_td_name' colspan='2'>" + cfEngine.PRODUCT_VERSION + " @ " + com.nary.util.Date.formatNow("kk:mm:ss.SSS   EEEE, d MMMM yyyy ") + "</td></tr>");
			out.write("</table>");

		} else if (longVersion) {
			_variable.dumpLong(out, _label, _top);
		} else {
			_variable.dump(out, _label, _top);
		}
		out.flush();
		return outChar.toString();
	}

	public static void writeDumpStyles(cfSession session, PrintWriter out) {
		writeDumpStyles( session, out, false );
	}
	
	public static void writeDumpStyles(cfSession session, PrintWriter out, boolean forceHeader ) {
		/* Only write out the style sheet once */
		if ( !forceHeader ){
			if (session.getDataBin("cfdumpstyles") != null )
				return;
			else
				session.setDataBin("cfdumpstyles", "1");
		}
		
		String fontSize = (session.isWindowsOrMacUser() ? "xx-small" : "small");

		out.write("<script>function openbdToggle(id){var ele = document.getElementById(id); if(ele.style.display=='block'){ele.style.display='none';}else{ele.style.display='block';} return false;}</script>");
		
		out.write("<style type=\"text/css\">");

		// the main table when the VAR attribute isn't specified
		out.write(".cfdump_table { cell-spacing: 2; background-color: #cccccc }");
		out.write(".cfdump_th { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: white; text-align: left; padding: 5px; background-color: #666666 }");
		out.write(".cfdump_td_name { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: black; text-align: left; padding: 3px; background-color: #e0e0e0; vertical-align: top }");

		// shared by all
		out.write(".cfdump_td_value { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: black; text-align: left; padding: 3px; background-color: #ffffff; vertical-align: top }");

		// struct (blue)
		out.write(".cfdump_table_struct { cell-spacing: 2; background-color: #336699 }");
		out.write(".cfdump_th_struct { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: white; text-align: left; padding: 5px; background-color: #3366cc }");
		out.write(".cfdump_td_struct { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: black; text-align: left; padding: 3px; background-color: #99ccff; vertical-align: top }");

		// array (green)
		out.write(".cfdump_table_array { cell-spacing: 2; background-color: #006600 }");
		out.write(".cfdump_th_array { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: white; text-align: left; padding: 5px; background-color: #009900 }");
		out.write(".cfdump_td_array { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: black; text-align: left; padding: 3px; background-color: #ccffcc; vertical-align: top }");

		// binary (yellow)
		out.write(".cfdump_table_binary { cell-spacing: 2; background-color: #ff6600 }");
		out.write(".cfdump_th_binary { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: white; text-align: left; padding: 5px; background-color: #ff9900 }");

		// object (red)
		out.write(".cfdump_table_object { cell-spacing: 2; background-color: #990000 }");
		out.write(".cfdump_th_object { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: white; text-align: left; padding: 5px; background-color: #cc3300 }");
		out.write(".cfdump_td_object { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: black; text-align: left; padding: 3px; background-color: #ffcccc; vertical-align: top }");

		// query (purple)
		out.write(".cfdump_table_query { cell-spacing: 2; background-color: #990066 }");
		out.write(".cfdump_th_query { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: white; text-align: left; padding: 5px; background-color: #993399 }");
		out.write(".cfdump_td_query { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: black; text-align: left; padding: 3px; background-color: #ffccff; vertical-align: top }");

		// xml (gray)
		out.write(".cfdump_table_xml { cell-spacing: 2; background-color: #666666 }");
		out.write(".cfdump_th_xml { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: white; text-align: left; padding: 5px; background-color: #999999 }");
		out.write(".cfdump_td_xml { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: black; text-align: left; padding: 3px; background-color: #dddddd; vertical-align: top }");

		// udf (brown)
		out.write(".cfdump_table_udf { cell-spacing: 2; background-color: #660033 }");
		out.write(".cfdump_th_udf { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: white; text-align: left; padding: 5px; background-color: #996633 }");
		out.write(".cfdump_td_udf { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; font-style: italic; color: black; text-align: left; padding: 3px; background-color: #ffffff; vertical-align: top }");
		out.write(".cfdump_table_udf_args { cell-spacing: 2; background-color: #996600 }");
		out.write(".cfdump_th_udf_args { font-size: " + fontSize + "; font-family: verdana, arial, helvetica, sans-serif; color: white; text-align: left; padding: 5px; background-color: #cc9900 }");
		out.write("</style>");

	}
}
