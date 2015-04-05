/* 
 *  Copyright (C) 2000 - 2014 TagServlet Ltd
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
 *  $Id: htmlBody.java 2390 2013-06-23 16:34:12Z alan $
 */

package com.naryx.tagfusion.expression.function.system;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import com.nary.servlet.MultiPartUploadedFile;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDecodedInput;
import com.naryx.tagfusion.cfm.engine.cfFormData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.expression.function.functionBase;

public class httpDump extends functionBase {
	private static final long serialVersionUID = 1L;

	public httpDump() {}
	
	public java.util.Map getInfo(){
		return makeInfo(
				"system", 
				"Takes a snapshot of everything associated with the incoming request", 
				ReturnType.STRUCTURE );
	}

	
	public cfData execute( cfSession _session, List<cfData> parameters )throws cfmRunTimeException{
		cfStructData httpRequest = new cfStructData();
		
		// Do the header
		cfStructData headers = new cfStructData();
		Enumeration<String> E = _session.REQ.getHeaderNames();
		while (E.hasMoreElements()) {
			String hdr = E.nextElement();
			headers.setData(hdr, new cfStringData(_session.REQ.getHeader(hdr)));
		}

		httpRequest.setData("headers", headers);

		
		// Do the content
		cfFormData formData = (cfFormData) _session.getQualifiedData(variableStore.FORM_SCOPE);
		byte[] requestData = formData.getRequestData();
		if (requestData != null) {
			/*
			 * For content to be considered string data, the FORM request header
			 * "CONTENT_TYPE" must start with "text/" or be special case
			 * "application/x-www-form-urlencoded". Other types are stored as a binary
			 * object.
			 */
			if (_session.REQ.getContentType().indexOf("text") == -1 
					&& _session.REQ.getContentType().indexOf("application/x-www-form-urlencoded") == -1) {

				httpRequest.setData("content", new cfBinaryData(requestData));

			} else {
				String bodyCharset = _session.REQ.getCharacterEncoding();
				if (bodyCharset == null) {
					httpRequest.setData("content", new cfStringData(requestData));
				} else {
					try {
						httpRequest.setData("content", new cfStringData(new String(requestData, bodyCharset)));
					} catch (UnsupportedEncodingException e) {
						httpRequest.setData("content", new cfStringData(requestData));
					}
				}
			}
		}
		
		
		// Uploaded files
		cfDecodedInput DI = (cfDecodedInput) _session.getDataBin(cfDecodedInput.DATA_BIN_KEY);
		if ( DI != null ){
			cfArrayData	fileArray	= cfArrayData.createArray(1);
			
			Iterator<MultiPartUploadedFile> it = DI.getFiles().iterator();
			while ( it.hasNext() ){
				MultiPartUploadedFile	uF	= it.next();
				
				cfStructData file = new cfStructData();
				file.setData("filename", uF.filename );
				file.setData("form", uF.formName );
				file.setData("contenttype", uF.contentType );
				file.setData("length", uF.tempFile.length() );
				fileArray.addElement(file);
			}

			httpRequest.setData("files", fileArray );
		}

		if ( formData.size() > 0 )
			httpRequest.setData("form", formData );
		
		
		// Do the remote section
		cfStructData remote = new cfStructData();
		remote.setData("ip", _session.getRemoteIP() );
		remote.setData("host", _session.REQ.getRemoteHost() );
		remote.setData("method", _session.REQ.getMethod() );
		remote.setData("protocol", _session.REQ.getProtocol() );		
		remote.setData("uri", _session.REQ.getRequestURI() );
		remote.setData("query", _session.REQ.getQueryString() );

		httpRequest.setData("remote", remote);

		
		return httpRequest;
	}

}