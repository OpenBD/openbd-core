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
 *  $Id: cfIF.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.parser.runTime;

/**
 * This tag controls the CFIF tag. This is a little special from other tags as it has controlling inner 
 * tags. For example CFELSE does not have an ending tag; its block is ended by either the end of the tag, 
 * or another CFELSE or CFELSEIF. The inner render has been flattened out to take account of this.
 */
public class cfIF extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	protected CFExpression tagExpression;
	protected String exprString;

	@SuppressWarnings("unchecked")
	public java.util.Map getInfo() {
		return createInfo("control", "<CFIF> controls whether the body of the tag is implemented based on a conditional expression.");
	}

	protected void parseExpression(String _tagName, String _tag) throws cfmBadFileException {
		int c1 = _tag.toLowerCase().indexOf(_tagName.toLowerCase());

		if (c1 != -1) {
			exprString = _tag.substring(c1 + _tagName.length(), _tag.length() - 1).trim();
			tagExpression = CFExpression.getCFExpression(exprString);
		}
	}

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		parseExpression("cfif", _tag);
	}

	public String getEndMarker() {
		return "</CFIF>";
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		// Run through the tags finding the next tag
		int t = 0, e = 0, s = 0;
		boolean bFoundTag, bUsingInnerTag = false;
		cfTag innerTag;

		// Check this particular tag first of all
		bFoundTag = evaluateExpression(_Session);
		boolean foundCFELSE = false;

		// check there are at most 1 <CFELSE> tag
		for (int x = 0; x < controlList.length; x++) {
			if (controlList[x] == TAG_MARKER) {
				innerTag = childTagList[t++];
				if (innerTag instanceof cfELSE) {
					if (foundCFELSE) {
						throw newRunTimeException("Too many <CFELSE> tags. You may only use one <CFELSE> tag per <CFIF> statement.");
					}
					foundCFELSE = true;
				}
			}
		}

		t = 0; // reset before next iteration

		for (int x = 0; x < controlList.length; x++) {
			if (controlList[x] == CHR_MARKER) {
				if (bFoundTag) {
					_Session.write(tagBody[s]);
				}
				s++;
			} else if (_Session.isStopped()) {
				_Session.abortPageProcessing();
			} else if (controlList[x] == TAG_MARKER) {
				innerTag = childTagList[t++];
				if ((innerTag instanceof cfELSEIF || innerTag instanceof cfELSE)) {
					if (!bFoundTag && ((cfIF) innerTag).evaluateExpression(_Session)) {
						// we have found the inner tag we are looking for so lets render uptil the
						// next one. This is controlled via the bFoundTag boolean
						_Session.pushTag(innerTag);
						bFoundTag = true;
						bUsingInnerTag = true;
					} else if (bFoundTag) {
						// we don't render anymore, we've found the next set of CFELSE / CFIF tag
						bFoundTag = false;
						if (bUsingInnerTag) {
							_Session.popTag();
						}
						bUsingInnerTag = false;
						break;
					}
				} else if (bFoundTag) {
					_Session.pushTag(innerTag);
					cfTagReturnType rt = innerTag.render(_Session);
					_Session.popTag();
					if (!rt.isNormal()) {
						if (bUsingInnerTag) {
							_Session.popTag();
						}
						return rt;
					}
				}
			} else if (controlList[x] == EXP_MARKER) {
				// we only want to render the expression, if have are processing the right tag
				if (bFoundTag) {
					renderExpression(_Session, expressionList[e]);
				}
				e++;
			}
		}

		if (bUsingInnerTag) {
			_Session.popTag();
		}

		return cfTagReturnType.NORMAL;
	}

	protected boolean evaluateExpression(cfSession _Session) throws cfmRunTimeException {
		return runTime.runExpression(_Session, tagExpression).getBoolean();
	}
}
