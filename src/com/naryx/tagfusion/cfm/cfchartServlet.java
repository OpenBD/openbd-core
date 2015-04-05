/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.file.cfFile;
import com.naryx.tagfusion.cfm.file.cfmlURI;
import com.naryx.tagfusion.cfm.tag.awt.cfCHART;

public class cfchartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static ServletContext thisServletContext;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		thisServletContext = config.getServletContext();
	}

	public static void staticInit(ServletConfig config) throws ServletException {
		thisServletContext = config.getServletContext();
	}

	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		staticService(request, response);
	}

	public static void staticService(HttpServletRequest request, HttpServletResponse response) throws IOException {
		switch (cfCHART.getStorage()) {
		case cfCHART.FILE:
			sendChartFromFile(request, response);
			return;
		case cfCHART.SESSION:
			sendChartFromSession(request, response);
			return;
		case cfCHART.DB:
			sendChartFromDB(request, response);
			return;
		default:
			throw new IllegalStateException("illegal CFCHART storage type - " + cfCHART.getStorage());
		}
	}

	private static void sendChartFromFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Retrieve the filename
		String filename = request.getQueryString();

		File f = new File(cfCHART.getCfchartDirectory(), filename);

		// Set the content-type
		response.setContentType("image/jpeg");

		// Set the content-length
		response.setContentLength((int) f.length());

		// Serve up the file
		FileInputStream in = new FileInputStream(f);
		OutputStream out = response.getOutputStream();
		byte data[] = new byte[4096];
		int num;

		while ((num = in.read(data, 0, 4096)) != -1)
			out.write(data, 0, num);
		out.flush();

		// Close the file input stream
		in.close();

		// Delete the file
		if (cfCHART.getStorageCacheSize() == 0)
			f.delete();
	}

	private static void sendChartFromSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Create a BD session object
		cfSession _Session = new cfSession(request, response, thisServletContext);

		// Configure the application by creating a cfapplication tag and rendering
		// it
		String appName = request.getParameter("appName");
		StringReader sr;
		if (appName.length() > 0)
			sr = new StringReader("<cfapplication name=\"" + appName + "\" sessionmanagement=\"yes\">");
		else
			sr = new StringReader("<cfapplication sessionmanagement=\"yes\">");
		try {
			cfFile stringFile = new cfFile(new cfmlURI(""), sr, "UTF-8");
			stringFile.renderToString(_Session);
		} catch (cfmRunTimeException e) {
			return;
		}

		// Retrieve the chart name
		String chartName = request.getParameter("chartName");

		// Retrieve the chart data from the session scope
		cfData sessionScope = _Session.getQualifiedData(variableStore.SESSION_SCOPE);
		cfBinaryData data = (cfBinaryData) sessionScope.getData(chartName);
		byte[] bytes = data.getByteArray();

		// Set the content-type
		response.setContentType("image/jpeg");

		// Set the content-length
		response.setContentLength(bytes.length);

		// Serve up the bytes
		response.getOutputStream().write(bytes, 0, bytes.length);

		// Remove the chart data from the session scope
		if (cfCHART.getStorageCacheSize() == 0) {
			try {
				sessionScope.deleteData(chartName);
			} catch (cfmRunTimeException e) {
			}
		}
	}

	private static void sendChartFromDB(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Retrieve the chart name
		String chartName = request.getQueryString();

		// Retrieve the chart data from the database
		byte[] bytes = null;
		Connection con = null;
		java.sql.PreparedStatement Statmt = null;
		java.sql.ResultSet rs = null;
		try {
			con = cfCHART.getStorageDataSource().getConnection();
			Statmt = con.prepareStatement(cfCHART.SQL_SELECT);
			Statmt.setString(1, chartName);
			rs = Statmt.executeQuery();
			rs.next();
			bytes = rs.getBytes(1);
		} catch (SQLException E) {
			cfEngine.log("Error retrieving data from BDCHARTDATA table: " + E);
			return;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
				}
			if (Statmt != null)
				try {
					Statmt.close();
				} catch (SQLException e) {
				}
			if (con != null)
				try {
					con.close();
				} catch (SQLException ignore) {
				}
		}

		// Set the content-type
		response.setContentType("image/jpeg");

		// Set the content-length
		response.setContentLength(bytes.length);

		// Serve up the bytes
		response.getOutputStream().write(bytes, 0, bytes.length);

		// Remove the chart data from the database
		if (cfCHART.getStorageCacheSize() == 0) {
			con = null;
			Statmt = null;
			try {
				con = cfCHART.getStorageDataSource().getConnection();
				Statmt = con.prepareStatement(cfCHART.SQL_DELETE);
				Statmt.setString(1, chartName);
				Statmt.executeUpdate();
			} catch (SQLException E) {
				cfEngine.log("Error deleting data from BDCHARTDATA table: " + E);
				return;
			} finally {
				if (Statmt != null)
					try {
						Statmt.close();
					} catch (SQLException e) {
					}
				if (con != null)
					try {
						con.close();
					} catch (SQLException ignore) {
					}
			}
		}
	}
}
