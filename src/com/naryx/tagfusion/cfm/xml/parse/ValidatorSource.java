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
 *  $Id: ValidatorSource.java 2506 2015-02-08 22:25:59Z alan $
 */

package com.naryx.tagfusion.cfm.xml.parse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Wrapper for handling validation sources of type File, URL, and String (where the String represents the actual 
 * validation content itself). The validation mechanisms supported are xml DTD and xml schema. Additionally, this 
 * may represent an empty String (which indicates validation should be determined by the xml document being parsed).
 * 
 * @author Matt Jacobsen
 *
 */
public class ValidatorSource {
	private Object source = null;

	/**
	 * Default constructor.
	 * 
	 * @param source
	 *          underlying validation data source
	 */
	public ValidatorSource(Object source) {
		this.source = source;
	}

	/**
	 * Returns true if the validation source is null, false otherwise.
	 * 
	 * @return true if the validation source is null, false otherwise
	 */
	public boolean isNull() {
		return source == null;
	}

	/**
	 * Returns true if the validation source is an empty string, false otherwise.
	 * 
	 * @return true if the validation source is an empty string, false otherwise
	 */
	public boolean isEmptyString() {
		return !isNull() && (source instanceof String) && ((String) source).trim().equals("");
	}

	/**
	 * Returns true if the underlying validation source is an xml schema document, false otherwise.
	 * 
	 * @return true if the underlying validation source is an xml schema document, false otherwise
	 * @throws IOException
	 */
	public boolean isSchema() throws IOException {
		if (!isNull() && !isEmptyString()) {
			if (source instanceof File) {
				File file = (File) source;
				if (file.getName().toLowerCase().endsWith(".xsd"))
					return true;
				else if (file.getName().toLowerCase().endsWith(".dtd"))
					return false;
				else
					return isXml(new InputSource(new BufferedInputStream(new FileInputStream(file))));
			} else if (source instanceof URL) {
				try {
					URL url = (URL) source;
					if (url.getFile().toLowerCase().endsWith(".xsd"))
						return true;
					else if (url.getFile().toLowerCase().endsWith(".dtd"))
						return false;
					else
						return isXml(new InputSource(url.openStream()));
				} catch (IllegalArgumentException ex) {
					com.nary.Debug.printStackTrace(ex);
					throw new IOException(ex.getMessage());
				}
			} else if (source instanceof String) {
				return isXml(new InputSource(new StringReader((String) source)));
			} else {
				throw new IOException("Unexpected data stream type: " + source.getClass().getName());
			}
		}
		return false;
	}

	/**
	 * Returns the validation source as a Source instance to be used with a Validator.
	 * 
	 * @return validation source as a Source instance to be used with a Validator
	 * @throws IOException
	 */
	public Source getAsSource() throws IOException {
		if (!isNull() && !isEmptyString()) {
			try {
				if (source instanceof File)
					return new StreamSource(new BufferedInputStream(new FileInputStream((File) source)));
				else if (source instanceof URL)
					return new StreamSource(((URL) source).openStream());
				else if (source instanceof String)
					return new StreamSource(new StringReader((String) source));
				else
					throw new IOException("Unexpected data stream type: " + source.getClass().getName());
			} catch (IllegalArgumentException ex) {
				com.nary.Debug.printStackTrace(ex);
				throw new IOException(ex.getMessage());
			}
		}
		return null;
	}

	/**
	 * Returns the validation source as an InputSource instance.
	 * 
	 * @return validation source as an InputSource instance
	 * @throws IOException
	 */
	public InputSource getAsInputSource() throws IOException {
		if (!isNull() && !isEmptyString()) {
			InputSource rtn = null;
			if (source instanceof File) {
				rtn = new InputSource(new BufferedInputStream(new FileInputStream((File) source)));
				rtn.setSystemId(((File) source).getAbsolutePath());
				return rtn;
			} else if (source instanceof URL) {
				try {
					rtn = new InputSource(((URL) source).openStream());
					rtn.setSystemId(((URL) source).toExternalForm());
					return rtn;
				} catch (IllegalArgumentException ex) {
					com.nary.Debug.printStackTrace(ex);
					throw new IOException(ex.getMessage());
				}
			} else if (source instanceof String) {
				String s = (String) source;
				rtn = new InputSource(new StringReader(s));
				return rtn;
			} else {
				throw new IOException("Unexpected data stream type: " + source.getClass().getName());
			}
		}
		return null;
	}

	/**
	 * Returns true if the specified InputSource is valid xml (and therefore is presumed to be valid xml schema), false otherwise.
	 * 
	 * @param is
	 *          InputSource to examine
	 * @return true if the specified InputSource is valid xml, false otherwise
	 */
	protected boolean isXml(InputSource is) {
		try {
			SAXParserFactory fact = SAXParserFactory.newInstance();
			SAXParser parser = fact.newSAXParser();
			parser.parse(is, new DefaultHandler());
			return true;
		} catch (IOException ex) {
			return false;
		} catch (SAXException ex) {
			return false;
		} catch (ParserConfigurationException ex) {
			return false;
		}
	}
}
