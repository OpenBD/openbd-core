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
 *  $Id: $
 */

package com.nary.net.http;

/**
 * urlResolverInputStream
 *
 * this class uses a tagFilterInputStream to get all the tags from
 * a stream of html sourced from a filterInputStream.
 * In order to use the tagFilterInputStream an adapter class is used to 
 * 'convert' the filterInputStream to an InputStream.
 * Meanwhile, this class is used by httpConnection to resolve relative urls
 * to absolute ones.
 *
 */

import com.nary.net.tagFilterInputStream;

public class urlResolverInputStream implements filterInputStream {

	// the source of input for this stream
	// private filterInputStream in;
	private tagFilterInputStream in = null;

	private String url;

	private int port;

	urlResolverInputStream(String _url, int _port) {
		url = _url;
		port = _port;
	}// urlResolverInputStream()

	synchronized public void setInputStream(filterInputStream _stream) {
		in = new tagFilterInputStream(new inputStreamAdapter(_stream));
		in.registerTagListener(new tagProcessor(url, port));
	}// setInputStream()

	public int readChar() throws java.io.IOException {
		return in.readChar();
	}// readChar()

	class inputStreamAdapter extends java.io.InputStream {

		filterInputStream in;

		inputStreamAdapter(filterInputStream _filterInputStream) {
			in = _filterInputStream;
		}// inputStream()

		public int read() throws java.io.IOException {
			return in.readChar();
		}// read()

	}// inputStreamAdapter

}// urlResolverInputStream
