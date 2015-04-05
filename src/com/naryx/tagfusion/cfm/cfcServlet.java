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

package com.naryx.tagfusion.cfm;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.encoding.TypeMappingImpl;
import org.apache.axis.transport.http.AxisServlet;
import org.apache.axis.transport.http.HTTPTransport;

import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfUrlData;

public class cfcServlet extends AxisServlet {
	private static final long serialVersionUID = 1L;

	public cfcServlet() {}

	public void init() throws ServletException {
		super.init();
		getOption(getServletConfig().getServletContext(), INIT_PROPERTY_TRANSPORT_NAME, HTTPTransport.DEFAULT_TRANSPORT_NAME);
	}

	/**
	 * Process GET requests. This includes handoff of pseudo-SOAP requests
	 *
	 * @param request
	 *          request in
	 * @param response
	 *          request out
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String queryString = null;
		cfSession _session = (cfSession) request.getAttribute(cfSession.ATTR_NAME); //see CfmlPageContext.include()

		if(_session != null)
			queryString = cfUrlData.getQueryString(_session); //this fixes NA#3281
		else
			queryString = request.getQueryString();
		
		if (queryString == null || queryString.equals("")) {
			// We can print out MCDL of the cfc
			renderMCDL(request, response);
		} else {
			// Ensure that Axis honors this setting when generating WSDL
			if (!TypeMappingImpl.dotnet_soapenc_bugfix)
				TypeMappingImpl.dotnet_soapenc_bugfix = true;

			boolean wsdlReq = queryString.equalsIgnoreCase("wsdl");
			boolean listReq = queryString.equalsIgnoreCase("list");
			boolean hasParams = request.getParameterNames().hasMoreElements();
			if (!wsdlReq && !listReq && hasParams) {
				// Use the params to invoke a method (if possible)
				// useParamsForSOAP(request, response); // replaced by following call to
				// fix bug #1488
				cfEngine.serviceCfcMethod(request, response);
			} else {
				// Let the superclass process it
				super.doGet(request, response);
			}
		}
	}

	protected void renderMCDL(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Respond with a 403
		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Ensure that Axis honors this setting when generating WSDL
		if (!TypeMappingImpl.dotnet_soapenc_bugfix)
			TypeMappingImpl.dotnet_soapenc_bugfix = true;

		String ct = request.getContentType();

		// If the content type is "application/x-www-form-urlencoded" then we need
		// to invoke the CFC method
		// as if it was directly invoked by a cfinvoke tag. This fixes bug #1488.
		// We also need to invoke it if the request is a POST but no SoapAction header is specified
		if ( ( (ct != null) && (ct.indexOf("application/x-www-form-urlencoded") == 0 ) )
				|| request.getHeader( "soapaction" ) == null )
			cfEngine.serviceCfcMethod(request, response);
		else{
			super.doPost(request, response);
		}

	}

	/**
	 * when we get an exception or an axis fault in a GET, we handle it almost
	 * identically: we go 'something went wrong', set the response code to 500 and
	 * then dump info. But we dump different info for an axis fault or subclass
	 * thereof.
	 *
	 * @param exception
	 *          what went wrong
	 * @param response
	 *          current response
	 * @param writer
	 *          open writer to response
	 */
	protected void reportTroubleInGet(Throwable exception, HttpServletResponse response, PrintWriter writer) {
		// We'll already have reported it.
	}

}
