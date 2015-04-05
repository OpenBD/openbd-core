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

package com.naryx.tagfusion.cfm.xml.parse;

import java.io.IOException;
import java.io.Reader;

/**
 * FilterReader that handles the <!DOCTYPE ...> element as the document is read.
 * according the the mode. If mode == READ_ADD, then, this will read the
 * existing <!DOCTYPE ...> element or add a <!DOCTYPE ...> element with a new
 * SYSTEM identifier, and CUSTOM_DTD. If mode == REMOVE, the <!DOCTYPE ...>
 * element is simply removed from the document stream and parsing proceeds
 * without it. If mode == NO_CHANGE, no changes wil be made.
 * 
 */
public class DTDFilterReader extends XmlFilterReader {
	/** Internal SYSTEM identifier for our modified DTD declarations */
	public static final String CUSTOM_DTD = "http://www.newatlanta.com/bluedragondtd";

	/**
	 * Using mode == READ_ADD, the existing doctype will be read. If no doctype
	 * exists in the xml data, then one will be inserted that contains the
	 * CUSTOM_DTD SYSTEM identifier. Using mode == REMOVE_MODIFY will result in
	 * any existing doctype declaration being updated so that it has no
	 * SYSTEM/PUBLIC identifier. Using mode == REMOVE will result in the <!DOCTYPE
	 * ...> element simply being removed from the document stream. Using mode ==
	 * NO_CHANGE, no changes will be made.
	 */
	public static final byte READ_ADD = 1;

	public static final byte REMOVE_MODIFY = 2;

	public static final byte REMOVE = 3;

	public static final byte NO_CHANGE = 4;

	private int state = 0;

	private int startPos = -1;

	private int startDTDPos = -1;

	private int endDTDPos = -1;

	private boolean more = true;

	protected byte mode = READ_ADD;

	protected StringBuilder inputBuffer = null;

	protected DTDListener listener = null;

	/**
	 * Default constructor. Takes the mode value. If mode == READ_ADD, then, this
	 * will read the existing <!DOCTYPE ...> element or add a <!DOCTYPE ...>
	 * element with a new SYSTEM identifier and CUSTOM_DTD. If mode ==
	 * REMOVE_MODIFY, any existing doctype declaration will be updated so that it
	 * has no SYSTEM/PUBLIC identifier. If mode == REMOVE, the <!DOCTYPE ...>
	 * element is simply removed from the document stream and parsing proceeds
	 * without it. If mode == NO_CHANGE, no changes will be made.
	 * 
	 * @param r
	 *          Reader to filter
	 * @param mode
	 *          either READ_ADD, REMOVE, REMOVE_MODIFY, or NO_CHANGE
	 */
	public DTDFilterReader(Reader r, byte mode) {
		super(r);
		this.mode = mode;
		this.state = 0;
		this.startPos = -1;
		this.startDTDPos = -1;
		this.endDTDPos = -1;
		this.more = true;
		this.inputBuffer = new StringBuilder();
	}

	/**
	 * Returns true if comment filtering should still continue, false otherwise.
	 * 
	 * @return true if comment filtering should still continue, false otherwise.
	 */
	protected boolean stillFiltering() {
		return (state < 16);
	}

	/**
	 * Inheritors must implement this method. It reads from the underlying Reader
	 * instance and fills the localBuffer. Note, implementations should not call
	 * any public methods in this class or infinite recursion will result. Returns
	 * true if reading from the underlying Reader is not limited. Returns false if
	 * the end of the data stream is reached during this read.
	 * 
	 * @param minCount
	 *          minimum number of characters that should be read for this call
	 * @return true if more data can be read, false otherwise
	 * @throws IOException
	 */
	protected boolean readUnderlying(int minCount) throws IOException {
		// Make sure we read at least minCount new data
		minCount += localBuffer.length();
		while (localBuffer.length() < minCount && more) {
			// Read the next bit of data into our input
			char[] chars = new char[512];
			int r = in.read(chars, 0, chars.length);
			if (r != -1) {
				// Process the input, look for <?, <!DOCTYPE, or other.
				inputBuffer.append(chars, 0, r);
				parseInput();
				more = true;
			} else {
				// Need to flush whatever's left in the input because we
				// won't be filtering after this call.
				localBuffer.append(inputBuffer);
				inputBuffer.setLength(0);
				more = false;
			}
		}

		return more;
	}

	/**
	 * Does the heavy work of parsing the read xml data and correctly keeping
	 * state so that the DTD can be filtered appropriately.
	 * 
	 * @throws IOException
	 */
	protected void parseInput() throws IOException {
		for (int pos = 0; pos < inputBuffer.length(); pos++) {
			char c = inputBuffer.charAt(pos);
			switch (state) {
			case 0: // Not in any tag
				if (c == '<') {
					// Opening of some tag. Don't add to the output just yet
					startPos = pos;
					state = 1;
				} else {
					// Add whatever it is (should be whitespace) to output
					localBuffer.append(c);
					inputBuffer.deleteCharAt(pos);
					pos = -1;
				}
				break;
			case 1: // In some (unknown tag)
				if (c == '?') {
					// Opening of either a PI or xml decl. OK, now we can add
					// the open bracket to the output, and this char too.
					localBuffer.append(inputBuffer.substring(startPos, pos + 1));
					inputBuffer.delete(startPos, pos + 1);
					pos = -1;
					state = 2;
				} else if (c == '!') {
					// Opening of either a comment or DTD
					state = 3;
				} else {
					// Must be opening of the document element (by elimination)
					state = 4;
				}
				break;
			case 2: // In either a PI, xml decl, or comment tag
				// Add whatever it is to output
				localBuffer.append(c);
				inputBuffer.deleteCharAt(pos);
				pos = -1;
				if (c == '>') {
					// Back to looking for DTD and document element
					state = 0;
				}
				break;
			case 3: // In either a comment or DTD
				if (c == 'D') {
					// Opening of the DTD (most likely).
					state = 5;
				} else {
					// Opening of a comment. OK, now we can add the open
					// bracket etc. to the output.
					localBuffer.append(inputBuffer.substring(startPos, pos + 1));
					inputBuffer.delete(startPos, pos + 1);
					pos = -1;
					state = 2;
				}
				break;
			case 4: // In the document element tag
				if (c == '>') {
					handleDTD(startPos, pos + 1);
					// Flush everything to output buffer
					localBuffer.append(inputBuffer.toString());
					inputBuffer.setLength(0);
					pos = -1;
					// Done filtering/scanning
					state = 16;
				}
				break;
			case 5: // In a DTD
				if (c == 'O') {
					// Opening of the DTD (most likely).
					state = 6;
				} else {
					// Opening of some other tag. OK, now we can add the open
					// bracket etc. to the output.
					localBuffer.append(inputBuffer.substring(startPos, pos + 1));
					inputBuffer.delete(startPos, pos + 1);
					pos = -1;
					state = 2;
				}
				break;
			case 6: // In a DTD
				if (c == 'C') {
					// Opening of the DTD (most likely).
					state = 7;
				} else {
					// Opening of some other tag. OK, now we can add the open
					// bracket etc. to the output.
					localBuffer.append(inputBuffer.substring(startPos, pos + 1));
					inputBuffer.delete(startPos, pos + 1);
					pos = -1;
					state = 2;
				}
				break;
			case 7: // In a DTD
				if (c == 'T') {
					// Opening of the DTD (most likely).
					state = 8;
				} else {
					// Opening of some other tag. OK, now we can add the open
					// bracket etc. to the output.
					localBuffer.append(inputBuffer.substring(startPos, pos + 1));
					inputBuffer.delete(startPos, pos + 1);
					pos = -1;
					state = 2;
				}
				break;
			case 8: // In a DTD
				if (c == 'Y') {
					// Opening of the DTD (most likely).
					state = 9;
				} else {
					// Opening of some other tag. OK, now we can add the open
					// bracket etc. to the output.
					localBuffer.append(inputBuffer.substring(startPos, pos + 1));
					inputBuffer.delete(startPos, pos + 1);
					pos = -1;
					state = 2;
				}
				break;
			case 9: // In a DTD
				if (c == 'P') {
					// Opening of the DTD (most likely).
					state = 10;
				} else {
					// Opening of some other tag. OK, now we can add the open
					// bracket etc. to the output.
					localBuffer.append(inputBuffer.substring(startPos, pos + 1));
					inputBuffer.delete(startPos, pos + 1);
					pos = -1;
					state = 2;
				}
				break;
			case 10: // In a DTD
				if (c == 'E') {
					// Opening of the DTD.
					state = 11;
				} else {
					// Opening of some other tag. OK, now we can add the open
					// bracket etc. to the output.
					localBuffer.append(inputBuffer.substring(startPos, pos + 1));
					inputBuffer.delete(startPos, pos + 1);
					pos = -1;
					state = 2;
				}
				break;
			case 11: // In a DTD (for sure now)
				if (c == '[') {
					// DTD has internal subset
					state = 12;
				} else if (c == '>') {
					// Closing the DTD.
					startDTDPos = startPos;
					endDTDPos = pos + 1;
					state = 13;
				}
				break;
			case 12: // In a DTD internal subset
				if (c == ']') {
					// Back to just DTD
					state = 11;
				}
				break;
			case 13: // Not in any tag (after finding the DTD)
				if (c == '<') {
					// Opening of some tag. Don't add to the output just yet
					startPos = pos;
					state = 14;
				}
				break;
			case 14: // In some (unknown tag) (after finding the DTD)
				if (c == '?') {
					// Opening of a PI.
					state = 15;
				} else if (c == '!') {
					// Opening of a comment
					state = 15;
				} else {
					// Must be opening of the document element (by elimination)
					state = 4;
				}
				break;
			case 15: // In a PI or comment tag (after finding the DTD)
				if (c == '>') {
					// Close the tag
					state = 0;
				}
				break;
			case 16: // No longer filtering
				// Add whatever it is (should be whitespace) to output
				localBuffer.append(c);
				inputBuffer.deleteCharAt(pos);
				pos = -1;
				break;
			}
		}
	}

	/**
	 * Handles manipulating the doctype data in the specified StringBuilder
	 * buffer. Returns the emptied StringBuilder instance after the manipulations
	 * are complete.
	 * 
	 * @param dtdBuffer
	 *          StringBuilder containing the doctype element and document element
	 *          data
	 * @return emptied StringBuilder instance
	 */
	protected void handleDTD(int startDocElem, int endDocElem) throws IOException {
		if (mode == REMOVE) {
			if (startDTDPos != -1) {
				inputBuffer.delete(startDTDPos, endDTDPos);
			}
		} else if (mode == REMOVE_MODIFY) {
			if (startDTDPos != -1) {
				String newDTD = replaceId(inputBuffer.substring(startDTDPos, endDTDPos));
				inputBuffer.delete(startDTDPos, endDTDPos);
				inputBuffer.insert(startDTDPos, newDTD);
			}
		} else if (mode == READ_ADD) {
			if (startDTDPos != -1) {
				replaceId(inputBuffer.substring(startDTDPos, endDTDPos));
			} else {
				String newDTD = "<!DOCTYPE " + readElementName(inputBuffer.substring(startDocElem, endDocElem)) + " SYSTEM \"" + CUSTOM_DTD + "\">";
				inputBuffer.insert(startDocElem, newDTD);
			}
		} else if (mode == NO_CHANGE) {
			// Don't change anything
		}
	}

	/**
	 * Reads the next xml element name from the specified string and returns it.
	 * 
	 * @param str
	 *          String to parse
	 * @return next xml element name
	 */
	protected String readElementName(String str) {
		StringBuilder buffy = new StringBuilder();
		boolean readingName = false;
		for (int i = str.indexOf('<') + 1; i < str.length(); i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				buffy.append(str.charAt(i));
				readingName = true;
			} else {
				if (readingName)
					break;
			}
		}
		return buffy.toString();
	}

	/**
	 * Replaces or removes the value of the SYSTEM/PUBLIC identifier in the
	 * specified <!DOCTYPE ...> String with an internal identifier (see
	 * ValidationInputSource.CUSTOM_DTD). Returns the updated <!DOCTYPE ...>
	 * String.
	 * 
	 * @param str
	 *          <!DOCTYPE ...> String to alter
	 * @return altered <!DOCTYPE ...> String
	 */
	private String replaceId(String str) throws IOException {
		char c = ' ';
		int localState = 0;
		StringBuilder buffy = new StringBuilder();
		str = str.trim();
		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(i);
			switch (localState) {
			case 0: // Reading the <!DOCTYPE token
				if (Character.isWhitespace(c))
					localState = 1;
				buffy.append(c);
				break;
			case 1: // Reading the name token
				if (Character.isWhitespace(c))
					localState = 2;
				buffy.append(c);
				break;
			case 2: // Reading either SYSTEM or PUBLIC or [ or >
				if (c == 'S') {
					if (str.length() > i + 6 && str.substring(i, i + 6).equals("SYSTEM")) {
						// Remove or replace this SYSTEM identifier
						for (int x = i + 6; x < str.length(); x++) {
							c = str.charAt(x);
							if (Character.isWhitespace(c)) {
								continue;
							} else if (c == '\'' || c == '"') {
								if (mode == READ_ADD) {
									// Replace and read
									String existingSysId = str.substring(x + 1, str.indexOf(c, x + 1));
									if (this.listener != null)
										this.listener.setDTD(null, existingSysId);
									buffy.append("SYSTEM ");
									buffy.append(c);
									buffy.append(CUSTOM_DTD);
									buffy.append(c);
									buffy.append(str.substring(str.indexOf(c, x + 1) + 1));
									return buffy.toString(); // Done!
								} else if (mode == REMOVE_MODIFY) {
									// Remove
									buffy.append(str.substring(str.indexOf(c, x + 1) + 1));
									return buffy.toString(); // Done!
								} else {
									// Should never reach here
									throw new IOException("Invalid DTD Filter mode: " + mode + ". Expecting ADD_MODIFY (" + READ_ADD + ") or REMOVE_MODIFY (" + REMOVE_MODIFY + ").");
								}
							} else {
								throw new IOException("Invalid doctype declaration. Expecting quoted SYSTEM " + "literal: " + str);
							}
						}
					} else {
						throw new IOException("Invalid doctype declaration. Expecting SYSTEM identifier: " + str);
					}
				} else if (c == 'P') {
					if (str.length() > i + 6 && str.substring(i, i + 6).equals("PUBLIC")) {
						// Remove or replace this PUBLIC identifier
						boolean tookCareOfPubId = false;
						for (int x = i + 6; x < str.length(); x++) {
							c = str.charAt(x);
							if (Character.isWhitespace(c)) {
								// Just continue
							} else if (c == '\'' || c == '"') {
								if (!tookCareOfPubId) {
									// Eat the first quoted string
									x = str.indexOf(c, x + 1);
									tookCareOfPubId = true;
									// Continue on
								} else {
									if (mode == READ_ADD) {
										// Replace and read
										String existingPubId = str.substring(x + 1, str.indexOf(c, x + 1));
										if (this.listener != null)
											this.listener.setDTD(existingPubId, null);
										buffy.append("SYSTEM ");
										buffy.append(c);
										buffy.append(CUSTOM_DTD);
										buffy.append(c);
										buffy.append(str.substring(str.indexOf(c, x + 1) + 1));
										return buffy.toString(); // Done!
									} else if (mode == REMOVE_MODIFY) {
										// Remove
										buffy.append(str.substring(str.indexOf(c, x + 1) + 1));
										return buffy.toString(); // Done!
									} else {
										// Should never reach here
										throw new IOException("Invalid DTD Filter mode: " + mode + ". Expecting ADD_MODIFY (" + READ_ADD + ") or REMOVE_MODIFY (" + REMOVE_MODIFY + ").");
									}
								}
							} else {
								throw new IOException("Invalid doctype declaration. Expecting quoted PUBLIC " + "literal: " + str);
							}
						}
					} else {
						throw new IOException("Invalid doctype declaration. Expecting PUBLIC identifier: " + str);
					}
				} else if (c == '[' || c == '>') {
					buffy.append(str.substring(i));
					return buffy.toString();
				} else if (Character.isWhitespace(c)) {
					buffy.append(c);
				} else {
					throw new IOException("Invalid doctype declaration. Expecting SYSTEM/PUBLIC identifier or " + "entity references, or ], or >: " + str);
				}
				break;
			default: // Should not reach here
				throw new IOException("Invalid doctype declaration. Expecting SYSTEM/PUBLIC identifier or " + "entity references, or ], or >: " + str);
			}
		}
		return buffy.toString();
	}

	/**
	 * Sets the DTDListener for this DTDFilterReader.
	 * 
	 * @param list
	 *          DTDListener for this DTDFilterReader
	 */
	public void setListener(DTDListener list) {
		this.listener = list;
	}

	/**
	 * Callback interface for objects interested in the DTD from the filtered xml
	 * document.
	 * 
	 * @author mattj
	 * 
	 */
	public interface DTDListener {
		/**
		 * Sets the public id and system id from the read <!DOCTYPE ...> element.
		 * 
		 * @param publicId
		 *          public id from the <!DOCTYPE ...> element
		 * @param systemId
		 *          system id from the <!DOCTYPE ...> element
		 */
		public void setDTD(String publicId, String systemId);
	}
}
