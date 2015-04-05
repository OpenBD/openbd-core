/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: ValidationErrorHandler.java 2506 2015-02-08 22:25:59Z alan $
 */

package com.naryx.tagfusion.cfm.xml.parse;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.naryx.tagfusion.cfm.engine.cfStructData;

/**
 * Simple ErrorHandler that ignores warning messages and throws error and fatal error messages when they arise. However, if constructed with a struct to capture all the messages, then no warning, error, or fatal error messages will be thrown. All messages will be added to the struct instead.
 * 
 * @author Matt Jacobsen
 *
 */
public class ValidationErrorHandler extends ValidationHandlerBase implements ErrorHandler {
	/**
	 * Default constructor.
	 *
	 */
	public ValidationErrorHandler() {
		super();
	}

	/**
	 * Alternate constructor. Takes a cfStructData instance that will collect the warning, error, and fatal error messages instead of throwing them.
	 *
	 * @param msgStruct
	 *          cfStructData to add all parse/validation messages to (instead of throwing them)
	 */
	public ValidationErrorHandler(cfStructData msgStruct) {
		super(msgStruct);
	}

	/**
	 * Handle warning Exceptions.
	 * 
	 * @param exception
	 *          warning to handle
	 * @throws SAXException
	 */
	public void warning(SAXParseException exception) throws SAXException {
		if (!recordWarning(exception.getMessage(), exception.getLineNumber(), exception.getColumnNumber())) {
			// Do nothing on warnings
		}
	}

	/**
	 * Handle error Exceptions.
	 * 
	 * @param exception
	 *          error to handle
	 * @throws SAXException
	 */
	public void error(SAXParseException exception) throws SAXException {
		if (!recordError(exception.getMessage(), exception.getLineNumber(), exception.getColumnNumber())) {
			throw exception;
		}
	}

	/**
	 * Handle fatal error Exceptions.
	 * 
	 * @param exception
	 *          fatal error to handle
	 * @throws SAXException
	 */
	public void fatalError(SAXParseException exception) throws SAXException {
		if (!recordFatalError(exception.getMessage(), exception.getLineNumber(), exception.getColumnNumber())) {
			throw exception;
		}
	}
}
