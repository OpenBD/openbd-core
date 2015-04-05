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

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.runTime;

public class cfTRACE extends cfLOG implements Serializable {
	static final long serialVersionUID = 1;

	private String category = "";
	private String text = "";
	private String varName = "";
	private cfData varData;
	private boolean abort = false;

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		initLogDir();

		defaultAttribute("ABORT", "No");
		defaultAttribute("INLINE", "No");
		defaultAttribute("TYPE", "Information");

		// the following attributes control the operation of cfLOG.render()
		defaultAttribute("LOG", "trace");
		defaultAttribute("APPLICATION", "No");
		defaultAttribute("THREAD", "No");

		parseTagHeader(_tag);
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		if (!_Session.isDebugEnabled()) {
			return cfTagReturnType.NORMAL;
		}

		abort = getDynamic(_Session, "ABORT").getBoolean();

		if (containsAttribute("TEXT")) {
			text = getDynamic(_Session, "TEXT").getString();
		}

		if (containsAttribute("CATEGORY")) {
			category = getDynamic(_Session, "CATEGORY").getString();
		}

		if (containsAttribute("VAR")) {
			varName = getDynamic(_Session, "VAR").getString();
			try {
				varData = runTime.runExpression(_Session, varName);
			} catch (cfmRunTimeException e) {
				varData = new cfStringData("(undefined)");
			}
		}

		super.render(_Session); // outputs to cftrace.log

		_Session.recordTracepoint(getText(true));

		boolean inline = getDynamic(_Session, "INLINE").getBoolean();
		if (inline) {
			_Session.write("<p><font color=\"#ff6600\"><b>[CFTRACE ");
			// current time
			_Session.write("] ");
			_Session.write(getText(false));
			_Session.write("</b></font>");

			// now dump the variable
			if (varName.length() > 0) {
				_Session.write("<table border=1 cellpadding=2 cellspacing=0>");
				_Session.write("<th bgcolor=\"#ff6600\"><font color=\"#ffffff\">");
				_Session.write(varName);
				_Session.write("</font></th><tr><td>");
				_Session.write(cfDUMP.dumpSession(_Session, varData)); // LONG dump of
																																// specified
																																// variable, no
																																// LABEL
				_Session.write("</td></tr></table>");
			}
		}

		if (abort) {
			_Session.abortPageProcessing();
		}

		return cfTagReturnType.NORMAL;
	}

	protected String getLogText(cfSession _Session) {
		return getText(true);
	}

	private String getText(boolean logText) {
		StringBuilder textBuff = new StringBuilder();
		textBuff.append("[" + getFile().getName() + " @ line: " + this.posLine + "] -");
		if (category.length() > 0) {
			textBuff.append(" [" + category + "]");
		}
		
		if (logText && (varName.length() > 0)) {
			textBuff.append(" [" + varName + " = " + varData.toString() + "]");
		}
		
		if (abort) {
			textBuff.append(" [ABORTED]");
		}
		if (text.length() > 0) {
			if (!logText) {
				textBuff.append("<i>");
			}
			textBuff.append(" " + text);
			if (!logText) {
				textBuff.append("</i>");
			}
		}
		return textBuff.toString();
	}
}