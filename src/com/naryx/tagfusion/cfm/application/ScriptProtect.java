/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
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
 */

package com.naryx.tagfusion.cfm.application;

import java.util.regex.Pattern;

import com.naryx.tagfusion.cfm.cookie.cfCookieData;
import com.naryx.tagfusion.cfm.engine.cfCGIData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfFormData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfUrlData;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.xmlConfig.xmlCFML;


public class ScriptProtect {

	private static Pattern p;
	
	public static void init(xmlCFML config){
		String SCRIPT_REGEX = config.getString( "server.system.scriptprotectregex", "<(\\s*)(object|embed|script|applet|meta)" );
		cfEngine.log( "cfEngine: [server.system.scriptprotectregex]=" + SCRIPT_REGEX );

		p = Pattern.compile(SCRIPT_REGEX, Pattern.CASE_INSENSITIVE );
	}
	
	public static String sanitize( String value ) {
		if ( value == null || value.isEmpty() )
			return value;
		
    return p.matcher(value).replaceAll("<$1InvalidTag");
	}

	
	/**
	 * Called to protect a given scope
	 * 
	 * @param _Session
	 * @param _scope
	 */
	public static void applyScriptProtection(cfSession _Session, int _scope) {		
		cfData scopeData = _Session.getQualifiedData(_scope);
		
		if ( scopeData != null && _scope == variableStore.CGI_SCOPE )
			((cfCGIData) scopeData).setScriptProtect();
		else if (scopeData != null && scopeData.getDataType() == cfData.CFSTRUCTDATA) {
			cfStructData data = (cfStructData) scopeData;
			Object[] keys = data.keys();
			for (int i = 0; i < keys.length; i++) {
				String nextKey = keys[i].toString();
				cfData valueData = data.getData(nextKey);

				if (valueData.getDataType() == cfData.CFSTRINGDATA) {
					String value = ((cfStringData) valueData).getString();
					int origLen = value.length();
					value = sanitize( value );

					// only replace the existing cfData if it's changed - note this works because any replaced string will grow the existing string length
					if (value.length() != origLen) {
						if (_scope == variableStore.COOKIE_SCOPE) {
							((cfCookieData) scopeData).overrideData(nextKey, value);
						} else if (_scope == variableStore.FORM_SCOPE) {
							((cfFormData) scopeData).overrideData(nextKey, new cfStringData(value));
						} else if (_scope == variableStore.URL_SCOPE) {
							((cfUrlData) scopeData).overrideData(nextKey, new cfStringData(value));
						}
					}
				}
			}
		}
	}
	
}
