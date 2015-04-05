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

package com.naryx.tagfusion.cfm.sql;

import java.io.Serializable;

import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfTRANSACTION extends cfTag implements Serializable {

	static final long serialVersionUID = 1;

	public static final String DATA_BIN_KEY = "CFTRANSACTION_DATA";

	public static final String ACTION_BEGIN = "BEGIN", ACTION_COMMIT = "COMMIT", ACTION_ROLLBACK = "ROLLBACK";

	private String endMarker = null;

	String actionType;

  public java.util.Map<String,String> getInfo(){
  	return createInfo(
  			"database", 
  			"Use to group multiple queries as a single transaction. Support for rollback and committing of the transaction");
  }
  
  @SuppressWarnings("rawtypes")
	public java.util.Map[] getAttInfo(){
  	return new java.util.Map[] {
  		createAttInfo("ACTION", 	 "The action - BEGIN, COMMIT and ROLLBACK are supported", "", true ),
  		createAttInfo("ISOLATION", "The isolation level of the transaction.", "", false ),
  	};
  }

	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("ACTION", ACTION_BEGIN);
		parseTagHeader(_tag);

		actionType = getConstant("ACTION").toUpperCase();
		if (!actionType.equals(ACTION_BEGIN) && !actionType.equals(ACTION_COMMIT) && !actionType.equals(ACTION_ROLLBACK))
			throw newBadFileException("Missing VALUE", "Invalid type for ACTION.  BEGIN/COMMIT/ROLLBACK only allowed.");
		if (actionType.equals(ACTION_BEGIN)) {
			endMarker = "</CFTRANSACTION>";
		}
	}

	public String getEndMarker() {
		return endMarker;
	}

	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {

		cfTransactionCache tCache = (cfTransactionCache) _Session.getDataBin(DATA_BIN_KEY);

		if (actionType.equals(ACTION_BEGIN)) {

			if (tCache != null)
				throw newRunTimeException("You may not embed a CFTRANSACTION within another CFTRANSACTION");

			if (containsAttribute("ISOLATION")) {
				String isolationLevel = getConstant("ISOLATION").toLowerCase();

				if (isolationLevel.equals("read_uncommitted"))
					tCache = new cfTransactionCache(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
				else if (isolationLevel.equals("read_committed"))
					tCache = new cfTransactionCache(java.sql.Connection.TRANSACTION_READ_COMMITTED);
				else if (isolationLevel.equals("repeatable_read"))
					tCache = new cfTransactionCache(java.sql.Connection.TRANSACTION_REPEATABLE_READ);
				else if (isolationLevel.equals("serializable"))
					tCache = new cfTransactionCache(java.sql.Connection.TRANSACTION_SERIALIZABLE);
				else
					throw newRunTimeException("Invalid ISOLATION level: " + isolationLevel);
			} else {
				tCache = new cfTransactionCache();
			}

			_Session.setDataBin(DATA_BIN_KEY, tCache); // Insert the Transaction cache in the session

			try {
				cfTagReturnType returnVal = renderToString(_Session, cfTag.HONOR_CF_SETTING);

				_Session.write(returnVal.getOutput()); // Render the contents of the CFTRANSACTION tag
				tCache.commit();
				return returnVal;

			} catch (cfmRunTimeException e) {
				tCache.rollback();
				throw e;
			} finally {
				_Session.deleteDataBin(DATA_BIN_KEY); // Remove the transaction cache
			}

		} else if (actionType.equals(ACTION_COMMIT)) {

			if (tCache != null)
				tCache.commit();

		} else if (actionType.equals(ACTION_ROLLBACK)) {

			if (tCache != null)
				tCache.rollback();

		}

		return cfTagReturnType.NORMAL;
	}

}
