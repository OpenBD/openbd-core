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
 *  $Id: XmlSource.java 2506 2015-02-08 22:25:59Z alan $
 */

package com.naryx.tagfusion.cfm.xml.parse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import org.xml.sax.InputSource;

/**
 * Factory to create InputSource objects from an underlying data stream. Supports File instances, URL instances, or String instances (that represent the actual content itself).
 * 
 * @author Matt Jacobsen
 * 
 */
public class XmlSource {
	private final static String DEFAULT_ENCODING = "UTF-8";

	private Object source = null;

	/**
	 * Default constructor.
	 * 
	 * @param source
	 *          underlying data source
	 */
	public XmlSource(Object source) {
		this.source = source;
	}

	/**
	 * Returns a new InputSource to parse.
	 * 
	 * @return new InputSource to parse
	 * @throws IOException
	 */
	public InputSource newInputSource() throws IOException {
		if (source instanceof File) {
			return new InputSource(new BufferedInputStream(new FileInputStream((File) source)));
		} else if (source instanceof URL) {
			try {
				return new InputSource(((URL) source).openStream());
			} catch (IllegalArgumentException ex) {
				com.nary.Debug.printStackTrace(ex);
				throw new IOException(ex.getMessage());
			}
		} else if (source instanceof String) {
			return new InputSource(new StringReader((String) source));
		} else {
			throw new IOException("Unexpected data stream type: " + ((source == null) ? "null" : source.getClass().getName()));
		}
	}

	/**
	 * Returns the encoding for the xml data contained in the InputStream, if specified. Otherwise defaults to UTF-8.
	 * 
	 * @param stream
	 *          InputStream to read from
	 * @return encoding specified in the xml declaration or UTF-8.
	 * @throws IOException
	 */
	public static String getStreamEncoding(InputStream stream) throws IOException {
		String str = null;

		// Get the prolog from the stream
		String prolog = readXmlProlog(stream);
		if (prolog != null)
			str = readEncoding(prolog);

		// If specified, use it. Otherwise go with the default.
		if (str == null || str.trim().equals(""))
			str = DEFAULT_ENCODING;

		return str;
	}

	/**
	 * Returns the value of the "encoding" attribute in the <?xml ...?> prolog if it exists in the specified String. Otherwise returns null.
	 * 
	 * @param str
	 *          String containing xml data
	 * @return value of the "encoding" attribute or null
	 */
	protected static String readEncoding(String str) {
		int sndx = str.indexOf("<?xml");
		if (sndx != -1) {
			int endx = str.indexOf("?>", sndx);
			if (endx != -1) {
				int encndx = str.indexOf("encoding", sndx);
				if (encndx != -1 && encndx < endx) {
					sndx = encndx + 8;
					sndx = str.indexOf('=', sndx);
					if (sndx != -1 && sndx < endx) {
						// May be single or double quoted according to the spec
						for (int i = sndx + 1; i < endx; i++) {
							if (str.charAt(i) == '\'') {
								// Single quoted, return everything inside the quotes
								int ndx = str.indexOf('\'', i + 1);
								if (ndx != -1 && ndx < endx)
									return str.substring(i + 1, ndx);
								else
									break; // no closing quote!
							} else if (str.charAt(i) == '"') {
								// Double quoted, return everything inside the quotes
								int ndx = str.indexOf('"', i + 1);
								if (ndx != -1 && ndx < endx)
									return str.substring(i + 1, ndx);
								else
									break; // no closing quote!
							}
						}
					}
				}
			}
		}

		// Didn't find it
		return null;
	}

	/**
	 * Reads and returns the <?xml ...?> declaration from the prolog in the xml data contained in the specified InputStream. If no <?xml ...?> declaration is found returns null.
	 * 
	 * @param stream
	 *          InputStream containing xml data
	 * @return the <?xml ...?> declaration or null
	 * @throws IOException
	 */
	protected static String readXmlProlog(InputStream stream) throws IOException {
		char[] buf = new char[64];
		String prev = "";
		int read = -1;

		CommentFilterReader reader = null; 
		try{
			reader = new CommentFilterReader(new InputStreamReader(stream));
			
			while ((read = reader.read(buf, 0, buf.length)) != -1) {
				String str = new String(buf, 0, read);
				String combined = prev + str;
				int ndx = -1;
				if ((ndx = combined.indexOf('<')) != -1) {
					// Need to read until we can at least check that this might be the prolog
					if (ndx + 5 < combined.length()) {
						if ((ndx = combined.indexOf("<?xml", ndx)) != -1) {
							// Read to the end of the prolog
							int endx = -1;
							if ((endx = combined.indexOf('>', ndx)) != -1) {
								// Have a full prolog, return it
								return combined.substring(ndx, endx + 1);
							}
						} else {
							// Not the prolog, so it must not have one
							break;
						}
					}
				}
				prev = str;
			}

		}finally{
			reader.close();
		}
		
		// No prolog found
		return null;
	}

	/**
	 * Returns true if the source has a <!DOCTYPE ...> declaration, false otherwise.
	 * 
	 * @return true if the source has a <!DOCTYPE ...> declaration, false otherwise
	 * @throws IOException
	 */
	public boolean hasDTD() throws IOException {
		if (source instanceof File) {
			FileInputStream fin = null;
			BufferedInputStream bin = null;
			try {
				fin = new FileInputStream((File) source);
				bin = new BufferedInputStream(fin);
				return streamHasDocType(bin);
			} finally {
				if (bin != null)
					bin.close();
				if (fin != null)
					fin.close();
			}
		} else if (source instanceof URL) {
			InputStream in = null;
			try {
				in = ((URL) source).openStream();
				return streamHasDocType(in);
			} catch (IllegalArgumentException ex) {
				com.nary.Debug.printStackTrace(ex);
				throw new IOException(ex.getMessage());
			} finally {
				if (in != null)
					in.close();
			}
		} else if (source instanceof String) {
			return ((String) source).indexOf("<!DOCTYPE") != -1;
		} else {
			throw new IOException("Unexpected data stream type: " + ((source == null) ? "null" : source.getClass().getName()));
		}
	}

	/**
	 * Reads a small window buffer through the source and looks for the <!DOCTYPE ...> element. Returns true if found, false otherwise.
	 * 
	 * @param stream
	 *          InputStream to search
	 * @return true if the content contains a <!DOCTYPE ...> element, false otherwise
	 * @throws IOException
	 */
	protected boolean streamHasDocType(InputStream stream) throws IOException {
		char[] buf = new char[9];
		String prev = "";
		int read = -1;
		CommentFilterReader reader = null;
		try {
			reader = new CommentFilterReader(new InputStreamReader(stream));
			while ((read = reader.read(buf, 0, buf.length)) != -1) {
				String str = new String(buf, 0, read);
				String combined = prev + str;
				int ndx = -1;
				if ((ndx = combined.indexOf("<!DOCTYPE")) != -1) {
					return true;
				} else if ((ndx = combined.indexOf('<')) != -1) {
					for (int i = ndx + 1; i < combined.length(); i++) {
						if (!Character.isWhitespace(combined.charAt(i))) {
							if (combined.charAt(i) == '?' || combined.charAt(i) == '!')
								break;
							else
								return false;
						}
					}
				}
				prev = str;
			}
		} finally {
			if (reader != null)
				reader.close();
		}
		return false;
	}
}
