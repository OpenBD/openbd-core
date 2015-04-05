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

package com.nary.util;

import java.util.Map;

/**
 * This class contains utility methods to help with localization.
 */
public class Localization {
	private static Map<String, String> noneNumberFormatPatterns = new java.util.HashMap<String, String>();

	static {
		/** ******* init the noneNumberFormatPatterns HashMap ************* */
		noneNumberFormatPatterns.put("en_US", "#,##0.00;(#,##0.00)");
		noneNumberFormatPatterns.put("es_MX", "#,##0.00;(#,##0.00)");
		noneNumberFormatPatterns.put("fr_CA", "#,##0.00;(#,##0.00)");
		noneNumberFormatPatterns.put("nl_NL", "#,##0.00;#,##0.00-");
	}

	public Localization() {
	}

	/**
	 * convertCharSetToCharEncoding
	 * 
	 * Converts a character set value to a java character encoding value.
	 * Character set values are used in HTTP headers while java character encoding
	 * values are used internally when calling Java API methods that require a
	 * character encoding value. Note that the latest JVMs support being passed
	 * charset values so the Java version of BlueDragon doesn't need to perform
	 * this conversion.
	 */
	public static String convertCharSetToCharEncoding(String cs) {
		if (cs.equalsIgnoreCase("utf-16") || cs.equalsIgnoreCase("utf16")) {
			cs = cs + "LE";
		}

		// utf8 is not recognized by JDK 1.3 so convert it to utf-8 which is
		// recognized by JDK 1.3, 1.4 and 1.5
		// NOTE: this check is needed so regression tests 2037 and 2038 will pass
		// with JDK 1.3.
		if (cs.equalsIgnoreCase("utf8")) {
			cs = "utf-8";
		}

		return cs;
	}

	/*
	 * updateNoneCurrencyFormat
	 * 
	 * This method is called by LSCurrencyFormat when the type is "none".
	 */
	public static void updateNoneCurrencyFormat(java.util.Locale l, java.text.NumberFormat f) {
		// For certain locales, BD/Java doesn't use the same pattern as java.
		String pattern = noneNumberFormatPatterns.get(l.toString());
		if (pattern != null) {
			java.text.DecimalFormat decFormat = (java.text.DecimalFormat) f;
			decFormat.applyPattern(pattern);
		}
	}
}
