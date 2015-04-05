/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  $Id: UrlLinkResolver.java 1713 2011-10-05 22:28:59Z alan $
 */

package com.nary.net.http;

import java.io.ByteArrayOutputStream;

/**
 * urlUtils
 * 
 * provides methods useful in the context of URLs
 * 
 */
public class UrlLinkResolver extends Object {

	private ByteArrayOutputStream outputStream;
	private int stepBackLimit;
	
	
	public UrlLinkResolver() {
		outputStream = new ByteArrayOutputStream();
	}

	/**
	 * given a base URL and source URL, return the actual URL got from these 2
	 * 
	 * @param baseURL
	 *          - the URL got from the <BASE> tag
	 * @param sourceURL
	 *          - the URL of the current location
	 */

	public String encode(String baseURL, String sourceURL) {
		outputStream.reset();
		stepBackLimit = 0;
		
		int nextIndex; // used to hold index of next char in the buffer

		// is baseURI relative, or absolute?

		// absolute
		if (baseURL.startsWith("http://") || baseURL.startsWith("https://")) {
			return baseURL;
		} else {
			// relative
			// create combined URL that includes all ./'s and ..'s
			String fullURL;

			// if base url is a root url
			if (baseURL.startsWith("/") || baseURL.startsWith("\\")) {
				// combine the 2 urls - taking the source uri from the start to the third
				// '/'
				fullURL = (sourceURL.substring(0, (sourceURL.indexOf('/', 7))) + baseURL).replace('\\', '/');
			} else {
				// else just combine the 2 urls - source first so the combined str = http....
				fullURL = (sourceURL + baseURL).replace('\\', '/');
			}

			byte[] urlCopy = fullURL.getBytes();
			// System.out.println("Full URL : " + new String(urlCopy));

			int index = 0;

			index = readNext(urlCopy, index); // read 'http:/'
			index = readNext(urlCopy, index); // read '/'
			index = readNext(urlCopy, index); // read up to next '/' -- this is the step back limit

			stepBackLimit = index - 1; // this is the place where you cannot .. back anymore

			while (index < urlCopy.length) {
				nextIndex = index + 1;

				if (urlCopy[index] == '.') {

					if (nextIndex == urlCopy.length) {
						// if there are no more chars
						index++;
					} else if (urlCopy[nextIndex] == '/') {
						// else if currently looking at "./" then ignore it (i.e. don't write it
						// to the output stream
						index = nextIndex + 1; // move to next char after the "./"
					} else if (urlCopy[nextIndex] == '.') {
						// else if currently looking at ".." then need to step back a directory
						// in the url
						stepBack();
						index = nextIndex + 2; // move to next char after the ".."
					} else {
						index++;
					}

				} else if (urlCopy[index] == '/') {
					// else if currently looking at "//" then ignore the 2nd /
					index = index + 1; // move to next char after the "//"
				} else {
					index = readNext(urlCopy, index);
				}

			}// while
		}

		return new String(outputStream.toString());
	}

	
	
	/**
	 * read the next part of the URL into the byteArrayOutputStream. start the read from position 'start' in 'buffer'
	 */
	private int readNext(byte[] buffer, int start) {
		int currIndex = start;
		while (currIndex < buffer.length && buffer[currIndex] != '/') {
			outputStream.write(buffer[currIndex]);
			currIndex++;
		}
		if (currIndex != buffer.length) {
			outputStream.write(buffer[currIndex]);
			currIndex++;
		}

		return currIndex;
	}

	

	private void stepBack() {
		
		// convert current stored url to byte[]
		byte[] temp = outputStream.toByteArray();

		// if can't step back anymore
		if (temp.length - 1 == stepBackLimit) {
			return;
		}

		// reset the output stream. going to copy back part of temp at the end
		outputStream.reset();

		// move back to before the current '/'
		int currIndex = temp.length - 2;

		while ((currIndex > stepBackLimit) && temp[currIndex] != '/') {
			currIndex--;
		}

		if (currIndex == stepBackLimit) {
			// stepBackLimit reached
			for (int i = 0; i <= stepBackLimit; i++) {
				outputStream.write(temp[i]);
			}
		} else {
			for (int j = 0; j <= currIndex; j++) {
				outputStream.write(temp[j]);
			}
		}

	}// stepBack

}
