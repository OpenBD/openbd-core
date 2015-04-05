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

/*
 * Created on Jan 8, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.naryx.tagfusion.cfm.xml.ws.dynws;

import java.io.Serializable;

/**
 * Value object to hold operation and parameter information for web service
 * Stubs.
 */
public class StubInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	protected StubInfo.Operation[] operations = null;

	protected String lName = null;
	protected String sName = null;
	protected String portName = null;
	protected String wsdlSum = null;

	public StubInfo() {}

	public StubInfo(String lName, String sName, String wsdlSum, String portName, StubInfo.Operation[] operations) {
		this.lName = lName;
		this.sName = sName;
		this.portName = portName;
		this.operations = operations;
		this.wsdlSum = wsdlSum;
	}

	public String getLocatorName() {
		return this.lName;
	}

	public void setLocatorName(String pVal) {
		this.lName = pVal;
	}

	public String getStubName() {
		return this.sName;
	}

	public void setStubName(String pVal) {
		this.sName = pVal;
	}

	public String getWSDLSum() {
		return this.wsdlSum;
	}

	public void setWSDLSum(String pVal) {
		this.wsdlSum = pVal;
	}

	public String getPortName() {
		return this.portName;
	}

	public void setPortName(String pVal) {
		this.portName = pVal;
	}

	public StubInfo.Operation[] getOperations() {
		return this.operations;
	}

	public void setOperations(StubInfo.Operation[] pVal) {
		this.operations = pVal;
	}

	
	public static class Operation implements Serializable {
		private static final long serialVersionUID = 1L;

		protected String name = null;

		protected StubInfo.Parameter[] parameters = null;

		protected StubInfo.Parameter[] subParameters = null;

		public Operation() {
		}

		public Operation(String name, StubInfo.Parameter[] parms) {
			this.name = name;
			this.parameters = parms;
		}

		public String getName() {
			return this.name;
		}

		public void setName(String pVal) {
			this.name = pVal;
		}

		public StubInfo.Parameter[] getParameters() {
			return this.parameters;
		}

		public void setParameters(StubInfo.Parameter[] pVal) {
			this.parameters = pVal;
		}

		public StubInfo.Parameter[] getSubParameters() {
			return this.subParameters;
		}

		public void setSubParameters(StubInfo.Parameter[] pVal) {
			this.subParameters = pVal;
		}
	}

	
	public static class Parameter implements Serializable {
		private static final long serialVersionUID = 1L;

		protected String name = null;

		protected boolean nillable = false;

		protected boolean omittable = false;

		public Parameter() {
		}

		public Parameter(String name, boolean nillable, boolean omittable) {
			this.name = name;
			this.omittable = omittable;
			this.nillable = nillable;
		}

		public String getName() {
			return this.name;
		}

		public void setName(String pVal) {
			this.name = pVal;
		}

		public boolean getNillable() {
			return this.nillable;
		}

		public void setNillable(boolean pVal) {
			this.nillable = pVal;
		}

		public boolean getOmittable() {
			return this.omittable;
		}

		public void setOmittable(boolean pVal) {
			this.omittable = pVal;
		}
	}
}
