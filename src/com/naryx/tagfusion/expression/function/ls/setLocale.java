/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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

package com.naryx.tagfusion.expression.function.ls;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class setLocale extends functionBase {

	private static final long serialVersionUID = 1L;

	static Map<String, String> supportedLocales;
	static Locale[] available = Locale.getAvailableLocales();

	static {
		supportedLocales = new FastMap<String, String>();

		for (int i = 0; i < available.length; i++) {
			String displayName = available[i].getDisplayName(Locale.US);
			if (displayName.indexOf(",") == -1) {
				supportedLocales.put(displayName.toLowerCase(), available[i].getLanguage() + "," + available[i].getCountry());
				supportedLocales.put((available[i].getLanguage() + (available[i].getCountry().length() > 0 ? "_" + available[i].getCountry() : "")).toLowerCase(), available[i].getLanguage() + "," + available[i].getCountry());
			}
		}

		// -[ put the CF5.0 suported strings in
		supportedLocales.put("dutch (belgian)", "nl,BE");
		supportedLocales.put("french (canadian)", "fr,CA");
		supportedLocales.put("norwegian (bokmal)", "no,NO");

		supportedLocales.put("dutch (standard)", "nl,NL");
		supportedLocales.put("french (standard)", "fr,FR");
		supportedLocales.put("norwegian (nynorsk)", "no,NO");

		supportedLocales.put("english (australian)", "en,AU");
		supportedLocales.put("french (swiss)", "fr,CH");
		supportedLocales.put("portuguese (brazilian)", "pt,BR");

		supportedLocales.put("english (canadian)", "en,CA");
		supportedLocales.put("german (austrian)", "de,AT");
		supportedLocales.put("portuguese (standard)", "pt,PT");

		supportedLocales.put("english (new zealand)", "en,NZ");
		supportedLocales.put("german (standard)", "de,DE");
		supportedLocales.put("spanish (mexican)", "es,MX");

		supportedLocales.put("english (uk)", "en,GB");
		supportedLocales.put("german (swiss)", "de,CH");
		supportedLocales.put("spanish (modern)", "es,ES");

		supportedLocales.put("english (us)", "en,US");
		supportedLocales.put("italian (standard)", "it,IT");
		supportedLocales.put("spanish (standard)", "es,ES");

		supportedLocales.put("french (belgian)", "fr,BE");
		supportedLocales.put("italian (swiss)", "it,CH");
		supportedLocales.put("swedish", "sv,SE");
	}

	public setLocale() {
		min = max = 1;
	}

	public String[] getParamInfo() {
		return new String[] { "locale" };
	}

	public java.util.Map getInfo() {
		return makeInfo("locale", "Sets the local for the current request, returning the previous one", ReturnType.STRING);
	}

	public cfData execute(cfSession _session, List<cfData> parameters) throws cfmRunTimeException {
		// --testing
		// for( int i=0; i<available.length; i++ )
		// _session.write("<p>" + available[i].getDisplayName()+" country=" +
		// available[i].getCountry()+" lang="+ available[i].getLanguage() +"<BR>");
		// ------

		Locale old_locale = _session.getLocale();
		String localeRaw = parameters.get(0).getString();

		Locale locale = getLocale(localeRaw);

		if (locale != null) {
			_session.setLocale(locale);
		} else
			throwException(_session, localeRaw + " must be a valid locale name");

		return new cfStringData(old_locale.getDisplayName(Locale.US));
	}

	public static Locale getLocale(String _locale) {
		String localeString = supportedLocales.get(_locale.toLowerCase());

		if (localeString != null) {
			String language = localeString.substring(0, localeString.indexOf(","));
			String country = "";

			if (!(localeString.charAt(localeString.length() - 1) == ',')) {
				country = localeString.substring(localeString.indexOf(",") + 1, localeString.length());
			}

			return new Locale(language, country);
		} else {
			return null;
		}

	}
}
