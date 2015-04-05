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
 *  $Id: ValidationInputSource.java 2506 2015-02-08 22:25:59Z alan $
 */

package com.naryx.tagfusion.cfm.xml.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.xml.sax.InputSource;

/**
 * InputSource subclass that manipulates the <!DOCTYPE ...> element depending on
 * the value of mode. If mode == ADD_MODIFY, then, this will replace the
 * existing <!DOCTYPE ...> element with the same <!DOCTYPE ...> element, but
 * with a new SYSTEM identifier, CUSTOM_DTD. If mode == REMOVE_MODIFY, any
 * existing doctype declaration will be updated so that it has no SYSTEM/PUBLIC
 * identifier. If mode == REMOVE, the <!DOCTYPE ...> element is simply removed
 * from the document stream and parsing proceeds without it. If mode ==
 * NO_CHANGE, no changes will be made.
 * 
 */
public class ValidationInputSource extends InputSource {
	/** Internal SYSTEM identifier for our modified DTD declarations */
	public static final String CUSTOM_DTD = DTDFilterReader.CUSTOM_DTD;


	/**
	 * Using mode == READ_ADD, the existing doctype will be read. If no doctype
	 * exists in the xml data, then one will be inserted that contains the
	 * CUSTOM_DTD SYSTEM identifier. Using mode == REMOVE_MODIFY will result in
	 * any existing doctype declaration being updated so that it has no
	 * SYSTEM/PUBLIC identifier. Using mode == REMOVE will result in the <!DOCTYPE
	 * ...> element simply being removed from the document stream. Using mode ==
	 * NO_CHANGE, no changes will be made.
	 */
	public static final byte READ_ADD = DTDFilterReader.READ_ADD;
	public static final byte REMOVE_MODIFY = DTDFilterReader.REMOVE_MODIFY;
	public static final byte REMOVE = DTDFilterReader.REMOVE;
	public static final byte NO_CHANGE = DTDFilterReader.NO_CHANGE;

	protected XmlSource xs = null;
	protected InputSource is = null;
	protected byte mode = READ_ADD;

	protected DTDFilterReader.DTDListener listener = null;

	/**
	 * Default constructor. Wraps an InputSource from the specified XmlSource. If
	 * mode == READ_ADD, then, this will read the existing <!DOCTYPE ...> element
	 * or add a <!DOCTYPE ...> element with a new SYSTEM identifier and
	 * CUSTOM_DTD. If mode == REMOVE_MODIFY, the any existing doctype declaration
	 * will be updated to have its SYSTEM/PUBLIC identifier removed. If mode ==
	 * NO_CHANGE, no changes will be made. If mode == REMOVE, the entire <!DOCTYPE
	 * ...> element is removed.
	 * 
	 * @param xs
	 *          XmlSource from which we can get an InputSource to wrap
	 * @param mode
	 *          either READ_ADD, REMOVE, REMOVE_MODIFY, or NO_CHANGE
	 * @throws IOException
	 */
	public ValidationInputSource(XmlSource xs, byte mode) throws IOException {
		this(xs, null, mode);
	}

	/**
	 * Default constructor. Wraps an InputSource from the specified XmlSource. If
	 * mode == READ_ADD, then, this will read the existing <!DOCTYPE ...> element
	 * or add a <!DOCTYPE ...> element with a new SYSTEM identifier and
	 * CUSTOM_DTD. If mode == REMOVE_MODIFY, the any existing doctype declaration
	 * will be updated to have its SYSTEM/PUBLIC identifier removed. If mode ==
	 * NO_CHANGE, no changes will be made. If mode == REMOVE, the entire <!DOCTYPE
	 * ...> element is removed.
	 * 
	 * @param xs
	 *          XmlSource from which we can get an InputSource to wrap
	 * @param listener
	 *          DTDFilterReader.DTDListener listener for the existing DTD info
	 * @param mode
	 *          either READ_ADD, REMOVE, REMOVE_MODIFY, or NO_CHANGE
	 * @throws IOException
	 */
	public ValidationInputSource(XmlSource xs, DTDFilterReader.DTDListener listener, byte mode) throws IOException {
		this.xs = xs;
		this.is = xs.newInputSource();
		this.mode = mode;
		this.listener = listener;
	}

	/**
	 * Returns the public identifier for the underlying InputSource.
	 * 
	 * @return public identifier for the underlying InputSource
	 */
	public String getPublicId() {
		return is.getPublicId();
	}

	/**
	 * Sets the public identifier for the underlying InputSource.
	 * 
	 * @param publicId
	 *          public identifier for the underlying InputSource
	 */
	public void setPublicId(String publicId) {
		this.is.setPublicId(publicId);
	}

	/**
	 * Returns the system identifier for the underlying InputSource.
	 * 
	 * @return system identifier for the underlying InputSource
	 */
	public String getSystemId() {
		return is.getSystemId();
	}

	/**
	 * Sets the system identifier for the underlying InputSource.
	 * 
	 * @param systemId
	 *          system identifier for the underlying InputSource
	 */
	public void setSystemId(String systemId) {
		is.setSystemId(systemId);
	}

	/**
	 * Returns the encoding for the underlying InputSource.
	 * 
	 * @return encoding for the underlying InputSource
	 */
	public String getEncoding() {
		return is.getEncoding();
	}

	/**
	 * Sets the encoding for the underyling InputSource.
	 * 
	 * @param encoding
	 *          encoding for the underlying InputSource
	 */
	public void setEncoding(String encoding) {
		is.setEncoding(encoding);
	}

	/**
	 * Returns a Reader for the character stream of the underlying InputSource,
	 * filtered by our DTDFilter.
	 * 
	 * @return filtered character stream
	 */
	public Reader getCharacterStream() {
		// Get our DTDFilterReader to do the handling
		Reader reader = is.getCharacterStream();
		
		if (reader == null) {
			// Try as an InputStream
			InputStream input = null;
			String enc = null;
			try {
				enc = is.getEncoding();
				input = is.getByteStream();
				
				if (enc == null) {
					enc 	= XmlSource.getStreamEncoding(input);
					is 		= xs.newInputSource();
					input = is.getByteStream();
				}
				
				reader = new BufferedReader(new InputStreamReader(input, enc));
				
			} catch (UnsupportedEncodingException ex) {
				throw new UnsupportedOperationException("Input data encoded in unsupported encoding. " + ex.getMessage());
			} catch (IOException ex) {
				throw new UnsupportedOperationException("Could not read input data. " + ex.getMessage());
			}
		}

		// Get our DTDFilterReader to do the handling
		DTDFilterReader rtn = new DTDFilterReader(reader, mode);
		if (this.listener != null)
			rtn.setListener(this.listener);
		
		return rtn;
	}

	/**
	 * Sets the character stream for our underlying InputSource. This operation is
	 * currently not supported and will throw an UnsupportedOperationException
	 * when called.
	 * 
	 * @param characterStream
	 *          replacement Reader for the underlying InputSource
	 */
	public void setCharacterStream(Reader characterStream) {
		throw new UnsupportedOperationException("Cannot set the character stream.");
	}

	/**
	 * Returns an InputStream for the byte stream of the underlying InputSource,
	 * filtered by our DTDFilter.
	 * 
	 * @return filtered byte stream
	 */
	public InputStream getByteStream() {
		return null;
	}

	/**
	 * Sets the byte stream for our underlying InputSource. This operation is
	 * currently not supported and will throw an UnsupportedOperationException
	 * when called.
	 * 
	 * @param byteStream
	 *          replacement InputStream for the underlying InputSource
	 */
	public void setByteStream(InputStream byteStream) {
		throw new UnsupportedOperationException("Cannot set the byte stream.");
	}
}
