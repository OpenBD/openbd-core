/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  $Id: cfTRY.java 2330 2013-02-21 12:04:09Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfCatchClause;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmAbortException;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.parser.runTime;
import com.naryx.tagfusion.cfm.parser.script.ExceptionVarHandler;
import com.naryx.tagfusion.cfm.parser.script.userDefinedFunction;

public class cfTRY extends cfTag implements Serializable {
	static final long serialVersionUID = 1;

	public static String CFCATCHVAR = "cfcatch";

	private transient List<cfCatchClause> cfcatchList;
	private transient cfTag finallyClause = null;

	public java.util.Map getInfo() {
		return createInfo("control", "Defines a block to which an error may occur and to be caught in a controlled manner");
	}

	public String getEndMarker() {
		return "</CFTRY>";
	}

	public void tagLoadingComplete() throws cfmBadFileException {
		buildCfcatchList();
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {

		// Note exceptionCatcher has to be created here as it takes note of file/tag stacks etc
		exceptionCatcher exHandler = new exceptionCatcher(this, cfcatchList);

		// Take a note of the active file before we run anything in this block
		cfFile activeFile = _Session.activeFile();
		userDefinedFunction activeUDF = _Session.peekUserDefinedFunction();

		try {
			cfTag innerTag;
			cfData token;
			int t = 0, s = 0, e = 0;

			for (int x = 0; x < controlList.length; x++) {
				if (controlList[x] == CHR_MARKER) {
					_Session.write(tagBody[s++]);
				} else if (_Session.isStopped()) {
					_Session.abortPageProcessing();
				} else if (controlList[x] == TAG_MARKER) {
					innerTag = childTagList[t++];
					if (innerTag instanceof cfCATCH || innerTag instanceof cfFINALLY) {
						break;
					}

					_Session.pushTag(innerTag);
					cfTagReturnType rt = innerTag.render(_Session);
					_Session.popTag();
					if (!rt.isNormal()) {
						return rt;
					}
				} else if (controlList[x] == EXP_MARKER) {
					token = runTime.runExpression(_Session, expressionList[e++]);
					if (token != null) {
						_Session.write(token.getString());
					}
				}
			}
			return cfTagReturnType.NORMAL;

		} catch (cfmAbortException ACF) {
			throw ACF; // catch and rethrow so it's not caught as RuntimeException below
		} catch (cfmRunTimeException RTE) {
			RTE.getCatchData().setSession(_Session);
			return handleException(_Session, exHandler, activeFile, activeUDF, RTE);
		} catch (RuntimeException e) {
			return handleException(_Session, exHandler, activeFile, activeUDF, new cfmRunTimeException(_Session, e));
		} finally {

			if (finallyClause != null) {
				renderFinally(_Session);
			}

		}
	}

	
	
	/**
	 * Renders the CFFINALLY block for this particular tag
	 * 
	 * @param _Session
	 * @throws cfmRunTimeException
	 */
	private void renderFinally(cfSession _Session) throws cfmRunTimeException {
		_Session.pushTag(finallyClause);
		finallyClause.render(_Session);
		_Session.popTag();
	}

	
	
	private static cfTagReturnType handleException(cfSession _Session, exceptionCatcher _exHandler, cfFile _activeFile, userDefinedFunction activeUDF, cfmRunTimeException _rte) throws cfmRunTimeException {
		cfCatchClause clause = _exHandler.processRuntimeException(_Session, _activeFile, activeUDF, _rte);
		cfCATCH parent = clause.getParentTag();

		// we may be within a cfcatch block so a cfcatch data already exists
		ExceptionVarHandler evh = new ExceptionVarHandler(_Session, CFCATCHVAR);
		try {
			evh.setExceptionVariable(_rte.getCatchData());
			_Session.pushTag(parent);
			cfTagReturnType rt = parent.render(_Session);
			_Session.popTag();
			evh.deleteExceptionVariable();
			return rt;
		} catch (cfmRunTimeException rte) {
			evh.deleteExceptionVariable();
			if (rte.isRethrow()) { // rethrowing same exception
				_exHandler.restoreSessionSnapshot(_Session);
			}
			throw rte;
		}
	}

	
	
	/**
	 * create a list of CFCATCH tags, put TYPE="Any" last
	 * @throws cfmBadFileException
	 */
	private void buildCfcatchList() throws cfmBadFileException {
		cfcatchList = new ArrayList<cfCatchClause>();
		cfCATCH cfcatchAny = null;

		for (int i = 0; i < childTagList.length; i++) {
			cfTag childTag = childTagList[i];

			if (childTag instanceof cfCATCH) {
				if (((cfCATCH) childTag).isCatchAny()) {
					if (cfcatchAny == null) {
						cfcatchAny = (cfCATCH) childTag;
					} else {
						cfcatchList = null;
						cfCatchData catchData = new cfCatchData();
						catchData.setMessage("Cannot have multiple CFCATCH tags with attribute TYPE=\"Any\"");
						throw new cfmBadFileException(catchData);
					}
				} else {
					cfcatchList.add(new cfCatchClause((cfCATCH) childTag));
				}
			} else if (childTag instanceof cfFINALLY) {

				if (finallyClause != null) {
					cfCatchData catchData = new cfCatchData();
					catchData.setMessage("Cannot have multiple CFFINALLY tags within a CFTRY");
					throw new cfmBadFileException(catchData);
				} else
					finallyClause = childTag;

			}
		}

		if (cfcatchAny != null) {
			cfcatchList.add(new cfCatchClause(cfcatchAny));
		}

		if (cfcatchList.size() == 0 && finallyClause == null) {
			cfcatchList = null;
			cfCatchData catchData = new cfCatchData();
			catchData.setMessage("There must be at least one CFCATCH OR a CFFINALLY within CFTRY");
			throw new cfmBadFileException(catchData);
		}
	}

}
