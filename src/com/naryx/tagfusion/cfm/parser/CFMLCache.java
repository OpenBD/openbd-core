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

import com.nary.cache.MapTimedCache;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class CFMLCache {

	private static CFMLCacheImpl expCache = new CFMLCacheImpl(300);

	private CFMLCache() {
	}

	public static CFExpression getExpression(String _infix) throws cfmRunTimeException {
		CFExpression exp = (CFExpression) expCache.getFromCache(_infix);
		// -- If the expression was found, then return
		if (exp != null) {
			return exp;
		}

		try {
			// Get a lock just for this expression
			synchronized (expCache.getLock(_infix)) {
				// check again in case another thread added it
				exp = (CFExpression) expCache.getFromCache(_infix);
				if (exp != null) {
					return exp;
				}
				exp = CFExpression.getCFExpression(_infix);
				expCache.setInCache(_infix, exp);
				return exp;
			}
		} finally {
			// Be sure to remove the lock for this expression
			expCache.removeLock(_infix);
		}
	}

	public static class CFMLCacheImpl extends MapTimedCache {
		public CFMLCacheImpl(int timeOutSeconds) {
			super(timeOutSeconds);
		}
	}
}
