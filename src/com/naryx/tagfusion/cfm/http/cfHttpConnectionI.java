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
 *  http://www.openbluedragon.org/
 */

package com.naryx.tagfusion.cfm.http;

import java.io.File;
import java.io.IOException;

import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public interface cfHttpConnectionI {

	public void setThrowOnError(boolean boolean1);
	public void setMethod(String methodStr, boolean boolean1) throws cfmRunTimeException;
	public void setURL(String url, int port) throws cfmRunTimeException ;
	public void setProxyServer(String proxyserver, int proxyPort);
	public void setUserAgent(String string);
	public void authenticate(String string, String string2);
	public void setFollowRedirects(boolean boolean1);
	public void authenticateProxy(String string, String string2);
	public void setFile(File file);
	public void setCharset(String string) throws cfmRunTimeException;

	public void setQueryDetails(String _name, String _cols, String _textqualifier, String _delimiter, boolean _first);

	public void setGetAsBinary(String lowerCase);

	public void setResolveLinks(boolean boolean1);

	public void connect() throws cfmRunTimeException ;

	public void close() throws IOException ;

	public void setTimeout(int i);
}
